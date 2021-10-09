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
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class CampusServer extends UnicastRemoteObject implements ServerInterface {
    private static final long serialVersionUID = 1L;
    private static final int MAX_NUM_BOOKING = 3;
    private static final int USER_TYPE_POS = 3;
    private static int UDPPort;

    //Variables for RMI Registry
    private static final String REGISTRY_HOST = "localhost";
    private static final int REGISTRY_PORT = 1099;
    private Registry registry = LocateRegistry.getRegistry(REGISTRY_HOST, REGISTRY_PORT);


    //Variable for each separate bank server
    private CampusID campusID;

    private static ArrayList<HashMap<String, Integer>> stuBkngCntMap;

    private static int recordIdCount = 1;

    private HashMap<String, Map.Entry<LocalDate, Integer>> roomRecords;
    private HashMap<String, List<Booking>> bookingRecords;

    private Logger logger;

    public CampusServer(String campusID, int UDPPort) throws RemoteException, AlreadyBoundException {
        super();
        this.campusID = CampusID.valueOf(campusID);
        CampusServer.UDPPort = UDPPort;

        this.roomRecords = new HashMap<>();
        this.bookingRecords = new HashMap<>();

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

        //ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, UDPPort);
        registry.bind(this.campusID.toString(), this);
    }

    // This method starts a RMI registry on the local host, if it
    // does not already exists at the specified port number.
    private void startRegistry() throws RemoteException {
        try {
            registry = LocateRegistry.getRegistry(UDPPort);
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
    public String createRoom(String adminID, int roomNumber, LocalDate date,
                             ArrayList<Map.Entry<Long, Long>> listOfTimeSlots) throws RemoteException {
        String resultLog;
        resultLog = validateAdmin(adminID);
        if (resultLog != null) {
            return resultLog;
        }
        resultLog = validateTimeSlot(listOfTimeSlots);
        if (resultLog != null) {
            return resultLog;
        }

        this.logger.info(String.format("Server Log | Request: createRoom | AdminID: %s | Room number: %d | Date: %s",
                adminID, roomNumber, date.toString()));

        //TODO: null checks
        Optional<Map.Entry<String, Map.Entry<LocalDate, Integer>>> record = roomRecords.entrySet().stream()
                .filter(h -> h.getValue().getKey().equals(date) && h.getValue().getValue() == roomNumber).findFirst();
        if (record.isPresent()) {
            String recordID = record.get().getKey();
            resultLog = updateRecord(recordID, listOfTimeSlots);
        } else {
            resultLog = createRecord(roomNumber, date, listOfTimeSlots);
        }
        return resultLog;
    }

    private String createRecord(int roomNumber, LocalDate date, ArrayList<Map.Entry<Long, Long>> listOfTimeSlots) {
        String resultLog;
        String recordID = "RR" + String.format("%05d", recordIdCount);
        incrementRecordID();
        while (roomRecords.get(recordID) != null) {
            incrementRecordID();
            recordID = "RR" + recordIdCount;
        }
        roomRecords.put(recordID, new AbstractMap.SimpleEntry<>(date, roomNumber));
        List<Booking> newBookings = new ArrayList<>();
        for (Map.Entry<Long, Long> slot : listOfTimeSlots) {
            newBookings.add(new Booking(recordID, null, slot));
        }
        bookingRecords.put(recordID, newBookings);
        resultLog = String.format("Server Log | Room record %s was created successfully", recordID);
        this.logger.info(resultLog);
        return resultLog;
    }

    private String updateRecord(String recordID, ArrayList<Map.Entry<Long, Long>> listOfTimeSlots) {
        String resultLog;
        List<Booking> previousBookings = bookingRecords.get(recordID);
        List<Booking> newBookings = new ArrayList<>(previousBookings);
        for (Map.Entry<Long, Long> slot : listOfTimeSlots) {
            newBookings.add(new Booking(recordID, null, slot));
        }
        bookingRecords.put(recordID, newBookings);
        resultLog = String.format("Server Log | Room record %s was already created. " +
                "It was updated successfully", recordID);
        this.logger.info(resultLog);
        return resultLog;
    }

    @Override
    public String deleteRoom(String adminID, int roomNumber, LocalDate date,
                             ArrayList<Map.Entry<Long, Long>> listOfTimeSlots) {

        String resultLog;
        resultLog = validateAdmin(adminID);
        if (resultLog != null) {
            return resultLog;
        }
        resultLog = validateTimeSlot(listOfTimeSlots);
        if (resultLog != null) {
            return resultLog;
        }

        this.logger.info(String.format("Server Log | Request: deleteRoom | AdminID: %s | Room number: %d | Date: %s",
                adminID, roomNumber, date.toString()));

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
            resultLog = String.format("Server Log | Room record %s was deleted successfully", recordID);
            this.logger.info(resultLog);
        } else {
            resultLog = String.format("Server Log | ERROR: Room was not found | Request: deleteRoom | " +
                    "Room number: %d | Date: %s", roomNumber, date.toString());
            this.logger.warning(resultLog);
        }
        return resultLog;
    }

    @Override
    public String bookRoom(String studentID, CampusID campusID, int roomNumber, LocalDate date,
                           Map.Entry<Long, Long> timeslot) throws RemoteException {
        String resultLog;
        resultLog = validateStudent(studentID);
        if (resultLog != null) {
            return resultLog;
        }
        resultLog = validateTimeSlot(Collections.singletonList(timeslot));
        if (resultLog != null) {
            return resultLog;
        }

        //forward request to other server
        if (campusID != this.campusID) {
            ServerInterface otherServer;
            try {
                this.logger.info(String.format("Server Log | Forwarding Request to %s Server: bookRoom | StudentID: %s " +
                                "| Room number: %d | Date: %s | Timeslot: %s", campusID.toString(), studentID, roomNumber,
                        date.toString(), timeslot.toString()));
                otherServer = (ServerInterface) registry.lookup(campusID.toString());
                return otherServer.bookRoom(studentID, campusID, roomNumber, date, timeslot);
            } catch (NotBoundException e) {
                resultLog = "Server Log | Request: bookRoom | ERROR: " + campusID.toString() + " Not Bound.";
                this.logger.severe(resultLog);
                return resultLog;
            }
        }
        this.logger.info(String.format("Server Log | Request: bookRoom | StudentID: %s | " +
                "Room number: %d | Date: %s | Timeslot: %s", studentID, roomNumber, date.toString(), timeslot.toString()));

        if (getStuBookingCnt(studentID, date) >= MAX_NUM_BOOKING) {
            resultLog = String.format("Server Log | ERROR: Booking limit (%d) for the week was reached | " +
                    "StudentID %s", MAX_NUM_BOOKING, studentID);
            this.logger.warning(resultLog);
            return resultLog;
        }
        //TODO: null check
        Optional<Map.Entry<String, Map.Entry<LocalDate, Integer>>> record = roomRecords.entrySet().stream()
                .filter(h -> h.getValue().getKey().equals(date) && h.getValue().getValue() == roomNumber).findFirst();
        if (record.isPresent()) {
            String recordID = record.get().getKey();
            Optional<Booking> booking = bookingRecords.get(recordID)
                    .stream().filter(b -> b.getTimeslot().equals(timeslot)).findFirst();
            if (booking.isPresent() && booking.get().getBookedBy() == null) {
                booking.get().book(studentID);
                setStuBookingCnt(studentID, date, 1);
                String bookingID = booking.get().getBookingID();
                resultLog = String.format("Server Log | Room record %s was booked successfully. BookingID: %s",
                        recordID, bookingID);
                this.logger.info(resultLog);
            } else {
                resultLog = String.format("Server Log | ERROR: Time slot was not available | Request: bookRoom | " +
                        "Room number: %d | Date: %s | Timeslot: %s", roomNumber, date.toString(), timeslot.toString());
                this.logger.warning(resultLog);
            }

        } else {
            resultLog = String.format("Server Log | ERROR: Room was not found | Request: bookRoom | " +
                    "Room number: %d | Date: %s", roomNumber, date.toString());
            this.logger.warning(resultLog);
        }
        return resultLog;
    }

    @Override
    public String cancelBooking(String studentID, String bookingID) {

        String resultLog;
        resultLog = validateStudent(studentID);
        if (resultLog != null) {
            return resultLog;
        }

        this.logger.info(String.format("Server Log | Request: cancelBooking | StudentID: %s | " +
                "BookingID: %s", studentID, bookingID));
        List<Booking> bookingList = bookingRecords.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Optional<Booking> booking = bookingList.stream().filter(b -> b.getBookingID() != null &&
                b.getBookingID().equals(bookingID)).findFirst();
        if (booking.isPresent() && booking.get().getBookedBy().equals(studentID)) {
            booking.get().setBookedBy(null);
            booking.get().setBookingID(null);
            resultLog = String.format("Server Log | Booking %s was cancelled successfully.", bookingID);
            LocalDate date = roomRecords.get(booking.get().getRecordID()).getKey();
            setStuBookingCnt(studentID, date, -1);
            this.logger.info(resultLog);
        } else {
            resultLog = String.format("Server Log | ERROR: Booking was not found | Request: cancelBooking | BookingID: %s",
                    bookingID);
            this.logger.warning(resultLog);
        }
        return resultLog;
    }

    @Override
    public HashMap<CampusID, Integer> getAvailableTimeSlot(LocalDate date) throws RemoteException {
        this.logger.info(String.format("Server Log | Request: getAvailableTimeSlot | Date: %s", date.toString()));

        HashMap<CampusID, Integer> totalTimeSlotCount = new HashMap<>();
        int localTimeSlotCount = getLocalAvailableTimeSlot();
        totalTimeSlotCount.put(this.campusID, localTimeSlotCount);

        DatagramSocket socket;
        String resultLog;

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
                    this.logger.severe("Server Log | getAvailableTimeSlot() ERROR: " + campusServer + " Not Bound.");
                    throw new RemoteException(e.getMessage());
                }

                //3. For each server we will ask for their local total count.
                boolean recv = false;
                int rData = 0;

                while (!recv) {
                    otherServer.getUDPData(CampusServer.UDPPort);

                    byte[] buffer = new byte[1024];
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                    try {
                        socket.receive(request);
                    } catch (IOException e) {
                        this.logger.severe("Server Log | getAvailableTimeSlot() ERROR: IO Exception at receiving reply.");
                        throw new RemoteException(e.getMessage());
                    }
                    try {
                        rData = Integer.parseInt(new String(request.getData()));
                    } catch (NumberFormatException e) {
                        rData = 0;
                    }

                    if (request.getPort() > 9000) {
                        recv = true;
                    }
                }

                totalTimeSlotCount.put(CampusID.valueOf(campusServer), rData);
                resultLog = "Server Log | Getting the available timeslots was successful.";
                this.logger.info(resultLog);
            }
            socket.close();
        } catch (SocketException e) {
            this.logger.severe("Server Log | getAvailableTimeSlot() ERROR: " + e.getMessage());
            throw new RemoteException(e.getMessage());
        }

        return totalTimeSlotCount;
    }

    @Override
    public int getLocalAvailableTimeSlot() {
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
            socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            this.logger.severe("Server Log: | getUDPData Error: " + e.getMessage());
            throw new RemoteException(e.getMessage());
        }
    }

    private int getStuBookingCnt(String studentID, LocalDate localDate) {
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

    private void setStuBookingCnt(String studentID, LocalDate localDate, int offset) {
        Calendar cal = Calendar.getInstance();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);

        HashMap<String, Integer> stuMap = stuBkngCntMap.get(week);
        Integer cnt = stuMap.get(studentID);
        if (cnt == null) cnt = 0;
        if (offset > 0) {
            stuMap.put(studentID, ++cnt);
        } else {
            stuMap.put(studentID, --cnt);
        }
    }

    private synchronized static void incrementRecordID() {
        recordIdCount++;
    }

    private String validateAdmin(String userID) {
        char userType = userID.charAt(USER_TYPE_POS);
        if (userType != 'A') {
            return "Login Error: This request is for admins only.";
        }
        return null;
    }

    private String validateStudent(String userID) {
        char userType = userID.charAt(USER_TYPE_POS);
        if (userType != 'S') {
            return "Login Error: This request is for students only.";
        }
        return null;
    }

    private String validateTimeSlot(List<Map.Entry<Long, Long>> listOfTimeSlots) {
        for (Map.Entry<Long, Long> slot : listOfTimeSlots) {
            if (slot.getKey() < 0 || slot.getKey() >= 24 || slot.getValue() < 0 ||
                    slot.getValue() >= 24 || slot.getKey() >= slot.getValue()) {
                return "Invalid timeslot format. Use the 24h clock.";
            }
        }
        return null;
    }
}
