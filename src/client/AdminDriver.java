package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.Month;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class AdminDriver {
    public static void main(String[] args) {
        ArrayList<Map.Entry<Long, Long>> listOfTimeSlots = new ArrayList<>();
        listOfTimeSlots.add(new AbstractMap.SimpleEntry<>(19L, 20L));
        listOfTimeSlots.add(new AbstractMap.SimpleEntry<>(12L, 13L));
        listOfTimeSlots.add(new AbstractMap.SimpleEntry<>(15L, 16L));

        ArrayList<Map.Entry<Long, Long>> listOfTimeSlots2 = new ArrayList<>();
        listOfTimeSlots2.add(new AbstractMap.SimpleEntry<>(1L, 2L));

        ArrayList<Map.Entry<Long, Long>> listOfTimeSlots3 = new ArrayList<>();
        listOfTimeSlots3.add(new AbstractMap.SimpleEntry<>(15L, 16L));

        String aid1 = "KKLA1234";
        String aid2 = "WSTA1234";
        String aid3 = "DVLA1234";
        String aid4 = "KKLS1214";

        try {
            AdminClient testClient1 = new AdminClient(aid1);
            AdminClient testClient2 = new AdminClient(aid2);
            AdminClient testClient3 = new AdminClient(aid3);

            //testing synchronization with multiple admins
            testClient1.createRoom(201, LocalDate.of(2020, Month.JANUARY, 3), listOfTimeSlots);
            testClient1.createRoom(201, LocalDate.of(2020, Month.JANUARY, 3), listOfTimeSlots2);
            testClient2.createRoom(211, LocalDate.of(2020, Month.JANUARY, 4), listOfTimeSlots);
            testClient2.deleteRoom(211, LocalDate.of(2020, Month.JANUARY, 4), listOfTimeSlots3);
            testClient3.createRoom(203, LocalDate.of(2020, Month.JANUARY, 1), listOfTimeSlots);


            //testing invalid admin
//            AdminClient studentClient = new AdminClient(aid4);
//            studentClient.createRoom(201, LocalDate.now(), listOfTimeSlots);


        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }
}
