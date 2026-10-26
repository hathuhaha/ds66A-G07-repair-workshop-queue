
package com.oop.project.ui;

import com.oop.project.ui.helpers.ButtonRenderer;
import com.oop.project.ui.helpers.ButtonEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;

public class Test extends JFrame {

    public Test() {
        setTitle("Test Table Buttons");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 350);
        setLocationRelativeTo(null);

        // Sample data
        String[] columns = {
            "Repair ID",
            "Device Name",
            "Customer Name",
            "Action"
        };

        Object[][] data = {
            {201, "Dell Inspiron 15", "Nguyen Van An", "Create Warranty"},
            {202, "iPhone 15", "Tran Thi Binh", "Create Warranty"},
            {203, "Samsung Galaxy S24", "Le Van Cuong", "Create Warranty"},
            {204, "MacBook Air M3", "Pham Thi Dung", "Create Warranty"}
        };

        // Create table model
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        // Create table
        JTable table = new JTable(model);
        table.setRowHeight(40);

        // Configure Action column
        TableColumn actionColumn =
                table.getColumnModel().getColumn(3);

        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(table));

        // Add table to window
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Test().setVisible(true);
        });
    }
}
