package frame.common;

public class Config {
    // Configurations for master
    public static int MASTER_RECEIVE_WAIT_TIMEOUT = 200; //ms, the wait time for MasterCommunicator to receive datagrams.

    public static int MASTER_PORT_NUMBER = 12345;

    public static int MASTER_SECONDARY_PORT_NUMBER = 13524;

    public static int MASTER_MAXIMUM_RECEIVE_DATA_SIZE = 512; // in bytes

    public static int MASTER_MAXIMUM_CHECK_RECEIVED_NUM = 10;

    public static int MAXIMUM_PASSWORD_LENGTH = 5;
}
