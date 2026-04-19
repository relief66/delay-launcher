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

    private Spinner delaySpinner, appSpinner;
    private Button startButton, resetButton;
    private List<ResolveInfo> launchableApps;
    private static final String PREFS = "DelayPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean isConfigured = prefs.getBoolean("configured", false);

        // 🚀 AVVIO SILENZIOSO
        if (isConfigured) {

            int savedDelay = prefs.getInt("delay", 20);
            String savedPackage = prefs.getString("package", null);

            if (savedPackage != null) {

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(savedPackage);
                    if (intent != null) startActivity(intent);
                    finish();
                }, Math.max(1000, savedDelay * 1000L));

            } else {
                // fallback sicurezza
                prefs.edit().clear().commit();
                recreate();
            }

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
💥 STATO

👉 ora hai:

✔ comportamento esattamente come volevi
✔ robusto (no blocchi)
✔ UX pulita
✔ pronto per test reale
🚀 ADESSO

👉 puoi fare:

commit
build
install su TBox
Quando provi:

dimmi:

👉 “test finale fatto”

e chiudiamo con:

👉 icona definitiva (ultimo punto rimasto) 💥
