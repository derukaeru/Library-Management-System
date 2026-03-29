import java.io.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LibraryManagement {

  // =========================================================
  //  CONSTANTS
  // =========================================================

  static final int BORROW_LIMIT_DAYS = 7;
  static final double LATE_FEE_PER_DAY = 5.0;
  static final double LOST_BOOK_PENALTY = 200.0;
  static final double DAMAGED_BOOK_FEE = 50.0;
  static final String DATE_FMT = "yyyy-MM-dd";
  static final String DATETIME_FMT = "yyyy-MM-dd HH:mm:ss";
  static final String ADMIN_USERNAME = "admin";
  static final String ADMIN_PASSWORD = "admin123";

  enum BookStatus {
    AVAILABLE,
    BORROWED,
    LOST,
    DAMAGED,
  }

  static class Book {

    String accessionNo;
    String title;
    String author;
    String genre;
    int year;
    BookStatus status;
    String condition;

    Book(
      String accessionNo,
      String title,
      String author,
      String genre,
      int year
    ) {
      this.accessionNo = accessionNo;
      this.title = title;
      this.author = author;
      this.genre = genre;
      this.year = year;
      this.status = BookStatus.AVAILABLE;
      this.condition = "Good";
    }
  }

  enum TransactionType {
    BORROW,
    RETURN,
    LOST,
    DAMAGED,
  }

  static class Transaction {

    String id;
    String studentId;
    String accessionNo;
    TransactionType type;
    String dateTime;
    String dueDate;
    double penaltyApplied;
    String notes;

    Transaction(
      String id,
      String studentId,
      String accessionNo,
      TransactionType type,
      String dateTime
    ) {
      this.id = id;
      this.studentId = studentId;
      this.accessionNo = accessionNo;
      this.type = type;
      this.dateTime = dateTime;
      this.penaltyApplied = 0.0;
      this.notes = "";
    }
  }

  static class Student {

    String studentId;
    String password;
    String firstName;
    String lastName;
    String gradeSection;
    String contactNo;
    double pendingFee;
    List<String> activeBorrows = new ArrayList<>();

    Student(
      String studentId,
      String password,
      String firstName,
      String lastName,
      String gradeSection,
      String contactNo
    ) {
      this.studentId = studentId;
      this.password = password;
      this.firstName = firstName;
      this.lastName = lastName;
      this.gradeSection = gradeSection;
      this.contactNo = contactNo;
      this.pendingFee = 0.0;
    }

    String fullName() {
      return firstName + " " + lastName;
    }
  }

  static class BorrowSlot {

    String studentId;
    String accessionNo;
    String borrowDateTime;
    String dueDate;

    BorrowSlot(
      String studentId,
      String accessionNo,
      String borrowDateTime,
      String dueDate
    ) {
      this.studentId = studentId;
      this.accessionNo = accessionNo;
      this.borrowDateTime = borrowDateTime;
      this.dueDate = dueDate;
    }
  }

  static Map<String, Book> bookDB = new LinkedHashMap<>();
  static Map<String, Student> studentDB = new LinkedHashMap<>();
  static List<Transaction> transactions = new ArrayList<>();
  static Map<String, BorrowSlot> activeLoans = new HashMap<>();
  static int txCounter = 1000;

  static BufferedReader reader = new BufferedReader(
    new InputStreamReader(System.in)
  );

  static void seedData() {
    bookDB.put(
      "ICN-001",
      new Book(
        "ICN-001",
        "Noli Me Tangere",
        "Jose Rizal",
        "Filipino Literature",
        1887
      )
    );
    bookDB.put(
      "ICN-004",
      new Book(
        "ICN-004",
        "Introduction to Programming",
        "Dean Tucker",
        "Computer Science",
        2020
      )
    );
    bookDB.put(
      "ICN-009",
      new Book("ICN-009", "The Art of War", "Sun Tzu", "Philosophy", 500)
    );
    bookDB.put(
      "ICN-011",
      new Book("ICN-011", "Biology: The Core", "Eric Simon", "Science", 2019)
    );

    studentDB.put(
      "2024-0001",
      new Student(
        "2024-0001",
        "pass123",
        "Dale",
        "Despi",
        "Grade 11 - ICT PROGRAMMING",
        "09171234567"
      )
    );

    String pastBorrow = LocalDate.now()
      .minusDays(10)
      .format(DateTimeFormatter.ofPattern(DATE_FMT));
    String pastDue = LocalDate.now()
      .minusDays(3)
      .format(DateTimeFormatter.ofPattern(DATE_FMT));

    Book b4 = bookDB.get("ICN-004");
    b4.status = BookStatus.BORROWED;
    Student s1 = studentDB.get("2024-0001");
    s1.activeBorrows.add("ICN-004");
    activeLoans.put(
      "ICN-004",
      new BorrowSlot("2024-0001", "ICN-004", pastBorrow + " 08:00:00", pastDue)
    );

    Transaction seedTx = new Transaction(
      "TX-0999",
      "2024-0001",
      "ICN-004",
      TransactionType.BORROW,
      pastBorrow + " 08:00:00"
    );
    seedTx.dueDate = pastDue;
    transactions.add(seedTx);
  }

  static String input(String prompt) throws IOException {
    System.out.print(prompt);
    String line = reader.readLine();
    return (line == null) ? "" : line.trim();
  }

  static void pause() throws IOException {
    input("\nPress Enter to continue...");
  }

  static void divider() {
    System.out.println("==================================================");
  }

  static void thin() {
    System.out.println("--------------------------------------------------");
  }

  static String nowDateTime() {
    return LocalDateTime.now().format(
      DateTimeFormatter.ofPattern(DATETIME_FMT)
    );
  }

  static String todayDate() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FMT));
  }

  static String dueDateFrom(String fromDate) {
    try {
      LocalDate d = LocalDate.parse(
        fromDate,
        DateTimeFormatter.ofPattern(DATE_FMT)
      );
      return d
        .plusDays(BORROW_LIMIT_DAYS)
        .format(DateTimeFormatter.ofPattern(DATE_FMT));
    } catch (Exception e) {
      return todayDate();
    }
  }

  static long daysOverdue(String dueDate) {
    try {
      LocalDate due = LocalDate.parse(
        dueDate,
        DateTimeFormatter.ofPattern(DATE_FMT)
      );
      return Math.max(0, ChronoUnit.DAYS.between(due, LocalDate.now()));
    } catch (Exception e) {
      return 0;
    }
  }

  static String nextTxId() {
    return "TX-" + (++txCounter);
  }

  static String truncate(String s, int max) {
    if (s == null) return "";
    return (s.length() <= max) ? s : s.substring(0, max - 1) + ".";
  }

  // =========================================================
  //  MAIN
  // =========================================================

  public static void main(String[] args) throws IOException {
    seedData();

    while (true) {
      divider();
      System.out.println("  INFORMATICS COLLEGE NORTHGATE, INC.");
      System.out.println("  Library Management System");
      divider();
      System.out.println("1. Admin Login");
      System.out.println("2. Student Login");
      System.out.println("3. Register New Student");
      System.out.println("0. Exit");
      thin();

      String choice = input("Select: ");
      switch (choice) {
        case "1" -> adminLogin();
        case "2" -> studentLogin();
        case "3" -> registerStudent();
        case "0" -> {
          System.out.println("\nSystem closed. Goodbye.");
          return;
        }
        default -> System.out.println("Invalid option. Try again.");
      }
    }
  }

  // =========================================================
  //  STUDENT REGISTRATION
  // =========================================================

  static void registerStudent() throws IOException {
    divider();
    System.out.println("  STUDENT REGISTRATION");
    divider();

    String id = input("Student ID (e.g. 2024-0010): ");
    if (id.isEmpty()) {
      System.out.println("Student ID cannot be blank.");
      pause();
      return;
    }
    if (studentDB.containsKey(id)) {
      System.out.println("That student ID is already registered.");
      pause();
      return;
    }

    String fn = input("First Name: ");
    String ln = input("Last Name: ");
    String section = input("Grade & Section (e.g. Grade 11 - STEM A): ");
    String contact = input("Contact Number: ");
    String pw = input("Password: ");
    String confirm = input("Confirm Password: ");

    if (!pw.equals(confirm)) {
      System.out.println("Passwords do not match. Registration cancelled.");
      pause();
      return;
    }

    studentDB.put(id, new Student(id, pw, fn, ln, section, contact));
    System.out.println("\nRegistration successful! You may now log in.");
    pause();
  }

  // =========================================================
  //  STUDENT LOGIN & MENU
  // =========================================================

  static void studentLogin() throws IOException {
    divider();
    System.out.println("  STUDENT LOGIN");
    divider();

    String id = input("Student ID: ");
    String pw = input("Password: ");

    Student s = studentDB.get(id);
    if (s == null || !s.password.equals(pw)) {
      System.out.println("Incorrect student ID or password.");
      pause();
      return;
    }

    System.out.println(
      "\nWelcome, " + s.fullName() + "! (" + s.gradeSection + ")"
    );
    showOverdueReminders(s);
    studentMenu(s);
  }

  static void showOverdueReminders(Student s) {
    boolean hasOverdue = false;
    for (String acc : s.activeBorrows) {
      BorrowSlot slot = activeLoans.get(acc);
      if (slot == null) continue;
      long over = daysOverdue(slot.dueDate);
      if (over > 0) {
        if (!hasOverdue) {
          System.out.println();
          System.out.println("  *** OVERDUE REMINDER ***");
          thin();
          hasOverdue = true;
        }
        Book b = bookDB.get(acc);
        String title = (b != null) ? b.title : acc;
        System.out.printf(
          "  \"%s\" was due %s (%d day(s) overdue) - Fine: PHP %.2f%n",
          title,
          slot.dueDate,
          over,
          over * LATE_FEE_PER_DAY
        );
      }
    }
    if (hasOverdue) {
      System.out.println(
        "  Please return overdue books immediately to the library desk."
      );
      thin();
    }
  }

  static void studentMenu(Student s) throws IOException {
    while (true) {
      divider();
      System.out.println("  STUDENT MENU - " + s.fullName());
      thin();
      System.out.println("1. Browse Books");
      System.out.println("2. Search Books");
      System.out.println("3. Borrow a Book");
      System.out.println("4. Return a Book");
      System.out.println("5. My Borrowed Books");
      System.out.println("6. My Borrow History");
      System.out.println("7. My Pending Fees");
      System.out.println("8. Pay Fees");
      System.out.println("0. Logout");
      thin();

      String choice = input("Select: ");
      switch (choice) {
        case "1" -> browseBooks();
        case "2" -> searchBooks();
        case "3" -> borrowBook(s);
        case "4" -> returnBook(s);
        case "5" -> myCurrentBooks(s);
        case "6" -> myHistory(s);
        case "7" -> viewMyFees(s);
        case "8" -> payFees(s);
        case "0" -> {
          return;
        }
        default -> System.out.println("Invalid option.");
      }
    }
  }

  // =========================================================
  //  BROWSE & SEARCH
  // =========================================================

  static void browseBooks() throws IOException {
    divider();
    System.out.println("  BROWSE BOOKS");
    thin();
    System.out.println(
      "Sort by:  1. Title   2. Author   3. Genre   4. Accession No."
    );
    String sort = input("Choose sort (default=1): ");

    List<Book> list = new ArrayList<>(bookDB.values());
    list.sort(sortComparator(sort));
    printBookTable(list);
    pause();
  }

  static void searchBooks() throws IOException {
    divider();
    System.out.println("  SEARCH BOOKS");
    thin();
    System.out.println("Search by:  1. Title   2. Author   3. Genre");
    String by = input("Choose (default=1): ");
    String query = input("Enter keyword: ").toLowerCase();

    List<Book> results = new ArrayList<>();
    for (Book b : bookDB.values()) {
      boolean match = switch (by) {
        case "2" -> b.author.toLowerCase().contains(query);
        case "3" -> b.genre.toLowerCase().contains(query);
        default -> b.title.toLowerCase().contains(query);
      };
      if (match) results.add(b);
    }

    divider();
    if (results.isEmpty()) {
      System.out.println("No books found matching \"" + query + "\".");
    } else {
      System.out.println("Found " + results.size() + " result(s):");
      printBookTable(results);
    }
    pause();
  }

  static Comparator<Book> sortComparator(String key) {
    return switch (key) {
      case "2" -> Comparator.comparing(b -> b.author.toLowerCase());
      case "3" -> Comparator.comparing(b -> b.genre.toLowerCase());
      case "4" -> Comparator.comparing(b -> b.accessionNo);
      case "5" -> Comparator.comparing(b -> b.status.name());
      default -> Comparator.comparing(b -> b.title.toLowerCase());
    };
  }

  static void printBookTable(List<Book> list) {
    thin();
    System.out.printf(
      "  %-10s %-34s %-22s %-18s %-6s %-10s%n",
      "Accession",
      "Title",
      "Author",
      "Genre",
      "Year",
      "Status"
    );
    thin();
    for (Book b : list) {
      System.out.printf(
        "  %-10s %-34s %-22s %-18s %-6d %-10s%n",
        b.accessionNo,
        truncate(b.title, 33),
        truncate(b.author, 21),
        truncate(b.genre, 17),
        b.year,
        b.status
      );
    }
    thin();
  }

  // =========================================================
  //  BORROW
  // =========================================================

  static void borrowBook(Student s) throws IOException {
    divider();
    System.out.println("  BORROW A BOOK");
    thin();

    if (s.pendingFee > 0) {
      System.out.printf("You have a pending fee of PHP %.2f.%n", s.pendingFee);
      System.out.println(
        "Please settle it at the library desk before borrowing again."
      );
      pause();
      return;
    }

    if (s.activeBorrows.size() >= 3) {
      System.out.println(
        "Maximum of 3 books allowed at a time. Return a book first."
      );
      pause();
      return;
    }

    List<Book> available = new ArrayList<>();
    for (Book b : bookDB.values())
      if (b.status == BookStatus.AVAILABLE) available.add(b);

    available.sort(sortComparator("1"));

    if (available.isEmpty()) {
      System.out.println("No books available for borrowing right now.");
      pause();
      return;
    }

    printBookTable(available);
    String acc = input(
      "Enter Accession No. to borrow (or 0 to cancel): "
    ).toUpperCase();
    if (acc.equals("0")) return;

    Book book = bookDB.get(acc);
    if (book == null || book.status != BookStatus.AVAILABLE) {
      System.out.println("That book is not available.");
      pause();
      return;
    }
    if (s.activeBorrows.contains(acc)) {
      System.out.println("You already have that book borrowed.");
      pause();
      return;
    }

    String now = nowDateTime();
    String dueDate = dueDateFrom(todayDate());
    String txId = nextTxId();

    book.status = BookStatus.BORROWED;
    s.activeBorrows.add(acc);
    activeLoans.put(acc, new BorrowSlot(s.studentId, acc, now, dueDate));

    Transaction tx = new Transaction(
      txId,
      s.studentId,
      acc,
      TransactionType.BORROW,
      now
    );
    tx.dueDate = dueDate;
    transactions.add(tx);

    System.out.println();
    System.out.println("  Borrow successful!");
    thin();
    System.out.println("  Transaction ID  : " + txId);
    System.out.println("  Book            : " + book.title);
    System.out.println("  Borrowed on     : " + now);
    System.out.println("  Due Date        : " + dueDate);
    System.out.printf(
      "  Late fee rule   : PHP %.2f per day after due date%n",
      LATE_FEE_PER_DAY
    );
    pause();
  }

  // =========================================================
  //  RETURN
  // =========================================================

  static void returnBook(Student s) throws IOException {
    divider();
    System.out.println("  RETURN A BOOK");
    thin();

    if (s.activeBorrows.isEmpty()) {
      System.out.println("You have no books to return.");
      pause();
      return;
    }

    System.out.printf(
      "  %-10s %-34s %-12s %-12s%n",
      "Accession",
      "Title",
      "Due Date",
      "Overdue"
    );
    thin();
    for (String acc : s.activeBorrows) {
      Book b = bookDB.get(acc);
      BorrowSlot slot = activeLoans.get(acc);
      long over = (slot != null) ? daysOverdue(slot.dueDate) : 0;
      System.out.printf(
        "  %-10s %-34s %-12s %-12s%n",
        acc,
        (b != null) ? truncate(b.title, 33) : acc,
        (slot != null) ? slot.dueDate : "-",
        over > 0 ? over + " day(s)" : "On time"
      );
    }
    thin();

    String acc = input(
      "Enter Accession No. to return (or 0 to cancel): "
    ).toUpperCase();
    if (acc.equals("0")) return;

    if (!s.activeBorrows.contains(acc)) {
      System.out.println("That accession number is not in your borrowed list.");
      pause();
      return;
    }

    System.out.println("\nBook condition on return:");
    System.out.println("1. Good");
    System.out.println("2. Damaged");
    System.out.println("3. Lost");
    String cond = input("Select: ");

    Book book = bookDB.get(acc);
    BorrowSlot slot = activeLoans.get(acc);
    String now = nowDateTime();
    String txId = nextTxId();
    long overdueDays = (slot != null) ? daysOverdue(slot.dueDate) : 0;
    double lateFine = overdueDays * LATE_FEE_PER_DAY;
    double penalty;
    TransactionType type;
    String notes;

    switch (cond) {
      case "3" -> {
        type = TransactionType.LOST;
        penalty = lateFine + LOST_BOOK_PENALTY;
        notes = "Book declared lost.";
        if (book != null) book.status = BookStatus.LOST;
      }
      case "2" -> {
        type = TransactionType.DAMAGED;
        penalty = lateFine + DAMAGED_BOOK_FEE;
        notes = "Book returned damaged.";
        if (book != null) {
          book.status = BookStatus.DAMAGED;
          book.condition = "Poor";
        }
      }
      default -> {
        type = TransactionType.RETURN;
        penalty = lateFine;
        notes = "";
        if (book != null) book.status = BookStatus.AVAILABLE;
      }
    }

    s.activeBorrows.remove(acc);
    activeLoans.remove(acc);
    s.pendingFee += penalty;

    Transaction tx = new Transaction(txId, s.studentId, acc, type, now);
    tx.penaltyApplied = penalty;
    tx.notes = notes;
    if (slot != null) tx.dueDate = slot.dueDate;
    transactions.add(tx);

    System.out.println();
    System.out.println("  --- RETURN RECEIPT ---");
    thin();
    System.out.println("  Transaction ID  : " + txId);
    System.out.println(
      "  Book            : " + (book != null ? book.title : acc)
    );
    System.out.println("  Returned on     : " + now);
    System.out.println(
      "  Due date was    : " + (slot != null ? slot.dueDate : "-")
    );
    System.out.println("  Days overdue    : " + overdueDays);
    System.out.printf("  Late fine       : PHP %.2f%n", lateFine);
    if (cond.equals("3")) System.out.printf(
      "  Lost penalty    : PHP %.2f%n",
      LOST_BOOK_PENALTY
    );
    if (cond.equals("2")) System.out.printf(
      "  Damaged fee     : PHP %.2f%n",
      DAMAGED_BOOK_FEE
    );
    System.out.printf("  TOTAL PENALTY   : PHP %.2f%n", penalty);

    if (penalty > 0) {
      System.out.printf(
        "  Total pending   : PHP %.2f - settle at the library desk.%n",
        s.pendingFee
      );
    } else {
      System.out.println("  No penalties. Thank you for returning on time!");
    }
    pause();
  }

  // =========================================================
  //  STUDENT: MY BOOKS / HISTORY / FEES
  // =========================================================

  static void myCurrentBooks(Student s) throws IOException {
    divider();
    System.out.println("  MY CURRENTLY BORROWED BOOKS");
    thin();

    if (s.activeBorrows.isEmpty()) {
      System.out.println("You have no books currently borrowed.");
      pause();
      return;
    }

    System.out.printf(
      "  %-10s %-34s %-12s %-12s %-10s%n",
      "Accession",
      "Title",
      "Borrowed On",
      "Due Date",
      "Overdue"
    );
    thin();
    for (String acc : s.activeBorrows) {
      Book b = bookDB.get(acc);
      BorrowSlot slot = activeLoans.get(acc);
      long over = (slot != null) ? daysOverdue(slot.dueDate) : 0;
      String on = (slot != null) ? slot.borrowDateTime.substring(0, 10) : "-";
      System.out.printf(
        "  %-10s %-34s %-12s %-12s %-10s%n",
        acc,
        (b != null) ? truncate(b.title, 33) : acc,
        on,
        (slot != null) ? slot.dueDate : "-",
        over > 0 ? over + " day(s)" : "No"
      );
    }
    pause();
  }

  static void myHistory(Student s) throws IOException {
    divider();
    System.out.println("  MY BORROW HISTORY");
    thin();

    List<Transaction> mine = new ArrayList<>();
    for (Transaction t : transactions)
      if (t.studentId.equals(s.studentId)) mine.add(t);

    if (mine.isEmpty()) {
      System.out.println("No transaction history on record.");
      pause();
      return;
    }

    System.out.printf(
      "  %-10s %-34s %-10s %-21s %-10s%n",
      "TX ID",
      "Book Title",
      "Type",
      "Date & Time",
      "Penalty"
    );
    thin();
    for (Transaction t : mine) {
      Book b = bookDB.get(t.accessionNo);
      String title = (b != null) ? truncate(b.title, 33) : t.accessionNo;
      System.out.printf(
        "  %-10s %-34s %-10s %-21s PHP %-8.2f%n",
        t.id,
        title,
        t.type,
        t.dateTime,
        t.penaltyApplied
      );
    }
    pause();
  }

  static void viewMyFees(Student s) throws IOException {
    divider();
    System.out.println("  MY PENDING FEES");
    thin();

    double accruing = 0.0;
    for (String acc : s.activeBorrows) {
      BorrowSlot slot = activeLoans.get(acc);
      if (slot != null) accruing +=
        daysOverdue(slot.dueDate) * LATE_FEE_PER_DAY;
    }

    System.out.printf("  Settled pending fees      : PHP %.2f%n", s.pendingFee);
    System.out.printf("  Accruing overdue fines    : PHP %.2f%n", accruing);
    System.out.printf(
      "  ESTIMATED TOTAL           : PHP %.2f%n",
      s.pendingFee + accruing
    );
    if (s.pendingFee == 0 && accruing == 0) System.out.println(
      "\n  All clear! No fees."
    );
    pause();
  }

  static void payFees(Student s) throws IOException {
    divider();
    System.out.println("  PAY FEES");
    thin();

    if (s.pendingFee == 0) {
      System.out.println("You have no pending fees to pay.");
      pause();
      return;
    }

    System.out.printf("Total amount due: PHP %.2f%n", s.pendingFee);
    String confirm = input("Confirm full payment? (yes/no): ");
    if (confirm.equalsIgnoreCase("yes")) {
      s.pendingFee = 0.0;
      System.out.println("Payment recorded. Your account is now clear.");
      System.out.println("Please present your student ID at the library desk.");
    } else {
      System.out.println("Payment cancelled.");
    }
    pause();
  }

  // =========================================================
  //  ADMIN LOGIN & MENU
  // =========================================================

  static void adminLogin() throws IOException {
    divider();
    System.out.println("  ADMIN LOGIN");
    divider();

    String un = input("Username: ");
    String pw = input("Password: ");

    if (!un.equals(ADMIN_USERNAME) || !pw.equals(ADMIN_PASSWORD)) {
      System.out.println("Incorrect credentials.");
      pause();
      return;
    }

    System.out.println("Access granted. Welcome, Administrator.");
    adminMenu();
  }

  static void adminMenu() throws IOException {
    while (true) {
      divider();
      System.out.println("  ADMIN MENU");
      thin();
      System.out.println("  CATALOG MANAGEMENT");
      System.out.println("  1.  View All Books");
      System.out.println("  2.  Search Books");
      System.out.println("  3.  Add Book");
      System.out.println("  4.  Edit Book");
      System.out.println("  5.  Mark Book as Lost / Damaged");
      System.out.println("  6.  Remove Book from Catalog");
      thin();
      System.out.println("  BORROWER MANAGEMENT");
      System.out.println("  7.  View All Students");
      System.out.println("  8.  View Student Borrow History");
      System.out.println("  9.  View All Active Loans");
      System.out.println("  10. Waive Student Fee");
      thin();
      System.out.println("  0.  Logout");
      thin();

      String choice = input("Select: ");
      switch (choice) {
        case "1" -> adminViewBooks();
        case "2" -> searchBooks();
        case "3" -> adminAddBook();
        case "4" -> adminEditBook();
        case "5" -> adminMarkBookStatus();
        case "6" -> adminRemoveBook();
        case "7" -> adminViewStudents();
        case "8" -> adminStudentHistory();
        case "9" -> adminActiveLoans();
        case "10" -> adminWaiveFee();
        case "0" -> {
          return;
        }
        default -> System.out.println("Invalid option.");
      }
    }
  }

  // =========================================================
  //  ADMIN: CATALOG
  // =========================================================

  static void adminViewBooks() throws IOException {
    divider();
    System.out.println("  ALL BOOKS");
    thin();
    System.out.println(
      "Sort by:  1. Title   2. Author   3. Genre   4. Accession No.   5. Status"
    );
    String sort = input("Choose sort (default=1): ");

    List<Book> list = new ArrayList<>(bookDB.values());
    list.sort(sortComparator(sort));
    printBookTable(list);
    pause();
  }

  static void adminAddBook() throws IOException {
    divider();
    System.out.println("  ADD BOOK");
    divider();

    String acc = input("Accession No. (e.g. ICN-013): ").toUpperCase();
    if (acc.isEmpty()) {
      System.out.println("Accession No. cannot be blank.");
      pause();
      return;
    }
    if (bookDB.containsKey(acc)) {
      System.out.println("That accession number already exists.");
      pause();
      return;
    }

    String title = input("Title: ");
    String author = input("Author: ");
    String genre = input("Genre: ");
    int year = 0;
    try {
      year = Integer.parseInt(input("Publication Year: "));
    } catch (NumberFormatException e) {
      System.out.println("Invalid year, saved as 0.");
    }

    bookDB.put(acc, new Book(acc, title, author, genre, year));
    System.out.println("Book added: " + title);
    pause();
  }

  static void adminEditBook() throws IOException {
    divider();
    System.out.println("  EDIT BOOK");
    divider();

    String acc = input("Enter Accession No. to edit: ").toUpperCase();
    Book b = bookDB.get(acc);
    if (b == null) {
      System.out.println("Book not found.");
      pause();
      return;
    }

    System.out.println("Leave blank to keep current value.");
    thin();

    String title = input("Title [" + b.title + "]: ");
    if (!title.isEmpty()) b.title = title;

    String author = input("Author [" + b.author + "]: ");
    if (!author.isEmpty()) b.author = author;

    String genre = input("Genre [" + b.genre + "]: ");
    if (!genre.isEmpty()) b.genre = genre;

    String yearStr = input("Year [" + b.year + "]: ");
    if (!yearStr.isEmpty()) {
      try {
        b.year = Integer.parseInt(yearStr);
      } catch (NumberFormatException e) {
        System.out.println("Invalid year, not changed.");
      }
    }

    System.out.println("Book updated: " + b.title);
    pause();
  }

  static void adminMarkBookStatus() throws IOException {
    divider();
    System.out.println("  MARK BOOK STATUS");
    divider();

    String acc = input("Enter Accession No.: ").toUpperCase();
    Book b = bookDB.get(acc);
    if (b == null) {
      System.out.println("Book not found.");
      pause();
      return;
    }

    System.out.println("Current status: " + b.status);
    System.out.println("1. Mark as Lost");
    System.out.println("2. Mark as Damaged");
    System.out.println("3. Restore to Available");
    String choice = input("Select: ");

    switch (choice) {
      case "1" -> {
        b.status = BookStatus.LOST;
        System.out.println("Marked as Lost.");
      }
      case "2" -> {
        b.status = BookStatus.DAMAGED;
        b.condition = "Poor";
        System.out.println("Marked as Damaged.");
      }
      case "3" -> {
        b.status = BookStatus.AVAILABLE;
        b.condition = "Good";
        System.out.println("Restored to Available.");
      }
      default -> System.out.println("Invalid choice.");
    }
    pause();
  }

  static void adminRemoveBook() throws IOException {
    divider();
    System.out.println("  REMOVE BOOK");
    divider();

    String acc = input("Enter Accession No. to remove: ").toUpperCase();
    Book b = bookDB.get(acc);
    if (b == null) {
      System.out.println("Book not found.");
      pause();
      return;
    }

    if (b.status == BookStatus.BORROWED) {
      System.out.println("Cannot remove a book that is currently borrowed.");
      pause();
      return;
    }

    String confirm = input(
      "Remove \"" + b.title + "\"? This cannot be undone. (yes/no): "
    );
    if (confirm.equalsIgnoreCase("yes")) {
      bookDB.remove(acc);
      System.out.println("Book removed from catalog.");
    } else {
      System.out.println("Cancelled.");
    }
    pause();
  }

  // =========================================================
  //  ADMIN: BORROWER MANAGEMENT
  // =========================================================

  static void adminViewStudents() throws IOException {
    divider();
    System.out.println("  ALL STUDENTS");
    thin();
    System.out.printf(
      "  %-12s %-24s %-22s %-8s %-12s%n",
      "Student ID",
      "Name",
      "Grade & Section",
      "Books",
      "Pending Fee"
    );
    thin();
    for (Student s : studentDB.values()) {
      System.out.printf(
        "  %-12s %-24s %-22s %-8d PHP %.2f%n",
        s.studentId,
        truncate(s.fullName(), 23),
        truncate(s.gradeSection, 21),
        s.activeBorrows.size(),
        s.pendingFee
      );
    }
    pause();
  }

  static void adminStudentHistory() throws IOException {
    divider();
    System.out.println("  STUDENT BORROW HISTORY");
    divider();

    String id = input("Enter Student ID: ");
    Student s = studentDB.get(id);
    if (s == null) {
      System.out.println("Student not found.");
      pause();
      return;
    }

    System.out.println("Student : " + s.fullName() + " | " + s.gradeSection);
    System.out.println("Contact : " + s.contactNo);
    thin();

    List<Transaction> mine = new ArrayList<>();
    for (Transaction t : transactions) if (t.studentId.equals(id)) mine.add(t);

    if (mine.isEmpty()) {
      System.out.println("No transaction history on record.");
      pause();
      return;
    }

    System.out.printf(
      "  %-10s %-34s %-10s %-21s %-12s %-10s%n",
      "TX ID",
      "Book Title",
      "Type",
      "Date & Time",
      "Due Date",
      "Penalty"
    );
    thin();
    for (Transaction t : mine) {
      Book b = bookDB.get(t.accessionNo);
      String title = (b != null) ? truncate(b.title, 33) : t.accessionNo;
      System.out.printf(
        "  %-10s %-34s %-10s %-21s %-12s PHP %-8.2f%n",
        t.id,
        title,
        t.type,
        t.dateTime,
        (t.dueDate != null ? t.dueDate : "-"),
        t.penaltyApplied
      );
    }
    pause();
  }

  static void adminActiveLoans() throws IOException {
    divider();
    System.out.println("  ALL ACTIVE LOANS");
    thin();

    if (activeLoans.isEmpty()) {
      System.out.println("No books are currently borrowed.");
      pause();
      return;
    }

    System.out.printf(
      "  %-10s %-34s %-12s %-20s %-12s %-10s%n",
      "Accession",
      "Title",
      "Student ID",
      "Borrower",
      "Due Date",
      "Overdue"
    );
    thin();
    for (BorrowSlot slot : activeLoans.values()) {
      Book b = bookDB.get(slot.accessionNo);
      Student s = studentDB.get(slot.studentId);
      long over = daysOverdue(slot.dueDate);
      System.out.printf(
        "  %-10s %-34s %-12s %-20s %-12s %-10s%n",
        slot.accessionNo,
        (b != null) ? truncate(b.title, 33) : slot.accessionNo,
        slot.studentId,
        (s != null) ? truncate(s.fullName(), 19) : slot.studentId,
        slot.dueDate,
        over > 0 ? over + " day(s)" : "No"
      );
    }
    pause();
  }

  static void adminWaiveFee() throws IOException {
    divider();
    System.out.println("  WAIVE STUDENT FEE");
    divider();

    String id = input("Enter Student ID: ");
    Student s = studentDB.get(id);
    if (s == null) {
      System.out.println("Student not found.");
      pause();
      return;
    }

    if (s.pendingFee == 0) {
      System.out.println(s.fullName() + " has no pending fees.");
      pause();
      return;
    }

    System.out.printf(
      "%s has a pending fee of PHP %.2f.%n",
      s.fullName(),
      s.pendingFee
    );
    String confirm = input("Waive this fee? (yes/no): ");
    if (confirm.equalsIgnoreCase("yes")) {
      s.pendingFee = 0.0;
      System.out.println("Fee waived for " + s.fullName() + ".");
    } else {
      System.out.println("Cancelled.");
    }
    pause();
  }
}
