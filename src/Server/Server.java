package Server;

import com.sun.tools.javac.util.Pair;
import model.Campus;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public interface Server extends Remote {

    //ADMIN ONLY
    String createRoom(int roomNumber, LocalDate date, ArrayList<Pair<LocalDate, LocalDate>> listOfTimeSlots)
            throws RemoteException;

    String deleteRoom(int roomNumber, LocalDate date, ArrayList<Pair<LocalDate, LocalDate>> listOfTimeSlots)
            throws RemoteException;

    //STUDENT ONLY
    String bookRoom(Campus campusName, int roomNumber, LocalDate date, Pair<LocalDate, LocalDate> listOfTimeSlots)
            throws RemoteException;

    HashMap<Campus, Integer> getAvailableTimeSlot(LocalDate date) throws RemoteException;

    String cancelBooking(String bookingID) throws RemoteException;

}

/*
 */
