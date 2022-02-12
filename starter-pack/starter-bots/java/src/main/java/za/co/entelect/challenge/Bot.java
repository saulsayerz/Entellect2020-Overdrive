package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        // Basic fix logic
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        int lanepos = myCar.position.lane;
        int rlane = 0, llane = 0;
        if (lanepos > 1 && lanepos < 4) {
            rlane = lanepos + 1;
            llane = lanepos - 1;
        } else if (lanepos == 1) {
            rlane = lanepos + 1;
            llane = lanepos;
        } else {
            llane = lanepos - 1;
            rlane = lanepos;
        }
        List<Object> rBlocks = getBlocksInFront(rlane, myCar.position.block, gameState);
        List<Object> lBlocks = getBlocksInFront(llane, myCar.position.block, gameState);
        List<Object> nextBlocks = blocks.subList(0, 1);

        //INI BAGIAN UNTUK NGECEK ADA CYBERTRUCK APA ENGGA 
        /*boolean isCT = false;
        boolean isCTLeft = false;
        boolean isCTRight = false;
        Lane Lane ;
        for (int i =0 ; i < myCar.speed ; i++) { 
            Lane = gameState.lanes.get(lanepos-1)[myCar.position.block-1 + i];
            if (Lane == null || Lane.terrain == Terrain.FINISH) {
                break;
            }
            if (Lane.cyberTruck){
                isCT = true;
            }
        } 

        if (lanepos > 1) {
            for (int i =0 ; i < myCar.speed ; i++) { 
                Lane = gameState.lanes.get(lanepos-2)[myCar.position.block-1 + i];
                if (Lane == null || Lane.terrain == Terrain.FINISH) {
                    break;
                }
                if (Lane.cyberTruck){
                    isCTLeft = true;
                }
            } 
        }

        if (lanepos < 4) {
            for (int i =0 ; i < myCar.speed ; i++) { 
                Lane = gameState.lanes.get(lanepos)[myCar.position.block-1 + i];
                if (Lane == null || Lane.terrain == Terrain.FINISH) {
                    break;
                }
                if (Lane.cyberTruck){
                    isCTRight = true;
                }
            } 
        }*/

        // Implement fix logic
        if (myCar.damage > 2) {
            if (myCar.position.block - opponent.position.block > 2 * (opponent.speed)) {
                return FIX;
            }

            if (myCar.damage > 4) {
                return FIX;
            }
        }

        // Accelerate first if going too slow
        // nanti ganti lagi
        if (myCar.speed <= 3) {
            return ACCELERATE;
        }

        /*Command TWEET = new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            return TWEET;
        }*/

        // Basic avoidance logic
        // WALL , MUD, OIL
        if (blocks.contains(Terrain.WALL)) {
            // check if the wall in the speed range
            if (blocks.size() <= myCar.speed) {
                // check if wall is in front of the car
                if (blocks.subList(0, 1).contains(Terrain.WALL)) {
                    // SEGMEN LURUS , PAKAI LIZARD
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                        return LIZARD;
                    }

                    // SEGMEN BELOK
                    // Lane 2 - 388u
                    if (myCar.position.lane > 1 && myCar.position.lane < 4) {
                        if (!rBlocks.subList(0, 1).contains(Terrain.WALL) &&
                                !rBlocks.subList(0, 1).contains(Terrain.WALL) &&
                                !rBlocks.subList(0, 1).contains(Terrain.WALL)) {
                            return TURN_LEFT;
                        }
                    }

                }

                // check if the wall is in the range of car speed
                if (blocks.subList(0, myCar.speed).contains(Terrain.WALL)) {
                    // CEK KANAN KIRI
                }

            }
            // pindah2 lane
            if (nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
                int i = random.nextInt(directionList.size());
                return directionList.get(i);
            }
        }

        // // lane contains cybertruck
        // if (Lane.cyberTruck) {
        // // blocks contains mud
        // if (blocks.contains(Terrain.MUD)) {
        // // LIZARD USE = priority 1
        // if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
        // return LIZARD;
        // }

        // // pindah2 lane
        // if (nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
        // int i = random.nextInt(directionList.size());
        // return directionList.get(i);
        // }
        // }
        // }

        // blocks contains mud
        if (blocks.contains(Terrain.MUD)) {
            // LIZARD USE = priority 1
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }

            // pindah2 lane
            if (nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
                int i = random.nextInt(directionList.size());
                return directionList.get(i);
            }
        }

        // blocks contains oil
        if (blocks.contains(Terrain.OIL_SPILL)) {
            // LIZARD USE = priority 1
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }

            // pindah2 lane
            if (nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
                int i = random.nextInt(directionList.size());
                return directionList.get(i);
            }
        }

        // Basic improvement logic
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        // Basic aggression logic
        if (myCar.speed == maxSpeed) {
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return OIL;
            }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                return EMP;
            }
        }

        return ACCELERATE;
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp : available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }
}
