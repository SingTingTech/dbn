package com.dci.intellij.dbn.browser;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.model.BrowserTreeModel;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.TabbedBrowserTreeModel;
import com.dci.intellij.dbn.browser.options.BrowserDisplayMode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.browser.options.ObjectFilterChangeListener;
import com.dci.intellij.dbn.browser.ui.BrowserToolWindowForm;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserForm;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.DisposableLazyValue;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

@State(
    name = DatabaseBrowserManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatabaseBrowserManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseBrowserManager";

    public static final String TOOL_WINDOW_ID = "DB Browser";

    private BooleanSetting autoscrollFromEditor = new BooleanSetting("autoscroll-from-editor", true);
    private BooleanSetting autoscrollToEditor   = new BooleanSetting("autoscroll-to-editor", false);
    private BooleanSetting showObjectProperties = new BooleanSetting("show-object-properties", true);
    public static final ThreadLocal<Boolean> AUTOSCROLL_FROM_EDITOR = new ThreadLocal<Boolean>();
    private LazyValue<BrowserToolWindowForm> toolWindowForm = new DisposableLazyValue<BrowserToolWindowForm>(this) {
        @Override
        protected BrowserToolWindowForm load() {
            return new BrowserToolWindowForm(getProject());
        }
    };

    private DatabaseBrowserManager(Project project) {
        super(project);
    }

    @Nullable
    public DatabaseBrowserTree getActiveBrowserTree() {
        return getToolWindowForm().getActiveBrowserTree();
    }

    @Nullable
    public ConnectionHandler getActiveConnection() {
        DatabaseBrowserTree activeBrowserTree = getActiveBrowserTree();
        if (activeBrowserTree != null) {
            BrowserTreeModel browserTreeModel = activeBrowserTree.getModel();
            if (browserTreeModel instanceof TabbedBrowserTreeModel) {
                TabbedBrowserTreeModel tabbedBrowserTreeModel = (TabbedBrowserTreeModel) browserTreeModel;
                return tabbedBrowserTreeModel.getConnectionHandler();
            }

            BrowserTreeNode browserTreeNode = activeBrowserTree.getSelectedNode();
            if (browserTreeNode != null) {
                return browserTreeNode.getConnectionHandler();
            }
        }

        return null;
    }

    @NotNull
    public ToolWindow getBrowserToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        return toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
    }

    @NotNull
    public BrowserToolWindowForm getToolWindowForm() {
        return toolWindowForm.get();
    }

    public BooleanSetting getAutoscrollFromEditor() {
        return autoscrollFromEditor;
    }

    public BooleanSetting getAutoscrollToEditor() {
        return autoscrollToEditor;
    }

    public BooleanSetting getShowObjectProperties() {
        return showObjectProperties;
    }

    public String toString() {
        return "DB Browser";
    }

    public void navigateToElement(@Nullable final BrowserTreeNode treeNode, final boolean focus, final boolean scroll) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                ToolWindow toolWindow = getBrowserToolWindow();

                toolWindow.show(null);
                if (treeNode != null) {
                    DatabaseBrowserForm browserForm = getToolWindowForm().getBrowserForm();
                    browserForm.selectElement(treeNode, focus, scroll);
                }
            }
        }.start();
    }

    public void navigateToElement(@Nullable final BrowserTreeNode treeNode, final boolean scroll) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                if (treeNode != null) {
                    DatabaseBrowserForm browserForm = getToolWindowForm().getBrowserForm();
                    browserForm.selectElement(treeNode, false, scroll);
                }
            }
        }.start();
    }

    public boolean isVisible() {
        ToolWindow toolWindow = getBrowserToolWindow();
        return toolWindow.isVisible();
    }

    /***************************************
     *     FileEditorManagerListener       *
     ***************************************/

    private boolean scroll() {
        return autoscrollFromEditor.value() && (AUTOSCROLL_FROM_EDITOR.get() == null || AUTOSCROLL_FROM_EDITOR.get());
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static DatabaseBrowserManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DatabaseBrowserManager.class);
    }

    @NonNls @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public void initComponent() {
        Project project = getProject();
        EventUtil.subscribe(project, this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        EventUtil.subscribe(project, this, ObjectFilterChangeListener.TOPIC, filterChangeListener);
    }

    public static void scrollToSelectedElement(final ConnectionHandler connectionHandler) {
        if (connectionHandler != null && !connectionHandler.isDisposed()) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(connectionHandler.getProject());
            BrowserToolWindowForm toolWindowForm = browserManager.getToolWindowForm();
            final DatabaseBrowserTree browserTree = toolWindowForm.getBrowserTree(connectionHandler);
            if (browserTree != null && browserTree.getTargetSelection() != null) {
                new SimpleLaterInvocator() {
                    @Override
                    protected void execute() {
                        browserTree.scrollToSelectedElement();
                    }
                }.start();
            }
        }
    }

    public boolean isTabbedMode() {
        DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(getProject());
        return browserSettings.getGeneralSettings().getDisplayMode() == BrowserDisplayMode.TABBED;
    }

    /**********************************************************
     *                       Listeners                        *
     **********************************************************/
    private ObjectFilterChangeListener filterChangeListener = new ObjectFilterChangeListener() {
        public void typeFiltersChanged(ConnectionId connectionId) {
            if (toolWindowForm.isLoaded()) {
                ConnectionHandler connectionHandler = getConnectionHandler(connectionId);
                if (connectionHandler == null) {
                    getToolWindowForm().getBrowserForm().rebuildTree();
                } else {
                    connectionHandler.getObjectBundle().rebuildTreeChildren();
                }
            }
        }

        @Override
        public void nameFiltersChanged(ConnectionId connectionId, @NotNull DBObjectType... objectTypes) {
            ConnectionHandler connectionHandler = getConnectionHandler(connectionId);
            if (toolWindowForm.isLoaded() && connectionHandler != null && objectTypes.length > 0) {
                connectionHandler.getObjectBundle().refreshTreeChildren(objectTypes);
            }
        }
    };

    @Nullable
    private ConnectionHandler getConnectionHandler(ConnectionId connectionId) {
        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        return connectionManager.getConnectionHandler(connectionId);
    }

    public Filter<BrowserTreeNode> getObjectTypeFilter() {
        DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(getProject());
        return browserSettings.getFilterSettings().getObjectTypeFilterSettings().getElementFilter();
    }

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerAdapter() {
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (scroll()) {
                if (file instanceof DBEditableObjectVirtualFile) {
                    DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
                    navigateToElement(databaseFile.getObject(), true);
                }
                else  if (file instanceof DBVirtualFileImpl) {
                    DBVirtualFileImpl databaseVirtualFile = (DBVirtualFileImpl) file;
                    ConnectionHandler connectionHandler = databaseVirtualFile.getConnectionHandler();
                    navigateToElement(connectionHandler.getObjectBundle(), false);
                }
            }
        }

        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            if (scroll()) {
                VirtualFile oldFile = event.getOldFile();
                VirtualFile newFile = event.getNewFile();

                if (oldFile == null || !oldFile.equals(newFile)) {
                    if (newFile instanceof DBEditableObjectVirtualFile) {
                        DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) newFile;
                        navigateToElement(databaseFile.getObject(), true);
                    }
                    else  if (newFile instanceof DBVirtualFileImpl) {
                        DBVirtualFileImpl databaseVirtualFile = (DBVirtualFileImpl) newFile;
                        ConnectionHandler connectionHandler = databaseVirtualFile.getConnectionHandler();
                        FileEditor oldEditor = event.getOldEditor();
                        DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
                        DBSchema currentSchema = databaseVirtualFile.getDatabaseSchema();
                        boolean scroll = oldEditor != null && oldEditor.isValid();
                        BrowserTreeNode treeNode = currentSchema == null ? objectBundle : currentSchema;
                        navigateToElement(treeNode, scroll);
                    }
                }
            }
        }
    };

    public void showObjectProperties(boolean visible) {
        BrowserToolWindowForm toolWindowForm = getToolWindowForm();
        if (visible)
            toolWindowForm.showObjectProperties(); else
            toolWindowForm.hideObjectProperties();
        showObjectProperties.setValue(visible);
    }

    public List<DBObject> getSelectedObjects() {
        List<DBObject> selectedObjects = new ArrayList<DBObject>();
        DatabaseBrowserTree activeBrowserTree = getActiveBrowserTree();
        if (activeBrowserTree != null) {
            TreePath[] selectionPaths = activeBrowserTree.getSelectionPaths();
            if (selectionPaths != null) {
                for (TreePath treePath : selectionPaths) {
                    Object lastPathComponent = treePath.getLastPathComponent();
                    if (lastPathComponent instanceof DBObject) {
                        DBObject object = (DBObject) lastPathComponent;
                        selectedObjects.add(object);
                    }
                }
            }
        }
        return selectedObjects;
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        autoscrollToEditor.writeConfiguration(element);
        autoscrollFromEditor.writeConfiguration(element);
        showObjectProperties.writeConfiguration(element);
        storeTouchedNodes(element);
        return element;
    }

    @Override
    public void loadState(final Element element) {
        autoscrollToEditor.readConfiguration(element);
        autoscrollFromEditor.readConfiguration(element);
        showObjectProperties.readConfiguration(element);
        initTouchedNodes(element);
    }

    private void storeTouchedNodes(Element element) {
        Element nodesElement = new Element("loaded-nodes");
        element.addContent(nodesElement);

        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        List<ConnectionHandler> connectionHandlers = connectionManager.getConnectionHandlers();
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            ConnectionDetailSettings settings = connectionHandler.getSettings().getDetailSettings();
            if (settings.isRestoreWorkspaceDeep()) {
                Element connectionElement = new Element("connection");

                boolean addConnectionElement = false;
                DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
                DBObjectList schemas = objectBundle.getObjectListContainer().getObjectList(DBObjectType.SCHEMA);
                if (schemas != null && schemas.isLoaded()) {
                    for (DBSchema schema : objectBundle.getSchemas()) {
                        List<DBObjectType> objectTypes = new ArrayList<DBObjectType>();
                        DBObjectListContainer childObjects = schema.getChildObjects();
                        if (childObjects != null) {
                            List<DBObjectList<DBObject>> allObjectLists = childObjects.getAllObjectLists();
                            for (DBObjectList<DBObject> objectList : allObjectLists) {
                                if (objectList.isLoaded() || objectList.isLoading()) {
                                    objectTypes.add(objectList.getObjectType());
                                }
                            }
                        }
                        if (objectTypes.size() > 0) {
                            Element schemaElement = new Element("schema");
                            schemaElement.setAttribute("name", schema.getName());
                            schemaElement.setAttribute("object-types", DBObjectType.toCommaSeparated(objectTypes));
                            connectionElement.addContent(schemaElement);
                            addConnectionElement = true;
                        }
                    }

                    if (addConnectionElement) {
                        connectionElement.setAttribute("connection-id", connectionHandler.getId().id());
                        nodesElement.addContent(connectionElement);
                    }
                }
            }
        }
    }


    private void initTouchedNodes(Element element) {
        Element nodesElement = element.getChild("loaded-nodes");
        if (nodesElement != null) {
            final Project project = getProject();
            List<Element> connectionElements = nodesElement.getChildren();
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            for (final Element connectionElement : connectionElements) {
                ConnectionId connectionId = ConnectionId.get(connectionElement.getAttributeValue("connection-id"));
                final ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(connectionId);
                if (connectionHandler != null) {
                    ConnectionDetailSettings settings = connectionHandler.getSettings().getDetailSettings();
                    if (settings.isRestoreWorkspaceDeep()) {
                        DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
                        String connectionString = " (" + connectionHandler.getName() + ")";
                        List<Element> schemaElements = connectionElement.getChildren();
                        for (final Element schemaElement : schemaElements) {
                            String schemaName = schemaElement.getAttributeValue("name");
                            final DBSchema schema = objectBundle.getSchema(schemaName);

                            if (schema != null) {
                                new BackgroundTask(project, "Loading data dictionary" + connectionString, true) {
                                    @Override
                                    protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                                        String objectTypesAttr = schemaElement.getAttributeValue("object-types");
                                        List<DBObjectType> objectTypes = DBObjectType.fromCommaSeparated(objectTypesAttr);
                                        for (DBObjectType objectType : objectTypes) {
                                            DBObjectListContainer childObjects = schema.getChildObjects();
                                            if (childObjects != null) {
                                                childObjects.loadObjectList(objectType);
                                            }
                                        }
                                    }
                                }.start();
                            }
                        }
                    }
                }
            }
        }
    }


}
