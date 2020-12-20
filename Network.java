import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class Network {
	
	Activation[] layerActivations;
	double[][] values;
	double[][] biases;
	double[][][] weights;
	Random random;

	
	// This is only used when loading from a file
	public Network() {
		this.random = new Random();
	}
	
	public Network(int[] layersList, Activation[] layerActivations, Random random) {
		this.random = random;
		this.layerActivations = layerActivations;
		this.values = new double[layersList.length][];
		this.biases = new double[layersList.length][];
		this.weights = new double[layersList.length-1][][];
		for(int i = 0; i < layersList.length-1; i++) {
			this.values[i] = new double[layersList[i]];
			this.biases[i] = new double[layersList[i]];
			this.weights[i] = new double[layersList[i]][layersList[i+1]];
		}
		this.values[this.values.length-1] = new double[layersList[this.values.length-1]];
		this.biases[this.biases.length-1] = new double[layersList[this.biases.length-1]];
		this.init();
	}
	
	public void init() {
		this.resetValues();
		for(int layer = 0; layer < this.weights.length; layer++) {
			for(int neuron = 0; neuron < this.weights[layer].length; neuron++) {
				for(int weight = 0; weight < this.weights[layer][neuron].length; weight++) {
					this.weights[layer][neuron][weight] = (random.nextDouble()*2)-1;
				}
			}
		}
		for(int layer = 0; layer < this.biases.length; layer++) {
			for(int neuron = 0; neuron < this.biases[layer].length; neuron++) {
				this.biases[layer][neuron] = (random.nextDouble()*2)-1;
				this.values[layer][neuron] = 0;
			}
		}
	}
	
	public double[] getInputFromPiece(Piece piece) {
		double[] output = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		switch(piece.type) {
			case PAWN:
				output[5] = piece.color ? -1 : 1;
			case KNIGHT:
				output[4] = piece.color ? -1 : 1;
			case BISHOP:
				output[3] = piece.color ? -1 : 1;
			case ROOK:
				output[2] = piece.color ? -1 : 1;
			case QUEEN:
				output[1] = piece.color ? -1 : 1;
			case KING:
				output[0] = piece.color ? -1 : 1;
			case EMPTY:
			default:
				return output;
		}
	}
	
	public double[] getInputsFromPosition(Piece[][] position, int fiftyMoveRule, int turn, boolean[][] enPassants, boolean[][] rooksMoved, boolean[] kingsMoved) {
		double[] inputs = new double[408];
		int count = 0;
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				double[] pieceInput = getInputFromPiece(position[y][x]);
				for(int i = 0; i < 6; i++) {
					inputs[i + count] = pieceInput[i];
				}
				count += 6;
			}
		}
		inputs[384] = fiftyMoveRule;
		inputs[385] = turn;
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 8; j++) {
				inputs[386 + i*8 + j] = enPassants[i][j] ? 1 : -1;
			}
		}
		inputs[402] = rooksMoved[0][0] ? 1 : -1;
		inputs[403] = rooksMoved[0][1] ? 1 : -1;
		inputs[404] = rooksMoved[1][0] ? 1 : -1;
		inputs[405] = rooksMoved[1][1] ? 1 : -1;
		inputs[406] = kingsMoved[0] ? 1 : -1;
		inputs[407] = kingsMoved[1] ? 1 : -1;
		return inputs;
	}
	
	public byte[] getBestMove(Board game) {
		byte[][] bestMoves = new byte[400][5];
		double[] bestMoveScores = new double[400];
		int count = 0;
		for(byte y = 0; y < 8; y++) {
			for(byte x = 0; x < 8; x++) {
				if(game.board[y][x].type != Type.EMPTY && game.board[y][x].color == game.turn) {
					byte[][] possibleMoves = game.getPossibleMovesFromCoords(y, x);
					if(possibleMoves != null) {
						for(int i = 0; i < possibleMoves.length; i++) {
							if(game.board[y][x].type == Type.PAWN && (possibleMoves[i][0] == 0 || possibleMoves[i][0] == 7)) {
								for(byte promotion = 0; promotion < 4; promotion++) {
									bestMoves[count++] = new byte[]{y, x, possibleMoves[i][0], possibleMoves[i][1], promotion};
									Board tryMove = game.tryMove(y, x, possibleMoves[i][0], possibleMoves[i][1], promotion);
									bestMoveScores[count] = this.feedForward(getInputsFromPosition(tryMove.board, tryMove.fiftyMoveRule, tryMove.turn ? 1 : -1, tryMove.enPassants, tryMove.rooksMoved, tryMove.kingsMoved))[0];
								}
							} else {
								bestMoves[count++] = new byte[]{y, x, possibleMoves[i][0], possibleMoves[i][1], 0};
								Board tryMove = game.tryMove(y, x, possibleMoves[i][0], possibleMoves[i][1], (byte) 0);
								bestMoveScores[count] = this.feedForward(getInputsFromPosition(tryMove.board, tryMove.fiftyMoveRule, tryMove.turn ? 1 : -1, tryMove.enPassants, tryMove.rooksMoved, tryMove.kingsMoved))[0];
							}
						}
					}
				}
			}
		}
		
		byte[] bestMove = bestMoves[0];
		double bestMoveScore = bestMoveScores[0];
		for(int i = 1; i < count; i++) {
			if(bestMoveScores[i] * (game.turn ? -1 : 1) > bestMoveScore) {
				bestMove = bestMoves[i];
				bestMoveScore = bestMoveScores[i];
			}
		}
		return bestMove;
	}
	
	public double activate(double num, Activation type) {
		switch(type) {
			case SIGMOID:
				return 1/(1 + Math.pow(Math.E, (-num)));
			case RELU:
				return num < 0 ? 0 : (num > 2 ? 2 : num);
			case LINEAR:
				return num;
			case BINARY:
				return num <= 0 ? 0 : 1;
			case LCAP:
				return num < -1 ? -1 : (num > 1 ? 1 : num);
			case DIVIDE:
				return num/100;
			default:
				System.out.println("ERROR: Activation function reached a default case.");
				return -1;
		}
	}
	
	public Network makeCopy() {
		Network network = new Network();
		double[][] biases = new double[this.biases.length][];
		double[][] values = new double[this.biases.length][];
		for(int i = 0; i < this.biases.length; i++) {
			biases[i] = new double[this.biases[i].length];
			values[i] = new double[this.biases[i].length];
			for(int j = 0; j < this.biases[i].length; j++) {
				biases[i][j] = this.biases[i][j] + 0;
				values[i][j] = 0;
			}
		}
		double[][][] weights = new double[this.weights.length][][];
		for(int i = 0; i < this.weights.length; i++) {
			weights[i] = new double[this.weights[i].length][];
			for(int j = 0; j < this.weights[i].length; j++) {
				weights[i][j] = new double[this.weights[i][j].length];
				for(int k = 0; k < this.weights[i][j].length; k++) {
					weights[i][j][k] = this.weights[i][j][k] + 0;
				}
			}
		}
		network.biases = biases;
		network.weights = weights;
		network.layerActivations = this.layerActivations;
		network.values = values;
		network.random = new Random();
		return network;
	}
	
	public Network makeAlteredCopy(double changeAmount) {
		Network output = makeCopy();
		for(int layer = 0; layer < output.weights.length; layer++) {
			for(int neuron = 0; neuron < output.weights[layer].length; neuron++) {
				for(int weight = 0; weight < output.weights[layer][neuron].length; weight++) {
					output.weights[layer][neuron][weight] += (this.random.nextDouble()*2-1)/changeAmount;
				}
			}
		}
		for(int layer = 0; layer < output.biases.length; layer++) {
			for(int neuron = 0; neuron < output.biases[layer].length; neuron++) {
				output.biases[layer][neuron] += (this.random.nextDouble()*2-1)/changeAmount;
			}
		}
		return output;
	}
	
	public double[] feedForward(double[] firstLayer) {
		this.resetValues();
		// Trusting user input here.
		for(int i = 0; i < firstLayer.length; i++) {
			this.values[0][i] = firstLayer[i];
		}
		for(int layer = 1; layer < this.values.length; layer++) {
			for(int neuron = 0; neuron < this.values[layer-1].length; neuron++) {
				this.values[layer-1][neuron] = activate(this.values[layer-1][neuron] + this.biases[layer-1][neuron], layerActivations[layer-1]);
//				System.out.println(this.weights[layer-1].length + " " + this.values[layer].length);
				for(int weight = 0; weight < this.weights[layer-1][neuron].length; weight++) {
					this.values[layer][weight] += this.values[layer-1][neuron] * this.weights[layer-1][neuron][weight];
				}
			}
		}
		for(int neuron = 0; neuron < this.values[this.values.length-1].length; neuron++) {
			this.values[this.values.length-1][neuron] = activate(this.values[this.values.length-1][neuron], layerActivations[this.values.length-1]);
		}
		return this.values[this.values.length-1];
	}
	
	public void resetValues() {
		for(int layer = 0; layer < this.values.length; layer++) {
			for(int neuron = 0; neuron < this.values[layer].length; neuron++) {
				this.values[layer][neuron] = 0;
			}
		}
	}
	
	// Recursive way to delete a directory with files in it.
	void deleteDir(File file) {
	    File[] allFiles = file.listFiles();
	    if (allFiles != null) {
	        for (File f : allFiles) {
	            if (! Files.isSymbolicLink(f.toPath())) {
	                deleteDir(f);
	            }
	        }
	    }
	    file.delete();
	}
	
	public void writeToFile(String path, String text) {
		try {
			File myObj = new File(path);
			myObj.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter myWriter = new FileWriter(path);
			myWriter.write(text);
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readFromFile(String path) {
        String output = "";
        try {
        	output = new String(Files.readAllLines(Paths.get(path)).get(0));
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return output;
	}
	
	public static double stringToDouble(String string) {
		if(string.charAt(0) == '-') {
			return Double.parseDouble(string.substring(1)) * -1;
		} else {
			return Double.parseDouble(string);
		}
	}
	
	public static Activation activationFromString(String string) {
		switch(string) {
			case "SIGMOID":
				return Activation.SIGMOID;
			case "RELU":
				return Activation.RELU;
			case "LINEAR":
				return Activation.LINEAR;
			case "BINARY":
				return Activation.BINARY;
			case "LCAP":
				return Activation.LCAP;
			case "DIVIDE":
				return Activation.DIVIDE;
			default:
				return null;
		}
	}
	
	public static Network loadFromFile(String path) {
		String oldPath = path;
		path = oldPath + "/t8Mpho83l2Mo9/";
		String[] biasesString1D = readFromFile(path + "biases.txt").split("a");
		String activationsStrings = readFromFile(path + "activations.txt");
		String[] activationsString = activationsStrings.split(" ");
		String[] neuronCountsString = readFromFile(path + "neuronCounts.txt").split(" ");
		String[] weightsString1D = readFromFile(path + "weights.txt").split("a");
		Activation[] activations = new Activation[activationsString.length];
		int[] neuronCounts = new int[neuronCountsString.length];
		for(int layer = 0; layer < activations.length; layer++) {
			activations[layer] = activationFromString(activationsString[layer]);
			neuronCounts[layer] = Integer.parseInt(neuronCountsString[layer]);
		}
		String[][] biasesString2D = new String[biasesString1D.length][];
		for(int layer = 0; layer < biasesString1D.length; layer++) {
			biasesString2D[layer] = biasesString1D[layer].split(" ");
		}
		double[][] biases = new double[biasesString2D.length][];
		for(int layer = 0; layer < biases.length; layer++) {
			biases[layer] = new double[biasesString2D[layer].length];
			for(int neuron = 0; neuron < biasesString2D[layer].length; neuron++) {
				biases[layer][neuron] = stringToDouble(biasesString2D[layer][neuron]);
			}
		}
		
		String[][] weightsString2D = new String[weightsString1D.length][];
		for(int layer = 0; layer < weightsString2D.length; layer++) {
			weightsString2D[layer] = weightsString1D[layer].split("!");
		}
		String[][][] weightsString3D = new String[weightsString2D.length][][];
		for(int layer = 0; layer < weightsString2D.length; layer++) {
			weightsString3D[layer] = new String[weightsString2D[layer].length][];
			for(int neuron = 0; neuron < weightsString2D[layer].length; neuron++) {
				weightsString3D[layer][neuron] = weightsString2D[layer][neuron].split(" ");
			}
		}
		double[][][] weights = new double[weightsString3D.length][][];
		for(int layer = 0; layer < weightsString3D.length; layer++) {
			weights[layer] = new double[weightsString3D[layer].length][];
			for(int neuron = 0; neuron < weightsString3D[layer].length; neuron++) {
				weights[layer][neuron] = new double[weightsString3D[layer][neuron].length];
				for(int weight = 0; weight < weightsString3D[layer][neuron].length; weight++) {
					weights[layer][neuron][weight] = stringToDouble(weightsString3D[layer][neuron][weight]);
				}
			}
		}
		
		double[][] values = new double[neuronCounts.length][];
		for(int i = 0; i < neuronCounts.length; i++) {
			values[i] = new double[neuronCounts[i]];
			for(int j = 0; j < neuronCounts[i]; j++) {
				values[i][j] = 0;
			}
		}
		Network output = new Network();
		output.values = values;
		output.biases = biases;
		output.weights = weights;
		output.layerActivations = activations;
		return output;
	}
	
	public void saveToFile(String path) {
		
		String activations = "";
		String neuronCounts = "";
		for(int layer = 0; layer < this.values.length; layer++) {
			neuronCounts += layer != 0 ? " " : "";
			activations += layer != 0 ? " " : "";
			neuronCounts += this.values[layer].length;
			activations += layerActivations[layer].toString();
		}
		
		String biasString = "";
		for(int biasLayer = 0; biasLayer < this.biases.length; biasLayer++) {
			biasString += biasLayer != 0 ? "a" : "";
			for(int bias = 0; bias < this.biases[biasLayer].length; bias++) {
				biasString += bias != 0 ? " " : "";
				biasString += String.format("%.12f", this.biases[biasLayer][bias]);
			}
		}
		String weightString = "";
		String weightLayer = "";
		String weightNeuron = "";
		for(int weightLayerCount = 0; weightLayerCount < this.weights.length; weightLayerCount++) {
			weightString += weightLayerCount != 0 ? "a" : "";
			weightLayer = "";
			for(int weightNeuronCount = 0; weightNeuronCount < this.weights[weightLayerCount].length; weightNeuronCount++) {
				weightLayer += weightNeuronCount != 0 ? "!" : "";
				weightNeuron = "";
				for(int weight = 0; weight < this.weights[weightLayerCount][weightNeuronCount].length; weight++) {
					weightNeuron += weight != 0 ? " " : "";
					weightNeuron += this.weights[weightLayerCount][weightNeuronCount][weight];
				}
				weightLayer += weightNeuron;
			}
			weightString += weightLayer;
			System.out.println("Saving weights... Layer " + (weightLayerCount+1) + " completed.");
		}
		String oldPath = path;
		// Delete the old network
		path = oldPath + "/t8Mpho83l2Mo9/";
		File file = new File(path);
		deleteDir(file);
		// Save network
		file.mkdir();
		System.out.println("Writing to file...");
		writeToFile(path+"weights.txt", weightString);
		writeToFile(path+"activations.txt", activations);
		writeToFile(path+"neuronCounts.txt", neuronCounts);
		writeToFile(path+"biases.txt", biasString);
		System.out.println("Done!");
	}
	
	public void print() {
		// \t was tabbing a bit too much so I used spaces.
		System.out.println("Network:");
		for(int layer = 0; layer < this.values.length; layer++) {
			System.out.println("   Layer " + (layer+1) + ":");
			System.out.println(layerActivations.length);
			System.out.println("      Activation: " + layerActivations[layer]);
			for(int neuron = 0; neuron < this.values[layer].length; neuron++) {
				System.out.println("      Neuron " + (neuron+1) + ":");
				System.out.println("         Bias: " + this.biases[layer][neuron]);
				System.out.println("         Value: " + this.values[layer][neuron]);
				System.out.println("         Weights:");
				if(layer != this.values.length-1) {
					for(int weight = 0; weight < this.weights[layer][neuron].length; weight++) {
						System.out.println("            Weight " + (weight+1) + ":");
						System.out.println("               Value: " + this.weights[layer][neuron][weight]);
					}
				}
			}
		}
	}
}
