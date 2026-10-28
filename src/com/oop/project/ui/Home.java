package com.oop.project.ui;

import javax.swing.*;
import com.oop.project.ui.theme.AppTheme;

public class Home extends JFrame {
    static {
        AppTheme.setup();
    }

    public Home() {
        setTitle("G07 - Repair Workshop Queue");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Mỗi tab là một JPanel
        JPanel customersPanel = new JPanel();
        AppTheme.styleContentPanel(customersPanel);
        JPanel queuePanel = new RepairQueue();
        JPanel estimatePanel = new JPanel();
        JPanel warrantyPanel = new JPanel();
        JPanel historyPanel = new JPanel();

        // Thêm nội dung vào từng panel
        customersPanel.add(new JLabel("Tên:"));
        customersPanel.add(new JTextField(15));
        JButton addButton = new JButton("Add");
        customersPanel.add(addButton);

        queuePanel.add(new JLabel("Danh sách job"));
        estimatePanel.add(new JLabel("Báo giá"));
        warrantyPanel.add(new JLabel("Bảo hành"));
        historyPanel.add(new JLabel("Lịch sử"));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customers/Devices", customersPanel);
        tabs.addTab("Repair Queue", queuePanel);
        tabs.addTab("Estimate", estimatePanel);
        tabs.addTab("Warranty", warrantyPanel);
        tabs.addTab("Job History", historyPanel);

        add(tabs);
        AppTheme.applyTo(getContentPane());
        AppTheme.stylePrimaryButton(addButton);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Home frame = new Home();
            frame.setVisible(true);
        });
    }
}
