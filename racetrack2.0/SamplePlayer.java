///BAnna, Berke.Anna.Vivien@stud.u-szeged.hu


import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
import game.racetrack.utils.PathCell;
import java.util.*;
/**
 * jatekos implementalasa
 * tartalmazza a jatekos logikajat, utvonal keresest
 */
public class SamplePlayer extends RaceTrackPlayer {
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
    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
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
    private boolean isGoal(Node node) {
        return node.i == goalPosition[0] && node.j == goalPosition[1];
    }



    /**
     * Kiszámítja a megadott koordináták alapján a mező távolságát a célhoz képest.
     * @param i - 1. koordinata -aktualis mezo sora
     * @param j - 2. koordinata - aktualis mezo oszlopa
     * @return célhoz vezető távolság heurisztikus erteke - abszolut ertek osszege, az i és celmezo sora, a j es celmezo oszlopa
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
     * @return utvonal a celig. Ha nem talált útvonalat egy helyben marad
     */
    @Override
    public Direction getDirection(long timeBudget) {
        //letrehoz egy uj node-ot , aminek koltsege 0, a heurisztikat pedig az aktualis helyzetbol szamolja
        Node startNode = new Node(state.i, state.j, null, 0, calcHeuristic(state.i, state.j));
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
            // Szomszédos mezők vizsgálata
            // dupla ciklussal megnezi az osszes szomszedjat, az atlosakat is
            for (int vi = SPEED; vi >= -SPEED; vi--) {
                for (int vj = SPEED; vj >= -SPEED; vj--) {
                    int nextRow = currentNode.i + vi;
                    int nextColumn = currentNode.j + vj;

                    // Ellenőrizzük, hogy léphetünk-e erre a mezőre
                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }
                    //ha lepheto, letrehoz egy uj node-t, ha nincs benne se a nyitott,se a zart sorban, akkor hozzadja az openNodes-hoz.
                    Node neighbor = new Node(nextRow, nextColumn, currentNode, currentNode.g + 1, calcHeuristic(nextRow, nextColumn));
                    if (!closedNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }
        }
        // Ha nem talált útvonalat
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
         *  ellenorzi, hogy a parameterben kapott object nem null Ă©s azonos osztalyu-e, mint a Node,
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
