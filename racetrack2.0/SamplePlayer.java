import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
import java.util.Random;
import java.util.*;

public class SamplePlayer extends RaceTrackPlayer {
    private int[][] track;
    private int[] goalPosition;
    private Set<Node> visited;

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

    private Direction reconstructTheGoodPath(Map<Node, Node> prev, Node current) {
        Node path = current;
        Node next = null;

        while (prev.containsKey(path)) {
            next = path;
            path = prev.get(path);
        }

        if (next != null) {
            int di = next.i - path.i;
            int dj = next.j - path.j;
            return new Direction(di, dj);
        }
        return RaceTrackGame.DIRECTIONS[0]; // Stay if no next step
    }

    private boolean canMoveTo(int i, int j) {
        if (i < 0 || i >= track.length || j < 0 || j >= track[0].length) {
            return false; // Out of bounds
        }
        if ((track[i][j] & RaceTrackGame.WALL) != 0) {
            return false; // Wall present
        }
        return !visited.contains(new Node(i, j, null, 0, 0));
    }

    private int calHeuristic(int i, int j) {
        if (goalPosition == null) {
            return Integer.MAX_VALUE;
        }
        int goalRow = goalPosition[0];
        int goalColumn = goalPosition[1];
        return Math.abs(i - goalRow) + Math.abs(j - goalColumn); // Manhattan distance
    }

    public Direction getDirection(long var1) {
        int currentRow = state.i;
        int currentColumn = state.j;

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::f));
        Set<Node> closedSet = new HashSet<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        visited.clear();
        Node start = new Node(currentRow, currentColumn, null, 0, calHeuristic(currentRow, currentColumn));
        visited.add(start);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (isGoal(current)) {
                return reconstructTheGoodPath(cameFrom, current);
            }

            closedSet.add(current);

            for (Direction direction : RaceTrackGame.DIRECTIONS) {
                int newI = current.i + direction.i;
                int newJ = current.j + direction.j;

                if (!canMoveTo(newI, newJ)) {
                    continue;
                }

                Node neighbor = new Node(newI, newJ, current, current.g + 1, calHeuristic(newI, newJ));

                if (closedSet.contains(neighbor) || (openSet.contains(neighbor) && neighbor.g >= current.g + 1)) {
                    continue;
                }

                cameFrom.put(neighbor, current);
                visited.add(neighbor);
                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                }
            }
        }

        return RaceTrackGame.DIRECTIONS[0]; // Stay if no path to goal
    }
}
