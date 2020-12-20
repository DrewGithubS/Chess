
public class Threader extends Thread {
	int threadNumber;
	MultithreadedInformation info;
	Network currentBest;
	int gamesToPlay;
	int rounds;
	int min;
	int max;
	
	public Threader(int threadNumber, MultithreadedInformation info, int gamesToPlay, int rounds, int min, int max) {
		this.threadNumber = threadNumber;
		this.info = info;
		this.currentBest = this.info.currentBest.makeCopy();
		this.gamesToPlay = gamesToPlay;
		this.rounds = rounds;
		this.min = min;
		this.max = max;
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
			if(moveCount == 100) {
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
		byte[] scoreAdding = new byte[2];
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
	
	public void run() {
		for(int i = 0; i < this.rounds; i++) {
			for(int games = 1; games < this.gamesToPlay+1; games++) {
				this.info.takeLock();
				this.currentBest = this.info.getBest();
				this.info.releaseLock();
				Network challenger = this.currentBest.makeAlteredCopy(min + ((max-min)*games/(this.gamesToPlay+1)));
				if(fightNetworks(this.currentBest, challenger) == 1) {
//					System.out.println("Better network found on thread " + this.threadNumber + ".");
					this.info.takeLock();
					this.info.setWinner(challenger);
					this.info.releaseLock();
				}
			}
			System.out.print((i+1) % 10 == 0 ? ((i+1) + "/" + this.rounds + " rounds played on thread " + this.threadNumber + ".\n") : "");
		}
	}
}
