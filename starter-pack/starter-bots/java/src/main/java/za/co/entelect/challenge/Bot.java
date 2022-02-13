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

        // COMPARE AMOUNT OF OBSTACLE BEHIND EACH LANE
        int countObstacle = 0;
        int countObstacleLeft = 0;
        int countObstacleRight = 0;
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i) == Terrain.WALL || blocks.get(i) == Terrain.MUD
                    || blocks.get(i) == Terrain.OIL_SPILL) {
                countObstacle++;
            }
            if (rBlocks.get(i) == Terrain.WALL || rBlocks.get(i) == Terrain.MUD
                    || rBlocks.get(i) == Terrain.OIL_SPILL) {
                countObstacleRight++;
            }
            if (lBlocks.get(i) == Terrain.WALL || lBlocks.get(i) == Terrain.MUD
                    || lBlocks.get(i) == Terrain.OIL_SPILL) {
                countObstacleLeft++;
            }
        }

        // COMPARE AMOUNT OF POWERUPS BEHIND EACH LANE
        int countPowerUp = 0;
        int countPowerUpLeft = 0;
        int countPowerUpRight = 0;
        for (int i = 0; i < blocks.size(); i++) {
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

        // Implement fix logic
        if (myCar.damage >= 2) {
            // if (myCar.position.block - opponent.position.block > 2 * (opponent.speed)) {
            // return FIX;
            // }

            if (myCar.damage >= 2) {
                return FIX;
            }
        }

        // Accelerate first if going too slow
        // nanti ganti lagi
        if (myCar.speed <= 5) {
            return ACCELERATE;
        }

        Command TWEET = new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);

        // Basic avoidance logic
        // CT, WALL , OIL, MUD

        // Kena CT -> berubah jadi speed_state_1, kena damage 2, stuck dibelakang CT
        if (isCT) {
            // Kasus 1
            if (lanepos == 1) {
                if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                } else {
                    return TURN_RIGHT;
                }
            }
            // Kasus 2
            if (lanepos == 2 || lanepos == 3) {
                int LeftBen = countPowerUpLeft - countObstacleLeft ;
                int RightBen = countPowerUpRight - countObstacleRight ;
                // Kasus 2.1 Ada wall di kanan kiri
                if (rBlocks.contains(Terrain.WALL) && lBlocks.contains(Terrain.WALL)){
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                        return LIZARD;
                    } else if (RightBen >= LeftBen){
                        return TURN_RIGHT;
                    } else {
                        return TURN_LEFT;
                    }
                }
                // Kasus 2.1 Cek kanan ada wall atau ga
                if (rBlocks.contains(Terrain.WALL)){
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                        return LIZARD;
                    } else {
                        return TURN_LEFT;
                    }
                }
                // Kasus 2.3 Cek kiri ada wall atau ga
                if (lBlocks.contains(Terrain.WALL)){
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                        return LIZARD;
                    } else {
                        return TURN_RIGHT;
                    }
                }
                if (!rBlocks.contains(Terrain.WALL) && !lBlocks.contains(Terrain.WALL)){
                    if (LeftBen >= RightBen){
                        return TURN_LEFT;
                    } else {
                        return TURN_RIGHT;
                    }
                }
            }
            // Kasus 3
            if (lanepos == 4) {
                if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                } else {
                    return TURN_RIGHT;
                }
            }
        }

        // Kena Wall -> berubah jadi speed_state_1, kena damage 2
        if (blocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL)) {
            // SEGMEN LURUS , PAKAI LIZARD
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }
            
            if (myCar.position.lane == 1) {
                /************* 1. NO WALL AND CT ON RIGHT LANE ***************/
                if (!rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL) && !isCTRight) {
                    return TURN_RIGHT;
                } // TODO: Implement Check Damage Comparison

                /***** 2. WALL PRESENT ON RIGHTSIDE LANE *****/
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL)) {

                    /* 2.2. */
                    if (countObstacle < countObstacleRight) {
                        return ACCELERATE; // TODO: CHECK IF THIS WORKS
                    }
                    /* 2.1. */
                    else if (countObstacle > countObstacleRight) {
                        return TURN_RIGHT;
                    } else {
                        if (countPowerUp == countPowerUpRight) {
                            return TURN_RIGHT;
                        } // Lebih menguntungkan di lane tengah
                        else if (countPowerUp > countPowerUpRight) {
                            return ACCELERATE;
                        } 
                        else {
                            return TURN_RIGHT;
                        }
                    }
                }
            }
            /* LANE 2 AND LANE 3 */
            if (myCar.position.lane > 1 && myCar.position.lane < 4) {
                boolean noObstacleLeft = !lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL) &&
                        !lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD) &&
                        !lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL);
                boolean noObstacleRight = !rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL) &&
                        !rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD) &&
                        !rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL);

                /**************** NO OBSTACLES ON RIGHT LANE ***************/
                if (noObstacleRight) {
                    return TURN_RIGHT;
                }

                /**************** NO OBSTACLES ON LEFT LANE ***************/
                if (noObstacleLeft) {
                    return TURN_LEFT;
                }

                /****************
                 * OBSTACLES PRESENT ON BOTH LANES, CHECK OBJECT TYPES
                 ***************/
                /* Left - X Right - X (X equals to any obstacle) */
                if ((lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL)
                        && rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL)) ||
                        (lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD)
                                && rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD))
                        ||
                        (lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL)
                                && rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL))) {
                    if (myCar.position.lane == 2) {
                        return TURN_RIGHT;
                    } else {
                        return TURN_LEFT;
                    }
                }

                /* Left - WALL Right - MUD || OIL_SPILL* */
                if (lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL) &&
                        (rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL)
                                || rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD))) {
                    return TURN_RIGHT;
                }

                /* Left - MUD || OIL_SPILL* Right - WALL */
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL) &&
                        (lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL)
                                || lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD))) {
                    return TURN_LEFT;
                }

                /* Left - MUD* Right - OIL_SPILL */
                if (lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD) &&
                        (rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL))) {
                    return TURN_LEFT;
                }

                /* Left - OIL_SPILL Right - MUD* */
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD) &&
                        (lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL))) {
                    return TURN_RIGHT;
                }

            }

        }

        // Kena Oil -> speed berkurang ke state sebelumnya, skor berkurang 4, damage + 1
        if (blocks.contains(Terrain.OIL_SPILL)) {
            // Menghitung jumlah powerups di lane

            // Kasus 1 : Lane paling kiri, hanya bisa belok kanan
            if (lanepos == 1) {
                if (rBlocks.contains(Terrain.MUD) || rBlocks.contains(Terrain.WALL)
                        || rBlocks.contains(Terrain.OIL_SPILL) || isCTRight) {
                    boolean adaMud = rBlocks.contains(Terrain.MUD);
                    boolean adaWall = rBlocks.contains(Terrain.WALL);
                    boolean adaOil = rBlocks.contains(Terrain.OIL_SPILL);

                    // Kasus 1.1 : Jalur kanan ada mud tapi gaada oil
                    if (adaMud && !adaOil && !adaWall && !isCTRight) {
                        // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                        if (rBlocks.contains(Terrain.LIZARD) || rBlocks.contains(Terrain.OIL_POWER)
                                || rBlocks.contains(Terrain.TWEET) || rBlocks.contains(Terrain.EMP)
                                || rBlocks.contains(Terrain.BOOST)) {
                            return TURN_RIGHT;
                        } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                || blocks.contains(Terrain.BOOST)) {
                            return ACCELERATE;
                        } else {
                            return TURN_RIGHT;
                        }
                    }
                    // Kasus 1.2 : Tidak ada obstacle di kanan
                    if (!adaMud && !adaOil && !adaWall && !isCTRight) {
                        return TURN_RIGHT;
                    }
                }
            }
            // Kasus 2 : Lane 2
            else if (lanepos == 2) {
                // Kasus 2.1 Ada CT/Wall di kanan
                if (isCTRight || rBlocks.contains(Terrain.WALL)) {
                    // Cek kiri --> ada mud tapi gaada yang lain
                    if (lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                            && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft) {
                        // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                        if (lBlocks.contains(Terrain.LIZARD) || lBlocks.contains(Terrain.OIL_POWER)
                                || lBlocks.contains(Terrain.TWEET) || lBlocks.contains(Terrain.EMP)
                                || lBlocks.contains(Terrain.BOOST)) {
                            return TURN_LEFT;
                        } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                || blocks.contains(Terrain.BOOST)) {
                            return ACCELERATE;
                        } else {
                            return TURN_LEFT;
                        }
                    }
                }
                // Kasus 2.2 Ada CT/wall di kiri
                if (isCTLeft || lBlocks.contains(Terrain.WALL)) {
                    // Cek kanan --> ada mud tapi gaada yang lain
                    if (rBlocks.contains(Terrain.MUD) && !rBlocks.contains(Terrain.WALL)
                            && !rBlocks.contains(Terrain.OIL_SPILL) && !isCTRight) {
                        // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                        if (rBlocks.contains(Terrain.LIZARD) || rBlocks.contains(Terrain.OIL_POWER)
                                || rBlocks.contains(Terrain.TWEET) || rBlocks.contains(Terrain.EMP)
                                || rBlocks.contains(Terrain.BOOST)) {
                            return TURN_RIGHT;
                        } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                || blocks.contains(Terrain.BOOST)) {
                            return ACCELERATE;
                        } else {
                            return TURN_RIGHT;
                        }
                    }
                }
                // Kasus 2.3 Ada mud di kanan
                if (rBlocks.contains(Terrain.MUD)) {
                    // Kasus 2.3.1 Kiri ada CT/Wall
                    if (isCTLeft || lBlocks.contains(Terrain.WALL)) {
                        // Cek kanan karena gamungkin belok kiri
                        if (rBlocks.contains(Terrain.MUD) && !rBlocks.contains(Terrain.WALL)
                                && !rBlocks.contains(Terrain.OIL_SPILL) && !isCTRight) {
                            // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                            if (rBlocks.contains(Terrain.LIZARD) || rBlocks.contains(Terrain.OIL_POWER)
                                    || rBlocks.contains(Terrain.TWEET) || rBlocks.contains(Terrain.EMP)
                                    || rBlocks.contains(Terrain.BOOST)) {
                                return TURN_RIGHT;
                            } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                    || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                    || blocks.contains(Terrain.BOOST)) {
                                return ACCELERATE;
                            } else {
                                return TURN_RIGHT;
                            }

                        }
                    }
                    // Kasus 2.3.2 Kanan ada CT / Wall
                    if (isCTRight || lBlocks.contains(Terrain.WALL)) {
                        // Cek kiri karena gamungkin belok kanan
                        if (lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                                && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft) {
                            // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                            if (lBlocks.contains(Terrain.LIZARD) || lBlocks.contains(Terrain.OIL_POWER)
                                    || lBlocks.contains(Terrain.TWEET) || lBlocks.contains(Terrain.EMP)
                                    || lBlocks.contains(Terrain.BOOST)) {
                                return TURN_LEFT;
                            } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                    || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                    || blocks.contains(Terrain.BOOST)) {
                                return ACCELERATE;
                            } else {
                                return TURN_LEFT;
                            }

                        }
                    }
                }
                // Kasus 2.4 Ada mud di kiri
                if (lBlocks.contains(Terrain.MUD)) {
                    // Kasus 2.4.1 Kanan ada CT/Wall
                    if (isCTRight || lBlocks.contains(Terrain.WALL)) {
                        // Cek kiri karena gamungkin belok kanan
                        if (lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                                && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft) {
                            // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                            if (lBlocks.contains(Terrain.LIZARD) || lBlocks.contains(Terrain.OIL_POWER)
                                    || lBlocks.contains(Terrain.TWEET) || lBlocks.contains(Terrain.EMP)
                                    || lBlocks.contains(Terrain.BOOST)) {
                                return TURN_LEFT;
                            } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                    || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                    || blocks.contains(Terrain.BOOST)) {
                                return ACCELERATE;
                            } else {
                                return TURN_LEFT;
                            }

                        }
                    }
                    // Kasus 2.4.2 Kiri ada CT/Wall
                    if (isCTLeft || lBlocks.contains(Terrain.WALL)) {
                        // Cek kanan karena gamungkin belok kiri
                        if (rBlocks.contains(Terrain.MUD) && !rBlocks.contains(Terrain.WALL)
                                && !rBlocks.contains(Terrain.OIL_SPILL) && !isCTRight) {
                            // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                            if (rBlocks.contains(Terrain.LIZARD) || rBlocks.contains(Terrain.OIL_POWER)
                                    || rBlocks.contains(Terrain.TWEET) || rBlocks.contains(Terrain.EMP)
                                    || rBlocks.contains(Terrain.BOOST)) {
                                return TURN_RIGHT;
                            } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                    || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                    || blocks.contains(Terrain.BOOST)) {
                                return ACCELERATE;
                            } else {
                                return TURN_RIGHT;
                            }
                        }
                    }
                }
                // Kasus 2.5 Kanan / kiri aman
                boolean kananaman = !rBlocks.contains(Terrain.MUD) && !rBlocks.contains(Terrain.WALL)
                        && !rBlocks.contains(Terrain.OIL_SPILL) && !isCTRight;
                boolean kiriaman = !lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                        && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft;
                if (kananaman && !kiriaman) {
                    return TURN_RIGHT;
                } else if (kiriaman && !kananaman) {
                    return TURN_LEFT;
                } else if (kananaman && kiriaman) {
                    if (rBlocks.contains(Terrain.LIZARD) || rBlocks.contains(Terrain.OIL_POWER)
                            || rBlocks.contains(Terrain.TWEET) || rBlocks.contains(Terrain.EMP)
                            || rBlocks.contains(Terrain.BOOST)) {
                        return TURN_RIGHT;
                    }
                    if (lBlocks.contains(Terrain.LIZARD) || lBlocks.contains(Terrain.OIL_POWER)
                            || lBlocks.contains(Terrain.TWEET) || lBlocks.contains(Terrain.EMP)
                            || lBlocks.contains(Terrain.BOOST)) {
                        return TURN_LEFT;
                    }
                    if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                            || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                            || blocks.contains(Terrain.BOOST)) {
                        return ACCELERATE;
                    }
                }
            }
            // Kasus 3 : Lane 3
            else if (lanepos == 3) {
                // Kasus 3.1 Ada CT/wall di kiri
                if (isCTLeft || lBlocks.contains(Terrain.WALL)) {
                    // Cek kanan
                    if (rBlocks.contains(Terrain.MUD) && !rBlocks.contains(Terrain.WALL)
                            && !rBlocks.contains(Terrain.OIL_SPILL) && !isCTRight) {
                        // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                        if (rBlocks.contains(Terrain.LIZARD) || rBlocks.contains(Terrain.OIL_POWER)
                                || rBlocks.contains(Terrain.TWEET) || rBlocks.contains(Terrain.EMP)
                                || rBlocks.contains(Terrain.BOOST)) {
                            return TURN_RIGHT;
                        } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                || blocks.contains(Terrain.BOOST)) {
                            return ACCELERATE;
                        } else {
                            return TURN_RIGHT;
                        }
                    }
                }
                // Kasus 3.2 Ada CT/Wall di kanan
                if (isCTRight || rBlocks.contains(Terrain.WALL)) {
                    // Cek kiri
                    if (lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                            && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft) {
                        // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                        if (lBlocks.contains(Terrain.LIZARD) || lBlocks.contains(Terrain.OIL_POWER)
                                || lBlocks.contains(Terrain.TWEET) || lBlocks.contains(Terrain.EMP)
                                || lBlocks.contains(Terrain.BOOST)) {
                            return TURN_LEFT;
                        } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                || blocks.contains(Terrain.BOOST)) {
                            return ACCELERATE;
                        } else {
                            return TURN_LEFT;
                        }
                    }
                }
                // Kasus 3.3 Ada mud di kiri
                if (lBlocks.contains(Terrain.MUD)) {
                    // Kasus 3.3.1 Kanan ada CT/Wall
                    if (isCTRight || lBlocks.contains(Terrain.WALL)) {
                        // Cek kiri karena gamungkin belok kanan
                        if (lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                                && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft) {
                            // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                            if (lBlocks.contains(Terrain.LIZARD) || lBlocks.contains(Terrain.OIL_POWER)
                                    || lBlocks.contains(Terrain.TWEET) || lBlocks.contains(Terrain.EMP)
                                    || lBlocks.contains(Terrain.BOOST)) {
                                return TURN_LEFT;
                            } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                    || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                    || blocks.contains(Terrain.BOOST)) {
                                return ACCELERATE;
                            } else {
                                return TURN_LEFT;
                            }
                        }
                    }
                    // Kasus 3.3.2 Kiri ada CT/Wall
                    if (isCTLeft || lBlocks.contains(Terrain.WALL)) {
                        // Cek kanan karena gamungkin belok kiri
                        if (rBlocks.contains(Terrain.MUD) && !rBlocks.contains(Terrain.WALL)
                                && !rBlocks.contains(Terrain.OIL_SPILL) && !isCTRight) {
                            // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                            if (rBlocks.contains(Terrain.LIZARD) || rBlocks.contains(Terrain.OIL_POWER)
                                    || rBlocks.contains(Terrain.TWEET) || rBlocks.contains(Terrain.EMP)
                                    || rBlocks.contains(Terrain.BOOST)) {
                                return TURN_RIGHT;
                            } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                    || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                    || blocks.contains(Terrain.BOOST)) {
                                return ACCELERATE;
                            } else {
                                return TURN_RIGHT;
                            }
                        }
                    }
                }
                // Kasus 3.4 Ada mud di kanan
                if (rBlocks.contains(Terrain.MUD)) {
                    // Kasus 3.4.1 Kiri ada CT/Wall
                    if (isCTLeft || lBlocks.contains(Terrain.WALL)) {
                        // Cek kanan karena gamungkin belok kiri
                        if (rBlocks.contains(Terrain.MUD) && !rBlocks.contains(Terrain.WALL)
                                && !rBlocks.contains(Terrain.OIL_SPILL) && !isCTRight) {
                            // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                            if (rBlocks.contains(Terrain.LIZARD) || rBlocks.contains(Terrain.OIL_POWER)
                                    || rBlocks.contains(Terrain.TWEET) || rBlocks.contains(Terrain.EMP)
                                    || rBlocks.contains(Terrain.BOOST)) {
                                return TURN_RIGHT;
                            } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                    || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                    || blocks.contains(Terrain.BOOST)) {
                                return ACCELERATE;
                            } else {
                                return TURN_RIGHT;
                            }
                        }
                    }
                    // Kasus 3.4.2 Kanan ada CT / Wall
                    if (isCTRight || lBlocks.contains(Terrain.WALL)) {
                        // Cek kiri karena gamungkin belok kanan
                        if (lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                                && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft) {
                            // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                            if (lBlocks.contains(Terrain.LIZARD) || lBlocks.contains(Terrain.OIL_POWER)
                                    || lBlocks.contains(Terrain.TWEET) || lBlocks.contains(Terrain.EMP)
                                    || lBlocks.contains(Terrain.BOOST)) {
                                return TURN_LEFT;
                            } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                                    || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                                    || blocks.contains(Terrain.BOOST)) {
                                return ACCELERATE;
                            } else {
                                return TURN_LEFT;
                            }
                        }
                    }
                }
                // Kasus 3.5 Kanan / kiri aman
                boolean kananaman = !rBlocks.contains(Terrain.MUD) && !rBlocks.contains(Terrain.WALL)
                        && !rBlocks.contains(Terrain.OIL_SPILL) && !isCTRight;
                boolean kiriaman = !lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                        && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft;
                if (kiriaman && !kananaman) {
                    return TURN_LEFT;
                } else if (kananaman && !kiriaman) {
                    return TURN_RIGHT;
                } else if (kananaman && kiriaman) {
                    if (lBlocks.contains(Terrain.LIZARD) || lBlocks.contains(Terrain.OIL_POWER)
                            || lBlocks.contains(Terrain.TWEET) || lBlocks.contains(Terrain.EMP)
                            || lBlocks.contains(Terrain.BOOST)) {
                        return TURN_LEFT;
                    }
                    if (rBlocks.contains(Terrain.LIZARD) || rBlocks.contains(Terrain.OIL_POWER)
                            || rBlocks.contains(Terrain.TWEET) || rBlocks.contains(Terrain.EMP)
                            || rBlocks.contains(Terrain.BOOST)) {
                        return TURN_RIGHT;
                    }
                    if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                            || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                            || blocks.contains(Terrain.BOOST)) {
                        return ACCELERATE;
                    }
                }
            }
            // Kasus 4 : Lane paling kanan, hanya bisa belok kiri
            else {
                // Kasus 4.1 : Ada mud di kiri
                if (lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                        && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft) {
                    // Prioritas jalur yang lebih menguntungkan (ada powerup karena skor jadi balik)
                    if (lBlocks.contains(Terrain.LIZARD) || lBlocks.contains(Terrain.OIL_POWER)
                            || lBlocks.contains(Terrain.TWEET) || lBlocks.contains(Terrain.EMP)
                            || lBlocks.contains(Terrain.BOOST)) {
                        return TURN_LEFT;
                    } else if (blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.OIL_POWER)
                            || blocks.contains(Terrain.TWEET) || blocks.contains(Terrain.EMP)
                            || blocks.contains(Terrain.BOOST)) {
                        return ACCELERATE;
                    } else {
                        return TURN_LEFT;
                    }
                }
                // Kasus 4.2 : Tidak ada obstacle di kiri
                if (!lBlocks.contains(Terrain.MUD) && !lBlocks.contains(Terrain.WALL)
                        && !lBlocks.contains(Terrain.OIL_SPILL) && !isCTLeft) {
                    return TURN_LEFT;
                }
            }
        }

        // Kena Mud -> speed berkurang ke state sebelumnya, skor berkurang 3, damage + 1
        if (blocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD)) { // ini udah diatasin mudnya
                                                                                        // dalam jangkauan speed apa ga
            // LIZARD USE = priority 1
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }

            // pindah2 lane. Kalau misal lane sebelah ada obstacles, mending nubruk mud aja
            // / dibiarin soalnya dah paling ringan ya ga
            boolean kiriaman = true; // dua variabel ini ngecek sebelah kiri atau kanannya jg ada obstacles apa ga
            boolean kananaman = true;
            if (lanepos == 1) { // kasus dia di paling atas, hanya mungkin turn right
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD)
                        || rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL)
                        || rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL) || isCTRight) {
                    kananaman = false;
                }
                if (kananaman) {
                    return TURN_RIGHT;
                }
            }
            if (lanepos == 4) { // kasus dia di paling bawah, hanya mungkin turn left
                if (lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD)
                        || lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL)
                        || lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL) || isCTLeft) {
                    kiriaman = false;
                }
                if (kiriaman) {
                    return TURN_LEFT;
                }
            }
            if (lanepos == 2 || lanepos == 3) { // ini bisa ke kiri bisa ke kanan
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD)
                        || rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL)
                        || rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL) || isCTRight) {
                    kananaman = false;
                }
                if (lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.MUD)
                        || lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_SPILL)
                        || lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.WALL) || isCTLeft) {
                    kiriaman = false;
                }
                if (kiriaman && !kananaman) {
                    return TURN_LEFT;
                }
                if (!kiriaman && kananaman) {
                    return TURN_RIGHT;
                }
                if (kananaman && kiriaman) { // cek mana yang ada powernya
                    boolean kiriadapower = false;
                    boolean kananadapower = false;
                    if (rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_POWER)
                            || rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.LIZARD)
                            || rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.EMP)
                            || rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.BOOST)
                            || rBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.TWEET)) {
                        kananadapower = true;
                    }
                    if (lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.OIL_POWER)
                            || lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.LIZARD)
                            || lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.EMP)
                            || lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.BOOST)
                            || lBlocks.subList(0, min(blocks.size(), myCar.speed)).contains(Terrain.TWEET)) {
                        kiriadapower = true;
                    }
                    if (kiriadapower && !kananadapower) {
                        return TURN_LEFT;
                    }
                    if (!kiriadapower && kananadapower) {
                        return TURN_RIGHT;
                    }
                    if (kiriadapower && kananadapower) { // KASUS GINI aku asumsi ambil lane yang bukan pojok aja,
                                                         // soalnya di pojokan itu membatasi gerak. Kalau di tegnah kan
                                                         // enak bisa kiri/kanan
                        if (lanepos == 2) {
                            return TURN_RIGHT;
                        }
                        if (lanepos == 3) {
                            return TURN_LEFT;
                        }
                    }
                }
            }
        }

        // Jalur amanBISA ATAU TIDAK PERLU MENGHINDARI OBST

        // Menggunakan powerup
        if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) && (isCT || blocks.contains(Terrain.WALL))) {
            return LIZARD;
        } else if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        } else if (hasPowerUp(PowerUps.EMP, myCar.powerups)
                && (opponent.position.lane == lanepos || opponent.position.lane == lanepos + 1 || opponent.position.lane == lanepos - 1)
                && opponent.position.block > myCar.position.block
                && opponent.position.block < myCar.position.block + 20) {
            return EMP;
        } else if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            return TWEET;
        } else if (hasPowerUp(PowerUps.OIL, myCar.powerups) && (opponent.position.lane == lanepos)) {
            return OIL;
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
