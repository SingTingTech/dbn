package com.dci.intellij.dbn.execution.statement.options.ui;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.common.options.TimeoutSettingsListener;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class StatementExecutionSettingsForm extends ConfigurationEditorForm<StatementExecutionSettings> {
    private JPanel mainPanel;
    private JTextField fetchBlockSizeTextField;
    private JTextField executionTimeoutTextField;
    private JCheckBox focusResultCheckBox;
    private JTextField debugExecutionTimeoutTextField;
    private JCheckBox promptExecutionCheckBox;

    public StatementExecutionSettingsForm(StatementExecutionSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        registerComponent(mainPanel);
    }

    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        StatementExecutionSettings settings = getConfiguration();
        settings.setResultSetFetchBlockSize(ConfigurationEditorUtil.validateIntegerInputValue(fetchBlockSizeTextField, "Fetch block size", true, 1, 10000, null));
        int executionTimeout = ConfigurationEditorUtil.validateIntegerInputValue(executionTimeoutTextField, "Execution timeout", true, 0, 6000, "\nUse value 0 for no timeout");
        int debugExecutionTimeout = ConfigurationEditorUtil.validateIntegerInputValue(debugExecutionTimeoutTextField, "Debug execution timeout", true, 0, 6000, "\nUse value 0 for no timeout");

        settings.setFocusResult(focusResultCheckBox.isSelected());
        settings.setPromptExecution(promptExecutionCheckBox.isSelected());

        boolean timeoutSettingsChanged = settings.setExecutionTimeout(executionTimeout);
        timeoutSettingsChanged = settings.setDebugExecutionTimeout(debugExecutionTimeout) || timeoutSettingsChanged;
        if (timeoutSettingsChanged) {
            SettingsChangeNotifier.register(() -> EventUtil.notify(getProject(), TimeoutSettingsListener.TOPIC).settingsChanged(ExecutionTarget.STATEMENT));
        }
    }

    @Override
    public void resetFormChanges() {
        StatementExecutionSettings settings = getConfiguration();
        fetchBlockSizeTextField.setText(Integer.toString(settings.getResultSetFetchBlockSize()));
        executionTimeoutTextField.setText(Integer.toString(settings.getExecutionTimeout()));
        debugExecutionTimeoutTextField.setText(Integer.toString(settings.getDebugExecutionTimeout()));
        focusResultCheckBox.setSelected(settings.isFocusResult());
        promptExecutionCheckBox.setSelected(settings.isPromptExecution());
    }
}
