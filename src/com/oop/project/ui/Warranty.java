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

public class Warranty extends JPanel {
    private String typeOfPanel = "Test";
    private JTable table;
    private DefaultTableModel tableModel;

    }

    public Warranty(String typeOfPanel) {
        this.typeOfPanel = typeOfPanel;
        if (typeOfPanel.equals("Test")) {
            this.typeOfPanel = "Test";
        } else {
            this.typeOfPanel = "Warranty";
        }
    }

    public static void main(String[] args) {

    }
}
