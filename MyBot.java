import java.util.ArrayList;
import java.util.List;

public class MyBot {
    public static Location center = null;
    public static Location attack = null;
    public static int currentStep = 0;
    public static Location highestProductionLocation = null;
    public static void main(String[] args) throws java.io.IOException {

        final InitPackage iPackage = Networking.getInit();
        final int myID = iPackage.myID;
        final GameMap gameMap = iPackage.map;

        Networking.sendInit("MyJavaBot");

        int first = 0;
        while(true) {
            if(currentStep > 10 && currentStep % 5 == 0){
                Direction.updateCenter(gameMap, myID);
            }
            List<Move> moves = new ArrayList<Move>();

            Networking.updateFrame(gameMap);

            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {
                    final Location location = gameMap.getLocation(x, y);
                    final Site site = location.getSite();
                    if(site.owner == myID) {
                        if(first == 0){
                            first = 1;
                            center = location;
                        }
                        moves.add(new Move(location, Direction.decideMove(location, gameMap, myID)));
                    }
                }
            }
            Networking.sendFrame(moves);
        }
    }
}
