package com.dci.intellij.dbn.editor.data.filter.ui;

import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterGroup;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;

public class DatasetFilterList extends JList {

    public DatasetFilterList() {
        super();
        setCellRenderer(LIST_CELL_RENDERER);
    }

    public DatasetFilterGroup getFilterGroup() {
        return (DatasetFilterGroup) getModel();
    }

    private static final ListCellRenderer LIST_CELL_RENDERER = new ColoredListCellRenderer(){
        @Override
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            DatasetFilter filter = (DatasetFilter) value;
            setIcon(filter.getIcon());
            append(filter.getVolatileName(), filter.isNew() ? 
                    SimpleTextAttributes.GRAY_ATTRIBUTES :
                    SimpleTextAttributes.REGULAR_ATTRIBUTES);

        }
    };
}
