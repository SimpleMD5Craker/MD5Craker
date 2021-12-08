package frame.master;// Java implementation of  Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java
// TODO: let web use sinlge connection always.

import frame.common.Config;

import java.net.*;
import java.io.*;


// Server class
public class Server implements Runnable{

//    public Server(){
//        ServerSocket serverSocket = null;
//    }


    @Override
    public void run() {
        try{
            ServerSocket ss = new ServerSocket(Config.SERVER_PORT_NUMBER);
            System.out.println("port: "+Config.SERVER_PORT_NUMBER);
            Socket t = ss.accept();
            try{
                t.setSoTimeout(150);
                PrintWriter socketSender = new PrintWriter(t.getOutputStream(), true);
                BufferedReader socketReceiver = new BufferedReader(new InputStreamReader(t.getInputStream()));
                while(true) {
                    try{
                        String in = socketReceiver.readLine();
                        System.out.println(in);
                        /* Some operations on the input */
                        if(in != null) {
                            String userId = in.split(":")[0];
                            String code = in.split(":")[1];
                            MasterNode.addNewUserRequest(userId + ":" + code);
                        }
                    } catch (SocketTimeoutException e) {

                    } catch (IOException e) {
                        System.exit(-1);
                    }
//                    String result = MasterNode.getNewResult();
                    String result = "results";
                    socketSender.println(result);
//                    while(result != null) {
//                        socketSender.println(result);
//                        result = MasterNode.getNewResult();
//                    }
                    Thread.sleep(1000);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

//        ServerSocket serverSocket = null;
//        while(true)
//        {
//            try
//            {
//                serverSocket = new ServerSocket(Config.SERVER_PORT_NUMBER);
//                System.out.println("等待远程连接，端口号为：" + serverSocket.getLocalPort() + "...");
//                Socket server = serverSocket.accept();
//                System.out.println("远程主机地址：" + server.getRemoteSocketAddress());
//                DataInputStream in = new DataInputStream(server.getInputStream());
//                byte[] msg = new byte[10000];
//                in.read(msg);
//                String decoded_msg = new String(msg, "UTF-8");
////                System.out.println(in.read(msg));
//                System.out.println(decoded_msg);
//                DataOutputStream out = new DataOutputStream(server.getOutputStream());
////                out.writeUTF(decoded_msg);
//                String userId = decoded_msg.split(":")[0];
//                String code = decoded_msg.split(":")[1];
//                MasterNode.addNewUserRequest(userId + ":" + code);
//                String res = MasterNode.getNewResult();
//                if(res != null) {
//                    out.writeUTF(res);
//                }
//                server.close();
////                OutputStream outputStream = rstSender.getOutputStream();
////                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
////                bw.write(res);  // return the result to the manager
////                bw.flush();
//            }catch(SocketTimeoutException s)
//            {
//                System.out.println("Socket timed out!");
//                break;
//            }catch(IOException e)
//            {
//                e.printStackTrace();
//                break;
//            }
//        }
    }
}