/*
 * Main.java
 *
 * Created on February 25, 2007, 8:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package application;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.UIManager;


/**
 *
 * @author gcheng
 */
public class Main {
    
    static ResourceBundle rb;
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    public static void main(final String[] args) {
        if (args.length == 0) {
            System.exit(2);
        }
        String laf = UIManager.getCrossPlatformLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {}
        
        //showSplashWindow();
        //loadSettings();
        rb = ResourceBundle.getBundle("i18n.MyResources", new Locale("", ""));
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainFrame(args[0]);
            }
        });
        //hideSplashWindow();
    }
    
}
