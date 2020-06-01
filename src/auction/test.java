package auction;

import java.util.Random;

public class test {
	
	public boolean getRandomBoolean() {
	    Random random = new Random();
	    return random.nextBoolean();
	}
	
	private int evaluateAction() {
		// Simulate an evaluation by generating a random number
		Random r = new Random();
		int low = 100;
		int high = 300;
		return r.nextInt(high-low) + low;
	}

	private boolean performAction() {
		// Simulate action execution by generating a random number
		return (Math.random() > 0.2);
	}
	
	public static void main(String[] args) {
		
		test test = new test();
		System.out.println(test.getRandomBoolean());
		
		System.out.println(test.evaluateAction());
		
		System.out.println(test.performAction());
		
		int i = Integer.parseInt("100");
		System.out.println(i + 2);
	}

}
