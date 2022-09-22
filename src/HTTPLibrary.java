import java.io.IOException;

public class HTTPLibrary {


    public void runCommand(String input) throws IOException {

        String[] argumentTokens = input.split(" ");
        String command = argumentTokens[0];
        if (!command.equals("httpc") || argumentTokens.length == 1 ) {
            System.out.println("Invalid command,please try again");
            return;
        }
        switch (argumentTokens[1]) {
            case "help":
                HELP.run(argumentTokens);
                break;
            case "get":
                GET.run(input);
                break;
            case "post":
                POST.run(argumentTokens, input);
                break;
            default:
                System.out.println("Invalid arguments,please try again");
        }

    }

}
