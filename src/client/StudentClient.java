package client;

import com.sun.tools.javac.util.Pair;
import model.CampusID;
import server.ServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.logging.Logger;

import static client.ClientLogUtil.initiateLogger;

public class StudentClient {

    //RMI Variables
    private static final String CAMPUS_HOST = "localhost";
    private static final int CAMPUS_PORT = 1199;
    private Registry registry;
    private String studentID;
    private CampusID campusID;
    private Logger logger;

    static final int USER_TYPE_POS = 3;

    public StudentClient(String userID) throws RemoteException {
        validateStudent(userID);

        try {
            initiateLogger(campusID, userID);
        } catch (Exception e) {
            throw new RemoteException("Login Error: Invalid ID.");
        }

        registry = LocateRegistry.getRegistry(CAMPUS_HOST, CAMPUS_PORT);

        System.out.println("Login Succeeded. | Student ID: " +
                this.studentID + " | Campus ID: " + this.campusID.toString());
    }

    private void validateStudent(String userID) throws RemoteException {
        char userType = userID.charAt(USER_TYPE_POS);
        String campusName = userID.substring(0, USER_TYPE_POS);

        if (userType != 'S') {
            throw new RemoteException("Login Error: This client is for students only.");
        }
        this.studentID = userID;

        try {
            this.campusID = CampusID.valueOf(campusName);
        } catch (Exception e) {
            throw new RemoteException("Login Error: Invalid ID.");
        }
    }

    public synchronized void bookRoom(CampusID campusID, int roomNumber, LocalDate date,
                                      Pair<Long, Long> timeSlot)
            throws RemoteException, NotBoundException {
        //TODO: maybe authentication right here? and on the server's side
        ServerInterface server = (ServerInterface) registry.lookup(this.campusID.toString());
        server.bookRoom(campusID, roomNumber, date, timeSlot);
    }
}
