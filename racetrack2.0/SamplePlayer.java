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

        List<PathCell> map = RaceTrackGame.BFS(state.i, state.j, track);
        System.out.println("map: " + map);
        return STAY ;
    }


    private int f(PathCell cell) {
        return gValues.getOrDefault(cell, 0) + hValues.getOrDefault(cell, 0);
    }

    @Override
    public Direction getDirection(long timeBudget) {
        PathCell startCell = new PathCell(state.i, state.j, null);
        PriorityQueue<PathCell> openCells = new PriorityQueue<>((cell1, cell2) -> f(cell1) - f(cell2));
        Set<PathCell> closedCells = new HashSet<>();
        openCells.add(startCell);
        gValues.put(startCell, 0);
        hValues.put(startCell, calcHeuristic(state.i, state.j));

        Map<PathCell, PathCell> cameFrom = new HashMap<>();
        PathCell currentCell;

        while (!openCells.isEmpty()) {
            currentCell = openCells.poll();

            if (isGoal(currentCell)) {
                return extractDirection(cameFrom, currentCell);
            }

            if (closedCells.contains(currentCell)) {
                continue;
            }
            closedCells.add(currentCell);

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
                        cameFrom.put(neighbor, currentCell);
                    }
                }
            }
        }
        return STAY;
    }

    private Direction extractDirection(Map<PathCell, PathCell> cameFrom, PathCell goalCell) {
        PathCell current = goalCell;
        PathCell parent = cameFrom.get(current);

        if (parent == null) {
            return STAY; // Ha nincs szülő, akkor maradunk a helyünkön
        }

        // Visszafelé haladunk az úton, hogy megtaláljuk az első lépést
        while (parent != null && parent.parent != null) {
            current = parent;
            parent = cameFrom.get(current);
        }

        // Meghatározzuk az irányt az első lépés alapján
        if (current.i > state.i) return DOWN;
        if (current.i < state.i) return UP;
        if (current.j > state.j) return RIGHT;
        if (current.j < state.j) return LEFT;

        return STAY;
    }


}
