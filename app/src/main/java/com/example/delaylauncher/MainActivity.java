package com.example.delaylauncher;

import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;
import android.os.CountDownTimer;

public class MainActivity extends AppCompatActivity {

    private Spinner delaySpinner, appSpinner;
    private Button startButton, resetButton;
    private List<ResolveInfo> launchableApps;
    private static final String PREFS = "DelayPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean isConfigured = prefs.getBoolean("configured", false);

        setContentView(R.layout.activity_main);

        delaySpinner = findViewById(R.id.delaySpinner);
        appSpinner = findViewById(R.id.appSpinner);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);

        FrameLayout circleContainer = findViewById(R.id.circleContainer);
        ProgressBar circleProgress = findViewById(R.id.circleProgress);
        TextView circleText = findViewById(R.id.circleText);

        circleContainer.setVisibility(FrameLayout.GONE);

        circleContainer.setScaleX(0.95f);
        circleContainer.setScaleY(0.95f);

        circleContainer.animate()
            
        .scaleX(1.05f)
        .scaleY(1.05f)
        .setDuration(900)
        .setRepeatMode(android.animation.ValueAnimator.REVERSE)
        .setRepeatCount(android.animation.ValueAnimator.INFINITE)
        .start();

        loadLaunchableApps();
        setupSpinners();

        if (isConfigured) {

            int savedDelay = prefs.getInt("delay", 20);
            String savedPackage = prefs.getString("package", null);

            hideSetupUI();
            circleContainer.setVisibility(FrameLayout.VISIBLE);

            if (savedPackage != null) {

                new Handler(Looper.getMainLooper()).postDelayed(() -> {

                    new CountDownTimer(savedDelay * 1000L, 100) {

                        public void onTick(long millisUntilFinished) {
                            int progress = (int)((savedDelay * 1000L - millisUntilFinished) * 100 / (savedDelay * 1000L));
                            circleProgress.setProgress(progress);

                            int sec = (int)Math.ceil(millisUntilFinished / 1000.0);
                            circleText.setText(String.valueOf(sec));
                        }

                        public void onFinish() {
                            Intent intent = getPackageManager().getLaunchIntentForPackage(savedPackage);
                            if (intent != null) startActivity(intent);
                            finishAffinity();
                        }

                    }.start();

                }, 200);
            }

            return;
        }

        startButton.setOnClickListener(v -> {

            int delay = Integer.parseInt(delaySpinner.getSelectedItem().toString());
            String pkg = launchableApps.get(appSpinner.getSelectedItemPosition()).activityInfo.packageName;

            prefs.edit()
                    .putBoolean("configured", true)
                    .putInt("delay", delay)
                    .putString("package", pkg)
                    .apply();

            hideSetupUI();
            circleContainer.setVisibility(FrameLayout.VISIBLE);

            new CountDownTimer(delay * 1000L, 100) {

                public void onTick(long millisUntilFinished) {
                    int progress = (int)((delay * 1000L - millisUntilFinished) * 100 / (delay * 1000L));
                    circleProgress.setProgress(progress);

                    int sec = (int)Math.ceil(millisUntilFinished / 1000.0);
                    circleText.setText(String.valueOf(sec));
                }

                public void onFinish() {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
                    if (intent != null) startActivity(intent);
                    finishAffinity();
                }

            }.start();
        });

        resetButton.setOnClickListener(v -> {
            prefs.edit().clear().commit();
            recreate();
        });
    }

    private void hideSetupUI() {
        delaySpinner.setVisibility(Spinner.GONE);
        appSpinner.setVisibility(Spinner.GONE);
        startButton.setVisibility(Button.GONE);
        resetButton.setVisibility(Button.GONE);
    }

    private void loadLaunchableApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_HOME);
        launchableApps = getPackageManager().queryIntentActivities(intent, 0);
    }

    private void setupSpinners() {

        String[] delays = {"10","15","20","25","30","35","40","45","50","55","60"};

        ArrayAdapter<String> delayAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_item, delays);
        delayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        delaySpinner.setAdapter(delayAdapter);
        delaySpinner.setSelection(2);

        List<String> names = new ArrayList<>();
        for (ResolveInfo app : launchableApps) {
            names.add(app.loadLabel(getPackageManager()).toString());
        }

        ArrayAdapter<String> appAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_item, names);
        appAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        appSpinner.setAdapter(appAdapter);
    }
}
