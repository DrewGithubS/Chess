public class Linker {
	boolean isEnd = true;
	Linker next;
	Piece[][] value;
	
	public Linker() {}
	
	public Linker(Piece[][] value2) {
		this.value = value2;
	}
	
	public Linker copy(Linker link) {
		Linker result = new Linker(link.get(0));
		if(!link.isEnd) {
			link = link.next;
		}
		while(!link.isEnd) {
			result.append(link.get(0).clone());
			link = link.next;
		}
		return result;
	}
	
	public void append(Piece[][] value) {
		Linker next = this;
		while(!next.isEnd) {
			next = next.next;
		}
		next.isEnd = false;
		next.next = new Linker(value);
	}
	
	public void appendLink(Linker value) {
		Linker next = this;
		while(!next.isEnd) {
			next = next.next;
		}
		next.isEnd = false;
		next.next = value;
	}
	
	public Piece[][] get(int index) {
		Linker next = this;
		while(index != 0) {
			next = next.next;
			index--;
		}
		return next.value;
	}
	
	public Linker getLink(int index) {
		Linker next = this.next;
		while(index != 0) {
			next = next.next;
			index--;
		}
		return next;
	}
	
	public void set(int index, Piece[][] value) {
		Linker next = this.next;
		while(index != 0) {
			next = next.next;
			index--;
		}
		next.value = value;
	}
	
	public void remove(int index) {
		if(index == 0) {
			this.next = this.isEnd ? null : this.next.next;
		} else {
			this.next.remove(index-1);
		}
	}
	
	public void cut(int index) {
		if(index == 0) {
			this.next = null;
			this.isEnd = true;
		} else {
			this.next.cut(index-1);
		}
	}
	
	public int getLength() {
		Linker next = this;
		int counter = 0;
		while(!next.isEnd) {
			next = next.next;
			counter++;
		}
		return counter;
	}
	
	public Piece[][][] toArray() {
		int length = this.getLength();
		Piece[][][] output = new Piece[length+1][8][8];
		Linker next = this;
		for(int i = 0; i < length+1; i++) {
			output[i] = next.value;
			next = next.next;
		}
		return output;
	}
}
