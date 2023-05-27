import java.io.*;
import java.net.*;

public class MyClient2 {

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

  public static void main(String args[]) {

    try {
      s = new Socket("localhost", 50000);
      dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
      dout = new DataOutputStream(s.getOutputStream());

      String serverResponse = "";

      // Send "HELO"
      clientSend("HELO");
      serverResponse = clientRead();
      System.out.println("RCVD " + serverResponse);

      // Send "AUTH"
      String username = System.getProperty("user.name");
      clientSend("AUTH " + username);
      serverResponse = clientRead();
      System.out.println("RCVD " + serverResponse);

      // Send "REDY"
      clientSend("REDY"); // Initial REDY before the loop
      serverResponse = clientRead();
      System.out.println("RCVD " + serverResponse);

      while (!serverResponse.contains("NONE")) {
        if (serverResponse.contains("JCPL") || serverResponse.contains("JOBN")) {
          if (serverResponse.contains("JOBN")) {
            String[] jobData = serverResponse.split(" ");
            int jobCore = Integer.valueOf(jobData[4]);
            int jobMemory = Integer.valueOf(jobData[5]);
            int jobDisk = Integer.valueOf(jobData[6]);

            clientSend("GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk);
            // dout.write(("GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk + "\n").getBytes());
            System.out.println("SENT GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk);

            String[] serverInfo = dis.readLine().split(" ");
            int nrecs = Integer.valueOf(serverInfo[1]);
            // SEND "OK"
            clientSend("OK");
            System.out.println("SENT OK");

            String serverData = "";
            String firstServer = "";

            boolean serverLocatedFlag = false;

            for (int i = 0; i < nrecs; i++) {
              String currentServerData = dis.readLine();
              String[] serverDataArr = currentServerData.split(" ");
              int serverCores = Integer.parseInt(serverDataArr[4]);
              if (i == 0) {
                firstServer = currentServerData;
              }
              if (serverCores >= jobCore && !serverLocatedFlag) {
                serverData = currentServerData;
                serverLocatedFlag = true;
              }
            }

            if (!serverLocatedFlag) {
              serverData = firstServer;
            }

            clientSend("OK");
            System.out.println("SENT OK");
            clientRead();

            String[] serverDataArr = serverData.split(" ");
            String serverType = serverDataArr[0];
            String serverID = serverDataArr[1];

            dout.write(("SCHD " + jobData[2] + " " + serverType + " " + serverID + "\n").getBytes());
            System.out.println("SENT SCHD " + jobData[2] + " " + serverType + " " + serverID);
            serverResponse = clientRead();
          }

          clientSend("REDY");
          System.out.println("SENT REDY");
        } else {
          System.out.println("message: " + serverResponse);
        }

        serverResponse = clientRead();
        if (serverResponse != null) {
          System.out.println("RCVD " + serverResponse);
        } else {
          System.out.println("RCVD null message");
        }
      }

      clientSend("QUIT");
      serverResponse = clientRead();
      System.out.println("RCVD " + serverResponse);
      dout.close();
      s.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}