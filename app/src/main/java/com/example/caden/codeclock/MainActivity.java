package com.example.caden.codeclock;

import android.content.SharedPreferences;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import android.os.Handler;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Thread;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Stopwatch codeStopwatch = new Stopwatch();
    Stopwatch researchStopwatch = new Stopwatch();

    SharedPreferences prefs;

    // Receive message from background thread and use it to update clock displays
    final Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle timeBundle = msg.getData();

            switch (timeBundle.getString("activeClock")){
                case ("code"):
                    TextView codeTime = findViewById(R.id.coding_time);
                    codeTime.setText(timeBundle.getString("activeClockTime"));
                    break;
                case ("research"):
                    TextView researchTime = findViewById(R.id.research_time);
                    researchTime.setText(timeBundle.getString("activeClockTime"));
                    break;
            }
            TextView totalTime = findViewById(R.id.total_time);
            totalTime.setText(timeBundle.getString("totalClockTime"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button codingButton = findViewById(R.id.btn_coding_start);
        final Button researchButton = findViewById(R.id.btn_research_start);

        prefs = this.getSharedPreferences("mainProfile", MODE_PRIVATE);
        //prefs.edit().clear().commit();
        if (prefs.contains("mainProfile")) {

            Gson gson = new Gson();
            String json = prefs.getString("mainProfile", "");
            JSONObject mainProfile = gson.fromJson(json, JSONObject.class);

            try {
                String codeTimeString = mainProfile.get("codeTime").toString();
                String codeStateString = mainProfile.get("codeState").toString();
                String codeStartTimeString = mainProfile.get("codeStartTime").toString();
                String researchTimeString = mainProfile.get("researchTime").toString();
                String researchStateString = mainProfile.get("researchState").toString();
                String researchStartTimeString = mainProfile.get("researchStartTime").toString();

                codeStopwatch.setElapsed(Long.parseLong(codeTimeString));
                codeStopwatch.setPaused(Boolean.parseBoolean(codeStateString));
                codeStopwatch.setStartTime(Long.parseLong(codeStartTimeString));
                researchStopwatch.setElapsed(Long.parseLong(researchTimeString));
                researchStopwatch.setPaused(Boolean.parseBoolean(researchStateString));
                researchStopwatch.setStartTime(Long.parseLong(researchStartTimeString));
            } catch (JSONException e) {
                System.out.println("json exception on startup catch");
            }
        }

        codingButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                if (codeStopwatch.isPaused()) {
                    researchStopwatch.pause();
                    new Thread(updateRunnable).start();
                    codeStopwatch.start();
                    codingButton.setText(R.string.pause);
                    researchButton.setText(R.string.start);
                } else if (!codeStopwatch.isPaused()) {
                    codeStopwatch.pause();
                    codingButton.setText(R.string.start);
                }
            }
        });

        researchButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                if (researchStopwatch.isPaused()) {
                    codeStopwatch.pause();
                    new Thread(updateRunnable).start();
                    researchStopwatch.start();
                    researchButton.setText(R.string.pause);
                    codingButton.setText(R.string.start);
                } else if (!researchStopwatch.isPaused()) {
                    researchStopwatch.pause();
                    researchButton.setText(R.string.start);
                }
            }

        });
    }

    // Update display to last status
    @Override
    public void onResume(){
        super.onResume();
        TextView codeTime = findViewById(R.id.coding_time);
        codeTime.setText(updateActiveStopwatch(codeStopwatch));

        TextView researchTime = findViewById(R.id.research_time);
        researchTime.setText(updateActiveStopwatch(researchStopwatch));

        TextView totalTime = findViewById(R.id.total_time);
        totalTime.setText(updateTotalStopwatch());

        if(!codeStopwatch.isPaused() || !researchStopwatch.isPaused()){
            new Thread(updateRunnable).start();
            if (!codeStopwatch.isPaused()){
                Button codingButton = findViewById(R.id.btn_coding_start);
                codingButton.setText(R.string.pause);
            } else {
                Button researchButton = findViewById(R.id.btn_research_start);
                researchButton.setText(R.string.pause);
            }
        }
    }

    // Save clocks when app stops
    @Override
    public void onPause(){
        super.onPause();
        saveClocks();
    }

    // Set instructions for background thread to update clock values
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                Stopwatch activeStopwatch = codeStopwatch;
                while (!codeStopwatch.isPaused() || !researchStopwatch.isPaused()) {
                    Message message = Message.obtain();
                    Bundle timeBundle = new Bundle();

                    if (!codeStopwatch.isPaused()) {
                        activeStopwatch = codeStopwatch;
                        timeBundle.putString("activeClock", "code");
                    } else if (!researchStopwatch.isPaused()) {
                        activeStopwatch = researchStopwatch;
                        timeBundle.putString("activeClock", "research");
                    }
                    timeBundle.putString("activeClockTime", updateActiveStopwatch(activeStopwatch));
                    timeBundle.putString("totalClockTime", updateTotalStopwatch());
                    message.setData(timeBundle);

                    updateHandler.sendMessage(message);
                }
            }
        };

    // Update running clock's display value
    public String updateActiveStopwatch(Stopwatch activeStopwatch){
        long currentNanoSeconds = activeStopwatch.getElapsed();
        long hour = TimeUnit.NANOSECONDS.toHours(currentNanoSeconds);
        long minute = TimeUnit.NANOSECONDS.toMinutes(currentNanoSeconds) % 60;
        long second = TimeUnit.NANOSECONDS.toSeconds(currentNanoSeconds) % 60;

        return (String.valueOf(String.format("%01d:%02d:%02d", hour, minute, second)));
    }

    // update total time display value
    public String updateTotalStopwatch(){
        long currentNanoSeconds = codeStopwatch.getElapsed() + researchStopwatch.getElapsed();
        long hour = TimeUnit.NANOSECONDS.toHours(currentNanoSeconds);
        long minute = TimeUnit.NANOSECONDS.toMinutes(currentNanoSeconds) % 60;
        long second = TimeUnit.NANOSECONDS.toSeconds(currentNanoSeconds) % 60;

        return (String.valueOf(String.format("%01d:%02d:%02d", hour, minute, second)));
    }

    // Save clock variables so they persist through app closes
    public void saveClocks(){
        JSONObject mainProfile = new JSONObject();
        try {
            mainProfile.put("codeTime", String.format("%01d", codeStopwatch.getElapsed()));
            mainProfile.put("codeState", String.valueOf(codeStopwatch.isPaused()));
            mainProfile.put("codeStartTime", String.format("%01d", codeStopwatch.getStartTime()));
            mainProfile.put("researchTime", String.format("%01d", researchStopwatch.getElapsed()));
            mainProfile.put("researchState", String.valueOf(researchStopwatch.isPaused()));
            mainProfile.put("researchStartTime", String.format("%01d", researchStopwatch.getStartTime()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mainProfile);
        prefsEditor.putString("mainProfile", json);
        prefsEditor.commit();
    }
}
