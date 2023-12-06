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

    private List<PathCell> map = RaceTrackGame.BFS(state.i, state.j, track);

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

    //visszaadja a parentet is
    private PathCell findGoalCell() {
        for (int i = 0; i < track.length; i++) {
            for (int j = 0; j < track[i].length; j++) {
                if ((track[i][j] & RaceTrackGame.FINISH) == RaceTrackGame.FINISH) {
                    // Find an adjacent cell to use as the parent for the goal
                    for (int vi = -1; vi <= 1; vi++) {
                        for (int vj = -1; vj <= 1; vj++) {
                            if (vi == 0 && vj == 0) continue; // Skip the goal cell itself
                            int parentRow = i + vi;
                            int parentCol = j + vj;
                            if (canMoveTo(parentRow, parentCol)) {
                                return new PathCell(i, j, new PathCell(parentRow, parentCol, null));
                            }
                        }
                    }
                }
            }
        }
        return null; // Goal cell not found or no valid parent
    }

    private boolean isGoal(PathCell cell) {
        return cell.i == goalPosition[0] && cell.j == goalPosition[1];
    }

    private int calcHeuristic(int i, int j) {
        return Math.abs(i - goalPosition[0]) + Math.abs(j - goalPosition[1]);
    }

    private Direction reconstructRoute(PathCell goal) {

        System.out.println("------------h-e-r-e----------reconstructRoute--------");
        LinkedList<Direction> route = new LinkedList<>();

        PathCell current = goal;
        while (current.parent != null) {
            route.addFirst(new Direction(current.i - current.parent.i, current.j - current.parent.j));
            current = current.parent;
        }
        System.out.println("------------r__------:" + current);
        return route.isEmpty() ? STAY : route.getFirst();
    }


    private int f(PathCell cell) {
        return gValues.getOrDefault(cell, 0) + hValues.getOrDefault(cell, 0);
    }

    @Override
    public Direction getDirection(long timeBudget) {
        PathCell startCell = new PathCell(state.i, state.j, null);


        System.out.println("------------h-e-r-e------------------");
        gValues.put(startCell, 0);
        System.out.println("------------h-e-r-e----1--------------");
        hValues.put(startCell, calcHeuristic(state.i, state.j));

        //Coin nearestCoin = findNearestCoin();

       /* if (nearestCoin != null && isCoinWithinDistance(nearestCoin, 5) && !collectedCoins.contains(nearestCoin)) {
            return computePathToCoin(startCell, nearestCoin);
        }
*/
        PriorityQueue<PathCell> openCells = new PriorityQueue<>((cell1, cell2) -> f(cell1) - f(cell2));

        Set<PathCell> closedCells = new HashSet<>();


        openCells.add(startCell);

        while (!openCells.isEmpty()) {

            PathCell currentCell = openCells.poll();

            if (closedCells.contains(currentCell)) {

                continue;

            }

            closedCells.add(currentCell);



            PathCell goalCell = findGoalCell();
//            System.out.println("------------h-e-r-e-----goalcell-------------" + goalCell.parent);
            int[] pos= findGoalPosition();
//            System.out.println("------------h-e-r-e-----pos-------------" + pos[0] + ", " + pos[1] );

//            if (goalCell != null && isGoal(currentCell)) {
//
//                System.out.println("------------h-e-r-e-----recs-------------");
//             //   return reconstructRoute(goalCell);
//            }


      if (isGoal(currentCell)) {
            //todo: tul hosszu a ciklus. lejar sz ido mielott vegigszkennelne es aznositana a celt. itt kelene egy olyan fuggveny, ami
            //megtalálja a celt es visszaadja a cel i,j idexet, a reconstructRoute, pedig csak azt dolgozna fel, azt kapna parametrben

            System.out.println("------------h-e-r-e---it is a goal cell :D---------------");
//
//            System.out.println("------------h-e-r-e-----ee-------------");
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

            return STAY;
        }
        return STAY;
    }
}
