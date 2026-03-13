import java.io.*;

public class system {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int choice;

        display_text("Hi!");
        choice = Integer.parseInt(br.readLine());

        if(choice == 0) {
            display_text("OK!");
        }
    }

    public static void display_text(String text) {
        System.out.print(text);
    }
}
