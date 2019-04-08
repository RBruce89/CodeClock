package com.example.caden.codeclock;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashSet;

public class ManageSpinner {

    private MainActivity mainActivity;

    public ManageSpinner(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private SharedPreferences projectListPrefs;
    private SharedPreferences selectedProjectPrefs;

    private ArrayList<String> projectList =  new ArrayList<>();
    public String selectedProjectName;

    private Boolean freshLoad = true;

    //Define OnItemSelected behaviour
    public void projectSpinnerItemSelected(){
        if (!freshLoad) {
            mainActivity.codeStopwatch.pause();
            mainActivity.researchStopwatch.pause();
            final Button codingButton = mainActivity.findViewById(R.id.btn_coding_start);
            codingButton.setText(R.string.start);
            final Button researchButton = mainActivity.findViewById(R.id.btn_research_start);
            researchButton.setText(R.string.start);
        }
        freshLoad = false;

        mainActivity.manageClocks.saveClocks(selectedProjectName);

        final Spinner projectsSpinner = mainActivity.findViewById(R.id.projects_spinner);
        selectedProjectName = projectsSpinner.getSelectedItem().toString();
        SharedPreferences.Editor selectedProjectPrefsEditor = selectedProjectPrefs.edit();
        selectedProjectPrefsEditor.putString("selectedProjectName", selectedProjectName);
        selectedProjectPrefsEditor.apply();

        mainActivity.manageClocks.loadClocks(selectedProjectName);
    }

    //Prepare relevant shared prefs and variables for spinner
    public void loadSpinner(){
        selectedProjectPrefs = mainActivity.getSharedPreferences(
                "selectedProjectName", mainActivity.MODE_PRIVATE);
        selectedProjectName = selectedProjectPrefs.getString("selectedProjectName", "");

        projectListPrefs = mainActivity.getSharedPreferences(
                "projectList", mainActivity.MODE_PRIVATE);
        if (!projectListPrefs.contains("projectList"))
        {
            addProject();
        }
        HashSet<String> defaultSet = new HashSet<>();
        projectList = new ArrayList<>(projectListPrefs.getStringSet("projectList", defaultSet));

        Spinner projectsSpinner = mainActivity.findViewById(R.id.projects_spinner);
        ArrayAdapter<String> projectsArrayAdapter = new ArrayAdapter<>(
                this.mainActivity, android.R.layout.simple_spinner_item, projectList);
        projectsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        projectsSpinner.setAdapter(projectsArrayAdapter);
        projectsSpinner.setSelection(projectsArrayAdapter.getPosition(selectedProjectName));
    }

    //Open pop-up dialog to add a new project
    public void addProject(){
        AlertDialog.Builder projectBuilder = new AlertDialog.Builder(mainActivity);
        projectBuilder.setTitle("New Project Name:");

        final EditText projectNameInput = new EditText(mainActivity);

        projectNameInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        projectBuilder.setView(projectNameInput);

        projectBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String projectName = projectNameInput.getText().toString();
                projectList.add(projectName);

                HashSet<String> projectListSet = new HashSet<>(projectList);

                SharedPreferences.Editor listPrefsEditor = projectListPrefs.edit();
                listPrefsEditor.putStringSet("projectList", projectListSet);
                listPrefsEditor.apply();

                mainActivity.codeStopwatch.pause();
                mainActivity.researchStopwatch.pause();
                final Button codingButton = mainActivity.findViewById(R.id.btn_coding_start);
                codingButton.setText(R.string.start);
                final Button researchButton = mainActivity.findViewById(R.id.btn_research_start);
                researchButton.setText(R.string.start);

                if (!selectedProjectName.equals("")){
                    mainActivity.manageClocks.saveClocks(selectedProjectName);
                }
                mainActivity.codeStopwatch.reset();
                mainActivity.researchStopwatch.reset();

                mainActivity.manageClocks.saveClocks(projectName);
                mainActivity.manageClocks.loadClocks(projectName);

                Spinner projectsSpinner = mainActivity.findViewById(R.id.projects_spinner);
                ArrayAdapter<String> projectsArrayAdapter = new ArrayAdapter<String>(
                        mainActivity, android.R.layout.simple_spinner_item, projectList);
                projectsArrayAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                projectsSpinner.setAdapter(projectsArrayAdapter);
                projectsSpinner.setSelection(projectsArrayAdapter.getPosition(projectName));

                selectedProjectName = projectName;
                SharedPreferences.Editor selectedProjectPrefsEditor = selectedProjectPrefs.edit();
                selectedProjectPrefsEditor.putString("selectedProjectName", selectedProjectName);
                selectedProjectPrefsEditor.apply();
            }
        });
        projectBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        projectBuilder.show();
    }
}
