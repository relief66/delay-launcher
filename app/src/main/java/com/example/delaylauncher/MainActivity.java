package com.example.delaylauncher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
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

    private CountDownTimer countdownTimer;

    private boolean launchTriggered=false;
    private int lastTickAnnounced=-1;

    static class AppEntry{
        String label;
        String pkg;
        AppEntry(String l,String p){
            label=l;
            pkg=p;
        }
    }

    private final List<AppEntry> allPreApps=
            new ArrayList<>();

    private final List<AppEntry> filteredPreApps2=
            new ArrayList<>();

    private final List<AppEntry> launcherApps=
            new ArrayList<>();


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

        countdownOverlay.setOnClickListener(v->{
            if(countdownOverlay.getVisibility()!=View.VISIBLE)
                return;

            if(countdownTimer!=null)
                countdownTimer.cancel();

            countdownOverlay.setVisibility(View.GONE);
            circularProgress.setProgress(0);

            launchTriggered=false;
            lastTickAnnounced=-1;
        });

        populateDelaySpinner();
        populateLaunchers();
        populatePreApps();

        preApp1Spinner.setOnItemSelectedListener(
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
                ){
                    repopulatePreApp2(position);
                }

                @Override
                public void onNothingSelected(
                    AdapterView<?> parent
                ){}
            }
        );

        startButton.setOnClickListener(
            v -> startFlow()
        );
    }


    private void populateDelaySpinner(){

        List<String> delays=
                new ArrayList<>();

        for(int i=0;i<=60;i+=5){
            delays.add(String.valueOf(i));
        }

        ArrayAdapter<String> a=
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                delays
            );

        a.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );

        delaySpinner.setAdapter(a);
        delaySpinner.setSelection(4);
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
            pm.queryIntentActivities(homeIntent,0)){

            String pkg=
                ri.activityInfo.packageName;

            if(pkg.equals(getPackageName()))
                continue;

            String label=
                ri.loadLabel(pm).toString();

            if(label.trim().isEmpty())
                continue;

            launcherApps.add(
                new AppEntry(label,pkg)
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

        ArrayAdapter<String> a=
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
            );

        a.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );

        launcherSpinner.setAdapter(a);
        launcherSpinner.setSelection(
            quickIndex+1
        );
    }


    private void populatePreApps(){

        allPreApps.clear();

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

        List<ApplicationInfo> installed=
            pm.getInstalledApplications(0);

        for(ApplicationInfo app:installed){

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

            allPreApps.add(
                new AppEntry(
                    label,
                    app.packageName
                )
            );
        }

        Collections.sort(
            allPreApps,
            Comparator.comparing(
                a->a.label.toLowerCase()
            )
        );

        List<String> labels=
            new ArrayList<>();

        labels.add("Nessuno");

        for(AppEntry e:allPreApps){
            labels.add(e.label);
        }

        ArrayAdapter<String> a=
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
            );

        a.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );

        preApp1Spinner.setAdapter(a);

        repopulatePreApp2(0);
    }


    private void repopulatePreApp2(
        int pre1Selection
    ){

        filteredPreApps2.clear();

        for(int i=0;i<allPreApps.size();i++){

            if((i+1)==pre1Selection)
                continue;

            filteredPreApps2.add(
                allPreApps.get(i)
            );
        }

        List<String> labels=
            new ArrayList<>();

        labels.add("Nessuno");

        for(AppEntry e:filteredPreApps2){
            labels.add(e.label);
        }

        ArrayAdapter<String> a=
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
            );

        a.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );

        preApp2Spinner.setAdapter(a);
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

        countdownOverlay.setVisibility(
            View.VISIBLE
        );

        countdownTimer=
            new CountDownTimer(
                delay*1000L,
                100
            ){

            public void onTick(long ms){

                int sec=
                    (int)((ms+999)/1000);

                countdownNumber.setText(
                    String.valueOf(sec)
                );

                int total=
                    delay*1000;

                circularProgress.setProgress(
                    (int)((total-ms)*100/total)
                );

                if(sec<=3 &&
                   sec>=1 &&
                   sec!=lastTickAnnounced){

                    lastTickAnnounced=sec;
                    playTick();
                }
            }

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

        launchFromSpinner(
            preApp1Spinner,
            allPreApps
        );

        launchFromSpinner(
            preApp2Spinner,
            filteredPreApps2
        );

        launchLauncher();
    }


    private void launchFromSpinner(
        Spinner s,
        List<AppEntry> src
    ){
        int pos=
            s.getSelectedItemPosition();

        if(pos<=0)
            return;

        Intent i=
            getPackageManager()
             .getLaunchIntentForPackage(
                src.get(pos-1).pkg
             );

        if(i!=null)
            startActivity(i);
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

        if(i!=null)
            startActivity(i);
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(countdownTimer!=null)
            countdownTimer.cancel();
    }

}
