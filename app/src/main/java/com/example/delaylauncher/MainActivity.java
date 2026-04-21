package com.example.delaylauncher;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.*;
import android.content.pm.*;
import android.media.MediaPlayer;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private Spinner delaySpinner, appSpinner;
    private Button startButton;
    private List<ResolveInfo> launchableApps;
    private static final String PREFS = "DelayPrefs";

    private ObjectAnimator scaleXAnim, scaleYAnim;
    private CountDownTimer timer;
    private MediaPlayer player;

    private FrameLayout circleContainer;
    private ProgressBar circleProgress;
    private TextView circleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean isConfigured = prefs.getBoolean("configured", false);

        setContentView(R.layout.activity_main);

        delaySpinner = findViewById(R.id.delaySpinner);
        appSpinner = findViewById(R.id.appSpinner);
        startButton = findViewById(R.id.startButton);

        circleContainer = findViewById(R.id.circleContainer);
        circleProgress = findViewById(R.id.circleProgress);
        circleText = findViewById(R.id.circleText);

        circleContainer.setVisibility(View.GONE);

        loadLaunchableApps();
        setupSpinners();

        // TAP = interrompe countdown
        circleContainer.setOnTouchListener((v, e) -> {
            stopAll();
            showSetupUI();
            return true;
        });

        if (isConfigured) {
            int savedDelay = prefs.getInt("delay", 20);
            String pkg = prefs.getString("package", null);
            startCountdown(savedDelay, pkg);
        }

        startButton.setOnClickListener(v -> {

            int delay = Integer.parseInt(delaySpinner.getSelectedItem().toString());
            String pkg = launchableApps.get(appSpinner.getSelectedItemPosition()).activityInfo.packageName;

            prefs.edit()
                    .putBoolean("configured", true)
                    .putInt("delay", delay)
                    .putString("package", pkg)
                    .apply();

            startCountdown(delay, pkg);
        });
    }

    private void startCountdown(int delay, String pkg) {

        hideSetupUI();
        circleContainer.setVisibility(View.VISIBLE);
        startBreathingAnimation();

        player = MediaPlayer.create(this, R.raw.tick);

        timer = new CountDownTimer(delay * 1000L, 1000) {

            public void onTick(long ms) {
                int sec = (int)Math.ceil(ms / 1000.0);
                circleText.setText(String.valueOf(sec));

                int progress = (int)((delay * 1000L - ms) * 100 / (delay * 1000L));
                circleProgress.setProgress(progress);

                if (player != null) {
                    try { player.start(); } catch (Exception ignored) {}
                }
            }

            public void onFinish() {

                stopAll();

                // 🔥 CHIUSURA COMPLETA + LANCIO PULITO
                new Handler(Looper.getMainLooper()).post(() -> {

                    Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
                    if (intent != null) {
                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        );
                        startActivity(intent);
                    }

                    finishAffinity();
                    System.exit(0); // elimina ogni residuo
                });
            }

        }.start();
    }

    private void stopAll() {
        if (timer != null) timer.cancel();

        if (player != null) {
            try { player.release(); } catch (Exception ignored) {}
            player = null;
        }

        if (scaleXAnim != null) scaleXAnim.cancel();
        if (scaleYAnim != null) scaleYAnim.cancel();
    }

    private void showSetupUI() {
        delaySpinner.setVisibility(View.VISIBLE);
        appSpinner.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.VISIBLE);
        circleContainer.setVisibility(View.GONE);
    }

    private void hideSetupUI() {
        delaySpinner.setVisibility(View.GONE);
        appSpinner.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
    }

    private void startBreathingAnimation() {

        scaleXAnim = ObjectAnimator.ofFloat(circleContainer, "scaleX", 0.95f, 1.05f);
        scaleYAnim = ObjectAnimator.ofFloat(circleContainer, "scaleY", 0.95f, 1.05f);

        scaleXAnim.setDuration(900);
        scaleYAnim.setDuration(900);

        scaleXAnim.setRepeatMode(ValueAnimator.REVERSE);
        scaleYAnim.setRepeatMode(ValueAnimator.REVERSE);

        scaleXAnim.setRepeatCount(ValueAnimator.INFINITE);
        scaleYAnim.setRepeatCount(ValueAnimator.INFINITE);

        scaleXAnim.start();
        scaleYAnim.start();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAll();
    }
}
