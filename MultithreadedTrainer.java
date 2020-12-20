import java.util.Random;

public class MultithreadedTrainer {
	
	
	public static String getPathToFile() {
		return MultithreadedTrainer.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6).replace("/", "\\");
	}
	
	public static byte[] playGame(Network network1, Network network2) {
		boolean gameOver = false;
		int whiteScore;
		int blackScore;
		byte result = 1;
		Board game = new Board();
		int moveCount = 0;
		boolean currentMove = true;
		while(!gameOver) {
			if(moveCount == 200) {
				whiteScore = game.getPlayerScore(false);
				blackScore = game.getPlayerScore(true);
				if(whiteScore > blackScore) {
					return new byte[]{0, 1};
				} else if(blackScore > whiteScore) {
					return new byte[]{1, 0};
				} else {
					return new byte[]{0, 0};
				}
			}
			if(currentMove) {
				byte[] bestMove = network1.getBestMove(game);
				result = game.doMove(bestMove[0], bestMove[1], bestMove[2], bestMove[3], bestMove[4]);
			} else {
				byte[] bestMove = network2.getBestMove(game);
				result = game.doMove(bestMove[0], bestMove[1], bestMove[2], bestMove[3], bestMove[4]);
			}
			moveCount++;
			switch(result) {
				case 2:
					return new byte[]{0, 0};
				case 3:
					return new byte[]{0, 0};
				case 4:
					whiteScore = game.getPlayerScore(false);
					blackScore = game.getPlayerScore(true);
					if(whiteScore > blackScore) {
						return new byte[]{0, 1};
					} else if(blackScore > whiteScore) {
						return new byte[]{1, 0};
					} else {
						return new byte[]{0, 0};
					}
				case 5:
					whiteScore = game.getPlayerScore(false);
					blackScore = game.getPlayerScore(true);
					if(whiteScore > blackScore) {
						return new byte[]{0, 1};
					} else if(blackScore > whiteScore) {
						return new byte[]{1, 0};
					} else {
						return new byte[]{0, 0};
					}
				case 6:
					return new byte[]{1, 0};
				case 7:
					return new byte[]{0, 1};
			}
			currentMove = !currentMove;
		}
		return null;
	}
	
	public static int fightNetworks(Network network1, Network network2) {
		byte[] scores = new byte[]{0, 0};
		byte[] scoreAdding;
		for(int games = 0; games < 1; games++) {
			scoreAdding = playGame(network1, network2);
			scores[0] += scoreAdding[0];
			scores[1] += scoreAdding[1];
			scoreAdding = playGame(network2, network1);
			scores[1] += scoreAdding[0];
			scores[0] += scoreAdding[1];
		}
		return scores[0] >= scores[1] ? 0 : 1;
	}
	
	public static Network doRounds(Network currentBest, int threads, int gamesToPlay, int rounds, int min, int max, MultithreadedInformation info) {
		System.out.println("Beginning a new super-round...");
		if(threads != 0) {
			Network best = currentBest.makeCopy();
			System.out.println("Creating " + threads + " threads, to play " + rounds + " rounds, " + gamesToPlay + " games per round.");
			Threader[] threadArray = new Threader[threads];
			
			for(int i = 0; i < threads; i++) {
				threadArray[i] = new Threader(i, info, gamesToPlay, rounds, min, max);
			}
			System.out.println("Starting threads...");
			for(int i = 0; i < threads; i++) {
				threadArray[i].start();
			}
			for(int i = 0; i < threads; i++) {
				try {
					threadArray[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			best = info.getBest().makeCopy();
			return best;
		} else {
			Network best = currentBest.makeCopy();
			for(int round = 0; round < rounds; round++) {
				for(int i = 1; i < gamesToPlay+1; i++) {
					Network challenger = best.makeAlteredCopy(i*500/gamesToPlay);
					if(fightNetworks(best, challenger) == 1) {
						best = challenger.makeCopy();
					}
					//System.out.println((i) + "/" + gamesToPlay + " games played on round " + (round+1) + "/" + rounds + ".");
				}
				System.out.println((round+1) + "/" + rounds + " rounds played.");
			}
			return best;
		}
	}
	
	public static void main(String[] args) {
//		Random random = new Random();
//		Network best = new Network(new int[] {408, 500, 250, 250, 100, 50,  1}, 
//				                   new Activation[]{Activation.LINEAR, Activation.LINEAR, Activation.DIVIDE, Activation.LCAP, Activation.DIVIDE, Activation.LINEAR, Activation.DIVIDE},
//				                   random);
		Network best = Network.loadFromFile(getPathToFile());
		int SUPERROUNDS = 50;
		// Supposed to play roughly 40 minutes of games before each save.
		int THREADS = 8;
		int GAMESTOPLAYPERROUND = 16;
		int ROUNDSPERSAVE = 40;
		int gamesPerSuperRound = THREADS * GAMESTOPLAYPERROUND * ROUNDSPERSAVE;
		long startTime = 0;
		long stopTime = 0;
		int[] winnersSetPerSuperRound = new int[SUPERROUNDS];
		for(int i = 1; i < SUPERROUNDS+1; i++) {
			startTime = System.nanoTime();
			MultithreadedInformation info = new MultithreadedInformation(best);
			best = doRounds(best, THREADS, GAMESTOPLAYPERROUND, ROUNDSPERSAVE, 10, 100, info);
			System.out.println(i + "/" + SUPERROUNDS + " super-rounds played");
			best.saveToFile(getPathToFile());
			stopTime = System.nanoTime();
			double timeToPlay = ((double) stopTime - (double) startTime)/1000000000;
			System.out.println(gamesPerSuperRound + " games played in " + timeToPlay + " seconds at " + gamesPerSuperRound/timeToPlay + " games per second.");
			System.out.println(info.winnerSets + "/" + gamesPerSuperRound + " winners were set out of games played.");
			winnersSetPerSuperRound[i-1] = info.winnerSets;
		}
		for(int i = 0; i < winnersSetPerSuperRound.length; i++) {
			System.out.println("On super round " + (i+1) + ", " + winnersSetPerSuperRound[i] + " winners were set.");
		}
	}
}
