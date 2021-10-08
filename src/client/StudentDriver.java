package client;

import model.CampusID;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static model.CampusID.DVL;
import static model.CampusID.KKL;

public class StudentDriver {

    public static void main(String[] args) {
        String sid1 = "DVLS1234";
        Map.Entry<Long, Long> timeSlot = new AbstractMap.SimpleEntry<>(19L, 20L);

        try {
            StudentClient testClient1 = new StudentClient(sid1);
            testClient1.bookRoom(KKL, 201, LocalDate.now(), timeSlot);
            HashMap<CampusID, Integer> serverTimeSlot = testClient1.getAvailableTimeSlot(LocalDate.now());
            serverTimeSlot.forEach((key, value) -> System.out.printf("Campus: %s, Available slots: %d%n",
                    key.toString(), value));
            testClient1.bookRoom(DVL, 201, LocalDate.now(), timeSlot);
            testClient1.cancelBooking(sid1);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }
}
