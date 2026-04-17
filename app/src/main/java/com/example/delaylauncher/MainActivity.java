package com.example.delaylauncher;

import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;
import android.os.CountDownTimer;

public class MainActivity extends AppCompatActivity {

    private boolean userInteracted = false;
    private Spinner delaySpinner, appSpinner;
    private Button startButton, resetButton;
    private List<ResolveInfo> launchableApps;
    private static final String PREFS = "DelayPrefs";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        if (prefs.getBoolean("configured", false)) {

            setContentView(R.layout.activity_main);

            delaySpinner = findViewById(R.id.delaySpinner);
            appSpinner = findViewById(R.id.appSpinner);
            startButton = findViewById(R.id.startButton);
            resetButton = findViewById(R.id.resetButton);

            delaySpinner.setOnTouchListener((v, e) -> {
                userInteracted = true;
                return false;
            });

            appSpinner.setOnTouchListener((v, e) -> {
                userInteracted = true;
                return false;
            });

            FrameLayout circleContainer = findViewById(R.id.circleContainer);
            ProgressBar circleProgress = findViewById(R.id.circleProgress);
            TextView circleText = findViewById(R.id.circleText);

            loadLaunchableApps();
            setupSpinners();

            int savedDelay = prefs.getInt("delay", 20);
            String savedPackage = prefs.getString("package", null);

            delaySpinner.setSelection(Arrays.asList("10","15","20","25","30","35","40","45","50","55","60")
                .indexOf(String.valueOf(savedDelay)));

            if (savedPackage != null) {
                for (int i = 0; i < launchableApps.size(); i++) {
                    if (launchableApps.get(i).activityInfo.packageName.equals(savedPackage)) {
                        appSpinner.setSelection(i);
                        break;
                    }
                }
            }

            new Handler().postDelayed(() -> {
                int delay = savedDelay;
                String packageName = savedPackage;

                if (packageName != null) {
                    startDelayedLaunch(delay, packageName);
                    finish();
                }
            }, 5000);

            return;
        }

        setContentView(R.layout.activity_main);

        delaySpinner = findViewById(R.id.delaySpinner);
        appSpinner = findViewById(R.id.appSpinner);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);

        FrameLayout circleContainer = findViewById(R.id.circleContainer);
        ProgressBar circleProgress = findViewById(R.id.circleProgress);
        TextView circleText = findViewById(R.id.circleText);

        circleContainer.setVisibility(View.GONE);

        loadLaunchableApps();
        setupSpinners();

        startButton.setOnClickListener(v -> {
            userInteracted = true;

            int delay = Integer.parseInt(delaySpinner.getSelectedItem().toString());
            String packageName = launchableApps.get(appSpinner.getSelectedItemPosition()).activityInfo.packageName;

            prefs.edit()
                    .putBoolean("configured", true)
                    .putInt("delay", delay)
                    .putString("package", packageName)
                    .apply();

            if (delay > 10) {

                // Nasconde UI
                delaySpinner.setVisibility(View.GONE);
                appSpinner.setVisibility(View.GONE);
                startButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.GONE);

                circleContainer.setVisibility(View.VISIBLE);

                final long totalTime = delay * 1000L;

                new CountDownTimer(totalTime, 16) {

                    public void onTick(long millisUntilFinished) {

                        float progress = ((totalTime - millisUntilFinished) / (float) totalTime) * 100f;
                        circleProgress.setProgress((int) progress);

                        int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                        circleText.setText(String.valueOf(secondsLeft));
                    }

                    public void onFinish() {
                        circleProgress.setProgress(100);
                        circleText.setText("🚀");

                        // lancio diretto senza delay → evita lag
                        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                        if (intent != null) {
                            startActivity(intent);
                        }

                        finish(); // chiude launcher → evita blocchi
                    }

                }.start();

            } else {
                startDelayedLaunch(delay, packageName);
            }

        });

        resetButton.setOnClickListener(v -> {
            userInteracted = true;
            getSharedPreferences(PREFS, MODE_PRIVATE)
               .edit()
               .clear()
               .commit(); // NON apply()

            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();
        });
    }

    private void startDelayedLaunch(int delaySeconds, String packageName) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) startActivity(intent);
        }, delaySeconds * 1000L);
    }

    private void loadLaunchableApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_HOME);
        launchableApps = getPackageManager().queryIntentActivities(intent, 0);
    }

    private void setupSpinners() {
        String[] delays = {"10","15","20","25","30","35","40","45","50","55","60"};
        delaySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, delays));
        delaySpinner.setSelection(2); // default 20

        List<String> names = new ArrayList<>();
        for (ResolveInfo app : launchableApps) {
            names.add(app.loadLabel(getPackageManager()).toString());
        }
        appSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
    }
}
