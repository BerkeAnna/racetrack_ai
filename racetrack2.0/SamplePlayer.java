import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
import java.util.Random;import java.util.*;

public class SamplePlayer extends RaceTrackPlayer {
    private int[][] track;
    private int[] goalPosition;
    private Set<Node> visited;
    private static final int SPEED = 1; // Consistent speed

    private Direction DOWN = RaceTrackGame.DIRECTIONS[7];
    private Direction LEFT = RaceTrackGame.DIRECTIONS[1];
    private Direction RIGHT = RaceTrackGame.DIRECTIONS[5];
    private Direction UP = RaceTrackGame.DIRECTIONS[3];
    private Direction STAY = RaceTrackGame.DIRECTIONS[0];

    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;
        this.goalPosition = findGoalPosition();
        this.visited = new HashSet<>();
    }

    /**
     * A cél poziciójának keresése
     * @return A cél pozíció koordinátái
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
     * Ellenőrzi az adott mező helyzete megegyezik-e a céllal
     * @param a mező
     * @return a mező megegyezik-e a célmezővel
     */
    private boolean isGoal(Node node) {
        return node.i == goalPosition[0] && node.j == goalPosition[1];
    }

    /**
     * Rekonstruálja az utat
     * @param célmező
     * @return ha üres az útvonal egy helyben marad, ha nem üres, az útvonal első elemét adja vissza
     */
    private Direction reconstructPath(Node goal) {
        LinkedList<Direction> path = new LinkedList<>();
        Node current = goal;
        while (current.parent != null) {
            path.addFirst(new Direction(current.i - current.parent.i, current.j - current.parent.j));
            current = current.parent;
        }
        return path.isEmpty() ? STAY : path.getFirst();
    }

    /**
     * Ellenőrzi, hogy az adott koordinátájú mező léphető-e
     * @param i - 1. koordináta
     * @param j - 2. koordináta
     * @return ha a mezőn fal van, vagy a pályán kívüli mező lenne hamisat ad vissza, ha léphető igazat
     */
    private boolean canMoveTo(int i, int j) {
        if (i < 0 || i >= track.length || j < 0 || j >= track[0].length) {
            return false;
        }
        return (track[i][j] & RaceTrackGame.WALL) == 0;
    }

    /**
     * Kiszámolja a megadott koordináták alapján a mező milyen távolságra van a célponttól
     * @param i - 1. koordináta
     * @param j - 2. koordináta
     * @return heurisztika értéke
     */
    private int calcHeuristic(int i, int j) {
        return Math.abs(i - goalPosition[0]) + Math.abs(j - goalPosition[1]);
    }

    @Override
    public Direction getDirection(long timeBudget) {
        Node startNode = new Node(state.i, state.j, null, 0, calcHeuristic(state.i, state.j));
        PriorityQueue<Node> openNodes = new PriorityQueue<>(Comparator.comparingInt(Node::f));
        Set<Node> closedNodes = new HashSet<>();
        openNodes.add(startNode);

        while (!openNodes.isEmpty()) {
            Node currentNode = openNodes.poll();
            if (closedNodes.contains(currentNode)) {
                continue;
            }
            closedNodes.add(currentNode);

            if (isGoal(currentNode)) {
                return reconstructPath(currentNode);
            }

            for (int di = -SPEED; di <= SPEED; di++) {
                for (int dj = -SPEED; dj <= SPEED; dj++) {
                    int nextRow = currentNode.i + di;
                    int nextColumn = currentNode.j + dj;

                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }

                    Node neighbor = new Node(nextRow, nextColumn, currentNode, currentNode.g + 1, calcHeuristic(nextRow, nextColumn));
                    if (!closedNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }
        }
        return STAY;
    }

    /**
     * Node osztály
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
         * Megtett út (g) és célpontig vezető út (h) költségének összegének kiszámízása
         * @return f = g + h
         */
        int f() {
            return g + h;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Node node = (Node) object;
            return i == node.i && j == node.j;
        }

        @Override
        public int hashCode() {
            return Objects.hash(i, j);
        }
    }

   /* private void printCostsTable() {
        // Temporary variables to simulate the state and other necessary data
        int currentRow = 0; // Example starting row
        int currentColumn = 0; // Example starting column
        int currentSpeedI = 0; // Example speed I component
        int currentSpeedJ = 0; // Example speed J component

        System.out.println("f-costs Table:");
        for (int i = 0; i < track.length; i++) {
            for (int j = 0; j < track[i].length; j++) {
                Node node = new Node(i, j, null, calculateGCost(i, j, currentRow, currentColumn, currentSpeedI, currentSpeedJ), calcHeuristic(i, j));
                System.out.print(node.f() + "\t");
            }
            System.out.println();
        }

        System.out.println("\ng-costs Table:");
        for (int i = 0; i < track.length; i++) {
            for (int j = 0; j < track[i].length; j++) {
                Node node = new Node(i, j, null, calculateGCost(i, j, currentRow, currentColumn, currentSpeedI, currentSpeedJ), calcHeuristic(i, j));
                System.out.print(node.g + "\t");
            }
            System.out.println();
        }
    }


    private int calculateGCost(int i, int j, int currentRow, int currentColumn, int currentSpeedI, int currentSpeedJ) {
        // Implement the logic to calculate the g-cost from the starting position to (i, j)
        // This could be a simple distance calculation or more complex based on your game's rules
        // For a basic example, you could use the Manhattan distance:
        return Math.abs(i - currentRow) + Math.abs(j - currentColumn);
    }

*/

}
