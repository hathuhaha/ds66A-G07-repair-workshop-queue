package com.oop.project.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.helpers.WrappedTableCellRenderer;

public class Warranty extends JPanel {
    private static String[] columnNames = { "Warranty ID", "Repair ID", "Device", "Customer", "Start Date",
            "End Date" };
    private DefaultTableModel tableModel;
    private JPanel createWarranty, warrantyForm;
    private List<Object[]> repairList; // RepairID, Device Name, Customer Name, Received Date, Fix Date

    public Warranty() {
        this("Test");
    }

    public Warranty(String typeOfPanel) {
        AppTheme.setup();
        this.repairList = new ArrayList<>();
        if ("test".equals(typeOfPanel)) {

            Object[][] data = {
                    { 1, 101, "Dell Inspiron 15", "Nguyen Van An", "2026-01-15", "2027-01-15" },
                    { 2, 102, "iPhone 15", "Tran Thi Binh", "2025-03-10", "2026-03-10" },
                    { 3, 103, "Samsung Galaxy S24", "Le Van Cuong", "2026-06-01", "2027-06-01" },
                    { 4, 104, "MacBook Air M3", "Pham Thi Dung", "2025-08-20", "2026-08-20" },
                    { 5, 105, "ASUS Vivobook 16", "Hoang Van Giang", "2026-09-01", "2027-09-01" }
            };
            repairList.add(new Object[] { 201, "Dell Inspiron 15", "Nguyen Van An", "2026-01-15", "2027-01-15" });
            repairList.add(new Object[] { 202, "iPhone 15", "Tran Thi Binh", "2025-03-10", "2026-03-10" });
            repairList.add(new Object[] { 203, "Samsung Galaxy S24", "Le Van Cuong", "2026-06-01", "2027-06-01" });
            tableModel = new DefaultTableModel(data, Warranty.columnNames);
        } else {
            tableModel = new DefaultTableModel(new Object[0][], Warranty.columnNames);
        }
        this.setLayout(new BorderLayout());

        // create Dashboard
        JPanel dashboard = new JPanel();
        dashboard.setLayout(new FlowLayout(FlowLayout.CENTER));

        // create number of warranties
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

        // create number of expired warranties

        // create number of valid warranties

        // add to warranty panel

        this.add(dashboard, BorderLayout.NORTH);

        // Create the warranty list and detail area.
        this.createWarranty = new JPanel();
        this.createWarranty.setLayout(new BorderLayout(12, 12));
        this.createWarranty.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel filter = new JPanel(new GridLayout(0, 2, 8, 8));
        filter.setBorder(BorderFactory.createTitledBorder("Search warranties"));
        filter.add(new JLabel("Search:"));
        filter.add(new JTextField(18));
        filter.add(new JLabel("Customer Name:"));
        filter.add(new JTextField(18));
        filter.add(new JLabel("Device Name:"));
        filter.add(new JTextField(18));
        JButton filterIcon = new JButton("Search");
        filter.add(new JLabel());
        filter.add(filterIcon);

        String warrantyColumns[] = { "Repair ID", "Device Name", "Customer Name", "Received Date", "Completed At" };
        DefaultTableModel model = new DefaultTableModel(repairList.toArray(new Object[0][]), warrantyColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable filteredTable = new JTable(model);
        WrappedTableCellRenderer.install(filteredTable);
        JScrollPane scrollPane = new JScrollPane(filteredTable);
        this.createWarranty.add(filter, BorderLayout.NORTH);
        this.createWarranty.add(scrollPane, BorderLayout.CENTER);

        createWarrantyForm();
        this.createWarranty.add(this.warrantyForm, BorderLayout.EAST);
        this.add(this.createWarranty, BorderLayout.CENTER);

    }

    void createWarrantyForm() {
        // create warranty form
        Object[] chosenSample = {
                102,
                "Apple Iphone 13",
                "Ha Thu Ha",
                "18-7-2026",
                "20-7-2026"
        };
        this.warrantyForm = new JPanel();
        this.warrantyForm.setLayout(new GridBagLayout());
        this.warrantyForm.setBorder(BorderFactory.createTitledBorder("Create Warranty"));
        this.warrantyForm.setPreferredSize(new Dimension(340, 220));

        JTextField repairIdField = new JTextField(String.valueOf(chosenSample[0]), 16);
        repairIdField.setEditable(false);
        JTextField deviceNameField = new JTextField(String.valueOf(chosenSample[1]), 16);
        deviceNameField.setEditable(false);
        JTextField customerNameField = new JTextField(String.valueOf(chosenSample[2]), 16);
        customerNameField.setEditable(false);

        addFormRow(0, "Repair ID:", repairIdField);
        addFormRow(1, "Device Name:", deviceNameField);
        addFormRow(2, "Customer Name:", customerNameField);

        JButton createButton = new JButton("Create Warranty");
        createButton.addActionListener(e -> {
            deleteWarrantyForm();
            createWarranty.revalidate();
            createWarranty.repaint();
            JOptionPane.showMessageDialog(
                    this,
                    "Created New Warranty Successfully!\n");
        });
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 3;
        buttonConstraints.gridwidth = 2;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.insets = new Insets(12, 8, 8, 8);
        buttonConstraints.weightx = 1;
        warrantyForm.add(createButton, buttonConstraints);
    }

    private void addFormRow(int row, String labelText, JTextField field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        labelConstraints.insets = new Insets(8, 8, 4, 8);
        warrantyForm.add(new JLabel(labelText), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;
        fieldConstraints.insets = new Insets(8, 0, 4, 8);
        warrantyForm.add(field, fieldConstraints);
    }

    private void deleteWarrantyForm() {
        createWarranty.remove(this.warrantyForm);
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
