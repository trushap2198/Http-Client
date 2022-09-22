import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GET {
    private static final String USER_AGENT = "Concordia-HTTP/1.0";

    public static void run(String[] argumentTokens, String input) {
        boolean isVerbose = false;
        List<String> data = Arrays.asList(input.split(" "));
        boolean writeToFile = false;
        String url = null;
        String fileName = "";
        try {

            int URLlength = 0;
            if (data.contains("-o")) {
                URLlength = input.indexOf("-o") - 2;

                fileName = input.substring(input.indexOf("-o") + 2);

            } else
                URLlength = input.length() - 1;


            url = input.substring(input.indexOf("http://"), URLlength); // will output the url http://httpbin.org/get?course=networking&assignment=1

        } catch (Exception e) {
            System.out.println("Please enter a valid URL");
            return;
        }

        URI uri = null;
        String server = null;
        try {
            uri = new URI(url); //Constructs a URI object by parsing the specified string url
            server = uri.getHost(); //Constructs a URI object by parsing the specified string , will return httpbin.org

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        if (data.contains("-d") || data.contains("-f")) {
            System.out.println("Arguments invalid please enter valid arguments");
            return;
        }

        if (data.contains("-v"))
            isVerbose = true;
        if (data.contains("-o"))
            writeToFile = true;
        String headerInfoKeyValue = "";
        List<String> headerInfoList = new ArrayList<>();

        StringBuilder request = new StringBuilder();

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

        try {

            Socket socket = new Socket(server, 80);

            PrintStream out = new PrintStream(socket.getOutputStream()); //for sending the data to the stream , we can easily write text with methods like println().
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //for reading from the socket stream in order to easily read text with methods like readLine()

            String r = "GET " + url + " HTTP/1.0 ";
            request.append(r);
            request.append(Constants.NEWLINE);
            request.append(headerInfoKeyValue);

            out.println(request);

            out.println();
            String line = in.readLine();
            String status = line;
            StringBuilder output = new StringBuilder();
            if (isVerbose) {
                while (line != null) {
                    line = in.readLine();

                    if (writeToFile && line != null)
                        output.append(line + Constants.NEWLINE);
                    else if (line != null)
                        System.out.println(line);

                }
            } else {

                while (line != null) {
                    if (line.startsWith("{") && line != null) {

                        if (writeToFile && line != null)
                            output.append(line + Constants.NEWLINE);
                        else if (line != null)
                            System.out.println(line);
                        while (!line.startsWith("}") && line != null) {
                            line = in.readLine();

                            if (writeToFile && line != null)
                                output.append(line + Constants.NEWLINE);
                            else if (line != null)
                                System.out.println(line);
                        }
                    }
                    line = in.readLine();
                }
            }

            if (writeToFile) {
                try {

                    String currentDir = System.getProperty("user.dir");

                    String filePath = currentDir + "\\" + fileName;

                    FileWriter fileWriter = new FileWriter(filePath, false);
                    fileWriter.write(output.toString());
                    fileWriter.close();
                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
                System.out.println("Data written to File Successfully");
            }
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printVerboseAndRedirect(String[] argumentTokens, BufferedReader in,String line,String input,String url) throws IOException {

        String location = null;

        while (line != null) {
            if (line != null) {
                System.out.println(line);
                if(line.contains("Location")) {
                    location = line.substring(line.indexOf(" ") + 1);
                    System.out.println(location);
                }
            }

            line = in.readLine();
        }

        System.out.println("------Redirecting-------");
        String redirectInput =  input.replace(url,location);
        run(argumentTokens,redirectInput);

    }

    private void printDocument(boolean isVerbose, String line, boolean writeToFile, String fileName, BufferedReader in) throws IOException {

        StringBuilder output = new StringBuilder();
        if (isVerbose) {
            while (line != null) {

                if (writeToFile && line != null)
                    output.append(line + Constants.NEWLINE);
                else if (line != null)
                    System.out.println(line);

                line = in.readLine();
            }
        } else {

            while (line != null) {
                if (line.startsWith("{") && line != null) {

                    if (writeToFile && line != null)
                        output.append(line + Constants.NEWLINE);
                    else if (line != null)
                        System.out.println(line);
                    while (!line.startsWith("}") && line != null) {
                        line = in.readLine();

                        if (writeToFile && line != null)
                            output.append(line + Constants.NEWLINE);
                        else if (line != null)
                            System.out.println(line);
                    }
                }
                line = in.readLine();
            }
        }

        if (writeToFile) {
            try {

                String currentDir = System.getProperty("user.dir");

                String filePath = currentDir + "\\" + fileName;

                FileWriter fileWriter = new FileWriter(filePath, false);
                fileWriter.write(output.toString());
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            System.out.println("Data written to File Successfully");
        }

    }


}
