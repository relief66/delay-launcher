package com.example.delaylauncher;

import android.app.Activity;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.os.*;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

private Spinner delaySpinner;
private Spinner appSpinner;
private Button startButton;

private FrameLayout rootLayout;
private FrameLayout circleContainer;
private ProgressBar circleProgress;
private TextView circleText;

private List<ResolveInfo> launchableApps;

private CountDownTimer timer;

static final String PREFS="DelayPrefs";

@Override
protected void onCreate(Bundle b){
super.onCreate(b);

setContentView(R.layout.activity_main);

rootLayout=findViewById(R.id.rootLayout);
delaySpinner=findViewById(R.id.delaySpinner);
appSpinner=findViewById(R.id.appSpinner);
startButton=findViewById(R.id.startButton);

circleContainer=findViewById(R.id.circleContainer);
circleProgress=findViewById(R.id.circleProgress);
circleText=findViewById(R.id.circleText);

loadLaunchers();
setupSpinners();

SharedPreferences p=
getSharedPreferences(PREFS,MODE_PRIVATE);

if(p.getBoolean("configured",false)){

startCountdown(
p.getInt("delay",20),
p.getString("package",null)
);

}

startButton.setOnClickListener(v->{

int delay=Integer.parseInt(
delaySpinner.getSelectedItem().toString()
);

String pkg=
launchableApps.get(
appSpinner.getSelectedItemPosition()
).activityInfo.packageName;

p.edit()
.putBoolean("configured",true)
.putInt("delay",delay)
.putString("package",pkg)
.apply();

startCountdown(delay,pkg);

});

rootLayout.setOnTouchListener(
(v,e)->{

if(e.getAction()==MotionEvent.ACTION_DOWN){

if(timer!=null) timer.cancel();

showSetup();

}

return true;
});

}

private void startCountdown(
int delay,
String pkg
){

hideSetup();

circleContainer.setVisibility(View.VISIBLE);

timer=
new CountDownTimer(delay*1000L,100){

public void onTick(long ms){

int sec=
(int)Math.ceil(ms/1000.0);

circleText.setText(
String.valueOf(sec)
);

int p=
(int)(
(delay*1000L-ms)
*100
/(delay*1000L)
);

circleProgress.setProgress(p);

}

public void onFinish(){

Intent i=
getPackageManager()
.getLaunchIntentForPackage(pkg);

if(i!=null){

i.addFlags(
Intent.FLAG_ACTIVITY_NEW_TASK
|Intent.FLAG_ACTIVITY_CLEAR_TASK
);

startActivity(i);

}

finishAffinity();
Process.killProcess(
Process.myPid()
);

}

}.start();

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
new Intent(
Intent.ACTION_MAIN,
null
);

i.addCategory(
Intent.CATEGORY_HOME
);

launchableApps=
getPackageManager()
.queryIntentActivities(i,0);

}

private void setupSpinners(){

String[] delays={
"10","15","20","25",
"30","35","40","45",
"50","55","60"
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
