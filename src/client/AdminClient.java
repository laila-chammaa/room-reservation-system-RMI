package client;

import model.CampusID;
import server.ServerInterface;

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
    private static final int CAMPUS_PORT = 1199; //change to 8080?
    private Registry registry;
    private String adminID;
    private CampusID campusID;
    private Logger logger;

    private static final int USER_TYPE_POS = 3;
    private static final int CAMPUS_NAME_POS = 3;

    public AdminClient(String userID) throws RemoteException {
        validateAdmin(userID);
        try {
            this.logger = initiateLogger(campusID, userID);
        } catch (Exception e) {
            throw new RemoteException("Login Error: Invalid ID.");
        }
        registry = LocateRegistry.getRegistry(CAMPUS_HOST, CAMPUS_PORT);

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
                                              ArrayList<Map.Entry<Long, Long>> listOfTimeSlots) {

        try {
            ServerInterface server = (ServerInterface) registry.lookup(this.campusID.toString());
            String result = server.createRoom(roomNumber, date, listOfTimeSlots);
            //TODO: generate recordID
            int recordID = 1;

            if (result != null) {
                logger.info("Room Successfully Created. | Record ID: " + recordID);
            } else {
                logger.warning("Room Creation Error: Unable to Create Room. Please consult server log.");
            }

        } catch (Exception e) {
            logger.warning("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
