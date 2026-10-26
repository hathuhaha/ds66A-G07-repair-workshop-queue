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
    private static String[] columnNames = { "Warranty ID", "Repair ID", "Device", "Customer", "Start Date", "End Date" };
    private JTable table;
    private DefaultTableModel tableModel;
    private JPanel createWarranty;
    private List<Object[]> repairList; // RepairID, Device Name, Customer Name, Received Date, Fix Date

    public Warranty() {
        this("Test");
    }

    public Warranty(String typeOfPanel) {
        AppTheme.setup();
        this.typeOfPanel = typeOfPanel;
        this.repairList = new ArrayList<>();
        if (typeOfPanel.equals("Test")) {

            Object[][] data = {
                {1, 101, "Dell Inspiron 15", "Nguyen Van An", "2026-01-15", "2027-01-15"},
                {2, 102, "iPhone 15", "Tran Thi Binh", "2025-03-10", "2026-03-10"},
                {3, 103, "Samsung Galaxy S24", "Le Van Cuong", "2026-06-01", "2027-06-01"},
                {4, 104, "MacBook Air M3", "Pham Thi Dung", "2025-08-20", "2026-08-20"},
                {5, 105, "ASUS Vivobook 16", "Hoang Van Giang", "2026-09-01", "2027-09-01"}
            };
            repairList.add(new Object[]{201, "Dell Inspiron 15", "Nguyen Van An", "2026-01-15", "2027-01-15"});
            repairList.add(new Object[]{202, "iPhone 15", "Tran Thi Binh", "2025-03-10", "2026-03-10"});
            repairList.add(new Object[]{203, "Samsung Galaxy S24", "Le Van Cuong", "2026-06-01", "2027-06-01"});
            tableModel = new DefaultTableModel(data, Warranty.columnNames);
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


        //Create Warranty Panel
        createWarranty = new JPanel();
        createWarranty.setLayout(new BoxLayout(createWarranty, BoxLayout.Y_AXIS));


        //List of Repair Job

        JPanel repairIDPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblRepairID = new JLabel("Repair ID:");
        JComboBox<Integer> repairIDComboBox = new JComboBox<>(repairList.stream().map(obj -> (Integer) obj[0]).toArray(Integer[]::new));
        repairIDPanel.add(lblRepairID);
        repairIDPanel.add(repairIDComboBox);

    }

    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Warranty Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Warranty());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    });
}
}
