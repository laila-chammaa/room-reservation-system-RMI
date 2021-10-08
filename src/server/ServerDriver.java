package server;

public class ServerDriver {
    public static void main(String[] args) {
        try {
            CampusServer DVLCampus = new CampusServer("DVL", 1099);
            CampusServer KKLCampus = new CampusServer("KKL", 1099);
            CampusServer WSTCampus = new CampusServer("WST", 1099);
        } catch (Exception e) {
            System.err.println("Server Driver Log: Error: Server initialization failure.");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
