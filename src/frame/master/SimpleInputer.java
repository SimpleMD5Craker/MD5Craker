package frame.master;

import java.util.Random;
import java.util.Scanner;

public class SimpleInputer implements Runnable{

    @Override
    public void run() {
        while(true) {
            Scanner input = new Scanner(System.in);
            String in = input.next();
            Random r = new Random();
            String userId = r.ints(97, 122 + 1)
                    .limit(4)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            MasterNode.addNewUserRequest(userId + ":" + in);

            String res = MasterNode.getNewResult();
            if(res != null) {
                System.out.println("Get Result " + res);
            }
        }
    }
}
