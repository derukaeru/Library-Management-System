import java.io.*;

public class LibraryManagement {

  public static void main(String[] args) throws IOException {
    display("testing\n");
    login(1);
  }

  public static void display(String text) {
    System.out.print(text);
  }

  public static void option() {}

  public static void login(int user) throws IOException {
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(System.in)
    );

    String[] first_name;
    String[] last_name;

    if (user == 1) {
      display("Enter your First Name: ");
      first_name = reader.readLine().split(" ");
      display("Enter your Last Name: ");
      last_name = reader.readLine().split(" ");
    } else if (user == 2) {
      // customer
    }
  }
}
