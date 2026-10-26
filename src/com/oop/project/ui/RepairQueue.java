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

public class RepairQueue extends JPanel {
    public static String[] columnNames = { "ID", "Device Name", "Symptoms", "Status", "Received At", "Customer Name",
            "Customer Phone" };
    public List<Object[]> data, filteredData;
    public String typeOfPanel = "Test";
    public JTable table, filteredTable;
    public JPanel filter;

    public RepairQueue() {
        this("Test");
    }

    public RepairQueue(String typeOfPanel) {
        this.typeOfPanel = typeOfPanel;
        if (typeOfPanel.equals("Test")) {
            this.data = loadSampleData();
        } else {
            this.data = new ArrayList<>();
        }
        this.filteredData = loadSampleFilteredData();
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
        this.filter.setLayout(new BorderLayout());
        createFilteredTable();
        this.filteredTable.setPreferredScrollableViewportSize(new Dimension(600, 350));
        JScrollPane scrollPane = new JScrollPane(this.filteredTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Filtered results"));
        this.filter.add(scrollPane, BorderLayout.CENTER);
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