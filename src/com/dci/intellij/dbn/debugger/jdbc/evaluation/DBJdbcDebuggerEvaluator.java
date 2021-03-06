package com.dci.intellij.dbn.debugger.jdbc.evaluation;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.common.debug.VariableInfo;
import com.dci.intellij.dbn.debugger.common.evaluation.DBDebuggerEvaluator;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugValue;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.dci.intellij.dbn.debugger.jdbc.frame.DBJdbcDebugStackFrame;
import com.dci.intellij.dbn.debugger.jdbc.frame.DBJdbcDebugValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DBJdbcDebuggerEvaluator extends DBDebuggerEvaluator<DBJdbcDebugStackFrame, DBJdbcDebugValue> {

    public DBJdbcDebuggerEvaluator(DBJdbcDebugStackFrame frame) {
        super(frame);
    }

    @Override
    public void computePresentation(@NotNull DBJdbcDebugValue debugValue, @NotNull final XValueNode node, @NotNull XValuePlace place) {
        Set<String> childVariableNames = debugValue.getChildVariableNames();
        try {
            DBJdbcDebugProcess debugProcess = debugValue.getDebugProcess();
            String variableName = debugValue.getVariableName();
            DBDebugValue parentValue = debugValue.getParentValue();
            String databaseVariableName = parentValue == null ? variableName : parentValue.getVariableName() + "." + variableName;
            VariableInfo variableInfo = debugProcess.getDebuggerInterface().getVariableInfo(
                    databaseVariableName.toUpperCase(),
                    debugValue.getStackFrame().getFrameIndex(),
                    debugProcess.getDebugConnection());
            String value = variableInfo.getValue();
            String type = variableInfo.getError();

            if (value == null) {
                value = childVariableNames != null ? "" : "null";
            } else {
                if (!StringUtil.isNumber(value)) {
                    value = '\'' + value + '\'';
                }
            }

            if (type != null) {
                type = type.toLowerCase();
                value = "";
            }
            if (childVariableNames != null) {
                type = "record";
            }

            debugValue.setValue(value);
            debugValue.setType(type);
        } catch (Exception e) {
            debugValue.setValue("");
            debugValue.setType(e.getMessage());
        } finally {
            node.setPresentation(
                    debugValue.getIcon(),
                    debugValue.getType(),
                    CommonUtil.nvl(debugValue.getValue(), "null"),
                    childVariableNames != null);
        }
    }
}
