package com.dci.intellij.dbn.language.common.element.util;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

import java.util.StringTokenizer;

public class ElementTypeAttributeHolder extends PropertyHolderImpl<ElementTypeAttribute>{

    @Override
    protected ElementTypeAttribute[] properties() {
        return ElementTypeAttribute.values();
    }

    public ElementTypeAttributeHolder(String definition) throws ElementTypeDefinitionException {
        StringTokenizer tokenizer = new StringTokenizer(definition, ",");
        while (tokenizer.hasMoreTokens()) {
            String attributeName = tokenizer.nextToken().trim();
            boolean found = false;
            for (ElementTypeAttribute attribute : ElementTypeAttribute.values()) {
                if (attribute.getName().equals(attributeName)) {
                    set(attribute, true);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ElementTypeDefinitionException("Invalid element type attribute '" + attributeName + "'");
            }
        }
    }
}
