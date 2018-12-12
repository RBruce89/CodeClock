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

public class MainActivity extends AppCompatActivity {

    Stopwatch codeStopwatch = new Stopwatch(0,0,true);
    Stopwatch researchStopwatch = new Stopwatch(0,0,true);

    final Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            String message = (String) msg.obj;
            TextView codeTime = (TextView) findViewById(R.id.coding_time);
            codeTime.setText(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button codingStart = (Button) findViewById(R.id.btn_coding_start);
        codingStart.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "code", Toast.LENGTH_SHORT).show();

                if (codeStopwatch.isPaused()) {
                    codeStopwatch.start();
                    updateThread.start();
                }
                researchStopwatch.pause();

                }

        });

        Button researchStart = (Button) findViewById(R.id.btn_research_start);
        researchStart.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "research", Toast.LENGTH_SHORT).show();
            }

        });
    }

    Thread updateThread = new Thread(new Runnable() {
        @Override
        public void run() {

            while (true) {
                Message message = Message.obtain();
                message.obj = updateDisplays();
                updateHandler.sendMessage(message);
                try {
                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

    });

    public String updateDisplays(){
        int currentSeconds = (int) codeStopwatch.getElapsed() / 1000;
        int hour = currentSeconds / 3600;
        int minute = (currentSeconds - (hour * 3600) / 60);
        int second = currentSeconds - ((hour * 3600) + (minute * 60));

        return (String.valueOf(String.format("%d:%02d:%02d", hour, minute, second)));
    }

}
