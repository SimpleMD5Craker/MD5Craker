package frame.master;

import frame.common.Message;

import java.util.concurrent.ConcurrentLinkedQueue;

class MasterQueueManager {
    private ConcurrentLinkedQueue<Message> receivedMsg;

    private ConcurrentLinkedQueue<Message> toSendMsg;

    /* Queue of userUid, the form of userUid is: "userId:userInput" */
    private ConcurrentLinkedQueue<String> newComingUsers;

    private static final MasterQueueManager manager = new MasterQueueManager();

    private MasterQueueManager() {
        receivedMsg = new ConcurrentLinkedQueue<>();
        toSendMsg = new ConcurrentLinkedQueue<>();
        newComingUsers = new ConcurrentLinkedQueue<>();
    }

    public static MasterQueueManager getManager() {
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

    public void newUser(String userUid){
        newComingUsers.offer(userUid);
    }

    public String pollUser() {
        if(newComingUsers.isEmpty()) {
            return null;
        } else {
            return newComingUsers.poll();
        }
    }
}
