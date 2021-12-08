package frame.master;

import frame.common.Config;
import frame.common.Message;
import frame.common.Node;
import frame.common.Task;
import frame.common.Utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

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

    /* address used by the master, since the machine may have multiple NIC, manager of the master should choose one*/

    private final MasterCommunicator communicator;



    public MasterNode(String selfIP) {
        waitedUsers = new LinkedList<>();
        runningUsers = new LinkedList<>();
        runningTasks = new HashMap<>();
        waitingTasks = new LinkedList<>();
        workers = new HashMap<>();
        communicator = new MasterCommunicator(selfIP);
    }


    private String getWorkerWithMinimumTask() {
        return Collections.min(workers.entrySet(), Comparator.comparingInt(entry -> entry.getValue().getV0())).getKey();
    }

    private void addWorkerNumTask(int increment, String worker) {
        Utils.Pair<Integer, Instant> original = workers.get(worker);
        int newNumRunningTasks = Math.max(original.getV0() + increment, 0);
        workers.put(worker, new Utils.Pair<>(newNumRunningTasks, original.getV1()));
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
            addWorkerNumTask(-1, workerAddr);
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

    private void finishTask(Task t, String worker) {
        if(runningTasks.containsKey(t)) {
            if(runningTasks.get(t).getV0().equals(worker)) {
                addWorkerNumTask(-1, worker);
            }
            runningTasks.remove(t);
        }
        if(t.isResultsFound()) {
            String user = t.getUserUid().split(":")[0];
            MasterQueueManager.getManager().newResult(String.join(":",
                    user, t.getResult()));
            userFinished(user);
            System.out.printf("Worker %s find answer \"%s\" for user:%s", worker, t.getResult(),
                    t.getUserUid().split(":")[0]);
        }
    }

    private void userFinished(String user) {
        runningUsers.remove(user);
    }


    @Override
    public void run() {
        Thread con = new Thread(communicator);
        con.start();
        while(true) {
            // 1. Begin to dispatch new task, first check whether there are workers, then check whether there is new
            //coming request
            if(workers.size() > 0) {
                String userUid = MasterQueueManager.getManager().pollUser();
                if(userUid != null) {
                    System.out.printf("Master begin to assign tasks: %s\n", userUid);
                    for(int i = 0; i < Config.MASTER_MAXIMUM_ADDED_USER_PER_ROUND; i++){
                        runningUsers.add(userUid);
                        // partition the task now.
                        String[] idAndInput = userUid.split(":");
                        if(idAndInput.length == 2) {
                            // only when the format of the userUid is correct(userID:Input) we process the request
                            // TODO: implement the real task partition logic, now we use the hardcoded implementation for test
                            Task t1 = new Task(userUid, "1:500");
                            Task t2 = new Task(userUid, "501:1000");
                            String assignedWorker = getWorkerWithMinimumTask();
                            MasterQueueManager.getManager().newSending(new Message(Message.Type.ASSIGNMENT, t1,
                                    assignedWorker, communicator.getStrAddress()));
                            runningTasks.put(t1, new Utils.Pair<>(assignedWorker, Instant.now()));
                            addWorkerNumTask(1, assignedWorker);

                            assignedWorker = getWorkerWithMinimumTask();
                            MasterQueueManager.getManager().newSending(new Message(Message.Type.ASSIGNMENT, t2,
                                    assignedWorker, communicator.getStrAddress()));
                            runningTasks.put(t2, new Utils.Pair<>(assignedWorker, Instant.now()));
                            addWorkerNumTask(1, assignedWorker);
                        }
                        userUid = MasterQueueManager.getManager().pollUser();
                        if(userUid == null) {
                            break;
                        }
                    }
                } else {
                    // TODO: The code segment below is just for test purpose
                    for(int i = 0; i < 3; i++){
                        Random r = new Random();
                        String input = Integer.toString(r.nextInt(1000));
                        String userId = r.ints(97, 122 + 1)
                                .limit(4)
                                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                .toString();
                        MasterQueueManager.getManager().newUser(userId + ":" + input);
                        System.out.printf("User:%s\nInput:%s\n", userId, input);
                    }
                }
            }
            System.out.println("Master Sleep");
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
                    String workerAddr = rcv.getSrcAddress();
                    if(!workers.containsKey(workerAddr)) {
                        addNewWorker(workerAddr);
                    }
                } else if(taskType == Message.Type.HEARTBEAT) {
                    String workerAddr = rcv.getSrcAddress();
                    Task workerTask = rcv.getTask();
                    if(workerTask == null) {
                        // worker is idle now
                        if(!workers.containsKey(workerAddr)) {
                            // Though it is heartbeat, but the worker has been removed from the worker list.
                            // Thus, we need to add it back , and remove the running task that has been assigned
                            // to the workers
                            addNewWorker(workerAddr);
                        } else {
                            // Update last update time of the worker
                            workers.get(workerAddr).setV1(Instant.now());
                        }
                    } else {
                        // worker is busy now
                        if(!workers.containsKey(workerAddr)){
                            // If the worker lost connection, then add it back
                            addNewWorker(workerAddr);
                        } else {
                            // Else update last-update-time of the worker
                            workers.get(workerAddr).setV1(Instant.now());
                        }
                        if(!runningTasks.containsKey(workerTask) && waitingTasks.contains(workerTask)) {
                            if(workerTask.isTaskFinished()) {
                                finishTask(workerTask, workerAddr);
                                waitingTasks.remove(workerTask);
                            } else {
                                // If the task is now waiting, then we add it to the running task
                                resurrectTask(workerTask, workerAddr);
                                addWorkerNumTask(1, workerAddr);
                            }
                        } else if(runningTasks.containsKey(workerTask) && !waitingTasks.contains(workerTask)) {
                            // The task is still running, but may have been assigned to others, if so, we keep this
                            // unchanged unless the task has been finished by this task(the owner of the heartbeat)
                            if(workerTask.isTaskFinished()) {
                                if(workerAddr.equals(getTaskExecutor(workerTask))) {
                                    // Only when in the master's record that the task was assigned to the worker,
                                    // then we finish the task
                                    finishTask(workerTask, workerAddr);
                                }
                            } else {
                                if(workerAddr.equals(getTaskExecutor(workerTask))) {
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

            // 3. Check tasks and workers in current record:
            // 3.1 First check worker
            HashSet<String> timeoutWorkers = new HashSet<>();
            HashSet<Task> tasksOfTimeoutWorkers = new HashSet<>();
            Instant current = Instant.now();
            for(String worker : workers.keySet()) {
                Instant lastUpdateTime = workers.get(worker).getV1();
                if(Duration.between(lastUpdateTime, current).toMillis() > Config.MASTER_WORKER_TIMEOUT) {
                    // The worker is considered lost connection with the master
                    timeoutWorkers.add(worker);
                    for(Task t: runningTasks.keySet()) {
                        //we also move all tasks assigned to it into waitingTask queue
                        if(runningTasks.get(t).getV0().equals(worker)){
                            tasksOfTimeoutWorkers.add(t);
                        }
                    }
                }
            }
            for(String timeOutWorker: timeoutWorkers) {
                workers.remove(timeOutWorker);
            }
            for(Task t: tasksOfTimeoutWorkers) {
                runningTasks.remove(t);
                waitingTasks.offer(t);
            }
            // TODO: 3.2 Check other timeout tasks, may not need this.

        }
    }

    public static void addNewUserRequest(String userUid) {
        MasterQueueManager.getManager().newUser(userUid);
    }

    public static String getNewResult() {
        return MasterQueueManager.getManager().pollResult();
    }

    public String getSelfAddress() {
        return communicator.getStrAddress();
    }
}
