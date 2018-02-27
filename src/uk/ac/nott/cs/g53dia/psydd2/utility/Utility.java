package uk.ac.nott.cs.g53dia.psydd2.utility;

public class Utility {
	
	/**
	 * Generate random integer in the given range (inclusive)
	 */
	public static int randBetween(int min, int max) {
		int range = Math.abs(max - min) + 1;
		return (int) (Math.random() * range) + (min <= max ? min : max);
	}
	
	public static int sign(int n) {
		return Integer.compare(n, 0);
	}
	
	/**
	 * @return True 50% of the time
	 */
	public static boolean fiftyFifty() {
		return Math.random() < 0.5;
	}
}
