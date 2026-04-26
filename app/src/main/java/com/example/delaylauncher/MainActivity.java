package com.example.delaylauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Spinner preApp1Spinner;
    private Spinner preApp2Spinner;
    private Spinner delaySpinner;
    private Spinner launcherSpinner;
    private Button startButton;

    private final List<AppEntry> apps=
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

        // only changed block
        populateApps();

        startButton.setOnClickListener(v->{
            /* still inert during countdown isolation */
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

        PackageManager pm=
            getPackageManager();

        Intent launchIntent=
            new Intent(
                Intent.ACTION_MAIN,
                null
            );

        launchIntent.addCategory(
            Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> resolved=
            pm.queryIntentActivities(
                launchIntent,
                0
            );

        for(ResolveInfo ri:resolved){

            String pkg=
                ri.activityInfo.packageName;

            String label=
                ri.loadLabel(pm).toString();

            if(label.trim().isEmpty())
                continue;

            apps.add(
                new AppEntry(
                    label,
                    pkg
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
