package server;

import com.sun.tools.javac.util.Pair;
import model.CampusID;
import model.RoomRecord;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
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
        HashMap<CampusID, Integer> totalTimeSlotCount = new HashMap<>();
        int localTimeSlotCount = getLocalAvailableTimeSlot();
        totalTimeSlotCount.put(this.campusID, localTimeSlotCount);

        DatagramSocket socket;

        //1. Create UDP Socket
        try {
            socket = new DatagramSocket(CampusServer.UDPPort);

            String[] campusServers = registry.list();

            //2. Get RMI Registry List of other servers.
            for (String campusServer : campusServers) {
                if (campusServer.equals(this.campusID.toString())) {
                    continue;
                }

                ServerInterface otherServer;
                try {
                    otherServer = (ServerInterface) registry.lookup(campusServer);
                } catch (NotBoundException e) {
                    this.logger.severe("Server Log: | getAvailableTimeSlot() Error: " + campusServer + " Not Bound.");
                    throw new RemoteException(e.getMessage());
                }

                //3. For each server we will ask for their local total count.
                boolean recv = false;
                int rData = 0; //TODO: might not be right

                while (!recv) {
                    otherServer.getUDPData(CampusServer.UDPPort);
                    byte[] buffer = new byte[1024];
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                    try {
                        socket.receive(request);
                    } catch (IOException e) {
                        this.logger.severe("Server Log: | getAvailableTimeSlot() Error: IO Exception at receiving reply.");
                        throw new RemoteException(e.getMessage());
                    }

                    rData = Integer.parseInt(new String(request.getData()));

                    if (request.getPort() > 9000) {
                        recv = true;
                    }
                }

                totalTimeSlotCount.put(CampusID.valueOf(campusServer), rData);
            }

            socket.close();
        } catch (SocketException e) {
            this.logger.severe("Server Log: | getAvailableTimeSlot() Error | " + e.getMessage());
            throw new RemoteException(e.getMessage());
        }

        return totalTimeSlotCount;

    }

    @Override
    public String cancelBooking(String bookingID) throws RemoteException {
        return "wowww";
    }

    @Override
    public int getLocalAvailableTimeSlot() {
        return 0;
    }

    @Override
    //This will create data-gram socket to connect to other servers.
    //Necessary in order to give total account count to other servers.
    public synchronized void getUDPData(int portNum) throws RemoteException {
        DatagramSocket dataSocket;

        try {
            dataSocket = new DatagramSocket();

            byte[] message = ByteBuffer.allocate(4).putInt(getLocalAvailableTimeSlot()).array();

            //Acquire local host
            InetAddress hostAddress = InetAddress.getByName("localhost");

            DatagramPacket request = new DatagramPacket(message, message.length, hostAddress, portNum);
            dataSocket.send(request);

            dataSocket.close();
        } catch (Exception e) {
            this.logger.severe("Server Log: | getUDPData Error: " + e.getMessage());
            throw new RemoteException(e.getMessage());
        }
    }

}
