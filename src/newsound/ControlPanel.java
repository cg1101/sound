/*
 * ControlPanel.java
 *
 * Created on 28 February 2007, 13:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

import java.awt.GridLayout;
import javax.sound.sampled.FloatControl;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author gcheng
 */
public class ControlPanel extends JPanel {
    private DisplayController controller = null;
    
    private JButton      btnPlay = new JButton("Play");
    private JButton      btnStop = new JButton("Stop");
    private JRadioButton both    = new JRadioButton("Both");
    private JRadioButton left    = new JRadioButton("Left only");
    private JRadioButton right   = new JRadioButton("Right only");
    private JSlider      volBar  = new JSlider();
    private VolumeMeter  volm    = new VolumeMeter();
    
    private GainListener volL    = null;
    
    /** Creates a new instance of ControlPanel */
    public ControlPanel() {
        setOpaque(true);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new GridLayout(0, 1, 5, 5));
        
        add(btnPlay);
        add(btnStop);
        add(volm);
        
        add(new JLabel("Channels:"));
        add(both);
        add(left);
        add(right);
        ButtonGroup bg = new ButtonGroup();
        bg.add(both);
        bg.add(left);
        bg.add(right);
        
        add(new JLabel("Volume:"));
        add(volBar);
        
        // initialize widget states
        both.setSelected(true);
        
        btnPlay.setEnabled(false);
        btnStop.setEnabled(false);
        both.setEnabled(false);
        left.setEnabled(false);
        right.setEnabled(false);
        volBar.setEnabled(false);
    }
    
    public void setController(DisplayController controller) {
        if (this.controller == controller) return;
        // notify current occuping controller
        if (this.controller != null)
            this.controller.setControlPanel(null);
        
        btnPlay.setAction(controller.PLAY_SEL);
        btnStop.setAction(controller.STOP_PLAY);
        both.setAction(controller.PAN_BOTH);
        left.setAction(controller.PAN_LEFT);
        right.setAction(controller.PAN_RIGHT);
        
        // remove volBar change listener if there is one
        if (volL != null) {
            volBar.removeChangeListener(volL);
            volL = null;
        }
        
        FloatControl gain  = null;
        gain = controller.getGainControl();
        if (gain != null) {
            
            float maxVDb = gain.getMaximum();
            float minVDb = gain.getMinimum();
            double minVA = Math.pow(10, minVDb / 20.0);
            double maxVA = Math.pow(10, maxVDb / 20.0);
            int value = (int)Math.floor(1 / minVA);
            int maxI  = (int)Math.floor(maxVA / minVA);
            volBar.setMinimum(1);
            volBar.setMaximum(maxI);
            volBar.setValue(value);
            volBar.setMajorTickSpacing(value);
            volBar.setMinorTickSpacing(value / 5);
            volBar.setPaintTicks(true);
            
            volL = new GainListener(gain, value);
            volBar.addChangeListener(volL);
            volBar.setEnabled(true);
        } else {
            volBar.setEnabled(false);
        }
        
        // notifying requesting controller
        this.controller = controller;
        controller.setControlPanel(this);
    }
    
    public void setLevel(double[] currLevel) {
        volm.setLevel(currLevel);
    }
    
    class GainListener implements ChangeListener {
        private FloatControl gain;
        private int base;
        public GainListener(FloatControl gain, int base) {
            this.gain = gain;
            this.base = base;
        }
        public void stateChanged(ChangeEvent e) {
            JSlider js = (JSlider)e.getSource();
            double value = js.getValue();
            float db = (float)(Math.log10(value / base)) * 20;
            gain.setValue(db);
        }
    }
    
}
