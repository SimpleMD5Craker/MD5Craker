package frame.common;

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
     * 1. REGISTER: "IP:port" of the master
     * 2. HEARTBEAT: "IP:port" of the master
     * 3. ASSIGNMENT: "IP:port" of the worker that assigned by the master
     * */
    private String targetAddress;

    /**
     * 1. REGISTER: "IP:port" of the worker
     * 2. HEARTBEAT: "IP:port" of the worker
     * 3. ASSIGNMENT: "IP:port" of the master
     * */

    private String srcAddress;

    public Message(Type type, Task task, String tgt, String src) {
        this.type = type;
        this.task = task;
        this.targetAddress = tgt;
        this.srcAddress = src;
    }

    public Type getType() {
        return type;
    }

    public Task getTask() {
        return task;
    }


    public String getTargetAddress() {
        return targetAddress;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public String toString() {
        String strTask;
        if(task == null) {
            strTask = "null";
        } else {
            strTask = task.toString();
        }
        String tgtAddr = targetAddress;
        if(targetAddress == null) {
            tgtAddr = "empty";
        }
        String srcAddr = srcAddress;
        if(srcAddr == null) {
            srcAddr = "empty";
        }
        return String.join("$", type.toString(), strTask, tgtAddr, srcAddr);
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
        return new Message(type, task, segments[2], segments[3]);
    }

}
