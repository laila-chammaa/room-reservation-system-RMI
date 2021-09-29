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
    private static final String CAMPUS_HOST = "localhost";
    private static final int CAMPUS_PORT = 1199; //change to 8080?
    private Registry registry;
    private String adminID;
    private CampusID campusID;

    private static final int USER_TYPE_POS = 3;
    private static final int CAMPUS_NAME_POS = 3;

    public AdminClient(String userID) throws RemoteException, NotBoundException {
        validateAdmin(userID);

        registry = LocateRegistry.getRegistry(CAMPUS_HOST, CAMPUS_PORT);

        System.out.println("Login Sucessed. | Admin ID: " +
                this.adminID + " | Campus ID: " + this.campusID.toString());
    }

    private void validateAdmin(String userID) throws RemoteException {
        char userType = userID.charAt(USER_TYPE_POS);
        String campusName = userID.substring(0, CAMPUS_NAME_POS);

        if (userType != 'A') {
            throw new RemoteException("Login Error: This client is for admins only.");
        }
        this.adminID = userID;

        try {
            this.campusID = CampusID.valueOf(campusName);
        } catch (Exception e) {
            throw new RemoteException("Login Error: Invalid ID.");
        }
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
