package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

public class AdminDriver {
    public static void main(String[] args) {
        ArrayList<Map.Entry<Long, Long>> listOfTimeSlots = new ArrayList<>();
        String testAdmin1 = "DVLA1234";
        String testAdmin2 = "KKLA1234";

        try {
            AdminClient testClient1 = new AdminClient(testAdmin1);
            AdminClient testClient2 = new AdminClient(testAdmin2);

            testClient1.createRoom(201, LocalDate.now(), listOfTimeSlots);
            testClient1.createRoom(201, LocalDate.now(), listOfTimeSlots);
            testClient1.createRoom(231, LocalDate.now(), listOfTimeSlots);
            testClient2.createRoom(203, LocalDate.now(), listOfTimeSlots);
            testClient2.createRoom(211, LocalDate.now(), listOfTimeSlots);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }
}
