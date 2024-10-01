import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;


public enum Direction {
    STILL, NORTH, EAST, SOUTH, WEST;
    public static final Direction[] DIRECTIONS = new Direction[]{STILL, NORTH, EAST, SOUTH, WEST};
    public static final Direction[] CARDINALS = new Direction[]{NORTH, EAST, SOUTH, WEST};

    public static Direction randomDirection() {
        Direction[] values = values();
        return values[new Random().nextInt(values.length)];
    }


    public static Direction getRelativeDirection1(Location loc1, Location loc2, GameMap gameMap) {
        double angle = gameMap.getAngle(loc1, loc2);


        if (angle >= 7 * Math.PI / 4 && angle <= 2 * Math.PI || angle >= 0 && angle < Math.PI / 4) {
            return EAST;
        }

        if (angle >= Math.PI / 4 && angle < 3 * Math.PI / 4) {
            return NORTH;
        }

        if (angle >= 3 * Math.PI / 4 && angle < 5 * Math.PI / 4) {
            return WEST;
        }

        if (angle >= 5 * Math.PI / 4 && angle < 7 * Math.PI / 4) {
            return SOUTH;
        }

        // - pi/4 -> pi/4
        if (angle >= -Math.PI / 4 && angle < Math.PI / 4) {
            return EAST;
        }

        // pi/4 -> 3*pi/4
        if (angle >= Math.PI / 4 && angle < 3 * Math.PI / 4) {
            return NORTH;
        }
        // 3*pi/4 -> pi SAU -pi -> -3*pi/4
        if ((angle >= 3 * Math.PI / 4 && angle <= Math.PI) || (angle >= -Math.PI
                && angle < -3 * Math.PI / 4)) {
            return WEST;
        }

        // -3*pi/4 -> -pi/4
        if (angle >= -3 * Math.PI / 4 && angle < -Math.PI / 4) {
            return SOUTH;
        }

        return WEST;
    }

    public static Direction getRelativeDirection(Location loc1, Location loc2, GameMap gameMap) {
        double angle = gameMap.getAngle(loc1, loc2);

        // Convert angle to degrees
        double degrees = Math.toDegrees(angle);

        // Normalize degrees to [0, 360)
        degrees = (degrees + 360) % 360;


        // Determine cardinal direction
        if ((degrees >= 0 && degrees <= 45) || (degrees >= 315 && degrees <= 360)) {
            return WEST;
        } else if (degrees >= 45 && degrees < 135) {
            return SOUTH;
        } else if (degrees >= 135 && degrees < 225) {
            return EAST;
        } else if (degrees >= 225 && degrees < 315) {
            return NORTH;
        } else {
            // Handle unexpected angles (optional)
            return STILL;
        }

    }

    public static boolean isLocationOnBorder(Location location, GameMap gameMap) {
        return location.x == 0 || location.x == gameMap.width - 1 || location.y == 0
                || location.y == gameMap.height - 1;
    }

    public static Direction getOppositeDirection(Direction d) {
        switch (d) {
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            default:
                return STILL;
        }
    }

    public static Direction increaseProduction(Location location, GameMap gameMap, int myID) {
        // We create two lists: One with the directions that are not ours and we want to conquer
        // and the other list with the directions that are already ours.
        ArrayList<Direction> directionsOk = new ArrayList<>();
        ArrayList<Direction> directionsOccupied = new ArrayList<>();
        Site site = location.getSite();
        // We iterate through the 4 directions
        for (Direction d : CARDINALS) {
            Location l = gameMap.getLocation(location, d);
            // If the location is not ours and the strength of the site is lower than ours, we add it to the directionsOk list
            if (l.getSite().owner != myID && l.getSite().strength < site.strength) {
                directionsOk.add(d);
            }
            // If the location is ours, we add it to the directionsOccupied list
            if (l.getSite().owner == myID) {
                directionsOccupied.add(d);
            }
        }

        // We try to move to the position with the highest production
        // Sort the list by the production in descending order
        directionsOk.sort((d1, d2) -> {
            Location l1 = gameMap.getLocation(location, d1);
            Location l2 = gameMap.getLocation(location, d2);
            return l2.getSite().production - l1.getSite().production;
        });

        // STILL if we don't have enough strength to go anywhere.
        return directionsOk.isEmpty() ? STILL
                : moveToDirectionIfCanConquerElseStill(location, gameMap, directionsOk.get(0),
                        myID);
    }

    public static Direction takeLowStrengthNeighbours(Location location, GameMap gameMap,
                                                      int myID) {
        // We create two lists: One with the directions that are not ours and we want to conquer
        // and the other list with the directions that are already ours.
        ArrayList<Direction> directionsOk = new ArrayList<>();
        ArrayList<Direction> directionsOccupied = new ArrayList<>();
        Site site = location.getSite();
        // We iterate through the 4 directions
        for (Direction d : CARDINALS) {
            Location l = gameMap.getLocation(location, d);
            // If the location is not ours and the strength of the site is lower than ours, we add it to the directionsOk list
            if (l.getSite().owner != myID && l.getSite().strength < site.strength) {
                directionsOk.add(d);
            }
            // If the location is ours, we add it to the directionsOccupied list
            if (l.getSite().owner == myID) {
                directionsOccupied.add(d);
            }
        }

        // We try to move to the position with the highest production
        // Sort the list by the production in descending order
        directionsOk.sort((d1, d2) -> {
            Location l1 = gameMap.getLocation(location, d1);
            Location l2 = gameMap.getLocation(location, d2);
            return l1.getSite().strength - l2.getSite().strength;
        });

        // STILL if we don't have enough strength to go anywhere.
        return directionsOk.isEmpty() ? STILL : directionsOk.get(0);
    }

    private static Direction combinePowerIfLocationIsEnemy(Location location, GameMap gameMap,
                                                           int myID, Direction d, Location l) {
        Site site = location.getSite();
        if (l.getSite().owner != myID && l.getSite().owner != 0) {
            if (l.getSite().strength < site.strength) {
                return STILL;
            }
            ArrayList<Location> locs = new ArrayList<>();
            locs.add(gameMap.getLocation(gameMap.getLocation(location, d), WEST));
            locs.add(gameMap.getLocation(gameMap.getLocation(location, d), EAST));
            locs.add(gameMap.getLocation(gameMap.getLocation(location, d), NORTH));
            locs.add(gameMap.getLocation(gameMap.getLocation(location, d), SOUTH));
            int totalPower = 0;
            for (int i = 0; i < 3; i++) {
                if (locs.get(i).getSite().owner == myID) {
                    totalPower += locs.get(i).getSite().strength;
                }
            }

            if (totalPower >= site.strength) { // Combinam pozitii
                return d;
            }
        }
        return STILL;
    }

    private static Direction combinePowerIfLocationIsUnoccupied(Location location, GameMap gameMap,
                                                                int myID, Direction d, Location l) {
        Site site = location.getSite();
        if (l.getSite().owner != myID) {
            if (l.getSite().strength < site.strength) {
                return STILL;
            }
            ArrayList<Location> locs = new ArrayList<>();
            locs.add(gameMap.getLocation(gameMap.getLocation(location, d), WEST));
            locs.add(gameMap.getLocation(gameMap.getLocation(location, d), EAST));
            locs.add(gameMap.getLocation(gameMap.getLocation(location, d), NORTH));
            locs.add(gameMap.getLocation(gameMap.getLocation(location, d), SOUTH));
            int totalPower = 0;
            for (int i = 0; i < 3; i++) {
                if (locs.get(i).getSite().owner == myID) {
                    totalPower += locs.get(i).getSite().strength;
                }
            }

            if (totalPower > site.strength) { // Combinam pozitii
                return d;
            }
        }
        return STILL;
    }

    public static int getNumberOfSitesOwnedByPlayer(int myID, GameMap gameMap) {
        int count = 0;
        for (int x = 0; x < gameMap.width; x++) {
            for (int y = 0; y < gameMap.height; y++) {
                Location currentLoc = gameMap.getLocation(x, y);
                int siteOwnerID = currentLoc.getSite().owner;

                if (siteOwnerID == myID) {
                    count++;
                }
            }
        }

        return count;
    }

    public static Direction toClosestEnemy(Location locatin, GameMap gameMap, int myID) {
        ArrayList<Location> enemies = getEnemyPositions(myID, gameMap);
        Location closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Location loc : enemies) {
            double distance = gameMap.getDistance(locatin, loc);
            if (distance < minDistance) {
                minDistance = distance;
                closest = loc;
            }
        }
        return moveToDirectionIfCanConquerElseStill(locatin, gameMap,
                getRelativeDirection(locatin, closest, gameMap), myID);
    }

    public static Direction toClosestUnoccupied(Location location, GameMap gameMap, int myID) {
        ArrayList<Location> unoccupied = new ArrayList<>();
        for (int x = 0; x < gameMap.width; x++) {
            for (int y = 0; y < gameMap.height; y++) {
                Location currentLoc = gameMap.getLocation(x, y);
                int siteOwnerID = currentLoc.getSite().owner;

                if (siteOwnerID == 0) {
                    unoccupied.add(currentLoc);
                }
            }
        }
        Location closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Location loc : unoccupied) {
            double distance = gameMap.getDistance(location, loc);
            if (distance < minDistance) {
                minDistance = distance;
                closest = loc;
            }
        }
        return moveToDirectionIfCanConquerElseStill(location, gameMap,
                getRelativeDirection(location, closest, gameMap), myID);

    }

    public static Direction decideMove(Location location, GameMap gameMap, int myID) {
        // NO ENEMIES, we just want to complete the board.
        ArrayList<Location> enemies = getEnemyPositions(myID, gameMap);
        if (enemies.size() == 0) {
            return getDirectionNoEnemies(location, gameMap, myID);
        }

        int nrOfSitesOwned = getNumberOfSitesOwnedByPlayer(myID, gameMap);
        Site site = location.getSite();
        // If we have an attack and we conquered the site, we set the attack to null
        if (MyBot.attack != null && MyBot.attack.getSite().owner == myID) {
            MyBot.attack = null;
        }

        // Separate strategy for the beginning of the game.
        if (nrOfSitesOwned < 25) {
            return getDirectionCircular(location, gameMap, myID);
        }


        // If we have more than 100 sites, we try to increase the strength of the sites in the center as much as possible.
        if (nrOfSitesOwned < 100) {
            if (site.strength < site.production * 5) {
                return STILL;
            }
        } else {
            ArrayList<Location> ourNeighbouringLoc = getOurNeighbouringLocations(location, gameMap,
                    myID);
            if (ourNeighbouringLoc.size() == 4 && site.strength < site.production * 6) {
                return STILL;
            }
            if (ourNeighbouringLoc.size() >= 2 && site.strength < site.production * 5) {
                return STILL;
            }
            if (site.strength < site.production * 5) {
                return STILL;
            }
        }


        // ENEMIES
        Location nearestEnemy = getNearestEnemy(location, gameMap, enemies);
        // If we are attacking, we try to combine powers. Set attack, so other sites know we are attacking.
        if (gameMap.getDistance(location, nearestEnemy) <= 1 && MyBot.attack == null) {
            MyBot.attack = nearestEnemy;
            return combinePowerIfLocationIsEnemy(location, gameMap, myID,
                    getRelativeDirection(location, nearestEnemy, gameMap), nearestEnemy);
        }

        // We will probably attack next turn. We set attack to receive support.
        if (gameMap.getDistance(location, nearestEnemy) <= 2 && MyBot.attack == null) {
            MyBot.attack = nearestEnemy;
        }

        // Enemy in proximity.
        if (gameMap.getDistance(location, nearestEnemy) < 7) {
            ArrayList<Location> ourNeighbouringLoc = getOurNeighbouringLocations(location, gameMap,
                    myID);

            // If we have more than 2 neighbouring locations, we try to make way to the enemy.
            if (ourNeighbouringLoc.size() >= 2) {
                Direction toEnemy = getRelativeDirection(location, nearestEnemy, gameMap);
                return moveToDirectionIfCanConquerElseStill(location, gameMap, toEnemy, myID);
            }
        }

        // If we are attacking, we go towards the attack location.
        if (MyBot.attack != null) {
            if (gameMap.getDistance(location, MyBot.attack) > 10) {
                return getDirectionCircular(location, gameMap, myID) == STILL ? toClosestUnoccupied(
                        location, gameMap, myID) : getDirectionCircular(location, gameMap, myID);
            }
            return moveToDirectionIfCanConquerElseStill(location, gameMap,
                    getRelativeDirection(location, MyBot.attack, gameMap), myID);
        }

        // No enemy near. Increase production.
        if (site.strength < site.production * 5) {
            return STILL;
        } else {
            return increaseProduction(location, gameMap, myID) == STILL ? toClosestUnoccupied(
                    location, gameMap, myID) : increaseProduction(location, gameMap,
                    myID); // NEW THING - trece ultimul, nu trece primul
        }
    }


    private static Direction moveToDirectionIfCanConquerAndNotOurs(Location location,
                                                                   GameMap gameMap,
                                                                   Direction toEnemy, int myID) {
        if (gameMap.getLocation(location, toEnemy).getSite().owner == myID) {
            return STILL;

        }
        if (gameMap.getLocation(location, toEnemy).getSite().strength
                < location.getSite().strength) {
            return toEnemy;
        } else {
            return STILL;
        }
    }

    private static int getEccentricity(Location location, GameMap gameMap, int myID) {
        Set<Location> visited = new HashSet<>();
        Queue<Location> q = new LinkedList<>();
        q.add(location);
        int eccentricity = 0;

        while (!q.isEmpty()) {
            Queue<Location> nextQ = new LinkedList<>();
            while (!q.isEmpty()) {
                Location current = q.poll();
                for (Direction d : CARDINALS) {
                    Location next = gameMap.getLocation(current, d);
                    if (next.getSite().owner == myID && !visited.contains(next)) {
                        nextQ.add(next);
                        visited.add(next);
                    }
                }
            }
            q = nextQ;
            eccentricity++;
        }

        return eccentricity;
    }

    private static int[][] getEccentricities(GameMap gameMap, int myID) {
        int[][] ecc = new int[gameMap.width][gameMap.height];
        for (int i = 0; i < gameMap.width; i++) {
            for (int j = 0; j < gameMap.height; j++) {
                if (gameMap.getLocation(i, j).getSite().owner == myID) {
                    ecc[i][j] = getEccentricity(gameMap.getLocation(i, j), gameMap, myID);
                } else {
                    ecc[i][j] = Integer.MAX_VALUE;
                }
            }
        }
        return ecc;
    }

    public static void updateCenter(GameMap gameMap, int myID) {
        int[][] ecc = getEccentricities(gameMap, myID);
        int minEcc = Integer.MAX_VALUE;
        Location center = null;
        for (int i = 0; i < gameMap.width; i++) {
            for (int j = 0; j < gameMap.height; j++) {
                if (ecc[i][j] < minEcc) {
                    minEcc = ecc[i][j];
                    center = gameMap.getLocation(i, j);
                }
            }
        }
        MyBot.center = center;
    }

    private static Direction moveToDirectionIfCanConquerElseStill(Location location,
                                                                  GameMap gameMap,
                                                                  Direction toEnemy, int myID) {
        Location next = gameMap.getLocation(location, toEnemy);

        if (next.getSite().owner == myID) {
            if (location.getSite().strength > 5 * location.getSite().production) {
                return toEnemy;
            } else {
                return STILL;
            }
        }
        if (next.getSite().strength < location.getSite().strength) {
            return toEnemy;
        } else {
            return STILL;
        }
    }

    private static ArrayList<Location> getOurNeighbouringLocations(Location location,
                                                                   GameMap gameMap, int myID) {
        ArrayList<Location> ourNeighbouringLoc = new ArrayList<>();
        for (Direction d : CARDINALS) {
            Location l = gameMap.getLocation(location, d);
            if (l.getSite().owner == myID) {
                ourNeighbouringLoc.add(l);
            }
        }
        return ourNeighbouringLoc;
    }

    public static Location getLocationHighestProduction(Location location, GameMap gameMap,
                                                        int myID) {
        Location highestProduction = null;
        int maxProduction = 0;
        for (int i = 0; i < gameMap.width; i++) {
            for (int j = 0; j < gameMap.height; j++) {
                Location currentLocation = gameMap.getLocation(i, j);
                if (currentLocation.getSite().owner != myID) {
                    if (currentLocation.getSite().production > maxProduction) {
                        maxProduction = currentLocation.getSite().production;
                        highestProduction = currentLocation;
                    }
                }
            }
        }
        return highestProduction;
    }

    public static Direction getDirectionHighestProduction(Location location, GameMap gameMap,
                                                          int myID) {
        if (MyBot.highestProductionLocation == null) {
            MyBot.highestProductionLocation = getLocationHighestProduction(location, gameMap, myID);
        }
        if (MyBot.highestProductionLocation.getSite().owner == myID) {
            return getDirectionCircular(location, gameMap, myID);
        } else {
            return moveToDirectionIfCanConquerElseStill(location, gameMap,
                    getRelativeDirection(location, MyBot.highestProductionLocation, gameMap), myID);
        }
    }

    public static Direction getDirectionCircular(Location location, GameMap gameMap, int myID) {
        Direction betterProduction = increaseProduction(location, gameMap, myID);
        if (gameMap.getLocation(location, betterProduction).getSite().owner != myID) {
            return moveToDirectionIfCanConquerElseStill(location, gameMap, betterProduction, myID);
        }

        Direction circular = getRelativeDirection(location, MyBot.center, gameMap);
        return moveToDirectionIfCanConquerAndNotOurs(location, gameMap, circular, myID) == STILL
                ? betterProduction : circular;
    }

    private static Direction getDirectionNoEnemies(Location location, GameMap gameMap, int myID) {

        Direction betterProduction = increaseProduction(location, gameMap, myID);
        if (gameMap.getLocation(location, betterProduction).getSite().owner != myID) {
            return moveToDirectionIfCanConquerElseStill(location, gameMap, betterProduction, myID);
        }

        int x = location.x;
        for (int y = 0; y < gameMap.height; y++) {
            if (gameMap.getLocation(x, y).getSite().owner != myID) {
                return moveToDirectionIfCanConquerElseStill(location, gameMap, NORTH, myID);
            }
        }
        return moveToDirectionIfCanConquerElseStill(location, gameMap, WEST, myID);
    }

    public static boolean canCreatePath(Location loc1, Location loc2) {
        return false;
    }


    private static double getDistance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow((loc1.x - loc2.x), 2) + Math.pow((loc1.y - loc2.y), 2));
    }

    private static Location getNearestEnemy(Location ourLocation, GameMap gameMap,
                                            ArrayList<Location> enemies) {
        enemies.sort((l1, l2) -> {
            double distance1 = gameMap.getDistance(ourLocation, l1);
            double distance2 = gameMap.getDistance(ourLocation, l2);
            return (int) (distance1 - distance2);
        });

        return enemies.get(0);
    }

    // function that returns a list of all positions of the enemy
    private static ArrayList<Location> getEnemyPositions(int myID, GameMap gameMap) {
        ArrayList<Location> enemies = new ArrayList<>();

        for (int x = 0; x < gameMap.width; x++) {
            for (int y = 0; y < gameMap.height; y++) {
                Location currentLoc = gameMap.getLocation(x, y);
                int siteOwnerID = currentLoc.getSite().owner;

                if (siteOwnerID != myID && siteOwnerID != 0) {
                    enemies.add(currentLoc);
                }
            }
        }

        return enemies;
    }


}
