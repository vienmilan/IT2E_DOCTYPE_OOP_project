import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class CharityApp extends JFrame {

    private final java.util.List<Person> needyPeople = new ArrayList<>();
    private final java.util.List<Donation> donations = new ArrayList<>();

    private final DefaultTableModel peopleModel;
    private final DefaultTableModel donationModel;

    private final JLabel totalLabel;
    private final DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");

    public CharityApp() {
        setTitle("Barangay Central Records");
        setSize(1100, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(new Color(18, 20, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(12, 14, 26));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("Barangay Central Records");
        title.setForeground(new Color(210, 230, 255));
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title, BorderLayout.WEST);

        totalLabel = new JLabel("₱ 0.00");
        totalLabel.setForeground(new Color(140, 220, 255));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(totalLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table models
        peopleModel = new DefaultTableModel(
                new String[]{"Name", "Address", "Needs", "Age", "Contact", "Category"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // FIX: Removed "Date" from the table header
        donationModel = new DefaultTableModel(
                new String[]{"Donor", "Amount", "Description", "Type"}, 0) { 
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable peopleTable = createStyledTable(peopleModel);
        JTable donationTable = createStyledTable(donationModel);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(new Color(18, 20, 30));

        tabs.add("Add Person", createAddPersonPanel());
        tabs.add("Add Donation", createAddDonationPanel());
        tabs.add("View Records", createViewPanel(peopleTable, donationTable));
        add(tabs, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("© 2025 Barangay Central", SwingConstants.CENTER);
        footer.setForeground(new Color(120, 140, 170));
        footer.setBorder(new EmptyBorder(8, 0, 8, 0));
        add(footer, BorderLayout.SOUTH);

        // Save data on close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("Saving data to database...");
                try {
                    PersonDAO.saveAll(needyPeople);
                    DonationDAO.saveAll(donations);
                    System.out.println("Data saved successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CharityApp.this,
                            "Error saving data: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Load data from database
        loadFromDatabase();
    }

    // --- Create Add Person panel (No Changes) ---
    private JPanel createAddPersonPanel() {
        JPanel p = formPanel();

        JTextField name = field();
        JTextField address = field();
        JTextField needs = field();
        JTextField age = field();
        JTextField contact = field();
        JComboBox<String> category = new JComboBox<>(new String[]{"Senior", "PWD", "Solo Parent", "Indigent"});

        JButton add = primaryButton("Add Person");

        addFormRow(p, 0, "Name", name);
        addFormRow(p, 1, "Address", address);
        addFormRow(p, 2, "Needs", needs);
        addFormRow(p, 3, "Age", age);
        addFormRow(p, 4, "Contact", contact);
        addFormRow(p, 5, "Category", category);

        add.addActionListener(e -> {
            if (name.getText().isEmpty() || age.getText().isEmpty()) {
                warn("Fill all required fields");
                return;
            }

            int ageVal;
            try {
                ageVal = Integer.parseInt(age.getText());
            } catch (NumberFormatException ex) {
                warn("Age must be a number");
                return;
            }

            Person person = new Person(
                    name.getText(), address.getText(), needs.getText(),
                    ageVal, contact.getText(),
                    category.getSelectedItem().toString()
            );

            needyPeople.add(person);
            peopleModel.addRow(new Object[]{
                    person.name, person.address, person.needs,
                    person.age, person.contact, person.category
            });

            clear(name, address, needs, age, contact);
            info("Person added successfully");
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        p.add(add, gbc);

        return p;
    }

    // --- Create Add Donation panel (Modifications applied) ---
    private JPanel createAddDonationPanel() {
        JPanel p = formPanel();

        JTextField donor = field();
        JTextField amount = field();
        JTextField desc = field();
        // FIX: Removed JTextField date = field();
        JComboBox<String> type = new JComboBox<>(new String[]{"Cash", "Goods", "Supplies", "Other"});

        JButton add = primaryButton("Add Donation");

        addFormRow(p, 0, "Donor", donor);
        addFormRow(p, 1, "Amount", amount);
        addFormRow(p, 2, "Description", desc);
        // FIX: Removed addFormRow(p, 3, "Date", date);
        addFormRow(p, 3, "Type", type); // Adjusted Y index

        add.addActionListener(e -> {
            double amt;
            try {
                amt = Double.parseDouble(amount.getText());
            } catch (Exception ex) {
                warn("Invalid amount or missing data.");
                return;
            }

            // FIX: Removed date parameter from the Donation constructor call
            Donation d = new Donation(
                    donor.getText(), amt, desc.getText(),
                    type.getSelectedItem().toString()
            );

            donations.add(d);
            // FIX: Removed d.date from the row data
            donationModel.addRow(new Object[]{
                    d.donor, d.amount, d.description, d.type
            });

            updateTotal();
            clear(donor, amount, desc); // FIX: Removed date from clear list
            info("Donation added");
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; 
        gbc.gridy = 4; // Adjusted Y index
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        p.add(add, gbc);

        return p;
    }

    // --- View Panel with Search ---
    private JPanel createViewPanel(JTable people, JTable donations) {
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 12));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.setBackground(new Color(18, 20, 30));

        // People panel with search and remove button
        JPanel peoplePanel = new JPanel(new BorderLayout(8, 8));
        peoplePanel.setBackground(new Color(10, 14, 22));
        peoplePanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(40, 60, 90)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Search panel for people
        JPanel peopleSearchPanel = new JPanel(new BorderLayout(5, 5));
        peopleSearchPanel.setBackground(new Color(10, 14, 22));
        JTextField peopleSearchField = new JTextField();
        JButton peopleSearchBtn = new JButton("Search");
        peopleSearchBtn.setBackground(new Color(40, 160, 160));
        peopleSearchBtn.setForeground(Color.WHITE);
        peopleSearchPanel.add(peopleSearchField, BorderLayout.CENTER);
        peopleSearchPanel.add(peopleSearchBtn, BorderLayout.EAST);

        // Label and search in north
        JPanel peopleNorth = new JPanel(new BorderLayout());
        peopleNorth.setBackground(new Color(10, 14, 22));
        JLabel peopleLabel = new JLabel("Needy People");
        peopleLabel.setForeground(Color.WHITE);
        peopleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        peopleNorth.add(peopleLabel, BorderLayout.NORTH);
        peopleNorth.add(peopleSearchPanel, BorderLayout.SOUTH);
        peoplePanel.add(peopleNorth, BorderLayout.NORTH);

        peoplePanel.add(new JScrollPane(people), BorderLayout.CENTER);

        JButton removePersonBtn = new JButton("Remove Selected Person");
        removePersonBtn.setBackground(new Color(200, 50, 50));
        removePersonBtn.setForeground(Color.WHITE);
        removePersonBtn.addActionListener(e -> removeSelectedPeople(people));
        peoplePanel.add(removePersonBtn, BorderLayout.SOUTH);

        // Set up sorter for people table
        TableRowSorter<DefaultTableModel> peopleSorter = new TableRowSorter<>(peopleModel);
        people.setRowSorter(peopleSorter);
        peopleSearchBtn.addActionListener(e -> {
            String text = peopleSearchField.getText().trim();
            if (text.isEmpty()) {
                peopleSorter.setRowFilter(null);
            } else {
                peopleSorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text, 0));
            }
        });

        // Donations panel with search and remove button
        JPanel donationPanel = new JPanel(new BorderLayout(8, 8));
        donationPanel.setBackground(new Color(10, 14, 22));
        donationPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(40, 60, 90)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Search panel for donations
        JPanel donationSearchPanel = new JPanel(new BorderLayout(5, 5));
        donationSearchPanel.setBackground(new Color(10, 14, 22));
        JTextField donationSearchField = new JTextField();
        JButton donationSearchBtn = new JButton("Search");
        donationSearchBtn.setBackground(new Color(40, 160, 160));
        donationSearchBtn.setForeground(Color.WHITE);
        donationSearchPanel.add(donationSearchField, BorderLayout.CENTER);
        donationSearchPanel.add(donationSearchBtn, BorderLayout.EAST);

        // Label and search in north
        JPanel donationNorth = new JPanel(new BorderLayout());
        donationNorth.setBackground(new Color(10, 14, 22));
        JLabel donationLabel = new JLabel("Donations");
        donationLabel.setForeground(Color.WHITE);
        donationLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        donationNorth.add(donationLabel, BorderLayout.NORTH);
        donationNorth.add(donationSearchPanel, BorderLayout.SOUTH);
        donationPanel.add(donationNorth, BorderLayout.NORTH);

        donationPanel.add(new JScrollPane(donations), BorderLayout.CENTER);

        JButton removeDonationBtn = new JButton("Remove Selected Donation");
        removeDonationBtn.setBackground(new Color(200, 50, 50));
        removeDonationBtn.setForeground(Color.WHITE);
        removeDonationBtn.addActionListener(e -> removeSelectedDonations(donations));
        donationPanel.add(removeDonationBtn, BorderLayout.SOUTH);

        // Set up sorter for donations table
        TableRowSorter<DefaultTableModel> donationSorter = new TableRowSorter<>(donationModel);
        donations.setRowSorter(donationSorter);
        donationSearchBtn.addActionListener(e -> {
            String text = donationSearchField.getText().trim();
            if (text.isEmpty()) {
                donationSorter.setRowFilter(null);
            } else {
                donationSorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text, 0));
            }
        });

        p.add(peoplePanel);
        p.add(donationPanel);

        return p;
    }

    private void removeSelectedPeople(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            warn("No person selected to remove");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String nameToRemove = (String) peopleModel.getValueAt(modelRow, 0);

        // Remove from list by matching name (could be extended for more precise matching)
        needyPeople.removeIf(p -> p.name.equals(nameToRemove));

        peopleModel.removeRow(modelRow);
        info("Person removed successfully");
    }

    private void removeSelectedDonations(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            warn("No donation selected to remove");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String donorToRemove = (String) donationModel.getValueAt(modelRow, 0);
        // Note: Casting to double might fail if column 1 contains objects not castable to double.
        // For safety, retrieve as String and parse:
        try {
            double amountToRemove = Double.parseDouble(donationModel.getValueAt(modelRow, 1).toString());

            // Remove from list by matching donor and amount
            donations.removeIf(d -> d.donor.equals(donorToRemove) && d.amount == amountToRemove);

            donationModel.removeRow(modelRow);
            updateTotal();
            info("Donation removed successfully");
        } catch (NumberFormatException e) {
            warn("Error removing donation: Invalid amount format in table.");
        }
    }

    // --- Helpers (No Changes) ---
    private JTable createStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        return t;
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(18, 20, 30));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    private void addFormRow(JPanel p, int y, String label, JComponent field) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = y;
        JLabel l = new JLabel(label);
        l.setForeground(Color.WHITE);
        p.add(l, g);

        g.gridx = 1;
        p.add(field, g);
    }

    private JTextField field() {
        return new JTextField(20);
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(40, 160, 160));
        b.setForeground(Color.WHITE);
        return b;
    }

    private void updateTotal() {
        double sum = donations.stream().mapToDouble(d -> d.amount).sum();
        totalLabel.setText("₱ " + moneyFmt.format(sum));
    }

    private void clear(JTextField... f) {
        for (JTextField t : f) t.setText("");
    }

    private void info(String m) {
        JOptionPane.showMessageDialog(this, m);
    }

    private void warn(String m) {
        JOptionPane.showMessageDialog(this, m, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    // --- Load from DB (Modification applied) ---
    private void loadFromDatabase() {
        try {
            needyPeople.addAll(PersonDAO.fetchAll());
            donations.addAll(DonationDAO.fetchAll()); // Using the fixed DAO

            // Sort needyPeople alphabetically by name
            needyPeople.sort((p1, p2) -> p1.name.compareToIgnoreCase(p2.name));

            // Sort donations alphabetically by donor
            donations.sort((d1, d2) -> d1.donor.compareToIgnoreCase(d2.donor));

            for (Person p : needyPeople) {
                peopleModel.addRow(new Object[]{
                        p.name, p.address, p.needs, p.age, p.contact, p.category
                });
            }

            for (Donation d : donations) {
                // FIX: Removed d.date from the Object array
                donationModel.addRow(new Object[]{
                        d.donor, d.amount, d.description, d.type
                });
            }

            updateTotal();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                        "Database not available. Running offline mode. Error: " + e.getMessage(),
                        "Warning", JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Load MySQL JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                        "MySQL JDBC Driver not found. Please add it to your classpath.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new CharityApp().setVisible(true));
    }
}