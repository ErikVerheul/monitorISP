/*
 * Fifo.java
 *
 * Created on 13 december 2007, 21:14
 */
package nl.verheulconsultants.monitorisp.service;

/**
 * Class Fifo
 *
 * @author erik
 */
public class Fifo implements FifoMBean {

    OutputQueue outputQueue = null;

    public Fifo() {
    }

    public Fifo(OutputQueue outputQueue) {
        this.outputQueue = outputQueue;
    }

    @Override
    public void setSize(int size) {
        outputQueue.setSize(size);
    }

    @Override
    public int getSize() {
        return outputQueue.getSize();
    }

    @Override
    public String showOutput() {
        String comment = "Below the resent " + getSize() + " log enties:\n";
        return comment + outputQueue.getAll();
    }
}


