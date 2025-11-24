public class Main {
    public static void main(String[] args) {
        // Run GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Freelancer", "Client"};
            int choice = JOptionPane.showOptionDialog(null, "Login as:",
                    "Freelancer Time Management System",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) {
                // Open Freelancer Dashboard
                new FreelancerDashboard().setVisible(true);
            } else if (choice == 1) {
                // Ask which client is logging in
                ClientDAO clientDAO = new ClientDAO();
                List<Client> clients = clientDAO.getAllClients();
                if (clients.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No clients in database.");
                    return;
                }
                Client selected = (Client) JOptionPane.showInputDialog(
                        null,
                        "Select Client:",
                        "Client Login",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        clients.toArray(),
                        clients.get(0)
                );
                if (selected != null) {
                    new ClientDashboard(selected).setVisible(true);
                }
            }
        });
    }
}
