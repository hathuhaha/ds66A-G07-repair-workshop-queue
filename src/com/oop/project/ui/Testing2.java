package com.oop.project.ui;

import javax.swing.*;
import java.awt.*;

public class Testing2 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Testing JTextField and JComboBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 200);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. JTextField: nhap van ban
        panel.add(new JLabel("Your name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);

        // 2. JComboBox: chon chuyen nganh
        panel.add(new JLabel("Major (normal):"));

        String[] majors = {
                "Computer Science",
                "Data Science",
                "Mathematics"
        };

        JComboBox<String> majorBox = new JComboBox<>(majors);
        panel.add(majorBox);

        // 3. JComboBox cho phep nhap gia tri tuy y
        panel.add(new JLabel("Major (editable):"));

        JComboBox<String> editableMajorBox = new JComboBox<>(majors);
        editableMajorBox.setEditable(true);
        panel.add(editableMajorBox);

        // Nut kiem tra
        JButton button = new JButton("Submit");

        button.addActionListener(e -> {
            String name = nameField.getText();

            String normalMajor = (String) majorBox.getSelectedItem();

            Object editableValue = editableMajorBox.getEditor().getItem();

            String editableMajor = String.valueOf(editableValue);

            JOptionPane.showMessageDialog(
                    frame,
                    "Name: " + name +
                            "\nNormal major: " + normalMajor +
                            "\nEditable major: " + editableMajor);
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.add(button, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}
