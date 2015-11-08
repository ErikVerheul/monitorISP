/*
 * A FIFO queue to store the most recent log entries.
 */
package nl.verheulconsultants.monitorisp.service;

import java.util.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author erik
 */
public class OutputQueue {
    static final Logger logger = LoggerFactory.getLogger(OutputQueue.class);
    private int size = 50;
    private int maxSize = 1000;
    BlockingQueue<String> queue;

    OutputQueue() {
        queue = new LinkedBlockingQueue<String>();
    }

    OutputQueue(int size) {
        queue = new LinkedBlockingQueue<String>();
        if (size > 0 && size <= maxSize) {
            this.size = size;
        }
    }

    private void trimSize(int size) {
        while (size < queue.size()) {
            queue.remove();
        }
    }

    public void setSize(int newSize) throws IllegalArgumentException {
        if (newSize <= 0 || newSize > maxSize) {
            throw new IllegalArgumentException("Output lines range must be >= 1 and <= " + maxSize);
        }
        size = newSize;
        trimSize(size);
    }

    public int getSize() {
        return size;
    }

    public void add(String s) {
        while (queue.size() >= size) {
            queue.remove();
        }
        try {
            queue.put(s);
        } catch (InterruptedException e) {
            System.out.println(s + " not added to queue, error: " + e);
        }
    }

    public String getAll() {
        StringBuilder buf = new StringBuilder(10000);
        Iterator<String> it = queue.iterator();
        while (it.hasNext()) {
            buf.append(it.next());
            buf.append("\n");
        }
        return buf.toString();
    }
}
