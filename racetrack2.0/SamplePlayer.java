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

    public SamplePlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
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
        if (currentRow == track.length - 1) {
            // Check if moving right would hit a wall
            if (currentColumn + right.j < track[0].length && (track[currentRow][currentColumn + right.j] & RaceTrackGame.WALL) == 0) {
                // If it's safe to move right, do so
                return right;
            } else {
                // If moving right would hit a wall, move up if it's safe
                if (currentRow + up.i >= 0 && (track[currentRow + up.i][currentColumn] & RaceTrackGame.WALL) == 0) {
                    return up;
                }
            }
        }

// Your code to move down is incorrect because it checks for a non-zero value,
// which could mean any property, not specifically empty or wall. It should be:
        if (currentRow + down.i < track.length && (track[currentRow + down.i][currentColumn + down.j] & RaceTrackGame.WALL) == 0) {
            // It's safe to move down
            return down;
        }



        // Ha nincs fal a lefelé irányban, mozoghatunk lefelé
        return stay;
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
