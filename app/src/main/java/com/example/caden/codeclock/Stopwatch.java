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
        mStartTime = System.currentTimeMillis();
        mPaused = false;
    }

    public void pause() {
        mElapsed = mElapsed + (System.currentTimeMillis() - mStartTime);
        mPaused = true;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public long getElapsed() {
        if (mPaused) {
            return mElapsed;
        } else {
            mElapsed = mElapsed + (System.currentTimeMillis() - mStartTime);
            return mElapsed;
        }
    }
}