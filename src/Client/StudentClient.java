package Client;

import Server.Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static model.Campus.DVL;

public class StudentClient {

    private StudentClient() {
    }

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host, 8080);
            Server stub = (Server) registry.lookup(DVL.name());
            String response = stub.cancelBooking("ID");
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
