package client;

import com.sun.tools.javac.util.Pair;
import model.CampusID;
import server.ServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.ArrayList;

public class AdminClient {

    //RMI Variables
    private static final String BANK_HOST = "localhost";
    private static final int BANK_PORT = 1099; //change to 8080?
    private Registry registry = null;
    private String customerID;
    private CampusID campusID;

    private static final int ACCOUNT_TYPE_POS = 2;

    public AdminClient(String customerID, CampusID campusID) throws RemoteException, NotBoundException {
        char accountType = customerID.charAt(ACCOUNT_TYPE_POS);
        if (accountType != 'M') {
            throw new RemoteException("Login Error: This client is for managers only.");
        }

        this.customerID = customerID;
        this.campusID = campusID;
        registry = LocateRegistry.getRegistry(BANK_HOST, BANK_PORT);

        System.out.println("Login Sucessed. | Customer ID: " +
                this.customerID + " | Campus ID: " + this.campusID.toString());
    }

    public synchronized void createRoomRecord(int roomNumber, LocalDate date,
                                              ArrayList<Pair<Long, Long>> listOfTimeSlots, String roomID) {

        try {
            ServerInterface server = (ServerInterface) registry.lookup(this.campusID.toString());
            String result = server.createRoom(roomNumber, date, listOfTimeSlots);

            if (result != null) {
                System.out.println("Room Successfully Created. | Room ID: " + roomID);
            } else {
                System.out.println("Room Creation Error: Room Unable to Create. Please consult server log.");
            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
