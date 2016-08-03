/*
 * Detector.java
 *
 * Created on 16 March 2007, 16:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newsound;

/**
 *
 * @author gcheng
 */
public class Detector extends Thread {
    
    private AudioManager   audioManager;
    
    /** Creates a new instance of Detector */
    public Detector(AudioManager audioManager) {
        super("Detector");
        this.audioManager = audioManager;
    }
    
    public void run() {
        int frameLength = audioManager.getFrameLength();
        int channels = audioManager.getChannels();
        
        RegionGenerator rg[] = new RegionGenerator[channels];
        Region[][] result = new Region[channels][];
        
        for (int ch = 0; ch < channels; ch++) {
            java.util.ArrayList<Region> l = new java.util.ArrayList<Region>();
            rg[ch] = new MergedRegionGenerator(new JoinedRegionGenerator(
                    new CompleteRegionGenerator(audioManager, ch)));
            while (rg[ch].hasNext()) {
                Region r = rg[ch].next();
                if (r.isSignalRegion()) {
                    l.add(r);
                }
            }
            result[ch] = l.toArray(new Region[0]);
        }
        audioManager.setRegions(result);
    }
    
    private double interval  = 0.007;
    private double threshold = 0.04;
    private double minSigLen = 0.1;
    private double maxSigGap = 0.9;
    
    public void   setRegionInterval(double newInterval) {
        if (newInterval <= 0)
            throw new IllegalArgumentException("invalid region interval");
        interval = newInterval;
    }
    
    public void   setSignalThreshold(double newThreshold) {
        if (newThreshold <= 0)
            throw new IllegalArgumentException("invalid signal threshold");
        threshold = newThreshold;
    }
    
    public void   setMinimalSignalLength(double newValue) {
        if (newValue <= 0)
            throw new IllegalArgumentException("invalid signal region length");
        minSigLen = newValue;
    }
    
    public void   setMaximalSignalGap(double newValue) {
        if (newValue <= 0)
            throw new IllegalArgumentException("invalid signal gap length");
        maxSigGap = newValue;
    }
    
    public double getRegionInterval() {
        return interval;
    }
    
    public double getSignalThreshold() {
        return threshold;
    }
    
    public int    getMinimalSignalFrameLength() {
        return (int)(minSigLen * audioManager.getFrameRate());
    }
    
    public int    getMaximalSignalFrameGap() {
        return (int)(maxSigGap * audioManager.getFrameRate());
    }
    
    private static final int OPEN     = 0;
    private static final int COMPLETE = 1;
    private static final int JOINED   = 2;
    private static final int MERGED   = 3;
    
    class Region {
        private int     start;
        private int     length;
        private double  summary;
        private int     state;
        
        private int     polarity;   // only used when state is OPEN
        private boolean signal;     // only used when state is MERGED
        private int     count;      // frame count for fixed interval region
        
        /** Create a new instance of Region.
         * @param frame  Starting frame of the region.
         * @param sample Sample of starting frame.
         */
        private Region(int frame, double sample) {
            start   = frame;
            length  = 1;
            summary = sample;
            state   = OPEN;
            if (sample > 0)
                polarity = 1;
            else if (sample < 0)
                polarity = -1;
            else
                polarity = 0;
            
            count = (int)(getRegionInterval() * audioManager.getFrameRate());
        }
        public  int     getStart() {
            return start;
        }
        public  int     getFrameLength() {
            return length;
        }
        public  double  getSummary() {
            return Math.abs(summary);
        }
        public  double  getLevel() {
            return getSummary() / getFrameLength();
        }
        public  boolean isSignalRegion() {
            if (isMerged())
                return signal;
            return getLevel() >= getSignalThreshold();
        }
        public  boolean join(Region another) {
            if (! (isComplete() || isJoined())) {
                throw new IllegalStateException("state must be complete or joined");
            }
            if (! another.isComplete())
                throw new IllegalArgumentException("region not complete");
            if (! isFollowedBy(another))
                throw new IllegalArgumentException("region not next to each other");
            setJoined();
            if (isSignalRegion() == another.isSignalRegion()) {
                length  += another.getFrameLength();
                summary = getSummary() + another.getSummary();
                another = null;
                return true;
            }
            return false;
        }
        public  boolean merge(Region next, Region other, int gap) {
            if (! (isJoined() || isMerged()))
                throw new IllegalStateException("state must be JOINED or MERGED");
            if (! (next.isJoined() && other.isJoined()))
                throw new IllegalArgumentException("region state must be joined");
            if (! isFollowedBy(next) || ! next.isFollowedBy(other)) {
                System.out.println(this);
                System.out.println(next);
                System.out.println(other);
                throw new IllegalArgumentException("regions not next to each other");
            }
            if (isSignalRegion() != other.isSignalRegion())
                throw new IllegalArgumentException("");
            signal = isSignalRegion();
            setMerged();
            if (next.getFrameLength() < gap) {
                length  += next.getFrameLength() + other.getFrameLength();
                summary = getSummary() + next.getSummary() + other.getSummary();
                next = null;
                other = null;
                return true;
            }
            return false;
        }
        public  String  toString() {
            String s;
            s = "start=" + getStart();
            s += ",end=" + (getStart() + getFrameLength());
            s += ",length=" + getFrameLength();
            s += ",state=" + (state == OPEN ? "OPEN" :
                (state == COMPLETE ? "COMPLETE" :
                    (state == JOINED ? "JOINED" : "MERGED")));
            s += ",summary=" + String.format("%.4f", getSummary());
            s += ",level=" + String.format("%.4f", getLevel());
            s += ",isSignalRegion=" + isSignalRegion();
            return s;
        }
        
        /**
         * Process next sample on the same channel. Complete current region
         * if needed.
         *
         * @param  nextSample next sample on the same channel to be processed
         * @return <code>true</code> if processed, <code>false</code> otherwise.
         */
        private boolean processSample(double sample) {
            if (state != OPEN)
                throw new IllegalStateException("region state is not OPEN");
            /*
            if ((sample == 0) && (polarity == 0) ||
                    ((sample > 0) && (polarity > 0)) ||
                    ((sample < 0) && (polarity < 0))) {
                summary += sample;
                length  += 1;
                return true;
            } else {
                setComplete();
                return false;
            }
             */
            if (count >= 0) {
                summary += Math.abs(sample);
                length  += 1;
                count--;
                return true;
            } else {
                setComplete();
                return false;
            }
        }
        private boolean isFollowedBy(Region another) {
            if (! (isComplete() || isJoined() || isMerged()))
                throw new IllegalStateException("region not complete");
            return (getStart() + getFrameLength()) == another.getStart();
        }
        private boolean isComplete() {
            return state == COMPLETE;
        }
        private boolean isJoined() {
            return state == JOINED;
        }
        private boolean isMerged() {
            return state == MERGED;
        }
        private void    setComplete() {
            state = COMPLETE;
        }
        private void    setJoined() {
            state = JOINED;
        }
        private void    setMerged() {
            state = MERGED;
        }
    }
    
    private interface RegionGenerator {
        boolean hasNext();
        Region  next();
    }
    
    private class CompleteRegionGenerator implements RegionGenerator {
        private AudioManager audioManager;
        private int channel;
        private int frame;
        private int frameLength;
        private Region r;
        public CompleteRegionGenerator(AudioManager audioManager, int channel) {
            this.audioManager = audioManager;
            this.channel = channel;
            frame = 0;
            frameLength = audioManager.getFrameLength();
            r = null;
        }
        public final boolean hasNext() {
            return frame < frameLength;
        }
        public Region next() {
            if (! hasNext())
                throw new java.util.NoSuchElementException("no more regions");
            if ((r == null) || r.isComplete()){
                r = new Region(frame, audioManager.decodeSample(frame, channel));
                frame++;
            }
            while (frame < frameLength) {
                double sample = audioManager.decodeSample(frame, channel);
                if (! r.processSample(sample)) {
                    return r;
                }
                frame++;
            }
            r.setComplete();
            return r;
        }
    }
    
    private class JoinedRegionGenerator implements RegionGenerator {
        private CompleteRegionGenerator crg;
        private Region toJoin;
        private Region incoming;
        public JoinedRegionGenerator(CompleteRegionGenerator crg) {
            this.crg = crg;
            toJoin = null;
            incoming = null;
        }
        public final boolean hasNext() {
            return crg.hasNext() || (incoming != null);
        }
        public Region next() {
            if (! hasNext())
                throw new java.util.NoSuchElementException("no more regions");
            toJoin = incoming;
            incoming = null;
            while (crg.hasNext()) {
                incoming = crg.next();
                if (toJoin == null) {
                    toJoin = incoming;
                    incoming = null;
                    continue;
                }
                if (! toJoin.join(incoming)) {
                    return toJoin;
                }
                // following statement is superfluous for
                // it is guranteed by Region.join();
                incoming = null;
            }
            toJoin.setJoined();
            return toJoin;
        }
    }
    
    private class MergedRegionGenerator implements RegionGenerator {
        private JoinedRegionGenerator jrg;
        private Region toMerge;
        private Region middle;
        private Region incoming;
        public MergedRegionGenerator(JoinedRegionGenerator jrg) {
            this.jrg = jrg;
        }
        public final boolean hasNext() {
            return jrg.hasNext() || (incoming != null) || (middle != null);
        }
        private void shift() {
            toMerge = middle;
            middle = incoming;
            incoming = null;
        }
        public Region next() {
            if (! hasNext())
                throw new java.util.NoSuchElementException("no more regions");
            shift();    // 1-1-1 => 1-1-0; 1-1-0 => 1-0-0
            while (jrg.hasNext()) {
                incoming = jrg.next();
                if (middle == null) {   // 0-0-1 => 0-1-x
                    shift();
                    continue;
                }
                if (toMerge == null) {  // 0-1-1 => 1-1-x
                    shift();
                    continue;
                }
                int gap = toMerge.isSignalRegion() ? getMaximalSignalFrameGap()
                : getMinimalSignalFrameLength();
                if (! toMerge.merge(middle, incoming, gap)) {
                    return toMerge; // 1-1-1
                }
                // merge success, 1-0-0 => 0-1-0
                middle = toMerge;
                toMerge = incoming = null;
            }
            if (toMerge == null) { // 0-1-0 => 1-0-0
                shift();
            }
            toMerge.setMerged();
            return toMerge;
        }
    }
}
