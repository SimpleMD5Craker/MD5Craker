package frame.worker;

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
            do{
                Task t = WorkerQueueManager.getManager().pollTask();

            }
        }
    }
}
