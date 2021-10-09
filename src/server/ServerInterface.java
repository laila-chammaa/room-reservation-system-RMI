package server;

import model.CampusID;

import java.net.DatagramSocket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface ServerInterface extends Remote {

    //ADMIN ONLY
    String createRoom(String adminID, int roomNumber, LocalDate date, ArrayList<Map.Entry<Long, Long>> listOfTimeSlots)
            throws RemoteException;

    String deleteRoom(String adminID, int roomNumber, LocalDate date, ArrayList<Map.Entry<Long, Long>> listOfTimeSlots)
            throws RemoteException;

    //STUDENT ONLY
    String bookRoom(String studentID, CampusID campusID, int roomNumber, LocalDate date, Map.Entry<Long, Long> timeSlot)
            throws RemoteException;

    HashMap<CampusID, Integer> getAvailableTimeSlot(LocalDate date) throws RemoteException;

    String cancelBooking(String studentID, String bookingID) throws RemoteException;

    //Misc
    int getLocalAvailableTimeSlot() throws RemoteException;
    void getUDPData(int udpPort) throws RemoteException;
}
