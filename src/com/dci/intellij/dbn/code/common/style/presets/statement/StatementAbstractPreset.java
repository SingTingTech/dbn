package com.dci.intellij.dbn.code.common.style.presets.statement;

import com.dci.intellij.dbn.code.common.style.presets.CodeStylePresetImpl;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.psi.PsiElement;

public abstract class StatementAbstractPreset extends CodeStylePresetImpl {
    StatementAbstractPreset(String id, String name) {
        super(id, name);
    }

    @Override
    public boolean accepts(BasePsiElement psiElement) {
        return psiElement.elementType.is(ElementTypeAttribute.STATEMENT);
    }

    protected Spacing getSpacing(BasePsiElement psiElement, boolean shouldWrap) {
        if (shouldWrap) {
            return SPACING_ONE_LINE;
        } else {
            PsiElement previousPsiElement = psiElement.getPrevSibling();
            if (previousPsiElement instanceof TokenPsiElement) {
                TokenPsiElement previousToken = (TokenPsiElement) previousPsiElement;
                SharedTokenTypeBundle sharedTokenTypes = psiElement.getLanguage().getSharedTokenTypes();
                TokenType tokenType = previousToken.getTokenType();
                return tokenType ==  sharedTokenTypes.getChrLeftParenthesis() ?
                        SPACING_NO_SPACE :
                        SPACING_ONE_SPACE;

            }
            return SPACING_ONE_SPACE;
        }
    }
}