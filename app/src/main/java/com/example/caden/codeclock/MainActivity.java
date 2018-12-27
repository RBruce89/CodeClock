package com.example.caden.codeclock;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import android.os.Handler;
import java.lang.Thread;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Stopwatch codeStopwatch = new Stopwatch();
    Stopwatch researchStopwatch = new Stopwatch();

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

    public String updateActiveStopwatch(Stopwatch activeStopwatch){

        long currentNanoSeconds = activeStopwatch.getElapsed();
        long hour = TimeUnit.NANOSECONDS.toHours(currentNanoSeconds);
        long minute = TimeUnit.NANOSECONDS.toMinutes(currentNanoSeconds) % 60;
        long second = TimeUnit.NANOSECONDS.toSeconds(currentNanoSeconds) % 60;

        return (String.valueOf(String.format("%01d:%02d:%02d", hour, minute, second)));
    }

    public String updateTotalStopwatch(){

        long currentNanoSeconds = codeStopwatch.getElapsed() + researchStopwatch.getElapsed();
        long hour = TimeUnit.NANOSECONDS.toHours(currentNanoSeconds);
        long minute = TimeUnit.NANOSECONDS.toMinutes(currentNanoSeconds) % 60;
        long second = TimeUnit.NANOSECONDS.toSeconds(currentNanoSeconds) % 60;

        return (String.valueOf(String.format("%01d:%02d:%02d", hour, minute, second)));
    }

}
