package com.jme3.phonon.utils;

public enum Clock {
    MILLISECONDS, NANOSECONDS, HIGHRES;

    private Clock() {

    }

    public long measure() {
        switch (this) {
            case MILLISECONDS : {
                return System.currentTimeMillis();
            }
            case HIGHRES : {
                sun.misc.Perf perf = sun.misc.Perf.getPerf();
                return perf.highResCounter();
            }
            default:
            case NANOSECONDS : {
                return System.nanoTime();
            }
        }

    }

    public long getExpectedTimeDelta(long updatesPerS) {
        switch (this) {
            case MILLISECONDS : {
                return 1000l / updatesPerS;
            }

            case HIGHRES : {
                return sun.misc.Perf.getPerf().highResFrequency() / updatesPerS;
            }
            default:
            case NANOSECONDS : {
                return 1000000000l / updatesPerS;
            }
        }
    }

    // public long diff(long previousMeasure) {
    //     switch (this) {
    //         case MILLISECONDS : {
    //             return measure() - previousMeasure;
    //         }
    //         case HIGHRES : {
    //             return measure() - previousMeasure;
    //         }
    //         default:
    //         case NANOSECONDS : {
    //             return measure() - previousMeasure;
    //         }
    //     }
    // }
}
