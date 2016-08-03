/*
 * MyResources.java
 *
 * Created on 23 March 2007, 13:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package i18n;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.ListResourceBundle;

/**
 *
 * @author gcheng
 */
public class MyResources extends ListResourceBundle {
    
    static final Object[][] contents = {
        { "open.name", "Open ..." },
        { "open.mnemonic_key",  KeyEvent.VK_O },
        { "open.short_desc", "Open wav file" },
        { "open.enabled", true },
        
        { "detect.name", "Auto Detect ..." },
        { "detect.mnemonic_key", KeyEvent.VK_A },
        { "detect.short_desc", "Auto detect" },
        { "detect.enabled", true },
        
        { "exit.name", "Exit" },
        { "exit.mnemonic_key", KeyEvent.VK_X },
        { "exit.short_desc", "Exit program" },
        { "exit.enabled", true },
        
        { "contents.name", "Help Contents" },
        { "contents.mnemonic_key", KeyEvent.VK_C },
        { "contents.short_desc", "Getting help" },
        { "contents.enabled", false },
        
        { "about.name", "About ..." },
        { "about.mnemonic_key", KeyEvent.VK_A },
        { "about.short_desc", "About" },
        { "about.enabled", true },
        
        { "Ocean", "Ocean" }, 
        { "Aqua", "Aqua" }, 
        { "Charcoal", "Charcoal" }, 
        { "Contrast", "Contrast" }, 
        { "Emerald", "Emerald" }, 
        { "Ruby", "Ruby" }, 
        { "Default", "Default" }, 

        { "file.name", "File" }, 
        { "file.mnemonic_key", KeyEvent.VK_F }, 
        { "theme.name", "Theme" }, 
        { "theme.mnemonic_key", KeyEvent.VK_T }, 
        { "help.name", "Help" }, 
        { "help.mnemonic_key", KeyEvent.VK_H }, 
        
        { "okay", "OK" },
        { "About.title", "About" }, 
        { "Toolbar.title", "Toolbar" }, 
        { "Detect.title", "Auto Detect" }, 
        { "Panel.title", "Playback Control" }, 

        { "wavfilter", "*.wav (wave files)" }, 
        
        { "threshold", "Signal threshold:" },
        { "interval", "Region interval:" },
        { "min_len", "Minimal signal length:" },
        { "max_gap", "Maximal signal gap:" },
        
        { "action.play", "Play" }, 
        { "action.stop", "Stop" }, 
        { "action.both", "Both" }, 
        { "action.left", "Left only" }, 
        { "action.right", "Right only" }, 
        { "channel.label", "Channels:" }, 
        { "volume.label", "Volume:" }, 
    };
    
    public Object[][] getContents() {
        return contents;
    }
    
}
