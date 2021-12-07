package frame.worker;

import frame.common.Config;
import frame.common.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class WorkerCommunicator implements Runnable{
    private DatagramSocket workerSocket;


    public WorkerCommunicator() {
        try{
            this.workerSocket = new DatagramSocket(Config.WORKER_PORT_NUMBER);
            this.workerSocket.setSoTimeout(Config.WORKER_RECEIVE_WAIT_TIMEOUT);
        } catch (SocketException e) {
            System.err.printf("Failed to create socket for worker: %s. Try to create it with the secondary port %d",
                    e.getMessage(), Config.WORKER_SECONDARY_PORT_NUMBER);
            try{
                this.workerSocket = new DatagramSocket(Config.WORKER_SECONDARY_PORT_NUMBER);
                this.workerSocket.setSoTimeout(Config.WORKER_RECEIVE_WAIT_TIMEOUT);
            } catch (SocketException ee){
                System.err.printf("Failed to create socket for master: %s. End the process.",
                        ee.getMessage());
                System.exit(-1);
            }
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                byte[] data = new byte[Config.WORKER_MAXIMUM_RECEIVE_DATA_SIZE];
                DatagramPacket received = new DatagramPacket(data, data.length);
                workerSocket.receive(received);
                String strMessage = new String(received.getData(), received.getOffset(), received.getLength(),
                        StandardCharsets.UTF_8);
                Message m = Message.parseString(strMessage);
                if(m != null) {
                    WorkerQueueManager.getManager().newReceived(m);
                }
                m = WorkerQueueManager.getManager().pollSending();
                if(m != null) {
                    if(m.getAddress() != null && !m.getAddress().equals("empty")) {
                        String[] strAddress = m.getAddress().split(":");
                        InetSocketAddress address = new InetSocketAddress(strAddress[0], Integer.parseInt(strAddress[1]));
                        byte[] sendingData = m.toString().getBytes(StandardCharsets.UTF_8);
                        DatagramPacket sending = new DatagramPacket(sendingData, sendingData.length, address);
                        workerSocket.send(sending);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
