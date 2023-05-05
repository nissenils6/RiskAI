package engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class GameEngine {

	public static final int PROVINCE_COUNT = 42;
	public static final int STARTING_TROOPS = 20;
	public static final int CONTINENT_COUNT = 6;
	
	public final Player[] players = new Player[3];

	public final Province[] provinces = new Province[PROVINCE_COUNT];

	public GameEngine() {
		
	}

	public void printProvinces() {
		for(Province province:provinces) {
			System.out.println(province.toString());
		}
	}

	public Player play() {

		Player winner = null;

		while ((winner = getWinner()) == null) {
			round();
		}
		return winner;
	}

	public static int getInitialDeploymentAmount(Player player) {
		return STARTING_TROOPS - player.provincesControlled.size();
	}
	
	public void initialRound() { // the "province claiming" round
		for (int i = 0; i < players.length; i++) {
			players[i].onInitialTurn(this);
		}
		initialDeployment();
	}
	
	public void initialDeployment() {
		for (int i = 0; i < players.length; i++) {
			players[i].onInitialDistribution(this);
		}
	}
	
	public void round() {
		for (int i = 0; i < players.length; i++) {
			players[i].onTurn(this);
		}
	}

	public Player getWinner() {
		Player nonZeroTroopCountPlayer = null;

		for (Player i : players) {
			if (i.totalTroopCount() > 0) {

				if (nonZeroTroopCountPlayer == null) {
					nonZeroTroopCountPlayer = i;
				} else {
					return null;
				}
			}
		}
		return nonZeroTroopCountPlayer;
	}

	public int troopsToDeploy(Player deployer) {
		return deployer.provincesControlled.size() / 3; // implement continent and card logic later
	}

	public void claimProvince(Province claimed, Player claimer) {
		claimed.owner.provincesControlled.remove(claimed);
		claimed.owner = claimer;
		claimer.provincesControlled.add(claimed);
	}

	public boolean moveTroops(Province origin, Province destination, int troopsMoved) {
		if (!isLegalTroopMove(origin, destination, troopsMoved)) {
			return false;
		} else {
			destination.troopCount += troopsMoved;
			origin.troopCount -= troopsMoved;
			return true;
		}
	}

	private boolean isLegalTroopMove(Province origin, Province destination, int troopsMoved) {
		Player mover = origin.owner;

		if (!mover.provincesControlled.contains(destination)) {
			return false;
		}

		else if (origin.troopCount - troopsMoved < 1) {
			return false;
		}

		else if (!(origin.neighbours.contains(destination))) {
			return false;
		}

		else {
			return true;
		}
	}

	public void attack(Province attackingProvince, Province attackedProvince) { // maybe return
																				// surviving troops
		int attackTroops = attackingProvince.troopCount - 1;

		Player attacker = attackingProvince.owner;

		if (!isLegalAttack(attacker, attackingProvince, attackedProvince, attackTroops)) { // should probably throw
																							// something or return -1
			return;
		}

		int attackerDieCount = Math.min(attackTroops, 3);
		int defenderDieCount = Math.min(attackingProvince.troopCount, 2);

		ArrayList<Integer> attackerDies = generateDies(attackerDieCount);
		ArrayList<Integer> defenderDies = generateDies(defenderDieCount);

		attackerDies.sort((a, b) -> {
			if (a == b)
				return 0;
			else
				return a > b ? -1 : 1;
		});

		defenderDies.sort((a, b) -> {
			if (a == b)
				return 0;
			else
				return a > b ? -1 : 1;
		});

		int attackerLoss = 0;
		int defenderLoss = 0;

		for (int i = 0; i < Math.min(attackerDieCount, defenderDieCount); i++) {
			if (attackerDies.get(i) > defenderDies.get(i)) {
				defenderLoss++;
			} else {
				attackerLoss++;
			}
		}

		attackingProvince.troopCount -= attackerLoss;
		attackedProvince.troopCount  -= defenderLoss;

		if (attackedProvince.troopCount <= 0) {
			claimProvince(attackedProvince, attacker);
			attackedProvince.troopCount = 0;
		}

	}

	public void deployTroopsSingleProvince(Player deployer, Province deployTo, int toDeploy) {
		if (!checkLegalDeploy(deployer, deployTo)) {
			return;
		}

		else {
			deployTo.troopCount += toDeploy;
		}

	}

	public boolean checkLegalDeploy(Player deployer, Province deployTo) {
		return deployer.provincesControlled.contains(deployTo);
	}

	public static ArrayList<Integer> generateDies(int dieCount) {
		ArrayList<Integer> toReturn = new ArrayList<Integer>();

		for (int i = 0; i < dieCount; i++) {
			int result = (int) Math.floor(new Random().nextDouble() * 6) + 1;
			toReturn.add(result);
		}
		return toReturn;
	}

	private boolean isLegalAttack(Player attacker, Province attackingProvince, Province attackedProvince,
			int attackTroops) {

		if (!attackingProvince.neighbours.contains(attackedProvince)) {
			return false;
		}
		if (attackingProvince.troopCount - attackTroops < 1) {
			return false;
		}

		return true;

	}

	public float getTotalTroopCount() {
		int toReturn = 0;
		
		for(int i=0; i<players.length; i++) {
			toReturn+=players[i].totalTroopCount();
		}
		
		return toReturn;
		
	}
	
}
