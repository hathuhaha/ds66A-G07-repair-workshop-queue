package com.oop.project.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Testing extends JFrame {

    private static final int PAGE_SIZE = 5;

    private int currentPage = 0;

    // Dữ liệu của bảng chính
    private final String[] mainColumns = {
        "ID", "Device Name", "Status"
    };

    private final Object[][] mainData = {
        {1, "Laptop Dell", "Pending"},
        {2, "iPhone 13", "Repairing"},
        {3, "MacBook Air", "Completed"},
        {4, "Samsung S22", "Pending"},
        {5, "iPad Pro", "Repairing"},
        {6, "Laptop HP", "Completed"},
        {7, "iPhone 14", "Pending"},
        {8, "Asus Zenbook", "Repairing"},
        {9, "Samsung A54", "Completed"},
        {10, "MacBook Pro", "Pending"},
        {11, "iPhone 15", "Repairing"},
        {12, "Laptop Lenovo", "Completed"}
    };

    // Dữ liệu bảng bên trái: cố định
    private final Object[][] leftData = {
        {"A01", "Customer A"},
        {"A02", "Customer B"},
        {"A03", "Customer C"},
        {"A04", "Customer D"},
        {"A05", "Customer E"}
    };

    // Dữ liệu bảng bên phải: cố định
    private final Object[][] rightData = {
        {"R01", "Technician A"},
        {"R02", "Technician B"},
        {"R03", "Technician C"},
        {"R04", "Technician D"},
        {"R05", "Technician E"}
    };

    // Model của bảng chính
    private final DefaultTableModel mainModel =
        new DefaultTableModel(mainColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

    private final JTable mainTable = new JTable(mainModel);

    // Hai bảng phụ không thay đổi khi chuyển trang
    private final JTable leftTable = createTable(
        new String[]{"Code", "Customer"},
        leftData
    );

    private final JTable rightTable = createTable(
        new String[]{"Code", "Technician"},
        rightData
    );

    // Hai nút điều hướng
    private final JButton prevButton = new JButton("←");
    private final JButton nextButton = new JButton("→");

    private final JLabel pageLabel = new JLabel(
        "Page 1", SwingConstants.CENTER
    );

    public Testing() {
        setTitle("Three Tables - Pagination Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 350);
        setMinimumSize(new Dimension(900, 300));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        // Cấu hình ba bảng với cùng chiều cao
        configureTable(mainTable);
        configureTable(leftTable);
        configureTable(rightTable);

        // Mỗi bảng có vùng hiển thị riêng
        JScrollPane leftScroll = new JScrollPane(leftTable);
        JScrollPane mainScroll = new JScrollPane(mainTable);
        JScrollPane rightScroll = new JScrollPane(rightTable);

        // Cố định kích thước các vùng bảng
        Dimension tableSize = new Dimension(350, 200);

        leftScroll.setPreferredSize(tableSize);
        mainScroll.setPreferredSize(tableSize);
        rightScroll.setPreferredSize(tableSize);

        leftScroll.setMinimumSize(tableSize);
        mainScroll.setMinimumSize(tableSize);
        rightScroll.setMinimumSize(tableSize);

        // Đặt ba bảng cạnh nhau
        JPanel tablesPanel = new JPanel(
            new GridLayout(1, 3, 10, 0)
        );

        tablesPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 0, 10)
        );

        tablesPanel.add(leftScroll);
        tablesPanel.add(mainScroll);
        tablesPanel.add(rightScroll);

        add(tablesPanel, BorderLayout.CENTER);

        // Thanh điều hướng nằm bên dưới
        JPanel navigationPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 15, 10)
        );

        prevButton.setPreferredSize(new Dimension(55, 30));
        nextButton.setPreferredSize(new Dimension(55, 30));
        pageLabel.setPreferredSize(new Dimension(100, 30));

        navigationPanel.add(prevButton);
        navigationPanel.add(pageLabel);
        navigationPanel.add(nextButton);

        add(navigationPanel, BorderLayout.SOUTH);

        // Sự kiện Previous
        prevButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateMainTable();
            }
        });

        // Sự kiện Next
        nextButton.addActionListener(e -> {
            int totalPages = getTotalPages();

            if (currentPage < totalPages - 1) {
                currentPage++;
                updateMainTable();
            }
        });

        // Hiển thị trang đầu tiên
        updateMainTable();
    }

    // Tạo bảng phụ không cho chỉnh sửa
    private static JTable createTable(
        String[] columns,
        Object[][] data
    ) {
        DefaultTableModel model =
            new DefaultTableModel(data, columns) {
                @Override
                public boolean isCellEditable(
                    int row, int column
                ) {
                    return false;
                }
            };

        return new JTable(model);
    }

    // Cấu hình hiển thị thống nhất
    private void configureTable(JTable table) {
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        table.getTableHeader().setReorderingAllowed(false);
    }

    private int getTotalPages() {
        return Math.max(
            1,
            (int) Math.ceil(
                (double) mainData.length / PAGE_SIZE
            )
        );
    }

    // Chỉ cập nhật bảng chính
    private void updateMainTable() {
        mainModel.setRowCount(0);

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(
            start + PAGE_SIZE,
            mainData.length
        );

        for (int i = start; i < end; i++) {
            mainModel.addRow(mainData[i]);
        }

        pageLabel.setText(
            "Page " + (currentPage + 1)
            + " / " + getTotalPages()
        );

        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(
            currentPage < getTotalPages() - 1
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Testing app = new Testing();
            app.setVisible(true);
        });
    }
}