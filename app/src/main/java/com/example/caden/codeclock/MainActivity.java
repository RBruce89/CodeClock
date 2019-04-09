package com.example.caden.codeclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import java.lang.Thread;

public class MainActivity extends AppCompatActivity {

    ManageClocks manageClocks = new ManageClocks(this);
    ManageSpinner manageSpinner = new ManageSpinner(this);
    ManageUpdateThread manageUpdateThread = new ManageUpdateThread(this);

    Stopwatch codeStopwatch = new Stopwatch();
    Stopwatch researchStopwatch = new Stopwatch();

    AdapterView.OnItemSelectedListener projectSpinnerItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                manageSpinner.projectSpinnerItemSelected();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button codingButton = findViewById(R.id.btn_coding_start);
        final Button researchButton = findViewById(R.id.btn_research_start);
        final Button addProjectButton = findViewById(R.id.btn_add_project);
        final Spinner projectsSpinner = findViewById(R.id.projects_spinner);

        manageSpinner.loadSpinner();
        manageClocks.loadClocks(manageSpinner.getSelectedProjectName());

        codingButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                if (codeStopwatch.isPaused()) {
                    researchStopwatch.pause();
                    new Thread(manageUpdateThread.getUpdateRunnable()).start();
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
                    new Thread(manageUpdateThread.getUpdateRunnable()).start();
                    researchStopwatch.start();
                    researchButton.setText(R.string.pause);
                    codingButton.setText(R.string.start);
                } else if (!researchStopwatch.isPaused()) {
                    researchStopwatch.pause();
                    researchButton.setText(R.string.start);
                }
            }

        });

        addProjectButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                manageSpinner.addProject();
            }
        });
        projectsSpinner.setOnItemSelectedListener(projectSpinnerItemSelectedListener);
    }

    // Update display to last status
    @Override
    public void onResume(){
        super.onResume();
        TextView codeTime = findViewById(R.id.coding_time);
        codeTime.setText(manageClocks.getTimeDisplayValue(codeStopwatch, null));

        TextView researchTime = findViewById(R.id.research_time);
        researchTime.setText(manageClocks.getTimeDisplayValue(researchStopwatch, null));

        TextView totalTime = findViewById(R.id.total_time);
        totalTime.setText(manageClocks.getTimeDisplayValue(codeStopwatch, researchStopwatch));

        if(!codeStopwatch.isPaused() || !researchStopwatch.isPaused()){
            new Thread(manageUpdateThread.getUpdateRunnable()).start();
            if (!codeStopwatch.isPaused()){
                final Button codingButton = findViewById(R.id.btn_coding_start);
                codingButton.setText(R.string.pause);
            } else {
                final Button researchButton = findViewById(R.id.btn_research_start);
                researchButton.setText(R.string.pause);
            }
        }
    }

    // Save clocks when app stops
    @Override
    public void onPause(){
        super.onPause();
        manageClocks.saveClocks(manageSpinner.getSelectedProjectName());
    }
}
