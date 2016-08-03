/*
 * AudioView.java
 *
 * Created on February 25, 2007, 8:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author gcheng
 */
public class AudioView extends JPanel implements DisplayListener {
    
    DisplayController controller;
    final boolean fullMode;
    
    final Color BORDER_COLOR = Color.GRAY;
    final Color GRID_COLOR = Color.GRAY;
    final Color SELECTED_COLOR = new Color(0, 0, 128);
    final Color SELECTED_FOREGROUND = Color.GREEN;
    final Color UNSELECTED_FOREGROUND = new Color(0, 128, 0);
    final Color UNSELECTED_COLOR = Color.BLACK;
    final Color RULER_COLOR = new Color(212, 208, 200);
    final Color MARKER_COLOR = Color.CYAN;
    final Color TICK_COLOR = Color.LIGHT_GRAY;
    final Color PANNING_COLOR = Color.YELLOW;
    
    final Font LARGE_RULER_FONT = new Font("Monospaced", Font.PLAIN, 10);
    final Font SMALL_RULER_FONT = new Font("Monospaced", Font.PLAIN, 9);
    final Font VOLUME_GRID_FONT = new Font("Monospaced", Font.PLAIN, 12);
    
    final static float dash1[] = {3.0f};
    final static BasicStroke dashed = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            3.0f, dash1, 0.0f);
    
    private Point leftOrigin = null;
    private Point rightOrigin = null;
    private Point endPoint = null;
    
    private Scale timeScale = null;
    private Scale volumeScale = null;
    
    /** Creates a new instance of AudioView */
    public AudioView(DisplayController controller, final boolean fullMode) {
        this.controller = controller;
        this.fullMode = fullMode;
        
        setOpaque(true);
        setBackground(Color.WHITE);
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill   = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridwidth = 0;
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.insets = new Insets(0, 0, 0, 0);
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        JComponent widget;
        MyMouseListener myL = new MyMouseListener();
        if (fullMode) {
            c.weighty = 0.0;
            widget = new SoundGraph(this);
            widget.addMouseListener(myL);
            widget.addMouseMotionListener(myL);
            widget.setBackground(Color.BLUE);
            widget.setMinimumSize(new Dimension(68, 30));
            widget.setPreferredSize(new Dimension(68, 30));
            add(widget, c);
            
            c.insets = new Insets(1, 0, 0, 0);
            widget = new TimeLine(this);
            widget.setBackground(RULER_COLOR);
            widget.setMinimumSize(new Dimension(68, 13));
            widget.setPreferredSize(new Dimension(68, 13));
            add(widget, c);
        } else {
            widget = new SoundGraph(this);
            widget.addMouseListener(myL);
            widget.addMouseMotionListener(myL);
            widget.setBackground(Color.BLUE);
            widget.setMinimumSize(new Dimension(68, 23));
            widget.setPreferredSize(new Dimension(68, 23));
            add(widget, c);
            
            c.insets = new Insets(1, 0, 0, 0);
            c.weighty = 0.0;
            widget = new CueSlot(this);
            widget.setBackground(Color.BLACK);
            widget.setMinimumSize(new Dimension(68, 9));
            widget.setPreferredSize(new Dimension(68, 9));
            add(widget, c);
            
            widget = new TimeLine(this);
            widget.setBackground(RULER_COLOR);
            widget.setMinimumSize(new Dimension(68, 15));
            widget.setPreferredSize(new Dimension(68, 15));
            add(widget, c);
        }
        
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (! fullMode)
                    volumeScale = calculateVolumeScale(getChannelHeight());
                timeScale = calculateTimeScale();
            }
        });
        
        controller.addDisplayListener(this);
        
    }
    
    public void displayChanged(boolean updateScale) {
        if (updateScale)
            timeScale = calculateTimeScale();
        repaint();
    }
    
    private double getChannelStride() {
        return fullMode ?
            (getHeight() - 14.0) / controller.getAudioManager().getChannels() :
            (getHeight() - 24.0) / controller.getAudioManager().getChannels();
    }
    
    private int getChannelHeight() {
        return fullMode ?
            (getHeight() - 14) / controller.getAudioManager().getChannels() :
            (getHeight() - 24) / controller.getAudioManager().getChannels() - 2;
    }
    
    private Scale calculateVolumeScale(int height) {
        if (height >= 400)
            return new Scale(10, 5, 0.01);
        else if (height >= 200)
            return new Scale(10, 5, 0.02);
        else if (height >= 80)
            return new Scale(5, 5, 0.1);
        else if (height >= 40)
            return new Scale(10, 5, 0.1);
        else
            return new Scale(5, 5, 0.2);
    }
    
    private Scale calculateTimeScale() {
        double scale[][] = {
            {0.000005, 0.000005, 0.000001},
            {0.00002, 0.00001, 0.000002},
            {0.00005, 0.00005, 0.00001},
            {0.0001, 0.00005, 0.00001},
            {0.0002, 0.0001, 0.00002},
            {0.0005, 0.0005, 0.0001},
            {0.001, 0.0005, 0.0001},
            {0.002, 0.001, 0.0002},
            {0.005, 0.005, 0.001},
            {0.01, 0.005, 0.001},
            {0.02, 0.01, 0.002},
            {0.05, 0.05, 0.01},
            {0.1, 0.05, 0.01},
            {0.2, 0.1, 0.02},
            {0.5, 0.5, 0.1},
            {1, 0.5, 0.1},
            {2, 1, 0.2},
            {5, 5, 1},
            {10, 5, 1},
            {20, 10, 2},
            {30, 30, 10},
            {60, 30, 6},
            {120, 60, 12},
            {300, 300, 60},
            {600, 300, 60},
            {1200, 600, 120},
            {1800, 1800, 600},
            {3600, 1800, 600},
            {7200, 3600, 600},
            {7200, 7200, 1800},
            {14400, 7200, 1800},
            {28800, 14400, 3600},
            {43200, 21600, 3600},
        };
        
        FontMetrics fm = fullMode ? getFontMetrics(SMALL_RULER_FONT) :
            getFontMetrics(LARGE_RULER_FONT);
        float frameRate = controller.getAudioManager().getFrameRate();
        double length = (getDrawOut() - getDrawIn()) / frameRate;
        double audioLength = controller.getAudioManager().getFrameLength() / frameRate;
        
        double pixelsPerSec = getPixelsPerSec();
        double pixelsPerGrid;
        double pixelsPerLabel;
        String maxLabel;
        boolean found = false;
        int major = 0, minor = 0;
        double tick = 0;
        for (int i = 0; i < scale.length; i++) {
            pixelsPerGrid = pixelsPerSec * scale[i][1];
            pixelsPerLabel = pixelsPerSec * scale[i][0];
            tick = scale[i][2];
            maxLabel = formatLabel(audioLength, tick);
            int labelMaxLength = fm.stringWidth(maxLabel) + 10;
            if ((pixelsPerGrid >= 25) &&
                    (labelMaxLength <= pixelsPerLabel)){
                found = true;
                minor = (int)(scale[i][1]/scale[i][2]);
                major = (int)(scale[i][0]/scale[i][2]);
                break;
            }
        }
        while (! found) {
            tick *= 2;
            pixelsPerGrid = pixelsPerSec * tick * 6;
            pixelsPerLabel = pixelsPerSec * 12;
            maxLabel = formatLabel(audioLength, tick);
            int labelMaxLength = fm.stringWidth(maxLabel);
            if ((pixelsPerGrid >= 25) &&
                    (labelMaxLength <= pixelsPerLabel)){
                found = true;
                minor = 6;
                major = 12;
                break;
            }
        }
        return new Scale(major, minor, tick);
    }
    
    private int getDrawIn() {
        return fullMode ? 0 : controller.getDispIn();
    }
    
    private int getDrawOut() {
        return fullMode ? controller.getAudioManager().getFrameLength() : controller.getDispOut();
    }
    
    private double getFramePixelRatio() {
        int w = getWidth();
        int frameDrawIn  = getDrawIn();
        int frameDrawOut = getDrawOut();
        
        return (double)(frameDrawOut - frameDrawIn) / (w - 1);
    }
    
    private double getPixelsPerSec() {
        float frameRate = controller.getAudioManager().getFrameRate();
        int widthInUse = getWidth() - 1;
        return frameRate * widthInUse / (getDrawOut() - getDrawIn());
    }
    
    private int convertFrameToX(int frame) {
        int frameDrawIn  = getDrawIn();
        return (int)Math.floor((frame - frameDrawIn) / getFramePixelRatio());
    }
    
    private int convertXToFrame(int x) {
        long frame = Math.round(x * getFramePixelRatio()) + getDrawIn();
        return (int)Math.min(frame, controller.getAudioManager().getFrameLength());
    }
    
    private int convertPosToFrame(double pos) {
        return (int)Math.round(pos * controller.getAudioManager().getFrameRate());
    }
    
    /**
     * Format input value to "hh:mm:ss.sss" format. The precision
     * depends on the value of tick.
     *
     * @param value    The value to be formatted.
     * @param tick     Display resolution in seconds.
     */
    private String formatLabel(double value, double tick) {
        StringBuffer fmt = new StringBuffer("%02d:%02d:");
        if (tick < 0.00001)
            fmt.append("%08.6f");
        else if (tick < 0.0001)
            fmt.append("%07.5f");
        else if (tick < 0.001)
            fmt.append("%06.4f");
        else if (tick < 0.01)
            fmt.append("%05.3f");
        else if (tick < 0.1)
            fmt.append("%04.2f");
        else if (tick < 1)
            fmt.append("%03.1f");
        else
            fmt.append("%02.0f");
        int hour = ((int)value) / 3600;
        int min  = ((int)value - hour * 3600) / 60;
        double sec  = value - hour * 3600 - min * 60;
        return String.format(fmt.toString(), hour, min, sec);
    }
    
    // must use time/pixel ratio to calculate x coordinate
    // to make time grid lines distributed evenly
    void paintTimeLine(Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.translate(x, y);
        
        int w = width;
        int h = height;
        Scale scale = timeScale;
        
        double frameRate  = controller.getAudioManager().getFrameRate();
        double drawInPos  = getDrawIn() / frameRate;
        double drawOutPos = getDrawOut() / frameRate;
        double pixelsPerSec = getPixelsPerSec();
        
        double startPos = fullMode ? 0 : Math.floor(
                drawInPos / (scale.tick * scale.major)) *
                scale.tick * scale.major;
        
        g2d.setColor(RULER_COLOR);
        g2d.fillRect(0, 0, w, h);
        g2d.setFont(fullMode ? SMALL_RULER_FONT : LARGE_RULER_FONT);
        
        g2d.setColor(Color.BLACK);
        int counter = 0;
        double currPos = startPos;
        while (currPos <= drawOutPos) {
            int xx = (int)Math.round((currPos - drawInPos) * pixelsPerSec);
            if (counter % scale.major == 0) {
                g2d.drawLine(xx, 0, xx, 5);
                g2d.drawString(formatLabel(currPos, scale.tick),
                        xx + 2, h - 1);
            } else if (counter % scale.minor == 0) {
                g2d.drawLine(xx, 0, xx, 3);
            } else {
                g2d.drawLine(xx, 0, xx, 1);
            }
            currPos = startPos + scale.tick * (++counter);
        }
        
        g2d.dispose();
    }
    
    void paintCueSlot(Graphics g, int x, int y, int width, int height) {
        if (fullMode) return;
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.translate(x, y);
        
        int w = width;
        int h = height;
        
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, w, h);
        paintTimeGrid(g2d, 0, 0, w, h);
        g2d.setColor(GRID_COLOR);
        
        g2d.drawLine(0, 0, w, 0);
        g2d.dispose();
    }
    
    void paintSoundGraph(Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.translate(x, y);
        
        int w = width;
        int h = height;
        
        int drawIn  = getDrawIn();
        int drawOut = getDrawOut();
        
        int selIn   = controller.getSelIn();
        int selOut  = controller.getSelOut();
        int selX0   = Math.max(-5, convertFrameToX(selIn));
        int selX1   = Math.min(w + 4, convertFrameToX(selOut));
        
        // clear background
        g2d.setColor(UNSELECTED_COLOR);
        g2d.fillRect(0, 0, w, h);
        
        int channels = controller.getAudioManager().getChannels();
        int channelHeight = getChannelHeight();
        double channelStride = getChannelStride();
        
        Color dark0 = new Color(0, 128, 0);
        Color dark1 = new Color(128, 0, 0);
        Color bright0 = new Color(0, 255, 0);
        Color bright1 = new Color(255, 0, 0);
        
        int totalFrame = drawOut - drawIn;
        boolean solid = totalFrame >= 2 * (w - 1);
        boolean bar   = totalFrame < w - 1;
        
        double data[][][] = null;
        double data2[][] = null;
        if (solid)
            data = controller.getAudioManager().getDisplayData(drawIn, drawOut, w);
        else
            data2 = controller.getAudioManager().getFrameData(drawIn, drawOut);
        
        // draw each channel
        for (int i = 0; i < channels; i++) {
            // calculate offset
            int yy = (int)Math.round(i * channelStride);
            
            boolean selected = controller.getAudioManager().isChannelOn(i);
            if (selected) {
                g2d.setColor(SELECTED_COLOR);
                g2d.fillRect(selX0, yy, selX1 - selX0, yy + channelHeight);
            }
            
            // draw vertical grid lines
            paintTimeGrid(g2d, 0, yy, w, channelHeight);
            // draw horizontal grid lines
            paintVolumeGrid(g2d, 0, yy, w, channelHeight);
            
            // draw sound data
            if (solid) {
                for (int j = 0; j < w; j++) {
                    int y0 = yy + (int)Math.round((1.0 - data[i][j][0]) *
                            (channelHeight - 1) / 2);
                    int y1 = yy + (int)Math.round((1.0 - data[i][j][1]) *
                            (channelHeight - 1) / 2);
                    if ((j < selX0) || (j > selX1) || ! selected) {
                        g2d.setColor((i % 2 == 0) ? dark0 : dark1);
                    } else {
                        g2d.setColor((i % 2 == 0) ? bright0 : bright1);
                    }
                    g2d.drawLine(j, y0, j, y1);
                }
            } else {
                Point lastPoint = null;
                for (int fr = 0; fr < data2[i].length; fr++) {
                    int absFrame = fr + drawIn;
                    if ((absFrame < selIn) || (absFrame >= selOut) || ! selected) {
                        g2d.setColor((i % 2 == 0) ? dark0 : dark1);
                    } else {
                        g2d.setColor((i % 2 == 0) ? bright0 : bright1);
                    }
                    int dy = yy + (int)Math.round((1.0 - data2[i][fr]) * (channelHeight - 1) / 2);
                    int dx = convertFrameToX(absFrame);
                    if (lastPoint != null)
                        g2d.drawLine(lastPoint.x, lastPoint.y, dx, dy);
                    lastPoint = new Point(dx, dy);
                    if (bar) {
                        dx = convertFrameToX(absFrame + 1) - 1;
                        g2d.drawLine(lastPoint.x, lastPoint.y, dx, dy);
                        lastPoint = new Point(dx, dy);
                    }
                }
            }
            // draw ticks
            paintVolumeTick(g2d, 0, yy, w, channelHeight);
            
            // draw center line
            if (! fullMode) {
                Stroke stroke = g2d.getStroke();
                g2d.setStroke(dashed);
                g2d.drawLine(0, yy + channelHeight / 2, w, yy + channelHeight / 2);
                g2d.setStroke(stroke);
            }
            
            // draw channel seperator
            paintChannelBorder(g2d, 0, yy, w, channelHeight, channelStride);
        }
        
        // display box
        paintDisplayBox(g2d, 0, 0, w, h);
        
        // hilight selected portion
        g2d.setColor(MARKER_COLOR);
        g2d.drawLine(selX0, 0, selX0, h);
        g2d.drawLine(selX1, 0, selX1, h);
        
        // draw dragging  box
        paintDraggingBox(g2d, 0, 0, w, h);
        
        if (controller.isPlaying()) {
            int dx = convertFrameToX(controller.getPlayingAt());
            g2d.setColor(Color.WHITE);
            g2d.drawLine(dx, 0, dx, h);
            g2d.setColor(Color.BLACK);
            g2d.drawLine(dx - 1, 0, dx - 1, h);
        }
        
        paintRegion(g2d, 0, 0, w, h);
        
        // release copy's resource
        g2d.dispose();
    }
    
    private void paintRegion(Graphics2D g2d, int x, int y,
            int width, int height) {
        
        if (fullMode) return;
        
        Detector.Region[][] regions = controller.getAudioManager().getRegions();
        if (regions == null)
            return;
        
        int w = width;
        int h = height;
        
        int drawIn  = getDrawIn();
        int drawOut = getDrawOut();
        
        int channels = controller.getAudioManager().getChannels();
        int channelHeight = getChannelHeight();
        double channelStride = getChannelStride();
        
        g2d.setColor(Color.MAGENTA);
        for (int ch = 0; ch < channels; ch++) {
            // calculate offset
            int yy = (int)Math.round(ch * channelStride) + channelHeight / 4;
            for (int i = 0; i < regions[ch].length; i++) {
                Detector.Region r = regions[ch][i];
                int start = r.getStart();
                int end = r.getStart() + r.getFrameLength();
                if (((start >= drawIn) && (start < drawOut)) ||
                        ((end >= drawIn) && (end < drawOut)) ||
                        ((start < drawIn) && (end >drawOut))) {
                    int x0 = Math.max(-5, convertFrameToX(start));
                    int x1 = Math.min(w + 5, convertFrameToX(end));
                    g2d.drawRect(x0, yy, x1 - x0 + 1, channelHeight / 2 + 1);
                }
            }
        }
    }
    
    private void paintTimeGrid(Graphics2D g2d, int x, int y,
            int width, int height) {
        if (fullMode) return;
        g2d.translate(x, y);
        
        int w = width;
        int h = height;
        Scale scale = timeScale;
        
        double frameRate  = controller.getAudioManager().getFrameRate();
        double drawInPos  = getDrawIn() / frameRate;
        double drawOutPos = getDrawOut() / frameRate;
        double pixelsPerSec = getPixelsPerSec();
        
        double startPos = Math.floor(drawInPos / (scale.tick * scale.minor)) *
                scale.tick * scale.minor;
        
        g2d.setColor(GRID_COLOR);
        int counter = 0;
        double currPos = startPos;
        while (currPos <= drawOutPos) {
            int xx = (int)Math.round((currPos - drawInPos) * pixelsPerSec);
            g2d.drawLine(xx, 0, xx, h);
            currPos = startPos + scale.tick * scale.minor * (++counter);
        }
        
        g2d.translate(-x, -y);
    }
    
    private void paintChannelBorder(Graphics2D g2d, int x, int y,
            int width, int height, double stride) {
        if (fullMode) return;
        
        // translate coordinates
        g2d.translate(x, y);
        g2d.setColor(GRID_COLOR);
        g2d.fillRect(0, height, width,
                (int)Math.round(stride - height));
        g2d.translate(-x, -y);
    }
    
    private void paintVolumeGrid(Graphics2D g2d, int x, int y,
            int width, int height) {
        if (fullMode) return;
        
        // translate coordinates
        g2d.translate(x, y);
        g2d.setColor(GRID_COLOR);
        
        Scale scale = volumeScale;
        double stride = scale.tick * scale.minor;
        double currPos = 1.0;
        int counter = 0;
        while (currPos >= -1.0) {
            int yy = (int)Math.round((1.0 - currPos) *
                    (height - 1) / 2);
            g2d.drawLine(0, yy, width, yy);
            counter++;
            currPos = 1.0 - counter * stride;
        }
        
        // translate back
        g2d.translate(-x, -y);
    }
    
    private void paintVolumeTick(Graphics2D g2d, int x, int y,
            int width, int height) {
        if (fullMode) return;
        
        // translate coordinates
        g2d.translate(x, y);
        g2d.setColor(TICK_COLOR);
        
        Scale scale = volumeScale;
        double stride = scale.tick * scale.minor;
        double currPos = 1.0;
        int counter = 0;
        while (currPos >= -1.0) {
            int yy = (int)Math.round((1.0 - currPos) * (height - 1) / 2);
            if (counter % scale.major == 0) {
                g2d.drawLine(0, yy, 5, yy);
                if (((scale.tick == 0.2) && (currPos == 0)) ||
                        ((scale.tick < 0.2) && (currPos > -1)))
                    g2d.drawString(String.format("%2.1f", currPos), 7, yy + 12);
            } else if (counter % scale.minor == 0) {
                g2d.drawLine(0, yy, 3, yy);
            } else {
                g2d.drawLine(0, yy, 1, yy);
            }
            counter++;
            currPos = 1.0 - counter * scale.tick;
        }
        g2d.translate(-x, -y);
    }
    
    private void paintDraggingBox(Graphics2D g2d, int x, int y,
            int width, int height) {
        g2d.translate(x, y);
        Point startPoint;
        if (endPoint != null) {
            if (leftOrigin != null) {
                g2d.setColor(MARKER_COLOR);
                startPoint = leftOrigin;
            } else {
                g2d.setColor(PANNING_COLOR);
                startPoint = rightOrigin;
            }
            int h = height;
            int w = endPoint.x - startPoint.x;
            g2d.drawRect(startPoint.x - 2, 0, w + 3, h - 1);
            g2d.drawRect(startPoint.x - 1, 1, w + 1, h - 3);
        }
        g2d.translate(-x, -y);
    }
    
    private void paintDisplayBox(Graphics2D g2d, int x, int y,
            int width, int height) {
        if (! fullMode) return;
        g2d.translate(x, y);
        
        int frameDispIn  = controller.getDispIn();
        int frameDispOut = controller.getDispOut();
        
        int dispX0 = convertFrameToX(frameDispIn);
        int dispX1 = convertFrameToX(frameDispOut);
        
        // minimal width is 9 pixels
        // if smaller than that, use middle point to align
        if (dispX1 - dispX0 < 8) {
            // use middle to align
            int middle = (frameDispIn + frameDispOut) / 2;
            int anchor = convertFrameToX(middle);
            anchor = Math.max(anchor, 5);
            anchor = Math.min(anchor, getWidth() - 5);
            dispX0 = anchor - 4;
            dispX1 = anchor + 4;
        } else {
            // make sure it is inside
            dispX0 = Math.max(dispX0, 0);
            dispX0 = Math.min(dispX0, getWidth() - 9);
            dispX1 = Math.max(dispX1, 8);
            dispX1 = Math.min(dispX1, getWidth() - 1);
        }
        
        int h = height;
        g2d.setColor(RULER_COLOR);
        g2d.drawRect(dispX0, 0, dispX1 - dispX0, h - 1);
        g2d.drawRect(dispX0 + 1, 1, dispX1 - dispX0 - 2, h - 3);
        g2d.setColor(Color.WHITE);
        g2d.drawLine(dispX0, 0, dispX0, h - 2);
        g2d.drawLine(dispX0, 0, dispX1 - 1, 0);
        g2d.drawLine(dispX0 + 3, h - 2, dispX1 - 2, h - 2);
        g2d.drawLine(dispX1 - 2, h - 2, dispX1 - 2, 2);
        g2d.setColor(GRID_COLOR);
        g2d.drawLine(dispX0 + 2, 1, dispX0 + 2, h - 3);
        g2d.drawLine(dispX0 + 2, 1, dispX1 - 3, 1);
        g2d.drawLine(dispX0 + 1, h - 1, dispX1, h - 1);
        g2d.drawLine(dispX1, h - 1, dispX1, 1);
        
        g2d.translate(-x, -y);
    }
    
    private class MyMouseListener extends MouseInputAdapter {
        public void mousePressed(MouseEvent e) {
            if (leftOrigin != null) {
                // other button pressed in selecting mode
                // quit selecting mode immediately
                if (e.getButton() != MouseEvent.BUTTON1) {
                    leftOrigin = null;
                    rightOrigin = null;
                }
            } else if (rightOrigin != null) {
                // left button pressed when panning
                // do nothing
                ;
            } else {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // entering selecting mode
                    leftOrigin = e.getPoint();
                    rightOrigin = null;
                    endPoint = null;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // entering panning mode
                    leftOrigin = null;
                    rightOrigin = e.getPoint();
                    endPoint = null;
                }
            }
            repaint();
        }
        public void mouseDragged(MouseEvent e) {
            if ((leftOrigin == null) && (rightOrigin == null)) return;
            endPoint = e.getPoint();
            if (endPoint.x < 0)
                endPoint.x = 0;
            if ((leftOrigin != null) && (endPoint.x < leftOrigin.x))
                leftOrigin = endPoint;
            else if ((rightOrigin != null) && (endPoint.x < rightOrigin.x))
                rightOrigin = endPoint;
            repaint();
        }
        public void mouseReleased(MouseEvent e) {
            int newIn, newOut;
            if (leftOrigin != null) {
                if (endPoint != null) {
                    newIn = convertXToFrame(leftOrigin.x);
                    newOut = convertXToFrame(endPoint.x);
                    controller.setSelection(newIn, newOut);
                } else {
                    int newPos = convertXToFrame(leftOrigin.x);
                    int selOut = controller.getSelOut();
                    if (newPos < selOut)
                        controller.setSelection(newPos, selOut);
                    else
                        controller.setSelection(controller.getSelIn(), newPos);
                }
            } else if (rightOrigin != null) {
                if (endPoint != null) {
                    // pop up small menu
                    newIn = convertXToFrame(rightOrigin.x);
                    newOut = convertXToFrame(endPoint.x);
                    controller.setViewport(newIn, newOut);
                } else {
                    // pop up big menu
                }
            }
            leftOrigin = null;
            rightOrigin = null;
            endPoint = null;
            repaint();
        }
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            int fr = convertXToFrame(p.x);
            if (fr >= controller.getAudioManager().getFrameLength()) return;
            ControlPanel c = controller.getControlPanel();
            // do not interfere with play back display
            if (! controller.isPlaying() && c != null)
                c.setLevel(controller.getAudioManager().decodeFrame(fr));
        }
    }
    
    private class Scale {
        double tick = 0;
        int minor = 0;
        int major = 0;
        
        /** Creates a new instance of Scale */
        public Scale(int major, int minor, double tick) {
            this.tick = tick;
            this.minor = minor;
            this.major = major;
        }
        
        public String toString() {
            return "major=" + major +
                    ",minor=" + minor +
                    ",tick=" + tick;
        }
    }
    
}
