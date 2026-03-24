import java.io.*;

public class LibraryManagement {

  public static void main(String[] args) throws IOException {
    String admin_first_name = "Dale";
    String admin_last_name = "Despi";
    String admin_password = "test123";

    String user_first_name = "Dale1";
    String user_last_name = "Despi1";
    String user_password = "test123";

    display("testing\n");
    bool login_success = login(1);
  }

  public static void display(String text) {
    System.out.print(text);
  }

  public static void display_line(String text) {
    System.out.println(text);
  }

  public static void option() {}

  public static bool login(int user) throws IOException {
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(System.in)
    );

    String[] first_name;
    String[] last_name;
    String[] password;

    if (user == 1) {
      display("Enter your First Name: ");
      first_name = reader.readLine().split(" ");
      display("Enter your Last Name: ");
      last_name = reader.readLine().split(" ");
      display("Enter your Password: ");
      password = reader.readLine().split(" ");

      if (
        first_name.equals(admin_first_name) &&
        last_name.equals(admin_last_name) &&
        password.equals(admin_password)
      ) {
        display("Login successful!\n");

        display_line("Welcome " + admin_first_name + "!");
        display_line("1. Manage books");
        display_line("2. Manage users");
      } else {
        display("Login failed. Please try again.\n");
        login(1);
      }
    } else if (user == 2) {
      display("Enter your First Name: ");
      first_name = reader.readLine().split(" ");
      display("Enter your Last Name: ");
      last_name = reader.readLine().split(" ");
      display("Enter your Password: ");
      password = reader.readLine().split(" ");

      if (
        first_name.equals(user_first_name) &&
        last_name.equals(user_last_name) &&
        password.equals(user_password)
      ) {
        display("Login successful!\n");

        display_line("Welcome " + user_first_name + "!");
        display_line("1. Manage books");
        display_line("2. Manage fees");

        display("=> ");
      } else {
        display("Login failed. Please try again.\n");
        login(2);
      }
    }
  }

  public static void admin_choice(int choice) {
    switch (choice) {
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        break;
    }
  }

  public static void user_choice(int choice) {
    switch (choice) {
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        break;
    }
  }
}
