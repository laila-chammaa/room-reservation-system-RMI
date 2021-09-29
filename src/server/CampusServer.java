package server;

import com.sun.tools.javac.util.Pair;
import model.CampusID;
import model.RoomRecord;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CampusServer implements ServerInterface {
    private static final long serialVersionUID = 1L;
    private static int UDPPort;
    private static final int CLIENT_NAME_INI_POS = 3;

    //Variables for RMI Registry
    private static final String BANK_HOST = "localhost";
    private static final int BANK_PORT = 1199;
    private Registry registry;

    //Variable for each separate bank server
    private CampusID campusID;
    private HashMap<LocalDate, HashMap<Integer, Pair<Long, Long>>> hashmapDB;
    //private Map<String, ArrayList<Client>> clientList = new HashMap<String, ArrayList<Client>>();
    private Logger logger;

    public CampusServer(String campusID, int UDPPort) throws RemoteException, AlreadyBoundException {
        super();
        this.campusID = CampusID.valueOf(campusID);
        CampusServer.UDPPort = UDPPort;

        initiateLogger();
        initializeServer(UDPPort);

        this.logger.info("Server: " + campusID + " initialization success.");
        this.logger.info("Server: " + campusID + " port is : " + UDPPort);
    }

    private void initializeServer(int UDPPort) throws RemoteException, AlreadyBoundException {
        this.logger.info("Initializing Server ...");
        // Bind the local server to the RMI Registry
        registry = LocateRegistry.createRegistry(UDPPort);
        // TODO: remove this line?
        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, UDPPort);
        registry.bind(this.campusID.toString(), stub);
    }

    private void initiateLogger() {
        Logger logger = Logger.getLogger("Server Logs/" + this.campusID + "- Server Log");
        FileHandler fh;

        try {
            //FileHandler Configuration and Format Configuration
            fh = new FileHandler("Server Logs/" + this.campusID + " - Server Log.log");

            //Disable console handling
            //logger.setUseParentHandlers(false);
            logger.addHandler(fh);

            //Formatting configuration
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            System.err.println("Server Log: Error: Security Exception " + e);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Server Log: Error: IO Exception " + e);
            e.printStackTrace();
        }

        System.out.println("Server Log: Logger initialization success.");

        this.logger = logger;
    }

    @Override
    public String createRoom(int roomNumber, LocalDate date, ArrayList<Pair<Long, Long>> listOfTimeSlots) throws RemoteException {
        RoomRecord roomRecord = new RoomRecord(roomNumber, date, listOfTimeSlots);
        //CHECK IF ITS AN ADMIN
        return "wow";
    }

    @Override
    public String deleteRoom(int roomNumber, LocalDate date, ArrayList<Pair<Long, Long>> listOfTimeSlots) throws RemoteException {
        return null;
    }

    @Override
    public String bookRoom(CampusID campusID, int roomNumber, LocalDate date, Pair<Long, Long> timeslot)
            throws RemoteException {
        //TODO: validate timeslot
        return null;
    }

    @Override
    public HashMap<CampusID, Integer> getAvailableTimeSlot(LocalDate date) throws RemoteException {
        return null;
    }

    @Override
    public String cancelBooking(String bookingID) throws RemoteException {
        return "wowww";
    }
}
