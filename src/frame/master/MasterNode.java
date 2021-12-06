package frame.master;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.LineInputStream;
import frame.common.Config;
import frame.common.Message;
import frame.common.Node;
import frame.common.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MasterNode implements Node {

    private LinkedList<String> waitedUsers;

    private LinkedList<String> runningUsers;

    /* The entry of the map is (Task, LastUpdateTime),
    * If the difference between update time minus current time exceed MASTER_MAX_TASK_TIMEOUT, then the task will be
    * moved back to the waited task queue.
    * */
    private HashMap<Task, Integer> runningTasks;

    private LinkedList<Task> waitedTasks;

    /* List of workers, use "IP:port" to represent each worker, and the value of the map is the number of tasks assigned
    * to the worker */
    private HashMap<String, Integer> workers;


    public MasterNode() {
        waitedUsers = new LinkedList<>();
        runningUsers = new LinkedList<>();
        runningTasks = new HashMap<>();
        waitedTasks = new LinkedList<>();
        workers = new HashMap<>();
    }

    private String getWorkerWithMinimumTask() {
        return Collections.min(workers.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
    }


    @Override
    public void run() {
        Thread con = new Thread(new MasterCommunicator());
        con.start();
        while(true) {
            // 1. Begin to dispatch new task, first check whether there are workers, then check whether there is new
            //coming request.
            if(workers.size() > 0) {
                String userUid = MasterQueueManager.getManager().pollUser();
                if(userUid != null) {
                    // partition the task now.
                    String[] idAndInput = userUid.split(":");
                    if(idAndInput.length == 2) {
                        // only when the format of the userUid is correct we process the request
                        // TODO: implement the real task partition logic, now we use the hardcoded implementation for test
                        Task t1 = new Task(userUid, "aaaaa:bbbbb");
                        Task t2 = new Task(userUid, "bbbbc:ccccc");
                        MasterQueueManager.getManager().newSending(new Message(Message.Type.ASSIGNMENT, t1,
                                getWorkerWithMinimumTask()));
                        MasterQueueManager.getManager().newSending(new Message(Message.Type.ASSIGNMENT, t2,
                                getWorkerWithMinimumTask()));
                    }
                }
            }

            // 2. Check received messages, check at most MASTER_MAXIMUM_CHECK_RECEIVED_NUM messages per round,
            // update the task and worker state
            for(int i = 0; i < Config.MASTER_MAXIMUM_CHECK_RECEIVED_NUM; i++) {
                Message rcv = MasterQueueManager.getManager().pollReceived();
                if(rcv == null) {
                    break;
                }
                Message.Type taskType = rcv.getType();
                if(taskType == Message.Type.REGISTER) {
                    String addr = rcv.getAddress();
                    if(!workers.containsKey(addr)) {
                        workers.put(addr, 0);
                    }
                } else if(taskType == Message.Type.HEARTBEAT) {
                    String addr = rcv.getAddress();
                    Task task = rcv.getTask();
                    if(task == null) {
                        // worker is idle now
                        if(!workers.containsKey(addr)) {
                            // Though it is heartbeat, but the worker has been removed from the worker list.
                            // Thus the results
                        }
                    }
                }
            }

        }
    }

    public static void addNewUser(String userUid) {
        MasterQueueManager.getManager().newUser(userUid);
    }
}
