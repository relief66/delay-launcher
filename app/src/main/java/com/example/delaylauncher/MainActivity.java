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

        loadLaunchableApps();
        setupSpinners();

        // 🚀 AVVIO AUTOMATICO
        if (isConfigured) {

            int savedDelay = prefs.getInt("delay", 20);
            String savedPackage = prefs.getString("package", null);

            hideSetupUI();
            circleContainer.setVisibility(FrameLayout.VISIBLE);
            startBreathingAnimation(circleContainer);

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

        // 👇 PRIMA CONFIGURAZIONE
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
            startBreathingAnimation(circleContainer);

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

    private void startBreathingAnimation(FrameLayout circleContainer) {

        circleContainer.setScaleX(0.95f);
        circleContainer.setScaleY(0.95f);

        android.animation.ObjectAnimator scaleX =
                android.animation.ObjectAnimator.ofFloat(circleContainer, "scaleX", 0.95f, 1.05f);

        android.animation.ObjectAnimator scaleY =
                android.animation.ObjectAnimator.ofFloat(circleContainer, "scaleY", 0.95f, 1.05f);

        scaleX.setDuration(900);
        scaleY.setDuration(900);

        scaleX.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        scaleY.setRepeatMode(android.animation.ValueAnimator.REVERSE);

        scaleX.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        scaleY.setRepeatCount(android.animation.ValueAnimator.INFINITE);

        scaleX.start();
        scaleY.start();
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
