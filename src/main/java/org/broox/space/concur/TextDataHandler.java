package org.broox.space.concur;

import java.util.concurrent.BlockingQueue;

import org.broox.space.inter.TextProcessor;

public class TextDataHandler implements Runnable {
	private final BlockingQueue<String> queue;
	private final TextProcessor processor;
	private final String POISON_PILL = "3829-5434";

    public TextDataHandler(BlockingQueue<String> queue, TextProcessor processor) {
        this.queue = queue;
        this.processor = processor;
    }
    
	public boolean takeData(String workItem) {
		try {
			queue.offer(workItem);
		} catch (NullPointerException e) {
			System.out.println("Passed Poison Pill.");
			queue.offer(POISON_PILL);
		}
		return true;
	}

    @Override
    public void run() {
        while (true) {
            try {
                String workItem = queue.take();
                if (workItem != POISON_PILL) processor.processText(workItem);
                else break;
            } catch (InterruptedException e) {
            	System.out.println(e);
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Writer Done.");
    }

}
