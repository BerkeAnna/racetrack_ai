import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.Cell;
import game.racetrack.utils.PathCell;
import game.racetrack.utils.PlayerState;
import java.util.*;

public class SamplePlayer extends RaceTrackPlayer {
    private int[][] track;
    private int[] goalPosition;
    private static final int SPEED = 1;

    private Direction DOWN = RaceTrackGame.DIRECTIONS[7];
    private Direction LEFT = RaceTrackGame.DIRECTIONS[1];
    private Direction RIGHT = RaceTrackGame.DIRECTIONS[5];
    private Direction UP = RaceTrackGame.DIRECTIONS[3];
    private Direction STAY = RaceTrackGame.DIRECTIONS[0];

    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;
        this.goalPosition = findGoalPosition();
    }

    private boolean canMoveTo(int i, int j) {
        if (i < 0 || i >= track.length || j < 0 || j >= track[0].length) {
            return false;
        }
        return (track[i][j] & RaceTrackGame.WALL) == 0;
    }

    /**
     *
     * @return
     */
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

    private boolean isGoal(Cell cell) {
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

    @Override
    public Direction getDirection(long timeBudget) {
        PathCell startCell = new PathCell(state.i, state.j, null);
        Map<PathCell, Integer> gValues = new HashMap<>();
        Map<PathCell, Integer> hValues = new HashMap<>();
        gValues.put(startCell, 0);
        hValues.put(startCell, calcHeuristic(startCell.i, startCell.j));

        PriorityQueue<PathCell> openCells = new PriorityQueue<>((cell1, cell2) -> gValues.get(cell1) + hValues.get(cell1) - gValues.get(cell2) - hValues.get(cell2));
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

            for (int vi = SPEED; vi >= -SPEED; vi--) {
                for (int vj = SPEED; vj >= -SPEED; vj--) {
                    int nextRow = currentCell.i + vi;
                    int nextColumn = currentCell.j + vj;

                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }

                    PathCell neighbor = new PathCell(nextRow, nextColumn, currentCell);
                    if (!closedCells.contains(neighbor) && !openCells.contains(neighbor)) {
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
