package com.oop.project.ui;

import com.oop.project.ui.helpers.WrappedTableCellRenderer;
import com.oop.project.ui.helpers.RefreshableTableModel;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.model.StatusHistory;
import com.oop.project.repository.StatusHistoryRepository;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class JobHistory extends JPanel {
    private static final String[] COLUMN_NAMES = {
            "History ID", "Repair ID", "Previous Status", "New Status", "Note", "Status Saved At"
    };
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RefreshableTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private final DateFilterPicker fromDatePicker;
    private final DateFilterPicker toDatePicker;
    private final JComboBox<String> statusFilter;
    private final JLabel recordCount;
    private final StatusHistoryRepository repository;

    public JobHistory() {
        this("");
    }

    public JobHistory(String typeOfPanel) {
        this(typeOfPanel, null);
    }

    public JobHistory(StatusHistoryRepository repository) {
        this("", repository);
    }

    private JobHistory(String typeOfPanel, StatusHistoryRepository repository) {
        AppTheme.setup();
        this.repository = repository;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(AppTheme.BACKGROUND);

        JLabel title = new JLabel("Repair Job History");
        AppTheme.styleSectionTitle(title);

        JLabel subtitle = new JLabel("Review status changes recorded for repair jobs.");
        AppTheme.styleSecondaryLabel(subtitle);

        JPanel heading = new JPanel(new BorderLayout(0, 4));
        heading.setOpaque(false);
        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.CENTER);
        add(heading, BorderLayout.NORTH);

        tableModel = new RefreshableTableModel(COLUMN_NAMES, false) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 5 ? LocalDateTime.class : Object.class;
            }
        };
        sorter = new TableRowSorter<>(tableModel);
        if (repository != null) {
            loadRows(repository.findAll());
        } else if ("test".equalsIgnoreCase(typeOfPanel)) {
            addSampleRows();
        }

        JTable table = new JTable(tableModel);
        AppTheme.styleTable(table);
        table.setDefaultRenderer(LocalDateTime.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value instanceof LocalDateTime dateTime
                        ? DISPLAY_DATE_TIME.format(dateTime)
                        : "");
            }
        });
        table.setRowSorter(sorter);
        WrappedTableCellRenderer.install(table);

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        add(tableScrollPane, BorderLayout.CENTER);

        searchField = new JTextField(22);
        AppTheme.styleTextField(searchField);
        statusFilter = new JComboBox<>(new String[] {
                "All statuses", "RECEIVED", "DIAGNOSING", "WAITING_FOR_APPROVAL", "REPAIRING", "READY", "CLOSED"
        });
        AppTheme.styleComboBox(statusFilter);
        fromDatePicker = new DateFilterPicker();
        toDatePicker = new DateFilterPicker();

        JButton searchButton = new JButton("Search");
        AppTheme.stylePrimaryButton(searchButton);
        searchButton.addActionListener(event -> applyFilters(true));

        JButton resetButton = new JButton("Reset");
        AppTheme.styleSecondaryButton(resetButton);
        resetButton.addActionListener(event -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            fromDatePicker.clear();
            toDatePicker.clear();
            applyFilters(false);
        });

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        filters.setBorder(BorderFactory.createTitledBorder("Filter history"));
        addFilterRow(filters, 0, new JLabel("Search"), searchField);
        addFilterRow(filters, 1, new JLabel("New status"), statusFilter);
        addFilterRow(filters, 2, new JLabel("From date"), fromDatePicker);
        addFilterRow(filters, 3, new JLabel("To date"), toDatePicker);
        JPanel filterButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterButtons.setOpaque(false);
        filterButtons.add(searchButton);
        filterButtons.add(resetButton);
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 1;
        buttonConstraints.gridy = 4;
        buttonConstraints.anchor = GridBagConstraints.LINE_START;
        buttonConstraints.insets = new Insets(6, 8, 6, 8);
        filters.add(filterButtons, buttonConstraints);
        add(filters, BorderLayout.WEST);

        recordCount = new JLabel();
        recordCount.setHorizontalAlignment(SwingConstants.RIGHT);
        AppTheme.styleSecondaryLabel(recordCount);
        add(recordCount, BorderLayout.SOUTH);
        updateRecordCount();
    }

    public void refresh() {
        if (repository == null) {
            return;
        }
        loadRows(repository.findAll());
        updateRecordCount();
    }

    private void loadRows(List<StatusHistory> histories) {
        List<Object[]> rows = new ArrayList<>(histories.size());
        for (StatusHistory history : histories) {
            rows.add(new Object[] {
                    history.getId(),
                    history.getJobId(),
                    history.getOldStatus() == null ? "-" : history.getOldStatus().name(),
                    history.getNewStatus().name(),
                    history.getNote(),
                    history.getChangedAt()
            });
        }
        tableModel.replaceRows(rows);
    }

    private static void addFilterRow(JPanel panel, int row, JLabel label, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        labelConstraints.insets = new Insets(6, 8, 6, 4);
        panel.add(label, labelConstraints);

        GridBagConstraints componentConstraints = new GridBagConstraints();
        componentConstraints.gridx = 1;
        componentConstraints.gridy = row;
        componentConstraints.weightx = 1;
        componentConstraints.fill = GridBagConstraints.HORIZONTAL;
        componentConstraints.insets = new Insets(6, 4, 6, 8);
        panel.add(component, componentConstraints);
    }

    private void addSampleRows() {
        tableModel.addRow(new Object[] {
                1, 101, "-", "RECEIVED", "Repair request received", LocalDateTime.of(2026, 10, 20, 9, 15)
        });
        tableModel.addRow(new Object[] {
                2, 101, "RECEIVED", "DIAGNOSING", "Technician started diagnosis", LocalDateTime.of(2026, 10, 20, 10, 0)
        });
        tableModel.addRow(new Object[] {
                3, 102, "DIAGNOSING", "WAITING_FOR_APPROVAL", "Estimate sent to customer", LocalDateTime.of(2026, 10, 20, 11, 30)
        });
        tableModel.addRow(new Object[] {
                4, 103, "REPAIRING", "READY", "Repair completed; ready for pickup", LocalDateTime.of(2026, 10, 21, 16, 45)
        });
    }

    private void applyFilters(boolean showValidationErrors) {
        String query = searchField.getText().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        LocalDate fromDate = fromDatePicker.getDate();
        LocalDate toDate = toDatePicker.getDate();
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            if (showValidationErrors) {
                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "From date must be on or before To date.",
                        "Invalid date range",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        final LocalDate filterFrom = fromDate;
        final LocalDate filterTo = toDate;
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (!query.isEmpty()) {
                    boolean matchesQuery = false;
                    for (int column = 0; column < entry.getValueCount(); column++) {
                        Object value = entry.getValue(column);
                        if (value != null && value.toString().toLowerCase().contains(query.toLowerCase())) {
                            matchesQuery = true;
                            break;
                        }
                    }
                    if (!matchesQuery) {
                        return false;
                    }
                }

                if (selectedStatus != null && !"All statuses".equals(selectedStatus)
                        && !selectedStatus.equals(entry.getStringValue(3))) {
                    return false;
                }

                LocalDateTime savedAt = parseSavedAt(entry.getValue(5));
                if (savedAt == null) {
                    return filterFrom == null && filterTo == null;
                }
                LocalDate savedDate = savedAt.toLocalDate();
                return (filterFrom == null || !savedDate.isBefore(filterFrom))
                        && (filterTo == null || !savedDate.isAfter(filterTo));
            }
        });
        updateRecordCount();
    }

    private static LocalDateTime parseSavedAt(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value == null) {
            return null;
        }
        throw new IllegalStateException("Status timestamp must be stored as LocalDateTime.");
    }

    private void updateRecordCount() {
        recordCount.setText("Showing " + sorter.getViewRowCount() + " of " + tableModel.getRowCount() + " records");
    }

    private static final class DateFilterPicker extends JPanel {
        private final JTextField dateField = new JTextField(10);
        private LocalDate selectedDate;

        private DateFilterPicker() {
            super(new BorderLayout(4, 0));
            setOpaque(false);
            dateField.setEditable(false);
            dateField.setFocusable(false);
            AppTheme.styleTextField(dateField);

            JButton calendarButton = new JButton("Choose");
            AppTheme.styleSecondaryButton(calendarButton);
            calendarButton.addActionListener(event -> showCalendar(calendarButton));
            add(dateField, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);
        }

        private LocalDate getDate() {
            return selectedDate;
        }

        private void clear() {
            selectedDate = null;
            dateField.setText("");
        }

        private void showCalendar(Component invoker) {
            Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
            CalendarDialog dialog = new CalendarDialog(owner, selectedDate);
            dialog.setLocationRelativeTo(invoker);
            dialog.setVisible(true);
            if (dialog.getSelectedDate() != null) {
                selectedDate = dialog.getSelectedDate();
                dateField.setText(DISPLAY_DATE.format(selectedDate));
            }
        }
    }

    private static final class CalendarDialog extends JDialog {
        private static final String[] WEEKDAYS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

        private LocalDate selectedDate;
        private LocalDate displayedMonth;
        private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        private final JPanel daysPanel = new JPanel(new GridBagLayout());

        private CalendarDialog(Window owner, LocalDate initialDate) {
            super(owner, "Choose date", ModalityType.APPLICATION_MODAL);
            displayedMonth = (initialDate == null ? LocalDate.now() : initialDate).withDayOfMonth(1);
            selectedDate = initialDate;
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            JButton previousMonth = new JButton("<");
            JButton nextMonth = new JButton(">");
            previousMonth.addActionListener(event -> {
                displayedMonth = displayedMonth.minusMonths(1);
                refreshCalendar();
            });
            nextMonth.addActionListener(event -> {
                displayedMonth = displayedMonth.plusMonths(1);
                refreshCalendar();
            });

            JPanel monthNavigation = new JPanel(new BorderLayout(8, 0));
            monthNavigation.add(previousMonth, BorderLayout.WEST);
            monthNavigation.add(monthLabel, BorderLayout.CENTER);
            monthNavigation.add(nextMonth, BorderLayout.EAST);
            add(monthNavigation, BorderLayout.NORTH);
            add(daysPanel, BorderLayout.CENTER);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(event -> dispose());
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.add(cancelButton);
            add(footer, BorderLayout.SOUTH);

            refreshCalendar();
            pack();
            setMinimumSize(new java.awt.Dimension(300, 260));
        }

        private LocalDate getSelectedDate() {
            return selectedDate;
        }

        private void refreshCalendar() {
            monthLabel.setText(displayedMonth.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("vi-VN"))));
            daysPanel.removeAll();

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1;
            constraints.weighty = 1;
            for (int column = 0; column < WEEKDAYS.length; column++) {
                constraints.gridx = column;
                constraints.gridy = 0;
                daysPanel.add(new JLabel(WEEKDAYS[column], SwingConstants.CENTER), constraints);
            }

            LocalDate firstDay = displayedMonth.withDayOfMonth(1);
            int column = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            for (int day = 1; day <= displayedMonth.lengthOfMonth(); day++) {
                LocalDate date = displayedMonth.withDayOfMonth(day);
                JButton dayButton = new JButton(Integer.toString(day));
                dayButton.setFocusPainted(false);
                if (date.equals(selectedDate)) {
                    dayButton.setBackground(AppTheme.SELECTION);
                }
                dayButton.addActionListener(event -> {
                    selectedDate = date;
                    dispose();
                });

                constraints.gridx = column;
                constraints.gridy = 1 + (day + firstDay.getDayOfWeek().getValue() - 2) / 7;
                daysPanel.add(dayButton, constraints);
                column = (column + 1) % 7;
            }

            daysPanel.revalidate();
            daysPanel.repaint();
            pack();
        }
    }
}
