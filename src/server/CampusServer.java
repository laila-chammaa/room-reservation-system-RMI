package server;

import model.Booking;
import model.CampusID;

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
import java.time.ZoneId;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class CampusServer extends UnicastRemoteObject implements ServerInterface {
    private static final long serialVersionUID = 1L;
    private static final int MAX_NUM_BOOKING = 3;
    private static int UDPPort;

    //Variables for RMI Registry
    private static final int REGISTRY_PORT = 1199;
    private Registry registry;

    //Variable for each separate bank server
    private CampusID campusID;

    private static ArrayList<HashMap<String, Integer>> stuBkngCntMap;

    private HashMap<String, Map.Entry<LocalDate, Integer>> roomRecords;
    private HashMap<String, List<Booking>> bookingRecords;

    private Logger logger;

    public CampusServer(String campusID, int UDPPort) throws RemoteException, AlreadyBoundException {
        super();
        this.campusID = CampusID.valueOf(campusID);
        CampusServer.UDPPort = UDPPort;

        this.roomRecords = new HashMap<>();
        this.bookingRecords = new HashMap<>();
        //TODO: is this necessary?
        stuBkngCntMap = new ArrayList<>(55);
        for (int i = 0; i < 55; i++)
            stuBkngCntMap.add(new HashMap<>());

        initiateLogger();
        initializeServer();

        this.logger.info("Server: " + campusID + " initialization success.");
        this.logger.info("Server: " + campusID + " port is : " + UDPPort);
    }

    private void initializeServer() throws RemoteException, AlreadyBoundException {
        this.logger.info("Initializing Server ...");
        // Bind the local server to the RMI Registry
        startRegistry();

        // TODO: remove this line?
        //ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, UDPPort);
        registry.bind(this.campusID.toString(), this);
    }

    // This method starts a RMI registry on the local host, if it
    // does not already exists at the specified port number.
    private void startRegistry() throws RemoteException {
        try {
            registry = LocateRegistry.getRegistry(CampusServer.REGISTRY_PORT);
            registry.list();
        } catch (RemoteException e) {
            registry = LocateRegistry.createRegistry(CampusServer.REGISTRY_PORT);
            this.logger.info("RMI registry created at port " + CampusServer.REGISTRY_PORT);
        }
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
    public String createRoom(int roomNumber, LocalDate date, ArrayList<Map.Entry<Long, Long>> listOfTimeSlots)
            throws RemoteException {
        this.logger.info("Attempting to create a room record.");
        //TODO: null checks
        Optional<Map.Entry<String, Map.Entry<LocalDate, Integer>>> record = roomRecords.entrySet().stream()
                .filter(h -> h.getValue().getKey().equals(date) && h.getValue().getValue() == roomNumber).findFirst();
        if (record.isPresent()) {
            String recordID = record.get().getKey();
            List<Booking> newBookings = new ArrayList<>();
            for (Map.Entry<Long,Long> slot : listOfTimeSlots) {
                newBookings.add(new Booking(recordID, null, slot));
            }
            bookingRecords.put(recordID, newBookings);
        } else {
            //TODO: generate new recordID
            String recordID = "1";
            roomRecords.put(recordID, new AbstractMap.SimpleEntry<>(date, roomNumber));
            List<Booking> newBookings = new ArrayList<>();
            for (Map.Entry<Long,Long> slot : listOfTimeSlots) {
                newBookings.add(new Booking(recordID, null, slot));
            }
            bookingRecords.put(recordID, newBookings);
        }
        return "wow";
    }

    @Override
    public String deleteRoom(int roomNumber, LocalDate date, ArrayList<Map.Entry<Long, Long>> listOfTimeSlots) throws RemoteException {
        //TODO: null checks
        Optional<Map.Entry<String, Map.Entry<LocalDate, Integer>>> record = roomRecords.entrySet().stream()
                .filter(h -> h.getValue().getKey().equals(date) && h.getValue().getValue() == roomNumber).findFirst();
        if (record.isPresent()) {
            String recordID = record.get().getKey();
            List<Booking> removedBookings = bookingRecords.get(recordID)
                    .stream().filter(b -> listOfTimeSlots.contains(b.getTimeslot())).collect(Collectors.toList());
            bookingRecords.get(recordID).removeIf(removedBookings::contains);
            for (Booking removedBooking : removedBookings) {
                if (removedBooking.getBookedBy() != null) {
                    //booked by the student, reducing the student's bookingCount
                    setStuBookingCnt(removedBooking.getBookedBy(), date, -1);
                }
            }
        } else {
            //log
        }
        return null;
    }

    @Override
    public String bookRoom(String studentID, CampusID campusID, int roomNumber, LocalDate date, Map.Entry<Long, Long> timeslot)
            throws RemoteException {
        //TODO: use campusID passed to book a room in another server?
        //TODO: null check
        if (getStuBookingCnt(studentID, date) >= MAX_NUM_BOOKING) {
            //TODO: not allowed any more, log
        }
        Optional<Map.Entry<String, Map.Entry<LocalDate, Integer>>> record = roomRecords.entrySet().stream()
                .filter(h -> h.getValue().getKey().equals(date) && h.getValue().getValue() == roomNumber).findFirst();
        if (record.isPresent()) {
            String recordID = record.get().getKey();
            Optional<Booking> booking = bookingRecords.get(recordID)
                    .stream().filter(b -> b.getTimeslot().equals(timeslot)).findFirst();
            if (booking.isPresent()) {
                booking.get().setBookedBy(studentID);
                //TODO: set bookingID?
                setStuBookingCnt(studentID, date, 1);
            } else {
                //log
            }

        }
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
    public String cancelBooking(String studentID, String bookingID) throws RemoteException {
        List<Booking> bookingList = bookingRecords.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Optional<Booking> booking = bookingList.stream().filter(b -> b.getBookingID().equals(bookingID)).findFirst();
        if (booking.isPresent()) {
            booking.get().setBookedBy(null);
            booking.get().setBookingID(null);

        } else {
            //not found, log
        }
        return null;
    }

    @Override
    public int getLocalAvailableTimeSlot() throws RemoteException {
        List<Booking> bookingList = bookingRecords.values().stream().flatMap(List::stream).collect(Collectors.toList());
        List<Booking> nullBookings = bookingList.stream().filter(b -> b.getBookedBy() == null).collect(Collectors.toList());
        return nullBookings.size();
    }

    //This will create data-gram socket to connect to other servers.
    //Necessary in order to give total account count to other servers.
    @Override
    public synchronized void getUDPData(int portNum) throws RemoteException {
        DatagramSocket socket;
        try {
            byte[] message = ByteBuffer.allocate(4).putInt(getLocalAvailableTimeSlot()).array();
            InetAddress hostAddress = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(message, message.length, hostAddress, portNum);
            socket = new DatagramSocket(CampusServer.UDPPort);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            this.logger.severe("Server Log: | getUDPData Error: " + e.getMessage());
            throw new RemoteException(e.getMessage());
        }
    }

    public int getStuBookingCnt(String studentID, LocalDate localDate) {
        Calendar cal = Calendar.getInstance();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);

        HashMap<String, Integer> stuMap = stuBkngCntMap.get(week);
        Integer cnt = stuMap.get(studentID);
        if (cnt == null)
            return 0;
        else
            return cnt;
    }

    public void setStuBookingCnt(String studentID, LocalDate localDate, int offset) {
        Calendar cal = Calendar.getInstance();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);

        HashMap<String, Integer> stuMap = stuBkngCntMap.get(week);
        Integer cnt = stuMap.get(studentID);
        if (offset > 0) {
            stuMap.put(studentID, ++cnt);
        } else {
            stuMap.put(studentID, --cnt);
        }
    }
}
