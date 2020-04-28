import javafx.util.Pair;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GameLogic extends Remote {
    String getId() throws RemoteException;
    List<List<Integer>> getField() throws RemoteException;
    boolean makeMove(Pair<Integer, Integer> point, String playerId) throws RemoteException;
    List<List<Integer>> waitForOpponent(String playerId) throws RemoteException;
    String whoWon(String playerId) throws RemoteException;
}
