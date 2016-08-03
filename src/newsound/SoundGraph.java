/*
 * SoundGraph.java
 *
 * Created on February 25, 2007, 9:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

import java.awt.Graphics;
import javax.swing.JComponent;

/**
 *
 * @author gcheng
 */
public class SoundGraph extends JComponent {
  
    AudioView view;
    
    /** Creates a new instance of SoundGraph */
    public SoundGraph(AudioView view) {
        this.view = view;
    }
    
    protected void paintComponent(Graphics g) {
        view.paintSoundGraph(g, 0, 0, getWidth(), getHeight());
    }
    
}
