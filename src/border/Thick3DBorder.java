/*
 * Thick3DBorder.java
 *
 * Created on February 25, 2007, 8:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.Border;

/**
 *
 * @author gcheng
 */
public class Thick3DBorder implements Border {
    public Insets getBorderInsets(Component c) {
        return new Insets(2, 2, 2, 3);
    }
    public boolean isBorderOpaque() {
        return true;
    }
    public void paintBorder(Component c, Graphics g,
            int x, int y, int width, int height) {
        int w = width;
        int h = height;
        Graphics g2d = g.create();
        g2d.translate(x, y);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(w - 3, 1, 3, h - 2);
        g2d.fillRect(1, h - 2, w - 2, 2);
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, w - 2, 2);
        g2d.fillRect(0, 0, 2, h - 1);
        g2d.dispose();
    }
}
