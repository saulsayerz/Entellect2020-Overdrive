package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 15;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command NOTHING = new DoNothingCommand();
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

        // COMPARE AMOUNT OF POWERUPS BEHIND EACH LANE
        int countPowerUp = 0;
        int countPowerUpLeft = 0;
        int countPowerUpRight = 0;
        for (int i = 0; i < min(blocks.size(), myCar.speed); i++) {
            if (blocks.get(i) == Terrain.BOOST ||
                    blocks.get(i) == Terrain.EMP ||
                    blocks.get(i) == Terrain.OIL_POWER ||
                    blocks.get(i) == Terrain.LIZARD ||
                    blocks.get(i) == Terrain.TWEET) {
                countPowerUp++;
            }
            if (rBlocks.get(i) == Terrain.BOOST ||
                    rBlocks.get(i) == Terrain.EMP ||
                    rBlocks.get(i) == Terrain.OIL_POWER ||
                    rBlocks.get(i) == Terrain.LIZARD ||
                    rBlocks.get(i) == Terrain.TWEET) {
                countPowerUpRight++;
            }
            if (lBlocks.get(i) == Terrain.BOOST ||
                    lBlocks.get(i) == Terrain.EMP ||
                    lBlocks.get(i) == Terrain.OIL_POWER ||
                    lBlocks.get(i) == Terrain.LIZARD ||
                    lBlocks.get(i) == Terrain.TWEET) {
                countPowerUpLeft++;
            }
        }

        /* CyberTruck check for Left Right and Current Lane */
        boolean isCT = false;
        boolean isCTLeft = false;
        boolean isCTRight = false;
        Lane Lane;
        for (int i = 0; i < maxSpeed; i++) {
            Lane = gameState.lanes.get(lanepos - 1)[6 + i];
            if (Lane == null || Lane.terrain == Terrain.FINISH) {
                break;
            }
            if (Lane.cyberTruck) {
                isCT = true;
            }
        }

        if (lanepos > 1) {
            for (int i = 0; i < maxSpeed; i++) {
                Lane = gameState.lanes.get(lanepos - 2)[6 + i];
                if (Lane == null || Lane.terrain == Terrain.FINISH) {
                    break;
                }
                if (Lane.cyberTruck) {
                    isCTLeft = true;
                }
            }
        }

        if (lanepos < 4) {
            for (int i = 0; i < maxSpeed; i++) {
                Lane = gameState.lanes.get(lanepos)[6 + i];
                if (Lane == null || Lane.terrain == Terrain.FINISH) {
                    break;
                }
                if (Lane.cyberTruck) {
                    isCTRight = true;
                }
            }
        }

        // COMPARE AMOUNT OF OBSTACLE BEHIND EACH LANE
        int countObstacle = 0;
        int countObstacleLeft = 0;
        int countObstacleRight = 0;
        for (int i = 0; i < min(blocks.size(), myCar.speed + 1); i++) {
            if (blocks.get(i) == Terrain.WALL || blocks.get(i) == Terrain.MUD || blocks.get(i) == Terrain.OIL_SPILL) {
                if (blocks.get(i) == Terrain.WALL) {
                    countObstacle += 2;
                } else
                    countObstacle++;
            }
            if (rBlocks.get(i) == Terrain.WALL || rBlocks.get(i) == Terrain.MUD
                    || rBlocks.get(i) == Terrain.OIL_SPILL) {

                if (rBlocks.get(i) == Terrain.WALL) {
                    countObstacleRight += 2;
                } else
                    countObstacleRight++;
            }
            if (lBlocks.get(i) == Terrain.WALL || lBlocks.get(i) == Terrain.MUD
                    || lBlocks.get(i) == Terrain.OIL_SPILL) {
                if (lBlocks.get(i) == Terrain.WALL) {
                    countObstacleLeft += 2;
                } else
                    countObstacleLeft++;
            }
        }

        if (isCT)
            countObstacle += 3;
        if (isCTLeft)
            countObstacleLeft += 3;
        if (isCTRight)
            countObstacleRight += 3;

        // Hitung benefit dari tiap lane
        int LeftBen = countPowerUpLeft - countObstacleLeft;
        int SelfBen = countPowerUp - countObstacle;
        int RightBen = countPowerUpRight - countObstacleRight;

        // Implement fix logic
        if (myCar.damage >= 2) {
            return FIX;
        }

        // Accelerate first if going too slow
        // nanti ganti lagi
        // if (myCar.speed + 1 <= 6) {
        // return ACCELERATE;
        // }

        Command TWEET = new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);

        // Basic avoidance logic
        // CT, WALL , OIL, MUD

        if(countObstacle==0){
            if(countObstacleLeft==0 && countPowerUpLeft>0) return TURN_LEFT;
            if(countObstacleRight==0 && countPowerUpRight>0) return TURN_RIGHT;
        }
        if(countObstacle >0){
            if(countObstacleLeft < countObstacle && countObstacleLeft < countObstacle){
                return TURN_LEFT;
            }
            if(countObstacleRight < countObstacle && countObstacleRight < countObstacle){
                return TURN_RIGHT;
            }
        }

        // Menggunakan powerup
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)
                && !(blocks.subList(0, min(blocks.size(), 15)).contains(Terrain.WALL) ||
                        blocks.subList(0, min(blocks.size(), 15)).contains(Terrain.OIL_SPILL) ||
                        blocks.subList(0, min(blocks.size(), 15)).contains(Terrain.MUD))) {
            return BOOST;
        } else if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            return TWEET;
        } else if (hasPowerUp(PowerUps.EMP, myCar.powerups)
                && (opponent.position.lane == lanepos || opponent.position.lane == lanepos + 1
                        || opponent.position.lane == lanepos - 1)
                && opponent.position.block > myCar.position.block) {
            return EMP;
        } else if (hasPowerUp(PowerUps.OIL, myCar.powerups) && (opponent.position.lane == lanepos)) {
            return OIL;
        }

        /* 0/1 ke 3 */
        if (myCar.speed == 0 || myCar.speed == 1) {
            if (!(blocks.subList(0, min(blocks.size(), 3)).contains(Terrain.WALL) ||
                    blocks.subList(0, min(blocks.size(), 3)).contains(Terrain.OIL_SPILL) ||
                    blocks.subList(0, min(blocks.size(), 3)).contains(Terrain.MUD))) {
                return ACCELERATE;
            }
        }
        /* 3/5 ke 6 */
        if (myCar.speed == 3 || myCar.speed == 5) {
            if (!(blocks.subList(0, min(blocks.size(), 6)).contains(Terrain.WALL) ||
                    blocks.subList(0, min(blocks.size(), 6)).contains(Terrain.OIL_SPILL) ||
                    blocks.subList(0, min(blocks.size(), 6)).contains(Terrain.MUD))) {
                return ACCELERATE;
            }
        }
        /* 6 ke 8 */
        if (myCar.speed == 6) {
            if (!(blocks.subList(0, min(blocks.size(), 8)).contains(Terrain.WALL) ||
                    blocks.subList(0, min(blocks.size(), 8)).contains(Terrain.OIL_SPILL) ||
                    blocks.subList(0, min(blocks.size(), 8)).contains(Terrain.MUD))) {
                return ACCELERATE;
            }
        }
        /* 8 ke 15 */
        if (myCar.speed == 8) {
            if (!(blocks.subList(0, min(blocks.size(), 15)).contains(Terrain.WALL) ||
                    blocks.subList(0, min(blocks.size(), 15)).contains(Terrain.OIL_SPILL) ||
                    blocks.subList(0, min(blocks.size(), 15)).contains(Terrain.MUD))) {
                return ACCELERATE;
            }
        }

        return NOTHING;
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