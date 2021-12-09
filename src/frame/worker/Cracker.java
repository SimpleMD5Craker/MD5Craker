package frame.worker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import frame.common.Message;
import frame.common.Task;


public class Cracker implements Runnable{

    private WorkerNode node;

    private String pythonCommand;
    public Cracker(WorkerNode node){
        this.node = node;
        try{
            File f = new File("./command.txt");
            Scanner scanner = new Scanner(f);
            boolean hasRead = false;
            while(scanner.hasNextLine() && !hasRead) {
                this.pythonCommand = scanner.nextLine();
                hasRead = true;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cracker Failed to read command file! File \"./command.txt\" doesn't exist");
            System.exit(-1);
        }
    }



    @Override
    public void run() {
        Process proc;
        while(true) {
            Task t = WorkerNode.getNewTask();
            if(t != null) {
                for(int i = 0; i < 10; i++) {
                    String input = t.getUserUid().split(":")[1];
                    String[] range = t.getRange().split(":");
                    int low = Integer.parseInt(range[0]);
                    int up = Integer.parseInt(range[1]);
                    String result = "notFound";
                    try {
                        String pythonFile = this.pythonCommand + " --password " +input+ " --left " +low+ " --right "+up;
                        System.out.println("run task for user: " + t.getUserUid());
                        proc = Runtime.getRuntime().exec(pythonFile);
                        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            System.out.println(line);
                            if(line.substring(0, 9).equals("find code")){
                                result = line.substring(11, 16);
                                break;
                            }
                        }
                        in.close();
                        proc.waitFor();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Task ans = new Task(t.getUserUid(), t.getRange(), String.format(result));
                    WorkerNode.addNewMessage(new Message(Message.Type.HEARTBEAT, ans, node.getMasterAddress(), node.getSelfAddress()));
                    t = WorkerNode.getNewTask();
                    if(t == null) {
                        break;
                    }
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

}

