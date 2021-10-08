package client;

import model.CampusID;
import server.ServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import static client.ClientLogUtil.initiateLogger;

public class AdminClient {

    //RMI Variables
    private static final String CAMPUS_HOST = "localhost";
    private static final int CAMPUS_PORT = 1099; //change to 8080?
    private Registry registry;
    private String adminID;
    private CampusID campusID;
    private Logger logger;
    private ServerInterface server;

    private static final int USER_TYPE_POS = 3;
    private static final int CAMPUS_NAME_POS = 3;

    public AdminClient(String userID) throws RemoteException, NotBoundException {
        validateAdmin(userID);
        try {
            this.logger = initiateLogger(campusID, userID);
        } catch (Exception e) {
            throw new RemoteException("Login Error: Invalid ID.");
        }
        registry = LocateRegistry.getRegistry(CAMPUS_HOST, CAMPUS_PORT);
        server = (ServerInterface) registry.lookup(this.campusID.toString());

        System.out.println("Login Succeeded. | Admin ID: " +
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

    public synchronized void createRoom(int roomNumber, LocalDate date,
                                        ArrayList<Map.Entry<Long, Long>> listOfTimeSlots) throws RemoteException {
        this.logger.info(String.format("Client Log | Request: createRoom | AdminID: %s | Room number: %d | Date: %s",
                adminID, roomNumber, date.toString()));
        this.logger.info(server.createRoom(adminID, roomNumber, date, listOfTimeSlots));
    }

    public synchronized void deleteRoom(int roomNumber, LocalDate date, ArrayList<Map.Entry<Long, Long>> listOfTimeSlots)
            throws RemoteException {
        this.logger.info(String.format("Client Log | Request: deleteRoom | AdminID: %s | Room number: %d | Date: %s",
                adminID, roomNumber, date.toString()));
        this.logger.info(server.deleteRoom(adminID, roomNumber, date, listOfTimeSlots));
    }
}
