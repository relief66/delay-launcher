package com.example.delaylauncher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

    private final List<AppEntry> apps =
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

        populateDelaySpinner();

        // REINTRODUCED TEST BLOCK
        populateApps();

        startButton.setOnClickListener(v->{
            // still inert intentionally
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

        // still keep resolveActivity disabled
        // to isolate package scan itself

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
}
