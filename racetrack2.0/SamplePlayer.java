//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;
import java.util.Random;
import game.racetrack.ui.CellAction;
import game.engine.ui.GameCanvas;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.*;

//BFS function in RaceTrackGame
public class SamplePlayer extends RaceTrackPlayer {
    private final RaceTrackPlayer[] players = new RaceTrackPlayer[2];
    private int[][] track;
    private Direction down = RaceTrackGame.DIRECTIONS[7];
    private Direction left = RaceTrackGame.DIRECTIONS[1];
    private Direction right = RaceTrackGame.DIRECTIONS[5];
    private Direction up = RaceTrackGame.DIRECTIONS[3];
    private Direction stay = RaceTrackGame.DIRECTIONS[0];
    private int[] goalPosition;
    private Set<Node> visited;

    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;
        this.goalPosition = findGoalPosition();
        this.visited = new HashSet<>();
        System.out.println("the goal posixxxxxx: " + goalPosition[0]);
        System.out.println("the goal posiy: " + goalPosition[1]);
        /*
        track-palya:
        System.out.println("A track[4][1]: " + track[4][1] ); --- fal: 2

        System.out.println("A track[1][5]: " + track[1][5] ); --- lepheto mezo:1
        System.out.println("A track[1][4]: " + track[1][4] ); --- user: 33
        csillag/pénz: 17
        cél: 5
        palya szele???: 0
        mar jart mezo: hullamvonal:



         */
      /*  for(int i = 0; i< track.length; i++){
            for(int j = 0; j<track[i].length; j++){

                System.out.print( track[i][j] + " " );
            }

            System.out.println();
        }
        System.out.println("A track[4][1]: " + track[4][1] );
        System.out.println("A track[3][1]: " + track[3][1] );
        System.out.println("A track[1][7]: " + track[1][7] );
        System.out.println("A track[1][5]: " + track[1][5] );
        System.out.println("A track[1][4]: " + track[1][4] );
        */

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
            if (next.j > path.j) return right;
            if (next.j < path.j) return left;
            if (next.i > path.i) return down;
            if (next.i < path.i) return up;
        }
        return stay; // Ha nincs következő lépés, maradunk
    }

    private boolean canMoveTo(int i, int j, Node current) {
        if (i < 0 || i >= track.length || j < 0 || j >= track[0].length) {
            return false; // A pálya szélein kívül esik
        }
        if ((track[i][j] & RaceTrackGame.WALL) != 0) {
            return false; // Fal van a mezőn
        }
        Node potentialMove = new Node(i, j, current, 0, 0);
        return !visited.contains(potentialMove); // Ellenőrizzük, hogy már jártunk-e ezen a mezőn
    }

    private int calHeuristic(int i, int j) {
        if (goalPosition == null) {
            return Integer.MAX_VALUE;
        }
        int goalRow = goalPosition[0];
        int goalColumn = goalPosition[1];
        return (int) Math.sqrt((i - goalRow) * (i - goalRow) + (j - goalColumn) * (j - goalColumn));
    }





    public Direction getDirection(long var1) {
        int currentRow = state.i;
        int currentColumn = state.j;

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::f));
        Set<Node> closedSet = new HashSet<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        visited.clear();
        Node start = new Node(currentRow, currentColumn, null, 0, calHeuristic(currentRow, currentColumn));
        visited.add(start); // Kezdőpont hozzáadása a meglátogatottakhoz
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

                if (!canMoveTo(newI, newJ, current)) {
                    continue;
                }

                Node neighbor = new Node(newI, newJ, current, current.g + 1, calHeuristic(newI, newJ));

                // Ellenőrizzük, hogy a szomszédos mezőt korábban már felfedeztük-e
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


        return stay; // Ha nincs elérhető cél, maradunk
    }
}
