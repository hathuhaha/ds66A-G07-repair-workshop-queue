
package com.oop.project.ui.helpers;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonEditor extends DefaultCellEditor implements ActionListener {

    private final JTable table;
    private final JButton button;

    private int repairId;
    private boolean clicked;

    public ButtonEditor(JTable table) {
        super(new JCheckBox());

        this.table = table;
        this.button = new JButton("Create Warranty");

        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(new Color(108, 131, 208));
        button.setForeground(Color.WHITE);
        button.setBorder(
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );

        button.addActionListener(this);
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {

        button.setText(value == null
                ? "Create Warranty"
                : value.toString());

        // Lấy Repair ID của đúng dòng đang được bấm.
        int modelRow = table.convertRowIndexToModel(row);
        TableModel model = table.getModel();

        this.repairId = ((Number)
                model.getValueAt(modelRow, 0)).intValue();

        this.clicked = true;

        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "Create Warranty";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (clicked) {
            int selectedRepairId = repairId;

            // Kết thúc chế độ chỉnh sửa trước khi xử lý.
            fireEditingStopped();

            JOptionPane.showMessageDialog(
                    table,
                    "Creating warranty for Repair ID: "
                            + selectedRepairId,
                    "Create Warranty",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clicked = false;
        }
    }
}
