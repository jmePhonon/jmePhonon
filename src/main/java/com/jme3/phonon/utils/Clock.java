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

    public long getExpectedTimeDelta(double deltaS) {
        switch (this) {
            case MILLISECONDS : {
                return (long)( 1000l * deltaS);
            }

            case HIGHRES : {
                return (long)(sun.misc.Perf.getPerf().highResFrequency() * deltaS);
            }
            default:
            case NANOSECONDS : {
                return (long)(1000000000l * deltaS);
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
