package com.example.caden.codeclock;

public class Stopwatch extends Thread {

    private long mStartTime;
    private long mElapsed;
    private boolean mPaused = true;



    public Stopwatch(){
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public void setElapsed(long elapsed) {
        mElapsed = elapsed;
    }

    public void setPaused(boolean paused) {
        mPaused = paused;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getElapsed() {
        if (mPaused) {
            return mElapsed;
        } else {
            return mElapsed + (System.nanoTime() - mStartTime);
        }
    }

    public void start() {
        mStartTime = System.nanoTime();
        mPaused = false;
    }

    public void pause() {
        if (!mPaused) {
            mElapsed = mElapsed + (System.nanoTime() - mStartTime);
            mPaused = true;
        }
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void reset(){
        mPaused = true;
        mElapsed = 0;
        mStartTime = 0;
    }

}