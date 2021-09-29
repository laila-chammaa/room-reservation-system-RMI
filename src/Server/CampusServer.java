package Server;

import com.sun.tools.javac.util.Pair;
import model.Campus;
import model.RoomRecord;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import static model.Campus.*;

public class CampusServer implements Server {
    private Campus campus;
    private HashMap<LocalDate, HashMap<Integer, Pair<LocalDate, LocalDate>>> hashmapDB;

    public CampusServer(Campus campus) {
        this.campus = campus;
    }

    public static void main(String[] args) {

        try {
            CampusServer DVLCampus = new CampusServer(DVL);
            Server stub1 = (Server) UnicastRemoteObject.exportObject(DVLCampus, 8081);

            CampusServer KKLCampus = new CampusServer(KKL);
            Server stub2 = (Server) UnicastRemoteObject.exportObject(KKLCampus, 8082);

            CampusServer WSTCampus = new CampusServer(Campus.WST);
            Server stub3 = (Server) UnicastRemoteObject.exportObject(WSTCampus, 8083);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(8080);
            registry.bind(DVL.name(), stub1);
            registry.bind(KKL.name(), stub2);
            registry.bind(WST.name(), stub3);

            System.out.println("Server is running.");

            //you have 2 interfaces in the client & server?
        } catch (Exception e) {
            System.err.println("Server.Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public String createRoom(int roomNumber, LocalDate date, ArrayList<Pair<LocalDate, LocalDate>> listOfTimeSlots) throws RemoteException {
        RoomRecord roomRecord = new RoomRecord(roomNumber, date, listOfTimeSlots);
        //CHECK IF ITS AN ADMIN
        return "wow";
    }

    @Override
    public String deleteRoom(int roomNumber, LocalDate date, ArrayList<Pair<LocalDate, LocalDate>> listOfTimeSlots) throws RemoteException {
        return null;
    }

    @Override
    public String bookRoom(Campus campusName, int roomNumber, LocalDate date, Pair<LocalDate, LocalDate> listOfTimeSlots) throws RemoteException {
        return null;
    }

    @Override
    public HashMap<Campus, Integer> getAvailableTimeSlot(LocalDate date) throws RemoteException {
        return null;
    }

    @Override
    public String cancelBooking(String bookingID) throws RemoteException {
        return "wowww";
    }
}
