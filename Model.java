*Client.java*
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

*Freelancer.java*
  package com.app.model;

public class Freelancer extends User {

    public Freelancer(int id, String name, String email) {
        super(id, name, email);
    }
}

*Invoice.java*
  package com.app.model;

import java.time.LocalDateTime;
import java.util.List;

public class Invoice {
    private int id;
    private int clientId;
    private double rate;
    private double totalAmount;
    private LocalDateTime createdAt;
    private List<TimeEntry> entries;

    public Invoice(int id, int clientId, double rate,
                   double totalAmount, LocalDateTime createdAt,
                   List<TimeEntry> entries) {
        this.id = id;
        this.clientId = clientId;
        this.rate = rate;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.entries = entries;
    }

    public int getId() { return id; }
    public int getClientId() { return clientId; }
    public double getRate() { return rate; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<TimeEntry> getEntries() { return entries; }
}
*Project.java*
  package com.app.model;

import java.time.LocalDate;

public class Project {
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

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getDeadline() { return deadline; }
    public int getClientId() { return clientId; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

*TimeEntry.java*
  package com.app.model;

import java.time.LocalDateTime;
import java.time.Duration;

public class TimeEntry {
    private int id;
    private int projectId;
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeEntry(int id, int projectId,
                     LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.projectId = projectId;
        this.start = start;
        this.end = end;
    }

    public int getId() { return id; }
    public int getProjectId() { return projectId; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }

    public double getHours() {
        return Duration.between(start, end).toMinutes() / 60.0;
    }
}

*User.java*
  package com.app.model;

public abstract class User {
    protected int id;
    protected String name;
    protected String email;

    public User() {}

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

  
  

