package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.ex.dummy.DummyFileIdGenerator;

public abstract class DBVirtualFileImpl extends VirtualFile implements DBVirtualFile, Presentable {
    private static AtomicInteger ID_STORE = new AtomicInteger(0);
    private int documentHashCode;
    private int id;
    protected String name;
    protected String path;
    protected String url;
    private ProjectRef projectRef;
    private VirtualFileSystem fileSystem = DatabaseFileSystem.getInstance();

    public DBVirtualFileImpl(Project project) {
        //id = ID_STORE.getAndIncrement();
        id = DummyFileIdGenerator.next();
        projectRef = new ProjectRef(project);
    }

    @NotNull
    @Override
    public EnvironmentType getEnvironmentType() {
        return getConnectionHandler().getEnvironmentType();
    }

    public int getDocumentHashCode() {
        return documentHashCode;
    }

    public void setDocumentHashCode(int documentHashCode) {
        this.documentHashCode = documentHashCode;
    }

    @Override
    public boolean isInLocalFileSystem() {
        return false;
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    @Nullable
    public Project getProject() {
        return projectRef.get();
    }

    public abstract Icon getIcon();

    public int getId() {
        return id;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public final String getPath() {
        if (path == null) {
            synchronized (this) {
                if (path == null) {
                    path = createPath();
                }
            }
        }
        return path;
    }

    @NotNull
    @Override
    public final String getUrl() {
        if (url == null) {
            synchronized (this) {
                if (url == null) {
                    url = createUrl();
                }
            }
        }
        return url;
    }

    @Override
    public void rename(Object requestor, @NotNull String newName) throws IOException {
        throw DatabaseFileSystem.READONLY_FILE_SYSTEM;
    }

    @Override
    public void move(Object requestor, @NotNull VirtualFile newParent) throws IOException {
        throw DatabaseFileSystem.READONLY_FILE_SYSTEM;
    }

    @NotNull
    @Override
    public VirtualFile copy(Object requestor, @NotNull VirtualFile newParent, @NotNull String copyName) throws IOException {
        throw DatabaseFileSystem.READONLY_FILE_SYSTEM;
    }

    @Override
    public void delete(Object requestor) throws IOException {
        throw DatabaseFileSystem.READONLY_FILE_SYSTEM;
    }

    @Override
    public final int hashCode() {
        return id;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof DBVirtualFileImpl && hashCode() == obj.hashCode();
    }

    @NotNull protected abstract String createPath();
    @NotNull protected abstract String createUrl();

    @Override
    public boolean isValid() {
        return !isDisposed() && getProject() != null;
    }

    private boolean disposed;

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        putUserData(FileDocumentManagerImpl.DOCUMENT_KEY, null);
    }

    @Override
    public void checkDisposed() {
        if (disposed) throw AlreadyDisposedException.INSTANCE;
    }
}
