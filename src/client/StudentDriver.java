package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;

import static model.CampusID.KKL;

public class StudentDriver {

    public static void main(String[] args) {
        String sid1 = "DVLS1234";
        Map.Entry<Long, Long> timeSlot = new AbstractMap.SimpleEntry<>(19L, 20L);

        try {
            StudentClient testClient1 = new StudentClient(sid1);
            testClient1.bookRoom(KKL, 201, LocalDate.now(), timeSlot);
            testClient1.cancelBooking(sid1);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }
}
