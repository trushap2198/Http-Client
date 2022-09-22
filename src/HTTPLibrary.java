
public class HTTPLibrary {


    public void runCommand(String input) {

        String[] argumentTokens = input.split(" ");
        String command = argumentTokens[0];
        if (!command.equals("httpc")) {
            System.out.println("Invalid command,please try again");
            return;
        }
        if (argumentTokens.length == 1) {
            System.out.println("Invalid command,please try again");
            return;
        }
        switch (argumentTokens[1]) {
            case "help":
                HELP.run(argumentTokens);
                break;
            case "get":
                GET.run(argumentTokens, input);
                break;
            case "post":
                POST.run(argumentTokens, input);
                break;
            default:
                System.out.println("Invalid arguments,please try again");
        }

    }

}
