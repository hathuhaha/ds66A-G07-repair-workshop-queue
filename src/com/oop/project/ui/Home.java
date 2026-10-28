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

        JobHistory historyPanel = new JobHistory(context.statusHistoryRepository());
        RepairQueue queuePanel = new RepairQueue(context);
        Warranty warrantyPanel = new Warranty(context.warrantyRepository());
        Runnable refreshWorkflow = () -> {
            queuePanel.refresh();
            historyPanel.refresh();
            warrantyPanel.refresh();
        };
        queuePanel.setAfterJobUpdate(refreshWorkflow);
        JPanel customersPanel = new CustomerDevicePanel(context, refreshWorkflow);
        JPanel estimatePanel = new Estimate(context, refreshWorkflow);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customers/Devices", customersPanel);
        tabs.addTab("Repair Queue", queuePanel);
        tabs.addTab("Estimate", estimatePanel);
        tabs.addTab("Warranty", warrantyPanel);
        tabs.addTab("Job History", historyPanel);

        add(tabs);
        AppTheme.applyTo(getContentPane());
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
