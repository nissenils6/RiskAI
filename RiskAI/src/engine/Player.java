package engine;
import java.util.HashSet;

public abstract class Player {

	public final int id;
	
	public HashSet<Province> provincesControlled;
	
	public Player(int id) {

		this.id = id;
	}

	public abstract void onTurn(GameEngine engine);
	
	public abstract void onInitialTurn(GameEngine engine);
	
	public abstract void onInitialDistribution(GameEngine engine);
	
	
	
	public int totalTroopCount() {
		
		int toReturn = 0;
		
		for(Province i:provincesControlled) {
			toReturn+=i.troopCount;
		}
		
		return toReturn;
	}

	
	
}
