package com.example.caden.codeclock;

import android.content.SharedPreferences;
import android.widget.TextView;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ManageClocks{
    private SharedPreferences stopwatchPrefs;

    private MainActivity mainActivity;

    public ManageClocks(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    //Get variable states for the relevant stopwatches and display the times
    public void loadClocks(String projectName) {
        stopwatchPrefs = mainActivity.getSharedPreferences(projectName, mainActivity.MODE_PRIVATE);
        if (stopwatchPrefs.contains(projectName)) {

            Gson stopwatchGson = new Gson();
            String stopwatchJson = stopwatchPrefs.getString(projectName, "");
            JSONObject clockValuesJson = stopwatchGson.fromJson(stopwatchJson, JSONObject.class);

            try {
                String codeTimeString = clockValuesJson.get("codeTime").toString();
                String codeStateString = clockValuesJson.get("codeState").toString();
                String codeStartTimeString = clockValuesJson.get("codeStartTime").toString();
                String researchTimeString = clockValuesJson.get("researchTime").toString();
                String researchStateString = clockValuesJson.get("researchState").toString();
                String researchStartTimeString = clockValuesJson.get("researchStartTime").toString();

                mainActivity.codeStopwatch.setElapsed(Long.parseLong(codeTimeString));
                mainActivity.codeStopwatch.setPaused(Boolean.parseBoolean(codeStateString));
                mainActivity.codeStopwatch.setStartTime(Long.parseLong(codeStartTimeString));
                mainActivity.researchStopwatch.setElapsed(Long.parseLong(researchTimeString));
                mainActivity.researchStopwatch.setPaused(Boolean.parseBoolean(researchStateString));
                mainActivity.researchStopwatch.setStartTime(Long.parseLong(researchStartTimeString));

                TextView codeTime = mainActivity.findViewById(R.id.coding_time);
                codeTime.setText(getTimeDisplayValue(mainActivity.codeStopwatch, null));
                TextView researchTime = mainActivity.findViewById(R.id.research_time);
                researchTime.setText(getTimeDisplayValue(mainActivity.researchStopwatch, null));
                TextView totalTime = mainActivity.findViewById(R.id.total_time);
                totalTime.setText(getTimeDisplayValue(
                        mainActivity.codeStopwatch, mainActivity.researchStopwatch));
            } catch (JSONException e) {
                System.out.println("json exception on loadClocks catch");
            }
        }
    }

    // Save clock variables so they persist through app closes
    public void saveClocks(String projectName){
        JSONObject clockJSONObject = new JSONObject();
        try {
            clockJSONObject.put("codeTime", String.format(
                    Locale.US, "%01d", mainActivity.codeStopwatch.getElapsed()));
            clockJSONObject.put("codeState", String.valueOf(
                    mainActivity.codeStopwatch.isPaused()));
            clockJSONObject.put("codeStartTime", String.format(
                    Locale.US, "%01d", mainActivity.codeStopwatch.getStartTime()));
            clockJSONObject.put("researchTime", String.format(
                    Locale.US, "%01d", mainActivity.researchStopwatch.getElapsed()));
            clockJSONObject.put("researchState", String.valueOf(
                    mainActivity.researchStopwatch.isPaused()));
            clockJSONObject.put("researchStartTime", String.format(
                    Locale.US, "%01d", mainActivity.researchStopwatch.getStartTime()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor stopWatchPrefsEditor = stopwatchPrefs.edit();
        Gson stopwatchGson = new Gson();
        String stopwatchJson = stopwatchGson.toJson(clockJSONObject);
        stopWatchPrefsEditor.putString(projectName, stopwatchJson);
        stopWatchPrefsEditor.commit();
    }

    // Return formatted value for either or both stopwatch times
    public String getTimeDisplayValue(Stopwatch stopwatch1, Stopwatch stopwatch2){
        long currentNanoSeconds;
        if (stopwatch2 == null) {
            currentNanoSeconds = stopwatch1.getElapsed();
        } else {
            currentNanoSeconds = stopwatch1.getElapsed() + stopwatch2.getElapsed();
        }
        long hour = TimeUnit.NANOSECONDS.toHours(currentNanoSeconds);
        long minute = TimeUnit.NANOSECONDS.toMinutes(currentNanoSeconds) % 60;
        long second = TimeUnit.NANOSECONDS.toSeconds(currentNanoSeconds) % 60;

        return (String.valueOf(String.format(
                Locale.US, "%01d:%02d:%02d", hour, minute, second)));
    }
}
