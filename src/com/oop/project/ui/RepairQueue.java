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

public class RepairQueue extends JPanel {
    private static String[] columnNames = { "ID", "Device Name", "Symptoms", "Status", "Received At", "Customer Name", "Customer Phone" };
    private List<Object[]> data, filteredData;
    private String typeOfPanel = "Test";
    private JTable table, filteredTable;
    private JPanel filter;

    public RepairQueue() {
        this("Test");
    }

    public RepairQueue(String typeOfPanel) {
        AppTheme.setup();
        this.typeOfPanel = typeOfPanel;
        if (typeOfPanel.equals("Test")) {
            this.data = loadSampleData();
            this.filteredData = loadSampleFilteredData();

        } else {
            this.data = new ArrayList<>();
            this.filteredData = new ArrayList<>();
        }
        createList();
        createFilter();

    }

    private void createList() {
        this.table = new JTable(createReadOnlyTableModel(this.data));
        this.table.setPreferredScrollableViewportSize(new Dimension(600, 350));
        JScrollPane scrollPane = new JScrollPane(this.table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("All repair requests"));
        this.setLayout(new GridLayout(1, 2, 12, 0));
        this.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.add(scrollPane);
    }

    private static List<Object[]> loadSampleData() {
        Path sampleDataPath = Path.of("src", "com", "oop", "project", "sample_data", "repair_queue.txt");
        List<Object[]> sampleData = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(sampleDataPath, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String[] fields = lines.get(i).split("\t", -1);
                if (fields.length != columnNames.length) {
                    throw new IllegalStateException("Invalid repair queue sample data at line " + (i + 1)
                            + ": expected " + columnNames.length + " tab-separated fields.");
                }
                try {
                    sampleData.add(new Object[] {
                            Integer.parseInt(fields[0]), fields[1], fields[2], fields[3], fields[4], fields[5],
                            fields[6]
                    });
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Invalid repair ID in sample data at line " + (i + 1) + ".", e);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read repair queue sample data from "
                    + sampleDataPath.toAbsolutePath(), e);
        }
        return sampleData;
    }

    private static List<Object[]> loadSampleFilteredData() {
        Path sampleDataPath = Path.of(
                "src", "com", "oop", "project", "sample_data", "repair_queue_filter.txt");
        List<Object[]> sampleData = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(sampleDataPath, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String[] fields = lines.get(i).split("\t", -1);
                if (fields.length != columnNames.length) {
                    throw new IllegalStateException("Invalid filtered repair queue sample data at line " + (i + 1)
                            + ": expected " + columnNames.length + " tab-separated fields.");
                }

                try {
                    sampleData.add(new Object[] {
                            Integer.parseInt(fields[0]), fields[1], fields[2], fields[3],
                            fields[4], fields[5], fields[6]
                    });
                } catch (NumberFormatException e) {
                    throw new IllegalStateException(
                            "Invalid repair ID in filtered sample data at line " + (i + 1) + ".", e);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read filtered repair queue sample data from "
                    + sampleDataPath.toAbsolutePath(), e);
        }

        return sampleData;
    }

    private void createFilter() {
        this.filter = new JPanel();
        this.filter.setLayout(new BoxLayout(this.filter, BoxLayout.Y_AXIS));
        this.filter.setBorder(BorderFactory.createTitledBorder("🔎"));

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));

        //create general search and add to filterPanel
        JPanel searchGeneral = new JPanel();
        searchGeneral.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel searchGeneralLabel = new JLabel("Search:");
        JTextField searchGeneralField = new JTextField(30);
        searchGeneral.add(searchGeneralLabel);
        searchGeneral.add(searchGeneralField);
        filterPanel.add(searchGeneral);

        // create search status and add to filterPanel
        String statusOption[] = { "All", "Pending", "In Progress", "Completed" };
        JComboBox<String> statusComboBox = new JComboBox<>(statusOption);
        statusComboBox.setSelectedIndex(0);

        JPanel searchStatus = new JPanel();
        searchStatus.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel searchStatusLabel = new JLabel("Status:");
        searchStatus.add(searchStatusLabel);
        searchStatus.add(statusComboBox);
        filterPanel.add(searchStatus);

        // create search device and add to filterPanel
        JPanel searchDevice = new JPanel();
        searchDevice.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel searchDeviceLabel = new JLabel("Device:");
        JTextField searchDeviceField = new JTextField(30);
        searchDevice.add(searchDeviceLabel);
        searchDevice.add(searchDeviceField);
        filterPanel.add(searchDevice);

        // create search customer and add to filterPanel
        JPanel searchCustomer = new JPanel();
        searchCustomer.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel searchCustomerLabel = new JLabel("Customer:");
        JTextField searchCustomerField = new JTextField(30);
        searchCustomer.add(searchCustomerLabel);
        searchCustomer.add(searchCustomerField);
        filterPanel.add(searchCustomer);

        // create search symptoms and add to filterPanel
        JPanel searchSymptoms = new JPanel();
        searchSymptoms.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel searchSymptomsLabel = new JLabel("Symptoms:");
        JTextField searchSymptomsField = new JTextField(30);
        searchSymptoms.add(searchSymptomsLabel);
        searchSymptoms.add(searchSymptomsField);
        filterPanel.add(searchSymptoms);

        // create sort by date mode

        String sortOption[] = { "Newest to Oldest", "Oldest to Newest" };
        JComboBox<String> sortComboBox = new JComboBox<>(sortOption);
        JPanel sortByDate = new JPanel();
        sortByDate.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel sortByDateLabel = new JLabel("Displaying Order");
        sortByDate.add(sortByDateLabel);
        sortByDate.add(sortComboBox);
        filterPanel.add(sortByDate);

        // create search button and add to filterPanel
        JPanel searchButtonPanel = new JPanel();
        searchButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("🔎︎");
        searchButtonPanel.add(searchButton);
        filterPanel.add(searchButtonPanel);

        // create FilterTable() and add to JPanel Filter
        createFilteredTable();
        this.filteredTable.setPreferredScrollableViewportSize(new Dimension(600, 350));
        JScrollPane filteredScrollPane = new JScrollPane(this.filteredTable);

        this.filter.add(filterPanel);
        this.filter.add(filteredScrollPane);
        this.add(this.filter);

    }

    private DefaultTableModel filteredTableModel;

    private void createFilteredTable() {
        filteredTableModel = createReadOnlyTableModel(filteredData);
        filteredTable = new JTable(filteredTableModel);
    }

    private static DefaultTableModel createReadOnlyTableModel(List<Object[]> rows) {
        return new DefaultTableModel(rows.toArray(new Object[0][]), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void updateFilteredTable(List<Object[]> rows) {
        filteredTableModel.setRowCount(0); // Clear the displayed rows
        for (Object[] row : rows) {
            filteredTableModel.addRow(row);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Repair Queue");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new RepairQueue());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}