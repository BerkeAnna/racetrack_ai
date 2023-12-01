///BAnna, Berke.Anna.Vivien@stud.u-szeged.hu

import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
import java.util.*;

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

    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;
        this.coins = coins;
        this.goalPosition = findGoalPosition();
    }
    private boolean isCoinAt(int i, int j) {
        for (Coin coin : coins) {
            // Feltételezve, hogy a Coin osztálynak van getPositionX() és getPositionY() metódusa
            if (coin.i == i && coin.j == j) {
                return true;
            }
        }
        return false;
    }


    /**
     * Ellenorzi, hogy az adott koordinataju mezo lepheto-e
     * @param i - 1. koordinata
     * @param j - 2. koordinata
     * @return ha a mezőn fal van, vagy a palyan kivuli mezo lenne hamisat ad vissza, ha lephető igazat
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
     * @return A celpozicio koordinatai
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
     * Ellenorzi az adott mezo helyzete megegyezik-e a cellal
     * @param a mezo
     * @return a mezo megegyezik-e a celmezovel
     */
    private boolean isGoal(Node node) {
        return node.i == goalPosition[0] && node.j == goalPosition[1];
    }



    /**
     * Kiszamolja a megadott koordinatak alapjan a mezo milyen tavolsagra van a celponttol
     * @param i - 1. koordinata
     * @param j - 2. koordinata
     * @return heurisztika erteke
     */
    private int calcHeuristic(int i, int j) {
        return Math.abs(i - goalPosition[0]) + Math.abs(j - goalPosition[1]);
    }

    // Külön heurisztika az érmékhez
    private int calcHeuristicToCoin(int i, int j, Coin nearestCoin) {
        if (nearestCoin != null) {
            return Math.abs(i - nearestCoin.i) + Math.abs(j - nearestCoin.j);
        }
        return Integer.MAX_VALUE; // Ha nincs érme, a heurisztika értéke legyen nagyon magas
    }


    /**
     * Rekonstrualja az utat, visszakoveti az utat kezdoponttol, celpontig
     * @param celmezo
     * @return ha ures az utvonal egy helyben marad, ha nem ures, az utvonal elso elemet adja vissza
     */
    private Direction reconstructRoute(Node goal) {
        LinkedList<Direction> route = new LinkedList<>();
        Node current = goal;
        while (current.parent != null) {
            route.addFirst(new Direction(current.i - current.parent.i, current.j - current.parent.j));
            current = current.parent;
        }
        return route.isEmpty() ? STAY : route.getFirst();
    }


    /**
     * Kiszamítja a kovetkezo lepest
     * @param timeBudget
     * @return A kovetkezo lepessel ter vissza
     */

    private Direction computePathToGoal(Node startNode) {
        Coin nearestCoin = findNearestCoin();

        // Ha van érme a közelben, ellenőrizzük az útvonalat
        if (nearestCoin != null) {
            int pathLengthToCoin = getPathLengthToCoin(nearestCoin, startNode);
            // Ellenőrizzük, hogy a coin felé vezető út rövidebb-e, mint a coin értéke kétszerese
            if (pathLengthToCoin < 2 * nearestCoin.value) {
                // Frissítjük a startNode heurisztikáját az érme felé
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
                return reconstructRoute(currentNode);
            }

            for (int vi = -SPEED; vi <= SPEED; vi++) {
                for (int vj = -SPEED; vj <= SPEED; vj++) {
                    int nextRow = currentNode.i + vi;
                    int nextColumn = currentNode.j + vj;

                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }

                    int newCost = currentNode.g + 1;
                    Node neighbor = new Node(nextRow, nextColumn, currentNode, newCost,
                            nearestCoin != null ? calcHeuristicToCoin(nextRow, nextColumn, nearestCoin) : calcHeuristic(nextRow, nextColumn));
                    if (!closedNodes.contains(neighbor) && !openNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }
        }
        return STAY;
    }

    private Direction restartPathFromCoin(Node coinNode) {
        state.i = coinNode.i;  // Frissítjük a játékos pozícióját az érme pozíciójára
        state.j = coinNode.j;

        Node startNode = new Node(state.i, state.j, null, 0, calcHeuristic(state.i, state.j));
        return computePathToGoal(startNode);  // Új útvonal számítása a cél felé
    }

    @Override
    public Direction getDirection(long timeBudget) {
        Node startNode = new Node(state.i, state.j, null, 0, calcHeuristic(state.i, state.j));
        Coin nearestCoin = findNearestCoin();

        if (nearestCoin != null && isCoinWithinDistance(nearestCoin, 5) && !collectedCoins.contains(nearestCoin)) {
            return computePathToCoin(startNode, nearestCoin);
        }
        return computePathToGoal(startNode);
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
    private Direction computePathToCoin(Node startNode, Coin coin) {
        PriorityQueue<Node> openNodes = new PriorityQueue<>((node1, node2) -> node1.f() - node2.f());
        Set<Node> closedNodes = new HashSet<>();
        openNodes.add(startNode);



        while (!openNodes.isEmpty()) {
            Node currentNode = openNodes.poll();
            if (closedNodes.contains(currentNode)) {
                continue;
            }
            closedNodes.add(currentNode);

            // Ha elérjük a coin pozícióját
            if (currentNode.i == coin.i && currentNode.j == coin.j) {
                collectedCoins.add(coin); // Érme hozzáadása a gyűjtött érmékhez
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

                    Node neighbor = new Node(nextRow, nextColumn, currentNode, currentNode.g + 1, calcHeuristicToCoin(nextRow, nextColumn, coin));
                    if (!closedNodes.contains(neighbor) && !openNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }
        }
        return STAY; // Ha nem talált útvonalat
    }


    private boolean isCoinWithinDistance(Coin coin, int distance) {
        return Math.abs(coin.i - state.i) + Math.abs(coin.j - state.j) <= distance;
    }




    private int getPathLengthToCoin(Coin coin, Node startNode) {
        PriorityQueue<Node> openNodes = new PriorityQueue<>((node1, node2) -> node1.f() - node2.f());
        Set<Node> closedNodes = new HashSet<>();
        openNodes.add(startNode);

        while (!openNodes.isEmpty()) {
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

                    Node neighbor = new Node(nextRow, nextColumn, currentNode, currentNode.g + 1, calcHeuristicToCoin(nextRow, nextColumn, coin));
                    if (!closedNodes.contains(neighbor) && !openNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }
        }
        return Integer.MAX_VALUE; // Ha nem talál útvonalat, egy nagyon nagy számot ad vissza
    }


    private Direction restartPathAfterCoin(Node coinNode) {
        state.i = coinNode.i;  // Frissítjük a játékos pozícióját az érme pozíciójára
        state.j = coinNode.j;

        Node startNode = new Node(state.i, state.j, null, 0, calcHeuristic(state.i, state.j));
        return computePathToGoal(startNode);  // Új útvonal számítása a cél felé
    }

    private Direction computePathToFinish(Node startNode) {
        PriorityQueue<Node> openNodes = new PriorityQueue<>((node1, node2) -> node1.f() - node2.f());
        Set<Node> closedNodes = new HashSet<>();
        openNodes.add(startNode);

        while (!openNodes.isEmpty()) {
            Node currentNode = openNodes.poll();
            if (closedNodes.contains(currentNode)) {
                continue;
            }
            closedNodes.add(currentNode);

            // Ha elérjük a célt
            if (isGoal(currentNode)) {
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
         * @param objekt
         * @return osszehasonlítja a ket Node objekt i és j koordinatait,
         *  ellenorzi, hogy a parameterben kapott object nem null és azonos osztalyu-e, mint a Node,
         *  az object onmagával egyezik-e
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
