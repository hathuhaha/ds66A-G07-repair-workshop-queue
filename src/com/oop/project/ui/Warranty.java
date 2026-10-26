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
    private JPanel createWarranty, warrantyForm;
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
        this.createWarranty = new JPanel();
        this.createWarranty.setLayout(new BorderLayout(0, 10));


        //List of Repair Job
        JPanel filter = new JPanel();
        filter.setLayout(new BoxLayout(filter, BoxLayout.Y_AXIS));

        //search generally
        JPanel searchGeneral = new JPanel();
        searchGeneral.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel searchGeneralLabel = new JLabel("Search:");
        JTextField searchGeneralField = new JTextField(30);
        searchGeneral.add(searchGeneralLabel);
        searchGeneral.add(searchGeneralField);
        filter.add(searchGeneral);

        //search by customer
        JPanel searchbyName = new JPanel();
        searchbyName.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel searchbyNameLabel = new JLabel("Customer Name:");
        JTextField searchbyNameField = new JTextField(30);
        searchbyName.add(searchbyNameLabel);
        searchbyName.add(searchbyNameField);
        filter.add(searchbyName);

        //search by device
        JPanel searchbyDevice = new JPanel();
        searchbyDevice.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel searchbyDeviceLabel = new JLabel("Device Name:");
        JTextField searchbyDeviceField = new JTextField(30);
        searchbyDevice.add(searchbyDeviceLabel);
        searchbyDevice.add(searchbyDeviceField);
        filter.add(searchbyDevice);


        //search button
        JPanel filterButton = new JPanel();
        filterButton.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton filterIcon = new JButton("🔎");
        filterButton.add(filterIcon);
        filter.add(filterButton);


        //create filtered table
        String warrantyColumns[] = {"Repair ID", "Device Name", "Customer Name", "Received Date", "Completed At"};
        DefaultTableModel model = new DefaultTableModel(repairList.toArray(new Object[0][]), warrantyColumns) {
            @Override 
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable filteredTable = new JTable(model);
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

        JScrollPane scrollPane = new JScrollPane(filteredTable);
        scrollPane.setPreferredSize(
            new Dimension(650, screenHeight / 4)
        );
        this.createWarranty.add(filter, BorderLayout.NORTH);
        this.createWarranty.add(scrollPane, BorderLayout.CENTER);

        createWarrantyForm();
        this.createWarranty.setPreferredSize(new Dimension(700, 500));
        this.add(this.createWarranty, BorderLayout.WEST);

    }
    void createWarrantyForm() {
        //create warranty form
        Object[] chosenSample = {
            102, 
            "Apple Iphone 13", 
            "Ha Thu Ha", 
            "18-7-2026", 
            "20-7-2026"
        };
        this.warrantyForm = new JPanel();
        this.warrantyForm.setLayout(new BoxLayout(this.warrantyForm, BoxLayout.Y_AXIS));
            //Repair ID
        JPanel repairID = new JPanel();
        repairID.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel repairIDLabel = new JLabel("Repair ID: ");
        JTextField repairIDContent = new JTextField(String.valueOf(chosenSample[0]));
        repairIDContent.setEditable(false);
        // repairIDContent.setEnabled(false);
        repairID.add(repairIDLabel);
        repairID.add(repairIDContent);
        warrantyForm.add(repairID);
            //Device Name
        JPanel repairDeviceName = new JPanel();
        repairDeviceName.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel repairDeviceNameLabel = new JLabel("Device Name: ");
        JTextField repairDeviceNameContent = new JTextField(String.valueOf(chosenSample[1]));
        repairDeviceNameContent.setEditable(false);
        // repairDeviceNameContent.setEnabled(false);
        repairDeviceName.add(repairDeviceNameLabel);
        repairDeviceName.add(repairDeviceNameContent);
        warrantyForm.add(repairDeviceName);
            //Customer Name
            //Received Date
            //Complete At
        //add to createWarrantt
        // warrantyForm.setPreferredSize(new Dimension(700, 250));

        repairID.setAlignmentX(Component.LEFT_ALIGNMENT);
        repairDeviceName.setAlignmentX(Component.LEFT_ALIGNMENT);

        repairID.setMaximumSize(repairID.getPreferredSize());
        repairDeviceName.setMaximumSize(repairDeviceName.getPreferredSize());

        createWarranty.add(warrantyForm, BorderLayout.SOUTH);
    }
    private void deleteWarrantyForm() {
        try {
            createWarranty.remove(this.warrantyForm);
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(
                this, 
                "Warranty Form does not exist in createWarranty anymore", 
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }   
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
