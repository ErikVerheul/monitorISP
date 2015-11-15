/*
 * The MIT License
 *
 * Copyright (c) 2015, Verheul Consultants
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package nl.verheulconsultants.monitorisp.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.model.util.CollectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PersistModel {   
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistModel.class);

    /**
     * Saves the choices model in a file.
     * 
     * @param model of type CollectionModel
     * @param fileName
     * @return true if successful
     */
    static boolean saveChoices(CollectionModel model, String fileName) {
        ObjectOutputStream oos;
        try (FileOutputStream fout = new FileOutputStream(fileName)) {
            oos = new ObjectOutputStream(fout);
            oos.writeObject(model);
            return true;
        } catch (IOException ex) {
            LOGGER.error("The choise list can not be saved in file {}. The exception is {}", fileName, ex);
            return false;
        }
    }
    
    /**
     * Save the selected items
     * 
     * @param model of type List
     * @param fileName
     * @return  true is successful.
     */
    static boolean saveSelected(List model, String fileName) {
        ObjectOutputStream oos;
        try (FileOutputStream fout = new FileOutputStream(fileName)) {
            oos = new ObjectOutputStream(fout);
            oos.writeObject(model);
            return true;
        } catch (IOException ex) {
            LOGGER.error("The selected items can not be saved in file {}. The exception is {}", fileName, ex);
            return false;
        }
    }
    
    /**
     * Loads the model in a file.
     * 
     * @param fileName
     * @return the loaded model or a fresh default instantiation when not found.
     * @throws ClassNotFoundException 
     */
    static CollectionModel loadModel(String fileName) {
        CollectionModel model;
        try (FileInputStream fin = new FileInputStream(fileName)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            model = (CollectionModel) ois.readObject();
            return model;
        } catch (IOException ex) {
            return init();
        } catch (ClassNotFoundException ex2) {
            LOGGER.error("Unexpected internal error {}, default model loaded.", ex2);
            return init();
        }
    }
    
    /**
     * Loads the selected items.
     * 
     * @param fileName
     * @return the selected items or an empty list when not found or on error.
     */
    static List loadSelected(String fileName) {
        List model;
        try (FileInputStream fin = new FileInputStream(fileName)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            model = (List) ois.readObject();
            return model;
        } catch (IOException ex) {
            return new ArrayList();
        } catch (ClassNotFoundException ex2) {
            LOGGER.error("Unexpected internal error {}, default model loaded.", ex2);
            return new ArrayList();
        }
    }
    
    private static CollectionModel init() {
        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("0", "willfailconnection.com"));       
        hosts.add(new Host("1", "uva.nl"));
        hosts.add(new Host("2", "xs4all.nl"));
        hosts.add(new Host("3", "vu.nl"));
        CollectionModel model = new CollectionModel<>(hosts);
        return model;
    }
}
