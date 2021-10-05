package client;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

public class AdminDriver {
    public static void main(String[] args) {
        ArrayList<Map.Entry<Long, Long>> listOfTimeSlots = new ArrayList<>();
        String testAdmin1 = "DVLA1234";

        try {
            AdminClient testClient1 = new AdminClient(testAdmin1);
            testClient1.createRoom(201, LocalDate.now(), listOfTimeSlots);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
