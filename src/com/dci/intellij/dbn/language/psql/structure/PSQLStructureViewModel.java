package com.dci.intellij.dbn.language.psql.structure;

import com.dci.intellij.dbn.language.common.structure.DBLanguageStructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.NodeProvider;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class PSQLStructureViewModel extends DBLanguageStructureViewModel {
    private static final Collection<NodeProvider> NODE_PROVIDERS = Arrays.<NodeProvider>asList(new ShowDetailsNodeProvider());


    private Sorter[] sorters = new Sorter[] {new PSQLStructureViewModelSorter()};
    private Grouper[] groupers = new Grouper[]{new PSQLStructureViewModelGrouper()};
    private Filter[] filters = new Filter[]{new PSQLStructureViewModelFilter()};

    public PSQLStructureViewModel(Editor editor, PsiFile psiFile) {
        super(editor, psiFile);
    }

    @NotNull
    public StructureViewTreeElement getRoot() {
        return new PSQLStructureViewElement(getPsiFile());
    }

    @NotNull
    public Grouper[] getGroupers() {
        return groupers;
    }

    @NotNull
    public Sorter[] getSorters() {
        return sorters;
    }

    @NotNull
    @Override
    public Filter[] getFilters() {
        return filters;
    }

    @NotNull
    @Override
    public Collection<NodeProvider> getNodeProviders() {
        return Collections.emptyList();//NODE_PROVIDERS;
    }
}