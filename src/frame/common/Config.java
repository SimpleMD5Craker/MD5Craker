package frame.common;

public class Config {
    // Configurations for master
    public static int MASTER_RECEIVE_WAIT_TIMEOUT = 200; //ms, the wait time for MasterCommunicator to receive datagrams.

    public static int MASTER_PORT_NUMBER = 12345;

    public static int MASTER_SECONDARY_PORT_NUMBER = 13524;

    public static int MASTER_MAXIMUM_RECEIVE_DATA_SIZE = 512; // in bytes

    public static int MASTER_MAXIMUM_CHECK_RECEIVED_NUM = 10;

    /* The maximum number of messages that can be sent by the master communicator per round */
    public static int MASTER_MAXIMUM_SENDING_NUM_PER_ROUND = 10;

    public static long MASTER_SLEEP_INTERVAL = 200; // ms

    public static long MASTER_TASK_TIMEOUT = 1000; // ms

    public static long MASTER_WORKER_TIMEOUT = 1000; // ms

    public static int MAXIMUM_PASSWORD_LENGTH = 5;

    public static long WORKER_SLEEP_INTERVAL = 100;

    public static int WORKER_PORT_NUMBER = 54321;

    public static int WORKER_SECONDARY_PORT_NUMBER = 42531;

    public static int WORKER_RECEIVE_WAIT_TIMEOUT = 00; //ms, the wait time for WorkerCommunicator to receive datagrams.

    public static int WORKER_MAXIMUM_RECEIVE_DATA_SIZE = 512; // in bytes

    public static int WORKER_MAXIMUM_CHECK_RECEIVED_NUM = 10;

    /* The maximum number of messages that can be sent by the worker communicator per round */
    public static int WORKER_MAXIMUM_SENDING_NUM_PER_ROUND = 10;
}
