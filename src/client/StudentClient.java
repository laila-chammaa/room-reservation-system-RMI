package client;

import com.sun.tools.javac.util.Pair;
import model.CampusID;
import server.ServerInterface;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

public class StudentClient {

    //RMI Variables
    private static final String BANK_HOST = "localhost";
    private static final int BANK_PORT = 1099;
    private Registry registry;
    private String customerID;
    private CampusID campusID;

    public StudentClient(String customerID, CampusID branchID) throws RemoteException, NotBoundException {
        this.customerID = customerID;
        this.campusID = branchID;

        registry = LocateRegistry.getRegistry(BANK_HOST, BANK_PORT);

        System.out.println("Login Sucessed. | Customer ID: " +
                this.customerID + " | Branch ID: " + this.campusID.toString());
    }

    public synchronized void bookRoom(CampusID campusID, int roomNumber, LocalDate date,
                                      Pair<Long, Long> timeSlot)
            throws RemoteException, NotBoundException {
        //TODO: maybe authentication right here? and on the server's side
        ServerInterface server = (ServerInterface) registry.lookup(this.campusID.toString());
        server.bookRoom(campusID, roomNumber, date, timeSlot);
    }
}
