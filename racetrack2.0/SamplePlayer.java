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

//BFS function in RaceTrackGame
public class SamplePlayer extends RaceTrackPlayer {
    private final RaceTrackPlayer[] players = new RaceTrackPlayer[2];
    private int[][] track;

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
        for(int i = 0; i< track.length; i++){
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
    }

    public Direction getDirection(long var1) {
        //hogy latom a mezoket?
        //ha latom, akkor generalni kellene egy cost-os tablat, amit latok,
        //utana itt kellene megoldani

        //1. le kell kerni a cella koordinatait, ahol all a jatekos
        //2. a kornyezo cellak lekerese / tabla lekerese

        int currentRow = state.i;
        int currentColumn = state.j;


        Direction down = RaceTrackGame.DIRECTIONS[7];
        Direction left = RaceTrackGame.DIRECTIONS[1];
        Direction right = RaceTrackGame.DIRECTIONS[5];
        Direction up = RaceTrackGame.DIRECTIONS[3];
        Direction stay = RaceTrackGame.DIRECTIONS[0];

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
