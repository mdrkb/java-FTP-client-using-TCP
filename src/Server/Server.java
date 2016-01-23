package Server;
/*
 * @author Rakibul Islam
 */
import java.io.*;
import java.net.*;

public class Server {
    static int Mid;
    static int[] auth = {0, 0, 0};
    
    public static void main(String[] args) throws Exception{
        
        ServerSocket serversocket = new ServerSocket(1234);
        System.out.println("Socket is open: "+!serversocket.isClosed());
        
        Socket clientsocket[] = new Socket[3];
        BufferedReader inFromClient[] = new BufferedReader[3];
        DataOutputStream outToClient[] = new DataOutputStream[3];
        
        // Initially accept all clients for authentication
        for(int i=0; i<3; i++){
            System.out.println("Waiting...");
            clientsocket[i] = serversocket.accept();
            System.out.println("connected "+ (i+1));
            inFromClient[i] = new BufferedReader(new InputStreamReader(clientsocket[i].getInputStream()));
            outToClient[i] = new DataOutputStream(clientsocket[i].getOutputStream());
        }
        
        SThread thread[] = new SThread[3];
        for(int i=0; i<3; i++){
            thread[i] = new SThread(inFromClient[i], outToClient, i, clientsocket);
        }
        for(int i=0; i<3; i++){
            thread[i].join();
        }
    }
}

class SThread extends Thread{
    BufferedReader inFromClient;
    DataOutputStream outToClient[];
    int srcId;
    String input;
    
    int[] totalClient = new int[3];
    Socket clientsocket[] = new Socket[3];
    
    String workingDir = System.getProperty("user.dir")+"\\src\\Server\\user-pass.txt";
    
    public SThread(BufferedReader in, DataOutputStream out[], int id, Socket[] s){
        inFromClient = in;
        outToClient = out;
        srcId = id;
        clientsocket = s;
        start();
    }
    
    @Override
    public void run(){
        try{
            while(true){
                if(Server.auth[srcId]==0){
                    BufferedReader inFromFile = new BufferedReader(new FileReader(workingDir));
                    String uname = inFromFile.readLine() + ' ';
                    String pass = inFromFile.readLine() + ' ';

                    String input = inFromClient.readLine();
                    String values[] = input.split("%up%");
                    String username = values[0];
                    String password = values[1];
                        
                    if(uname.matches(username) && pass.matches(password)){
                        System.out.println("Client " + (srcId+1) + " is accepted");
                        Server.auth[srcId] = 1;
                        outToClient[srcId].writeBytes("1" + '\n');
                    }
                    else{
                        System.out.println("Client" + (srcId+1) + " is not accepted");
                        outToClient[srcId].writeBytes("0" + '\n');
                        clientsocket[srcId].close();
                        break;
                    }
                }
                else{
                    String input = inFromClient.readLine();
                    System.out.println("From Client "+(srcId+1)+": "+input);
                    
                    if(input.contains("cmd")){
                        Server.Mid = srcId;
                        if(input.matches("getListcmd") || input.contains("copy")){
                            for(int i=0; i<3; i++){
                                if(i!=srcId && Server.auth[i]==1){
                                    outToClient[i].writeBytes(input+'\n');
                                }
                            }
                        }
                        else{
                            outToClient[srcId].writeBytes("Invalid command. Try again..." + '\n');
                        }
                    }
                    else{
                        outToClient[Server.Mid].writeBytes(input+'\n');
                    }
                }
            }
        }catch(Exception e){
            System.out.println("Error in Server Thread: "+e);
        }
    }
}

