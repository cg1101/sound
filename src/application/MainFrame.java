/*
 * MainFrame.java
 *
 * Created on March 22, 2007, 6:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package application;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.io.File;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import javax.swing.filechooser.FileFilter;
import newsound.*;
import theme.*;

/**
 *
 * @author gcheng
 */
public class MainFrame {
    
    Action  OPEN   = createAction("open");
    Action  DETECT = createAction("detect");
    Action  EXIT   = createAction("exit");
    Action  HELP   = createAction("contents");
    Action  ABOUT  = createAction("about");
    
    SortedMap<String, MetalTheme> themes = new TreeMap<String, MetalTheme>();
    {
        themes.put("Default", new DefaultMetalTheme());
        themes.put("Ocean", new OceanTheme());
        themes.put("Aqua", new AquaTheme());
        themes.put("Charcoal", new CharcoalTheme());
        themes.put("Contrast", new ContrastTheme());
        themes.put("Emerald", new EmeraldTheme());
        themes.put("Ruby", new RubyTheme());
    }
    
    static   ImageIcon logo = createImageIcon("/images/playwav.png");
    static   ImageIcon none = createImageIcon("/images/default.png");
    
    JFrame       frame;
    JDialog      ctrlDlg;
    About        aboutDlg;
    JPanel       dsPane;
    JFileChooser fileChooser;
    
    SoundDisplay disp;
    
    /** Creates a new instance of MainFrame */
    public MainFrame(String file) {
        final java.util.ResourceBundle rb = Main.rb;
        
        frame = new JFrame(Version.name() + " - " + file);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        disp = new SoundDisplay(file);
        frame.setJMenuBar(createMenuBar());
        //frame.add(createToolBar(), BorderLayout.NORTH);
        frame.add(disp);
        frame.setSize(800, 600);
        frame.setIconImage(logo.getImage());
        frame.setVisible(true);
        
        ControlPanel ctrlPanel = new ControlPanel();
        ctrlPanel.setController(disp.controller);
        ctrlDlg = new JDialog(frame, rb.getString("Panel.title"), false);
        ctrlDlg.setContentPane(ctrlPanel);
        ctrlDlg.pack();
        Rectangle rect = frame.getBounds();
        ctrlDlg.setLocation(rect.x + rect.width, rect.y);
        ctrlDlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        aboutDlg = new About(frame);
        dsPane   = new DetectSettingsPane();
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                String ext = null;
                String s = f.getName();
                int i = s.lastIndexOf('.');
                if (i > 0 &&  i < s.length() - 1) {
                    ext = s.substring(i+1).toLowerCase();
                }
                return ext != null && ext.equals("wav") || f.isDirectory();
            }
            public String getDescription() {
                return rb.getString("wavfilter");
            }
        });
        fileChooser.setMultiSelectionEnabled(true);
        ctrlDlg.setVisible(true);
        frame.setVisible(true);
    }
    
    protected void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("about")) {
            aboutDlg.setVisible(true);
        } else if (cmd.equals("exit")) {
            System.exit(0);
        } else if (cmd.equals("open")) {
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                disp.controller.setAudioFile(file.getAbsolutePath());
            }
        } else if (cmd.equals("contents")) {
            
        } else if (cmd.equals("detect")) {
            java.util.ResourceBundle rb = Main.rb;
            if (JOptionPane.showConfirmDialog(
                    frame,
                    dsPane,
                    rb.getString("Detect.title"), 
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                (new Detector(disp.controller.getAudioManager())).start();
            }
        }
    }
    
    protected void updateMetalTheme(MetalTheme newTheme) {
        MetalLookAndFeel mlf = (MetalLookAndFeel)UIManager.getLookAndFeel();
        mlf.setCurrentTheme(newTheme);
        try {
            UIManager.setLookAndFeel(mlf);
        } catch (Exception e) {}
        SwingUtilities.updateComponentTreeUI(frame);
        SwingUtilities.updateComponentTreeUI(ctrlDlg);
        SwingUtilities.updateComponentTreeUI(aboutDlg);
        SwingUtilities.updateComponentTreeUI(dsPane);
        SwingUtilities.updateComponentTreeUI(fileChooser);
    }
    
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = Main.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            return none;
            //System.err.println("Couldn't find file: " + path);
            //return null;
        }
    }
    
    protected Action createAction(String command) {
        java.util.ResourceBundle rb = Main.rb;
        final MainFrame delegate = this;
        Action a = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                delegate.actionPerformed(e);
            }
        };
        a.putValue(Action.NAME, rb.getString(command + ".name"));
        a.putValue(Action.ACTION_COMMAND_KEY, command);
        a.putValue(Action.SMALL_ICON,
                createImageIcon("/images/" + command + ".png"));
        a.putValue(Action.MNEMONIC_KEY,
                rb.getObject(command + ".mnemonic_key"));
        a.putValue(Action.SHORT_DESCRIPTION,
                rb.getObject(command + ".short_desc"));
        a.setEnabled((Boolean)rb.getObject(command + ".enabled"));
        return a;
    }
    
    protected JMenuBar createMenuBar() {
        java.util.ResourceBundle rb = Main.rb;
        JMenu file = new JMenu(rb.getString("file.name"));
        file.setMnemonic(((Integer)rb.getObject("file.mnemonic_key")).intValue());
        file.add(new JMenuItem(OPEN));
        file.add(new JSeparator());
        file.add(new JMenuItem(DETECT));
        file.add(new JSeparator());
        file.add(new JMenuItem(EXIT));
        
        JMenu help = new JMenu(rb.getString("help.name"), true);
        help.setMnemonic(((Integer)rb.getObject("help.mnemonic_key")).intValue());
        help.add(new JMenuItem(HELP));
        help.add(new JSeparator());
        help.add(new JMenuItem(ABOUT));
        
        JMenu theme = new JMenu(rb.getString("theme.name"));
        ButtonGroup bg = new ButtonGroup();
        theme.setMnemonic(((Integer)rb.getObject("theme.mnemonic_key")).intValue());
        MetalTheme curr = MetalLookAndFeel.getCurrentTheme();
        for (Iterator<String> it = themes.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            MetalTheme aTheme = themes.get(key);
            JRadioButtonMenuItem jrmi = new JRadioButtonMenuItem(
                    new ThemeAction(key, aTheme));
            bg.add(jrmi);
            theme.add(jrmi);
            if ((aTheme.getClass()).isInstance(curr))
                jrmi.setSelected(true);
        }
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(file);
        menuBar.add(theme);
        menuBar.add(help);
        return menuBar;
    }
    
    protected JToolBar createToolBar() {
        java.util.ResourceBundle rb = Main.rb;
        JToolBar toolBar = new JToolBar(rb.getString("Toolbar"));
        toolBar.add(EXIT);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(ABOUT);
        return toolBar;
    }
    
    class ThemeAction extends AbstractAction {
        private MetalTheme mt;
        public ThemeAction(String key, MetalTheme mt) {
            this.mt = mt;
            java.util.ResourceBundle rb = Main.rb;
            String theme = rb.getString(key);
            putValue(NAME, theme);
            //putValue(SHORT_DESCRIPTION, theme);
        }
        public void actionPerformed(ActionEvent e) {
            updateMetalTheme(mt);
        }
    }
    
    class About extends JDialog {
        public About(JFrame frame) {
            super(frame, true);
            
            java.util.ResourceBundle rb = Main.rb;
            setLayout(new GridLayout(0, 1, 5, 5));
            setTitle(rb.getString("About.title"));
            
            JButton okay = new JButton(rb.getString("okay"));
            okay.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
            p.add(okay);
            
            JLabel ver = new JLabel(Version.getVersion(), logo, JLabel.CENTER);
            add(ver);
            add(p);
            setSize(200, 100);
            setLocationRelativeTo(frame);
        }
    }
    
    class DetectSettingsPane extends JPanel {
        private double interval  = 0.007;
        private double threshold = 0.04;
        private double minSigLen = 0.1;
        private double maxSigGap = 0.9;
        private JTextField
                sle_interval  = new JTextField("" + interval),
                sle_minSigLen = new JTextField("" + minSigLen),
                sle_maxSigGap = new JTextField("" + maxSigGap),
                sle_threshold = new JTextField("" + threshold);
        public DetectSettingsPane() {
            java.util.ResourceBundle rb = Main.rb;
            setLayout(new GridLayout(4, 2, 5, 5));
            add(new JLabel(rb.getString("interval")));
            add(sle_interval);
            add(new JLabel(rb.getString("min_len")));
            add(sle_minSigLen);
            add(new JLabel(rb.getString("max_gap")));
            add(sle_maxSigGap);
            add(new JLabel(rb.getString("threshold")));
            add(sle_threshold);
        }
    }
    
}
