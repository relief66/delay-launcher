package com.example.delaylauncher;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.*;

public class MainActivity extends Activity {

private static final String PREFS="DelayPrefs";

Spinner preApp1Spinner,preApp2Spinner;
Spinner delaySpinner,appSpinner;
Button startButton;

FrameLayout circleContainer;
ProgressBar circleProgress;
TextView circleText;

List<ResolveInfo> launchers;
List<ResolveInfo> preApps;

CountDownTimer timer;

@Override
protected void onCreate(Bundle b){
super.onCreate(b);

setContentView(R.layout.activity_main);

preApp1Spinner=findViewById(R.id.preApp1Spinner);
preApp2Spinner=findViewById(R.id.preApp2Spinner);
delaySpinner=findViewById(R.id.delaySpinner);
appSpinner=findViewById(R.id.appSpinner);
startButton=findViewById(R.id.startButton);

circleContainer=findViewById(R.id.circleContainer);
circleProgress=findViewById(R.id.circleProgress);
circleText=findViewById(R.id.circleText);

loadApps();
setupSpinners();

startButton.setOnClickListener(v->{

int delay=Integer.parseInt(
delaySpinner.getSelectedItem().toString()
);

String pkg=null;

if(delay!=0){
pkg=launchers.get(
appSpinner.getSelectedItemPosition()
).activityInfo.packageName;
}

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

private void loadApps(){

launchers=new ArrayList<>();
preApps=new ArrayList<>();

PackageManager pm=getPackageManager();

Intent home=new Intent(
Intent.ACTION_MAIN,null
);

home.addCategory(
Intent.CATEGORY_HOME
);

for(ResolveInfo r:
pm.queryIntentActivities(home,0)){

if(!r.activityInfo.packageName.equals(
getPackageName()
)){
launchers.add(r);
}

}

Intent all=new Intent(
Intent.ACTION_MAIN,null
);

all.addCategory(
Intent.CATEGORY_LAUNCHER
);

for(ResolveInfo r:
pm.queryIntentActivities(all,0)){

boolean isLauncher=false;

for(ResolveInfo h:launchers){
if(h.activityInfo.packageName.equals(
r.activityInfo.packageName
)){
isLauncher=true;
break;
}
}

if(!isLauncher &&
!r.activityInfo.packageName.equals(
getPackageName()
)){
preApps.add(r);
}

}

}

private void setupSpinners(){

String[] delays={
"0","5","10","15","20","25",
"30","35","40","45","50","55","60"
};

ArrayAdapter<String>d=
new ArrayAdapter<>(
this,
R.layout.spinner_item,
delays
);

d.setDropDownViewResource(
R.layout.spinner_dropdown
);

delaySpinner.setAdapter(d);
delaySpinner.setSelection(2);

delaySpinner.setOnItemSelectedListener(
new AdapterView.OnItemSelectedListener(){

public void onItemSelected(
AdapterView<?> p,
View v,
int pos,
long id){

appSpinner.setVisibility(
pos==0?View.GONE:View.VISIBLE
);

}

public void onNothingSelected(
AdapterView<?> p){}
});

List<String> lNames=
new ArrayList<>();

for(ResolveInfo r:launchers){
lNames.add(
r.loadLabel(
getPackageManager()
).toString()
);
}

ArrayAdapter<String>la=
new ArrayAdapter<>(
this,
R.layout.spinner_item,
lNames
);

la.setDropDownViewResource(
R.layout.spinner_dropdown
);

appSpinner.setAdapter(la);

List<String> pNames=
new ArrayList<>();

pNames.add("NONE");

for(ResolveInfo r:preApps){
pNames.add(
r.loadLabel(
getPackageManager()
).toString()
);
}

ArrayAdapter<String>pa=
new ArrayAdapter<>(
this,
R.layout.spinner_item,
pNames
);

pa.setDropDownViewResource(
R.layout.spinner_dropdown
);

preApp1Spinner.setAdapter(pa);
preApp2Spinner.setAdapter(pa);

}

private void launchOptional(
Spinner s,
Set<String> launched){

int pos=s.getSelectedItemPosition();

if(pos<=0) return;

String pkg=
preApps.get(pos-1)
.activityInfo.packageName;

if(launched.contains(pkg)) return;

launched.add(pkg);

Intent i=
getPackageManager()
.getLaunchIntentForPackage(pkg);

if(i!=null){
i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
startActivity(i);
}

}

private void startCountdown(
int d,
String pkg
){

hideSetup();

circleContainer.setVisibility(
View.VISIBLE
);

timer=
new CountDownTimer(
Math.max(d,1)*1000L,
100
){

public void onTick(long ms){

circleText.setText(
String.valueOf(
(int)Math.ceil(ms/1000.0)
));

circleProgress.setProgress(
(int)(
((Math.max(d,1)*1000L-ms)*100)
/
(Math.max(d,1)*1000L)
)
);

}

public void onFinish(){

Set<String> launched=
new HashSet<>();

launchOptional(
preApp1Spinner,
launched
);

launchOptional(
preApp2Spinner,
launched
);

if(d==0){
finishAffinity();
return;
}

new Handler(
Looper.getMainLooper()
).postDelayed(()->{

Intent i=
getPackageManager()
.getLaunchIntentForPackage(pkg);

if(i!=null){
startActivity(i);
}

finishAffinity();

},1500);

}

}.start();

}

private void hideSetup(){

preApp1Spinner.setVisibility(View.GONE);
preApp2Spinner.setVisibility(View.GONE);
delaySpinner.setVisibility(View.GONE);
appSpinner.setVisibility(View.GONE);
startButton.setVisibility(View.GONE);

}

private void showSetup(){

preApp1Spinner.setVisibility(View.VISIBLE);
preApp2Spinner.setVisibility(View.VISIBLE);
delaySpinner.setVisibility(View.VISIBLE);
startButton.setVisibility(View.VISIBLE);

if(delaySpinner.getSelectedItemPosition()!=0){
appSpinner.setVisibility(View.VISIBLE);
}

circleContainer.setVisibility(View.GONE);

}

}
