package engine.players;

import org.jblas.FloatMatrix;

import engine.GameEngine;
import engine.Player;
import neuralnet.ActivationFunction;
import neuralnet.NeuralNetParser;
import neuralnet.NeuralNetwork;
import neuralnet.activationfunctions.SigmoidFunction;

public class AIPlayer extends Player {

	// initial deployment network claims provinces at the beginning of the game
	// 42 vector [0,1] in (to encode if a province is claimed or not
	// 42 vector [0,1] in (to encode if a province is owned by this player or not)
	// 42 vector [0,1] in (to encode if a province is owned by opponent #1 or #2)
	// 42 vector [0,1] out (sets the province it wishes to deploy to to 1 and all
	// other to zero)
	private final NeuralNetwork provinceClaimingNetwork;

	// initial deployment network chooses how to distribute
	// 42 vector [0,1] in (to encode the share of *total* amount of troops in *this*
	// province)
	// 42 vector [0,1] in (to encode if a province is owned by this player or not)
	// 42 vector [0,1] in (to encode if a province is owned by opponent #1 or #2)
	// scalar in (sigmoided amount of total troops on the board)
	// scalar in (sigmoided amount of troops to deploy)
	// 42 vector [0,1] out, outputs the share of troops to deploy to be deployed in
	// a certain province

	private final NeuralNetwork initialDistributionNetwork;

	// deployment network chooses where to deploy troops
	// 42 vector [0,1] in (to encode the share of *total* amount of troops in *this*
	// province)
	// 42 vector [0,1] in (to encode if a province is owned by this player or not)
	// 42 vector [0,1] in (to encode if a province is owned by opponent #1 or #2)
	// 42 vector [0,1] in (to encode the suggested move (all legal moves are tried
	// one at a time)
	// scalar in (sigmoided amount of total troops on the board)
	// scalar in (sigmoided amount of troops to deploy)
	// scalar out (How much it "likes" the move, after all moves have been passed
	// through, choose the one that yielded the highest output)

	private final NeuralNetwork deploymentNetwork;

	// where to attack network, chooses a province to attack and a province to
	// attack from
	// deployment network chooses where to deploy troops
	// 42 vector [0,1] in (to encode the share of *total* amount of troops in *this*
	// province)
	// 42 vector [0,1] in (to encode if a province is owned by this player or not)
	// 42 vector [0,1] in (to encode if a province is owned by opponent #1 or #2)
	// 42 vector [0,1] in (to encode the suggested province to attack from (all
	// legal moves are tried
	// one at a time)
	// 42 vector [0,1] in (to encode the suggested province to attack (all legal
	// moves are tried
	// one at a time)
	// scalar in (sigmoided amount of total troops on the board)
	// scalar out (How much it "likes" the move, after all moves have been passed
	// through, choose the one that yielded the highest output)
	private final NeuralNetwork attackNetwork;

	// where to attack network, chooses a province to attack and a province to
	// attack from
	// deployment network chooses where to deploy troops
	// 42 vector [0,1] in (to encode the share of *total* amount of troops in *this*
	// province)
	// 42 vector [0,1] in (to encode if a province is owned by this player or not)
	// 42 vector [0,1] in (to encode if a province is owned by opponent #1 or #2)
	// 42 vector [0,1] in (to encode the suggested province to attack from (all
	// legal moves are tried
	// one at a time)
	// 42 vector [0,1] in (to encode the suggested province to attack (all legal
	// moves are tried
	// one at a time)
	// scalar in (sigmoided amount of total troops on the board)
	// scalar out (share of troops in province to attack with)
	private final NeuralNetwork attackingForceNetwork;

	// intra attack network, decides on if it wants to end attack or not
	// 42 vector [0,1] in (to encode the share of *total* amount of troops in *this*
	// province)
	// 42 vector [0,1] in (to encode if a province is owned by this player or not)
	// 42 vector [0,1] in (to encode if a province is owned by opponent #1 or #2)
	// 42 vector [0,1] in (to encode the suggested province to attack from (all
	// legal moves are tried
	// one at a time)
	// 42 vector [0,1] in (to encode the suggested province to attack (all legal
	// moves are tried
	// one at a time)
	// scalar in (sigmoided amount of total troops on the board)
	// scalar out (set this to >0.5 if it wants to continue, <0.5 otherwise)

	private final NeuralNetwork intraAttackNetwork;

	// where to attack network, chooses a province to attack and a province to
	// attack from
	// deployment network chooses where to deploy troops
	// 42 vector [0,1] in (to encode the share of *total* amount of troops in *this*
	// province)
	// 42 vector [0,1] in (to encode if a province is owned by this player or not)
	// 42 vector [0,1] in (to encode if a province is owned by opponent #1 or #2)
	// 42 vector [0,1] in (to encode the suggested province to move from (all
	// legal moves are tried
	// one at a time)
	// 42 vector [0,1] in (to encode the suggested province to move to (all legal
	// moves are tried
	// one at a time)
	//
	// scalar out (how much it wants to make this move)

	private final NeuralNetwork troopMovementNetwork;

	// where to attack network, chooses a province to attack and a province to
	// attack from
	// deployment network chooses where to deploy troops
	// 42 vector [0,1] in (to encode the share of *total* amount of troops in *this*
	// province)
	// 42 vector [0,1] in (to encode if a province is owned by this player or not)
	// 42 vector [0,1] in (to encode if a province is owned by opponent #1 or #2)
	// 42 vector [0,1] in (to encode the suggested province to move from (all
	// legal moves are tried
	// one at a time)
	// 42 vector [0,1] in (to encode the suggested province to move to (all legal
	// moves are tried
	// one at a time)
	//
	// scalar out (how many troops to move)

	private final NeuralNetwork troopMovementAmountNetwork;

	private final ActivationFunction activationFunction;
	private final ActivationFunction troopCountActivationFunction;

	public AIPlayer(int id) {
		super(id);

		activationFunction = new SigmoidFunction(1, 1);

		troopCountActivationFunction = new SigmoidFunction(10, 1);

		this.provinceClaimingNetwork = createNetwork(GameEngine.PROVINCE_COUNT * 3, GameEngine.PROVINCE_COUNT * 2,
				GameEngine.PROVINCE_COUNT * 2, GameEngine.PROVINCE_COUNT);

		this.initialDistributionNetwork = createNetwork(GameEngine.PROVINCE_COUNT * 3 + 2,
				GameEngine.PROVINCE_COUNT * 2, GameEngine.PROVINCE_COUNT * 2, GameEngine.PROVINCE_COUNT);

		this.deploymentNetwork = createNetwork(GameEngine.PROVINCE_COUNT * 5 + 2, GameEngine.PROVINCE_COUNT * 4,
				GameEngine.PROVINCE_COUNT, 1);

		this.attackNetwork = createNetwork(GameEngine.PROVINCE_COUNT * 5 + 1, GameEngine.PROVINCE_COUNT * 4,
				GameEngine.PROVINCE_COUNT, 1);

		this.attackingForceNetwork = createNetwork(GameEngine.PROVINCE_COUNT * 5 + 1, GameEngine.PROVINCE_COUNT * 4,
				GameEngine.PROVINCE_COUNT, 1);

		this.intraAttackNetwork = createNetwork(GameEngine.PROVINCE_COUNT * 5 + 1, GameEngine.PROVINCE_COUNT * 4,
				GameEngine.PROVINCE_COUNT, 1);

		this.troopMovementNetwork = createNetwork(GameEngine.PROVINCE_COUNT * 5 + 1, GameEngine.PROVINCE_COUNT * 4,
				GameEngine.PROVINCE_COUNT, 1);

		this.troopMovementAmountNetwork = createNetwork(GameEngine.PROVINCE_COUNT * 5 + 1,
				GameEngine.PROVINCE_COUNT * 4, GameEngine.PROVINCE_COUNT, 1);
	}

	private NeuralNetwork createNetwork(int input, int hidden1, int hidden2, int output) {
		return new NeuralNetwork(input, output, new int[] { hidden1, hidden2 }, activationFunction);
	}

	@Override
	public void onTurn(GameEngine engine) {

		deploy(engine); // We should use the 1/e shit
		
	}

	@Override
	public void onInitialTurn(GameEngine engine) {
		// TODO Auto-generated method stub
		FloatMatrix currentState = getInitialProvinceGamestate(engine);

		FloatMatrix rawOutput = provinceClaimingNetwork.feedForward(currentState);

		int provinceIndexToClaim = NeuralNetParser.highLowPass(rawOutput);

		engine.claimProvince(engine.provinces[provinceIndexToClaim], this);
	}

	@Override
	public void onInitialDistribution(GameEngine engine) {
		// TODO Auto-generated method stub

		FloatMatrix currentState = getInitialDistributionGamestate(engine);

		FloatMatrix rawOutput = initialDistributionNetwork.feedForward(currentState);

		float totalTroopCount = engine.getTotalTroopCount();

		int[] toDeploy = new int[GameEngine.PROVINCE_COUNT];

		rawOutput.div(rawOutput.norm1()); // normalises the vector to have a sum of 1

		int totalTroopsDeployed = 0;

		for (int i = 0; i < toDeploy.length; i++) {
			toDeploy[i] = (int) Math.floor(rawOutput.get(i) * totalTroopCount);
			totalTroopsDeployed += toDeploy[i];
		}

		int leftoverTroops = Math.max(0, GameEngine.getInitialDeploymentAmount(this) - totalTroopsDeployed);

		int deployLeftoverProvince = NeuralNetParser.maxIndex(rawOutput.data);

		for (int i = 0; i < toDeploy.length; i++) {
			engine.deployTroopsSingleProvince(this, engine.provinces[i], toDeploy[i]);
		}
		engine.deployTroopsSingleProvince(this, engine.provinces[deployLeftoverProvince], leftoverTroops);

	}

	private void addOwners(float[] currentData, int startIndex, GameEngine engine) {

		for (int i = 0; i < GameEngine.PROVINCE_COUNT; i++) {
			if (engine.provinces[i].owner == this) {
				currentData[i + startIndex] = 1;
			} else {
				for (int j = 0; j < engine.players.length; j++) {
					if (engine.players[j] == engine.provinces[i].owner) {
						currentData[i + startIndex + GameEngine.PROVINCE_COUNT] = ((float) j)
								/ (engine.players.length - 1);
						break;
					}
				}
			}

		}

	}

	private void addTroopShare(float[] currentData, int startIndex, GameEngine engine) {

		float totalTroops = engine.getTotalTroopCount();

		for (int i = startIndex; i < startIndex + GameEngine.PROVINCE_COUNT; i++) {
			currentData[i] = engine.provinces[i - startIndex].troopCount / totalTroops;
		}

	}

	private void addSuggestedMove(float[] currentData, int startIndex, int suggestedProvince) {
		currentData[startIndex + suggestedProvince] = 1;
	}

	private FloatMatrix getDeploymentGamestate(GameEngine engine, int suggestedProvince) {
		float[] toReturn = new float[GameEngine.PROVINCE_COUNT * 4 + 2];

		addTroopShare(toReturn, 0, engine);
		addOwners(toReturn, GameEngine.PROVINCE_COUNT, engine);
		addSuggestedMove(toReturn, GameEngine.PROVINCE_COUNT * 3, suggestedProvince);

		float troopsToDeploy = GameEngine.getInitialDeploymentAmount(this);
		float totalTroops = engine.getTotalTroopCount();

		toReturn[toReturn.length - 2] = totalTroops;
		toReturn[toReturn.length - 1] = troopsToDeploy;

		return new FloatMatrix(toReturn);

	}

	private float feedForwardDeployment(GameEngine engine, int suggestedProvince) {
		FloatMatrix currentState = getDeploymentGamestate(engine, suggestedProvince);

		return deploymentNetwork.feedForward(currentState).get(0);
	}

	private void deploy(GameEngine engine) {

		int bestProvince = 0;
		float maxValue = feedForwardDeployment(engine, 0);

		for (int i = 1; i < GameEngine.PROVINCE_COUNT; i++) {

			float local = feedForwardDeployment(engine, i);

			if (local > maxValue) {
				bestProvince = i;
				maxValue = local;
			}

		}

		engine.deployTroopsSingleProvince(this, engine.provinces[bestProvince], engine.troopsToDeploy(this));

	}

	private FloatMatrix getInitialDistributionGamestate(GameEngine engine) {

		float[] toReturn = new float[GameEngine.PROVINCE_COUNT * 3 + 2];

		addTroopShare(toReturn, 0, engine);
		addOwners(toReturn, GameEngine.PROVINCE_COUNT, engine);

		float troopsToDeploy = GameEngine.getInitialDeploymentAmount(this);
		float totalTroops = engine.getTotalTroopCount();

		toReturn[toReturn.length - 2] = totalTroops;
		toReturn[toReturn.length - 1] = troopsToDeploy;

		return new FloatMatrix(toReturn);

	}

	private FloatMatrix getInitialProvinceGamestate(GameEngine engine) {
		float[] toReturn = new float[GameEngine.PROVINCE_COUNT * 3];

		for (int i = 0; i < GameEngine.PROVINCE_COUNT; i++) {
			if (engine.provinces[i].owner == null) {
				toReturn[i] = 1;
			}
		}
		addOwners(toReturn, GameEngine.PROVINCE_COUNT, engine);

		return new FloatMatrix(toReturn);
	}

}
