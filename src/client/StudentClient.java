package client;

import com.sun.tools.javac.util.Pair;
import model.CampusID;
import server.ServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

public class StudentClient {

    //RMI Variables
    private static final String CAMPUS_HOST = "localhost";
    private static final int CAMPUS_PORT = 1199;
    private Registry registry;
    private String studentID;
    private CampusID campusID;

    private static final int USER_TYPE_POS = 3;
    private static final int CAMPUS_NAME_POS = 3;

    public StudentClient(String userID) throws RemoteException, NotBoundException {
        this.studentID = userID;

        validateStudent(userID);

        registry = LocateRegistry.getRegistry(CAMPUS_HOST, CAMPUS_PORT);

        System.out.println("Login Sucessed. | Student ID: " +
                this.studentID + " | Campus ID: " + this.campusID.toString());
    }

    private void validateStudent(String userID) throws RemoteException {
        char userType = userID.charAt(USER_TYPE_POS);
        String campusName = userID.substring(0, CAMPUS_NAME_POS);

        if (userType != 'A') {
            throw new RemoteException("Login Error: This client is for admins only.");
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
