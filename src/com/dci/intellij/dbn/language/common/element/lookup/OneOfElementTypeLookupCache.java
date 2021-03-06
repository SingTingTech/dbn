package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.OneOfElementType;

import java.util.Set;

public class OneOfElementTypeLookupCache extends ElementTypeLookupCacheIndexed<OneOfElementType> {
    public OneOfElementTypeLookupCache(OneOfElementType elementType) {
        super(elementType);
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementTypeBase source) {
        boolean notInitialized = !firstPossibleLeafs.contains(leaf);
        return notInitialized && (isWrapperBeginLeaf(leaf) || source.lookupCache.couldStartWithLeaf(leaf));
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementTypeBase source) {
        boolean notInitialized = !firstRequiredLeafs.contains(leaf);
        return notInitialized && source.lookupCache.shouldStartWithLeaf(leaf);
    }

    @Override
    public boolean checkStartsWithIdentifier() {
        for(ElementTypeRef child : elementType.getChildren()){
            if (child.getLookupCache().startsWithIdentifier()) return true;
        }
        return false;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, Set<LeafElementType> bucket) {
        bucket = super.collectFirstPossibleLeafs(context, bucket);
        ElementTypeRef[] elementTypeRefs = elementType.getChildren();
        for (ElementTypeRef child : elementTypeRefs) {
            if (context.check(child)) {
                bucket = child.elementType.lookupCache.collectFirstPossibleLeafs(context, bucket);
            }
        }
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, Set<TokenType> bucket) {
        bucket = super.collectFirstPossibleTokens(context, bucket);
        ElementTypeRef[] elementTypeRefs = elementType.getChildren();
        for (ElementTypeRef child : elementTypeRefs) {
            if (context.check(child)) {
                bucket = child.elementType.lookupCache.collectFirstPossibleTokens(context, bucket);
            }
        }
        return bucket;
    }
}