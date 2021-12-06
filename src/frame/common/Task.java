package frame.common;

import java.util.Objects;

public class Task {
    /* The userUid is in the form of: "userId:userInput"*/
    private String userUid;

    /* index1:index2 */
    private String range;

    /* If the result is found, then set it to the results, else set it as null */
    private String result;

    public Task(String uid, String range) {
        userUid = uid;
        this.range = range;
        this.result = "null";
    }

    public Task(String uid, String range, String result) {
        userUid = uid;
        this.range = range;
        this.result = result;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getRange() {
        return range;
    }

    public String toString() {
        return userUid + ";" + range + ";" + result;
    }

    public static Task parseString(String strTask) {
        String[] segments = strTask.split(";");
        if(segments.length != 3) {
            return null;
        }
        return new Task(segments[0], segments[1], segments[2]);
    }


    public String getResult() {
        return result;
    }

    public boolean isTaskFinished() {
        return !result.equals("null");
    }

    public boolean isResultsFound() {
        return !result.equals("null") && !result.equalsIgnoreCase("notFound");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return userUid.equals(task.userUid) && range.equals(task.range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userUid, range);
    }

//    public static String[] partitionTask(int workerSize) {
//        int passwordMaxLen = Config.MAXIMUM_PASSWORD_LENGTH;
//
//    }
}
