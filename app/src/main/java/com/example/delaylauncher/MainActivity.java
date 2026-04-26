package com.example.delaylauncher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
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

    private boolean launchTriggered=false;
    private int lastTickAnnounced=-1;

    private final List<AppEntry> preApps=
            new ArrayList<>();

    private final List<AppEntry> launcherApps=
            new ArrayList<>();

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

        countdownOverlay.setOnClickListener(v -> {

            if(countdownOverlay.getVisibility()!=View.VISIBLE)
                return;

            if(countdownTimer!=null){
                countdownTimer.cancel();
            }

            circularProgress.setProgress(0);

            countdownOverlay.setVisibility(
                    View.GONE
            );

            launchTriggered=false;
            lastTickAnnounced=-1;
        });

        populateDelaySpinner();
        populateLaunchers();
        populatePreApps();

        startButton.setOnClickListener(
                v->startFlow()
        );
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

        delaySpinner.setSelection(4); //20 sec
    }

    private void populateLaunchers(){

        launcherApps.clear();

        PackageManager pm=
                getPackageManager();

        Intent homeIntent=
                new Intent(Intent.ACTION_MAIN);

        homeIntent.addCategory(
                Intent.CATEGORY_HOME
        );

        for(ResolveInfo ri :
                pm.queryIntentActivities(
                        homeIntent,
                        0
                )){

            String pkg=
                    ri.activityInfo.packageName;

            if(pkg.equals(getPackageName()))
                continue;

            String label=
                    ri.loadLabel(pm).toString();

            if(label.trim().isEmpty())
                continue;

            launcherApps.add(
                    new AppEntry(
                            label,
                            pkg
                    )
            );
        }

        Collections.sort(
                launcherApps,
                Comparator.comparing(
                        a->a.label.toLowerCase()
                )
        );

        List<String> labels=
                new ArrayList<>();

        labels.add("Nessuno");

        int quickIndex=0;
        int idx=0;

        for(AppEntry e:launcherApps){

            labels.add(e.label);

            if(e.label.toLowerCase()
                    .contains("quickstep")){
                quickIndex=idx;
            }

            idx++;
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

        launcherSpinner.setAdapter(adapter);

        if(labels.size()>1){
            launcherSpinner.setSelection(
                    quickIndex+1
            );
        }
    }

    private void populatePreApps(){

        preApps.clear();

        PackageManager pm=
                getPackageManager();

        Set<String> blacklistPkgs=
                new HashSet<>();

        for(AppEntry e:launcherApps){
            blacklistPkgs.add(e.pkg);
        }

        blacklistPkgs.add(
                getPackageName()
        );

        Set<String> blacklistLabels=
                new HashSet<>(
                        Arrays.asList(
                                "File",
                                "Fota Update",
                                "Google",
                                "Impostazioni",
                                "LED",
                                "Play Store",
                                "Telefono BT",
                                "Tutte le App"
                        )
                );

        List<ApplicationInfo> installedApps=
                pm.getInstalledApplications(0);

        for(ApplicationInfo app:installedApps){

            if(blacklistPkgs.contains(
                    app.packageName))
                continue;

            if(pm.getLaunchIntentForPackage(
                    app.packageName)==null)
                continue;

            String label=
                    pm.getApplicationLabel(app)
                            .toString();

            if(label.trim().isEmpty())
                continue;

            if(blacklistLabels.contains(label))
                continue;

            preApps.add(
                    new AppEntry(
                            label,
                            app.packageName
                    )
            );
        }

        Collections.sort(
                preApps,
                Comparator.comparing(
                        a->a.label.toLowerCase()
                )
        );

        List<String> labels=
                new ArrayList<>();

        labels.add("Nessuno");

        for(AppEntry e:preApps){
            labels.add(e.label);
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
    }

    private void startFlow(){

        if(launchTriggered)
            return;

        int delay=
                Integer.parseInt(
                        delaySpinner
                                .getSelectedItem()
                                .toString()
                );

        if(delay==0){
            launchSelectedApps();
            return;
        }

        lastTickAnnounced=-1;

        countdownOverlay.setVisibility(
                View.VISIBLE
        );

        circularProgress.setProgress(0);

        countdownTimer=
                new CountDownTimer(
                        delay*1000L,
                        100
                ){

                    @Override
                    public void onTick(
                            long msRemaining
                    ){

                        int sec=
                                (int)((msRemaining+999)/1000);

                        countdownNumber.setText(
                                String.valueOf(sec)
                        );

                        int total=
                                delay*1000;

                        int progress=
                                (int)(
                                        (total-msRemaining)
                                                *100/total
                                );

                        circularProgress.setProgress(
                                Math.min(
                                        100,
                                        progress
                                )
                        );

                        if(sec<=3 &&
                                sec>=1 &&
                                sec!=lastTickAnnounced){

                            lastTickAnnounced=sec;
                            playTick();
                        }
                    }

                    @Override
                    public void onFinish(){

                        countdownOverlay.setVisibility(
                                View.GONE
                        );

                        playChime();

                        launchSelectedApps();
                    }

                }.start();
    }

    private void playTick(){

        MediaPlayer mp=
                MediaPlayer.create(
                        this,
                        R.raw.tick_soft
                );

        if(mp!=null){
            mp.setOnCompletionListener(
                    p->p.release()
            );
            mp.start();
        }
    }

    private void playChime(){

        MediaPlayer mp=
                MediaPlayer.create(
                        this,
                        R.raw.chime_soft
                );

        if(mp!=null){
            mp.setOnCompletionListener(
                    p->p.release()
            );
            mp.start();
        }
    }

    private void launchSelectedApps(){

        if(launchTriggered)
            return;

        launchTriggered=true;

        launchPre(preApp1Spinner);

        if(preApp2Spinner.getSelectedItemPosition()>0 &&
                preApp2Spinner.getSelectedItemPosition()!=
                        preApp1Spinner.getSelectedItemPosition()){

            launchPre(preApp2Spinner);
        }

        launchLauncher();
    }

    private void launchPre(
            Spinner spinner
    ){

        int pos=
                spinner.getSelectedItemPosition();

        if(pos<=0)
            return;

        Intent i=
                getPackageManager()
                        .getLaunchIntentForPackage(
                                preApps.get(pos-1).pkg
                        );

        if(i!=null){
            startActivity(i);
        }
    }

    private void launchLauncher(){

        int pos=
                launcherSpinner
                        .getSelectedItemPosition();

        if(pos<=0)
            return;

        Intent i=
                getPackageManager()
                        .getLaunchIntentForPackage(
                                launcherApps.get(pos-1).pkg
                        );

        if(i!=null){
            startActivity(i);
        }
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();

        if(countdownTimer!=null){
            countdownTimer.cancel();
        }
    }
}
