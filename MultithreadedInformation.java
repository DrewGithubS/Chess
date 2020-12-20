import java.util.concurrent.locks.ReentrantLock;

public class MultithreadedInformation {
	Network currentBest;
	private ReentrantLock mutex = new ReentrantLock();
	int winnerSets;
	
	public MultithreadedInformation(Network currentBest) {
		this.currentBest = currentBest;
		this.winnerSets = 0;
	}
	
	public void setWinner(Network winner) {
		this.currentBest = winner.makeCopy();
		winnerSets++;
	}
	
	public Network getBest() {
		return this.currentBest;
		
	}
	
	public void takeLock() {
		mutex.lock();
	}
	
	public void releaseLock() {
		mutex.unlock();
	}
}
