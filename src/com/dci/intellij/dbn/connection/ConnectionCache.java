package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.util.EventUtil;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConnectionCache implements ApplicationComponent{
    private static Map<String, ConnectionHandler> CACHE = new THashMap<String, ConnectionHandler>();

    @Nullable
    public static ConnectionHandler findConnectionHandler(String connectionId) {
        ConnectionHandler connectionHandler = CACHE.get(connectionId);
        ProjectManager projectManager = ProjectManager.getInstance();
        if (connectionHandler == null && projectManager != null) {
            synchronized (ConnectionCache.class) {
                connectionHandler = CACHE.get(connectionId);
                if (connectionHandler == null) {
                    for (Project project : projectManager.getOpenProjects()) {
                        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                        connectionHandler = connectionManager.getConnectionHandler(connectionId);
                        if (connectionHandler != null && !connectionHandler.isDisposed()) {
                            CACHE.put(connectionId, connectionHandler);
                            return connectionHandler;
                        }
                    }
                }
            }
        }
        return connectionHandler == null || connectionHandler.isDisposed() ? null : connectionHandler;
    }


    @Override
    public void initComponent() {
        EventUtil.subscribe(null, ProjectLifecycleListener.TOPIC, projectLifecycleListener);
    }

    @Override
    public void disposeComponent() { }

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.ConnectionCache";
    }

    /*********************************************************
     *              ProjectLifecycleListener                 *
     *********************************************************/
    private ProjectLifecycleListener projectLifecycleListener = new ProjectLifecycleListener.Adapter() {

        @Override
        public void projectComponentsInitialized(Project project) {
            List<ConnectionHandler> connectionHandlers = ConnectionManager.getInstance(project).getConnectionHandlers();
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                CACHE.put(connectionHandler.getId(), connectionHandler);
            }
        }

        @Override
        public void afterProjectClosed(@NotNull Project project) {
            Iterator<String> connectionIds = CACHE.keySet().iterator();
            while (connectionIds.hasNext()) {
                String connectionId = connectionIds.next();
                ConnectionHandler connectionHandler = CACHE.get(connectionId);
                if (connectionHandler.isDisposed() || connectionHandler.getProject() == project) {
                    connectionIds.remove();
                }
            }

        }
    };
}
