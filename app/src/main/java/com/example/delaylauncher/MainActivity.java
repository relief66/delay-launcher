
package com.example.delaylauncher;

import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private Spinner delaySpinner, appSpinner;
    private Button startButton, resetButton;
    private List<ResolveInfo> launchableApps;
    private static final String PREFS = "DelayPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        if (prefs.getBoolean("configured", false)) {
            startDelayedLaunch(prefs.getInt("delay", 10), prefs.getString("package", null));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        loadLaunchableApps();
        setupSpinners();

        startButton.setOnClickListener(v -> {
            int delay = Integer.parseInt(delaySpinner.getSelectedItem().toString());
            String packageName = launchableApps.get(appSpinner.getSelectedItemPosition()).activityInfo.packageName;

            prefs.edit()
                    .putBoolean("configured", true)
                    .putInt("delay", delay)
                    .putString("package", packageName)
                    .apply();

            startDelayedLaunch(delay, packageName);
        });

        resetButton.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            recreate();
        });
    }

    private void startDelayedLaunch(int delaySeconds, String packageName) {
        new Handler().postDelayed(() -> {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) startActivity(intent);
        }, delaySeconds * 1000L);
    }

    private void loadLaunchableApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        launchableApps = getPackageManager().queryIntentActivities(intent, 0);
    }

    private void setupSpinners() {
        String[] delays = {"10","15","30","45","60"};
        delaySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, delays));

        List<String> names = new ArrayList<>();
        for (ResolveInfo app : launchableApps) {
            names.add(app.loadLabel(getPackageManager()).toString());
        }
        appSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
    }

    private View createLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60,60,60,60);

        TextView title = new TextView(this);
        title.setText("Delay Launcher");
        title.setTextSize(22);

        delaySpinner = new Spinner(this);
        appSpinner = new Spinner(this);

        startButton = new Button(this);
        startButton.setText("Avvia");

        resetButton = new Button(this);
        resetButton.setText("Reset");

        layout.addView(title);
        layout.addView(delaySpinner);
        layout.addView(appSpinner);
        layout.addView(startButton);
        layout.addView(resetButton);

        return layout;
    }
}
