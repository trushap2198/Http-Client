import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class POST {
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
  private String contentData = "";
  private int contentLength;

  public POST() {
    isVerbose = false;
    writeToFile = false;
  }

  public void run( String input) throws IOException {
    data = Arrays.asList(input.split(" "));
    if (data.contains("-f") && (data.contains("-d") || data.contains("--d"))) {
      System.out.println("Arguments invalid please enter valid arguments");
      return;
    }
    parseInputPost(data);
    getResponsePOST(url);
  }

  private void getResponsePOST(String url) throws IOException {
    StringBuilder request = new StringBuilder();
    socket = new Socket(server, 80);
    PrintStream out = new PrintStream(socket.getOutputStream());
    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    String r =
        "POST " + url + " HTTP/1.0 " + Constants.NEWLINE + "Host: " + server + Constants.NEWLINE;
    request.append(r);
    request.append("Content-Length: " + contentLength);
    request.append(Constants.NEWLINE);
    if (data.contains("-h")) {
      List<String> headerInfoList = new ArrayList<>();
      for (int i = 0; i < data.size(); i++) {
        if (data.get(i).equals("-h")) {
          headerInfoList.add(data.get(i + 1));
        }
      }
      if (!headerInfoList.isEmpty()) {
        for (String headerInfo : headerInfoList) {
          headerInfoKeyValue =
              headerInfo.split(":")[0] + ":" + headerInfo.split(":")[1] + Constants.NEWLINE;
          request.append(headerInfoKeyValue);
        }
      }
    }
    request.append(Constants.NEWLINE);
    request.append(contentData);
    System.out.println("Request:" + request);
    System.out.println("-------");
    out.print(request);
    String status = in.readLine();
    String line = "";
    // String line = in.readLine();
    if (status.split(" ")[1].startsWith("3")) {
      printRedirectPOST(in);
    }
    StringBuilder output = new StringBuilder();
    if (isVerbose) {
      while ((line = in.readLine()) != null) {
        if (writeToFile) {
          output.append(line + Constants.NEWLINE);
        } else {
          System.out.println(line);
        }
        if (line.equals("}")) {
          break;
        }
      }
    } else {
      System.out.println("in here");
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
        else{
          if (writeToFile) {
            output.append(line + Constants.NEWLINE);
          } else {
            System.out.println(line);
          }
        }
      }
    }

    if (writeToFile) {
      FileUtility.writeOutputToFile(output,fileName);
    }
    in.close();
    out.close();
    socket.close();
  }

  private void printRedirectPOST(BufferedReader in) throws IOException {
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
    getResponsePOST(location);
  }


  private void parseInputPost(List<String> data) {
    try {
      if (data.contains("-o")) {
        fileName = data.get(data.size() - 1);
        System.out.println("file name:" + fileName);
        url = data.get(data.indexOf("-o") - 1).replaceAll("\\'", "");

      } else {
        url = data.get(data.size() - 1).replaceAll("\\'", "");
      }
      System.out.println("url POST:" + url);
    } catch (Exception e) {
      System.out.println("Please enter a valid URL");
      return;
    }
    try {
      uri = new URI(url);
      server = uri.getHost();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    if (data.contains("-v")) isVerbose = true;

    if (data.contains("-o")) writeToFile = true;

    StringBuilder readData = new StringBuilder("");

    if (data.contains("--d") || data.contains("-d")) {
      contentData = "";
      for (int i = 0; i < data.size(); i++) {
        if (data.get(i).equals("-d") || data.get(i).equals("--d")) {
          String content = data.get(i + 1);
          System.out.println("Content:" + content);
          if (content.contains("{")) {
            String datas[] = content.replaceAll("[\\'\\{\\}]", "").split(",");
            contentData = "{";
            for (int j = 0; j < datas.length - 1; j++) {
              String vals[] = datas[j].split(":");
              contentData += "\"" + vals[0] + "\": \"" + vals[1] + "\",";
            }
            String vals[] = datas[datas.length - 1].split(":");
            contentData += "\"" + vals[0] + "\": \"" + vals[1] + "\"}";
          } else if (content.contains("=")) {
            if (content.contains("&")) {
              String datas[] = content.split("&");
              contentData = "{";
              for (int j = 0; j < datas.length - 1; j++) {
                String vals[] = datas[j].split(":");
                contentData += "\"" + vals[0] + "\": \" " + vals[1] + "\",";
              }
              String vals[] = datas[datas.length - 1].split(":");
              contentData += "\"" + vals[0] + "\": \" " + vals[1] + "\"}";
            } else {
              String datas[] = content.split("=");
              contentData = "{\"" + datas[0] + "\":\"" + datas[1] + "\"}";
            }
          }
        }
      }
      System.out.println("Inline data:" + contentData);
      contentLength = contentData.length();

    } else {

      if (data.contains("-f")) {
        contentData = "";
        String inputLines = "";
        try {
          String currentDir = System.getProperty("user.dir");
          String fileToRead = data.get(data.indexOf("-f") + 1);
          System.out.println("File to send to the post:" + fileToRead);
          String filePath = currentDir + "/" + fileToRead;
          BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));

          while ((inputLines = bufferedReader.readLine()) != null) {
            readData.append(inputLines + "\n");
          }
          bufferedReader.close();
        } catch (IOException e) {
          System.out.println("Error reading file named ");
        }
        contentData = readData.toString();
        contentLength = readData.toString().length();
        System.out.println("From file:" + contentData);
      }
    }
  }
}
