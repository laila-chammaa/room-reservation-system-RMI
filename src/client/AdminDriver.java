package client;

import com.sun.tools.javac.util.Pair;
import model.CampusID;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.ArrayList;

public class AdminDriver {
    public static void main(String[] args) {
        try {
            ArrayList<Pair<Long, Long>> listOfTimeSlots = new ArrayList<>();
            String cid1 = "BCCA1234";

            //TODO: shouldn't we be generating the customerID
            String testAdmin1 = "BCMA1234";
            AdminClient testClient1 = new AdminClient(testAdmin1);
            testClient1.createRoomRecord(201, LocalDate.now(), listOfTimeSlots, cid1);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }
}
