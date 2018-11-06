package com.example.caden.codeclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the Button that starts the coding timer
        Button codingStart = (Button) findViewById(R.id.btn_coding_start);
        // Set a click listener on that Button
        codingStart.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "code", Toast.LENGTH_SHORT).show();
            }

        });

        // Find the Button that starts the coding timer
        Button researchStart = (Button) findViewById(R.id.btn_research_start);
        // Set a click listener on that Button
        researchStart.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "research", Toast.LENGTH_SHORT).show();
            }

        });
    }
}
