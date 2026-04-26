package com.example.delaylauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

        populateDelaySpinner();
        populatePreApps();
        populateLaunchers();

        startButton.setOnClickListener(v->startFlow());
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

    private void populatePreApps(){

        preApps.clear();

        PackageManager pm=
            getPackageManager();

        Intent intent=
            new Intent(
                Intent.ACTION_MAIN,
                null
            );

        intent.addCategory(
            Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> results=
            pm.queryIntentActivities(
                intent,
                0
            );

        Set<String> blacklist=
            new HashSet<>();

        blacklist.add("com.example.delaylauncher");

        for(ResolveInfo home :
            pm.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN)
                    .addCategory(
                      Intent.CATEGORY_HOME
                    ),
                0
            )){
            blacklist.add(
                home.activityInfo.packageName
            );
        }

        for(ResolveInfo ri:results){

            String pkg=
                ri.activityInfo.packageName;

            String label=
                ri.loadLabel(pm).toString();

            if(blacklist.contains(pkg))
                continue;

            if(label.equalsIgnoreCase("File")) continue;
            if(label.equalsIgnoreCase("Google")) continue;
            if(label.equalsIgnoreCase("Impostazioni")) continue;
            if(label.equalsIgnoreCase("Fota Update")) continue;
            if(label.equalsIgnoreCase("LED")) continue;
            if(label.equalsIgnoreCase("Play Store")) continue;
            if(label.equalsIgnoreCase("Telefono BT")) continue;
            if(label.equalsIgnoreCase("Tutte le App")) continue;

            preApps.add(
                new AppEntry(label,pkg)
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

        for(AppEntry a:preApps){
            labels.add(a.label);
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

    private void populateLaunchers(){

        launcherApps.clear();

        PackageManager pm=
            getPackageManager();

        Intent homeIntent=
            new Intent(Intent.ACTION_MAIN);

        homeIntent.addCategory(
            Intent.CATEGORY_HOME
        );

        List<ResolveInfo> homes=
            pm.queryIntentActivities(
                homeIntent,
                0
            );

        for(ResolveInfo ri:homes){

            String pkg=
                ri.activityInfo.packageName;

            if(pkg.equals(
               "com.example.delaylauncher"))
                continue;

            String label=
                ri.loadLabel(pm).toString();

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

        for(AppEntry a:launcherApps){
            labels.add(a.label);
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

        countdownTimer=
            new CountDownTimer(
                delay*1000L,
                100
            ){

                @Override
                public void onTick(
                   long msRemaining
                ){

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

                    countdownOverlay.setVisibility(
                      View.GONE
                    );

                    // ToneGenerator intentionally excluded

                    launchSelectedApps();
                }

            }.start();
    }

    private void launchSelectedApps(){

        launchTriggered=true;

        launchFromPreSpinner(
            preApp1Spinner
        );

        if(preApp2Spinner
           .getSelectedItemPosition()>0 &&
           preApp2Spinner
           .getSelectedItemPosition()!=
           preApp1Spinner
           .getSelectedItemPosition()){

           launchFromPreSpinner(
             preApp2Spinner
           );
        }

        launchFromLauncherSpinner();
    }

    private void launchFromPreSpinner(
        Spinner spinner
    ){
        int pos=
            spinner.getSelectedItemPosition();

        if(pos<=0) return;

        Intent i=
            getPackageManager()
             .getLaunchIntentForPackage(
               preApps.get(pos-1).pkg
             );

        if(i!=null){
            startActivity(i);
        }
    }

    private void launchFromLauncherSpinner(){

        int pos=
            launcherSpinner
                .getSelectedItemPosition();

        if(pos<0) return;

        Intent i=
            getPackageManager()
             .getLaunchIntentForPackage(
               launcherApps.get(pos).pkg
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
