///BAnna, Berke.Anna.Vivien@stud.u-szeged.hu


import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
import game.racetrack.utils.PathCell;
import game.racetrack.utils.Cell;
import java.util.*;
/**
 * jatekos implementalasa
 * tartalmazza a jatekos logikajat, utvonal keresest
 */
public class SamplePlayer extends RaceTrackPlayer {
    private int[][] track;
    private int[] goalPosition;
    private static final int SPEED = 1;
    private List<PathCell> pathToGoal;
    /**
     * Mozgasiranyok
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
    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;
        this.goalPosition = findGoalPosition();
        this.pathToGoal = RaceTrackGame.BFS(state.i, state.j, track);
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
        return RaceTrackGame.isNotWall(i, j, track);
    }

    /**
     * A cel poziciojanak keresese
     * @return A cel pozicio koordinatait tarolo egesz szamokbol allo tomb.
     *  *         Ha nincs cel a palyan vagy nem talal, null ertekkel ter vissza.
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
     * A cel cella keresese
     * @return Egy pathcell, ami a célezőt tárolja az osevel együtt
     *  *         Ha nincs cel a palyan vagy nem talal, null ertekkel ter vissza.
     */
    private PathCell findGoalCell() {
        for (int i = 0; i < track.length; i++) {
            for (int j = 0; j < track[i].length; j++) {
                if ((track[i][j] & RaceTrackGame.FINISH) == RaceTrackGame.FINISH) {
                    // szomszedos ezoket keres, ami szulo lehet
                    for (int vi = -1; vi <= 1; vi++) {
                        for (int vj = -1; vj <= 1; vj++) {
                            if (vi == 0 && vj == 0) continue; // sajat magat kihagyja
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


    /**
     * Ellenorzi, hogy az adott mezo helyzete megegyezik-e a cellal
     * @param cell - a vizsgalando cella, amit meg akarunk nezni, hogy azonos-e a cellal
     * @return logikai valtozo, hogy a cella megegyezik-e a cellal
     */
    private boolean isGoal(PathCell node) {
        return node.i == goalPosition[0] && node.j == goalPosition[1];
    }



    /** kiszamolja a megadott koordinatak alapjan a mezo manhattan tavolsagat a celhoz kepest
     * @param i - 1. koordinata -aktualis mezo sora
     * @param j - 2. koordinata - aktualis mezo oszlopa
     * @return  heurisztikus erteke - abszolut ertek osszege, az i es celmezo sora, a j es celmezo oszlopa
     */
    private int calcHeuristic(int i, int j, PathCell cell) {
        Cell startCell = new Cell(i,j);
        return RaceTrackGame.manhattanDistance(startCell, cell);
    }


    /**
     * Rekonstrualja az utat, visszakoveti az utat a celmezotol a kezdopontig
     * A metodus a PathCell objektumok parent adatait hasznalja
     * minden lepest a route-hoz ad hozza
     * @param celmezo -  cel pozicioja talalhato, celmezo PathCell objektum
     * @return ha ures az utvonal egy helyben marad, ha nem ures, az utvonal elso elemet adja vissza
     */
    private Direction reconstructRoute(PathCell goal) {
        LinkedList<Direction> route = new LinkedList<>();
        PathCell current = goal; //celra mutat a current
        while (current != null && current.parent != null) {//addig fut, amig van szuloje a mezonek
            route.addFirst(new Direction(current.i - current.parent.i, current.j - current.parent.j));//hozzaadja a current es szuloje kozotti iranyt
            current = current.parent;
        }
        return route.isEmpty() ? STAY : route.getFirst(); //ha ures a route maradjon egyhelyben, ha nem ures a route 1. elemevel ter vissza
    }


    /**
     * A celhoz vezeto ut kiszamitasa
     * @param timeBudget
     * @return utvonal a celig. Ha nem talalt utvonalat egy helyben marad
     */
    @Override
    public Direction getDirection(long timeBudget) {
        if (pathToGoal != null && !pathToGoal.isEmpty()) {
            PathCell nextStep = pathToGoal.get(0); // Get the first element
            pathToGoal.remove(0); // Remove the first element

            if (nextStep != null) {
                int dirRow = nextStep.i - state.i;
                int dirCol = nextStep.j - state.j;
                return new Direction(dirRow, dirCol);
            }
        }
        return STAY;
    }






}
