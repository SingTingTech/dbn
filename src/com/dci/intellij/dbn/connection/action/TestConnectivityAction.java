package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class TestConnectivityAction extends AbstractConnectionAction {

    public TestConnectivityAction(ConnectionHandler connectionHandler) {
        super("Test connectivity", "Test connectivity of " + connectionHandler.getName(), null, connectionHandler);
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        final ConnectionHandler connectionHandler = getConnectionHandler();
        connectionHandler.getInstructions().setAllowAutoConnect(true);
        TaskInstructions taskInstructions = new TaskInstructions("Trying to connect to " + connectionHandler.getName(), false, false);
        new ConnectionAction("testing the connectivity", connectionHandler, taskInstructions) {
            @Override
            protected void execute() {
                ConnectionManager.testConnection(connectionHandler, true, true);
            }

            @Override
            protected boolean isManaged() {
                return true;
            }
        }.start();
    }
}
