package com.dci.intellij.dbn.execution.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.openapi.vfs.VirtualFile;

public class LogOutputContext {
    public enum Status{
        NEW,
        ACTIVE,
        FINISHED,    // finished normally (or with error)
        STOPPED,     // interrupted by user
        CLOSED      // cancelled completely (console closed)
    }
    private ConnectionHandlerRef connectionHandlerRef;
    private VirtualFile sourceFile;
    private Process process;
    private Status status = Status.NEW;
    private boolean hideEmptyLines = false;

    public LogOutputContext(@NotNull ConnectionHandler connectionHandler) {
        this(connectionHandler, null, null);
    }

    public LogOutputContext(@NotNull ConnectionHandler connectionHandler, @Nullable VirtualFile sourceFile, @Nullable Process process) {
        this.connectionHandlerRef = connectionHandler.getRef();
        this.sourceFile = sourceFile;
        this.process = process;
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @Nullable
    public VirtualFile getSourceFile() {
        return sourceFile;
    }

    @Nullable
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public boolean isHideEmptyLines() {
        return hideEmptyLines;
    }

    public void setHideEmptyLines(boolean hideEmptyLines) {
        this.hideEmptyLines = hideEmptyLines;
    }

    public boolean matches(LogOutputContext context) {
        return getConnectionHandler() == context.getConnectionHandler() &&
                CommonUtil.safeEqual(getSourceFile(), context.getSourceFile());
    }

    public void start() {
        status = Status.ACTIVE;
    }

    public void finish() {
        if (isActive()) {
            status = Status.FINISHED;
        }
        destroyProcess();
    }


    public void stop() {
        if (isActive()) {
            status = Status.STOPPED;
        }
        destroyProcess();
    }


    public void close() {
        status = Status.CLOSED;
        destroyProcess();
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean isClosed() {
        return status == Status.CLOSED;
    }

    public boolean isStopped() {
        return status == Status.STOPPED;
    }


    private void destroyProcess() {
        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    public ConnectionId getConnectionId() {
        return connectionHandlerRef.getConnectionId();
    }
}
