package client;

import com.sun.tools.javac.util.Pair;
import model.CampusID;
import server.CampusServer;
import server.ServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

import static model.CampusID.DVL;
import static model.CampusID.KKL;

public class StudentDriver {

    public static void main(String[] args) {
        String customer1 = "BCCA1234";

        try {
            StudentClient testClient1 = new StudentClient(customer1, DVL);
            Pair<Long, Long> timeSlot = new Pair<>(19L, 20L);
            testClient1.bookRoom(KKL, 201,LocalDate.now(), timeSlot);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }
}
