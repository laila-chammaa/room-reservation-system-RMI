package client;

import model.CampusID;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.Month;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static model.CampusID.*;

public class StudentDriver {

    public static void main(String[] args) {
        String sid1 = "DVLS1234";
        String sid2 = "DVLS2234";
        String sid3 = "DVLA1234";
        String sid4 = "DVLD1234";

        Map.Entry<Long, Long> timeSlot = new AbstractMap.SimpleEntry<>(19L, 20L);
        Map.Entry<Long, Long> timeSlot2 = new AbstractMap.SimpleEntry<>(2L, 15L);
        Map.Entry<Long, Long> timeSlot3 = new AbstractMap.SimpleEntry<>(12L, 13L);
        Map.Entry<Long, Long> invalidTimeSlot = new AbstractMap.SimpleEntry<>(26L, 20L);

        try {
            StudentClient testClient1 = new StudentClient(sid1);
            StudentClient testClient2 = new StudentClient(sid2);
            //StudentClient adminClient = new StudentClient(sid3);
            //StudentClient invalidClient = new StudentClient(sid4);

            HashMap<CampusID, Integer> serverTimeSlot = testClient1.getAvailableTimeSlot(LocalDate.now());
            serverTimeSlot.forEach((key, value) -> System.out.printf("Campus: %s, Available slots: %d%n",
                    key.toString(), value));

            //testing invalid parameters:
            //testClient1.bookRoom(KKL, 201, LocalDate.of(2020, Month.JANUARY, 3), invalidTimeSlot);
            //adminClient.bookRoom(KKL, 201, LocalDate.of(2020, Month.JANUARY, 3), timeslot);

            //testing max booking for student
            testClient1.bookRoom(KKL, 201, LocalDate.of(2020, Month.JANUARY, 3), timeSlot);
            testClient1.bookRoom(WST, 211, LocalDate.of(2020, Month.JANUARY, 4), timeSlot);
            String bookingID = testClient1.bookRoom(DVL, 203, LocalDate.of(2020, Month.JANUARY, 1), timeSlot);
            testClient1.bookRoom(DVL, 203, LocalDate.of(2020, Month.JANUARY, 1), timeSlot3);
            //testClient1.bookRoom(DVL, 203, LocalDate.of(2020, Month.JANUARY, 1), timeSlot3);

            testClient2.cancelBooking(bookingID); //shouldn't work since it's not the student who booked
            testClient1.cancelBooking(bookingID);
            testClient1.bookRoom(DVL, 203, LocalDate.of(2020, Month.JANUARY, 1), timeSlot3);


        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }
}