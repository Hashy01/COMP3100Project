import java.io.*;
import java.net.*;

public class MyClient2 {

    static Socket hostSocket;
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
            hostSocket = new Socket("localhost", 50000);
            dis = new BufferedReader(new InputStreamReader(hostSocket.getInputStream()));
            dout = new DataOutputStream(hostSocket.getOutputStream());

            String serverResponse = "";

            // Send "HELO"
            clientSend("HELO");
            serverResponse = clientRead();

            // Send "AUTH"
            String username = System.getProperty("user.name");
            clientSend("AUTH " + username);
            serverResponse = clientRead();

            // Send "REDY"
            clientSend("REDY"); // Initial REDY before the loop
            serverResponse = clientRead();

            while (!serverResponse.contains("NONE")) {
                if (serverResponse.contains("JCPL") || serverResponse.contains("JOBN")) {
                    if (serverResponse.contains("JOBN")) {
                        String[] jobData = serverResponse.split(" ");
                        int jobCore = Integer.valueOf(jobData[4]);
                        int jobMemory = Integer.valueOf(jobData[5]);
                        int jobDisk = Integer.valueOf(jobData[6]);

                        clientSend("GETS Avail " + jobCore + " " + jobMemory + " " + jobDisk);
                        String getsResponse = clientRead();
                        String[] serverInfo = getsResponse.split(" ");
                        int nrecs = Integer.valueOf(serverInfo[1]);

                        if (nrecs == 0) {
                            clientSend("OK");
                            clientRead();
                            clientSend("GETS Capable " + jobCore + " " + jobMemory + " " + jobDisk);
                            getsResponse = clientRead();
                            serverInfo = getsResponse.split(" ");
                            nrecs = Integer.valueOf(serverInfo[1]);
                        }

                        // SEND "OK"
                        clientSend("OK");

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
                        clientRead();

                        String[] serverDataArr = serverData.split(" ");
                        String serverType = serverDataArr[0];
                        String serverID = serverDataArr[1];

                        dout.write(("SCHD " + jobData[2] + " " + serverType + " " + serverID + "\n").getBytes());
                        serverResponse = clientRead();
                    }

                    clientSend("REDY");

                }

                serverResponse = clientRead();

            }

            clientSend("QUIT");
            serverResponse = clientRead();
            dout.close();
            hostSocket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}