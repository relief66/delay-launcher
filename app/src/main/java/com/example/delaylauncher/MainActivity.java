package com.example.delaylauncher;

import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;
import android.os.CountDownTimer;
import android.net.Uri;
import android.widget.VideoView;
import android.media.ToneGenerator;
import android.media.AudioManager;

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

        // 🚀 AVVIO AUTOMATICO CON UI MINIMA
if (isConfigured) {

    setContentView(R.layout.activity_main);

    VideoView videoView = findViewById(R.id.videoView);
    TextView countdownText = findViewById(R.id.countdownText);
    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

    // nascondi UI
    findViewById(R.id.delaySpinner).setVisibility(View.GONE);
    findViewById(R.id.appSpinner).setVisibility(View.GONE);
    findViewById(R.id.startButton).setVisibility(View.GONE);
    findViewById(R.id.resetButton).setVisibility(View.GONE);
    findViewById(R.id.circleContainer).setVisibility(View.GONE);

    videoView.setVisibility(View.VISIBLE);
    countdownText.setVisibility(View.VISIBLE);

    int savedDelay = prefs.getInt("delay", 20);
    String savedPackage = prefs.getString("package", null);

    // avvia video
    Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.anim);
    videoView.setVideoURI(uri);
    videoView.start();

    final long totalTime = savedDelay * 1000L;

    new CountDownTimer(totalTime, 1000) {

        public void onTick(long millisUntilFinished) {
            int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
            countdownText.setText(String.valueOf(secondsLeft));
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
        }

        public void onFinish() {
            toneGenerator.release();
            if (savedPackage != null) {
                Intent intent = getPackageManager().getLaunchIntentForPackage(savedPackage);
                if (intent != null) startActivity(intent);
            }
            finish();
        }

    }.start();

    return;
}

        // 👇 PRIMA CONFIGURAZIONE
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

        // 👇 START
        startButton.setOnClickListener(v -> {

            int delay = Integer.parseInt(delaySpinner.getSelectedItem().toString());
            String packageName = launchableApps.get(appSpinner.getSelectedItemPosition()).activityInfo.packageName;

            prefs.edit()
                    .putBoolean("configured", true)
                    .putInt("delay", delay)
                    .putString("package", packageName)
                    .apply();

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

                    Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                    if (intent != null) startActivity(intent);

                    finish();
                }

            }.start();
        });

        // 👇 RESET
        resetButton.setOnClickListener(v -> {

            prefs.edit().clear().commit();

            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();
        });
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
