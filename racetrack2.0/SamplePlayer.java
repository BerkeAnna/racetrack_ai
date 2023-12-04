///BAnna, Berke.Anna.Vivien@stud.u-szeged.hu

import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
import java.util.*;
/**
 * jatekos implementalasa
 * tartalmazza a jatekos logikajat, utvonal keresest
 */
public class SamplePlayer extends RaceTrackPlayer {
    private int[][] track;
    private int[] goalPosition;
    private static final int SPEED = 1;
    private Coin[] coins;

    private Set<Coin> collectedCoins = new HashSet<>();
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
        this.coins = coins;
        this.goalPosition = findGoalPosition();
    }

    /**
     * Ellenorzi az adott mezot, van -e rajta erme
     * @param i - erme poziciojanak sor indexe
     * @param j - erme poziciojanak oszlop indexe
     * @return logikai valtozoval ter vissza, igaz, ha van a  mezon erme
     */
    private boolean isCoinAt(int i, int j) {
        for (Coin coin : coins) {
            if (coin.i == i && coin.j == j) {
                return true;
            }
        }
        return false;
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
     * Kiszámítja a megadott koordináták alapján a mező távolságát egy kozelei ermehez képest.
     * @param i - 1. koordinata -aktualis mezo sora
     * @param j - 2. koordinata - aktualis mezo oszlopa
     * @param nearestCoin - a legkozelebbi erme
     * @return ermehez vezető távolság heurisztikus erteke
     */
    private int calcHeuristicToCoin(int i, int j, Coin nearestCoin) {
        if (nearestCoin != null) {
            return Math.abs(i - nearestCoin.i) + Math.abs(j - nearestCoin.j);
        }
        return Integer.MAX_VALUE; // Ha nincs érme, a heurisztika értéke -> nagyon magas
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
        while (current.parent != null) { //addig fut, amig van szuloje a mezonek
            route.addFirst(new Direction(current.i - current.parent.i, current.j - current.parent.j)); //hozzaadja a current es szuloje kozotti iranyt
            current = current.parent;
        }
        return route.isEmpty() ? STAY : route.getFirst(); //ha ures a route maradjon egyhelyben, ha nem ures a route 1. elemevel ter vissza
    }


    /**
     * Kiszamítja a kovetkezo lepest
     * @param timeBudget
     * @return A kovetkezo lepessel ter vissza
     */

    private Direction computePathToGoal(Node startNode) {
        Coin nearestCoin = findNearestCoin(); //megkeresi a legkozelebbi ermet

        // Ha van érme a közelben, ellenőrizzük az utat
        if (nearestCoin != null) {
            int pathLengthToCoin = getPathLengthToCoin(nearestCoin, startNode);
            // Ellenőrizzük, hogy a coin felé vezető út rövidebb-e, mint a coin értéke kétszerese
            if (pathLengthToCoin < 2 * nearestCoin.value) {
                // Frissítjük a startNode heurisztikáját
                startNode = new Node(state.i, state.j, null, 0, calcHeuristicToCoin(state.i, state.j, nearestCoin));
            }
        }

        PriorityQueue<Node> openNodes = new PriorityQueue<>((node1, node2) -> node1.f() - node2.f());
        Set<Node> closedNodes = new HashSet<>();
        openNodes.add(startNode);

        while (!openNodes.isEmpty()) {
            Node currentNode = openNodes.poll();
            if (closedNodes.contains(currentNode)) {
                continue;
            }
            closedNodes.add(currentNode);

            // Ha elértük a célt vagy az érmét
            if (nearestCoin != null && currentNode.i == nearestCoin.i && currentNode.j == nearestCoin.j ||
                    isGoal(currentNode)) {
                return reconstructRoute(currentNode); //ha megtalalja a celt rekonstrualja az utat
            }

            for (int vi = -SPEED; vi <= SPEED; vi++) {
                for (int vj = -SPEED; vj <= SPEED; vj++) {
                    int nextRow = currentNode.i + vi;
                    int nextColumn = currentNode.j + vj;

                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }

                    int newCost = currentNode.g + 1;
                    //Letrehoz egy uj Node-ot a szomszedos helyzetben, frissitve a koltseget es a heurisztikat
                    Node neighbor = new Node(nextRow, nextColumn, currentNode, newCost,
                            nearestCoin != null ? calcHeuristicToCoin(nextRow, nextColumn, nearestCoin) : calcHeuristic(nextRow, nextColumn));
                    if (!closedNodes.contains(neighbor) && !openNodes.contains(neighbor)) {
                        openNodes.add(neighbor); //ha a szomszed mezo nincs se a zart, se a ynitott halmazban, hozzaadja a nyitothoz
                    }
                }
            }
        }
        return STAY;
    }

        /**
         * A felvett ermetol kiszamitja az utat a celig
         * @param coinNode
         * @return uj utvonal a celig
         */
        private Direction restartPathFromCoin(Node coinNode) {
            state.i = coinNode.i;
            state.j = coinNode.j;

            //letrehoz egy uj node-ot az erme helyzetevel, aminek koltsege 0, a heurisztikat pedig az aktualis helyzetbol szamolja
            Node startNode = new Node(state.i, state.j, null, 0, calcHeuristic(state.i, state.j));
            //kiszamolja az utvonalat a celig
            return computePathToGoal(startNode);
        }


    /**
     * A celhoz vezeto ut kiszamitasa
     * @param timeBudget
     * @return utvonal a celig
     */
    @Override
    public Direction getDirection(long timeBudget) {
        //letrehoz egy uj node-ot , aminek koltsege 0, a heurisztikat pedig az aktualis helyzetbol szamolja
        Node startNode = new Node(state.i, state.j, null, 0, calcHeuristic(state.i, state.j));
        //megkeresi a legkozelebbi ermet
        Coin nearestCoin = findNearestCoin();

        //ellenorzi, hogy az erme 5 egysegen belul van-e és fel lett-e mar veve
        if (nearestCoin != null && isCoinWithinDistance(nearestCoin, 5) && !collectedCoins.contains(nearestCoin)) {
            //kiszamitja az utvonalat az ermehez
            return computePathToCoin(startNode, nearestCoin);
        }
        //kiszamitja az utat a celhoz
        return computePathToGoal(startNode);
    }


    /**
     * megkeresi a legkozelebbi ermet
     * @return a legkozelebbi erme
     */
    private Coin findNearestCoin() {
        Coin nearest = null;
        int minDistance = Integer.MAX_VALUE;
        for (Coin coin : coins) { //vegigiteral az osszes elerheto ermen
            if (!collectedCoins.contains(coin)) { //ha az erme mar egyszer fel leltt vege nem veszi figyelembe
                int distance = Math.abs(coin.i - state.i) + Math.abs(coin.j - state.j); //kiszamitja az ermekre a jatekostol valo tavolsagot
                if (distance < minDistance) { //megkeresi a legkozelebbi ermet
                    nearest = coin;
                    minDistance = distance;
                }
            }
        }
        return nearest; //visszaadja a legkozelebbi, meg nem felvett ermet
    }

    /**
     * kiszamitja startponttol a celpontig a legrovidebb utat
     * @param startNode kezdo pozicio
     * @param coin az erme, amit el akarunk erni
     * @return a megfelelo irannyal, lepessel ter vissza
     */
    private Direction computePathToCoin(Node startNode, Coin coin) {
        PriorityQueue<Node> openNodes = new PriorityQueue<>((node1, node2) -> node1.f() - node2.f());
        Set<Node> closedNodes = new HashSet<>();
        openNodes.add(startNode);



        while (!openNodes.isEmpty()) { //addig fut, amig az openNodes nem ures
            Node currentNode = openNodes.poll(); //kiveszi a legjobb csomopontot
            if (closedNodes.contains(currentNode)) {
                continue;
            }
            closedNodes.add(currentNode);

            // Ha elérjük a coin pozícióját
            if (currentNode.i == coin.i && currentNode.j == coin.j) {
                collectedCoins.add(coin); // Érme hozzáadása a gyűjtött érmékhez
                return reconstructRoute(currentNode); //rekonstrualja az utat
            }

            // Szomszédos mezők vizsgálata
            for (int vi = -SPEED; vi <= SPEED; vi++) {
                for (int vj = -SPEED; vj <= SPEED; vj++) {
                    int nextRow = currentNode.i + vi;
                    int nextColumn = currentNode.j + vj;

                    // Ellenőrizzük, hogy léphetünk-e erre a mezőre
                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }

                    Node neighbor = new Node(nextRow, nextColumn, currentNode, currentNode.g + 1, calcHeuristicToCoin(nextRow, nextColumn, coin));
                    if (!closedNodes.contains(neighbor) && !openNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }
        }
        return STAY; // Ha nem talált útvonalat
    }


    /**
     * Az erme tavolsagat szamitja ki a jelenlegi poziciotol, es ellenorzi ez kisebb tav-e mint a megadott tav
     * @param coin
     * @param distance
     * @return kozelebb van-e az erme, mint a distance erteke
     */
    private boolean isCoinWithinDistance(Coin coin, int distance) {
        return Math.abs(coin.i - state.i) + Math.abs(coin.j - state.j) <= distance;
    }


    /**
     * Kiszámítja egy érme felé vezető útvonal hosszát.
     * @param coin - a celerme, amit el akarunk erni
     * @param startNode - a kezdopozicio Node objektuma
     * @return az utvonal hossza
     */
    private int getPathLengthToCoin(Coin coin, Node startNode) {
        PriorityQueue<Node> openNodes = new PriorityQueue<>((node1, node2) -> node1.f() - node2.f());
        Set<Node> closedNodes = new HashSet<>();
        openNodes.add(startNode);

        while (!openNodes.isEmpty()) { //addig fut, amig az openNodes nem ures
            Node currentNode = openNodes.poll();
            if (closedNodes.contains(currentNode)) {
                continue;
            }
            closedNodes.add(currentNode);

            // Ha elérjük a coin pozícióját, visszaadjuk az útvonal hosszát
            if (currentNode.i == coin.i && currentNode.j == coin.j) {
                return currentNode.g;
            }

            for (int vi = -SPEED; vi <= SPEED; vi++) {
                for (int vj = -SPEED; vj <= SPEED; vj++) {
                    int nextRow = currentNode.i + vi;
                    int nextColumn = currentNode.j + vj;

                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }
                    //ha lehet lepni letrehoz egy uj node-t a szomszedos helyen, ha ez nincs benne se a nyitott, se a zart sorban, hozzaadja az openNodes-hoz
                    Node neighbor = new Node(nextRow, nextColumn, currentNode, currentNode.g + 1, calcHeuristicToCoin(nextRow, nextColumn, coin));
                    if (!closedNodes.contains(neighbor) && !openNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }
        }
        return Integer.MAX_VALUE; // Ha nem talál útvonalat, egy nagy számot ad vissza
    }

    /**
     * ha felvette az ermet, kiszamitja az uj utat a celig
     * @param coinNode -  érmével kapcsolatos Node objektum.
     * @return uj utvonal a celig
     */
    private Direction restartPathAfterCoin(Node coinNode) {
        state.i = coinNode.i;
        state.j = coinNode.j;

        //uj node letrehozasa, nincs szuloje, heurisztikat az aktualis helyzet alapjan kiszamolja
        Node startNode = new Node(state.i, state.j, null, 0, calcHeuristic(state.i, state.j));
        //kiszamolja az utat a celig.
        return computePathToGoal(startNode);
    }

    /**
     * kiszamolja a legrovidebb utat a celig
     * @param startNode - a kezdopont Node objektuma
     * @return a megfelelo irany a cel fele
     */
    private Direction computePathToFinish(Node startNode) {
        PriorityQueue<Node> openNodes = new PriorityQueue<>((node1, node2) -> node1.f() - node2.f());
        Set<Node> closedNodes = new HashSet<>();
        openNodes.add(startNode);

        while (!openNodes.isEmpty()) { //addig fut, amig az openNodes nem ures
            Node currentNode = openNodes.poll(); //kiveszi a legjobbat
            if (closedNodes.contains(currentNode)) {
                continue;
            }
            closedNodes.add(currentNode);

            // Ha elérjük a célt
            if (isGoal(currentNode)) {
                //rekonstrualja es visszaadja az utat
                return reconstructRoute(currentNode);
            }

            // Szomszédos mezők vizsgálata
            for (int vi = -SPEED; vi <= SPEED; vi++) {
                for (int vj = -SPEED; vj <= SPEED; vj++) {
                    int nextRow = currentNode.i + vi;
                    int nextColumn = currentNode.j + vj;

                    // Ellenőrizzük, hogy léphetünk-e erre a mezőre
                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }
                    //ha lepheto, letrehoz egy uj node-t, ha nincs benne se a nyitott,se a zart sorban, akkor hozzadja az openNodes-hoz.
                    Node neighbor = new Node(nextRow, nextColumn, currentNode, currentNode.g + 1, calcHeuristic(nextRow, nextColumn));
                    if (!closedNodes.contains(neighbor) && !openNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }
        }
        return STAY; // Ha nem talált útvonalat
    }

    /**
     * Node osztaly
     */
    private class Node {
        int i, j;
        Node parent;
        int g, h;

        Node(int i, int j, Node parent, int g, int h) {
            this.i = i;
            this.j = j;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        /**
         * Megtett ut (g) és celpontig vezeto ut (h) koltsegenek osszegenek kiszamitasa
         * @return f = g + h
         */
        int f() {
            return g + h;
        }

        /**
         * Ellenorzi, hogy a ket Node objektum egyenlo-e
         * ellenorzi, hogy a parameterben kapott object nem null és azonos osztalyu-e, mint a Node,
         *  az object onmagával egyezik-e
         * @param object
         * @return osszehasonlítja a ket Node objekt i és j koordinatait,

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
         * @return general egy szamot, ami az i és j ertekeit veszi figyelembe
         */
        @Override
        public int hashCode() {
            return Objects.hash(i, j);
        }
    }


}