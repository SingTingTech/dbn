package com.dci.intellij.dbn.language.editor.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import static com.dci.intellij.dbn.common.util.ActionUtil.getProject;
import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class ConsoleDeleteAction extends DumbAwareAction {
    ConsoleDeleteAction() {
        super("Delete console", "", Icons.ACTION_DELETE);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        if (project != null && virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
            DatabaseConsoleManager.getInstance(project).deleteConsole(consoleVirtualFile);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Delete Console");
        VirtualFile virtualFile = getVirtualFile(e);
        presentation.setEnabled(virtualFile instanceof DBConsoleVirtualFile);
    }


}