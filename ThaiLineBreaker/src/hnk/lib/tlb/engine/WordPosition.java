package hnk.lib.tlb.engine;

public class WordPosition {
	private final int start, end;

	public WordPosition(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public boolean isEmpty() {
		return start == end;
	}

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}

	public int length() {
		return end - start;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(start).append(", ").append(end)
				.append(']').toString();
	}
}
