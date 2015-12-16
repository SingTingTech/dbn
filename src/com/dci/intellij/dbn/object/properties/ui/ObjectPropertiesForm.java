package com.dci.intellij.dbn.object.properties.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventAdapter;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Disposer;

public class ObjectPropertiesForm extends DBNFormImpl<DBNForm> {
    private JPanel mainPanel;
    private JLabel objectLabel;
    private JLabel objectTypeLabel;
    private JTable objectPropertiesTable;
    private JScrollPane objectPropertiesScrollPane;
    private JPanel closeActionPanel;
    private DBObject object;

    public ObjectPropertiesForm(DBNForm parentForm) {
        super(parentForm);
        //ActionToolbar objectPropertiesActionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.Browser.ObjectProperties");
        //closeActionPanel.add(objectPropertiesActionToolbar.getComponent(), BorderLayout.CENTER);
        objectPropertiesTable.setRowHeight(objectPropertiesTable.getRowHeight() + 2);
        objectPropertiesTable.setRowSelectionAllowed(false);
        objectPropertiesTable.setCellSelectionEnabled(true);
        objectPropertiesScrollPane.getViewport().setBackground(objectPropertiesTable.getBackground());
        objectTypeLabel.setText("Object properties:");
        objectLabel.setText("(no object selected)");

        EventUtil.subscribe(getProject(), this, BrowserTreeEventListener.TOPIC, browserTreeEventListener);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    private BrowserTreeEventListener browserTreeEventListener = new BrowserTreeEventAdapter() {
        @Override
        public void selectionChanged() {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
            if (browserManager.getShowObjectProperties().value()) {
                DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
                if (activeBrowserTree != null) {
                    BrowserTreeNode treeNode = activeBrowserTree.getSelectedNode();
                    if (treeNode instanceof DBObject) {
                        DBObject object = (DBObject) treeNode;
                        setObject(object);
                    }
                }
            }
        }
    };

    public DBObject getObject() {
        return object;
    }

    public void setObject(final DBObject object) {
        if (!object.equals(this.object)) {
            this.object = object;

            new BackgroundTask(object.getProject(), "Rendering object properties", true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    final ObjectPropertiesTableModel tableModel = new ObjectPropertiesTableModel(object.getPresentableProperties());
                    Disposer.register(ObjectPropertiesForm.this, tableModel);

                    new SimpleLaterInvocator() {
                        @Override
                        protected void execute() {
                            objectLabel.setText(object.getName());
                            objectLabel.setIcon(object.getIcon());
                            objectTypeLabel.setText(NamingUtil.capitalize(object.getTypeName()) + ":");


                            ObjectPropertiesTableModel oldTableModel = (ObjectPropertiesTableModel) objectPropertiesTable.getModel();
                            objectPropertiesTable.setModel(tableModel);
                            ((DBNTable) objectPropertiesTable).accommodateColumnsSize();

                            mainPanel.revalidate();
                            mainPanel.repaint();
                            Disposer.dispose(oldTableModel);
                        }
                    }.start();
                }
            }.start();
        }
    }

    public void dispose() {
        super.dispose();
        object = null;
    }

    private void createUIComponents() {
        objectPropertiesTable = new ObjectPropertiesTable(null, new ObjectPropertiesTableModel());
        objectPropertiesTable.getTableHeader().setReorderingAllowed(false);
        Disposer.register(this, (Disposable) objectPropertiesTable);
    }
}
