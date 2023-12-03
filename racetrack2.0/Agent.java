///BAnna, Berke.Anna.Vivien@stud.u-szeged.hu

import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.Cell;
import game.racetrack.utils.PathCell;
import game.racetrack.utils.PlayerState;
import java.util.*;

/**
 * jatekos implementalasa
 * tartalmazza a jatekos logikajat, utvonal keresest
 */
public class Agent extends RaceTrackPlayer {
    private int[][] track;
    private int[] goalPosition;
    private static final int SPEED = 1;


    /**
     * iranyok
     */
    private Direction DOWN = RaceTrackGame.DIRECTIONS[7];
    private Direction LEFT = RaceTrackGame.DIRECTIONS[1];
    private Direction RIGHT = RaceTrackGame.DIRECTIONS[5];
    private Direction UP = RaceTrackGame.DIRECTIONS[3];
    private Direction STAY = RaceTrackGame.DIRECTIONS[0];

    /**
     * Konstruktor
     *
     * @param state - kezdeti jatekos allapot
     * @param random - veletlenszam-genertor
     * @param track - a palya
     * @param coins - ermek tombje, amik a palyan vannak
     * @param color - szinek
     */
    public Agent(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;
        this.goalPosition = findGoalPosition();
    }



    /**
     * Ellenorzi, hogy az adott koordinataju mezo lepheto-e
     * eloszor ellenorzi, hogy a palyan belul talalhato-e a koordinata
     * aztan elleorzi, hogy fal van-e a mezon
     * @param i - 1. koordinata - a mezo soranak indexe
     * @param j - 2. koordinata -a mezo oszlopanak indexe
     * @return ha a mezon fal van, vagy a palyan kivuli mezo lenne hamisat ad vissza, ha lepheto igazat
     */

    private boolean canMoveTo(int i, int j) {
        if (i < 0 || i >= track.length) {
            return false;
        }
        if(j < 0 || j >= track[0].length){
            return false;
        }
        return (track[i][j] & RaceTrackGame.WALL) == 0;
    }

    /**
     * A cel poziciojanak keresese
     * @return A cel pozicio koordinatait tarolo egesz szamokból allo tomb.
     *  *         Ha nincs cel a palyan vagy nem talal, null értékkel tér vissza.
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

    /**
     * Ellenőrzi, hogy az adott mezo helyzete megegyezik-e a cellal
     * @param cell - a vizsgalando cella, amit meg akarunk nezni, hogy azonos-e a cellal
     * @return logikai valtozo, hogy a cella megegyezik-e a cellal
     */
    private boolean isGoal(Cell cell) {
        return cell.i == goalPosition[0] && cell.j == goalPosition[1];
    }
    /**
     * Kiszamolja a megadott koordinatak alapjan a mezo milyen tavolsagra van a celponttol
     * @param i - 1. koordinata -aktualis mezo sora
     * @param j - 2. koordinata - aktualis mezo oszlopa
     * @return heurisztika erteke - abszolut ertek osszege, az i és celmezo sora, a j es celmezo oszlopa
     */

    private int calcHeuristic(int i, int j) {
        return Math.abs(i - goalPosition[0]) + Math.abs(j - goalPosition[1]);
    }



    /**
     * Rekonstrualja az utat, visszakoveti az utat a celmezotol a kezdopontig
     * A metódus a PathCell objektumok parent adatait használja
     * minden lepest a route-hoz ad hozza
     * @param celmezo -  cél pozíciója található, célmező PathCell objektum
     * @return ha ures az utvonal egy helyben marad, ha nem ures, az utvonal elso elemet adja vissza
     */

    private Direction reconstructRoute(PathCell goal) {
        LinkedList<Direction> route = new LinkedList<>();
        PathCell current = goal;
        while (current.parent != null) {
            route.addFirst(new Direction(current.i - current.parent.i, current.j - current.parent.j));
            current = current.parent;
        }
        return route.isEmpty() ? STAY : route.getFirst();
    }



    /**
     * Kiszamitja a kovetkezo lepest
     * Az a* algoritmust haszanlja a legrovidebb ut kiszamitasara
     * @param timeBudget
     * @return A kovetkezo lepessel iranyaval ter vissza. Ha nincs megfelelo lepes egy helyben marad
     */

    @Override
    public Direction getDirection(long timeBudget) {
        PathCell startCell = new PathCell(state.i, state.j, null);
        Map<PathCell, Integer> g = new HashMap<>();
        Map<PathCell, Integer> h = new HashMap<>();
        g.put(startCell, 0);
        h.put(startCell, calcHeuristic(startCell.i, startCell.j));

        PriorityQueue<PathCell> openCells = new PriorityQueue<>((cell1, cell2) -> g.get(cell1) + h.get(cell1) - g.get(cell2) - h.get(cell2));
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
                        g.put(neighbor, g.get(currentCell) + 1);
                        h.put(neighbor, calcHeuristic(nextRow, nextColumn));
                        openCells.add(neighbor);
                    }
                }
            }
        }
        return STAY;
    }
}
