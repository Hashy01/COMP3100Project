import java.io.*;
import java.net.*;
public class MyClient {
  public static void main(String[] args) {
    try {
      Socket s = new Socket("localhost", 50000);
      DataOutputStream dout = new DataOutputStream(s.getOutputStream());
      BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));

      dout.write(("HELO\n").getBytes());
      dout.flush();
      String str = (String) dis.readLine();
      System.out.println("Client Received= " + str);

      String username = System.getProperty("user.name");
      dout.write(("AUTH" + username + "\n").getBytes());
      dout.flush();
      str = (String) dis.readLine();
      System.out.println("Client Received= " + str);

      // Ready
      dout.write(("REDY\n").getBytes());
      dout.flush();
      String jobRequest = (String) dis.readLine();
      System.out.println("Client Received (REDY)= " + jobRequest);
      // Required for job scheduling later!
      String[] jobReqSplit = jobRequest.split(" ");
      int jobId = Integer.parseInt(jobReqSplit[2]); // keep track of jobID

      // GETS ALL
      dout.write(("GETS All\n").getBytes());
      dout.flush();
      String getsResponse = (String) dis.readLine();
      System.out.println("Client Received (getsResponse)= " + getsResponse);

      // C Sends OK
      dout.write(("OK\n").getBytes());
      dout.flush();

      // Server sends: DATA 7 124 (number of records)
      String[] indivData = getsResponse.split(" ");
      int nRecs = Integer.parseInt(indivData[1]); // we are keeping track of n which is 7
      System.out.println("nRecs " + nRecs);

      String serverInfo = ""; // for testing atm
      String largestServerType = ""; // initialise empty string to track the type of server i.e., medium
      String largestServerID = ""; // initialise empty string to track the type of serverid
      int largestCPU = 0; // Placeholder to track the amount of CPU cores

      for (int i = 0; i < nRecs; i++) {
        serverInfo = (String) dis.readLine();
        // Request information of all servers (7)
        System.out.println("What is servers doing: " + serverInfo);
        // split serverInfo into an array. Index 4 is the CPU core info of the server.
        String[] serverSplit = serverInfo.split(" ");
        int serverCPU = Integer.parseInt(serverSplit[4]); // Convert String to Int
        System.out.println("Amount of cores? " + serverSplit[4]);

        // Comparing CPU size of each server and storing the largest value in largestCPU
        if (serverCPU > largestCPU) {
          largestServerType = serverSplit[0];
          largestServerID = serverSplit[1];
          largestCPU = Integer.parseInt(serverSplit[4]);
        }
      }
      // We may now determine largestCPU is 8 and largestServerType is Medium
      System.out.println("largestCPU? " + largestCPU);
      System.out.println("largestServerType? " + largestServerType);
      System.out.println("largestServerID " + largestServerID);
      dout.write(("OK\n").getBytes());
      dis.readLine();
      dout.flush();

      if (jobReqSplit[0].equals("JOBN")) {
        String schdCommand = "SCHD " + jobId + " " + largestServerType + " " + largestServerID;
        dout.write((schdCommand + "\n").getBytes());
        dout.flush();
      }

      dout.write(("QUIT\n").getBytes());
      dout.flush();
      str = (String) dis.readLine();
      System.out.println("Client Received= " + str);

      dout.close();
      s.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
