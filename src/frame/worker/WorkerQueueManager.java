package frame.worker;

import frame.common.Message;
import frame.common.Task;

import java.util.concurrent.ConcurrentLinkedQueue;

class WorkerQueueManager {
    private ConcurrentLinkedQueue<Message> receivedMsg;

    private ConcurrentLinkedQueue<Message> toSendMsg;

    private ConcurrentLinkedQueue<Task> taskQueue;

    private static final WorkerQueueManager manager = new WorkerQueueManager();

    private WorkerQueueManager() {
        receivedMsg = new ConcurrentLinkedQueue<>();
        toSendMsg = new ConcurrentLinkedQueue<>();
        taskQueue = new ConcurrentLinkedQueue<>();
    }

    public static WorkerQueueManager getManager() {
        return manager;
    }

    public void newReceived(Message msg) {
        receivedMsg.offer(msg);
    }

    public void newSending(Message msg) {
        toSendMsg.offer(msg);
    }

    public Message pollReceived() {
        if(receivedMsg.isEmpty()) {
            return null;
        } else {
            return receivedMsg.poll();
        }
    }

    public Message pollSending(){
        if(toSendMsg.isEmpty()) {
            return null;
        } else {
            return toSendMsg.poll();
        }
    }

    public void newTask(Task t) {
        taskQueue.offer(t);
    }

    public Task pollTask() {
        if(taskQueue.isEmpty()) {
            return null;
        } else {
            return taskQueue.poll();
        }

    }
}
