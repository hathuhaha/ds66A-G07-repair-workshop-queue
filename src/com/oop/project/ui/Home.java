package com.oop.project.ui;

import javax.swing.*;
import com.oop.project.config.AppContext;
import com.oop.project.ui.theme.AppTheme;


public class Home extends JFrame {
    static {
        AppTheme.setup();
    }

    public Home() {
        this(AppContext.create());
    }

    public Home(AppContext context) {
        setTitle("G07 - Repair Workshop Queue");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Mỗi tab là một JPanel
        JPanel customersPanel = new JPanel();
        AppTheme.styleContentPanel(customersPanel);
        JPanel queuePanel = new RepairQueue(context.repairJobRepository());
        JPanel estimatePanel = new Estimate("test");
        JPanel warrantyPanel = new Warranty("test");
        JPanel historyPanel = new JobHistory("test");

        // Thêm nội dung vào từng panel
        customersPanel.add(new JLabel("Tên:"));
        customersPanel.add(new JTextField(15));
        JButton addButton = new JButton("Add");
        customersPanel.add(addButton);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customers/Devices", customersPanel);
        tabs.addTab("Repair Queue", queuePanel);
        tabs.addTab("Estimate", estimatePanel);
        tabs.addTab("Warranty", warrantyPanel);
        tabs.addTab("Job History", historyPanel);

        add(tabs);
        AppTheme.applyTo(getContentPane());
        AppTheme.stylePrimaryButton(addButton);
        pack();
        setMinimumSize(new java.awt.Dimension(900, 600));
        setResizable(true);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Home frame = new Home();
                frame.setVisible(true);
            } catch (RuntimeException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Could not start the repair workshop queue: " + e.getMessage(),
                        "Application Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
