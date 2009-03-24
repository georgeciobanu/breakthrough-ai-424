package breakthrough;

import java.io.*;
import java.util.*;

/**
 * Feed-forward neural net with all the units in a layer connected to all the
 * units in the previous layer.
 * 
 * You need to fill in the train() methods. It will be helpful to look at the
 * constructor to see how the data structures are set up.
 * 
 * The class includes a main() method which can be used to test everything.
 */
public class NeuralNet {

	/**
	 * Number of units in each layer, including the input and output layers.
	 * This size of this array determines the number of layers.
	 */
	private int structure[];

	/**
	 * Weights, indexed by layer, by neuron, and by weight. Each neuron in a
	 * layer has an weight for each neuron in the previous layer, plus one for
	 * the threshold (the last weight in the array).
	 */
	private double weights[][][];

	/**
	 * Activations, indexed by layer, and by neuron. Store the activation of
	 * each neuron here.
	 */
	private double activations[][];

	/**
	 * Errors, indexed by layer, and by neuron. Store the backprop error
	 * gradient for each neuron here.
	 */
	private double errors[][];

	/**
	 * Run the neural net and return the output neuron activations. This method
	 * fills the activations[][] array.
	 * 
	 * @param inputs
	 *            the activations of the input layer.
	 * @return the activations of the output layer.
	 */
	public double[] query(double inputs[]) {

		// Set input activations to whatever the inputs are
		System.arraycopy(inputs, 0, activations[0], 0, activations[0].length);

		// For each layer after the input layer
		for (int layer = 1; layer < structure.length; layer++) {
			int numInputs = structure[layer - 1];
			// For each neuron in the layer
			for (int unit = 0; unit < structure[layer]; unit++) {
				// Compute the input function
				double sum = 0;
				for (int input = 0; input < numInputs; input++)
					sum += weights[layer][unit][input]
							* activations[layer - 1][input];
				// Add the threshold weight
				sum += weights[layer][unit][numInputs] * (-1);
				// Compute the activation
				activations[layer][unit] = 1 / (1 + Math.exp(-sum));
			}
		}

		// Return the output layer activations
		return activations[structure.length - 1];

	}

	/**
	 * Use backpropgation to learn from one example. This method fills the
	 * errors[][] array and updates the weights[][][] array.
	 * 
	 * @param inputs
	 *            the activations of the input layer.
	 * @param targetOutputs
	 *            the desired output layer activations.
	 * @param learningRate
	 *            the learning rate to use.
	 */
	public void train(double inputs[], double targetOutputs[],
			double learningRate) {

		boolean something = true;
		while (something) {

			int outputLayerIndex = activations.length - 1;
			// For each input
			for (int i = 0; i < inputs.length; i++) {

				// Initialize input layer
				for (int j = 0; j < structure[0]; j++) {
					activations[0][j] = inputs[j];
				}

				// For each layer compute the activation
				for (int l = 1; l < structure.length; l++) {
					// For each neuron
					for (int k = 0; k < structure[l]; k++) {
						// For each neuron (or input) compute in
						double ink = Double.NEGATIVE_INFINITY;
						for (int t = 0; t < weights[l][k].length; t++) {
							ink = weights[l][k][t] * activations[l - 1][t];
						}
						activations[l][k] = sigmoid(ink);
					}
				}

				// For each neuron in the output layer compute the error
				for (int o = 0; o < structure[outputLayerIndex]; o++) {
					// For each neuron in the previous layer compute in
					double inm = Double.NEGATIVE_INFINITY;
					for (int t = 0; t < weights[outputLayerIndex][o].length; t++) {
						inm = weights[outputLayerIndex][o][t]
								* activations[outputLayerIndex - 1][t];
					}
					errors[outputLayerIndex][o] = sigPrime(inm)
							* (targetOutputs[o] - activations[outputLayerIndex][o]);
				}

				// For each layer before the output layer, backpropagate
				for (int j = outputLayerIndex - 1; j > 0; j--) {
					// For each node in the layer compute error
					for (int l = 0; l < structure[j]; l++) {
						// Compute inl
						double inl = Double.NEGATIVE_INFINITY;
						for (int t = 0; t < weights[j][l].length; t++) {
							inl = weights[j][l][t] * activations[j][l];
						}

						// Compute error fraction for this neuron, based on
						// upper-layer error
						double frac = Double.NEGATIVE_INFINITY;
						for (int p = 0; p < weights[j + 1].length; p++) {
							frac = weights[j + 1][p][l] * errors[j][p];
						}

						errors[j][l] = sigPrime(inl) * frac;

						// For each node in upper layer
						for (int uppLNodeIndex = 0; uppLNodeIndex < structure[j + 1]; uppLNodeIndex++) {
							weights[j + 1][uppLNodeIndex][l] += learningRate
									* activations[j][l]
									* errors[j + 1][uppLNodeIndex];
						}
					}
				}
			}
		}// End While

		// ========= BEGIN SOLUTION ========= //

		// /////////////////////////////////////////////////
		// TODO: Implement backpropagation for one
		// training example. You may assume that
		// there are no hidden layers: that is,
		// you can assume that the first size of the
		// structure[], weights[], activations[] and
		// error[] arrays is 2. There can be any
		// number of input units.
		// /////////////////////////////////////////////////

		// ========== END SOLUTION ========== //
	}

	private double sigPrime(double inm) {
		return sigmoid(inm) * (1 - sigmoid(inm));
	}

	private double sigmoid(double ink) {
		return 1 / (1 + Math.exp(-ink));
	}

	// /////////////////////////////////////////////////////////////
	// //////// CONSTRUCTORS
	// ////////////////////////////////////////////////////////////

	/**
	 * Create a new neural net with random weights.
	 * 
	 * @param structure
	 *            an array containing the number of units in each later. Must be
	 *            of size at least 2, for the input and output layers
	 *            respectively.
	 */
	public NeuralNet(int[] structure) {

		// Check input
		if (structure.length < 2)
			throw new IllegalArgumentException(
					"Need at least an input and output layer");

		// Save a copy of the structure
		this.structure = new int[structure.length];
		System.arraycopy(structure, 0, this.structure, 0, structure.length);

		// Initialize random weights
		this.allocateData();
		this.randomizeWeights();

	}

	/**
	 * Load a neural net from a file.
	 * 
	 * @param filename
	 *            a neural net file.
	 */
	public NeuralNet(String filename) throws IOException {
		load(filename);
	}

	/** Allocate the arrays to hold weights, activations and errors */
	protected void allocateData() {
		// We need to store the activation for each neuron in every layer
		activations = new double[structure.length][];
		for (int layer = 0; layer < structure.length; layer++)
			activations[layer] = new double[structure[layer]];

		// We need weights for neurons in every layer except the first,
		// so just leave the first entry of the array empty.
		weights = new double[structure.length][][];
		for (int layer = 1; layer < structure.length; layer++) {
			// We have one more weight than inputs from the previous layer
			// for the threshold weight, so each unit has
			// ( structure[layer-1] + 1 ) weights.
			weights[layer] = new double[structure[layer]][structure[layer - 1] + 1];
		}

		// We need the error for neurons in every layer except the first,
		// so just leave the first entry empty, like above.
		errors = new double[structure.length][];
		for (int layer = 1; layer < structure.length; layer++)
			errors[layer] = new double[structure[layer]];
	}

	/** Assign random weights everywhere. */
	public void randomizeWeights() {
		for (int layer = 1; layer < structure.length; layer++)
			for (int unit = 0; unit < structure[layer]; unit++)
				for (int wt = 0; wt < structure[layer - 1] + 1; wt++)
					weights[layer][unit][wt] = Math.random() * 0.02 - 0.01;
	}

	// ////////////////////////////////////////////////////////////////
	// ////// TESTING CODE
	// This code is commented out since it will not work if you
	// assume that the network has no hidden layers. If you
	// implement the full backprop algorithm, uncomment it to
	// test your code.
	// ////////////////////////////////////////////////////////////////

	/**
	 * Use a small neural net to learn the AND, OR and XOR functions. This is
	 * meant as a way to test this class. The network is written to the file
	 * 'and-or-xor-net.txt'
	 */
	/*
	 * public static void main( String argv[] ) { // Create a net with 2 inputs,
	 * 3 outputs and 2 hidden units int structure[] = { 2, 2, 3 }; NeuralNet net =
	 * new NeuralNet( structure ); // Create the training data to learn the AND,
	 * OR and XOR functions // functions (outputs 0, 1 and 2 respectively).
	 * double inputs[][] = { {0,0}, {0,1}, {1,0}, {1,1} }; double outputs[][] = {
	 * {0,0,0}, {0,1,1}, {0,1,1}, {1,1,0} }; // Pick some parameters double
	 * learningRate = 0.1; int numEpochs = 50; int samplesPerEpoch = 1000; // A
	 * place to store the sum squared errors double sse[] = new double[3]; // Do
	 * the training System.out.println( "Epoch\t\tRMS error (AND)\t\t\t" + "RMS
	 * error (OR)\t\t\tRMS error (XOR)" ); System.out.println(
	 * "-----\t\t---------------\t\t\t" + "--------------\t\t\t---------------" );
	 * for( int epoch = 0; epoch < numEpochs; epoch++ ) { // Learn on the whole
	 * training set for( int example = 0; example < samplesPerEpoch; example++ )
	 * net.train(inputs[example%4], outputs[example%4], learningRate); //
	 * Compute the error on the training set Arrays.fill( sse, 0 ); for( int
	 * example = 0; example < inputs.length; example++ ) { double out[] =
	 * net.query( inputs[example] ); for( int i = 0; i < 3; i++ ) sse[i] +=
	 * (outputs[example][i] - out[i]) * (outputs[example][i] - out[i]); } //
	 * Print some output System.out.print( epoch+1 ); for( int i = 0; i < 3; i++ )
	 * System.out.print( "\t\t" + Math.sqrt( sse[i] / 4 ) ); System.out.print(
	 * '\n' ); }
	 * 
	 * System.out.println( "=============================" ); // Save try {
	 * net.save( "and-or-xor-net.txt" ); } catch (Exception e) {
	 * e.printStackTrace(); return; } // Load NeuralNet net2 = null; try { net2 =
	 * new NeuralNet( "and-or-xor-net.txt" ); } catch (Exception e) {
	 * e.printStackTrace(); return; } // See if it still works
	 * System.out.println( "=============================" );
	 * System.out.println( " A \t B \tA AND B\t\t\tA OR B\t\t\tA XOR B" );
	 * System.out.println( "---\t---\t-------\t\t\t------\t\t\t-------" ); for(
	 * int example = 0; example < inputs.length; example++ ) { double out[] =
	 * net2.query( inputs[example] ); System.out.println( inputs[example][0] +
	 * "\t" + inputs[example][1] + "\t" + out[0] + "\t" + out[1] + "\t" + out[2] ); } }
	 */

	// //////////////////////////////////////////////////////////////////
	// ////// INPUT / OUTPUT METHODS
	// //////////////////////////////////////////////////////////////////
	/** Write the network to a file. */
	public void save(String filename) throws IOException {
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(filename));
			out.println("# Feed forward neural net ");
			out.println("# Number of layers followed by number of"
					+ " units in each layer ");
			printStructure(out);
			for (int layer = 1; layer < structure.length; layer++)
				for (int unit = 0; unit < structure[layer]; unit++) {
					out.println("# Weights for unit " + unit + " of layer "
							+ layer);
					printWeights(out, layer, unit);
					out.print('\n');
				}
			System.out.println("Wrote network to file '" + filename + "'.");
		} catch (IOException e) {
			System.err.println("Error writing network to file '" + filename
					+ "'.");
			throw e;
		} finally {
			out.close();
		}
	}

	/** Print the net structure array */
	public void printStructure(PrintStream out) {
		out.print(structure.length);
		for (int i = 0; i < structure.length; i++) {
			out.print(",\t");
			out.print(structure[i]);
		}
		out.print('\n');
	}

	/** Print the specified weights */
	public void printWeights(PrintStream out, int layer, int unit) {
		for (int i = 0; i <= structure[layer - 1]; i++) {
			if (i > 0)
				out.print(",\t");
			out.print(weights[layer][unit][i]);
		}
	}

	/** Print all the weights on a single line */
	public void printWeights(PrintStream out) {
		for (int layer = 1; layer < structure.length; layer++) {
			for (int unit = 0; unit < structure[layer]; unit++) {
				if (layer > 1 || unit > 0)
					out.print(",\t");
				printWeights(out, layer, unit);
			}
		}
	}

	/** Load the network from a file */
	public void load(String filename) throws IOException {
		FileInputStream in = null;
		try {
			in = new FileInputStream(filename);
			StreamTokenizer tok = new StreamTokenizer(new InputStreamReader(in));
			tok.whitespaceChars(',', ','); // Ignore commas
			tok.parseNumbers();
			tok.eolIsSignificant(true);
			tok.commentChar('#');
			while (tok.nextToken() == StreamTokenizer.TT_EOL)
				;
			int numLayers = (int) tok.nval;
			structure = new int[numLayers];
			for (int i = 0; i < numLayers; i++) {
				while (tok.nextToken() == StreamTokenizer.TT_EOL)
					;
				if (tok.ttype == StreamTokenizer.TT_NUMBER)
					structure[i] = (int) tok.nval;
				else
					throw new IOException("Invalid neural network file: '"
							+ filename + "'.");
			}
			allocateData();
			// Move to the first number token
			while (tok.nextToken() == StreamTokenizer.TT_EOL)
				;
			for (int layer = 1; layer < structure.length; layer++)
				for (int unit = 0; unit < structure[layer]; unit++)
					readWeights(tok, layer, unit);
			System.out.println("Read network from file '" + filename + "'.");
		} catch (IOException e) {
			System.err.println("Error reading network from file '" + filename
					+ "'.");
			throw e;
		} finally {
			in.close();
		}
	}

	private void readWeights(StreamTokenizer tok, int layer, int unit)
			throws IOException {
		for (int i = 0; i <= structure[layer - 1]; i++) {
			if (tok.ttype == StreamTokenizer.TT_NUMBER) {
				weights[layer][unit][i] = tok.nval;
				while (tok.nextToken() == StreamTokenizer.TT_EOL)
					;
				// StreamTokenizer doesn't support scientific notation!!!
				if (tok.ttype == StreamTokenizer.TT_WORD
						&& (tok.sval.startsWith("E") || tok.sval
								.startsWith("e"))) {
					double exp = Integer.parseInt(tok.sval.substring(1));
					weights[layer][unit][i] *= Math.pow(10, exp);
					while (tok.nextToken() == StreamTokenizer.TT_EOL)
						;
				}
			} else
				throw new IOException("Invalid neural network file.");
		}
	}
}