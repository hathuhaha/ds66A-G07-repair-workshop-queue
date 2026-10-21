package com.oop.project.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RepairQueue extends JPanel {
    public String[] columnNames = {"ID", "Device Name", "Symptoms", "Status", "Received At", "Customer Name", "Customer Phone"};
    public List<Object[]> data;
    public String typeOfPanel = "Test";
    public RepairQueue() {

    }
    public RepairQueue(String typeOfPanel) {
        if(typeOfPanel.equals("Test")) {
            data = new ArrayList<>();
            data.add(new Object[]{1, "Laptop", "Not turning on", "In Progress", "2024-06-01 10:00", "John Doe", "123456789"});
            data.add(new Object[]{2, "Smartphone", "Screen cracked", "Pending", "2024-06-02 11:30", "Jane Smith", "987654321"});
            data.add(new Object[]{3, "Tablet", "Battery issue", "Completed", "2024-06-03 14:15", "Alice Johnson", "555555555"});
            data.add(new Object[]{4, "Desktop", "Overheating", "In Progress", "2024-06-04 09:45", "Bob Brown", "444444444"});
        } else {
            data = new ArrayList<>();
        }
        
    }

    public static void main(String[] args) {
        
    }
}