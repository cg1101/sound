/*
 * AudioDataListener.java
 *
 * Created on 1 March 2007, 10:40
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
public interface AudioDataListener extends EventListener {
    void audioDataChanged();
}
