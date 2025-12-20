*clientDAO.java*
  package com.app.model;

public class Client extends User {

    public Client(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

*InvoiceDAO.java*
  package com.app.dao;

import com.app.model.Invoice;
import com.app.model.TimeEntry;
import com.app.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    public int insertInvoice(int clientId, double rate, double totalAmount,
                             List<TimeEntry> entries) {

        String invSql = "INSERT INTO invoices(client_id, rate, total_amount) VALUES (?,?,?)";
        String linkSql = "INSERT INTO invoice_entries(invoice_id, time_entry_id) VALUES (?,?)";
        int invoiceId = -1;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoiceId;
    }

    public List<Invoice> getInvoicesByClient(int clientId) {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE client_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Invoice(
                        rs.getInt("id"),
                        clientId,
                        rs.getDouble("rate"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        getInvoiceEntries(rs.getInt("id"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<TimeEntry> getInvoiceEntries(int invoiceId) {
        List<TimeEntry> list = new ArrayList<>();
        String sql =
                "SELECT t.* FROM time_entries t " +
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
*ProjectDAO.java*
  package com.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.app.model.Project;
import com.app.util.DBConnection;

public class ProjectDAO {

    public void insertProject(Project p) throws SQLException {

        String sql = "INSERT INTO projects " +
                     "(name, description, deadline, client_id, status) " +
                     "VALUES (?, ?, ?, ?, ?)";

        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, p.getName());
        ps.setString(2, p.getDescription());
        ps.setDate(3, java.sql.Date.valueOf(p.getDeadline()));
        ps.setInt(4, p.getClientId());
        ps.setString(5, p.getStatus());

        ps.executeUpdate();
    }
}
*TimeEntryDAO.java*
  package com.app.dao;

import com.app.model.TimeEntry;
import com.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimeEntryDAO {

    public void insertEntry(TimeEntry t) {
        String sql = "INSERT INTO time_entries(project_id, start_time, end_time) VALUES (?,?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, t.getProjectId());
            ps.setTimestamp(2, Timestamp.valueOf(t.getStart()));
            ps.setTimestamp(3, Timestamp.valueOf(t.getEnd()));
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TimeEntry> getEntriesForClient(int clientId) {
        List<TimeEntry> list = new ArrayList<>();
        String sql =
                "SELECT t.* FROM time_entries t " +
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
