import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/* ============================================================
   2. JDBC CONNECTION HELPER (Singleton + Synchronization)
   ============================================================ */
class DBConnection {
    // TODO: CHANGE THESE VALUES FOR YOUR MYSQL
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/freelancer_db";
    private static final String DB_USER = "root";       // your username
    private static final String DB_PASS = "123456";       // your password

    private static Connection connection;

    // synchronized ensures thread-safe (used for multithreading mark)
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }
        return connection;
    }
}

/* ============================================================
   3. MODEL CLASSES (OOP: Inheritance, Encapsulation, Methods)
   ============================================================ */

// Base User class (used for Freelancer and Client)
abstract class User {
    protected int id;
    protected String name;
    protected String email;

    public User() {}

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId()    { return id;    }
    public String getName(){ return name; }
    public String getEmail(){return email;}
}

// Freelancer extends User (Inheritance)
class Freelancer extends User {
    public Freelancer(int id, String name, String email) {
        super(id, name, email);
    }
}

// Client extends User (Inheritance)
class Client extends User {
    public Client(int id, String name, String email) {
        super(id, name, email);
    }
    @Override
    public String toString() {
        // useful for JComboBox display
        return id + " - " + name;
    }
}

// Project entity
class Project {
    private int id;
    private String name;
    private String description;
    private LocalDate deadline;
    private int clientId;
    private String status;

    public Project(int id, String name, String description,
                   LocalDate deadline, int clientId, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.clientId = clientId;
        this.status = status;
    }

    public int getId()          { return id; }
    public String getName()     { return name; }
    public String getDescription(){ return description; }
    public LocalDate getDeadline(){ return deadline; }
    public int getClientId()    { return clientId; }
    public String getStatus()   { return status; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

// TimeEntry entity for billable hours
class TimeEntry {
    private int id;
    private int projectId;
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeEntry(int id, int projectId, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.projectId = projectId;
        this.start = start;
        this.end = end;
    }

    public int getId()        { return id; }
    public int getProjectId() { return projectId; }
    public LocalDateTime getStart(){ return start; }
    public LocalDateTime getEnd()  { return end; }

    // Helper: calculate hours between start & end
    public double getHours() {
        return java.time.Duration.between(start, end).toMinutes() / 60.0;
    }
}

// Invoice entity
class Invoice {
    private int id;
    private int clientId;
    private double rate;
    private double totalAmount;
    private LocalDateTime createdAt;
    private List<TimeEntry> entries;

    public Invoice(int id, int clientId, double rate, double totalAmount,
                   LocalDateTime createdAt, List<TimeEntry> entries) {
        this.id = id;
        this.clientId = clientId;
        this.rate = rate;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.entries = entries;
    }

    public int getId()            { return id; }
    public int getClientId()      { return clientId; }
    public double getRate()       { return rate; }
    public double getTotalAmount(){ return totalAmount; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public List<TimeEntry> getEntries(){ return entries; }
}

/* ============================================================
   4. DAO CLASSES (DATABASE OPERATIONS WITH JDBC)
   ============================================================ */

// CLIENT DAO
class ClientDAO {
    // Get all clients
    public List<Client> getAllClients() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Get client by ID
    public Client getClientById(int id) {
        String sql = "SELECT * FROM clients WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}

// PROJECT DAO
class ProjectDAO {
    // Get all projects
    public List<Project> getAllProjects() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Get projects for a specific client
    public List<Project> getProjectsByClient(int clientId) {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE client_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Insert new project
    public void insertProject(Project p) {
        String sql = "INSERT INTO projects(name,description,deadline,client_id,status) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(p.getDeadline()));
            ps.setInt(4, p.getClientId());
            ps.setString(5, p.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Helper to map DB row to Project object
    private Project mapRow(ResultSet rs) throws SQLException {
        return new Project(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("deadline").toLocalDate(),
                rs.getInt("client_id"),
                rs.getString("status")
        );
    }
}

// TIME ENTRY DAO
class TimeEntryDAO {
    // Insert new time entry
    public void insertEntry(TimeEntry t) {
        String sql = "INSERT INTO time_entries(project_id,start_time,end_time) VALUES (?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, t.getProjectId());
            ps.setTimestamp(2, Timestamp.valueOf(t.getStart()));
            ps.setTimestamp(3, Timestamp.valueOf(t.getEnd()));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Get entries by project
    public List<TimeEntry> getEntriesByProject(int projectId) {
        List<TimeEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM time_entries WHERE project_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new TimeEntry(
                        rs.getInt("id"),
                        rs.getInt("project_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime()
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Get all time entries for a specific client (join projects)
    public List<TimeEntry> getEntriesForClient(int clientId) {
        List<TimeEntry> list = new ArrayList<>();
        String sql = "SELECT t.* FROM time_entries t " +
                "JOIN projects p ON t.project_id = p.id " +
                "WHERE p.client_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new TimeEntry(
                        rs.getInt("id"),
                        rs.getInt("project_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime()
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}

// INVOICE DAO
class InvoiceDAO {

    // Insert invoice and link its time entries
    public int insertInvoice(int clientId, double rate, double totalAmount,
                             List<TimeEntry> entries) {
        String invSql  = "INSERT INTO invoices(client_id,rate,total_amount) VALUES (?,?,?)";
        String linkSql = "INSERT INTO invoice_entries(invoice_id,time_entry_id) VALUES (?,?)";
        int invoiceId = -1;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false); // transaction
            try (PreparedStatement ps = con.prepareStatement(invSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, clientId);
                ps.setDouble(2, rate);
                ps.setDouble(3, totalAmount);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) invoiceId = rs.getInt(1);
            }
            try (PreparedStatement ps2 = con.prepareStatement(linkSql)) {
                for (TimeEntry t : entries) {
                    ps2.setInt(1, invoiceId);
                    ps2.setInt(2, t.getId());
                    ps2.addBatch();
                }
                ps2.executeBatch();
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) { e.printStackTrace(); }
        return invoiceId;
    }

    // Get all invoices for specific client
    public List<Invoice> getInvoicesByClient(int clientId) {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE client_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                double rate = rs.getDouble("rate");
                double total = rs.getDouble("total_amount");
                LocalDateTime created = rs.getTimestamp("created_at").toLocalDateTime();
                List<TimeEntry> entries = getInvoiceEntries(id);
                list.add(new Invoice(id, clientId, rate, total, created, entries));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Helper to get all time entries belonging to one invoice
    private List<TimeEntry> getInvoiceEntries(int invoiceId) {
        List<TimeEntry> list = new ArrayList<>();
        String sql = "SELECT t.* FROM time_entries t " +
                "JOIN invoice_entries ie ON t.id = ie.time_entry_id " +
                "WHERE ie.invoice_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new TimeEntry(
                        rs.getInt("id"),
                        rs.getInt("project_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime()
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}

/* ============================================================
   5. FREELANCER DASHBOARD (Swing GUI)
   ============================================================ */

class FreelancerDashboard extends JFrame {
    private final ProjectDAO projectDAO   = new ProjectDAO();
    private final ClientDAO clientDAO     = new ClientDAO();
    private final TimeEntryDAO timeDAO    = new TimeEntryDAO();
    private final InvoiceDAO invoiceDAO   = new InvoiceDAO();

    private JComboBox<Project> projectCombo;
    private JTable projectTable;
    private DefaultTableModel projectModel;

    private JTextField startField, endField;

    private JTextArea messageArea;
    private JComboBox<Client> invoiceClientCombo;
    private JTextField rateField;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FreelancerDashboard() {
        setTitle("Freelancer Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Billable Hours Tracking", createTimeTrackingPanel());   // Time entries
        tabs.add("Project Management", createProjectPanel());            // Project table + add form
        tabs.add("Invoice Generation", createInvoicePanel());            // Generate invoice
        tabs.add("Client Communication", createCommunicationPanel());    // Send messages
        add(tabs);

        // MULTITHREADING: background thread refreshes projects every 30s
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    SwingUtilities.invokeLater(this::loadProjects);
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    // ---------- TAB: Time Tracking ----------
    private JPanel createTimeTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        projectCombo = new JComboBox<>();
        loadProjectsIntoCombo();

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("Project:"));
        form.add(projectCombo);

        form.add(new JLabel("Start (yyyy-MM-dd HH:mm):"));
        startField = new JTextField();
        form.add(startField);

        form.add(new JLabel("End (yyyy-MM-dd HH:mm):"));
        endField = new JTextField();
        form.add(endField);

        JButton saveBtn = new JButton("Save Time Entry");
        saveBtn.addActionListener(e -> saveTimeEntry());
        form.add(new JLabel());
        form.add(saveBtn);

        panel.add(form, BorderLayout.NORTH);
        return panel;
    }

    // ---------- TAB: Project Management ----------
    private JPanel createProjectPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        projectModel = new DefaultTableModel(new Object[]{"ID","Name","Deadline","Client","Status"},0);
        projectTable = new JTable(projectModel);
        loadProjects();
        panel.add(new JScrollPane(projectTable), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(5,2,5,5));
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField deadlineField = new JTextField("2025-12-31");
        JComboBox<Client> clientCombo = new JComboBox<>();
        for (Client c : clientDAO.getAllClients()) clientCombo.addItem(c);

        form.add(new JLabel("Name:")); form.add(nameField);
        form.add(new JLabel("Description:")); form.add(descField);
        form.add(new JLabel("Deadline (yyyy-MM-dd):")); form.add(deadlineField);
        form.add(new JLabel("Client:")); form.add(clientCombo);

        JButton addBtn = new JButton("Add Project");
        addBtn.addActionListener(e -> {
            try {
                Client c = (Client) clientCombo.getSelectedItem();
                LocalDate dl = LocalDate.parse(deadlineField.getText());
                Project p = new Project(0, nameField.getText(), descField.getText(), dl, c.getId(), "In Progress");
                projectDAO.insertProject(p);
                JOptionPane.showMessageDialog(this, "Project created successfully.");
                loadProjects();
                loadProjectsIntoCombo();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        form.add(new JLabel()); form.add(addBtn);
        panel.add(form, BorderLayout.SOUTH);

        return panel;
    }

    // ---------- TAB: Invoice Generation ----------
    private JPanel createInvoicePanel() {
        JPanel panel = new JPanel(new GridLayout(4,2,5,5));
        invoiceClientCombo = new JComboBox<>();
        for (Client c : clientDAO.getAllClients()) invoiceClientCombo.addItem(c);

        rateField = new JTextField("30");   // default hourly rate

        panel.add(new JLabel("Client:")); panel.add(invoiceClientCombo);
        panel.add(new JLabel("Hourly Rate:")); panel.add(rateField);

        JButton genBtn = new JButton("Generate Invoice (for all time entries of client)");
        genBtn.addActionListener(e -> generateInvoice());
        panel.add(new JLabel()); panel.add(genBtn);

        return panel;
    }

    // ---------- TAB: Client Communication ----------
    private JPanel createCommunicationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JComboBox<Client> clientCombo = new JComboBox<>();
        for (Client c : clientDAO.getAllClients()) clientCombo.addItem(c);
        panel.add(clientCombo, BorderLayout.NORTH);

        messageArea = new JTextArea(8, 40);
        panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send Message");
        sendBtn.addActionListener(e -> {
            Client c = (Client) clientCombo.getSelectedItem();
            JOptionPane.showMessageDialog(this,
                    "Message sent to " + c.getName() + ":\n" + messageArea.getText());
            messageArea.setText("");
        });
        panel.add(sendBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- Helper methods for Freelancer Dashboard ----------

    // Reload projects table
    private void loadProjects() {
        if (projectModel == null) return;
        projectModel.setRowCount(0);
        List<Project> projects = projectDAO.getAllProjects();
        for (Project p : projects) {
            Client c = clientDAO.getClientById(p.getClientId());
            projectModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getDeadline(),
                    c != null ? c.getName() : "",
                    p.getStatus()
            });
        }
    }

    // Reload project combo box
    private void loadProjectsIntoCombo() {
        if (projectCombo == null) return;
        projectCombo.removeAllItems();
        for (Project p : projectDAO.getAllProjects()) projectCombo.addItem(p);
    }

    // Save time entry to DB
    private void saveTimeEntry() {
        try {
            Project p = (Project) projectCombo.getSelectedItem();
            LocalDateTime start = LocalDateTime.parse(startField.getText(), dtf);
            LocalDateTime end = LocalDateTime.parse(endField.getText(), dtf);
            if (end.isBefore(start)) throw new RuntimeException("End time before start time.");
            TimeEntry t = new TimeEntry(0, p.getId(), start, end);
            timeDAO.insertEntry(t);
            JOptionPane.showMessageDialog(this, "Time entry saved successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Generate invoice for one client
    private void generateInvoice() {
        try {
            Client c = (Client) invoiceClientCombo.getSelectedItem();
            double rate = Double.parseDouble(rateField.getText());
            List<TimeEntry> entries = timeDAO.getEntriesForClient(c.getId());

            if (entries.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No time entries for selected client.");
                return;
            }

            double hours = entries.stream().mapToDouble(TimeEntry::getHours).sum();
            double total = hours * rate;
            int invoiceId = invoiceDAO.insertInvoice(c.getId(), rate, total, entries);

            JOptionPane.showMessageDialog(this,
                    "Invoice #" + invoiceId + " generated.\nHours: " + hours + "\nTotal: $" + total);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}

/* ============================================================
   6. CLIENT DASHBOARD (View Invoices & Project Status)
   ============================================================ */

class ClientDashboard extends JFrame {
    private final Client client;
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final TimeEntryDAO timeDAO = new TimeEntryDAO();

    private DefaultTableModel projectModel;
    private DefaultTableModel invoiceModel;

    public ClientDashboard(Client client) {
        this.client = client;

        setTitle("Client Dashboard - " + client.getName());
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Project Status Review", createProjectStatusPanel());
        tabs.add("View Invoices", createInvoicePanel());
        add(tabs);
    }

    // Tab: Project Status
    private JPanel createProjectStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        projectModel = new DefaultTableModel(new Object[]{"ID","Name","Deadline","Status","Total Hours"},0);
        JTable table = new JTable(projectModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        loadProjectStatus();
        return panel;
    }

    // Tab: Invoices
    private JPanel createInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        invoiceModel = new DefaultTableModel(new Object[]{"ID","Rate","Total Amount","Created At"},0);
        JTable table = new JTable(invoiceModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        loadInvoices();
        return panel;
    }

    // Fill project status table
    private void loadProjectStatus() {
        projectModel.setRowCount(0);
        List<Project> projects = projectDAO.getProjectsByClient(client.getId());
        for (Project p : projects) {
            double hours = timeDAO.getEntriesByProject(p.getId()).stream()
                    .mapToDouble(TimeEntry::getHours).sum();
            projectModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getDeadline(),
                    p.getStatus(),
                    hours
            });
        }
    }

    // Fill invoice table
    private void loadInvoices() {
        invoiceModel.setRowCount(0);
        List<Invoice> invoices = invoiceDAO.getInvoicesByClient(client.getId());
        for (Invoice inv : invoices) {
            invoiceModel.addRow(new Object[]{
                    inv.getId(),
                    inv.getRate(),
                    inv.getTotalAmount(),
                    inv.getCreatedAt()
            });
        }
    }
}
