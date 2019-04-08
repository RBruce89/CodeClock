package com.example.caden.codeclock;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class ManageUpdateThread {

    private MainActivity mainActivity;

    public ManageUpdateThread(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private String previousCodeTime = "0:00:00";
    private String previousResearchTime = "0:00:00";

    // Receive message from background thread and use it to update clock displays
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle timeBundle = msg.getData();
            String activeClockTime = timeBundle.getString("activeClockTime");

            switch (timeBundle.getString("activeClock")){
                case ("code"):
                    if (!previousCodeTime.equals(activeClockTime)) {
                        TextView codeTime = mainActivity.findViewById(R.id.coding_time);
                        codeTime.setText(activeClockTime);

                        TextView totalTime = mainActivity.findViewById(R.id.total_time);
                        totalTime.setText(timeBundle.getString("totalClockTime"));

                        previousCodeTime = activeClockTime;
                    }
                    break;
                case ("research"):
                    if (!previousResearchTime.equals(activeClockTime)) {
                        TextView researchTime = mainActivity.findViewById(R.id.research_time);
                        researchTime.setText(activeClockTime);

                        TextView totalTime = mainActivity.findViewById(R.id.total_time);
                        totalTime.setText(timeBundle.getString("totalClockTime"));

                        previousResearchTime = activeClockTime;
                    }
                    break;
            }
        }
    };

    // Set instructions for background thread to update clock values
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Stopwatch activeStopwatch = mainActivity.codeStopwatch;
            while (!mainActivity.codeStopwatch.isPaused() || !mainActivity.researchStopwatch.isPaused()) {
                Message timeMessage = Message.obtain();
                Bundle timeBundle = new Bundle();

                if (!mainActivity.codeStopwatch.isPaused()) {
                    activeStopwatch = mainActivity.codeStopwatch;
                    timeBundle.putString("activeClock", "code");
                } else if (!mainActivity.researchStopwatch.isPaused()) {
                    activeStopwatch = mainActivity.researchStopwatch;
                    timeBundle.putString("activeClock", "research");
                }
                timeBundle.putString("activeClockTime", mainActivity.manageClocks.getTimeDisplayValue(
                        activeStopwatch, null));
                timeBundle.putString("totalClockTime", mainActivity.manageClocks.getTimeDisplayValue(
                        mainActivity.codeStopwatch, mainActivity.researchStopwatch));
                timeMessage.setData(timeBundle);

                updateHandler.sendMessage(timeMessage);
            }
        }
    };

    public Runnable getUpdateRunnable() {
        return updateRunnable;
    }
}
