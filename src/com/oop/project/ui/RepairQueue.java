package com.oop.project.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.oop.project.config.AppContext;
import com.oop.project.model.Customer;
import com.oop.project.model.Device;
import com.oop.project.model.JobStatus;
import com.oop.project.model.RepairJob;
import com.oop.project.repository.RepairJobRepository;
import com.oop.project.service.JobService;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.helpers.WrappedTableCellRenderer;
import com.oop.project.ui.helpers.RefreshableTableModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

public class RepairQueue extends JPanel {
    private static final String[] columnNames = { "ID", "Device Name", "Symptoms", "Status", "Received At", "Customer Name",
            "Customer Phone" };
    private static final DateTimeFormatter RECEIVED_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private List<Object[]> data, filteredData;
    private JTable table, filteredTable;
    private JPanel filter;
    private RepairJobRepository repository;
    private JobService jobService;
    private Runnable afterJobUpdate = () -> { };
    private String currentGeneralSearch = "";
    private String currentStatus = "All";
    private String currentDeviceSearch = "";
    private String currentCustomerSearch = "";
    private String currentSymptomsSearch = "";

    public RepairQueue() {
        this(loadSampleData(), loadSampleFilteredData());
    }

    public RepairQueue(String typeOfPanel) {
        if ("test".equalsIgnoreCase(typeOfPanel)) {
            this.data = loadSampleData();
            this.filteredData = loadSampleFilteredData();
        } else {
            this.repository = AppContext.create().repairJobRepository();
            this.data = loadRealData(repository);
            this.filteredData = new ArrayList<>(data);
        }
        initialize();
    }

    public RepairQueue(RepairJobRepository repository) {
        this.repository = repository;
        this.data = loadRealData(repository);
        this.filteredData = new ArrayList<>(data);
        initialize();
    }

    public RepairQueue(AppContext context) {
        this(context, () -> { });
    }

    public RepairQueue(AppContext context, Runnable afterJobUpdate) {
        this(context.repairJobRepository());
        this.jobService = context.jobService();
        this.afterJobUpdate = afterJobUpdate;
    }

    public void setAfterJobUpdate(Runnable afterJobUpdate) {
        this.afterJobUpdate = afterJobUpdate;
    }

    private RepairQueue(List<Object[]> initialData, List<Object[]> initialFilteredData) {
        this.data = initialData;
        this.filteredData = initialFilteredData == null
                ? new ArrayList<>(initialData)
                : initialFilteredData;
        initialize();
    }

    private void initialize() {
        AppTheme.setup();
        createList();
        createFilter();
    }

    private static List<Object[]> loadRealData(RepairJobRepository repository) {
        List<Object[]> rows = new ArrayList<>();
        for (RepairJob job : repository.findQueue()) {
            Device device = job.getDevice();
            Customer customer = device.getCustomer();
            rows.add(new Object[] {
                    job.getId(),
                    deviceName(device),
                    job.getIssueDescription(),
                    job.getStatus().getDisplayName(),
                    job.getCreatedAt() == null ? "" : RECEIVED_AT_FORMAT.format(job.getCreatedAt()),
                    customer.getFullName(),
                    customer.getPhone()
            });
        }
        return rows;
    }

    private static String deviceName(Device device) {
        String brand = device.getBrand();
        String model = device.getModel();
        String name = ((brand == null ? "" : brand) + " " + (model == null ? "" : model)).trim();
        return name.isEmpty() ? device.getDeviceType() : name;
    }


    private void createList() {
        this.table = new JTable(createReadOnlyTableModel(this.data));
        WrappedTableCellRenderer.install(this.table);
        this.table.setPreferredScrollableViewportSize(new Dimension(600, 350));
        JScrollPane scrollPane = new JScrollPane(this.table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("All repair requests"));
        this.setLayout(new GridLayout(1, 2, 12, 0));
        this.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.add(scrollPane);
    }

    private static List<Object[]> loadSampleData() {
        Path sampleDataPath = Path.of(
                "src", "com", "oop", "project", "sample_data", "repair_queue.txt");
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

        // create general search and add to filterPanel
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
        sortComboBox.addActionListener(event -> {
            SortOrder order = sortComboBox.getSelectedIndex() == 0
                    ? SortOrder.DESCENDING
                    : SortOrder.ASCENDING;
            filteredTableSorter.setSortKeys(
                    List.of(new RowSorter.SortKey(4, order)));
        });

        // create search button and add to filterPanel
        JPanel searchButtonPanel = new JPanel();
        searchButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("🔎︎");
        searchButtonPanel.add(searchButton);
        filterPanel.add(searchButtonPanel);
        searchButton.addActionListener(event -> applyFilters(
                searchGeneralField.getText(),
                (String) statusComboBox.getSelectedItem(),
                searchDeviceField.getText(),
                searchCustomerField.getText(),
                searchSymptomsField.getText()));

        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<JobStatus> nextStatus = new JComboBox<>(JobStatus.values());
        JTextField statusNote = new JTextField(18);
        JButton updateStatusButton = new JButton("Update selected repair");
        updatePanel.add(new JLabel("New status:"));
        updatePanel.add(nextStatus);
        updatePanel.add(new JLabel("Note:"));
        updatePanel.add(statusNote);
        updatePanel.add(updateStatusButton);
        updateStatusButton.addActionListener(event -> updateSelectedJob(
                nextStatus.getItemAt(nextStatus.getSelectedIndex()), statusNote.getText()));
        filterPanel.add(updatePanel);

        // create FilterTable() and add to JPanel Filter
        createFilteredTable();
        this.filteredTable.setPreferredScrollableViewportSize(new Dimension(600, 350));
        JScrollPane filteredScrollPane = new JScrollPane(this.filteredTable);

        this.filter.add(filterPanel);
        this.filter.add(filteredScrollPane);
        this.add(this.filter);

    }

    private void applyFilters(
            String generalSearch,
            String selectedStatus,
            String deviceSearch,
            String customerSearch,
            String symptomsSearch) {
        currentGeneralSearch = generalSearch;
        currentStatus = selectedStatus;
        currentDeviceSearch = deviceSearch;
        currentCustomerSearch = customerSearch;
        currentSymptomsSearch = symptomsSearch;
        String generalQuery = normalize(generalSearch);
        String deviceQuery = normalize(deviceSearch);
        String customerQuery = normalize(customerSearch);
        String symptomsQuery = normalize(symptomsSearch);

        List<Object[]> matchingRows = new ArrayList<>();
        for (Object[] row : data) {
            if (rowText(row).contains(generalQuery)
                    && statusMatches(String.valueOf(row[3]), selectedStatus)
                    && normalize(row[1]).contains(deviceQuery)
                    && (normalize(row[5]).contains(customerQuery)
                            || normalize(row[6]).contains(customerQuery))
                    && normalize(row[2]).contains(symptomsQuery)) {
                matchingRows.add(row);
            }
        }
        filteredTableModel.replaceRows(matchingRows);
    }

    public void refresh() {
        if (repository == null) {
            return;
        }
        data = loadRealData(repository);
        DefaultTableModel allRows = createReadOnlyTableModel(data);
        table.setModel(allRows);
        applyFilters(currentGeneralSearch, currentStatus, currentDeviceSearch,
                currentCustomerSearch, currentSymptomsSearch);
    }

    private void updateSelectedJob(JobStatus status, String note) {
        int selected = table.getSelectedRow();
        if (jobService == null) {
            JOptionPane.showMessageDialog(this, "Database mode is required to update repair status.");
            return;
        }
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a repair request first.");
            return;
        }
        try {
            int row = table.convertRowIndexToModel(selected);
            int jobId = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
            jobService.updateJobStatusWithHistory(jobId, status, note);
            refresh();
            afterJobUpdate.run();
        } catch (RuntimeException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Could not update repair status", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static boolean statusMatches(String value, String selectedStatus) {
        String status = normalize(value);
        if (selectedStatus == null || "All".equalsIgnoreCase(selectedStatus)) {
            return true;
        }
        return switch (selectedStatus) {
            case "Pending" -> status.contains("pending")
                    || status.contains("received")
                    || status.contains("diagnos")
                    || status.contains("approval");
            case "In Progress" -> status.contains("progress") || status.contains("repair");
            case "Completed" -> status.contains("completed")
                    || status.contains("ready")
                    || status.contains("closed");
            default -> status.equals(normalize(selectedStatus));
        };
    }

    private static String rowText(Object[] row) {
        StringBuilder text = new StringBuilder();
        for (Object value : row) {
            if (value != null) {
                text.append(value).append(' ');
            }
        }
        return normalize(text.toString());
    }

    private static String normalize(Object value) {
        return value == null ? "" : value.toString().toLowerCase(Locale.ROOT).trim();
    }

    private RefreshableTableModel filteredTableModel;
    private TableRowSorter<DefaultTableModel> filteredTableSorter;

    private void createFilteredTable() {
        filteredTableModel = createReadOnlyTableModel(filteredData);
        filteredTable = new JTable(filteredTableModel);
        filteredTableSorter = new TableRowSorter<>(filteredTableModel);
        filteredTableSorter.setComparator(
                4,
                Comparator.comparing(value -> value == null ? "" : value.toString()));
        filteredTableSorter.setSortKeys(
                List.of(new RowSorter.SortKey(4, SortOrder.DESCENDING)));
        filteredTable.setRowSorter(filteredTableSorter);
        WrappedTableCellRenderer.install(filteredTable);
    }

    private static RefreshableTableModel createReadOnlyTableModel(List<Object[]> rows) {
        RefreshableTableModel model = new RefreshableTableModel(columnNames, false);
        model.replaceRows(rows);
        return model;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Repair Queue");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(new RepairQueue("official"));
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (RuntimeException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Could not load the repair queue: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}