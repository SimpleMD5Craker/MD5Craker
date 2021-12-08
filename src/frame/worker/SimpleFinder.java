package frame.worker;

import frame.common.Message;
import frame.common.Task;

public class SimpleFinder implements Runnable{

    private WorkerNode node;

    public SimpleFinder(WorkerNode node){
        this.node = node;
    }

    @Override
    public void run() {
        while(true) {
            Task t = WorkerNode.getNewTask();
            if(t != null) {
                while(true) {
                    int input = Integer.parseInt(t.getUserUid().split(":")[1]);
                    String[] range = t.getRange().split(":");
                    int low = Integer.parseInt(range[0]);
                    int up = Integer.parseInt(range[0]);
                    boolean found = false;
                    for (int i = low; i <= up; i++) {
                        if (i == input) {
                            found = true;
                            Task ans = new Task(t.getUserUid(), t.getRange(),
                                    String.format("Found %d between %d and %d", i, low, up));
                            WorkerNode.addNewMessage(new Message(Message.Type.HEARTBEAT, ans, node.getMasterAddress(),
                                    node.getSelfAddress()));
                        }
                    }
                    if(!found) {
                        Task ans = new Task(t.getUserUid(), t.getRange(), "notFound");
                        WorkerNode.addNewMessage(new Message(Message.Type.HEARTBEAT, ans, node.getMasterAddress(),
                                node.getSelfAddress()));
                    }
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
