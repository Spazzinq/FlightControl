/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.object;

public abstract class Timer {
    public static boolean alwaysDecrease;
    private long startTime;
    private long totalTime;
    private long elapsedTime;

    public Timer(long totalTime) {
        this.totalTime = totalTime;
    }

    public void setTotalTime(long totalTime) {
        if (totalTime > 0) {
            this.totalTime = totalTime;
        } else {
            reset();
        }
    }

    public void addTimeLeft(long timeLeft) {
        this.totalTime += timeLeft;
    }

    public void addElapsedTime(long duration) {
        elapsedTime += duration;
    }

    public void start() {
        if (totalTime != 0) {
            startTime = System.currentTimeMillis();
            onStart();
        }
    }

    public void pause() {
        if (!alwaysDecrease && startTime != 0) {
            elapsedTime += System.currentTimeMillis() - startTime;
            startTime = 0;
        }
    }

    public void reset() {
        totalTime = 0;
        startTime = 0;
        elapsedTime = 0;
    }

    public Long getTimeLeft() {
        if (startTime != 0 && totalTime != 0) {
            elapsedTime += System.currentTimeMillis() - startTime;
            startTime = System.currentTimeMillis();

            // Assurance that flight is cancelled
            if (totalTime <= elapsedTime) {
                reset();
                onFinish();
            }
        }

        return totalTime - elapsedTime;
    }

    public boolean hasTimeLeft() {
        return getTimeLeft() != 0;
    }

    public abstract void onFinish();
    public abstract void onStart();
}
