package server;

public class ServerDriver {
    public static void main(String[] args) {
        try {
            CampusServer DVLCampus = new CampusServer("DVL", 8081);
            CampusServer KKLCampus = new CampusServer("KKL", 8082);
            CampusServer WSTCampus = new CampusServer("WST", 8083);
        } catch (Exception e) {
            System.err.println("Server Driver Log: Error: Server initialization failure.");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
