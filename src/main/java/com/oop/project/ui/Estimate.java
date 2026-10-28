package com.oop.project.ui;

import com.oop.project.ui.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Estimate extends JPanel {
    private final JTextField jobIdField = new JTextField(12);
    private final JTextField versionField = new JTextField("1", 8);
    private final JComboBox<String> statusCombo = new JComboBox<>(
            new String[] { "DRAFT", "APPROVED", "REJECTED" });
    private final JTextArea noteArea = new JTextArea(3, 30);
    private final DefaultTableModel partsModel = createEditableModel(
            new String[] { "Part name", "Unit price", "Quantity", "Subtotal" });
    private final DefaultTableModel laborModel = createEditableModel(
            new String[] { "Labor description", "Hours", "Hourly rate", "Subtotal" });
    private final JLabel totalLabel = new JLabel("Total estimate: 0.00");
    private final JLabel validationLabel = new JLabel("Prototype only - not connected to PostgreSQL.");
    private boolean calculatingTotal;

    public Estimate() {
        this("");
    }

    public Estimate(String typeOfPanel) {
        AppTheme.setup();
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(AppTheme.BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createEstimateForm(), BorderLayout.WEST);
        add(createLineItemsPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        partsModel.addTableModelListener(this::updateTotal);
        laborModel.addTableModelListener(this::updateTotal);
        updateTotal(null);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);

        JLabel title = new JLabel("Repair Estimate");
        AppTheme.styleSectionTitle(title);
        JLabel subtitle = new JLabel("Prepare an estimate from parts and labor charges.");
        AppTheme.styleSecondaryLabel(subtitle);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        return header;
    }

    private JPanel createEstimateForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Estimate details"));

        jobIdField.setToolTipText("Repair job ID this estimate belongs to");
        AppTheme.styleTextField(jobIdField);
        versionField.setToolTipText("Estimate version for this repair job");
        AppTheme.styleTextField(versionField);
        versionField.setEditable(false);
        AppTheme.styleComboBox(statusCombo);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(AppTheme.BODY);
        noteArea.setForeground(AppTheme.TEXT);
        noteArea.setBackground(AppTheme.SURFACE);
        noteArea.setBorder(AppTheme.INPUT_BORDER);

        addFormRow(form, 0, "Repair job ID", jobIdField);
        addFormRow(form, 1, "Version", versionField);
        addFormRow(form, 2, "Status", statusCombo);
        addFormRow(form, 3, "Note", new JScrollPane(noteArea));
        return form;
    }

    private JPanel createLineItemsPanel() {
        JPanel lineItems = new JPanel(new BorderLayout(8, 8));
        lineItems.setBorder(BorderFactory.createTitledBorder("Estimate items"));

        JTable partsTable = new JTable(partsModel);
        AppTheme.styleTable(partsTable);
        JTable laborTable = new JTable(laborModel);
        AppTheme.styleTable(laborTable);

        JButton addPartButton = new JButton("Add part");
        AppTheme.stylePrimaryButton(addPartButton);
        addPartButton.addActionListener(event -> partsModel.addRow(new Object[] { "", "0.00", "1", "0.00" }));

        JButton removePartButton = new JButton("Remove selected part");
        AppTheme.styleSecondaryButton(removePartButton);
        removePartButton.addActionListener(event -> removeSelectedRow(partsTable, partsModel));

        JButton addLaborButton = new JButton("Add labor");
        AppTheme.stylePrimaryButton(addLaborButton);
        addLaborButton.addActionListener(event -> laborModel.addRow(new Object[] { "", "1.00", "0.00", "0.00" }));

        JButton removeLaborButton = new JButton("Remove selected labor");
        AppTheme.styleSecondaryButton(removeLaborButton);
        removeLaborButton.addActionListener(event -> removeSelectedRow(laborTable, laborModel));

        JPanel partsPanel = createItemsPanel(partsTable, addPartButton, removePartButton);
        JPanel laborPanel = createItemsPanel(laborTable, addLaborButton, removeLaborButton);

        JTabbedPane itemTabs = new JTabbedPane();
        itemTabs.addTab("Parts", partsPanel);
        itemTabs.addTab("Labor", laborPanel);
        lineItems.add(itemTabs, BorderLayout.CENTER);
        return lineItems;
    }

    private JPanel createItemsPanel(JTable table, JButton addButton, JButton removeButton) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(addButton);
        actions.add(removeButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);

        totalLabel.setFont(AppTheme.BODY_BOLD);
        totalLabel.setForeground(AppTheme.PRIMARY);
        validationLabel.setFont(AppTheme.BODY);

        JButton saveButton = new JButton("Save draft");
        AppTheme.stylePrimaryButton(saveButton);
        saveButton.addActionListener(event -> saveDraft());

        JPanel summary = new JPanel(new BorderLayout(0, 4));
        summary.setOpaque(false);
        summary.add(totalLabel, BorderLayout.NORTH);
        summary.add(validationLabel, BorderLayout.CENTER);

        footer.add(summary, BorderLayout.CENTER);
        footer.add(saveButton, BorderLayout.EAST);
        return footer;
    }

    private static DefaultTableModel createEditableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 3;
            }
        };
    }

    private static void addFormRow(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        labelConstraints.insets = new Insets(8, 8, 8, 8);
        panel.add(new JLabel(label + ":"), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(8, 0, 8, 8);
        panel.add(component, fieldConstraints);
    }

    private static void removeSelectedRow(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            model.removeRow(table.convertRowIndexToModel(selectedRow));
        }
    }

    private void updateTotal(TableModelEvent event) {
        if (calculatingTotal) {
            return;
        }
        calculatingTotal = true;
        try {
            BigDecimal total = calculatePartsTotal().add(calculateLaborTotal());
            totalLabel.setText("Total estimate: " + total.setScale(2, RoundingMode.HALF_UP));
            validationLabel.setText("Prototype only - not connected to PostgreSQL.");
            validationLabel.setForeground(AppTheme.TEXT_SECONDARY);
        } catch (NumberFormatException | ArithmeticException exception) {
            totalLabel.setText("Total estimate: —");
            validationLabel.setText("Check prices, quantities, and labor hours.");
            validationLabel.setForeground(AppTheme.DANGER);
        } finally {
            calculatingTotal = false;
        }
    }

    private BigDecimal calculatePartsTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int row = 0; row < partsModel.getRowCount(); row++) {
            BigDecimal unitPrice = new BigDecimal(partsModel.getValueAt(row, 1).toString().trim());
            int quantity = Integer.parseInt(partsModel.getValueAt(row, 2).toString().trim());
            if (unitPrice.signum() < 0 || quantity <= 0) {
                throw new NumberFormatException("Part price and quantity must be non-negative/positive.");
            }
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            partsModel.setValueAt(subtotal.setScale(2, RoundingMode.HALF_UP).toPlainString(), row, 3);
            total = total.add(subtotal);
        }
        return total;
    }

    private BigDecimal calculateLaborTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int row = 0; row < laborModel.getRowCount(); row++) {
            BigDecimal hours = new BigDecimal(laborModel.getValueAt(row, 1).toString().trim());
            BigDecimal hourlyRate = new BigDecimal(laborModel.getValueAt(row, 2).toString().trim());
            if (hours.signum() <= 0 || hourlyRate.signum() < 0) {
                throw new NumberFormatException("Labor hours must be positive and hourly rate non-negative.");
            }
            BigDecimal subtotal = hours.multiply(hourlyRate);
            laborModel.setValueAt(subtotal.setScale(2, RoundingMode.HALF_UP).toPlainString(), row, 3);
            total = total.add(subtotal);
        }
        return total;
    }

    private void saveDraft() {
        if (jobIdField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a repair job ID.", "Missing information",
                    JOptionPane.WARNING_MESSAGE);
            jobIdField.requestFocusInWindow();
            return;
        }
        try {
            Integer.parseInt(jobIdField.getText().trim());
            Integer.parseInt(versionField.getText().trim());
            calculatePartsTotal();
            calculateLaborTotal();
            updateTotal(null);
        } catch (NumberFormatException | ArithmeticException exception) {
            JOptionPane.showMessageDialog(this, "Check the job ID, version, and line item values.",
                    "Invalid estimate", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "The estimate is ready in the UI, but it is not saved to PostgreSQL yet.",
                "Persistence not connected", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Repair Estimate");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new Estimate("test"));
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
