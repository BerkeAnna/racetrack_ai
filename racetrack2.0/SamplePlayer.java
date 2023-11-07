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

//BFS function in RaceTrackGame
public class SamplePlayer extends RaceTrackPlayer {
    private final RaceTrackPlayer[] players = new RaceTrackPlayer[2];
    private int[][] track;
    private Direction down = RaceTrackGame.DIRECTIONS[7];
    private Direction left = RaceTrackGame.DIRECTIONS[1];
    private Direction right = RaceTrackGame.DIRECTIONS[5];
    private Direction up = RaceTrackGame.DIRECTIONS[3];
    private Direction stay = RaceTrackGame.DIRECTIONS[0];

    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.track = track;

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
        public Node parent;
        public int i, j, g, h;
        public int f() { return g + h; }

        public Node(Node parent, int i, int j, int g, int h) {
            this.parent = parent;
            this.i = i;
            this.j = j;
            this.g = g;
            this.h = h;
        }
    }
    private int[] findGoalPosition() {
        for(int i =0; i<track.length; i++){
            for(int j=0; j< track[i].length; j++){
                if ((track[i][j] & RaceTrackGame.FINISH) == RaceTrackGame.FINISH) {
                    return new int[] { i, j };
                }
            }
        }
        return  null;
    }
    private boolean isGoal(Node node) {
        int[] goalPosition = findGoalPosition();
        return node.i == goalPosition[0] && node.j == goalPosition[1];
    }
    private Direction reconstructTheGoodPath(HashMap<Node, Node> prev, Node current) {
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
    private boolean canMoveTo(int i, int j) {
        if (i < 0 || i >= track.length || j < 0 || j >= track[0].length) {
            return false; // A pálya szélein kívül esik
        }
        return (track[i][j] & RaceTrackGame.WALL) == 0; // Nincs fal
    }

    private int calHeuristic(int i, int j) {
        int[] goalPosition = findGoalPosition();
        if (goalPosition == null) {
            // Hibakezelés, ha a célmező nem található
            return Integer.MAX_VALUE;
        }
        int goalRow = goalPosition[0];
        int goalColumn = goalPosition[1];
        return Math.abs(i - goalRow) + Math.abs(j - goalColumn);
    }


    public Direction getDirection(long var1) {
        //----------------------------------------------------------------------

        int currentRow = state.i;
        int currentColumn = state.j;

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(Node::f));
        Set<Node> closed = new HashSet<>();
        HashMap<Node, Node> cameFrom = new HashMap<>();
        Node start = new Node(null, currentRow, currentColumn, 0, calHeuristic(currentRow, currentColumn));


        open.add(start);
        while (!open.isEmpty()) {
            Node current = open.poll();

            if (isGoal(current)) {
                return reconstructTheGoodPath(cameFrom, current);
            }

            closed.add(current);

            for (Direction direction : RaceTrackGame.DIRECTIONS) {
                int newI = current.i + direction.i;
                int newJ = current.j + direction.j;

                if (canMoveTo(newI, newJ)) {
                    Node neighbor = new Node(current, newI, newJ, current.g + 1, calHeuristic(newI, newJ));

                    if (closed.contains(neighbor)) {
                        continue;
                    }

                    if (!open.contains(neighbor) || open.peek().g > neighbor.g) {
                        cameFrom.put(neighbor, current);
                        open.add(neighbor);
                    }
                }
            }
        }



        //----------------------------------------------------------------------
        //hogy latom a mezoket?
        //ha latom, akkor generalni kellene egy cost-os tablat, amit latok,
        //utana itt kellene megoldani

        //1. le kell kerni a cella koordinatait, ahol all a jatekos
        //2. a kornyezo cellak lekerese / tabla lekerese
/*
        int currentRow = state.i;
        int currentColumn = state.j;

*/


        //most csak a pálya szélét nézi, azért nem lép feljebb, mert még nem ért el a pálya végére?
        if (currentRow + down.i >= track.length || (track[currentRow + down.i][currentColumn] & RaceTrackGame.WALL) != 0) {
            // If moving down would hit the bottom or a wall, then change direction.
            // This is where you would decide to move left, right, or up instead, or even stay.
            // For example, if it's safe to move right, then do so:
            if (currentColumn + right.j < track[0].length && (track[currentRow][currentColumn + right.j] & RaceTrackGame.WALL) == 0) {
                return right;
            }
            // If it's not safe to move right, check if it's safe to move up, and so on.
            // You'll need to add your logic here based on how you want the car to behave.
        } else {
            // If it's safe to move down, then move down.
            return down;
        }
        if (currentRow + up.i >= 0 && (track[currentRow + up.i][currentColumn] & RaceTrackGame.WALL) == 0) {
            return up;
        }

        // If none of the above conditions are met, the car will stay in place.
        // You might want to change this to some other behavior based on your game's rules.
        return stay;



        // Ha nincs fal a lefelé irányban, mozoghatunk lefelé

        /*
        LEPESIRANYOK
        --------------------------------------------
        RaceTrackGame.DIRECTIONS[0] - helyben marad
        RaceTrackGame.DIRECTIONS[1] - balra
        RaceTrackGame.DIRECTIONS[2] - balra fel srégan
        RaceTrackGame.DIRECTIONS[3] - felfelé
        RaceTrackGame.DIRECTIONS[4] - jobbra fel srégan
        RaceTrackGame.DIRECTIONS[5] => jobbra
        RaceTrackGame.DIRECTIONS[6] - jobbra le srégan
        RaceTrackGame.DIRECTIONS[7] - lefelé
        RaceTrackGame.DIRECTIONS[8] - balra le
        ------------------------------------------------
        RaceTrackGame.DIRECTIONS[9] - el se indult??
        RaceTrackGame.DIRECTIONS[10] - ez se
        RaceTrackGame.DIRECTIONS[11] - ez se
         */
    }
}
