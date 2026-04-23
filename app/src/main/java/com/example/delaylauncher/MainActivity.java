package com.example.delaylauncher;

import android.app.Activity;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.os.*;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import java.util.*;

public class MainActivity extends Activity {

private static final String PREFS="DelayPrefs";

Spinner delaySpinner;
Spinner appSpinner;
Spinner preApp1Spinner;
Spinner preApp2Spinner;
Button startButton;

FrameLayout circleContainer;
ProgressBar circleProgress;
TextView circleText;

List<ResolveInfo> launchers;
CountDownTimer timer;

@Override
protected void onCreate(Bundle b){
super.onCreate(b);

setContentView(R.layout.activity_main);

delaySpinner=findViewById(R.id.delaySpinner);
appSpinner=findViewById(R.id.appSpinner);
preApp1Spinner=findViewById(R.id.preApp1Spinner);
preApp2Spinner=findViewById(R.id.preApp2Spinner);
startButton=findViewById(R.id.startButton);

circleContainer=findViewById(R.id.circleContainer);
circleProgress=findViewById(R.id.circleProgress);
circleText=findViewById(R.id.circleText);

circleContainer.setVisibility(View.GONE);

loadLaunchers();
setupSpinners();

SharedPreferences p=
getSharedPreferences(PREFS,MODE_PRIVATE);

if(p.getBoolean("configured",false)){

String savedPkg=
p.getString("package",null);

if(savedPkg!=null){
startCountdown(
p.getInt("delay",20),
savedPkg
);
}

}

startButton.setOnClickListener(v->{

int delay=
Integer.parseInt(
delaySpinner.getSelectedItem().toString()
);

String pkg=
launchers.get(
appSpinner.getSelectedItemPosition()
).activityInfo.packageName;

p.edit()
.putBoolean("configured",true)
.putInt("delay",delay)
.putString("package",pkg)
.apply();

launchOptional(preApp1Spinner);
launchOptional(preApp2Spinner);

startCountdown(delay,pkg);

});

circleContainer.setOnTouchListener((v,e)->{
if(e.getAction()==MotionEvent.ACTION_DOWN){
if(timer!=null) timer.cancel();
showSetup();
}
return true;
});

}

private void launchOptional(Spinner s){

int pos=s.getSelectedItemPosition();

if(pos<=0) return;

String pkg=
launchers.get(pos-1)
.activityInfo.packageName;

Intent i=
getPackageManager()
.getLaunchIntentForPackage(pkg);

if(i!=null) startActivity(i);

}

private void startCountdown(int d,String pkg){

hideSetup();

circleContainer.setVisibility(View.VISIBLE);

if(timer!=null) timer.cancel();

timer=
new CountDownTimer(d*1000L,100){

public void onTick(long ms){

int sec=
(int)Math.ceil(ms/1000.0);

circleText.setText(
String.valueOf(sec)
);

int p=(int)(
((d*1000L-ms)*100)/(d*1000L)
);

circleProgress.setProgress(p);

}

public void onFinish(){

Intent i=
getPackageManager()
.getLaunchIntentForPackage(pkg);

if(i!=null){
i.addFlags(
Intent.FLAG_ACTIVITY_NEW_TASK |
Intent.FLAG_ACTIVITY_CLEAR_TASK
);
startActivity(i);
}

finishAffinity();

android.os.Process.killProcess(
android.os.Process.myPid()
);

}

}.start();

}

private void hideSetup(){

delaySpinner.setVisibility(View.GONE);
appSpinner.setVisibility(View.GONE);
preApp1Spinner.setVisibility(View.GONE);
preApp2Spinner.setVisibility(View.GONE);
startButton.setVisibility(View.GONE);

}

private void showSetup(){

circleContainer.setVisibility(View.GONE);

delaySpinner.setVisibility(View.VISIBLE);
appSpinner.setVisibility(View.VISIBLE);
preApp1Spinner.setVisibility(View.VISIBLE);
preApp2Spinner.setVisibility(View.VISIBLE);
startButton.setVisibility(View.VISIBLE);

}

private void loadLaunchers(){

Intent i=
new Intent(Intent.ACTION_MAIN,null);

i.addCategory(Intent.CATEGORY_HOME);

List<ResolveInfo> raw=
getPackageManager()
.queryIntentActivities(i,0);

launchers=new ArrayList<>();

String mine=getPackageName();

for(ResolveInfo r:raw){
if(!r.activityInfo.packageName.equals(mine)){
launchers.add(r);
}
}

}

private void setupSpinners(){

String[] delays={
"10","15","20","25","30",
"35","40","45","50","55","60"
};

delaySpinner.setAdapter(
new ArrayAdapter<>(
this,
android.R.layout.simple_spinner_dropdown_item,
delays
)
);

delaySpinner.setSelection(2);

List<String> names=
new ArrayList<>();

for(ResolveInfo r:launchers){
names.add(
r.loadLabel(
getPackageManager()
).toString()
);
}

ArrayAdapter<String> a=
new ArrayAdapter<>(
this,
android.R.layout.simple_spinner_dropdown_item,
names
);

appSpinner.setAdapter(a);

List<String> optional=
new ArrayList<>();

optional.add("NONE");
optional.addAll(names);

ArrayAdapter<String> o=
new ArrayAdapter<>(
this,
android.R.layout.simple_spinner_dropdown_item,
optional
);

preApp1Spinner.setAdapter(o);
preApp2Spinner.setAdapter(o);

}

}
