package com.jme3.phonon.utils;

import org.junit.Test;
import junit.framework.TestCase;

/**
 * ClockAndSleeperTest
 */
public class ClockAndSleeperTest extends TestCase {
    


    public double performTest(long msToWait,Clock clock, Sleeper sleeper,long acceptableError) throws InterruptedException {
        
        long controlTime = System.nanoTime();
        double deltaS=msToWait/1000.;

        long m1 = clock.measure();
        long delta = clock.getExpectedTimeDelta(deltaS);
        sleeper.wait(clock, m1, delta);
        
        long controlTimeDelta = System.nanoTime() - controlTime;
        // long expectedTime = (long) (deltaS * 1000000000l);
        // long error = controlTimeDelta - expectedTime;
        // double errorMs = (double) error / 1000000l;

        long actualWaitTime = controlTimeDelta / 1000000l;
        long waitTimeError = msToWait - actualWaitTime;
        if (waitTimeError < 0)
            waitTimeError = -waitTimeError;

            System.out.println("Actual wait "+actualWaitTime+" expected "+msToWait);
            assertTrue("Difference between expected and actual wait time is too great " + waitTimeError,
                waitTimeError <= acceptableError);

        // assertTrue("Difference between  "+errorMs+" ms",errorMs<acceptableError);
        // System.out.println("Error: " + errorMs);
        
        return waitTimeError;
    }

    @Test
    public void testClockAndSleeper() throws InterruptedException {
        System.out.println("Test Sleep");
        performTest(20, Clock.MILLI, Sleeper.SLEEP, 1);
        performTest(32, Clock.NANO, Sleeper.SLEEP,1);
        performTest(17, Clock.HIGHRES, Sleeper.SLEEP, 1);
        
        System.out.println("Test BusySleep");
        performTest(47, Clock.MILLI, Sleeper.BUSYSLEEP, 1);
        performTest(56, Clock.NANO, Sleeper.BUSYSLEEP,1);
        performTest(71, Clock.HIGHRES, Sleeper.BUSYSLEEP, 1);        

        System.out.println("Test BusySleep-nano");
        performTest(87, Clock.MILLI, Sleeper.BUSYSLEEP_NANO, 1);
        performTest(99, Clock.NANO, Sleeper.BUSYSLEEP_NANO,1);
        performTest(76, Clock.HIGHRES, Sleeper.BUSYSLEEP_NANO, 1);

        System.out.println("Test BusyWait");

        performTest(87, Clock.MILLI, Sleeper.BUSYWAIT, 1);
        performTest(99, Clock.NANO, Sleeper.BUSYWAIT,1);
        performTest(76, Clock.HIGHRES, Sleeper.BUSYWAIT,1);
    }
    
}