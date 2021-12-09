package frame.worker;

import frame.common.Config;
import frame.common.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class WorkerCommunicator implements Runnable{
    private DatagramSocket workerSocket;

    private String ipAddr;

    private int usedPort;


    public WorkerCommunicator(String ipAddr) {
        this.ipAddr = ipAddr;
        try{
            this.workerSocket = new DatagramSocket(null);
            this.workerSocket.bind(new InetSocketAddress(ipAddr, Config.WORKER_PORT_NUMBER));
            this.workerSocket.setSoTimeout(Config.WORKER_RECEIVE_WAIT_TIMEOUT);
            usedPort = Config.WORKER_PORT_NUMBER;
        } catch (SocketException e) {
            System.err.printf("Failed to create socket for worker: %s. Try to create it with the secondary port %d\n",
                    e.getMessage(), Config.WORKER_SECONDARY_PORT_NUMBER);
            try{
                this.workerSocket = new DatagramSocket(null);
                this.workerSocket.bind(new InetSocketAddress(ipAddr, Config.WORKER_SECONDARY_PORT_NUMBER));
                this.workerSocket.setSoTimeout(Config.WORKER_RECEIVE_WAIT_TIMEOUT);
                usedPort = Config.WORKER_SECONDARY_PORT_NUMBER;
            } catch (SocketException ee){
                System.err.printf("Failed to create socket for worker: %s. End the process.",
                        ee.getMessage());
                System.exit(-1);
            }
        }
    }

    public String getStrAddress() {
        return ipAddr + ":" + Integer.toString(usedPort);
    }

    @Override
    public void run() {
        System.out.println("Worker Communicator Start!");
        while(true) {
            try {
                for(int i = 0; i < Config.WORKER_MAXIMUM_CHECK_RECEIVED_NUM*2; i++) {
                    byte[] data = new byte[Config.WORKER_MAXIMUM_RECEIVE_DATA_SIZE];
                    DatagramPacket received = new DatagramPacket(data, data.length);
                    workerSocket.receive(received);
                    String strMessage = new String(received.getData(), received.getOffset(), received.getLength(),
                            StandardCharsets.UTF_8);
                    Message m = Message.parseString(strMessage);
                    if (m != null) {
                        WorkerQueueManager.getManager().newReceived(m);
                    }
                }
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                System.err.printf("Worker failed to receive message: %s", e.getMessage());
                System.exit(-1);
            }

            try{
                Thread.sleep(Config.WORKER_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                System.err.printf("Worker failed to sleep!");
                System.exit(-1);
            }

            try {
                for (int i = 0; i < Config.WORKER_MAXIMUM_SENDING_NUM_PER_ROUND; i++) {
                    Message m = WorkerQueueManager.getManager().pollSending();
                    if (m != null) {
                        if (m.getTargetAddress() != null && !m.getTargetAddress().equals("empty")) {
//                            System.out.printf("Worker communicator send message %s\n", m);
                            String[] strAddress = m.getTargetAddress().split(":");
                            InetSocketAddress address = new InetSocketAddress(strAddress[0], Integer.parseInt(strAddress[1]));
                            byte[] sendingData = m.toString().getBytes(StandardCharsets.UTF_8);
                            DatagramPacket sending = new DatagramPacket(sendingData, sendingData.length, address);
                            workerSocket.send(sending);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.printf("Worker failed to send message: %s", e.getMessage());
                System.exit(-1);
            }

        }
    }
}
