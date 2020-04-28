import javafx.util.Pair;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class GameLogicImpl implements GameLogic {

    private static final String BLUE_TAG = "BLUE";
    private static final int BLUE_TAG_ID = 1;
    private static final int BLUE_KILLED_RED = 12;
    private static int blueMoveCount = 0;

    private static final String RED_TAG = "RED";
    private static final int RED_TAG_ID = 2;
    private static final int RED_KILLED_BLUE = 21;
    private static int redMoveCount = 0;

    private static final int FIELD_SIZE_Y = 10;
    private static final int FIELD_SIZE_X = 15;
    private static int countOfPlayers = 0;
    private static String winnerId = null;

    private final Object obj = new Object();
    private static boolean checkBlueInitialMove = false;
    private static List<List<Integer>> gameField = new ArrayList<>(FIELD_SIZE_Y);

    public GameLogicImpl() {
        for (int i = 0; i < FIELD_SIZE_Y; i++) {
            List<Integer> tmp = new ArrayList<>();
            for (int j = 0; j < FIELD_SIZE_X; j++) {
                tmp.add(0);
            }
            gameField.add(tmp);
        }
    }


    @Override
    public String getId() throws RemoteException {
        String result;
        switch (countOfPlayers++) {
            case 0:
                result = BLUE_TAG;
                break;
            case 1:
                result = RED_TAG;
                break;
            default:
                return null;
        }
        System.out.printf("Player %s connected to the game! \n", result);
        if (result.equals(BLUE_TAG)) {
            while (countOfPlayers != 2) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        } else if (result.equals(RED_TAG)) {
            while (checkBlueInitialMove == false) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        System.out.println("The game started!");
        return result;
    }

    @Override
    public List getField() throws RemoteException {
        return gameField;
    }

    @Override
    public boolean makeMove(Pair<Integer, Integer> point, String playerId) throws RemoteException {
        synchronized (obj) {
            int x = point.getKey();
            int y = point.getValue();
            // Если выходим за границы игрового поля
            if (x < 0 || x > 14 || y < 0 || y > 14) {
                return false;
            }

            Integer cellValue = gameField.get(y).get(x);

            if (!checkNeighbours(x, y, playerId)) {
                return false;
            }
            if (playerId.equals(BLUE_TAG)) {
                // Если клетка занята крестиком
                if (cellValue == BLUE_TAG_ID) {
                    return false;
                } else if (cellValue == 0) {
                    // Ход в доступную клетку
                    gameField.get(y).set(x, BLUE_TAG_ID);
                } else if (wantToKill(x, y, playerId)) {
                    // Убийство нолика
                    gameField.get(y).set(x, BLUE_KILLED_RED);
                    System.out.println("BLUE KILLED RED");
                }
                System.out.println("Player " + playerId + " made his move on point" + "(" + x + "," + y + ")");
                blueMoveCount++;
                if (blueMoveCount == 3) {
                    checkBlueInitialMove = true;
                }
                winnerId = checkVictory(playerId) ? playerId : null;

            } else if (playerId.equals(RED_TAG)) {
                if (cellValue == RED_TAG_ID) {
                    return false;
                } else if (cellValue == 0) {
                    // Ход в доступную клетку
                    gameField.get(y).set(x, RED_TAG_ID);
                } else if (wantToKill(x, y, playerId)) {
                    // Убийство крестика
                    gameField.get(y).set(x, RED_KILLED_BLUE);
                    System.out.println("RED KILLED BLUE!");
                }
                System.out.println("Player " + playerId + " made his move on point" + "(" + x + "," + y + ")");
                redMoveCount++;
                winnerId = checkVictory(playerId) ? playerId : null;
            }
            return true;
        }
    }

    @Override
    public List<List<Integer>> waitForOpponent(String playerId) throws RemoteException {
        while ((!playerId.equals(whoseMove()) && winnerId == null)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return gameField;
    }

    @Override
    public String whoWon(String playerId) throws RemoteException {
        if (winnerId != null) {
            System.out.println("Player " + playerId + " won!");
        }
        return winnerId;
    }

    private boolean checkNeighbours(int x, int y, String playerId) {
        // Нам не нужно проверять соседей для первых ходов
        if (playerId.equals(BLUE_TAG) && blueMoveCount == 0) {
            return true;
        }
        if (playerId.equals(RED_TAG) && redMoveCount == 0) {
            return true;
        }

        int right = x + 1;
        int left = x - 1;
        int top = y - 1;
        int bottom = y + 1;

        if (right == 10) {
            right--;
        }
        if (left == -1) {
            left++;
        }
        if (top == -1) {
            top++;
        }
        if (bottom == 10) {
            bottom--;
        }

        for (int i = left; i <= right; i++) {
            for (int j = top; j <= bottom; j++) {
                if (playerId.equals(BLUE_TAG)) {
                    if (i != x || j != y) {
                        // System.out.println("Value [" + i + "][" + j + "] = " + gameField.get(i).get(j));
                        if (gameField.get(j).get(i) == BLUE_TAG_ID || gameField.get(j).get(i) == BLUE_KILLED_RED) {
                            return true;
                        }
                    }
                } else {
                    if (i != x || j != y) {
                        if (gameField.get(j).get(i) == RED_TAG_ID || gameField.get(j).get(i) == RED_KILLED_BLUE) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean wantToKill(int x, int y, String playerId) {
        int right = x + 1;
        int left = x - 1;
        int top = y - 1;
        int bottom = y + 1;
        for (int i = left; i <= right; i++) {
            for (int j = top; j <= bottom; j++) {
                if (playerId.equals(BLUE_TAG)) {
                    if (i != x || j != y) {
                        if (gameField.get(j).get(i) == RED_TAG_ID) {
                            return true;
                        }
                    }
                } else {
                    if (i != x || j != y) {
                        if (gameField.get(j).get(i) == BLUE_TAG_ID) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean checkVictory(String playerId) {
        if (playerId.equals(BLUE_TAG) && blueMoveCount / 3 == 1) {
            return false;
        }
        for (int i = 0; i < getFieldSizeY(); i++) {
            for (int j = 0; j < getFieldSizeX(); j++) {
                if (playerId.equals(BLUE_TAG)) {
                    if (gameField.get(i).get(j) == RED_TAG_ID) {
                        return false;
                    }
                } else {
                    if (gameField.get(i).get(j) == BLUE_TAG_ID) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private String whoseMove() {
        if ((redMoveCount % 3 == 0 || blueMoveCount % 3 == 0) && (blueMoveCount / 3 > redMoveCount / 3)) {
            return RED_TAG;
        } else {
            return BLUE_TAG;
        }

    }

    public static int getFieldSizeX() {
        return FIELD_SIZE_X;
    }

    public static int getFieldSizeY() {
        return FIELD_SIZE_Y;
    }
}