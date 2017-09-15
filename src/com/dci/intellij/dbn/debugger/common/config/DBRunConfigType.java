package com.dci.intellij.dbn.debugger.common.config;

import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.intellij.execution.configurations.ConfigurationType;

public abstract class DBRunConfigType<T extends DBRunConfigFactory> implements ConfigurationType {
    public abstract T[] getConfigurationFactories();
    public abstract String getDefaultRunnerName();
    public abstract T getConfigurationFactory(DBDebuggerType debuggerType);

    public boolean isDumbAware() {
        return false;
    }
}
