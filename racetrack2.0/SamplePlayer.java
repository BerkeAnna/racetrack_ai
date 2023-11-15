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

    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;
        this.goalPosition = findGoalPosition();
        this.visited = new HashSet<>();
        printHeuristicTable();
        printCostsTable();
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

        private void printCostsTable() {
            for (int i = 0; i < track.length; i++) {
                for (int j = 0; j < track[i].length; j++) {
                    System.out.print(f() + "-" + g);
                }
                System.out.println();
            }
        }

        int f() {
            return g + h;
        }

        void setOpen(boolean isOpen) {
            this.open = isOpen;
        }

        void setChecked(boolean isChecked) {
            this.checked = isChecked;
        }

        void setSolid(boolean isSolid) {
            this.solid = isSolid;
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
        int currentRow = state.i;
        int currentColumn = state.j;
        int currentSpeedI = state.vi;  // A jelenlegi sebességvektor i komponense
        int currentSpeedJ = state.vj;  // A jelenlegi sebességvektor j komponense

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::f));
        Map<Node, Node> cameFrom = new HashMap<>();
        Node start = new Node(currentRow, currentColumn, null, 0, calculateHeuristic(currentRow, currentColumn));
        start.setOpen(true);
        openSet.add(start);
        visited.clear();

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (visited.contains(current)) {
                continue;
            }
            current.setChecked(true);
            visited.add(current);

            //System.out.println("Current Node (" + current.i + ", " + current.j + ") - g: " + current.g + ", f: " + current.f());


            if (isGoal(current)) {
                return reconstructPath(current);
            }

            for (int di = -1; di <= 1; di++) {
                for (int dj = -1; dj <= 1; dj++) {
                    int nextSpeedI = currentSpeedI + di;
                    int nextSpeedJ = currentSpeedJ + dj;
                    int nextRow = current.i + nextSpeedI;
                    int nextColumn = current.j + nextSpeedJ;

                    if (!canMoveTo(nextRow, nextColumn)) {
                        continue;
                    }

                    Node neighbor = new Node(nextRow, nextColumn, current, current.g + 1, calculateHeuristic(nextRow, nextColumn));
                    if (visited.contains(neighbor) || (cameFrom.containsKey(neighbor) && neighbor.g >= current.g + 1)) {
                        continue;
                    }

                    neighbor.setOpen(true);
                    cameFrom.put(neighbor, current);
                    openSet.add(neighbor);

                    //System.out.println("Neighbor Node (" + neighbor.i + ", " + neighbor.j + ") - g: " + neighbor.g + ", f: " + neighbor.f());
                }
            }
        }

        return RaceTrackGame.DIRECTIONS[0]; // Ha nincs út, válassz egy alapértelmezett irányt
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
