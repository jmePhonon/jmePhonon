package com.jme3.phonon.utils;

/**
 * Sleeper
 */
public enum Sleeper {
    SLEEP, BUSYSLEEP, BUSYSLEEP_NANO, BUSYWAIT,NONE,NATIVE;

    public boolean wait(Clock clock, long startTime, long expectedTimeDelta)
            throws InterruptedException {
        if (clock == Clock.NATIVE||this==NATIVE)
            return true;
        int sleeptfor=0;
        switch (this) {
            case SLEEP : {
                switch (clock) {
                    case MILLI : {
                        long sleeptime = expectedTimeDelta - (clock.measure()-startTime);
                        if (sleeptime > 0) {
                            // System.out.println("Sleep " + sleeptime);
                            Thread.sleep(sleeptime);
                            sleeptfor++;
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
                            Thread.sleep(msToSleep);
                            sleeptfor++;
                            break;
                        }
                    }
                  
                    default:
                    case NANO : {
                        long sleeptime = expectedTimeDelta - (clock.measure()-startTime);
                        long msToSleep = sleeptime / 1000000;
                        int nsToSleep = (int) (sleeptime - msToSleep * 1000000);
                        if (sleeptime > 0 || nsToSleep > 0) {
                            // System.out.println("Sleep for " + msToSleep + "ms and " + nsToSleep +
                            // " ns");
                            Thread.sleep(msToSleep);
                            sleeptfor++;
                            break;
                        }
                    }
                }
                break;
            }
            case BUSYSLEEP : {
                long endTime = clock.measure();
                long diffTime = endTime - startTime;

                if (diffTime < expectedTimeDelta) {
                    long expectedEndTime = startTime + expectedTimeDelta;
                    long timenow;
                    while ((timenow =clock.measure())< expectedEndTime) {
                        Thread.sleep(1);
                        sleeptfor++;
                    }
                }
                break;
            }
            case BUSYSLEEP_NANO : {
                long endTime = clock.measure();
                long diffTime = endTime - startTime;


                if (diffTime < expectedTimeDelta) {
                    long expectedEndTime = startTime + expectedTimeDelta;
                    long timenow;

                    while ((timenow =clock.measure())< expectedEndTime) {
                        Thread.sleep(0, 1);
                        sleeptfor++;
                    }
                }

                break;
            }
            case BUSYWAIT : {
                long endTime = clock.measure();
                long diffTime = endTime - startTime;
                if (diffTime < expectedTimeDelta) {

                    long expectedEndTime = startTime + expectedTimeDelta;
                    do {
                        long timenow;
                        Thread.yield();
                        if ((timenow =clock.measure()) >= expectedEndTime)
                            break;
                            sleeptfor++;
                    } while (true);
                }
                break;
            }
            case NONE : {
                sleeptfor++;
                break;
            }
        }
        return sleeptfor>0;
    }

}
