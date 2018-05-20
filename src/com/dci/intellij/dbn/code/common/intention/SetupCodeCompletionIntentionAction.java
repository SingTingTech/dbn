package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SetupCodeCompletionIntentionAction extends GenericIntentionAction implements LowPriorityAction {
    @NotNull
    public String getText() {
        return "Setup code completion";
    }

    @NotNull
    public String getFamilyName() {
        return IntentionActionGroups.SETUP;
    }

    @Override
    public Icon getIcon(int flags) {
        return null;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return psiFile instanceof DBLanguagePsiFile && psiFile.getVirtualFile().getParent() != null;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
        settingsManager.openProjectSettings(ConfigId.CODE_COMPLETION);
    }

    public boolean startInWriteAction() {
        return false;
    }
}