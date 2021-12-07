package frame.worker;

import frame.common.Config;
import frame.common.Message;
import frame.common.Node;
import frame.common.Task;

public class WorkerNode implements Node {
    private final WorkerCommunicator communicator;

    /* IP:Port address of master */
    private final String masterAddress;


    public WorkerNode(String masterAddr, String selfAddress){
        communicator = new WorkerCommunicator(selfAddress);
        masterAddress = masterAddr;
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
        con.start();
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
                    }
                }
            }

            // 2. Send heartbeat to master
        }
    }

    public static Task getNewTask() {
        return WorkerQueueManager.getManager().pollTask();
    }

    public static void addNewMessage(Message m) {
        WorkerQueueManager.getManager().newSending(m);
    }
}
