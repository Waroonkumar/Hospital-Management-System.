import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

// OOP: Inheritance
abstract class Person {
    protected String name;
    public Person(String name) { this.name = name; }
    public String getName() { return name; }
}

// OOP: Encapsulation
class PatientEntity extends Person {
    private String id, disease;
    public PatientEntity(String id, String name, String disease) {
        super(name);
        this.id = id;
        this.disease = disease;
    }
}

public class HospitalManagement extends JFrame {
    
    // Database Configuration
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_db1";
    private static final String USER = "root"; 
    private static final String PASS = "NION_551@"; 

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private DefaultTableModel tableModel;

    public HospitalManagement() {
        setTitle("HEALOS v2.0 | Integrated Hospital Management");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupSidebar();
        setupPanels();
        setVisible(true);
    }

    private void setupSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(33, 37, 41));
        sidebar.setPreferredSize(new Dimension(230, 700));
        sidebar.setLayout(new GridLayout(6, 1, 10, 10));
        String[] navs = {"DASHBOARD", "ADMIT PATIENT", "DISCHARGE", "PATIENT RECORDS"};
        for (String n : navs) {
            JButton btn = new JButton(n);
            btn.setFocusPainted(false);
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(52, 58, 64));
            btn.addActionListener(e -> cardLayout.show(mainPanel, n));
            sidebar.add(btn);
        }
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupPanels() {
        mainPanel.add(createWelcomePanel(), "DASHBOARD");
        mainPanel.add(createAdmitPanel(), "ADMIT PATIENT");
        mainPanel.add(createDischargePanel(), "DISCHARGE");
        mainPanel.add(createRecordPanel(), "PATIENT RECORDS");
    }

    private JPanel createWelcomePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel("HOSPITAL COMMAND CENTER");
        lbl.setFont(new Font("Arial", Font.BOLD, 32));
        p.add(lbl);
        return p;
    }

    private JPanel createAdmitPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(248, 249, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField[] fields = {new JTextField(15), new JTextField(15), new JTextField(15), 
                               new JTextField(15), new JTextField(15), new JTextField(15)};
        String[] labels = {"Patient_ID:", "Name:", "Age:", "Disease:", "Doc ID:", "Appt ID:"};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            p.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; p.add(fields[i], gbc);
        }
        JButton btn = new JButton("CONFIRM ADMISSION");
        btn.setBackground(new Color(40, 167, 69));
        btn.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        p.add(btn, gbc);
        btn.addActionListener(e -> admitPatientToDB(fields[0].getText(), fields[1].getText(), fields[2].getText(), 
                             fields[3].getText(), fields[4].getText(), fields[5].getText()));
        return p;
    }

    private JPanel createDischargePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        JTextField tId = new JTextField(15);
        JButton btnRelease = new JButton("RELEASE PATIENT");
        JButton btnDelete = new JButton("DELETE PERMANENTLY");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnRelease.addActionListener(e -> dischargePatientFromDB(tId.getText()));
        btnDelete.addActionListener(e -> deletePatientPermanently(tId.getText()));
        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Enter Patient ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; p.add(tId, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; p.add(btnRelease, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; p.add(btnDelete, gbc);
        return p;
    }

    private JPanel createRecordPanel() {
        JPanel p = new JPanel(new BorderLayout());
        String[] cols = {"PID", "Name", "Disease", "ApptID", "Doctor", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        JPanel buttonPanel = new JPanel();
        JButton refresh = new JButton("REFRESH VIEW");
        JButton update = new JButton("UPDATE DISEASE");
        update.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String pid = table.getValueAt(row, 0).toString();
                String newDisease = JOptionPane.showInputDialog(this, "New Disease for Patient " + pid + ":");
                if (newDisease != null) updatePatientDisease(pid, newDisease);
            } else { JOptionPane.showMessageDialog(this, "Select a patient first."); }
        });
        refresh.addActionListener(e -> loadRecords());
        buttonPanel.add(refresh); buttonPanel.add(update);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(buttonPanel, BorderLayout.SOUTH);
        return p;
    }

    private void loadDriver() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver"); //
    }

    // Corrected logic to handle circular Foreign Key constraints
    private void admitPatientToDB(String pid, String name, String age, String disease, String did, String aid) {
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                conn.setAutoCommit(false); // ACID Transaction

                // 1. Insert Patient with NULL Appointment first
                String q_patient = "INSERT INTO Patient (PatientID, Name, Age, Disease, CurrentAppointmentID) VALUES (?,?,?,?, NULL)";
                try (PreparedStatement ps_p = conn.prepareStatement(q_patient)) {
                    ps_p.setString(1, pid); ps_p.setString(2, name); 
                    ps_p.setInt(3, Integer.parseInt(age)); ps_p.setString(4, disease);
                    ps_p.executeUpdate();
                }

                // 2. Insert Appointment
                String q_appt = "INSERT INTO Appointment (AppointmentID, PatientID, DoctorID, ApptDate, Status) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps_a = conn.prepareStatement(q_appt)) {
                    ps_a.setString(1, aid); ps_a.setString(2, pid); ps_a.setString(3, did);
                    ps_a.setDate(4, Date.valueOf(LocalDate.now())); ps_a.setString(5, "Active");
                    ps_a.executeUpdate();
                }

                // 3. Update Patient to link the Appointment
                String q_link = "UPDATE Patient SET CurrentAppointmentID = ? WHERE PatientID = ?";
                try (PreparedStatement ps_l = conn.prepareStatement(q_link)) {
                    ps_l.setString(1, aid); ps_l.setString(2, pid);
                    ps_l.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Admitted Successfully!");
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Admission Error: " + ex.getMessage()); }
    }

    private void updatePatientDisease(String pid, String disease) {
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement("UPDATE Patient SET Disease = ? WHERE PatientID = ?")) {
                ps.setString(1, disease); ps.setString(2, pid);
                if (ps.executeUpdate() > 0) { JOptionPane.showMessageDialog(this, "Updated!"); loadRecords(); }
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void dischargePatientFromDB(String pid) {
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                conn.setAutoCommit(false);
                PreparedStatement ps1 = conn.prepareStatement("UPDATE Patient SET CurrentAppointmentID = NULL WHERE PatientID = ?");
                ps1.setString(1, pid); ps1.executeUpdate();
                PreparedStatement ps2 = conn.prepareStatement("UPDATE Appointment SET Status = 'Discharged' WHERE PatientID = ? AND Status = 'Active'");
                ps2.setString(1, pid); ps2.executeUpdate();
                conn.commit();
                JOptionPane.showMessageDialog(this, "Discharged.");
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void deletePatientPermanently(String pid) {
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                conn.setAutoCommit(false);
                // Break Foreign Key link before deletion
                PreparedStatement ps0 = conn.prepareStatement("UPDATE Patient SET CurrentAppointmentID = NULL WHERE PatientID = ?");
                ps0.setString(1, pid); ps0.executeUpdate();
                PreparedStatement ps1 = conn.prepareStatement("DELETE FROM Appointment WHERE PatientID = ?");
                ps1.setString(1, pid); ps1.executeUpdate();
                PreparedStatement ps2 = conn.prepareStatement("DELETE FROM Patient WHERE PatientID = ?");
                ps2.setString(1, pid);
                int result = ps2.executeUpdate();
                conn.commit();
                if (result > 0) JOptionPane.showMessageDialog(this, "Removed.");
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void loadRecords() {
        tableModel.setRowCount(0);
        String sql = "SELECT p.PatientID, p.Name, p.Disease, p.CurrentAppointmentID, d.DoctorName, a.Status " +
                     "FROM Patient p LEFT JOIN Appointment a ON p.PatientID = a.PatientID " +
                     "LEFT JOIN Doctor d ON a.DoctorID = d.DoctorID";
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)});
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HospitalManagement());
    }
}