import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GET {

    private static final String USER_AGENT = "Concordia-HTTP/1.0";

    private boolean isVerbose;
    private boolean writeToFile;
    private String url;
    private String fileName;
    private String server;
    private URI uri;
    private List<String> data;
    private String headerInfoKeyValue = "";
    private Socket socket;

    public GET() {
        isVerbose = false;
        writeToFile = false;
    }

    public void run(String input) throws IOException {
        data = Arrays.asList(input.split(" "));

        //get cannot contain -d or -f
        if (data.contains("-d") || data.contains("-f")) {
            System.out.println("Arguments invalid please enter valid arguments");
            return;
        }

        parseInput(data);

        getResponse(url);

    }

    public void getResponse(String url) throws IOException {
        socket = new Socket(server, 80);
        PrintStream out = new PrintStream(socket.getOutputStream()); // for sending the data to the stream , we can easily write
        // text with methods like println().
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        StringBuilder request = new StringBuilder();
        String r = "GET " + url + " HTTP/1.0 ";
        request.append(r);
        request.append(Constants.NEWLINE);
        request.append(headerInfoKeyValue);
        out.println(request);
        out.println();
        String line = in.readLine();
        String status = line;

        if (status.split(" ")[1].startsWith("3")) {
            printRedirect(in);
        }
        StringBuilder output = new StringBuilder();
        if (isVerbose) {

            while (line != null) {

                if (writeToFile && line != null)
                    output.append(line + Constants.NEWLINE);
                else if (line != null)
                    System.out.println(line);
                if (line.equals("}")) {
                    break;
                }
                line = in.readLine();
            }
        } else {
            boolean isJson = false;
            while ((line = in.readLine()) != null) {
                if (line.trim().equals("{")) {
                    isJson = true;
                }
                if (isJson) {
                    if (writeToFile) {
                        output.append(line + Constants.NEWLINE);
                    } else {
                        System.out.println(line);
                    }
                    if (line.equals("}")) {
                        break;
                    }
                }
//                else{
//                    if (writeToFile) {
//                        output.append(line + Constants.NEWLINE);
//                    } else {
//                        System.out.println(line);
//                    }
//                }
            }
        }
        if (writeToFile) {
            FileUtility.writeOutputToFile(output,fileName);
        }

        in.close();
        out.close();
        socket.close();
    }

    public void parseInput(List<String> data) {
        try {
            if (data.contains("-o")) {
                fileName = data.get(data.size() - 1);
                System.out.println("file name:" + fileName);
                url = data.get(data.indexOf("-o") - 1).replaceAll("\\'", "");

            } else {
                url = data.get(data.size() - 1).replaceAll("\\'", "");
            }
        } catch (Exception e) {
            System.out.println("Please enter a valid URL");
            return;
        }
        try {
            uri = new URI(url); // Constructs a URI object by parsing the specified string url
            server = uri.getHost(); // Constructs a URI object by parsing the specified string , will return
            // httpbin.org
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (data.contains("-v")) isVerbose = true;
        if (data.contains("-o")) writeToFile = true;
        List<String> headerInfoList = new ArrayList<>();
        if (data.contains("-h")) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).equals("-h")) {
                    headerInfoList.add(data.get(i + 1));
                }
            }
            if (!headerInfoList.isEmpty()) {
                for (String headerInfo : headerInfoList) {
                    headerInfoKeyValue += headerInfo.split(":")[0] + ":" + headerInfo.split(":")[1] + Constants.NEWLINE;
                }
            }
        }
        headerInfoKeyValue += "User-Agent:" + USER_AGENT;
    }

    private void printRedirect(BufferedReader in) throws IOException {
        String location = null;
        String line = in.readLine();
        while (line != null) {
            if (line != null) {
                System.out.println(line);
                if (line.contains("Location")) {
                    location = line.substring(line.indexOf(" ") + 1);
                    System.out.println("new location: " + location);
                }
            }
            line = in.readLine();
        }
        System.out.println("------Redirecting-------");
        socket.close();
        getResponse(location);
    }

}
