/*
 * Version.java
 *
 * Created on March 23, 2007, 7:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package application;

/**
 *
 * @author gcheng
 */
public class Version {
    
    static int major = 1;
    static int minor = 0;

    public static String name() {
        return "wavPlayer";
    }
    
    public static String ver() {
        return "v" + major + "." + minor;
    }
    
    public static String getVersion() {
        return name() + " " + ver();
    }
}
