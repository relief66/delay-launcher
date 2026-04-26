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
}
