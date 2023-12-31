///BAnna, Berke.Anna.Vivien@stud.u-szeged.hu


import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
import game.racetrack.utils.PathCell;
import game.racetrack.utils.Cell;
import java.util.Random;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
/**
 * jatekos implementalasa
 * tartalmazza a jatekos logikajat, utvonal keresest
 */
public class Agent extends RaceTrackPlayer {
    private int[][] track;
    private int[] goalPosition;
    private static final int SPEED = 1;

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
     * @return Egy pathcell, ami a celt tarolja az osevel egyutt
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
    private boolean isGoal(Node node) {
        return node.i == goalPosition[0] && node.j == goalPosition[1];
    }



    /** kiszamolja a megadott koordinatak alapjan a mezo manhattan tavolsagat a celhoz kepest
     * A Manhattan-tavolsag azt mutatja meg, hogy hany lepesre van egymastol ket pont,
     * ha csak vizszintes vagy fuggoleges lepesek lehetnek.
     * @param i - 1. koordinata -aktualis mezo sora
     * @param j - 2. koordinata - aktualis mezo oszlopa
     * @param cell - a cel Pathcell, aminek koordinataihoz viszonyitva szamoljuk a tavolsagot.
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
    private Direction reconstructRoute(Node goal) {
        LinkedList<Direction> route = new LinkedList<>();
        Node current = goal; //celra mutat a current
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
        //letrehoz egy uj node-ot , aminek koltsege 0, a heurisztikat pedig az aktualis helyzetbol szamolja
        Node startNode = new Node(state.i, state.j, null, 0, calcHeuristic(state.i, state.j, findGoalCell()));
        //prioritasi sor,  ket node-ot hasonlit ossze, az f-ek alapjan, alacsonyabb f-el rendelkezo kap nagyobb prioritast
        PriorityQueue<Node> openNodes = new PriorityQueue<>((node1, node2) -> node1.f() - node2.f());
        //letrehoz egy halmazt
        Set<Node> closedNodes = new HashSet<>();
        //atadja a kezdomezot
        openNodes.add(startNode);

        while (!openNodes.isEmpty()) {//addig fut, amig az openNodes nem ures
            Node currentNode = openNodes.poll(); //kiveszi a legjobbat
            if (closedNodes.contains(currentNode)) { //ha benne van mar a closedNodes-ban, akkor tovabblep a kovetkezo iteracioba
                continue;
            }
            closedNodes.add(currentNode); //hozzaadja a closedNodes-hoz

            if (isGoal(currentNode)) { //ha a celmezo, akkor visszavezeti  az utat. felepiti a cel es a kezdopont kozotti utat
                return reconstructRoute(currentNode);
            }
            // Szomszedos mezok vizsgalata
            // dupla ciklussal megnezi az osszes szomszedjat, az atlosakat is
            // Szomszedos mezok keresese
            for (int vi = -1; vi <= 1; vi++) {
                for (int vj = -1; vj <= 1; vj++) {
                    // Kihagyja a jelenlegi poziciot (maradas)
                    if (vi == 0 && vj == 0) {
                        continue;
                    }

                    int nextRow = currentNode.i + vi;
                    int nextColumn = currentNode.j + vj;


                    // Ellenorzes, hogy lephetunk-e a mezore

                    if (!canMoveTo(nextRow, nextColumn)) {
                        nextRow = currentNode.i - vi;
                        nextColumn = currentNode.j - vj;
                        continue;
                    }
                    //ha lepheto, letrehoz egy uj node-t, ha nincs benne se a nyitott,se a zart sorban, akkor hozzadja az openNodes-hoz.


                    Node neighbor = new Node(nextRow, nextColumn, currentNode, currentNode.g + 1, calcHeuristic(nextRow, nextColumn, findGoalCell()));
                    if (!closedNodes.contains(neighbor) && !openNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }

        }
        // Ha nem talal utvonalat
        return STAY;
    }

    /**
     * Node osztaly letrehozasa, ami a pathCell-bol oroklodik. Hasznalja a pathcell eredeti adattagjait, de
     * az a* algoritmushoz szukseges g,h, f adatokat is kiszamolja.
     */
    private class Node extends PathCell {
        Node parent;
        int g, h;

        Node(int i, int j, Node parent, int g, int h) {
            super(i, j, parent);
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        /**
         * Megtett ut (g) es celpontig vezeto ut (h) koltsegenek osszegenek kiszamitasa
         * @return f = g + h
         */
        int f() {
            return g + h;
        }

        /**
         * Ellenorzi, hogy a ket Node objektum egyenlo-e
         * @param objekt
         * @return osszehasonlitja a ket Node objekt i es j koordinatait,
         *  ellenorzi, hogy a parameterben kapott object nem null es azonos osztalyu-e, mint a Node,
         *  az object onmagaval egyezik-e
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) { return true; }
            if (object == null || getClass() != object.getClass()){ return false; }
            Node node = (Node) object;
            return i == node.i && j == node.j;
        }

        /**
         * Egesz szamot general, ami az objektum tartalmat mutatja
         * @return general egy szamot, ami az i es j ertekeit veszi figyelembe
         */
        @Override
        public int hashCode() {
            return Objects.hash(i, j);
        }
    }
}
