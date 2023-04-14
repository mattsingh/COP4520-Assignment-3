import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TheBirthdayPresentsParty {
	// public static final int NUM_PRESENTS = 500_000;
	public static final int NUM_PRESENTS = 10;
	public static final int NUM_SERVANTS = 4;
	public static final boolean ENABLE_SERVANT_PRINTS = false;
	public static AtomicInteger thankYouCardCount = new AtomicInteger(0);

	public static void main(String[] args) throws InterruptedException {
		ExecutorService threadExecutor = Executors.newFixedThreadPool(NUM_SERVANTS);

		// Presents are identified by their Integer (which represents the present's tag)
		List<Integer> tempBag = new ArrayList<>();
		Queue<Integer> unorderedBag;
		PresentChain orderedChain = new PresentChain();

		for (int i = 0; i < NUM_PRESENTS; i++) {
			tempBag.add(i);
		}

		Collections.shuffle(tempBag);

		unorderedBag = new LinkedBlockingQueue<>(tempBag);

		System.out.println("Commanding servants to write Thank You cards...");

		// Record start time
		long startTime = System.currentTimeMillis();

		// Spawn servant threads
		for (int i = 0; i < NUM_SERVANTS; i++) {
			threadExecutor.execute(new Servant(i, unorderedBag, orderedChain));
		}

		threadExecutor.shutdown();
		threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		System.out.println("Thank you card count: " + thankYouCardCount);
		if (thankYouCardCount.get() == NUM_PRESENTS) {
			System.out.println("All presents were accounted for!");
			System.out.println("Time taken: " + (System.currentTimeMillis() - startTime)
					+ "ms");
		} else {
			System.out.println("Some presents were not accounted for!");
			throw new RuntimeException("Some presents were not accounted for!");
		}
	}
}

class Servant implements Runnable {
	private int id;
	private Queue<Integer> unorderedBag;
	private PresentChain orderedChain;

	public Servant(int id, Queue<Integer> unorderedBag, PresentChain orderedChain) {
		this.id = id;
		this.unorderedBag = unorderedBag;
		this.orderedChain = orderedChain;
	}

	@Override
	public void run() {
		while (!unorderedBag.isEmpty()) {
			takeFromUnorderedBagAndAddToChain();

			// Simulate that Minotaur asks servants to check for a present 1/5 of the time
			int random = (int) (Math.random() * 5);
			if (random == 0)
				checkIfPresentWasInChain();

			writeThankYouCardAndRemoveFromChain();
		}
	}

	// Action 1
	private void takeFromUnorderedBagAndAddToChain() {
		Integer present = unorderedBag.poll();
		// System.out.println("Servant " + id + " took present " + present);
		if (present != null)
			orderedChain.add(present);
	}

	// Action 2
	private void writeThankYouCardAndRemoveFromChain() {
		Integer present = orderedChain.poll();
		// System.out.println("Servant " + id + " wrote thank you card for present " + present);
		if (present != null) {
			TheBirthdayPresentsParty.thankYouCardCount.incrementAndGet();
		}
	}

	// Action 3
	private void checkIfPresentWasInChain() {
		int present = (int) (Math.random() * TheBirthdayPresentsParty.NUM_PRESENTS);
		// System.out.println("Servant " + id + " is checking if present " + present + " is in chain");
		boolean contained = orderedChain.contained(present);
		if (contained) {
			// System.out.println("Servant " + id + " found present " + present);
		} else {
			// System.out.println("Servant " + id + " did not find present " + present);
		}
	}

}

// Modified Lazy List
class PresentChain {

	private Node head;

	public PresentChain() {
		head = new Node(Integer.MIN_VALUE);
		head.next = new Node(Integer.MAX_VALUE);
	}

	public boolean add(int data) {
		while (true) {
			Node pred = head;
			Node curr = head.next;
			while (curr.data < data) {
				pred = curr;
				curr = curr.next;
			}
			pred.lock.lock();
			try {
				curr.lock.lock();
				try {
					if (validate(pred, curr)) {
						if (curr.data == data) {
							return false;
						} else {
							Node NewNode = new Node(data);
							NewNode.next = curr;
							pred.next = NewNode;
							return true;
						}
					}
				} finally {
					curr.lock.unlock();
				}
			} finally {
				pred.lock.unlock();
			}
		}
	}

	public int poll() {
		while (true) {
			Node pred = head;
			Node curr = head.next;
			if (curr == null) {
				return 8008135; // list is empty, return null
			}
			pred.lock.lock();
			try {
				curr.lock.lock();
				try {
					if (validate(pred, curr)) {
						curr.marked = true;
						pred.next = curr.next;
						return curr.data;
					}
				} finally {
					curr.lock.unlock();
				}
			} finally {
				pred.lock.unlock();
			}
		}
	}

	public boolean contained(int data) {
		Node curr = head;
		while (curr.data < data)
			curr = curr.next;
		return curr.data == data;
	}

	private boolean validate(Node pred, Node curr) {
		return !pred.marked && !curr.marked && pred.next == curr;
	}

	private class Node {
		private int data;
		private Node next;
		private boolean marked = false;
		Lock lock = new ReentrantLock();

		public Node(int data) {
			this.data = data;
		}
	}
}