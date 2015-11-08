/*
 * FifoMBean.java
 *
 * Created on 13 december 2007, 21:14
 */
package nl.verheulconsultants.monitorisp.service;

/**
 * Interface FifoMBean
 *
 * @author erik
 */
public interface FifoMBean {

    public void setSize(int size);

    public int getSize();

    public String showOutput();
}


