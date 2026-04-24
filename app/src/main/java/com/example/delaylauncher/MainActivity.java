package com.example.delaylauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private Spinner launcherSpinner,delaySpinner,pre1Spinner,pre2Spinner;
    private ProgressBar countdownCircle;
    private TextView countdownText;
    private View countdownOverlay;

    private ToneGenerator tone;

    private final List<String> launcherLabels=new ArrayList<>();
    private final List<String> launcherPkgs=new ArrayList<>();

    private final List<AppEntry> preApps=new ArrayList<>();

    private int delaySeconds=10;
    private boolean launchGuard=false;

    private static class AppEntry{
        String label;
        String pkg;

        AppEntry(String l,String p){
            label=l;
            pkg=p;
        }
    }

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        tone=new ToneGenerator(AudioManager.STREAM_MUSIC,50);

        launcherSpinner=findViewById(R.id.launcherSpinner);
        delaySpinner=findViewById(R.id.delaySpinner);
        pre1Spinner=findViewById(R.id.preApp1Spinner);
        pre2Spinner=findViewById(R.id.preApp2Spinner);

        countdownOverlay=findViewById(R.id.countdownOverlay);
        countdownCircle=findViewById(R.id.countdownCircle);
        countdownText=findViewById(R.id.countdownText);

        findViewById(R.id.startButton)
                .setOnClickListener(v->startSequence());

        loadLaunchers();
        loadPreApps();
        initDelaySpinner();
    }

    private void loadLaunchers(){

        PackageManager pm=getPackageManager();

        for(ApplicationInfo ai:pm.getInstalledApplications(0)){

            if((ai.flags & ApplicationInfo.FLAG_SYSTEM)!=0)
                continue;

            Intent li=pm.getLaunchIntentForPackage(ai.packageName);

            if(li!=null){
                launcherLabels.add(
                        pm.getApplicationLabel(ai).toString()
                );

                launcherPkgs.add(ai.packageName);
            }
        }

        ArrayAdapter<String> ad=
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        launcherLabels
                );

        ad.setDropDownViewResource(
                R.layout.spinner_dropdown
        );

        launcherSpinner.setAdapter(ad);
    }

    private boolean isAllowedPreApp(ApplicationInfo ai){

        if((ai.flags & ApplicationInfo.FLAG_SYSTEM)!=0)
            return false;

        String pkg=ai.packageName;

        if(pkg.equals(getPackageName()))
            return false;

        if(pkg.toLowerCase().contains("settings"))
            return false;

        if(pkg.toLowerCase().contains("packageinstaller"))
            return false;

        Intent home=new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> homes=
                getPackageManager()
                        .queryIntentActivities(home,0);

        for(ResolveInfo ri:homes){
            if(ri.activityInfo.packageName.equals(pkg))
                return false;
        }

        Intent launch=
                getPackageManager()
                        .getLaunchIntentForPackage(pkg);

        return launch!=null;
    }

    private void loadPreApps(){

        preApps.clear();

        PackageManager pm=getPackageManager();

        for(ApplicationInfo ai:pm.getInstalledApplications(0)){

            if(isAllowedPreApp(ai)){

                preApps.add(
                        new AppEntry(
                                pm.getApplicationLabel(ai).toString(),
                                ai.packageName
                        )
                );
            }
        }

        Collections.sort(
                preApps,
                Comparator.comparing(a -> a.label.toLowerCase())
        );

        List<String> labels=new ArrayList<>();
        labels.add("None");

        for(AppEntry e:preApps){
            labels.add(e.label);
        }

        ArrayAdapter<String> ad=
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        labels
                );

        ad.setDropDownViewResource(
                R.layout.spinner_dropdown
        );

        pre1Spinner.setAdapter(ad);
        pre2Spinner.setAdapter(ad);
    }

    private String getSelectedPrePkg(Spinner s){

        int pos=s.getSelectedItemPosition();

        if(pos<=0) return "";

        return preApps.get(pos-1).pkg;
    }

    private void initDelaySpinner(){

        String[] vals={
                "0","5","10","15","20","30","45","60"
        };

        ArrayAdapter<String> ad=
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        vals
                );

        ad.setDropDownViewResource(
                R.layout.spinner_dropdown
        );

        delaySpinner.setAdapter(ad);
        delaySpinner.setSelection(2);

        delaySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener(){

                    public void onItemSelected(
                            AdapterView<?> p,
                            View v,
                            int pos,
                            long id){

                        delaySeconds=
                                Integer.parseInt(vals[pos]);
                    }

                    public void onNothingSelected(
                            AdapterView<?> p){}
                });
    }

    private void startSequence(){

        launchGuard=false;

        String p1=getSelectedPrePkg(pre1Spinner);
        String p2=getSelectedPrePkg(pre2Spinner);

        if(!p1.isEmpty() && p1.equals(p2)){

            Toast.makeText(
                    this,
                    "Pre-launch apps must differ",
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

        if(pkg.isEmpty()) return;

        Intent i=
                getPackageManager()
                        .getLaunchIntentForPackage(pkg);

        if(i!=null)
            startActivity(i);
    }

    private void runCountdown(){

        countdownOverlay.setVisibility(View.VISIBLE);
        countdownCircle.setProgress(0);

        new CountDownTimer(
                delaySeconds*1000L,
                1000
        ){

            public void onTick(long ms){

                int sec=
                        (int)Math.ceil(ms/1000.0);

                countdownText.setText(
                        String.valueOf(sec)
                );

                int pct=
                        ((delaySeconds-sec)*100)
                                /delaySeconds;

                countdownCircle.setProgress(pct);

                tone.startTone(
                        ToneGenerator.TONE_PROP_BEEP,
                        180
                );

                pulse();
            }

            public void onFinish(){

                countdownText.setText("0");
                countdownCircle.setProgress(100);

                countdownOverlay.setVisibility(
                        View.GONE
                );

                launchSelectedLauncher();

                finish();
            }

        }.start();
    }

    private void pulse(){

        ScaleAnimation s=
                new ScaleAnimation(
                        1f,1.08f,
                        1f,1.08f,
                        Animation.RELATIVE_TO_SELF,.5f,
                        Animation.RELATIVE_TO_SELF,.5f
                );

        s.setDuration(400);
        s.setRepeatMode(Animation.REVERSE);
        s.setRepeatCount(1);

        countdownCircle.startAnimation(s);
    }

    private void launchSelectedLauncher(){

        if(launchGuard)
            return;

        if(launcherPkgs.isEmpty())
            return;

        launchGuard=true;

        Intent i=
                getPackageManager()
                        .getLaunchIntentForPackage(
                                launcherPkgs.get(
                                        launcherSpinner
                                                .getSelectedItemPosition()
                                )
                        );

        if(i!=null)
            startActivity(i);
    }
}
