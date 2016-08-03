/*
 * DisplayObserver.java
 *
 * Created on 27 February 2007, 16:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

import java.util.EventListener;

/**
 *
 * @author gcheng
 */
public interface DisplayListener extends EventListener {
    void displayChanged(boolean updateScale);
}
