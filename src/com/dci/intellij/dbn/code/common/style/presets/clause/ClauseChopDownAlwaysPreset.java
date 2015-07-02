package com.dci.intellij.dbn.code.common.style.presets.clause;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class ClauseChopDownAlwaysPreset extends ClauseAbstractPreset {
    public ClauseChopDownAlwaysPreset() {
        super("chop_down", "Chop down");
    }

    @Nullable
    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        return WRAP_ALWAYS;
    }

    @Nullable
    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        return SPACING_LINE_BREAK;
    }
}