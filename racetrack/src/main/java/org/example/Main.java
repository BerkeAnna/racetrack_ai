package org.example;

import game.engine.Engine;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        int rand_int1 = ThreadLocalRandom.current().nextInt();
        String szam = Integer.toString(rand_int1);

        String[] args1 = {"0", "game.racetrack.RaceTrackGame", "11", "27", "5", "0.1", "10",
                    "1234567890", "1000", "SamplePlayer"};

        try{
            Engine.main(args1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}