/*
 * VolumeMeter.java
 *
 * Created on 2 March 2007, 10:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComponent;

/**
 *
 * @author gcheng
 */
public class VolumeMeter extends JComponent {
    
    private boolean newData = false;
    private double[] currLevel = null;
    private double[] maxLevel = null;
    
    private Paint dbMeter;
    private Paint volLevel;
    
    private Timer fainter = null;
    
    public VolumeMeter() {
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Color
                        bclr0 = new Color(0, 97, 0),
                        bclr1 = new Color(132, 130, 0),
                        bclr2 = new Color(132, 0, 0),
                        fclr0 = new Color(0, 195, 0),
                        fclr1 = new Color(255, 255, 0),
                        fclr2 = new Color(255, 0, 0);
                
                int w = getWidth();
                int h = getHeight();
                int w1 = (int)(w * 0.66f);
                
                GradientPaint bg1, bg2, fg1, fg2;
                BufferedImage bi;
                Graphics2D g2d;
                
                bg1 = new GradientPaint(0, 0, bclr0, w1, 0, bclr1);
                bg2 = new GradientPaint(w1, 0, bclr1, w - 1, 0, bclr2);
                bi = new BufferedImage(w, 1, BufferedImage.TYPE_INT_RGB);
                
                g2d = (Graphics2D)bi.getGraphics();
                g2d.setPaint(bg1);
                g2d.fillRect(0, 0, w1, 1);
                g2d.setPaint(bg2);
                g2d.fillRect(w1, 0, w - w1, 1);
                g2d.dispose();
                
                dbMeter = new TexturePaint(bi, new Rectangle(0, 0, w, h));
                
                fg1 = new GradientPaint(0, 0, fclr0, w1, 0, fclr1);
                fg2 = new GradientPaint(w1, 0, fclr1, w, 0, fclr2);
                bi = new BufferedImage(w, 1, BufferedImage.TYPE_INT_RGB);

                g2d = (Graphics2D)bi.getGraphics();
                g2d.setPaint(fg1);
                g2d.fillRect(0, 0, w1, 1);
                g2d.setPaint(fg2);
                g2d.fillRect(w1, 0, w - w1, 1);
                g2d.dispose();
                
                volLevel = new TexturePaint(bi, new Rectangle(0, 0, w, h));
            }
        });
    }
    
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g.create();
        
        int w = getWidth();
        int h = getHeight();
        
        // paint background
        g2d.setPaint(dbMeter);
        g2d.fillRect(0, 0, w, h);
        
        // paint foreground
        if (currLevel != null) {
            g2d.setPaint(volLevel);
            int chs = currLevel.length;
            int hh = h / chs;
            for (int ch = 0; ch < chs; ch++) {
                double currVal = Math.abs(currLevel[ch]);
                double maxVal = Math.abs(maxLevel[ch]);
                
                if (maxVal < currVal)
                    maxLevel[ch] = currVal;
                
                //currLevel[ch] = 0;
                
                int ww = (int)Math.round((w - 1) * currVal + 1);
                int yy = ch * h / chs;
                
                // paint current
                g2d.fillRect(0, yy, ww, hh);
                
                // paint max
                ww = (int)Math.round((w - 1) * maxVal + 1);
                g2d.fillRect(ww - 2, yy, 2, hh);
            }
        }
        g2d.dispose();
    }
    
    private void checkMax() {
        boolean keepAlive = false;
        if (maxLevel.length > 0) {
            for (int ch = 0; ch < maxLevel.length; ch++) {
                if (maxLevel[ch] > 0) {
                    keepAlive = true;
                    maxLevel[ch] -= 0.01;
                }
            }
        }
        repaint();
        if (! keepAlive) {
            fainter.cancel();
            fainter = null;
        }
    }
    
    public void setLevel(double[] currLevel) {
        if ((currLevel == null) || (currLevel.length == 0))
            throw new IllegalArgumentException("invalid data for selLevel()");
        
        this.currLevel = currLevel;
        if (maxLevel == null) {
            maxLevel = new double[currLevel.length];
        }
        if (maxLevel.length < currLevel.length) {
            maxLevel = new double[currLevel.length];
        }
        repaint();
        if (fainter != null) {
            fainter.cancel();   // cancel existing task if there is any
            fainter = null;
        }
        fainter = new Timer("faint timer");
        fainter.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                checkMax();
            }
        }, 100, 50);
    }
}
