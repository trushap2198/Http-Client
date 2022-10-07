import java.io.IOException;

public class HTTPLibrary {


    public void runCommand(String input) throws IOException {

        String[] argumentTokens = input.split(" ");
        String command = argumentTokens[0];
        if (!command.equals("httpc") || argumentTokens.length == 1) {
            System.out.println("Invalid command,please try again");
            return;
        }
        switch (argumentTokens[1]) {
            case "help":
                new HELP().run(argumentTokens);
                break;
            case "get":
                new GET().run(input);
                break;
            case "post":
                new POST().run(input);
                break;
            default:
                System.out.println("Invalid arguments,please try again");
        }

    }

}
