package frame.master;

import frame.common.Config;
import frame.common.Message;
import frame.common.Node;
import frame.common.Task;
import frame.common.Utils;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MasterNode implements Node {

    private LinkedList<String> waitedUsers;

    private LinkedList<String> runningUsers;

    /* The entry of the map is (Task, LastUpdateTime),
     If the difference between update time minus current time exceed MASTER_MAX_TASK_TIMEOUT, then the task will be
     moved back to the waited task queue. */
    private HashMap<Task, Utils.Pair<String, Instant>> runningTasks;

    private LinkedList<Task> waitingTasks;

    /* List of workers, use "IP:port" to represent each worker, and the value of the map is a pair of the number of the
     task assigned to the worker and last update time of the worker */
    private HashMap<String, Utils.Pair<Integer, Instant>> workers;


    public MasterNode() {
        waitedUsers = new LinkedList<>();
        runningUsers = new LinkedList<>();
        runningTasks = new HashMap<>();
        waitingTasks = new LinkedList<>();
        workers = new HashMap<>();
    }

    private String getWorkerWithMinimumTask() {
        return Collections.min(workers.entrySet(), Comparator.comparingInt(entry -> entry.getValue().getV0())).getKey();
    }

    private void addWorkerNumTask(int increment, String worker) {
        Utils.Pair<Integer, Instant> original = workers.get(worker);
        workers.put(worker, new Utils.Pair<>(original.getV0() + increment, original.getV1()));
    }

    private void stopTasksAssignedTo(String workerAddr) {
        HashSet<Task> stoppedOnes = new HashSet<>();
        for(Task t : runningTasks.keySet()) {
            if(runningTasks.get(t).getV0().equals(workerAddr)) {
                stoppedOnes.add(t);
                if(waitingTasks.contains(t)) {
                    System.err.printf("The task %s is in both running and waiting queue!\n", t);
                    System.exit(-1);
                }
            }
        }
        for(Task t: stoppedOnes) {
            runningTasks.remove(t);
            waitingTasks.offer(t);
        }
    }

    private void addNewWorker(String workerAddr) {
        workers.put(workerAddr, new Utils.Pair<>(0, Instant.now()));
    }

    private void updateWorkerHeartbeat(String workerAddr) {
        workers.get(workerAddr).setV1(Instant.now());
    }

    private void stopTask(Task t) {
        runningTasks.remove(t);
        waitingTasks.offer(t);
    }

    private String getTaskExecutor(Task t) {
        if(runningTasks.containsKey(t)) {
            return runningTasks.get(t).getV0();
        } else {
            return null;
        }
    }

    private void resurrectTask(Task t, String assignedWorker) {
        waitingTasks.remove(t);
        runningTasks.put(t, new Utils.Pair<>(assignedWorker, Instant.now()));
    }

    private void updateTask(Task t) {
        runningTasks.get(t).setV1(Instant.now());
    }

    private void finishTask(Task t) {
        runningTasks.remove(t);
        if(t.isResultsFound()) {
            String user = t.getUserUid().split(":")[0];
            MasterQueueManager.getManager().newResult(String.join(":",
                    user, t.getResult()));
            userFinished(user);
        }
    }

    private void userFinished(String user) {
        runningUsers.remove(user);
    }


    @Override
    public void run() {
        Thread con = new Thread(new MasterCommunicator());
        con.start();
        while(true) {
            // 1. Begin to dispatch new task, first check whether there are workers, then check whether there is new
            //coming request, TODO: finally check whether there are waiting tasks
            if(workers.size() > 0) {
                String userUid = MasterQueueManager.getManager().pollUser();
                if(userUid != null) {
                    runningUsers.add(userUid);
                    // partition the task now.
                    String[] idAndInput = userUid.split(":");
                    if(idAndInput.length == 2) {
                        // only when the format of the userUid is correct we process the request
                        // TODO: implement the real task partition logic, now we use the hardcoded implementation for test
                        Task t1 = new Task(userUid, "aaaaa:bbbbb");
                        Task t2 = new Task(userUid, "bbbbc:ccccc");
                        String assignedWorker = getWorkerWithMinimumTask();
                        MasterQueueManager.getManager().newSending(new Message(Message.Type.ASSIGNMENT, t1,
                                assignedWorker));
                        runningTasks.put(t1, new Utils.Pair<>(assignedWorker, Instant.now()));
                        addWorkerNumTask(1, assignedWorker);

                        assignedWorker = getWorkerWithMinimumTask();
                        MasterQueueManager.getManager().newSending(new Message(Message.Type.ASSIGNMENT, t2,
                                assignedWorker));
                        runningTasks.put(t2, new Utils.Pair<>(assignedWorker, Instant.now()));
                        addWorkerNumTask(1, assignedWorker);
                    }
                }
            }

            try{
                Thread.sleep(Config.MASTER_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                System.err.printf("Master failed to sleep: %s", e.getMessage());
                System.exit(-1);
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
                    String workerAddr = rcv.getAddress();
                    if(!workers.containsKey(workerAddr)) {
                        addNewWorker(workerAddr);
                    }
                } else if(taskType == Message.Type.HEARTBEAT) {
                    String workerAddr = rcv.getAddress();
                    Task workerTask = rcv.getTask();
                    if(workerTask == null) {
                        // worker is idle now
                        if(!workers.containsKey(workerAddr)) {
                            // Though it is heartbeat, but the worker has been removed from the worker list.
                            // Thus, we need to add it back , and remove the running task that has been assigned
                            // to the workers
                            stopTasksAssignedTo(workerAddr);
                            addNewWorker(workerAddr);
                        } else {
                            // Update last update time of the worker
                            workers.get(workerAddr).setV1(Instant.now());
                        }
                    } else {
                        // worker is busy now
                        if(!workers.containsKey(workerAddr)) {
                            // We need to add the worker back , and only keep the task currently executed by the worker in the
                            // runningTasks map, move other running ones that assigned to the worker to the waiting task.

                            addNewWorker(workerAddr);
                            if(!runningTasks.containsKey(workerTask) && waitingTasks.contains(workerTask)) {
                                // If the task is now waiting, then we add it to the running task
                                if(workerTask.isTaskFinished()) {
                                    finishTask(workerTask);
                                    waitingTasks.remove(workerTask);
                                } else {
                                    stopTasksAssignedTo(workerAddr);
                                    resurrectTask(workerTask, workerAddr);
                                    addWorkerNumTask(1, workerAddr);
                                }
                            } else if(runningTasks.containsKey(workerTask) && !waitingTasks.contains(workerTask)) {
                                // The task is still running, but may have been assigned to others, if so, we keep this
                                // unchanged unless the task has been finished by this task(the owner of the heartbeat)
                                stopTasksAssignedTo(workerAddr);
                                if(workerTask.isTaskFinished()) {
                                    finishTask(workerTask);
                                } else {
                                    if(getTaskExecutor(workerTask) == null) {
                                        // Since we removed all running tasks of the worker, so by this condition we can
                                        // know that the task was executing by the worker according to the record of the
                                        // master. Thus add it back.
                                        resurrectTask(workerTask, workerAddr);
                                        addWorkerNumTask(1, workerAddr);
                                    }
                                }
                            } else if(runningTasks.containsKey(workerTask) && waitingTasks.contains(workerTask)) {
                                // Should not reach here, if happens, then we can know that our system is broken.
                                System.err.printf("One lost task is in both running and waiting queue: %s", workerTask);
                                System.exit(-1);
                            }

                        } else {
                            // Update last-update-time of the worker
                            workers.get(workerAddr).setV1(Instant.now());

                            if(!runningTasks.containsKey(workerTask) && waitingTasks.contains(workerTask)) {
                                // If the task is now waiting and not finished by the worker,
                                // then we add it to the running task
                                if(workerTask.isTaskFinished()) {
                                    finishTask(workerTask);
                                    waitingTasks.remove(workerTask);
                                } else {
                                    resurrectTask(workerTask, workerAddr);
                                    addWorkerNumTask(1, workerAddr);
                                }
                            } else if(runningTasks.containsKey(workerTask) && !waitingTasks.contains(workerTask)) {
                                // The task is still running, but may have been assigned to others, if so, we keep this
                                // unchanged unless the task has been finished by this task(the owner of the heartbeat)
                                if(workerTask.isTaskFinished()) {
                                    finishTask(workerTask);
                                } else {
                                    if(getTaskExecutor(workerTask).equals(workerAddr)) {
                                        // Only when in the master's record that the task was assigned to the worker,
                                        // then we update the last-heartbeat-time of the task
                                        updateTask(workerTask);
                                    }
                                }
                            } else if(runningTasks.containsKey(workerTask) && waitingTasks.contains(workerTask)) {
                                // Should not reach here, if happens, then we can know that our system is broken.
                                System.err.printf("One lost task is in both running and waiting queue: %s", workerTask);
                                System.exit(-1);
                            }
                        }
                    }
                }
            }

        }
    }

    public static void addNewUserRequest(String userUid) {
        MasterQueueManager.getManager().newUser(userUid);
    }
}
