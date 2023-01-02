import java.io.*;
import java.util.*;

/**
 * The driver code that executes the whole application
 */
public class httpc {

    public static void main(String[] args) throws IOException {

        HTTPLibrary httpLibrary = new HTTPLibrary();

        System.out.println("-------------------cURL started ------------------------------");

        String input;
        do {
            System.out.println("Enter Command or Enter 'exit' to exit from the system");
            Scanner scan = new Scanner(System.in);
            while (scan.hasNextLine()){
                input = scan.nextLine();
            if (input.equals("exit")) {
                return;
            } else {
                httpLibrary.runCommand(input);
            }
            System.out.println("------------------------------------------------------------------------");
        }
        } while (true);
    }

}
