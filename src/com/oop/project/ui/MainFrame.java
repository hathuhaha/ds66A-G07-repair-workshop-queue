package com.oop.project.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("G07 - Repair Workshop Queue");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel title = new JLabel(
            "G07 - Repair Workshop Queue",
            SwingConstants.CENTER
        );

        add(title, BorderLayout.CENTER);
    }
}