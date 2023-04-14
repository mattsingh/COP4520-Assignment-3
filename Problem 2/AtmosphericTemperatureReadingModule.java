import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AtmosphericTemperatureReadingModule {
	private static final int NUM_SENSORS = 8;
	public static final long DURATION_SECONDS = 60;
	public static long startTime;
	public static long endTime;

	public static AtomicBoolean isDone = new AtomicBoolean(false);

	public static void main(String[] args) throws InterruptedException {
		ExecutorService threadExecutor = Executors.newFixedThreadPool(NUM_SENSORS);
		Readings sharedMem = new Readings();

		startTime = System.currentTimeMillis();
		endTime = startTime + (long) (1000 * DURATION_SECONDS);

		for (int i = 0; i < NUM_SENSORS; i++) {
			threadExecutor.execute(new TemperatureSensor(sharedMem));
		}

		while (System.currentTimeMillis() < endTime) {
			// Print readings every minute
			Thread.sleep((long) (1000 * 60));
			sharedMem.printReadings();
		}

		isDone.set(true);

		threadExecutor.shutdown();
		threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}
}

class TemperatureSensor implements Runnable {

	Readings sharedMem;

	public TemperatureSensor(Readings sharedMem) {
		this.sharedMem = sharedMem;
	}

	@Override
	public void run() {
		int interval = 1;
		while (!AtmosphericTemperatureReadingModule.isDone.get()) {
			try {
				// Read in 10 second intervals
				for (int i = 0; i < 10; i++) {
					// wait one second
					Thread.sleep(1000);
					// read temperature (random number between -100 and 70)
					int temperature = (int) (Math.random() * 170 - 100);
					// send temperature to shared memory space
					sharedMem.recordTemperature(temperature, interval);
				}
				interval++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}

class Readings {

	ConcurrentSkipListSet<Integer> topFive = new ConcurrentSkipListSet<Integer>();
	ConcurrentSkipListSet<Integer> bottomFive = new ConcurrentSkipListSet<Integer>();

	public void recordTemperature(int temperature, int interval) {
		int reading = 1000 * (temperature + 100) + interval;
		if (topFive.size() < 4) {
			topFive.add(reading);
		} else if (reading > topFive.first()) {
			topFive.pollFirst();
			topFive.add(reading);
		}

		if (bottomFive.size() < 4) {
			bottomFive.add(reading);
		} else if (reading < bottomFive.last()) {
			bottomFive.pollLast();
			bottomFive.add(reading);
		}
	}

	public void clearReadings() {
		topFive.clear();
		bottomFive.clear();
	}

	public void printReadings() {
		ConcurrentSkipListSet<Integer> topFiveCopy = new ConcurrentSkipListSet<Integer>(topFive);
		ConcurrentSkipListSet<Integer> bottomFiveCopy = new ConcurrentSkipListSet<Integer>(bottomFive);
		int largestTemp = topFiveCopy.first();
		int smallestTemp = bottomFiveCopy.last();
		System.out.print("Top 5: \t\t");
		for (Integer reading : topFiveCopy) {
			System.out.print((reading / 1000) - 100 + "F, ");
			if ((reading / 1000) - 100 > (largestTemp / 1000) - 100) {
				largestTemp = reading;
			}
		}
		System.out.println();
		System.out.println("Interval of largest temperature: " + Math.abs(largestTemp) % 1000);

		System.out.print("Bottom 5: \t");
		for (Integer reading : bottomFiveCopy) {
			System.out.print((reading / 1000) - 100 + "F, ");
			if ((reading / 1000) - 100 < (smallestTemp / 1000) - 100) {
				smallestTemp = reading;
			}
		}
		System.out.println();
		System.out.println("Interval of smallest temperature: " + Math.abs(smallestTemp) % 1000);
	}
}