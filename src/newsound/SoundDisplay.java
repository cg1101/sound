/*
 * SoundDisplay.java
 *
 * Created on February 25, 2007, 8:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;

import border.Thick3DBorder;

/**
 *
 * @author gcheng
 */
public class SoundDisplay extends JPanel {
    
    public DisplayController controller;
    
    /** Creates a new instance of SoundDisplay */
    public SoundDisplay(String audioFile) {
        controller = new DisplayController(audioFile);
        
        AudioView zoomView = new AudioView(controller, false);
        AudioView fullView = new AudioView(controller, true);
        NavigateBar navBar = new NavigateBar(controller);
        
        setOpaque(true);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill   = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridwidth  = GridBagConstraints.REMAINDER;
        c.gridx  = 0;
        c.gridy  = GridBagConstraints.RELATIVE;
        c.insets = new Insets(0, 14, 0, 17);
        c.ipadx  = 0;
        c.ipady  = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new Thick3DBorder());
        p.add(zoomView);
        add(p, c);
        
        p = new JPanel(new BorderLayout());
        p.setBorder(new Thick3DBorder());
        p.add(fullView);
        c.insets = new Insets(2, 14, 0, 17);
        c.weighty = 0.0;
        add(p, c);
        
        c.insets = new Insets(1, 0, 0, 0);
        add(navBar, c);
        
        addMouseWheelListener(new MyWheelListener());
    }
    
    class MyWheelListener implements MouseWheelListener {
        public void mouseWheelMoved(MouseWheelEvent e) {
            int notches = e.getWheelRotation();
            if (notches < 0) {
                for (int i = 0; i > notches; i--)
                    controller.zoomIn();
            } else {
                for (int i = 0; i < notches; i++)
                    controller.zoomOut();
            }
        }
    }
    
}
