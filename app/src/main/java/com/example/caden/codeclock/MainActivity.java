package com.example.caden.codeclock;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Handler;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Stopwatch codeStopwatch = new Stopwatch();
    Stopwatch researchStopwatch = new Stopwatch();

    SharedPreferences prefs;
    SharedPreferences projectListPrefs;
    SharedPreferences selectedProjectPrefs;

    String selectedProjectName;
    Boolean freshLoad = true;

    ArrayList<String> projectList =  new ArrayList<>();

    String previousCodeTime = "0:00:00";
    String previousResearchTime = "0:00:00";


    // Receive message from background thread and use it to update clock displays
    final Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle timeBundle = msg.getData();
            String activeClockTime = timeBundle.getString("activeClockTime");

            switch (timeBundle.getString("activeClock")){
                case ("code"):
                    if (!previousCodeTime.equals(activeClockTime)) {
                        TextView codeTime = findViewById(R.id.coding_time);
                        codeTime.setText(activeClockTime);

                        TextView totalTime = findViewById(R.id.total_time);
                        totalTime.setText(timeBundle.getString("totalClockTime"));

                        previousCodeTime = activeClockTime;
                    }
                    break;
                case ("research"):
                    if (!previousResearchTime.equals(activeClockTime)) {
                        TextView researchTime = findViewById(R.id.research_time);
                        researchTime.setText(activeClockTime);

                        TextView totalTime = findViewById(R.id.total_time);
                        totalTime.setText(timeBundle.getString("totalClockTime"));

                        previousResearchTime = activeClockTime;
                    }
                    break;
            }
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

        selectedProjectPrefs = this.getSharedPreferences("selectedProjectName", MODE_PRIVATE);
        selectedProjectName = selectedProjectPrefs.getString("selectedProjectName", "");

        projectListPrefs = this.getSharedPreferences("projectList", MODE_PRIVATE);
        if (!projectListPrefs.contains("projectList"))
        {
            addProject();
        }
        HashSet<String> defaultSet = new HashSet<>();
        projectList = new ArrayList<>(projectListPrefs.getStringSet("projectList", defaultSet));

        ArrayAdapter<String> projectsArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, projectList);
        projectsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        projectsSpinner.setAdapter(projectsArrayAdapter);

        projectsSpinner.setSelection(projectsArrayAdapter.getPosition(selectedProjectName));

        loadClocks(selectedProjectName);

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

        addProjectButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                addProject();
            }
        });

        projectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!freshLoad) {
                    codeStopwatch.pause();
                    researchStopwatch.pause();
                    codingButton.setText(R.string.start);
                    researchButton.setText(R.string.start);
                }
                freshLoad = false;

                saveClocks(selectedProjectName);

                selectedProjectName = projectsSpinner.getSelectedItem().toString();
                SharedPreferences.Editor selectedProjectPrefsEditor = selectedProjectPrefs.edit();
                selectedProjectPrefsEditor.putString("selectedProjectName", selectedProjectName);
                selectedProjectPrefsEditor.commit();
                loadClocks(selectedProjectName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

    }

    //Get variable states for the relevant stopwatches and display the times
    public void loadClocks(String projectName) {
        prefs = this.getSharedPreferences(projectName, MODE_PRIVATE);
        if (prefs.contains(projectName)) {

            Gson gson = new Gson();
            String json = prefs.getString(projectName, "");
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

                TextView codeTime = findViewById(R.id.coding_time);
                codeTime.setText(updateActiveStopwatch(codeStopwatch));
                TextView researchTime = findViewById(R.id.research_time);
                researchTime.setText(updateActiveStopwatch(researchStopwatch));
                TextView totalTime = findViewById(R.id.total_time);
                totalTime.setText(updateTotalStopwatch());
            } catch (JSONException e) {
                System.out.println("json exception on loadClocks catch");
            }
        }
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
        saveClocks(selectedProjectName);
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

    // Update total time display value
    public String updateTotalStopwatch(){
        long currentNanoSeconds = codeStopwatch.getElapsed() + researchStopwatch.getElapsed();
        long hour = TimeUnit.NANOSECONDS.toHours(currentNanoSeconds);
        long minute = TimeUnit.NANOSECONDS.toMinutes(currentNanoSeconds) % 60;
        long second = TimeUnit.NANOSECONDS.toSeconds(currentNanoSeconds) % 60;

        return (String.valueOf(String.format("%01d:%02d:%02d", hour, minute, second)));
    }

    // Save clock variables so they persist through app closes
    public void saveClocks(String projectName){
        JSONObject clockJSONObject = new JSONObject();
        try {
            clockJSONObject.put("codeTime", String.format("%01d", codeStopwatch.getElapsed()));
            clockJSONObject.put("codeState", String.valueOf(codeStopwatch.isPaused()));
            clockJSONObject.put("codeStartTime", String.format("%01d", codeStopwatch.getStartTime()));
            clockJSONObject.put("researchTime", String.format("%01d", researchStopwatch.getElapsed()));
            clockJSONObject.put("researchState", String.valueOf(researchStopwatch.isPaused()));
            clockJSONObject.put("researchStartTime", String.format("%01d", researchStopwatch.getStartTime()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(clockJSONObject);
        prefsEditor.putString(projectName, json);
        prefsEditor.commit();
    }

    //Open pop-up dialog to add a new project
    public void addProject(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Project Name:");

        final EditText projectNameInput = new EditText(this);

        projectNameInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(projectNameInput);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String projectName = projectNameInput.getText().toString();
                projectList.add(projectName);

                HashSet<String> projectListSet = new HashSet<>(projectList);

                SharedPreferences.Editor listPrefsEditor = projectListPrefs.edit();
                listPrefsEditor.putStringSet("projectList", projectListSet);
                listPrefsEditor.commit();

                codeStopwatch.pause();
                researchStopwatch.pause();
                final Button codingButton = findViewById(R.id.btn_coding_start);
                final Button researchButton = findViewById(R.id.btn_research_start);
                codingButton.setText(R.string.start);
                researchButton.setText(R.string.start);

                if (!selectedProjectName.equals("")){
                    saveClocks(selectedProjectName);
                }
                codeStopwatch.reset();
                researchStopwatch.reset();

                saveClocks(projectName);
                loadClocks(projectName);

                Spinner projectsSpinner = findViewById(R.id.projects_spinner);
                ArrayAdapter<String> projectsArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, projectList);
                projectsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                projectsSpinner.setAdapter(projectsArrayAdapter);

                projectsSpinner.setSelection(projectsArrayAdapter.getPosition(projectName));

                selectedProjectName = projectName;
                SharedPreferences.Editor selectedProjectPrefsEditor = selectedProjectPrefs.edit();
                selectedProjectPrefsEditor.putString("selectedProjectName", selectedProjectName);
                selectedProjectPrefsEditor.commit();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
