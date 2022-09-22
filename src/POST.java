import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class POST {

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

            url = input.substring(input.indexOf("http://"), input.length());
            if (url.contains(" ")) {
                url = url.split(" ")[0];
            }
            if (url.contains("'"))
                url = url.substring(0, url.length() - 1);
        } catch (Exception e) {
            System.out.println("Please enter a valid URL");
            return;
        }

        URI uri = null;
        String server = null;
        try {
            uri = new URI(url);
            server = uri.getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        if (data.contains("-f") && (data.contains("-d") || data.contains("--d"))) {
            System.out.println("Arguments invalid please enter valid arguments");
            return;
        }
        if (data.contains("-v"))
            isVerbose = true;

        if (data.contains("-o"))
            writeToFile = true;
        String contentData = "";
        int contentLength = 0;

        StringBuilder readData = new StringBuilder("");

        if (data.contains("--d") || data.contains("-d")) {
            contentData = input.substring(input.indexOf("{", input.indexOf("-d")), input.indexOf("}") + 1);
            contentLength = contentData.length();
        } else {
            String inputLines ="";
            try
            {
                String currentDir = System.getProperty("user.dir");
                String fileToRead = input.substring(input.indexOf("-f") + 3, input.indexOf("http://")-1);

                String filePath = currentDir + "\\" + fileToRead;

                BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));

                while((inputLines = bufferedReader.readLine()) != null)
                {
                    readData.append(inputLines + "\n");
                }
                bufferedReader.close();
            }
            catch(IOException e)
            {
                System.out.println("Error reading file named ");
            }
            contentData = readData.toString();
            contentLength = readData.toString().length();
            //System.out.println(contentData);

        }


        try {
            String headerInfoKeyValue = "";
            StringBuilder request = new StringBuilder();
            List<String> headerInfoList = new ArrayList<>();

            List<String> inputData = Arrays.asList(argumentTokens);


            Socket socket = new Socket(server, 80);
            String r = "POST " + url + " HTTP/1.0 " + Constants.NEWLINE + "Host: " + server + Constants.NEWLINE;
            request.append(r);

            request.append("Content-Length: " + contentLength);
            request.append(Constants.NEWLINE);


            if (!headerInfoList.isEmpty()) {
                for (String headerInfo : headerInfoList) {
                    headerInfoKeyValue = headerInfo.split(":")[0] + ":" + headerInfo.split(":")[1] + Constants.NEWLINE;
                    request.append(headerInfoKeyValue);
                }
            }

            PrintStream out = new PrintStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            request.append(Constants.NEWLINE);
            request.append(contentData);


            out.print(request);

            String line = in.readLine();

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

}
