import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
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

    private boolean canMoveTo(int i, int j) {
        return i >= 0 && i < track.length && j >= 0 && j < track[0].length && (track[i][j] & RaceTrackGame.WALL) == 0;
    }

    private int calcHeuristic(int i, int j) {
        return Math.abs(i - goalPosition[0]) + Math.abs(j - goalPosition[1]);
    }

    private Direction getBestDirection(PathCell goal) {
        if (goal == null) return STAY;
        PathCell current = goal;
        Direction bestDirection = STAY;

        while (current.parent != null) {
            bestDirection = new Direction(current.i - current.parent.i, current.j - current.parent.j);
            current = current.parent;
        }

        return bestDirection;
    }

    private Direction findPathToGoal() {
        PriorityQueue<PathCell> openCells = new PriorityQueue<>(Comparator.comparingInt(this::f));
        Set<PathCell> closedCells = new HashSet<>();
        PathCell startCell = new PathCell(state.i, state.j, null);
        openCells.add(startCell);
        gValues.put(startCell, 0);
        hValues.put(startCell, calcHeuristic(state.i, state.j));

        while (!openCells.isEmpty()) {
            PathCell currentCell = openCells.poll();

            if (currentCell.i == goalPosition[0] && currentCell.j == goalPosition[1]) {
                return getBestDirection(currentCell);
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
                        gValues.put(neighbor, gValues.getOrDefault(currentCell, 0) + 1);
                        hValues.put(neighbor, calcHeuristic(nextRow, nextColumn));
                        openCells.add(neighbor);
                    }
                }
            }
            closedCells.add(currentCell);
        }

        return STAY;
    }

    @Override
    public Direction getDirection(long timeBudget) {
        return findPathToGoal();
    }

    private int f(PathCell cell) {
        return gValues.getOrDefault(cell, 0) + hValues.getOrDefault(cell, 0);
    }
}
