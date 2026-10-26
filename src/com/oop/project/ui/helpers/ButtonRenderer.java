
package com.oop.project.ui.helpers;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JButton
        implements TableCellRenderer {

    public ButtonRenderer() {
        setOpaque(true);
        setFocusPainted(false);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setBackground(new Color(108, 131, 208));
        setForeground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
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

        if (isSelected) {
            setBackground(new Color(91, 113, 190));
        } else {
            setBackground(new Color(108, 131, 208));
        }

        return this;
    }
}
