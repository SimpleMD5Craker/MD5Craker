package frame.master;

import frame.common.Config;
import frame.common.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;


/**
 * The master communicator is part of the master, which will use UDP to collect and send message.
 * The communicator periodically waits for datagrams from workers for 150 ms, if there is no datagram in, then the
 * communicator send datagram(tasks) that wait in the queue, together with another component QueueManager,
 * which stores received datagrams as well as datagrams to be sent
 */
public class MasterCommunicator implements Runnable{
    private DatagramSocket masterSocket;

    /* address used by the master, since the machine may have multiple NIC, manager of the master should choose one*/
    private final String ipAddr;

    private int usedPort;


    public MasterCommunicator(String ipAddr) {
        this.ipAddr = ipAddr;
        try{
            this.masterSocket = new DatagramSocket(null);
            this.masterSocket.bind(new InetSocketAddress(ipAddr, Config.MASTER_PORT_NUMBER));
            this.masterSocket.setSoTimeout(Config.MASTER_RECEIVE_WAIT_TIMEOUT);
            usedPort = Config.MASTER_PORT_NUMBER;
        } catch (SocketException e) {
            System.err.printf("Failed to create socket for master: %s. Try to create it with the secondary port %d",
                    e.getMessage(), Config.MASTER_SECONDARY_PORT_NUMBER);
            try{
                this.masterSocket = new DatagramSocket(null);
                this.masterSocket.bind(new InetSocketAddress(ipAddr, Config.MASTER_SECONDARY_PORT_NUMBER));
                this.masterSocket.setSoTimeout(Config.MASTER_RECEIVE_WAIT_TIMEOUT);
                usedPort = Config.MASTER_SECONDARY_PORT_NUMBER;
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
                byte[] data = new byte[Config.MASTER_MAXIMUM_RECEIVE_DATA_SIZE];
                DatagramPacket received = new DatagramPacket(data, data.length);
                masterSocket.receive(received);
                String strMessage = new String(received.getData(), received.getOffset(), received.getLength(),
                        StandardCharsets.UTF_8);
                Message m = Message.parseString(strMessage);
                if(m != null) {
                    MasterQueueManager.getManager().newReceived(m);
                }
            } catch (SocketTimeoutException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.err.printf("Master failed to receive message: %s", e.getMessage());
                System.exit(-1);
            }
            try {
                for (int i = 0; i < Config.MASTER_MAXIMUM_SENDING_NUM_PER_ROUND; i++) {
                    Message m = MasterQueueManager.getManager().pollSending();
                    if (m != null) {
                        if (m.getTargetAddress() != null && !m.getTargetAddress().equals("empty")) {
                            String[] strAddress = m.getTargetAddress().split(":");
                            InetSocketAddress address = new InetSocketAddress(strAddress[0], Integer.parseInt(strAddress[1]));
                            byte[] sendingData = m.toString().getBytes(StandardCharsets.UTF_8);
                            DatagramPacket sending = new DatagramPacket(sendingData, sendingData.length, address);
                            masterSocket.send(sending);
                        }
                    }
                }
            } catch(IOException e){
                System.err.printf("Master failed to send message: %s", e.getMessage());
                System.exit(-1);
            }
        }
    }

    public String getStrAddress() {
        return ipAddr + ":" + Integer.toString(usedPort);
    }
}
