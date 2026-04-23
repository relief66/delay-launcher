package com.example.delaylauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String PREFS="DelayPrefs";

    private Spinner delaySpinner;
    private Spinner appSpinner;
    private Button startButton;

    private FrameLayout circleContainer;
    private ProgressBar circleProgress;
    private TextView circleText;

    private List<ResolveInfo> launchableApps;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        delaySpinner=findViewById(R.id.delaySpinner);
        appSpinner=findViewById(R.id.appSpinner);
        startButton=findViewById(R.id.startButton);

        circleContainer=findViewById(R.id.circleContainer);
        circleProgress=findViewById(R.id.circleProgress);
        circleText=findViewById(R.id.circleText);

        circleContainer.setVisibility(View.GONE);

        loadLaunchers();
        setupSpinners();

        SharedPreferences prefs=
            getSharedPreferences(PREFS,MODE_PRIVATE);

        if(prefs.getBoolean("configured",false)){

            String pkg=prefs.getString("package",null);

            if(pkg!=null){
                startCountdown(
                    prefs.getInt("delay",20),
                    pkg
                );
            }
        }

        startButton.setOnClickListener(v->{

            int delay=Integer.parseInt(
                delaySpinner.getSelectedItem().toString()
            );

            String pkg=
                launchableApps
                .get(appSpinner.getSelectedItemPosition())
                .activityInfo.packageName;

            prefs.edit()
                .putBoolean("configured",true)
                .putInt("delay",delay)
                .putString("package",pkg)
                .apply();

            startCountdown(delay,pkg);
        });

        circleContainer.setOnTouchListener((v,e)->{

            if(e.getAction()== MotionEvent.ACTION_DOWN){
                cancelCountdown();
                showSetup();
            }

            return true;
        });
    }

    private void startCountdown(int delay,String pkg){

        hideSetup();

        circleContainer.setVisibility(View.VISIBLE);

        cancelCountdown();

        timer=
        new CountDownTimer(delay*1000L,100){

            public void onTick(long ms){

                int sec=(int)Math.ceil(ms/1000.0);

                circleText.setText(
                    String.valueOf(sec)
                );

                int progress=(int)(
                   ((delay*1000L)-ms)
                   *100
                   /(delay*1000L)
                );

                circleProgress.setProgress(progress);
            }

            public void onFinish(){

                Intent launch=
                   getPackageManager()
                   .getLaunchIntentForPackage(pkg);

                if(launch!=null){

                    launch.addFlags(
                      Intent.FLAG_ACTIVITY_NEW_TASK |
                      Intent.FLAG_ACTIVITY_CLEAR_TOP |
                      Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(launch);
                }

                finishAffinity();

                android.os.Process.killProcess(
                    android.os.Process.myPid()
                );
            }

        }.start();
    }

    private void cancelCountdown(){
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    private void hideSetup(){

        delaySpinner.setVisibility(View.GONE);
        appSpinner.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);

    }

    private void showSetup(){

        circleContainer.setVisibility(View.GONE);

        delaySpinner.setVisibility(View.VISIBLE);
        appSpinner.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.VISIBLE);

    }

    private void loadLaunchers(){

        Intent i=
            new Intent(Intent.ACTION_MAIN,null);

        i.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> raw=
            getPackageManager()
            .queryIntentActivities(i,0);

        launchableApps=new ArrayList<>();

        String myPkg=getPackageName();

        for(ResolveInfo r:raw){

            if(!r.activityInfo.packageName.equals(myPkg)){
                launchableApps.add(r);
            }
        }
    }

    private void setupSpinners(){

        String[] delays={
         "10","15","20","25","30",
         "35","40","45","50","55","60"
        };

        ArrayAdapter<String> d=
         new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            delays
         );

        d.setDropDownViewResource(
          android.R.layout.simple_spinner_dropdown_item
        );

        delaySpinner.setAdapter(d);
        delaySpinner.setSelection(2);

        List<String> names=
           new ArrayList<>();

        for(ResolveInfo r:launchableApps){

            names.add(
              r.loadLabel(
                 getPackageManager()
              ).toString()
            );
        }

        ArrayAdapter<String> a=
         new ArrayAdapter<>(
           this,
           android.R.layout.simple_spinner_item,
           names
         );

        a.setDropDownViewResource(
         android.R.layout.simple_spinner_dropdown_item
        );

        appSpinner.setAdapter(a);
    }
}
