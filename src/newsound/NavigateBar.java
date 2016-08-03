/*
 * NavigateBar.java
 *
 * Created on 27 February 2007, 16:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JScrollBar;

/**
 *
 * @author gcheng
 */
public class NavigateBar extends JScrollBar 
        implements DisplayListener, AdjustmentListener {
    
    DisplayController controller;
    private boolean passive;
    
    /** Creates a new instance of NavigateBar */
    public NavigateBar(DisplayController controller) {
        super(JScrollBar.HORIZONTAL);
        this.controller = controller;
        int frameLength = controller.getAudioManager().getFrameLength();
        
        setValues(0, frameLength, 0, frameLength);
        setBlockIncrement(frameLength);
        setUnitIncrement(frameLength / 6);
        setVisible(false);
        controller.addDisplayListener(this);
        addAdjustmentListener(this);
    }
    
    public void displayChanged(boolean updateScale) {
        int dispIn = controller.getDispIn();
        int dispOut = controller.getDispOut();
        int extent = dispOut - dispIn;
        int frameLength = controller.getAudioManager().getFrameLength();
        passive = true;
        setValues(dispIn, extent, 0, frameLength);
        setBlockIncrement(extent);
        setUnitIncrement(extent / 6);
        setVisible(extent != frameLength);
        passive = false;
    }
    
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getValueIsAdjusting()) return;
        if (passive) {
            // if value is changed on notification 
            // from controller, then return.
            return;
        }
        int newDispIn = getValue();
        int newDispOut = newDispIn + getVisibleAmount();
        controller.setViewport(newDispIn, newDispOut);
    }
}
