package com.example.delaylauncher;

import android.content.Intent;
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
import java.util.List;

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

        // SAFE BOOT diagnostic
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

    // DIAGNOSTIC VERSION
    // no package scan
    private void populateApps(){

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

    private void startFlow(){

        if(launchTriggered){
            return;
        }

        int delay=
                Integer.parseInt(
                        delaySpinner
                                .getSelectedItem()
                                .toString()
                );

        if(delay==0){
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
                    public void onTick(
                            long msRemaining
                    ){

                        int total=
                                delay*1000;

                        int progress=
                                (int)(
                                        (total-msRemaining)
                                                *100
                                                /total
                                );

                        circularProgress.setProgress(
                                Math.min(
                                        100,
                                        progress
                                )
                        );

                        countdownNumber.setText(
                                String.valueOf(
                                        (msRemaining+999)/1000
                                )
                        );
                    }

                    @Override
                    public void onFinish(){

                        circularProgress.setProgress(
                                100
                        );

                        countdownOverlay.setVisibility(
                                View.GONE
                        );

                        tone.startTone(
                                ToneGenerator
                                        .TONE_PROP_BEEP
                        );
                    }

                }.start();
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();

        if(countdownTimer!=null){
            countdownTimer.cancel();
        }

        if(tone!=null){
            tone.release();
        }
    }
}
