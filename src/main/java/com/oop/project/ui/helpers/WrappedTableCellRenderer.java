package com.oop.project.ui.helpers;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public final class WrappedTableCellRenderer extends JTextArea implements TableCellRenderer {
    private static final int ROW_PADDING = 8;

    private WrappedTableCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
    }

    public static void install(JTable table) {
        WrappedTableCellRenderer renderer = new WrappedTableCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);

        Runnable resizeRows = () -> SwingUtilities.invokeLater(() -> adjustRowHeights(table));
        table.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                resizeRows.run();
            }
        });
        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnMarginChanged(javax.swing.event.ChangeEvent event) {
                resizeRows.run();
            }

            @Override
            public void columnAdded(TableColumnModelEvent event) {
                resizeRows.run();
            }

            @Override
            public void columnRemoved(TableColumnModelEvent event) {
                resizeRows.run();
            }

            @Override
            public void columnMoved(TableColumnModelEvent event) {
                resizeRows.run();
            }

            @Override
            public void columnSelectionChanged(javax.swing.event.ListSelectionEvent event) {
            }
        });
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent event) {
                resizeRows.run();
            }
        });
        resizeRows.run();
    }

    private static void adjustRowHeights(JTable table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            int rowHeight = table.getRowHeight();

            for (int column = 0; column < table.getColumnCount(); column++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component component = table.prepareRenderer(cellRenderer, row, column);
                int columnWidth = table.getColumnModel().getColumn(column).getWidth();
                component.setSize(new Dimension(columnWidth, Short.MAX_VALUE));
                rowHeight = Math.max(rowHeight, component.getPreferredSize().height + ROW_PADDING);
            }

            if (table.getRowHeight(row) != rowHeight) {
                table.setRowHeight(row, rowHeight);
            }
        }
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        setText(value == null ? "" : value.toString());
        setToolTipText(getText().isEmpty() ? null : getText());
        setFont(table.getFont());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return this;
    }
}
