package neuralnet;

import org.jblas.FloatMatrix;

import java.util.Random;

public class NeuralNetwork {

	private final FloatMatrix[] weights;
	private final FloatMatrix[] biases;

	private final int inputSize;
	private final int outputSize;
	private final int[] hiddenLayerSizes;

	private final ActivationFunction activationFunction;

	public NeuralNetwork(int inputSize, int outputSize, int[] hiddenLayerSizes, ActivationFunction activationFunction) {

		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.hiddenLayerSizes = hiddenLayerSizes;

		this.activationFunction = activationFunction;

		weights = new FloatMatrix[hiddenLayerSizes.length + 1];
		biases = new FloatMatrix[hiddenLayerSizes.length + 1];
		initialiseRandomWeights();
		initialiseRandomBiases();
	}

	private void initialiseRandomWeights() {

		weights[0] = randomMatrixData(inputSize, hiddenLayerSizes[0]);
		weights[weights.length - 1] = randomMatrixData(hiddenLayerSizes[hiddenLayerSizes.length - 1], outputSize);
		for (int i = 1; i < weights.length - 1; i++) {
			weights[i] = randomMatrixData(hiddenLayerSizes[i - 1], hiddenLayerSizes[i]);
		}
	}

	private void initialiseRandomBiases() {

		biases[0] = randomMatrixData(inputSize, 1);
		biases[biases.length - 1] = randomMatrixData(hiddenLayerSizes[hiddenLayerSizes.length - 1], 1);
		for (int i = 1; i < biases.length - 1; i++) {
			biases[i] = randomMatrixData(hiddenLayerSizes[i - 1], 1);
		}

	}

	private FloatMatrix randomMatrixData(int columns, int rows) {

		float[][] toReturn = new float[columns][rows];

		Random random = new Random();

		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				toReturn[x][y] = random.nextFloat();
			}
		}
		return new FloatMatrix(toReturn);
	}

	public FloatMatrix feedForward(FloatMatrix input) {

		FloatMatrix toMultiply = input;

		for (int i = 0; i < weights.length; i++) {

			FloatMatrix result = new FloatMatrix();
			toMultiply.mmuli(weights[i], result);

			result.add(biases[i]);
			elementWiseActivate(result);
			toMultiply = result;

		}

		return toMultiply;
		
	}

	private void elementWiseActivate(FloatMatrix input) {

		for (int i = 0; i < input.data.length; i++) {
			input.data[i] = activationFunction.apply(input.data[i]);
		}

	}

}
