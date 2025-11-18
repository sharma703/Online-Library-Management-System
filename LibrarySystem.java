import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
public class LibrarySystem extends JFrame {

    static class Book {
        String id, title, author;
        boolean isIssued;
        String issuedTo;
        Date issueDate;

        Book(String id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.isIssued = false;
            this.issuedTo = "";
            this.issueDate = null;
        }
    }

    static class Member {
        String id, name, email;
        Member(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    ArrayList<Book> books = new ArrayList<>();
    ArrayList<Member> members = new ArrayList<>();
    JTable bookTable, memberTable;
    DefaultTableModel bookModel, memberModel;
    JTextArea statsArea;
    JLabel statusLabel;
    JTextField issueField, returnField;
    JLabel fineResult;

    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    public LibrarySystem() {
        setTitle("Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);

        setJMenuBar(createMenuBar());

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Books", createBookPanel());
        tabs.add("Members", createMemberPanel());
        tabs.add("Fine Calculator", createFineCalcPanel());
        tabs.add("Statistics", createStatsPanel());
        add(tabs, BorderLayout.CENTER);

        statusLabel = new JLabel("Welcome to Library Management System");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        preloadBooks();
        preloadMembers();
        refreshBooks();
        refreshMembers();

        JOptionPane.showMessageDialog(this,
                "Welcome to the Library Management System!\nYou can add, issue, return, and calculate fines.",
                "Library Project", JOptionPane.INFORMATION_MESSAGE);

        setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu actions = new JMenu("Actions");
        JMenuItem issue = new JMenuItem("Issue Book");
        JMenuItem ret = new JMenuItem("Return Book");
        JMenuItem calc = new JMenuItem("Fine Calculator");
        JMenuItem exit = new JMenuItem("Exit");

        issue.addActionListener(e -> issueBook());
        ret.addActionListener(e -> returnBook());
        calc.addActionListener(e -> showFineTab());
        exit.addActionListener(e -> System.exit(0));

        actions.add(issue);
        actions.add(ret);
        actions.add(calc);
        actions.addSeparator();
        actions.add(exit);

        JMenu manage = new JMenu("Manage");
        JMenuItem addBook = new JMenuItem("Add Book");
        JMenuItem addMember = new JMenuItem("Add Member");

        addBook.addActionListener(e -> addBook());
        addMember.addActionListener(e -> addMember());

        manage.add(addBook);
        manage.add(addMember);

        bar.add(actions);
        bar.add(manage);

        return bar;
    }

    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        bookModel = new DefaultTableModel(new String[]{
                "Book ID", "Title", "Author", "Status", "Issued To", "Issue Date", "Fine (₹)"
        }, 0);
        bookTable = new JTable(bookModel);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton add = new JButton("Add Book");
        JButton remove = new JButton("Remove Book");
        JButton issue = new JButton("Issue");
        JButton ret = new JButton("Return");
        JButton refresh = new JButton("Refresh");

        add.addActionListener(e -> addBook());
        remove.addActionListener(e -> removeBook());
        issue.addActionListener(e -> issueBook());
        ret.addActionListener(e -> returnBook());
        refresh.addActionListener(e -> refreshBooks());

        buttons.add(add);
        buttons.add(remove);
        buttons.add(issue);
        buttons.add(ret);
        buttons.add(refresh);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMemberPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        memberModel = new DefaultTableModel(new String[]{"Member ID", "Name", "Email"}, 0);
        memberTable = new JTable(memberModel);
        panel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton add = new JButton("Add Member");
        JButton remove = new JButton("Remove");
        JButton refresh = new JButton("Refresh");

        add.addActionListener(e -> addMember());
        remove.addActionListener(e -> removeMember());
        refresh.addActionListener(e -> refreshMembers());

        buttons.add(add);
        buttons.add(remove);
        buttons.add(refresh);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createFineCalcPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel issueLbl = new JLabel("Issue Date (dd-MM-yyyy):");
        JLabel returnLbl = new JLabel("Return Date (dd-MM-yyyy):");
        issueField = new JTextField("01-01-2025");
        returnField = new JTextField("10-01-2025");
        JButton calcBtn = new JButton("Calculate Fine");
        fineResult = new JLabel("Fine: ₹0");

        calcBtn.addActionListener(e -> calculateManualFine());

        c.gridx = 0; c.gridy = 0; panel.add(issueLbl, c);
        c.gridx = 1; panel.add(issueField, c);
        c.gridx = 0; c.gridy = 1; panel.add(returnLbl, c);
        c.gridx = 1; panel.add(returnField, c);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; panel.add(calcBtn, c);
        c.gridy = 3; panel.add(fineResult, c);

        panel.setBorder(BorderFactory.createTitledBorder("Fine Calculator"));
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statsArea = new JTextArea();
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        statsArea.setEditable(false);

        JButton refresh = new JButton("Update Stats");
        refresh.addActionListener(e -> updateStats());

        panel.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        panel.add(refresh, BorderLayout.SOUTH);
        return panel;
    }

    private void addBook() {
        String id = "B" + (books.size() + 1);
        String title = JOptionPane.showInputDialog("Enter Book Title:");
        if (title == null || title.trim().isEmpty()) return;
        String author = JOptionPane.showInputDialog("Enter Author Name:");
        if (author == null || author.trim().isEmpty()) return;

        books.add(new Book(id, title.trim(), author.trim()));
        refreshBooks();
        showStatus("Added Book: " + title);
    }

    private void issueBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book first!");
            return;
        }

        Book b = books.get(row);
        if (b.isIssued) {
            JOptionPane.showMessageDialog(this, "Book already issued!");
            return;
        }

        String memId = JOptionPane.showInputDialog("Enter Member ID:");
        Member m = findMember(memId);
        if (m == null) {
            JOptionPane.showMessageDialog(this, "No such member!");
            return;
        }

        b.isIssued = true;
        b.issuedTo = m.name;
        b.issueDate = new Date();

        refreshBooks();
        showStatus("Issued book to " + m.name);
    }

    private void returnBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book first!");
            return;
        }

        Book b = books.get(row);
        if (!b.isIssued) {
            JOptionPane.showMessageDialog(this, "Book not issued!");
            return;
        }

        long fine = calculateFine(b.issueDate);
        JOptionPane.showMessageDialog(this,
                "Book: " + b.title + "\nIssued to: " + b.issuedTo +
                        "\nFine Due: ₹" + fine, "Return Summary", JOptionPane.INFORMATION_MESSAGE);

        b.isIssued = false;
        b.issuedTo = "";
        b.issueDate = null;

        refreshBooks();
        showStatus("Returned Book. Fine ₹" + fine);
    }

    private void removeBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) return;
        Book b = books.get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "Remove \"" + b.title + "\"?");
        if (confirm == JOptionPane.YES_OPTION) {
            books.remove(b);
            refreshBooks();
        }
    }

    private void addMember() {
        String id = "M" + (members.size() + 1);
        String name = JOptionPane.showInputDialog("Enter Member Name:");
        if (name == null || name.trim().isEmpty()) return;
        String email = JOptionPane.showInputDialog("Enter Email:");
        if (email == null || email.trim().isEmpty()) return;

        members.add(new Member(id, name.trim(), email.trim()));
        refreshMembers();
        showStatus("Added Member: " + name);
    }

    private void removeMember() {
        int row = memberTable.getSelectedRow();
        if (row == -1) return;
        Member m = members.get(row);
        int c = JOptionPane.showConfirmDialog(this, "Remove member " + m.name + "?");
        if (c == JOptionPane.YES_OPTION) {
            members.remove(m);
            refreshMembers();
        }
    }

    private Member findMember(String id) {
        for (Member m : members)
            if (m.id.equalsIgnoreCase(id)) return m;
        return null;
    }

    private void refreshBooks() {
        bookModel.setRowCount(0);
        for (Book b : books) {
            long fine = b.isIssued ? calculateFine(b.issueDate) : 0;
            bookModel.addRow(new Object[]{
                    b.id, b.title, b.author,
                    b.isIssued ? "Issued" : "Available",
                    b.issuedTo.isEmpty() ? "-" : b.issuedTo,
                    b.issueDate == null ? "-" : DATE_FORMAT.format(b.issueDate),
                    fine
            });
        }
    }

    private void refreshMembers() {
        memberModel.setRowCount(0);
        for (Member m : members)
            memberModel.addRow(new Object[]{m.id, m.name, m.email});
    }

    private void showStatus(String msg) {
        statusLabel.setText(msg);
    }

    private void updateStats() {
        int total = books.size();
        int issued = (int) books.stream().filter(b -> b.isIssued).count();
        int available = total - issued;

        StringBuilder sb = new StringBuilder();
        sb.append("------ Library Statistics ------\n\n");
        sb.append("Total Books     : ").append(total).append("\n");
        sb.append("Books Issued    : ").append(issued).append("\n");
        sb.append("Books Available : ").append(available).append("\n");
        sb.append("Total Members   : ").append(members.size()).append("\n\n");

        sb.append("Issued Books:\n");
        for (Book b : books) {
            if (b.isIssued) {
                sb.append("• ").append(b.title)
                        .append(" → ").append(b.issuedTo)
                        .append(" | Date: ").append(DATE_FORMAT.format(b.issueDate))
                        .append(" | Fine: ₹").append(calculateFine(b.issueDate)).append("\n");
            }
        }

        if (issued == 0) sb.append("No books are currently issued.\n");

        statsArea.setText(sb.toString());
        showStatus("Stats updated");
    }

    private long calculateFine(Date issueDate) {
        if (issueDate == null) return 0;
        long diff = new Date().getTime() - issueDate.getTime();
        long days = diff / (1000 * 60 * 60 * 24);
        return days > 7 ? (days - 7) * 10 : 0;
    }

    private void calculateManualFine() {
        try {
            Date issueDate = DATE_FORMAT.parse(issueField.getText().trim());
            Date returnDate = DATE_FORMAT.parse(returnField.getText().trim());
            long diff = returnDate.getTime() - issueDate.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            long fine = days > 7 ? (days - 7) * 10 : 0;
            fineResult.setText("Fine: ₹" + fine + "  (" + days + " days)");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use dd-MM-yyyy");
        }
    }

    private void preloadBooks() {
        books.add(new Book("B1", "The Alchemist", "Paulo Coelho"));
        books.add(new Book("B2", "“Wings of Fire", "Dr. A.P.J. Abdul Kalam"));
        books.add(new Book("B3", "The Power of Your Subconscious Mind", "Joseph Murphy"));
        books.add(new Book("B4", "Think Like a Monk", "Jay Shetty"));
        books.add(new Book("B5", "Rich Dad Poor Dad", "Robert T. Kiyosaki"));
    }

    private void preloadMembers() {
        members.add(new Member("M1", "Riya Sharma", "riya@gmail.com"));
        members.add(new Member("M2", "Aman Verma", "aman@gmail.com"));
        members.add(new Member("M3", "Neha Singh", "neha@gmail.com"));
    }

    private void showFineTab() {
        JOptionPane.showMessageDialog(this,
                "Switch to the 'Fine Calculator' tab to use the manual fine calculator.",
                "Fine Calculator", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibrarySystem::new);
    }
}