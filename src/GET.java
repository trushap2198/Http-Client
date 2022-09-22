import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GET {
    private static final String USER_AGENT = "Concordia-HTTP/1.0";
    private static boolean isVerbose = false;
    private static boolean writeToFile = false;
    private static String url;
    private static String fileName;
    private static String server;
    private static URI uri;
    private static List<String> data;
    private static String headerInfoKeyValue;
    private static Socket socket;

    public static void run(String input) throws IOException
    {
          data = Arrays.asList(input.split(" "));
          if (data.contains("-d") || data.contains("-f")) { //get cannot contain -d or -f
            System.out.println("Arguments invalid please enter valid arguments");
            return;
          }
          parseInput(data);
             // for reading from the socket stream in order to easily
            // read text with methods like readLine()
          getResponse(url);

    }

    public static void getResponse(String url) throws IOException
    {
          socket = new Socket(server, 80);
          PrintStream out = new PrintStream(socket.getOutputStream()); // for sending the data to the stream , we can easily write
          // text with methods like println().
          BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          StringBuilder request = new StringBuilder();
          String r = "GET " + url + " HTTP/1.0 ";
          request.append(r);
          request.append(Constants.NEWLINE);
          request.append(headerInfoKeyValue);
          out.println(request);
          out.println();
          String status = in.readLine();
          String line = "";
          if(status.split(" ")[1].startsWith("3")){
            printRedirect(in);
          }
          StringBuilder output = new StringBuilder();
          if (isVerbose)
          {
              while ((line = in.readLine()) != null)
              {
                  if (writeToFile)
                  {
                      output.append(line + Constants.NEWLINE);
                  }
                else
                  {
                    System.out.println(line);
                  }
                if (line.equals("}"))
                {
                  break;
                }
             }
         }
        else
        {
            boolean isJson = false;
            while ((line = in.readLine()) != null) {
              if (line.trim().equals("{")) {
                isJson = true;
              }
              if (isJson)
              {
                  if (writeToFile)
                  {
                    output.append(line + Constants.NEWLINE);
                  }
                  else
                  {
                    System.out.println(line);
                  }
                  if (line.equals("}"))
                  {
                    break;
                  }
              }
          }
      }
      if (writeToFile)
      {
        writeOutputToFile(output);
      }
      in.close();
      out.close();
      socket.close();
    }

    private static void writeOutputToFile(StringBuilder output)
    {
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

    public static void parseInput(List<String> data)
    {
        try
        {
          if (data.contains("-o"))
          {
            fileName = data.get(data.size() - 1);
            System.out.println("file name:" + fileName);
            url = data.get(data.indexOf("-o") - 1).replaceAll("\\'", "");

          } else
          {
            url = data.get(data.size() - 1).replaceAll("\\'", "");
          }
          System.out.println("url:" + url);
        }
        catch (Exception e)
        {
          System.out.println("Please enter a valid URL");
          return;
        }
        try
        {
          uri = new URI(url); // Constructs a URI object by parsing the specified string url
          server = uri.getHost(); // Constructs a URI object by parsing the specified string , will return
          // httpbin.org
        }
        catch (URISyntaxException e)
        {
          e.printStackTrace();
        }
        if (data.contains("-v")) isVerbose = true;
        if (data.contains("-o")) writeToFile = true;
        List<String> headerInfoList = new ArrayList<>();
        if (data.contains("-h"))
        {
          for (int i = 0; i < data.size(); i++)
          {
            if (data.get(i).equals("-h"))
            {
              headerInfoList.add(data.get(i + 1));
            }
          }
          if (!headerInfoList.isEmpty())
          {
            for (String headerInfo : headerInfoList)
            {
              headerInfoKeyValue += headerInfo.split(":")[0] + ":" + headerInfo.split(":")[1] + Constants.NEWLINE;
            }
          }
        }
        headerInfoKeyValue += "User-Agent:" + USER_AGENT;
      }

    private static void printRedirect(BufferedReader in) throws IOException {
      String location = null;
      String line = in.readLine();
      while (line != null)
      {
        if (line != null)
        {
          System.out.println(line);
          if (line.contains("Location"))
          {
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
