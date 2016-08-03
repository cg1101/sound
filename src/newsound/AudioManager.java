/*
 * AudioManager.java
 *
 * Created on February 25, 2007, 8:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

import java.io.File;
import java.io.IOException;
import javax.swing.event.EventListenerList;
import javax.sound.sampled.*;

/**
 *
 * @author gcheng
 */
public class AudioManager {
    protected EventListenerList   listenerList;
    
    private   int                 frameLength;
    private   int                 channels;
    private   float               frameRate;
    private   int                 frameSize;
    private   int                 sampleSize;
    
    private   boolean[]           channelOff;
    
    private   Detector.Region[][] regions;
    
    private   AudioInputStream    ais;
    private   AudioFormat         af;
    private   Decoder             decoder;
    byte[]                        dat;
    
    /** Creates a new instance of AudioManager */
    public AudioManager(File audioFile)
    throws IOException, UnsupportedAudioFileException {
        this(audioFile, true);
    }
    public AudioManager(File audioFile, boolean async)
    throws IOException, UnsupportedAudioFileException {
        listenerList = new EventListenerList();
        
        ais = AudioSystem.getAudioInputStream(audioFile);
        
        AudioFileFormat  aff;
        aff = AudioSystem.getAudioFileFormat(audioFile);
        af  = aff.getFormat();
        
        frameLength = aff.getFrameLength();
        channels    = af.getChannels();
        frameRate   = af.getFrameRate();
        frameSize   = af.getFrameSize();
        sampleSize  = frameSize / channels;
        
        channelOff = new boolean[channels];
        decoder = new Decoder(af);
        
        
        int totalBytes = (int)(frameSize * frameLength);
        try {
            dat = new byte[totalBytes];
            DataLoader t;
            if (async) {
                // load data asynchronously
                t = new DataLoader(ais, dat);
            } else {
                int totalRead = 0, readCount = 0;
                while (totalRead < totalBytes) {
                    readCount = ais.read(dat, totalRead,
                            totalBytes - totalRead);
                    totalRead += readCount;
                }
                ais.close();
                fireAudioDataChanged();
            }
        } catch (java.lang.OutOfMemoryError e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Sorry, not enough memory to load audio file.",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            System.err.println("FATAL: not enough memory to load audio file");
            System.exit(1);
        }
    }
    
////////////////////////
// audio format methods
////////////////////////
    public int getFrameLength() {
        return frameLength;
    }
    
    public int getChannels() {
        return channels;
    }
    
    public int getFrameSize() {
        return frameSize;
    }
    
    public float getFrameRate() {
        return frameRate;
    }
    
    public AudioFormat getAudioFormat() {
        return af;
    }
    
////////////////////////
// audio data methods
////////////////////////
    public boolean isChannelOn(int channel) {
        return ! channelOff[channel];
    }
    
    public void setChannelOn(int channel, boolean isOn) {
        if (channelOff[channel] == isOn) {
            channelOff[channel] = ! isOn;
            fireAudioDataChanged();
        }
    }
    
    public double[] decodeFrame(int frame) {
        double[] result = new double[channels];
        for (int ch = 0; ch < channels; ch++)
            result[ch] = decodeSample(frame, ch);
        return result;
    }
    
    public double decodeSample(int frame, int channel) {
        int offset = frame * frameSize + channel * sampleSize;
        if (dat != null) {
            return decoder.decodeSample(dat, offset);
        } else {
            // decode from AudioInputStream ais
            throw new RuntimeException("FATAL: Not implemented");
        }
    }
    
    public double[][] getFrameData(int frameIn, int frameOut) {
        int chs = getChannels();
        int totalFrame = frameOut - frameIn;
        
        double[][] result = new double[chs][totalFrame];
        
        for (int fr = frameIn; fr < frameOut; fr++) {
            for (int ch = 0; ch < chs; ch++) {
                result[ch][fr - frameIn] = decodeSample(fr, ch);
            }
        }
        return result;
    }
    
    public double[][][] getDisplayData(int frameIn, int frameOut, int width) {
        int chs = getChannels();
        
        double[][][] result = new double[chs][width][2];
        
        int totalFrame = frameOut - frameIn;
        int stride = totalFrame > 50000 ? totalFrame / 50000 : 1;
        
        for (int fr = frameIn; fr < frameOut; fr += stride) {
            int x = (int)Math.floor((fr - frameIn) * (width - 1.0) / totalFrame);
            for (int ch = 0; ch < chs; ch++) {
                double value = decodeSample(fr, ch);
                result[ch][x][0] = Math.max(result[ch][x][0], value);
                result[ch][x][1] = Math.min(result[ch][x][1], value);
            }
        }
        return result;
    }
    
    public void setRegions(Detector.Region[][] regions) {
        this.regions = regions;
        fireAudioDataChanged();
    }
    
    public Detector.Region[][] getRegions() {
        return regions;
    }
    
//////////////////////////
// data listener methods
//////////////////////////
    public void addAudioDataListener(AudioDataListener l) {
        listenerList.add(AudioDataListener.class, l);
    }
    
    public void removeDataListener(AudioDataListener l) {
        listenerList.remove(AudioDataListener.class, l);
    }
    
    private void fireAudioDataChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == AudioDataListener.class) {
                // Lazily create the event:
                //if (audioDataEvent == null)
                //    audioDataEvent = new AudioDataEvent(this);
                ((AudioDataListener)listeners[i+1]).audioDataChanged();
            }
        }
    }
    
///////////////////
// helper classes
///////////////////
    private class Decoder {
        private final int     sampleSize;
        private final boolean bigEndian;
        private final int     scale;
        private final boolean isSigned;
        
        public Decoder(AudioFormat af) {
            bigEndian = af.isBigEndian();
            
            int sampleSizeInBits = af.getSampleSizeInBits();
            scale = (int)Math.pow(2, sampleSizeInBits - 1);
            
            sampleSize = (sampleSizeInBits + 7) / 8;
            
            AudioFormat.Encoding encoding = af.getEncoding();
            if (encoding == AudioFormat.Encoding.PCM_SIGNED)
                isSigned = true;
            else if (encoding == AudioFormat.Encoding.PCM_UNSIGNED)
                isSigned = false;
            else
                throw new IllegalArgumentException("Unsupported format");
        }
        public float decodeSample(byte[] buffer, int offset) {
            int value = 0;
            if (! bigEndian)
                for (int i = offset + sampleSize - 1; i >= offset; i--)
                    value = (value << 8) | (buffer[i] & 0xFF);
            else
                for (int i = offset; i < offset + sampleSize; i++)
                    value = (value << 8) | (buffer[i] & 0xFF);
            if (isSigned) {
                if (value >= scale)
                    value = value - scale * 2;
            } else
                value = value - scale;
            return (float)value / scale;
        }
    }
    
    private class DataLoader extends Thread {
        private AudioInputStream ais;
        private byte[]           dataBuffer;
        private int              percentage = 0;
        public DataLoader(AudioInputStream ais, byte[] dataBuffer) {
            super("DataLoader");
            this.ais = ais;
            this.dataBuffer = dataBuffer;
            start();
        }
        public void run() {
            int totalBytes = dataBuffer.length;
            int totalRead = 0, readCount = 0;
            int oneGo = totalBytes / 500;
            try {
                while (totalRead < totalBytes) {
                    readCount = ais.read(dat, totalRead,
                            Math.min(oneGo, totalBytes - totalRead));
                    totalRead += readCount;
                    int newPercentage;
                    newPercentage = (int)((totalRead) / (totalBytes * 0.01));
                    if (newPercentage > percentage)
                        fireAudioDataChanged();
                    percentage = newPercentage;
                    try {
                        sleep(2);
                    } catch (InterruptedException e) {}
                }
                ais.close();
            } catch (IOException e) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "I/O Error when loading data from input file.",
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                System.err.println("WARNING: data loading error");
            }
        }
    }
}
