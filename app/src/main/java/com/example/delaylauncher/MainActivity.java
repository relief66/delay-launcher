package com.example.delaylauncher;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.*;
import android.view.View;
import android.widget.*;

import java.util.*;

public class MainActivity extends Activity {

Spinner preApp1Spinner,preApp2Spinner,delaySpinner,appSpinner;
Button startButton;
FrameLayout circleContainer;
ProgressBar circleProgress;
TextView circleText;

List<ResolveInfo> launchers;
List<ResolveInfo> preApps;

CountDownTimer timer;

ToneGenerator tone=
new ToneGenerator(
AudioManager.STREAM_NOTIFICATION,
50);

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

int d=Integer.parseInt(
delaySpinner.getSelectedItem().toString()
);

String pkg=null;

if(d!=0){
pkg=launchers.get(
appSpinner.getSelectedItemPosition()
).activityInfo.packageName;
}

startCountdown(d,pkg);

});

}

private void loadApps(){

PackageManager pm=getPackageManager();

launchers=new ArrayList<>();
preApps=new ArrayList<>();

Intent home=
new Intent(Intent.ACTION_MAIN,null);

home.addCategory(Intent.CATEGORY_HOME);

for(ResolveInfo r:
pm.queryIntentActivities(home,0)){

if(!r.activityInfo.packageName.equals(
getPackageName()
)){
launchers.add(r);
}

}

List<ApplicationInfo> apps=
pm.getInstalledApplications(0);

for(ApplicationInfo a:apps){

if((a.flags &
ApplicationInfo.FLAG_SYSTEM)!=0)
continue;

String pkg=a.packageName;

if(pkg.equals(getPackageName()))
continue;

if(pkg.toLowerCase().contains("settings"))
continue;

Intent li=
pm.getLaunchIntentForPackage(pkg);

if(li!=null){
ResolveInfo ri=
pm.resolveActivity(li,0);

if(ri!=null)
preApps.add(ri);
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

List<String> ln=new ArrayList<>();

for(ResolveInfo r:launchers)
ln.add(
r.loadLabel(
getPackageManager()
).toString()
);

ArrayAdapter<String> la=
new ArrayAdapter<>(
this,
R.layout.spinner_item,
ln
);

la.setDropDownViewResource(
R.layout.spinner_dropdown
);

appSpinner.setAdapter(la);

List<String> pn=
new ArrayList<>();

pn.add("NONE");

for(ResolveInfo r:preApps)
pn.add(
r.loadLabel(
getPackageManager()
).toString()
);

ArrayAdapter<String> pa=
new ArrayAdapter<>(
this,
R.layout.spinner_item,
pn
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

if(launched.contains(pkg))
return;

launched.add(pkg);

Intent i=
getPackageManager()
.getLaunchIntentForPackage(pkg);

if(i!=null)
startActivity(i);

}

private void startCountdown(
int d,
String pkg){

if(d==0){

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

finishAffinity();
return;

}

circleContainer.setVisibility(
View.VISIBLE
);

timer=
new CountDownTimer(
d*1000L,
1000){

public void onTick(long ms){

int sec=(int)(ms/1000);

circleText.setText(
String.valueOf(sec)
);

circleProgress.setProgress(
(d-sec)*100/d
);

tone.startTone(
ToneGenerator.TONE_PROP_BEEP2,
120
);

circleContainer.animate()
.scaleX(1.08f)
.scaleY(1.08f)
.setDuration(180)
.withEndAction(() ->
circleContainer.animate()
.scaleX(1f)
.scaleY(1f)
.setDuration(180));

}

public void onFinish(){

circleProgress.setProgress(100);
circleText.setText("0");

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

new Handler(
Looper.getMainLooper()
).postDelayed(()->{

Intent i=
getPackageManager()
.getLaunchIntentForPackage(pkg);

if(i!=null)
startActivity(i);

finishAffinity();

},1500);

}

}.start();

}

}
