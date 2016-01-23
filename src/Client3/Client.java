package Client3;
/*
 * @author Rakibul Islam
 */
import java.net.*;
import java.io.*;

public class Client {
    static String file_name;
    
    public static void main(String[] args) throws Exception{

        InetAddress ip = InetAddress.getLocalHost();
        System.out.println("IP: " + ip);
        Socket clientsocket = new Socket(ip, 1234);
        
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
        DataOutputStream outToServer = new DataOutputStream(clientsocket.getOutputStream());
        
        CThread write = new CThread(inFromServer, outToServer, 0, clientsocket);
        CThread read = new CThread(inFromServer, outToServer, 1, clientsocket);
        write.join();
        read.join();
    }
}

class CThread extends Thread{
    BufferedReader inFromServer;
    DataOutputStream outToServer;
    Socket clientsocket;
    int flag, auth = 0, reply = 1;
    
    public CThread(BufferedReader in, DataOutputStream out, int f, Socket s){
        inFromServer = in;
        outToServer = out;
        flag = f;
        clientsocket = s;
        start();
    }
    
    @Override
    public void run(){
        String input;
        try{
           while(true){
               BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
               if(flag==0){         // Write to Server
                   if(auth==0 && reply==1){
                        reply = 0;
                        System.out.print("Enter username: ");
                        String username = inFromUser.readLine() + ' ';
                        System.out.print("Enter password: ");
                        String password = inFromUser.readLine() + ' ';
                        outToServer.writeBytes(username + "%up%" + password + '\n');
                   }
                   else{
                       String cmd = inFromUser.readLine();
                       if(cmd.contains("copy")){
                           String values[] = cmd.split(" ");
                            String fpart = values[0];
                            String lpart = values[1];
                            Client.file_name = lpart.trim();
                            String workingDir = System.getProperty("user.dir")+"\\src\\Client3\\filelist.txt";
                            String currentLine, totalLine = "";
                            BufferedReader inFromFile = new BufferedReader(new FileReader(workingDir));
                            while ((currentLine = inFromFile.readLine()) != null) {
                                 totalLine += currentLine + '\t';
                            }
                            if(totalLine.contains(lpart)){
                                System.out.println(lpart+" is already in the directory");
                            }
                            else{
                                outToServer.writeBytes(cmd.trim()+"cmd\n");
                            }
                       }
                       else outToServer.writeBytes(cmd.trim()+"cmd\n");
                   }
                   
               }
               else if(flag==1){    // Read from Server
                   input = inFromServer.readLine();
                   if(!input.contains("cmd") && !input.contains("0") && !input.contains("1") && !input.contains("cpy")){
                       System.out.println(input);
                   }
                   else{
                        String workingDir = System.getProperty("user.dir")+"\\src\\Client3\\filelist.txt";
                        if(input.matches("1")){
                             auth = 1;
                             System.out.println("Successfully connected to server.");
                             System.out.print("\nEnter commands:\n");
                        }
                        else if(input.matches("0")){
                             System.out.println("Authentication error. Client is closed");
                             clientsocket.close();
                             System.exit(0);
                             break;
                        }
                        else if(input.matches("getListcmd")){
                            String currentLine, totalLine = "";
                            BufferedReader inFromFile = new BufferedReader(new FileReader(workingDir));
                            while ((currentLine = inFromFile.readLine()) != null) {
                                 totalLine += currentLine + '\t';
                            }
                            outToServer.writeBytes(totalLine + '\n');
                        }
                        else if(input.contains("copy")){
                            String values[] = input.split(" ");
                            String fpart = values[0];
                            String lpart = values[1].replaceAll("cmd", "");
                            String currentLine, totalLine = "";
                            BufferedReader inFromFile = new BufferedReader(new FileReader(workingDir));
                            
                            while ((currentLine = inFromFile.readLine()) != null) {
                                 totalLine += currentLine + '\t';
                            }
                            if(totalLine.contains(lpart)){
                                String path = System.getProperty("user.dir")+"\\src\\Client3\\"+lpart;
                                //System.out.println(path);
                                totalLine = "";
                                BufferedReader fileTxt = new BufferedReader(new FileReader(path));
                                while ((currentLine = fileTxt.readLine()) != null) {
                                    totalLine += currentLine;
                                }
                                outToServer.writeBytes(totalLine + "cpy\n");
                            }
                        }
                        else if(input.contains("cpy")){
                            input = input.replace("cpy","");
                            String path = System.getProperty("user.dir")+"\\src\\Client3\\"+Client.file_name;
                            BufferedWriter output = null;
                            try {
                                File file = new File(path);
                                output = new BufferedWriter(new FileWriter(file));
                                output.write(input);
                            } catch ( IOException e ) {
                                System.out.println("Error in writing files.");
                            } finally {
                                if(output != null) output.close();
                                System.out.println(Client.file_name+" is copied successfully.");
                            }
                        }
                    }
               }
           }
        }catch(Exception e){
            System.out.println("Error in Client Thread: "+e);
        }
    }
}
