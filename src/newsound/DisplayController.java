/*
 * DisplayController.java
 *
 * Created on February 25, 2007, 8:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;
import javax.swing.AbstractAction;
import javax.swing.event.EventListenerList;

/**
 *
 * @author gcheng
 */
public class DisplayController implements AudioDataListener {
    
    final public PlayAction
            PLAY_SEL  = new PlayAction("Play", "play_selected"),
            STOP_PLAY = new PlayAction("Stop", "stop");
    final public PanAction
            PAN_BOTH  = new PanAction("Both", "both"),
            PAN_LEFT  = new PanAction("Left only", "left"),
            PAN_RIGHT = new PanAction("Right only", "right");
    
    private int frameLength;
    private int dispIn;
    private int dispOut;
    private int selIn;
    private int selOut;
    
    private AudioManager audioManager;
    private EventListenerList listenerList;
    private ControlPanel ctrlPanel = null;
    
    private boolean ignoreChange = false;
    
    final private int PLAY_SIZE = 400;
    private boolean     playing = false;
    private int       playingAt = -1;
    private AudioPlayer player  = null;
    
    private SourceDataLine line = null;
    private FloatControl   pan  = null;
    private FloatControl   gain = null;
    private LineTracker tracker = null;
    
    /** Creates a new instance of DisplayController */
    public DisplayController() {
        this("");
    }
    
    public DisplayController(String audioFile) {
        listenerList = new EventListenerList();
        setAudioFile(audioFile);
    }
    
    public void setAudioFile(String audioFile) {
        if (audioManager != null) {
            audioManager.removeDataListener(this);
        }

        try {
            audioManager = new AudioManager(new File(audioFile));
        } catch (IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error reading file: " + audioFile,
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            System.err.println("FATAL: file read error");
            System.exit(1);
            
        } catch (UnsupportedAudioFileException e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Unsupported file format: " + audioFile,
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            System.err.println("FATAL: file format is not supported");
            System.exit(1);
        }
        
        AudioFormat af = audioManager.getAudioFormat();
        try {
            line = AudioSystem.getSourceDataLine(af);
        } catch (LineUnavailableException e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Line not available",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            System.err.println("WARNING: line not available");
        }
        
        if (line != null) {
            try {
                pan = (FloatControl)line.getControl(FloatControl.Type.PAN);
            } catch (IllegalArgumentException e) {
                // PAN control is not supported
                pan = null;
                PAN_LEFT.setEnabled(false);
                PAN_RIGHT.setEnabled(false);
            }
            try {
                gain = (FloatControl)line.getControl(
                        FloatControl.Type.MASTER_GAIN);
            } catch (IllegalArgumentException e) {
                // MASTER_GAIN control is not supported
                gain = null;
            }
            line.addLineListener(new MyLineListener());
        }
        
        frameLength = audioManager.getFrameLength();
        dispIn  = 0;
        dispOut = frameLength;
        selIn   = 0;
        selOut  = frameLength;
        
        // data loader is already running now, so we should register
        // as data listener as the final step of initialization to 
        // ensure "this" reference is available to listener callbacks
        audioManager.addAudioDataListener(this);
        fireDisplayChanged(true);
    }
    
    public AudioManager getAudioManager() {
        return audioManager;
    }
    
    public ControlPanel getControlPanel() {
        return ctrlPanel;
    }
    
    public void setControlPanel(ControlPanel ctrlPanel) {
        this.ctrlPanel = ctrlPanel;
    }
    
/////////////////////////////////
// viewport manipulation methods
/////////////////////////////////
    public int getDispIn() {
        return dispIn;
    }
    
    public int getDispOut() {
        return dispOut;
    }
    
    public int getSelIn() {
        return selIn;
    }
    
    public int getSelOut() {
        return selOut;
    }
    
    public void setViewport(int newDispIn, int newDispOut) {
        newDispIn = Math.max(0, newDispIn);
        newDispOut = Math.min(frameLength, newDispOut);
        if (newDispOut <= newDispIn) return;
        if ((newDispIn != dispIn) || (newDispOut != dispOut)) {
            dispIn = newDispIn;
            dispOut = newDispOut;
            fireDisplayChanged(true);
        }
    }
    
    public void setSelection(int newSelIn, int newSelOut) {
        newSelIn = Math.max(0, newSelIn);
        newSelOut = Math.min(frameLength, newSelOut);
        if ((newSelIn != selIn) || (newSelOut != selOut)) {
            selIn = newSelIn;
            selOut = newSelOut;
            fireDisplayChanged(false);
        }
    }
    
    public void zoomIn() {
        float delta = (dispOut - dispIn) / 8.0f;
        setViewport((int)(dispIn + delta), (int)(dispOut - delta));
    }
    
    public void zoomOut() {
        float delta = (dispOut - dispIn) / 6.0f;
        setViewport((int)(dispIn - delta), (int)(dispOut + delta));
    }
    
///////////////////////////////////////
// audio playback manipulation methods
///////////////////////////////////////
    void startAudioPlay(int playIn, int playOut, boolean repeat) {
        playing = true;
        player = new AudioPlayer(this, playIn, playOut, repeat);
        PLAY_SEL.setEnabled(false);
        STOP_PLAY.setEnabled(true);
    }
    
    void stopAudioPlay() {
        if (player != null) {
            player.stopPlaying();
        }
    }
    
    void playStopped() {
        playing = false;
        player = null;
        PLAY_SEL.setEnabled(true);
        STOP_PLAY.setEnabled(false);
        fireDisplayChanged(false);
    }
    
    void setPlayingAt(int frame) {
        playingAt = frame;
        fireDisplayChanged(false);
    }
    
    public int getPlayingAt() {
        return playingAt;
    }
    
    public boolean isPlaying() {
        return playing;
    }
    
    private void panControlChanged(int newValue) {
        // only take action when there are at least 2 channels
        if (audioManager.getChannels() > 1) {
            ignoreChange = true;
            switch (newValue) {
                case -1:
                    audioManager.setChannelOn(0, true);
                    audioManager.setChannelOn(1, false);
                    break;
                case 1:
                    audioManager.setChannelOn(0, false);
                    audioManager.setChannelOn(1, true);
                    break;
                case 0:
                    audioManager.setChannelOn(0, true);
                    audioManager.setChannelOn(1, true);
                    break;
                default:
            }
            ignoreChange = false;
            fireDisplayChanged(false);
        }
    }
    
////////////////////////////////
// line control access methods
////////////////////////////////
    public SourceDataLine getDataLine() {
        return line;
    }
    
    public FloatControl getGainControl() {
        return gain;
    }
    
    public FloatControl getPanControl() {
        return pan;
    }
    
    
/////////////////////
// listener methods
/////////////////////
    public void addDisplayListener(DisplayListener l) {
        listenerList.add(DisplayListener.class, l);
    }
    
    public void removeDisplayListener(DisplayListener l) {
        listenerList.remove(DisplayListener.class, l);
    }
    
    private void fireDisplayChanged(boolean updateScale) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == DisplayListener.class) {
                // Lazily create the event:
                //if (displayEvent == null)
                //    displayEvent = new displayEvent(this);
                ((DisplayListener)listeners[i+1]).displayChanged(updateScale);
            }
        }
    }
    
////////////////////////////////////
// AudioDataListener implementation
////////////////////////////////////
    public void audioDataChanged() {
        // notify views to repaint
        if (! ignoreChange)
            fireDisplayChanged(false);
    }
    
/////////////////////
// action classes
/////////////////////
    class PlayAction extends AbstractAction {
        public PlayAction(String name, String command) {
            super(name);
            putValue(ACTION_COMMAND_KEY, command);
            if (command.equals("stop"))
                setEnabled(false);
        }
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("play_selected")) {
                startAudioPlay(getSelIn(), getSelOut(), false);
            } else if (command.equals("stop")) {
                stopAudioPlay();
            }
        }
    }
    
    class PanAction extends AbstractAction {
        public PanAction(String name, String command) {
            super(name);
            putValue(ACTION_COMMAND_KEY, command);
        }
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            int newValue = 0;
            if (command.equals("both")) {
                newValue = 0;
            } else if (command.equals("left")) {
                newValue = -1;
            } else if (command.equals("right")) {
                newValue = 1;
            }
            FloatControl pan = getPanControl();
            if (pan != null)
                pan.setValue(newValue);
            panControlChanged(newValue);
        }
    }
    
/////////////////////
// Line classes
/////////////////////
    class MyLineListener implements LineListener {
        public void update(LineEvent e) {
            LineEvent.Type type = e.getType();
            DataLine who = (DataLine)e.getLine();
            if (type == LineEvent.Type.START) {
                // start tracking
                if (tracker != null)
                    tracker.start();
            } else if (type == LineEvent.Type.STOP) {
                // stop tracking when line stops
                if (tracker != null)
                    tracker.stopTracking();
            } else if (type == LineEvent.Type.OPEN) {
                // create tracker but don't start it
                if (tracker == null)
                    tracker = new LineTracker(who);
            } else if (type == LineEvent.Type.CLOSE) {
                // tracker should be notified to stop already
                if (tracker != null) {
                    // if it is still alive, stop it
                    if (tracker.isAlive()) {
                        tracker.stopTracking();
                        try {
                            tracker.join();
                        } catch (InterruptedException ex) {
                            // should never happen
                        }
                    }
                    tracker = null;
                }
            }
        }
    }
    
    class LineTracker extends Thread {
        final private DataLine line;
        final private AudioManager audioManager;
        final private ControlPanel ctrlPanel;
        private boolean shouldStop = false;
        public LineTracker(DataLine line) {
            super("LineTracker");
            this.line = line;
            audioManager = getAudioManager();
            ctrlPanel = getControlPanel();
        }
        public void stopTracking() {
            shouldStop = true;
        }
        public void run() {
            int selIn = getSelIn();
            while (! shouldStop) {
                int fr = line.getFramePosition() + selIn;
                setPlayingAt(fr);
                if ((ctrlPanel != null) &&
                        (fr < audioManager.getFrameLength()))
                    ctrlPanel.setLevel(audioManager.decodeFrame(fr));
                try {
                    sleep(100);
                } catch (InterruptedException e) {}
            }
        }
    }
    
/////////////////////
// AudioPlayer class
/////////////////////
    class AudioPlayer extends Thread {
        DisplayController controller;
        AudioManager      audioManager;
        ControlPanel      ctrlPanel;
        SourceDataLine    line;
        byte[]  dat;
        
        boolean stop = false;
        boolean repeat = false;
        int     frameIn;
        int     frameOut;
        int     frameSize;
        
        public AudioPlayer(DisplayController controller, int playIn,
                int playOut, boolean repeat) {
            super("AudioPlayer");
            this.controller = controller;
            audioManager = controller.getAudioManager();
            ctrlPanel = controller.getControlPanel();
            dat = audioManager.dat;
            line = getDataLine();
            
            int frameLength = audioManager.getFrameLength();
            frameIn = Math.max(playIn, 0);
            frameOut = Math.min(playOut, frameLength);
            frameSize = audioManager.getFrameSize();
            
            this.repeat = repeat;
            start();
        }
        public void stopPlaying() {
            stop = true;
        }
        public void run() {
            int remaining = frameSize * (frameOut - frameIn);
            int size = frameSize * PLAY_SIZE;
            try {
                line.open(); // might throw LineUnavailableException
                line.start();
                exit_loop:
                    do {
                        // calculate start offset
                        int offset = frameSize * frameIn;
                        int written = 0;
                        while (remaining > 0) {
                            size = Math.min(size, remaining);
                            written = line.write(dat, offset, size);
                            int playing = offset / frameSize;
                            remaining -= size;
                            // increase offset
                            offset += size;
                            if (stop) {
                                break exit_loop;
                            }
                        }
                    } while (repeat);
                    if (! stop) {
                        // wait until buffer is empty
                        line.drain();
                    }
                    line.stop();
                    line.close();
            } catch (LineUnavailableException e) {
                // prompt user
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Line not available",
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                System.err.println("line not available");
            }
            playStopped();
        }
    }
    
}
