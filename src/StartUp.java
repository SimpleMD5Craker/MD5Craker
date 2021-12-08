import com.sun.corba.se.spi.orbutil.threadpool.Work;
import frame.common.Node;
import frame.master.MasterNode;
import frame.worker.WorkerNode;

public class StartUp {
    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            System.err.println("Invalid input format! The valid input should be: \"[master] [master ip address]\" or " +
                    "\"[worker] [worker ip address] [master ip:port]\"");
            System.exit(-1);
        }
        String nodeType = args[0];
        Node node;
        if ("master".equalsIgnoreCase(nodeType)) {
            String nodeIP = args[1];
            node = new MasterNode(nodeIP);
            node.run();
        } else if ("worker".equalsIgnoreCase(nodeType)) {
            String nodeIP = args[1];
            String masterIp = args[2];
            node = new WorkerNode(masterIp, nodeIP);
            node.run();
        } else {
            System.err.println("Invalid input format! The valid input should be: \"[master] [master ip address]\" or " +
                    "\"[worker] [worker ip address] [master ip:port]\"");
            System.exit(-1);
        }
    }
}
