package neuralnet;

import engine.players.AIPlayer;
import engine.GameEngine;
import engine.Player;
public class Environment {

	private final AIPlayer[] players = new AIPlayer[1000];
	private final int[] wins = new int[players.length];
	private final int[] matchesPlayed = new int[players.length];
	public Environment() {
		
	}
	
	public void playSingleMatch(int[] team) {
		
		GameEngine engine = new GameEngine();
		
		
		
		for(int i=0; i<team.length; i++) {
			engine.players[i]=players[team[i]];
			matchesPlayed[team[i]]++;
		}
		Player winner = engine.play();
		
		for(int i=0; i<team.length; i++) {
			if(winner==players[team[i]]) {
				wins[team[i]]++;
				return;
			}
		}
		
	}
	
}
