import javafx.util.Pair;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final String BLUE_TAG = "BLUE";
    private static final String RED_TAG = "RED";
    private String id;
    private GameLogic gameLogic;
    private static List<List<Integer>> gameField;

    private Client(String id, GameLogic gameLogic) throws RemoteException {
        Scanner sc = new Scanner(System.in);
        this.id = id;
        this.gameLogic = gameLogic;
        System.out.println("I am player " + id);
        if (id.equals(BLUE_TAG)) {
            int subMoveCount = 1;
            System.out.println("Initial move of Blues! SubMove " + subMoveCount++ + " at (0, 0).");
            gameLogic.makeMove(createPoint(0, 0), id);
            gameField = gameLogic.getField();
            displayGameField(gameField);
            while (subMoveCount <= 3) {
                int x, y;
                System.out.print("Make your " + subMoveCount++ + " subMove! x: ");
                x = sc.nextInt();
                System.out.print("y: ");
                y = sc.nextInt();
                while (!gameLogic.makeMove(createPoint(x, y), id)) {
                    System.out.println("You made an invalid move, please enter another x: ");
                    x = sc.nextInt();
                    System.out.println("Another y: ");
                    y = sc.nextInt();
                }
                gameField = gameLogic.getField();
                displayGameField(gameField);
            }
        } else if (id.equals(RED_TAG)) {
            int subMoveCount = 1;
            System.out.println("Initial move of Reds! SubMove " + subMoveCount++ + " at (14, 9).");
            System.out.println("SSSS");
            gameLogic.makeMove(createPoint(14, 9), id);
            gameField = gameLogic.getField();
            displayGameField(gameField);
            while (subMoveCount <= 3) {
                int x, y;
                System.out.print("Make your " + subMoveCount++ + " subMove! x: ");
                x = sc.nextInt();
                System.out.print("y: ");
                y = sc.nextInt();
                while (!gameLogic.makeMove(createPoint(x, y), id)) {
                    System.out.println("You made an invalid move, please enter another x: ");
                    x = sc.nextInt();
                    System.out.println("Another y: ");
                    y = sc.nextInt();
                }
                gameField = gameLogic.getField();
                displayGameField(gameField);
            }
        }
        while (gameLogic.whoWon(id) == null) {
            System.out.println("Waiting for another player! ");
            gameField = gameLogic.waitForOpponent(id);
            System.out.println("Move of " + id + " player!");
            if (gameLogic.whoWon(id) != null) {
                System.out.println("Player " + gameLogic.whoWon(id) + " won!");
                return;
            }
            displayGameField(gameField);
            System.out.println("Other player made his turn, make your own!");
            int subMoveCount = 1;
            while (subMoveCount <= 3) {
                int x, y;
                System.out.print("Make your " + subMoveCount++ + " subMove! x: ");
                x = sc.nextInt();
                System.out.print("y: ");
                y = sc.nextInt();
                while (!gameLogic.makeMove(createPoint(x, y), id)) {
                    System.out.println("You made an invalid move, please enter another x: ");
                    x = sc.nextInt();
                    System.out.println("Another y: ");
                    y = sc.nextInt();
                }
                if (gameLogic.whoWon(id) != null) {
                    System.out.println("Player " + gameLogic.whoWon(id) + " won!");
                    return;
                }
                gameField = gameLogic.getField();
                displayGameField(gameField);
            }
        }

    }
    private static void displayGameField(List<List<Integer>> gameField) {
        System.out.println("   0 1 2 3 4 5 6 7 8 9 A B C D E   ");
        System.out.println("   - - - - - - - - - - - - - - -   ");
        for(int i = 0; i < GameLogicImpl.getFieldSizeY(); i++)
        {
            System.out.print((i));
            System.out.print("|");
            System.out.print(' ');
            for (int j = 0; j < GameLogicImpl.getFieldSizeX(); j++)
            {
                System.out.print(gameField.get(i).get(j) + " ");
            }
            System.out.println();
        }
    }

    private static Pair<Integer, Integer> createPoint(int x, int y) {
        return new Pair<>(x, y);
    }


    public static void main(String[] args) {
        int host = 8080;
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            GameLogic stub = (GameLogic) registry.lookup("GameLogic");
            System.out.println(stub);
            Client client = new Client(stub.getId(), stub);

        } catch (RemoteException | NotBoundException ex) {
            ex.printStackTrace();
        }

    }


}