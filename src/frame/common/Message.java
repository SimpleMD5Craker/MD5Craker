package frame.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Message {
    public enum Type{
        /* send by worker, tell the master it can work for the master now.*/
        REGISTER{
            public String toString() {
                return "REGISTER";
            }
        },
        /* send by worker, periodically send to the master, to tell the state of the worker */
        HEARTBEAT {
            public String toString() {
                return "HEARTBEAT";
            }
        },
        ASSIGNMENT {
            public String toString() {
                return "ASSIGNMENT";
            }
        };

        public static Type parseType(String strType) {
            switch (strType) {
                case("REGISTER"):
                    return REGISTER;
                case("HEARTBEAT"):
                    return HEARTBEAT;
                case("ASSIGNMENT"):
                    return ASSIGNMENT;
                default:
                    return null;
            }
        }
    }

    private Type type;

    /**
     * If the type is:
     *  1. REGISTER: task is null
     *  2. HEARTBEAT: if the worker is now idle, then task is null; else task is the current running task of the worker
     *  3. ASSIGNMENT: task is the assigned one
     */
    private Task task;


    /**
     * 1. REGISTER: "IP:port" of the worker
     * 2. HEARTBEAT: "IP:port" of the worker
     * 3. ASSIGNMENT: "IP:port" of the worker that assigned by the master
     * */
    private String address;

    public Message(Type type, Task task, String address) {
        this.type = type;
        this.task = task;
        this.address = address;
    }

    public Type getType() {
        return type;
    }

    public Task getTask() {
        return task;
    }


    public String getAddress() {
        return address;
    }

    public String toString() {
        String strTask;
        if(task == null) {
            strTask = "null";
        } else {
            strTask = task.toString();
        }
        String revisedAddress = address;
        if(address == null) {
            revisedAddress = "empty";
        }
        return String.join("$", type.toString(), strTask, revisedAddress);
    }

    public static Message parseString(String strMessage) {
        String[] segments = strMessage.split("\\$");
        if(segments.length != 3) {
            return null;
        }
        Type type = Type.parseType(segments[0]);
        if(type == null) {
            return null;
        }
        Task task;
        if(segments[1].equals("null")) {
            task = null;
        } else {
            task = Task.parseString(segments[1]);
        }
        return new Message(type, task, segments[2]);
    }

}
