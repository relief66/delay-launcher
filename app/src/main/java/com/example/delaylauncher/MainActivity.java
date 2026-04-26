package com.example.delaylauncher;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Spinner preApp1Spinner;
    private Spinner preApp2Spinner;
    private Spinner delaySpinner;
    private Spinner launcherSpinner;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        preApp1Spinner=findViewById(R.id.preApp1Spinner);
        preApp2Spinner=findViewById(R.id.preApp2Spinner);
        delaySpinner=findViewById(R.id.delaySpinner);
        launcherSpinner=findViewById(R.id.launcherSpinner);
        startButton=findViewById(R.id.startButton);

        populateDelaySpinner();
        populateAppsSafe();

        startButton.setOnClickListener(v->{
            /* intentionally inert for diagnostic */
        });
    }

    private void populateDelaySpinner(){

        List<String> delays=
                new ArrayList<>();

        for(int i=0;i<=60;i+=5){
            delays.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter=
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        delays
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        delaySpinner.setAdapter(adapter);
    }

    private void populateAppsSafe(){

        List<String> labels=
                new ArrayList<>();

        labels.add("Nessuno");

        ArrayAdapter<String> adapter=
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        labels
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        preApp1Spinner.setAdapter(adapter);
        preApp2Spinner.setAdapter(adapter);
        launcherSpinner.setAdapter(adapter);
    }
}package com.example.delaylauncher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Spinner preApp1Spinner;
    private Spinner preApp2Spinner;
    private Spinner delaySpinner;
    private Spinner launcherSpinner;

    private Button startButton;
    private View countdownOverlay;
    private ProgressBar circularProgress;
    private TextView countdownNumber;
    private TextView countdownLabel;

    private CountDownTimer countdownTimer;
    private ToneGenerator tone;

    private boolean launchTriggered=false;

    private final List<AppEntry> apps=new ArrayList<>();

    static class AppEntry{
        String label;
        String pkg;

        AppEntry(String label,String pkg){
            this.label=label;
            this.pkg=pkg;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preApp1Spinner=findViewById(R.id.preApp1Spinner);
        preApp2Spinner=findViewById(R.id.preApp2Spinner);
        delaySpinner=findViewById(R.id.delaySpinner);
        launcherSpinner=findViewById(R.id.launcherSpinner);

        startButton=findViewById(R.id.startButton);

        countdownOverlay=findViewById(R.id.countdownOverlay);
        circularProgress=findViewById(R.id.circularProgress);

        countdownNumber=findViewById(R.id.countdownNumber);
        countdownLabel=findViewById(R.id.countdownLabel);

        tone=new ToneGenerator(
                AudioManager.STREAM_NOTIFICATION,
                50
        );

        populateDelaySpinner();
        populateApps();

        startButton.setOnClickListener(v->startFlow());
    }

    private void populateDelaySpinner(){

        List<String> delays=new ArrayList<>();

        for(int i=0;i<=60;i+=5){
            delays.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter=
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                delays
            );

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );

        delaySpinner.setAdapter(adapter);
    }

    private void populateApps(){

        apps.clear();

        PackageManager pm=getPackageManager();

        Set<String> blockedPackages=
            new HashSet<>(
                Arrays.asList(
                    "com.android.settings",
                    "com.android.packageinstaller",
                    "com.google.android.packageinstaller"
                )
            );

        Intent homeIntent=
            new Intent(Intent.ACTION_MAIN);

        homeIntent.addCategory(
            Intent.CATEGORY_HOME
        );

        /* diagnostic isolation:
           resolveActivity branch removed */

        List<ApplicationInfo> installedApps=
            pm.getInstalledApplications(0);

        for(ApplicationInfo app:installedApps){

            if((app.flags &
                ApplicationInfo.FLAG_SYSTEM)!=0)
                continue;

            if(blockedPackages.contains(app.packageName))
                continue;

            if(pm.getLaunchIntentForPackage(
                app.packageName)==null)
                continue;

            String label=
                pm.getApplicationLabel(app)
                    .toString();

            if(label.trim().isEmpty())
                continue;

            apps.add(
                new AppEntry(
                    label,
                    app.packageName
                )
            );
        }

        Collections.sort(
            apps,
            Comparator.comparing(
                a->a.label.toLowerCase()
            )
        );

        List<String> labels=
            new ArrayList<>();

        labels.add("Nessuno");

        for(AppEntry app:apps){
            labels.add(app.label);
        }

        ArrayAdapter<String> adapter=
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
            );

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );

        preApp1Spinner.setAdapter(adapter);
        preApp2Spinner.setAdapter(adapter);
        launcherSpinner.setAdapter(adapter);
    }

    private void startFlow(){

        if(launchTriggered)
            return;

        int delay=Integer.parseInt(
            delaySpinner
                .getSelectedItem()
                .toString()
        );

        if(delay==0){
            launchSelectedApps();
            return;
        }

        countdownOverlay.setVisibility(
            View.VISIBLE
        );

        circularProgress.setProgress(0);

        countdownLabel.setText("sec");

        countdownTimer=
            new CountDownTimer(
                delay*1000L,
                100
            ){

                @Override
                public void onTick(long msRemaining){

                    int total=delay*1000;

                    int progress=
                        (int)(
                          (total-msRemaining)
                            *100/total
                        );

                    circularProgress.setProgress(
                        Math.min(100,progress)
                    );

                    countdownNumber.setText(
                        String.valueOf(
                          (msRemaining+999)/1000
                        )
                    );
                }

                @Override
                public void onFinish(){

                    circularProgress.setProgress(100);

                    countdownOverlay.setVisibility(
                        View.GONE
                    );

                    tone.startTone(
                      ToneGenerator.TONE_PROP_BEEP
                    );

                    launchSelectedApps();
                }

            }.start();
    }

    private void launchSelectedApps(){

        if(launchTriggered)
            return;

        launchTriggered=true;

        launchFromSpinner(preApp1Spinner);

        if(preApp2Spinner.getSelectedItemPosition()>0 &&
           preApp2Spinner.getSelectedItemPosition()!=
           preApp1Spinner.getSelectedItemPosition()){

            launchFromSpinner(preApp2Spinner);
        }

        launchFromSpinner(launcherSpinner);
    }

    private void launchFromSpinner(
        Spinner spinner
    ){

        int pos=
            spinner.getSelectedItemPosition();

        if(pos<=0)
            return;

        Intent launchIntent=
            getPackageManager()
              .getLaunchIntentForPackage(
                 apps.get(pos-1).pkg
              );

        if(launchIntent!=null){
            startActivity(launchIntent);
        }
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();

        if(countdownTimer!=null)
            countdownTimer.cancel();

        if(tone!=null)
            tone.release();
    }
}
