package com.dci.intellij.dbn.debugger.jdbc.process;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.dci.intellij.dbn.debugger.jdbc.config.DBStatementRunConfig;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;

public class DBStatementDebugProcess extends DBJdbcDebugProcess<StatementExecutionInput> {
    public DBStatementDebugProcess(@NotNull XDebugSession session, ConnectionHandler connectionHandler) {
        super(session, connectionHandler);
    }

    @Override
    protected void executeTarget() throws SQLException {
        StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(getProject());
        statementExecutionManager.debugExecute(getExecutionProcessor(), getTargetConnection());

    }

    @Override
    protected void registerDefaultBreakpoint() {
        DBStatementRunConfig runConfiguration = (DBStatementRunConfig) getSession().getRunProfile();
        if (runConfiguration != null) {
            List<DBMethod> methods = runConfiguration.getMethods();
            if (methods.size() > 0) {
                getBreakpointHandler().registerDefaultBreakpoint(methods.get(0));
            }
        }
    }

    public VirtualFile getRuntimeInfoFile(DebuggerRuntimeInfo runtimeInfo) {
        DBSchemaObject schemaObject = getDatabaseObject(runtimeInfo);
        return schemaObject == null ?
            getExecutionProcessor().getVirtualFile() :
            schemaObject.getVirtualFile();
    }
    private StatementExecutionProcessor getExecutionProcessor() {
        return getExecutionInput().getExecutionProcessor();
    }

    @NotNull
    @Override
    public String getName() {
        return getExecutionProcessor().getPsiFile().getName();
    }
    @Nullable
    @Override
    public Icon getIcon() {
        return getExecutionProcessor().getPsiFile().getIcon();
    }
}