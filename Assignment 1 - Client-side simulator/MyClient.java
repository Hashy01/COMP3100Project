import java.io.*;
import java.net.*;
import java.util.ArrayList;
public class MyClient {

  static Socket s;
  static DataOutputStream dout;
  static BufferedReader dis;

  static void clientSend(String message) throws IOException {
    dout.write((message + "\n").getBytes());
    dout.flush();
    System.out.println("Client Sent: " + message);
  }

  static String clientRead() throws IOException {
    String message = (String) dis.readLine();
    System.out.println("Server Message: " + message);
    return message;
  }

  public static void main(String[] args) {
    try {

      s = new Socket("localhost", 50000);
      dout = new DataOutputStream(s.getOutputStream());
      dis = new BufferedReader(new InputStreamReader(s.getInputStream()));

      // SEND HELO
      clientSend("HELO");
      clientRead();

      // SEND AUTH
      String username = System.getProperty("user.name");
      clientSend("AUTH " + username);
      clientRead();

      // SEND REDY
      clientSend("REDY");
      String jobRequest = clientRead();

      // SEND GETS All
      clientSend("GETS All");
      String getsResponse = clientRead();

      // SEND OK
      clientSend("OK");

      // Server sends: DATA 7 124 (number of records)
      String[] serverData = getsResponse.split(" ");
      int nRecs = Integer.parseInt(serverData[1]);

      String largestServerType = ""; // initialise empty string to track the type of server i.e., medium
      ArrayList < String > largestServerList = new ArrayList < > (); // ArrayList to store the servers to.
      int largestCPU = 0; // // Placeholder to track the amount of CPU cores

      for (int i = 0; i < nRecs; i++) {
        String serverInfo = (String) dis.readLine();
        // split serverInfo into an array. Index 4 is the CPU core info of the server.   
        String[] serverSplit = serverInfo.split(" ");
        int serverCPU = Integer.parseInt(serverSplit[4]);

        // Comparing CPU size of each server and storing the largest value 
        // Also checking, to see if largest servertype of a different name exists
        if (serverCPU > largestCPU) {
          largestServerType = serverSplit[0];
          largestCPU = serverCPU;
          largestServerList.clear();
          largestServerList.add(serverSplit[1]);
        } else if (serverCPU == largestCPU && serverSplit[0].equals(largestServerType)) {
          largestServerList.add(serverSplit[1]);
        }
      }
      clientSend("OK");
      clientRead();

      int currentIndex = 0;

      while (true) {
        String[] jobReqSplit = jobRequest.split(" ");

        if (jobReqSplit[0].equals("JOBN")) {
          int jobId = Integer.parseInt(jobReqSplit[2]);
          String serverInfo = largestServerList.get(currentIndex);
          String schdCommand = "SCHD " + jobId + " " + largestServerType + " " + serverInfo;

          System.out.println(schdCommand);
          dout.write((schdCommand + "\n").getBytes());
          dout.flush();
          System.out.println((String) dis.readLine());
          // LRR Implementation: If Index > number of servers then reset index 
          currentIndex = (currentIndex + 1) % largestServerList.size();
        } else if (jobReqSplit[0].equals("NONE")) {
          break;
        }

        // SEND REDY
        clientSend("REDY");
        jobRequest = clientRead();
      }
      clientSend("QUIT");
      clientRead();

      dout.close();
      s.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

}
