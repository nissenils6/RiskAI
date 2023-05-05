package neuralnet;

import org.jblas.FloatMatrix;

import engine.GameEngine;

public class NeuralNetParser { // this class parses neural network outputs into moves

	public final GameEngine engine;

	public NeuralNetParser(GameEngine engine) {
		this.engine = engine;
	}

	public static int highLowPass(FloatMatrix input) { // used for things where the network for example is only allowed to
													// attack one province, set the value in the "adjacency matrix" (a
													// column vector due to NN stuff) to 1 and all else to 0


		int maxIndex = 0;
		float maxValue = 0;

		
		for (int i = 0; i < input.length; i++) {
			if (input.get(i) > maxValue) {
				maxIndex = i;
				maxValue = input.get(i);
			}
		}
		return maxIndex;
	}

	

	public static int maxIndex(float[] input) {
		
		float maxValue = input[0];
		int maxIndex = 0;
		
		for(int i=1; i<input.length; i++) {
			if(input[i]>maxValue) {
				maxValue = input[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	// Good idea: We can write our own column-matrix multiplication algorithm since the "Adjacency
	// matrix" (really a 1764 row column vector) and use the fact that 90% of its entries are zero
	
	// a player consists of four networks:
	//
	// 1. Claiming network, which claims provinces in the beginning based on other
	// claims. This takes in an "adjacency matrix", a vector representing which player 
	// controls a province and a vector representing the share of the total troop count 
	// in a province. Lastly it takes in the troop count in a single node. 
	// It outputs an "adjacency matrix". Send this through the HL filter 
	// to determine the deployment location 
	// 
	//2. Deploying network, which deploys troops to a province before a turn. This takes in 

}
