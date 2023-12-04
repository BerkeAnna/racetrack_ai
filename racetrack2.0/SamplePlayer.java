import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
import game.racetrack.utils.Cell;
import game.racetrack.utils.PathCell;
import java.util.*;

public class SamplePlayer extends RaceTrackPlayer {
    private int[][] track;
    private int[] goalPosition;
    private static final int SPEED = 1;
    private Coin[] coins;
    private Set<Coin> collectedCoins = new HashSet<>();
    private Map<PathCell, Integer> gValues = new HashMap<>();
    private Map<PathCell, Integer> hValues = new HashMap<>();

    private Direction DOWN = RaceTrackGame.DIRECTIONS[7];
    private Direction LEFT = RaceTrackGame.DIRECTIONS[1];
    private Direction RIGHT = RaceTrackGame.DIRECTIONS[5];
    private Direction UP = RaceTrackGame.DIRECTIONS[3];
    private Direction STAY = RaceTrackGame.DIRECTIONS[0];

    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;
        this.goalPosition = findGoalPosition();
        this.coins = coins;
    }

    private boolean isCoinWithinDistance(Coin coin, int distance) {
        return Math.abs(coin.i - state.i) + Math.abs(coin.j - state.j) <= distance;
    }

    private boolean canMoveTo(int i, int j) {
        if (i < 0 || i >= track.length || j < 0 || j >= track[0].length) {
            return false;
        }
        return (track[i][j] & RaceTrackGame.WALL) == 0;
    }

    private int[] findGoalPosition() {
        for (int i = 0; i < track.length; i++) {
            for (int j = 0; j < track[i].length; j++) {
                if ((track[i][j] & RaceTrackGame.FINISH) == RaceTrackGame.FINISH) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private boolean isGoal(PathCell cell) {
        return cell.i == goalPosition[0] && cell.j == goalPosition[1];
    }

    private int calcHeuristic(int i, int j) {
        return Math.abs(i - goalPosition[0]) + Math.abs(j - goalPosition[1]);
    }

    private Direction reconstructRoute(PathCell goal) {
        LinkedList<Direction> route = new LinkedList<>();
        PathCell current = goal;
        while (current.parent != null) {
            route.addFirst(new Direction(current.i - current.parent.i, current.j - current.parent.j));
            current = current.parent;
        }
        return route.isEmpty() ? STAY : route.getFirst();
    }

    private Coin findNearestCoin() {
        Coin nearest = null;
        int minDistance = Integer.MAX_VALUE;
        for (Coin coin : coins) {
            if (!collectedCoins.contains(coin)) {
                int distance = Math.abs(coin.i - state.i) + Math.abs(coin.j - state.j);
                if (distance < minDistance) {
                    nearest = coin;
                    minDistance = distance;
                }
            }
        }
        return nearest;
    }

    private Direction computePathToCoin(PathCell startCell, Coin coin) {
        PriorityQueue<PathCell> openCells = new PriorityQueue<>((cell1, cell2) -> f(cell1) - f(cell2));
        Set<PathCell> closedCells = new HashSet<>();
        openCells.add(startCell);
        gValues.put(startCell, 0);
        hValues.put(startCell, calcHeuristicToCoin(startCell, coin));

        while (!openCells.isEmpty()) {
            PathCell currentCell = openCells.poll();
            if (closedCells.contains(currentCell)) {
                continue;
            }
            closedCells.add(currentCell);

            if (currentCell.i == coin.i && currentCell.j == coin.j) {
                collectedCoins.add(coin);
                return reconstructRoute(currentCell);
            }

            for (int vi = -SPEED; vi <= SPEED; vi++) {
                for (int vj = -SPEED; vj <= SPEED; vj++) {
                    int nextRow = currentCell.i + vi;
                    int nextColumn = currentCell.j + vj;

                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }

                    PathCell neighbor = new PathCell(nextRow, nextColumn, currentCell);
                    if (!closedCells.contains(neighbor) && !openCells.contains(neighbor)) {
                        gValues.put(neighbor, gValues.get(currentCell) + 1);
                        hValues.put(neighbor, calcHeuristicToCoin(neighbor, coin));
                        openCells.add(neighbor);
                    }
                }
            }
        }
        return STAY;
    }

    private int calcHeuristicToCoin(PathCell cell, Coin nearestCoin) {
        if (nearestCoin != null) {
            return Math.abs(cell.i - nearestCoin.i) + Math.abs(cell.j - nearestCoin.j);
        }
        return Integer.MAX_VALUE;
    }

    private int f(PathCell cell) {
        return gValues.getOrDefault(cell, 0) + hValues.getOrDefault(cell, 0);
    }

    @Override
    public Direction getDirection(long timeBudget) {
        PathCell startCell = new PathCell(state.i, state.j, null);
        gValues.put(startCell, 0);
        hValues.put(startCell, calcHeuristic(state.i, state.j));
        Coin nearestCoin = findNearestCoin();

        if (nearestCoin != null && isCoinWithinDistance(nearestCoin, 5) && !collectedCoins.contains(nearestCoin)) {
            return computePathToCoin(startCell, nearestCoin);
        }

        PriorityQueue<PathCell> openCells = new PriorityQueue<>((cell1, cell2) -> f(cell1) - f(cell2));
        Set<PathCell> closedCells = new HashSet<>();
        openCells.add(startCell);

        while (!openCells.isEmpty()) {
            PathCell currentCell = openCells.poll();
            if (closedCells.contains(currentCell)) {
                continue;
            }
            closedCells.add(currentCell);

            if (isGoal(currentCell)) {
                return reconstructRoute(currentCell);
            }

            for (int vi = -SPEED; vi <= SPEED; vi++) {
                for (int vj = -SPEED; vj <= SPEED; vj++) {
                    int nextRow = currentCell.i + vi;
                    int nextColumn = currentCell.j + vj;

                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }

                    PathCell neighbor = new PathCell(nextRow, nextColumn, currentCell);
                    if (!closedCells.contains(neighbor)) {
                        gValues.put(neighbor, gValues.get(currentCell) + 1);
                        hValues.put(neighbor, calcHeuristic(nextRow, nextColumn));
                        openCells.add(neighbor);
                    }
                }
            }
        }
        return STAY;
    }
}
