package com.example.caden.codeclock;

public class Stopwatch extends Thread {

    private long mStartTime;
    private long mElapsed;
    private boolean mPaused;



    public Stopwatch(long startTime, long elapsed, boolean paused){
        mStartTime = startTime;
        mElapsed = elapsed;
        mPaused = paused;
    }

    public void start() {
        mStartTime = System.nanoTime();
        mPaused = false;
    }

    public void pause() {
        mElapsed = mElapsed + (System.nanoTime() - mStartTime);
        mPaused = true;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public long getElapsed() {
        if (mPaused) {
            return mElapsed;
        } else {
            return mElapsed + (System.nanoTime() - mStartTime);
        }
    }
}