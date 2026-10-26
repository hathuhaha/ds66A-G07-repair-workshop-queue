package com.oop.project.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import com.oop.project.ui.theme.AppTheme;


public class Warranty extends JPanel {
    private String typeOfPanel = "Test";
    private JTable table;
    private DefaultTableModel tableModel;

    public Warranty() {
        this("Test");
    }

    public Warranty(String typeOfPanel) {
        AppTheme.setup();
        this.typeOfPanel = typeOfPanel;
        if (typeOfPanel.equals("Test")) {
            String[] columnNames = {
                "Warranty ID",
                "Device",
                "Customer",
                "Start Date",
                "End Date"
            };

            Object[][] data = {
                {1, "Dell Inspiron 15", "Nguyen Van An", "2026-01-15", "2027-01-15"},
                {2, "iPhone 15", "Tran Thi Binh", "2025-03-10", "2026-03-10"},
                {3, "Samsung Galaxy S24", "Le Van Cuong", "2026-06-01", "2027-06-01"},
                {4, "MacBook Air M3", "Pham Thi Dung", "2025-08-20", "2026-08-20"},
                {5, "ASUS Vivobook 16", "Hoang Van Giang", "2026-09-01", "2027-09-01"}
            };
            tableModel = new DefaultTableModel(data, columnNames);
        }
        else {

        }
        this.setLayout(new BorderLayout());
        
        //create Dashboard
        JPanel dashboard = new JPanel();
        dashboard.setLayout(new FlowLayout(FlowLayout.CENTER));

        //create number of warranties
        JPanel cntWarranties = new JPanel();
        cntWarranties.setLayout(new BoxLayout(cntWarranties, BoxLayout.Y_AXIS));


        int wCount = tableModel.getRowCount();
        JLabel lblWarranties = new JLabel("Total Warranties");
        JLabel lblWarrantiesCount = new JLabel(String.valueOf(wCount));
        lblWarranties.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblWarrantiesCount.setFont(new Font("SansSerif", Font.BOLD, 24));
        cntWarranties.add(lblWarranties);
        cntWarranties.add(lblWarrantiesCount);
        dashboard.add(cntWarranties);



        //create number of expired warranties
        

        //create number of valid warranties

        //add to warranty panel

        this.add(dashboard, BorderLayout.NORTH);


    }

    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Warranty Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Warranty());
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    });
}
}
