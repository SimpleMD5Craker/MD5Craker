package frame.worker;

import frame.common.Config;
import frame.common.Message;
import frame.common.Node;
import frame.common.Task;

public class WorkerNode implements Node {
    private final WorkerCommunicator communicator;

    /* IP:Port address of master */
    private final String masterAddress;

    private final Cracker finder;


    public WorkerNode(String masterAddr, String selfAddress){
        communicator = new WorkerCommunicator(selfAddress);
        masterAddress = masterAddr;
        finder = new Cracker(this);
    }

    String getMasterAddress() {
        return masterAddress;
    }

    String getSelfAddress() {
        return communicator.getStrAddress();
    }

    @Override
    public void run() {
        Thread con = new Thread(communicator);
        Thread finder = new Thread(this.finder);
        con.start();
        finder.start();
        // Send register request
        Message register = new Message(Message.Type.REGISTER, null, masterAddress, communicator.getStrAddress());
        WorkerQueueManager.getManager().newSending(register);
        // TODO: Start Cracker here
        while(true) {
            try{
                Thread.sleep(Config.WORKER_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                System.err.printf("Worker failed to sleep!");
                System.exit(-1);
            }
            // 1. Check received messages, put tasks into taskQueue
            for(int i = 0; i < Config.WORKER_MAXIMUM_CHECK_RECEIVED_NUM; i++) {
                Message m = WorkerQueueManager.getManager().pollReceived();
                if(m == null) {
                    break;
                } else {
                    if(m.getType() == Message.Type.ASSIGNMENT) {
                        WorkerQueueManager.getManager().newTask(m.getTask());
                        System.out.printf("Worker %s received a task %s\n", getSelfAddress(), m.getTask());
                    }
                }
            }

            // 2. Prepare heartbeat message to master
            // TODO: get task from cracker
            Message heartbeat = new Message(Message.Type.HEARTBEAT, null, getMasterAddress(), getSelfAddress());
            WorkerQueueManager.getManager().newSending(heartbeat);
        }
    }

    public static Task getNewTask() {
        return WorkerQueueManager.getManager().pollTask();
    }

    public static void addNewMessage(Message m) {
        WorkerQueueManager.getManager().newSending(m);
    }
}
