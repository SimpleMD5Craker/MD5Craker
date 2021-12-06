package frame.master;

import frame.common.Config;
import frame.common.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;


/**
 * The master communicator is part of the master, which will use UDP to collect and send message.
 * The communicator periodically waits for datagrams from workers for 150 ms, if there is no datagram in, then the
 * communicator send datagram(tasks) that wait in the queue, together with another component QueueManager,
 * which stores received datagrams as well as datagrams to be sent
 */
public class MasterCommunicator implements Runnable{
    private DatagramSocket masterSocket;


    public MasterCommunicator() {
        try{
            this.masterSocket = new DatagramSocket(Config.MASTER_PORT_NUMBER);
            this.masterSocket.setSoTimeout(Config.MASTER_RECEIVE_WAIT_TIMEOUT);
        } catch (SocketException e) {
            System.err.printf("Failed to create socket for master: %s. Try to create it with the secondary port %d",
                    e.getMessage(), Config.MASTER_SECONDARY_PORT_NUMBER);
            try{
                this.masterSocket = new DatagramSocket(Config.MASTER_PORT_NUMBER);
                this.masterSocket.setSoTimeout(Config.MASTER_RECEIVE_WAIT_TIMEOUT);
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
                m = MasterQueueManager.getManager().pollSending();
                if(m != null) {
                    if(m.getAddress() != null && !m.getAddress().equals("empty")) {
                        String[] strAddress = m.getAddress().split(":");
                        InetSocketAddress address = new InetSocketAddress(strAddress[0], Integer.parseInt(strAddress[1]));
                        byte[] sendingData = m.toString().getBytes(StandardCharsets.UTF_8);
                        DatagramPacket sending = new DatagramPacket(sendingData, sendingData.length, address);
                        masterSocket.send(sending);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
