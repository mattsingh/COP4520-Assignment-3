import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AtmosphericTemperatureReadingModule {
    private static final int NUM_SENSORS = 8;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService threadExecutor = Executors.newFixedThreadPool(NUM_SENSORS);

        for (int i = 0; i < NUM_SENSORS; i++) {
            threadExecutor.execute(new TemperatureSensor());
        }

        threadExecutor.shutdown();
        threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}

class TemperatureSensor implements Runnable {

    Readings sharedMem;

    public TemperatureSensor() {
        sharedMem = new Readings();
    }

    @Override
    public void run() {
        while (true) {
            try {
                // wait one minute
                Thread.sleep(1000 * 60);
                // read temperature (random number between -100 and 70)
                int temperature = (int) (Math.random() * 170 - 100);
                // send temperature to shared memory space
                sharedMem.recordTemperature(temperature);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}

class Readings {

    

	public void recordTemperature(double temperature) {
	}

}