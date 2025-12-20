*InvoiceServlet.java*
  package com.app.servlet;

import com.app.dao.InvoiceDAO;
import com.app.dao.TimeEntryDAO;
import com.app.model.TimeEntry;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/invoice")
public class InvoiceServlet extends HttpServlet {

    private InvoiceDAO invoiceDAO;
    private TimeEntryDAO timeDAO;

    @Override
    public void init() {
        invoiceDAO = new InvoiceDAO();
        timeDAO = new TimeEntryDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int clientId = Integer.parseInt(request.getParameter("clientId"));
        double rate = Double.parseDouble(request.getParameter("rate"));

        List<TimeEntry> entries = timeDAO.getEntriesForClient(clientId);
        double hours = entries.stream().mapToDouble(TimeEntry::getHours).sum();
        double total = hours * rate;

        invoiceDAO.insertInvoice(clientId, rate, total, entries);

        response.sendRedirect("dashboard.jsp");
    }

  *LoginServlet.java*
package com.app.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String role = request.getParameter("role");

        HttpSession session = request.getSession();
        session.setAttribute("role", role);

        response.sendRedirect("dashboard.jsp");
    }
}

*ProjectServlet.java*
  package com.app.servlet;

import com.app.dao.ProjectDAO;
import com.app.model.Project;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/project")
public class ProjectServlet extends HttpServlet {

    private ProjectDAO projectDAO;

    @Override
    public void init() {
        projectDAO = new ProjectDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String desc = request.getParameter("description");
        LocalDate deadline = LocalDate.parse(request.getParameter("deadline"));
        int clientId = Integer.parseInt(request.getParameter("clientId"));

        Project p = new Project(0, name, desc, deadline, clientId, "In Progress");
        try {
			projectDAO.insertProject(p);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        response.sendRedirect("projects.jsp");
    }
}

*TimeEntryServlet.java*
  package com.app.servlet;

import com.app.dao.TimeEntryDAO;
import com.app.model.TimeEntry;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/timeEntry")
public class TimeEntryServlet extends HttpServlet {

    private TimeEntryDAO timeDAO;

    @Override
    public void init() {
        timeDAO = new TimeEntryDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int projectId = Integer.parseInt(request.getParameter("projectId"));
        LocalDateTime start = LocalDateTime.parse(request.getParameter("start"));
        LocalDateTime end = LocalDateTime.parse(request.getParameter("end"));

        timeDAO.insertEntry(new TimeEntry(0, projectId, start, end));

        response.sendRedirect("dashboard.jsp");
    }
}



}
