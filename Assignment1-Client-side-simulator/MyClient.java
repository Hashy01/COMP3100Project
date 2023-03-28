import java.io.*;
import java.net.*;
public class MyClient {
  public static void main(String[] args) {
    try {
      Socket s = new Socket("localhost", 50000);
      DataOutputStream dout = new DataOutputStream(s.getOutputStream());
      dout.write(("HELO\n").getBytes());
      dout.flush();
    //   DataInputStream dis = new DataInputStream(s.getInputStream());
      BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
      String str = (String) dis.readLine();
      System.out.println("Client Received= " + str);
    //   dout.writeUTF("BYE");
    String username = System.getProperty("user.name");
      dout.write(("AUTH" + username + "\n").getBytes());
      dout.flush();
      str = (String) dis.readLine();
      System.out.println("Client Received= " + str);

      dout.write(("REDY\n").getBytes());
      dout.flush();
      str = (String) dis.readLine();
      System.out.println("Client Received= " + str);

      // GETS ALL
      dout.write(("GETS All\n").getBytes());
      dout.flush();
      str = (String) dis.readLine();
      System.out.println("Client Received= " + str);

       // C Sends OK
       dout.write(("OK\n").getBytes());
       dout.flush();
       str = (String) dis.readLine();
       System.out.println("Client Received= " + str);     


      // dout.write(("QUIT\n").getBytes());
      // dout.flush();
      // str = (String) dis.readLine();
      // System.out.println("Client Received= " + str);

      dout.close();
      s.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}