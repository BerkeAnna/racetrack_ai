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

    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;
        this.goalPosition = findGoalPosition();
        this.visited = new HashSet<>();
    }

    private class Node {
        int i, j;
        Node parent;
        int g, h;
        boolean open, checked, solid;

        Node(int i, int j, Node parent, int g, int h) {
            this.i = i;
            this.j = j;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        int f() {
            return g + h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return i == node.i && j == node.j;
        }

        @Override
        public int hashCode() {
            return Objects.hash(i, j);
        }
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

    private boolean isGoal(Node node) {
        return node.i == goalPosition[0] && node.j == goalPosition[1];
    }

    private Direction reconstructPath(Node goal) {
        LinkedList<Direction> path = new LinkedList<>();
        Node current = goal;
        while (current.parent != null) {
            path.addFirst(new Direction(current.i - current.parent.i, current.j - current.parent.j));
            current = current.parent;
        }
        return path.isEmpty() ? RaceTrackGame.DIRECTIONS[0] : path.getFirst();
    }

    private boolean canMoveTo(int i, int j) {
        if (i < 0 || i >= track.length || j < 0 || j >= track[0].length) {
            return false;
        }
        return (track[i][j] & RaceTrackGame.WALL) == 0;
    }

    private int calculateHeuristic(int i, int j) {
        return Math.abs(i - goalPosition[0]) + Math.abs(j - goalPosition[1]);
    }

    // ... [A korábbi osztálydefiníciók]

    private void printHeuristicTable() {
        for (int i = 0; i < track.length; i++) {
            for (int j = 0; j < track[i].length; j++) {
                System.out.print(calculateHeuristic(i, j) + "\t");
            }
            System.out.println();
        }
    }



    @Override
    public Direction getDirection(long timeBudget) {
        Node startNode = new Node(state.i, state.j, null, 0, calculateHeuristic(state.i, state.j));
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::f));
        Set<Node> closedSet = new HashSet<>();
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();
            if (closedSet.contains(currentNode)) {
                continue;
            }
            closedSet.add(currentNode);

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

                    Node neighbor = new Node(nextRow, nextColumn, currentNode, currentNode.g + 1, calculateHeuristic(nextRow, nextColumn));
                    if (!closedSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return RaceTrackGame.DIRECTIONS[0]; // Default direction if path not found
    }

    private void printCostsTable() {
        // Temporary variables to simulate the state and other necessary data
        int currentRow = 0; // Example starting row
        int currentColumn = 0; // Example starting column
        int currentSpeedI = 0; // Example speed I component
        int currentSpeedJ = 0; // Example speed J component

        System.out.println("f-costs Table:");
        for (int i = 0; i < track.length; i++) {
            for (int j = 0; j < track[i].length; j++) {
                Node node = new Node(i, j, null, calculateGCost(i, j, currentRow, currentColumn, currentSpeedI, currentSpeedJ), calculateHeuristic(i, j));
                System.out.print(node.f() + "\t");
            }
            System.out.println();
        }

        System.out.println("\ng-costs Table:");
        for (int i = 0; i < track.length; i++) {
            for (int j = 0; j < track[i].length; j++) {
                Node node = new Node(i, j, null, calculateGCost(i, j, currentRow, currentColumn, currentSpeedI, currentSpeedJ), calculateHeuristic(i, j));
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



}
