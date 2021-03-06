package com.dci.intellij.dbn.editor;

import com.dci.intellij.dbn.code.common.intention.DatabaseConnectIntentionAction;
import com.dci.intellij.dbn.code.common.intention.DebugMethodIntentionAction;
import com.dci.intellij.dbn.code.common.intention.DebugStatementIntentionAction;
import com.dci.intellij.dbn.code.common.intention.ExecuteScriptIntentionAction;
import com.dci.intellij.dbn.code.common.intention.ExecuteStatementIntentionAction;
import com.dci.intellij.dbn.code.common.intention.ExplainPlanIntentionAction;
import com.dci.intellij.dbn.code.common.intention.JumpToExecutionResultIntentionAction;
import com.dci.intellij.dbn.code.common.intention.RunMethodIntentionAction;
import com.dci.intellij.dbn.code.common.intention.SelectConnectionIntentionAction;
import com.dci.intellij.dbn.code.common.intention.SelectSchemaIntentionAction;
import com.dci.intellij.dbn.code.common.intention.SelectSessionIntentionAction;
import com.dci.intellij.dbn.code.common.intention.ToggleDatabaseLoggingIntentionAction;
import com.intellij.codeInsight.intention.IntentionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class EditorManager implements ApplicationComponent{
    public static EditorManager getInstance() {
        return ApplicationManager.getApplication().getComponent(EditorManager.class);
    }
    @Override
    public void initComponent() {
        IntentionManager intentionManager = IntentionManager.getInstance();
        intentionManager.addAction(new ExecuteScriptIntentionAction());
        intentionManager.addAction(new ExecuteStatementIntentionAction());
        intentionManager.addAction(new DebugStatementIntentionAction());
        intentionManager.addAction(new RunMethodIntentionAction());
        intentionManager.addAction(new DebugMethodIntentionAction());
        intentionManager.addAction(new ExplainPlanIntentionAction());
        intentionManager.addAction(new DatabaseConnectIntentionAction());
        intentionManager.addAction(new JumpToExecutionResultIntentionAction());
        intentionManager.addAction(new SelectConnectionIntentionAction());
        intentionManager.addAction(new SelectSchemaIntentionAction());
        intentionManager.addAction(new SelectSessionIntentionAction());
        intentionManager.addAction(new ToggleDatabaseLoggingIntentionAction());
        //intentionManager.addAction(new SetupCodeCompletionIntentionAction());
    }

    @Override
    public void disposeComponent() { }

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.EditorManager";
    }
}
