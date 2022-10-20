/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2022 Spazzinq
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
