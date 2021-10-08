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
        String sid2 = "DVLA1234";
        String sid3 = "DVLD1234";

        Map.Entry<Long, Long> timeSlot = new AbstractMap.SimpleEntry<>(19L, 20L);
        Map.Entry<Long, Long> timeSlot2 = new AbstractMap.SimpleEntry<>(2L, 15L);
        Map.Entry<Long, Long> invalidTimeSlot = new AbstractMap.SimpleEntry<>(26L, 20L);

        try {
            StudentClient testClient1 = new StudentClient(sid1);
            //StudentClient adminClient = new StudentClient(sid2);
            //StudentClient invalidClient = new StudentClient(sid3);

            testClient1.bookRoom(KKL, 201, LocalDate.now(), timeSlot);
            HashMap<CampusID, Integer> serverTimeSlot = testClient1.getAvailableTimeSlot(LocalDate.now());
            serverTimeSlot.forEach((key, value) -> System.out.printf("Campus: %s, Available slots: %d%n",
                    key.toString(), value));
            testClient1.bookRoom(DVL, 201, LocalDate.now(), timeSlot);
            testClient1.cancelBooking(sid1);

            //testing invalid parameters:
            //testClient1.bookRoom(KKL, 201, LocalDate.of(2020, Month.JANUARY, 3), invalidTimeSlot);
            //adminClient.bookRoom(KKL, 201, LocalDate.of(2020, Month.JANUARY, 3), timeslot);

            //testing max booking for student
            testClient1.bookRoom(KKL, 201, LocalDate.of(2020, Month.JANUARY, 3), timeSlot);
            testClient1.bookRoom(WST, 201, LocalDate.of(2020, Month.JANUARY, 4), timeSlot);
            testClient1.bookRoom(DVL, 201, LocalDate.of(2020, Month.JANUARY, 5), timeSlot);


        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }
}

/*
Testing synchronization with multiple admins and users.
Testing with incorrect/invalid parameters (dates, time slot format)
Testing max limit of booking for a student, cancelling and trying again with different servers.

 */