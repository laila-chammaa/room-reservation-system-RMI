package server;

import com.sun.tools.javac.util.Pair;
import model.CampusID;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public interface ServerInterface extends Remote {

    //ADMIN ONLY
    String createRoom(int roomNumber, LocalDate date, ArrayList<Pair<Long, Long>> listOfTimeSlots)
            throws RemoteException;

    String deleteRoom(int roomNumber, LocalDate date, ArrayList<Pair<Long, Long>> listOfTimeSlots)
            throws RemoteException;

    //STUDENT ONLY
    String bookRoom(CampusID campusID, int roomNumber, LocalDate date, Pair<Long, Long> timeSlot)
            throws RemoteException;

    HashMap<CampusID, Integer> getAvailableTimeSlot(LocalDate date) throws RemoteException;

    String cancelBooking(String bookingID) throws RemoteException;

    //Misc
    int getLocalAvailableTimeSlot();
    void getUDPData(int udpPort) throws RemoteException;
}
