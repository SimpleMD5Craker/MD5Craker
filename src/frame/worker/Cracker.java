package frame.worker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import frame.common.Message;
import frame.common.Task;


public class Cracker implements Runnable{

    private WorkerNode node;
    public Cracker(WorkerNode node){
        this.node = node;
    }



    @Override
    public void run() {
        Process proc;
        while(true) {
            Task t = WorkerNode.getNewTask();
            if(t != null) {
                while(true) {
                    String input = t.getUserUid().split(":")[1];
                    String[] range = t.getRange().split(":");
                    int low = Integer.parseInt(range[0]);
                    int up = Integer.parseInt(range[1]);
                    String result = "notFound";
                    try {
                        String pythonFile = "python /Users/syp1997/Desktop/CS655/geni-mini-project/MD5Craker/src/frame/worker/crack.py " + " --password " +input+ " --left " +low+ " --right "+up;
                        System.out.println("run python file: "+pythonFile);
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

