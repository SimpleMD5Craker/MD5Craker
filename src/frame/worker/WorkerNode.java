package frame.worker;

import frame.common.Message;
import frame.common.Node;
import frame.common.Task;

public class WorkerNode implements Node {
    WorkerCommunicator communicator;

    public WorkerNode(){
        communicator = new WorkerCommunicator();
    }

    @Override
    public void run() {
        Thread con = new Thread(new WorkerCommunicator());
        con.start();

        // TODO: Start Cracker here
        while(true) {
            // 1. Check whether there are new task in the received queue
            while(true) {
                Message m = WorkerQueueManager.getManager().pollReceived();
            }
        }
    }

    public static Task getNewTask() {
        return WorkerQueueManager.getManager().pollTask();
    }

    public static void addNewMessage(Message m) {
        WorkerQueueManager.getManager().newSending(m);
    }
}
