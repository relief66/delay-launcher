package com.delaylauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Spinner launcherSpinner;
    private Spinner delaySpinner;
    private Spinner preApp1Spinner;
    private Spinner preApp2Spinner;
    private Button startButton;

    private View countdownOverlay;
    private ProgressBar countdownCircle;
    private TextView countdownText;

    private final List<String> launcherLabels = new ArrayList<>();
    private final List<String> launcherPackages = new ArrayList<>();

    private final List<String> preAppLabels = new ArrayList<>();
    private final List<String> preAppPackages = new ArrayList<>();

    private int delaySeconds = 10;
    private boolean launchGuard = false;

    private ToneGenerator tone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tone = new ToneGenerator(AudioManager.STREAM_MUSIC,80);

        launcherSpinner = findViewById(R.id.launcherSpinner);
        delaySpinner = findViewById(R.id.delaySpinner);
        preApp1Spinner = findViewById(R.id.preApp1Spinner);
        preApp2Spinner = findViewById(R.id.preApp2Spinner);
        startButton = findViewById(R.id.startButton);

        countdownOverlay = findViewById(R.id.countdownOverlay);
        countdownCircle = findViewById(R.id.countdownCircle);
        countdownText = findViewById(R.id.countdownText);

        loadLaunchers();
        loadPreApps();
        initDelaySpinner();

        startButton.setOnClickListener(v -> startSequence());
    }

    private void loadLaunchers() {

        PackageManager pm = getPackageManager();

        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        for (ApplicationInfo ai : apps) {

            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue;
            }

            Intent launchIntent =
                    pm.getLaunchIntentForPackage(ai.packageName);

            if (launchIntent != null) {

                launcherLabels.add(
                        pm.getApplicationLabel(ai).toString()
                );

                launcherPackages.add(ai.packageName);
            }
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        launcherLabels
                );

        adapter.setDropDownViewResource(
                R.layout.spinner_dropdown
        );

        launcherSpinner.setAdapter(adapter);
    }


    private void loadPreApps() {

        preAppLabels.add("None");
        preAppPackages.add("");

        PackageManager pm = getPackageManager();

        List<ApplicationInfo> apps =
                pm.getInstalledApplications(0);

        for (ApplicationInfo ai : apps) {

            if (isUserNonLauncherApp(ai)) {

                preAppLabels.add(
                        pm.getApplicationLabel(ai).toString()
                );

                preAppPackages.add(
                        ai.packageName
                );
            }
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        preAppLabels
                );

        adapter.setDropDownViewResource(
                R.layout.spinner_dropdown
        );

        preApp1Spinner.setAdapter(adapter);
        preApp2Spinner.setAdapter(adapter);
    }


    private boolean isUserNonLauncherApp(ApplicationInfo ai){

        if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            return false;

        if ((ai.flags &
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
            return false;

        if (ai.packageName.equals(getPackageName()))
            return false;

        Intent launchIntent =
                getPackageManager()
                        .getLaunchIntentForPackage(
                                ai.packageName
                        );

        return launchIntent == null;
    }


    private void initDelaySpinner(){

        String[] delays={
                "0","5","10","15","20","30","45","60"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        delays
                );

        adapter.setDropDownViewResource(
                R.layout.spinner_dropdown
        );

        delaySpinner.setAdapter(adapter);

        delaySpinner.setSelection(2);

        delaySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        delaySeconds =
                                Integer.parseInt(
                                        delays[position]
                                );
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ){}
                }
        );
    }


    private void startSequence(){

        launchGuard=false;

        String p1=
                preAppPackages.get(
                        preApp1Spinner
                                .getSelectedItemPosition()
                );

        String p2=
                preAppPackages.get(
                        preApp2Spinner
                                .getSelectedItemPosition()
                );

        if(!p1.isEmpty() && p1.equals(p2)){
            Toast.makeText(
                    this,
                    "Pre-launch apps must be different",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        launchPreApp(p1);
        launchPreApp(p2);

        if(delaySeconds==0){
            launchSelectedLauncher();
            return;
        }

        runCountdown();
    }


    private void launchPreApp(String pkg){

        if(pkg==null || pkg.isEmpty())
            return;

        Intent i=
                getPackageManager()
                        .getLaunchIntentForPackage(pkg);

        if(i!=null){
            startActivity(i);
        }
    }


    private void runCountdown(){

        countdownOverlay.setVisibility(View.VISIBLE);

        pulse();

        new CountDownTimer(
                delaySeconds*1000L,
                1000
        ){

            @Override
            public void onTick(long millisUntilFinished){

                int sec=
                        (int)Math.ceil(
                                millisUntilFinished/1000.0
                        );

                countdownText.setText(
                        String.valueOf(sec)
                );

                tone.startTone(
                        ToneGenerator.TONE_PROP_BEEP,
                        180
                );

                pulse();
            }

            @Override
            public void onFinish(){

                countdownText.setText("0");

                countdownOverlay.setVisibility(
                        View.GONE
                );

                launchSelectedLauncher();

                finish();
            }

        }.start();
    }


    private void pulse(){

        ScaleAnimation anim=
                new ScaleAnimation(
                        1f,1.08f,
                        1f,1.08f,
                        Animation.RELATIVE_TO_SELF,.5f,
                        Animation.RELATIVE_TO_SELF,.5f
                );

        anim.setDuration(400);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(1);

        countdownCircle.startAnimation(anim);
    }


    private void launchSelectedLauncher(){

        if(launchGuard)
            return;

        launchGuard=true;

        String pkg=
                launcherPackages.get(
                        launcherSpinner
                                .getSelectedItemPosition()
                );

        Intent i=
                getPackageManager()
                        .getLaunchIntentForPackage(pkg);

        if(i!=null){
            startActivity(i);
        }
    }

}
