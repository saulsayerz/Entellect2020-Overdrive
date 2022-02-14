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
            countObstacle += 2;
        if (isCTLeft)
            countObstacleLeft += 2;
        if (isCTRight)
            countObstacleRight += 2;

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
                // Kasus 2.1 Ada wall di kanan kiri
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)
                        && lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)) {
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                        return LIZARD;
                    } else if (RightBen >= LeftBen) {
                        return TURN_RIGHT;
                    } else {
                        return TURN_LEFT;
                    }
                }
                // Kasus 2.1 Cek kanan ada wall atau ga
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)) {
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                        return LIZARD;
                    } else {
                        return TURN_LEFT;
                    }
                }
                // Kasus 2.3 Cek kiri ada wall atau ga
                if (lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)) {
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                        return LIZARD;
                    } else {
                        return TURN_RIGHT;
                    }
                }
                if (!rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)
                        && !lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)) {
                    if (LeftBen >= RightBen) {
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
                    return TURN_LEFT;
                }
            }
        }

        // Kena Wall -> berubah jadi speed_state_1, kena damage 2
        if (blocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)) {
            // SEGMEN LURUS , PAKAI LIZARD
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }

            if (myCar.position.lane == 1) {
                /************* 1. NO WALL AND CT ON RIGHT LANE ***************/
                if (!rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL) && !isCTRight) {
                    if (countObstacleRight < countObstacle) {
                        return TURN_RIGHT;
                    }
                }

                /***** 2. WALL PRESENT ON RIGHTSIDE LANE *****/
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)) {
                    /* 2.1. */
                    if (countObstacle > countObstacleRight) {
                        return TURN_RIGHT;
                    } else if (countObstacle == countObstacleRight) {
                        if (countPowerUp < countPowerUpRight) {
                            return TURN_RIGHT;
                        }
                    }
                }
            }
            /* LANE 2 AND LANE 3 */
            if (myCar.position.lane > 1 && myCar.position.lane < 4) {

                if (countObstacleLeft == 0) {
                    if (countObstacleRight == 0) {
                        if (countPowerUpLeft >= countPowerUpRight) {
                            return TURN_LEFT;
                        } else {
                            return TURN_RIGHT;
                        }
                    } else {
                        return TURN_LEFT;
                    }
                } else {
                    if (countObstacleRight == 0) {
                        return TURN_RIGHT;
                    } else {
                        if (countObstacleLeft > countObstacleRight) {
                            return TURN_RIGHT;
                        } else {
                            return TURN_LEFT;
                        }
                    }
                }
            }

            /* LANE 4 */
            if (myCar.position.lane == 4) {
                if (!lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL) && !isCTLeft) {
                    if (countObstacleLeft < countObstacle) {
                        return TURN_LEFT;
                    }
                }

                /***** 2. WALL PRESENT ON RIGHTSIDE LANE *****/
                if (lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)) {
                    /* 2.1. */
                    if (countObstacle > countObstacleLeft) {
                        return TURN_LEFT;
                    } else if (countObstacle == countObstacleLeft) {
                        if (countPowerUp < countPowerUpLeft) {
                            return TURN_LEFT;
                        }
                    }
                }
            }
        }

        // Kena Oil -> speed berkurang ke state sebelumnya, skor berkurang 4, damage + 1
        if (blocks.subList(0, min(blocks.size(),
                myCar.speed + 1)).contains(Terrain.OIL_SPILL) || blocks.subList(0,
                        min(blocks.size(), myCar.speed + 1)).contains(Terrain.MUD)) {
            // Menghitung jumlah powerups di lane

            // Kasus 1 : Lane paling kiri, hanya bisa belok kanan
            if (lanepos == 1) {
                if (!isCTRight && !rBlocks.contains(Terrain.WALL)) {
                    if (RightBen > SelfBen) {
                        return TURN_RIGHT;
                    }
                }
            }
            // Kasus 2 : Lane 2
            else if (lanepos == 2) {
                // Kasus 2.1 Ada CT/Wall di kanan
                if (isCTRight || rBlocks.subList(0, min(blocks.size(),
                        myCar.speed + 1)).contains(Terrain.WALL)) {
                    if (LeftBen > SelfBen) {
                        return TURN_LEFT;
                    }
                }
                // Kasus 2.2 Ada CT/wall di kiri
                if (isCTLeft || lBlocks.subList(0, min(blocks.size(),
                        myCar.speed + 1)).contains(Terrain.WALL)) {
                    // Cek kanan --> ada mud tapi gaada yang lain
                    if (RightBen > SelfBen) {
                        return TURN_RIGHT;
                    }
                }
                // Kasus 2.3 Ada mud di kanan
                if (rBlocks.subList(0, min(blocks.size(),
                        myCar.speed + 1)).contains(Terrain.MUD)) {
                    // Kasus 2.3.1 Kiri ada CT/Wall
                    if (isCTLeft || lBlocks.subList(0, min(blocks.size(),
                            myCar.speed + 1)).contains(Terrain.WALL)) {
                        // Cek kanan karena gamungkin belok kiri
                        if (RightBen > SelfBen) {
                            return TURN_RIGHT;
                        }
                    }
                    // Kasus 2.3.2 Kanan ada CT / Wall
                    if (isCTRight || lBlocks.subList(0, min(blocks.size(),
                            myCar.speed + 1)).contains(Terrain.WALL)) {
                        // Cek kiri karena gamungkin belok kanan
                        if (LeftBen > SelfBen) {
                            return TURN_LEFT;
                        }
                    }
                }
                // Kasus 2.4 Ada mud di kiri
                if (lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.MUD)) {
                    // Kasus 2.4.1 Kanan ada CT/Wall
                    if (isCTRight || rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)) {
                        // Cek kiri karena gamungkin belok kanan
                        if (LeftBen > SelfBen) {
                            return TURN_LEFT;
                        }
                    }
                    // Kasus 2.4.2 Kiri ada CT/Wall
                    if (isCTLeft || lBlocks.subList(0, min(blocks.size(),
                            myCar.speed + 1)).contains(Terrain.WALL)) {
                        // Cek kanan karena gamungkin belok kiri
                        if (RightBen > SelfBen) {
                            return TURN_RIGHT;
                        }
                    }
                }
                // Kasus 2.5 Kanan / kiri aman
                if (countObstacleLeft == 0 && countObstacleRight == 0) {
                    if (LeftBen > SelfBen) {
                        if (LeftBen >= RightBen) {
                            return TURN_LEFT;
                        } else {
                            return TURN_RIGHT;
                        }
                    }
                    if (RightBen > SelfBen) {
                        if (LeftBen <= RightBen) {
                            return TURN_LEFT;
                        } else {
                            return TURN_RIGHT;
                        }
                    }
                }
            }
            // Kasus 3 : Lane 3
            else if (lanepos == 3) {
                // Kasus 3.1 Ada CT/wall di kiri
                if (isCTLeft || lBlocks.subList(0, min(blocks.size(),
                        myCar.speed + 1)).contains(Terrain.WALL)) {
                    // Cek kanan
                    if (RightBen > SelfBen) {
                        return TURN_RIGHT;
                    }
                }
                // Kasus 3.2 Ada CT/Wall di kanan
                if (isCTRight || rBlocks.subList(0, min(blocks.size(),
                        myCar.speed + 1)).contains(Terrain.WALL)) {
                    // Cek kiri
                    if (LeftBen > SelfBen) {
                        return TURN_LEFT;
                    }
                }
                // Kasus 3.3 Ada mud di kiri
                if (lBlocks.subList(0, min(blocks.size(),
                        myCar.speed + 1)).contains(Terrain.MUD)) {
                    // Kasus 3.3.1 Kanan ada CT/Wall
                    if (isCTRight || lBlocks.subList(0, min(blocks.size(),
                            myCar.speed + 1)).contains(Terrain.WALL)) {
                        // Cek kiri karena gamungkin belok kanan
                        if (LeftBen > SelfBen) {
                            return TURN_LEFT;
                        }
                    }
                    // Kasus 3.3.2 Kiri ada CT/Wall
                    if (isCTLeft || lBlocks.subList(0, min(blocks.size(),
                            myCar.speed + 1)).contains(Terrain.WALL)) {
                        // Cek kanan karena gamungkin belok kiri
                        if (RightBen > SelfBen) {
                            return TURN_RIGHT;
                        }
                    }
                }
                // Kasus 3.4 Ada mud di kanan
                if (rBlocks.contains(Terrain.MUD)) {
                    // Kasus 3.4.1 Kiri ada CT/Wall
                    if (isCTLeft || lBlocks.subList(0, min(blocks.size(),
                            myCar.speed + 1)).contains(Terrain.WALL)) {
                        // Cek kanan karena gamungkin belok kiri
                        if (RightBen > SelfBen) {
                            return TURN_RIGHT;
                        }
                    }
                    // Kasus 3.4.2 Kanan ada CT / Wall
                    if (isCTRight || lBlocks.subList(0, min(blocks.size(),
                            myCar.speed + 1)).contains(Terrain.WALL)) {
                        // Cek kiri karena gamungkin belok kanan
                        if (LeftBen > SelfBen) {
                            return TURN_LEFT;
                        }
                    }
                }
                // Kasus 3.5 Kanan / kiri aman
                if (countObstacleLeft == 0 && countObstacleRight == 0) {
                    if (LeftBen > SelfBen) {
                        if (LeftBen >= RightBen) {
                            return TURN_LEFT;
                        } else {
                            return TURN_RIGHT;
                        }
                    }
                    if (RightBen > SelfBen) {
                        if (LeftBen <= RightBen) {
                            return TURN_LEFT;
                        } else {
                            return TURN_RIGHT;
                        }
                    }
                }
            }
            // Kasus 4 : Lane paling kanan, hanya bisa belok kiri
            else {
                if (!isCTLeft && !lBlocks.subList(0, min(blocks.size(),
                        myCar.speed + 1)).contains(Terrain.WALL)) {
                    if (LeftBen > SelfBen) {
                        return TURN_LEFT;
                    }
                }
            }
        }

        // Kena Mud -> speed berkurang ke state sebelumnya, skor berkurang 3, damage
        // + 1
        if (blocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.MUD)
                || blocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.OIL_SPILL)) { // ini udah
            // diatasin mudnya
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
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.MUD)
                        || rBlocks.subList(0, min(blocks.size(),
                                myCar.speed + 1)).contains(Terrain.OIL_SPILL)
                        || rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)
                        || isCTRight) {
                    kananaman = false;
                }
                if (kananaman) {
                    return TURN_RIGHT;
                }
            }
            if (lanepos == 4) { // kasus dia di paling bawah, hanya mungkin turn left
                if (lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.MUD)
                        || lBlocks.subList(0, min(blocks.size(),
                                myCar.speed + 1)).contains(Terrain.OIL_SPILL)
                        || lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)
                        || isCTLeft) {
                    kiriaman = false;
                }
                if (kiriaman) {
                    return TURN_LEFT;
                }
            }
            if (lanepos == 2 || lanepos == 3) { // ini bisa ke kiri bisa ke kanan
                if (rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.MUD)
                        || rBlocks.subList(0, min(blocks.size(),
                                myCar.speed + 1)).contains(Terrain.OIL_SPILL)
                        || rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)
                        || isCTRight) {
                    kananaman = false;
                }
                if (lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.MUD)
                        || lBlocks.subList(0, min(blocks.size(),
                                myCar.speed + 1)).contains(Terrain.OIL_SPILL)
                        || lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.WALL)
                        || isCTLeft) {
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
                    if (rBlocks.subList(0, min(blocks.size(),
                            myCar.speed + 1)).contains(Terrain.OIL_POWER)
                            || rBlocks.subList(0, min(blocks.size(),
                                    myCar.speed + 1)).contains(Terrain.LIZARD)
                            || rBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.EMP)
                            || rBlocks.subList(0, min(blocks.size(),
                                    myCar.speed + 1)).contains(Terrain.BOOST)
                            || rBlocks.subList(0, min(blocks.size(),
                                    myCar.speed + 1)).contains(Terrain.TWEET)) {
                        kananadapower = true;
                    }
                    if (lBlocks.subList(0, min(blocks.size(),
                            myCar.speed + 1)).contains(Terrain.OIL_POWER)
                            || lBlocks.subList(0, min(blocks.size(),
                                    myCar.speed + 1)).contains(Terrain.LIZARD)
                            || lBlocks.subList(0, min(blocks.size(), myCar.speed + 1)).contains(Terrain.EMP)
                            || lBlocks.subList(0, min(blocks.size(),
                                    myCar.speed + 1)).contains(Terrain.BOOST)
                            || lBlocks.subList(0, min(blocks.size(),
                                    myCar.speed + 1)).contains(Terrain.TWEET)) {
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

        // Jalur aman BISA ATAU TIDAK PERLU MENGHINDARI OBST

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

        // Cek jalur
        if (myCar.speed != 0) {
            if (lanepos == 1) {
                if (countObstacleRight == 0 && countObstacle == 0) {
                    if (countPowerUpRight > countPowerUp) {
                        return TURN_RIGHT;
                    }
                }
                if (countObstacleRight == countObstacle && countObstacle != 0) {
                    if (RightBen >= SelfBen) {
                        return TURN_RIGHT;
                    }
                }
            } else if (lanepos == 2) {
                if (countObstacle == 0 && countObstacleRight == 0) {
                    if (countPowerUpRight > countPowerUp) {
                        return TURN_RIGHT;
                    }
                } else {
                    if (LeftBen > SelfBen) {
                        return TURN_RIGHT;
                    }
                }
            } else if (lanepos == 3) {
                if (countObstacle == 0 && countObstacleLeft == 0) {
                    if (countPowerUpLeft > countPowerUp) {
                        return TURN_LEFT;
                    }
                } else {
                    if (RightBen > SelfBen) {
                        return TURN_LEFT;
                    }
                }
            } else if (lanepos == 4 && countObstacleLeft == 0 && countObstacle == 0) {
                if (countObstacleLeft == 0 && countObstacle == 0) {
                    if (countPowerUpLeft > countPowerUp) {
                        return TURN_LEFT;
                    }
                }
                if (countObstacleLeft == countObstacle && countObstacle != 0) {
                    if (LeftBen >= SelfBen) {
                        return TURN_LEFT;
                    }
                }
            }
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
