
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {

    public static void main(String[] args) {
        try {
            GameLogicImpl gameLogic = new GameLogicImpl();
            GameLogic stub = (GameLogic) UnicastRemoteObject.exportObject(gameLogic, 0);

            // Bind the remote object stub to the registry
            Registry registry = LocateRegistry.createRegistry(8080);
            registry.bind("GameLogic", stub);
            System.out.println("Server is ready!");

        } catch (RemoteException | AlreadyBoundException ex) {
            ex.printStackTrace();
        }

    }
}
