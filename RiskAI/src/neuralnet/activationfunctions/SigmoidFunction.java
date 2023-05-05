package neuralnet.activationfunctions;

import neuralnet.ActivationFunction;

public class SigmoidFunction extends ActivationFunction {

	public final float xFactor;
	public final float yFactor;
	
	public SigmoidFunction(float xf, float yf) {
		this.xFactor = xf;
		this.yFactor = yf;
	}
	
	@Override
	public float apply(float value) {
		return (float) (yFactor/(1+Math.exp(-value/xFactor)));
	}

}
