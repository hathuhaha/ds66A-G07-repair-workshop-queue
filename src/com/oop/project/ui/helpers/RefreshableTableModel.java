package com.oop.project.ui.helpers;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class RefreshableTableModel extends DefaultTableModel {
    private final boolean editable;

    public RefreshableTableModel(String[] columns, boolean editable) {
        super(columns, 0);
        this.editable = editable;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return editable;
    }

    public void replaceRows(List<Object[]> rows) {
        Objects.requireNonNull(rows, "rows");
        @SuppressWarnings("rawtypes")
        Vector<Vector> replacement = new Vector<>(rows.size());
        for (Object[] row : rows) {
            if (row == null || row.length != getColumnCount()) {
                throw new IllegalArgumentException("Every table row must match the model column count.");
            }
            Vector<Object> values = new Vector<>(row.length);
            for (Object value : row) {
                values.add(value);
            }
            replacement.add(values);
        }
        dataVector = replacement;
        fireTableDataChanged();
    }
}
