import java.net.*;
import java.io.*;

public class GreetingServer extends Thread
{
    private ServerSocket serverSocket;

    public GreetingServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(0);
    }

    public void run()
    {
        while(true)
        {
            try
            {
                System.out.println("等待远程连接，端口号为：" + serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("远程主机地址：" + server.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(server.getInputStream());
                byte[] msg = new byte[10000];
                System.out.println("after receive in");
                in.read(msg);
                String decoded_msg = new String(msg, "UTF-8");
//                System.out.println(in.read(msg));
                System.out.println(decoded_msg);
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
//                out.writeUTF(decoded_msg);
                out.writeUTF("hello this is from java server");
                server.close();

//                OutputStream outputStream = rstSender.getOutputStream();
//                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
//                bw.write(res);  // return the result to the manager
//                bw.flush();
            }catch(SocketTimeoutException s)
            {
                System.out.println("Socket timed out!");
                break;
            }catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }
    public static void main(String [] args)
    {
        int port = Integer.parseInt(args[0]);
        try
        {
            Thread t = new GreetingServer(port);
            t.run();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}