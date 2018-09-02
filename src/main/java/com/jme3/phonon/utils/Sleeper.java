package com.jme3.phonon.utils;

/**
 * Sleeper
 */
public enum Sleeper {

    SLEEP, BUSYSLEEP, BUSYSLEEP_NANO, BUSYWAIT;

    public boolean wait(Clock clock, long startTime, long expectedTimeDelta)
            throws InterruptedException {
        boolean sleepedonce = false;
        switch (this) {
            case SLEEP : {
                switch (clock) {
                    case MILLISECONDS : {
                        long sleeptime = expectedTimeDelta - (clock.measure()-startTime);
                        if (sleeptime > 0) {
                            // System.out.println("Sleep " + sleeptime);
                            Thread.sleep(sleeptime);
                            sleepedonce=true;
                        }
                        break;
                    }
                    case HIGHRES : {
                        sun.misc.Perf perf = sun.misc.Perf.getPerf();
                        long delta = expectedTimeDelta - (clock.measure()-startTime);
                        long sleeptime = (delta * 1000000000l) / perf.highResFrequency();
                        long msToSleep = sleeptime / 1000000;
                        int nsToSleep = (int) (sleeptime - msToSleep * 1000000);
                        if (sleeptime > 0 || nsToSleep > 0) {
                            // System.out.println("Sleep for " + msToSleep + "ms and " + nsToSleep +
                            // " ns");
                            Thread.sleep(msToSleep, nsToSleep);
                            sleepedonce=true;
                            break;
                        }
                    }
                    default:
                    case NANOSECONDS : {
                        long sleeptime = expectedTimeDelta - (clock.measure()-startTime);
                        long msToSleep = sleeptime / 1000000;
                        int nsToSleep = (int) (sleeptime - msToSleep * 1000000);
                        if (sleeptime > 0 || nsToSleep > 0) {
                            // System.out.println("Sleep for " + msToSleep + "ms and " + nsToSleep +
                            // " ns");
                            Thread.sleep(msToSleep, nsToSleep);
                            sleepedonce=true;
                            break;
                        }
                    }
                }
                break;
            }
            case BUSYSLEEP : {
                long expectedEndTime=startTime+expectedTimeDelta;
                while (clock.measure() < expectedEndTime) {
                    Thread.sleep(1);
                    sleepedonce=true;
                }
                break;
            }
            case BUSYSLEEP_NANO : {
                long expectedEndTime=startTime+expectedTimeDelta;

                while (clock.measure() < expectedEndTime) {
                    Thread.sleep(0, 1);
                    sleepedonce=true;
                }
                break;
            }
            case BUSYWAIT : {
                do {
                    long expectedEndTime=startTime+expectedTimeDelta;

                    if (clock.measure() < expectedEndTime)
                        break;
                    sleepedonce = true;
                } while (true);
                break;
            }
        }
        return sleepedonce;
    }

}
