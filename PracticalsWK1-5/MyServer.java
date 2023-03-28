import java.io.*;  
import java.net.*;  
public class MyServer {  
public static void main(String[] args){  
try{  
ServerSocket ss=new ServerSocket(6666);  
Socket s=ss.accept();//establishes connection   
DataInputStream dis=new DataInputStream(s.getInputStream());  
String  str=(String)dis.readUTF();  
System.out.println("message= "+str);  
DataOutputStream dout=new DataOutputStream(s.getOutputStream()); 
dout.writeUTF("G'DAY");
dout.flush();
str=(String)dis.readUTF();
System.out.println("message= "+str);  
dout.writeUTF("BYE");
dout.flush();
dout.close();
ss.close();  
}catch(Exception e){System.out.println(e);}  
}  
}  