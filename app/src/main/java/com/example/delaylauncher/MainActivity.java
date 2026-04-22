package com.example.delaylauncher;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {

Spinner delaySpinner,appSpinner;
Button startButton;
List<ResolveInfo> launchableApps;

@Override
protected void onCreate(Bundle savedInstanceState) {

super.onCreate(savedInstanceState);

try {

setContentView(R.layout.activity_main);

delaySpinner=findViewById(R.id.delaySpinner);
appSpinner=findViewById(R.id.appSpinner);
startButton=findViewById(R.id.startButton);

loadApps();
setupSpinners();

Toast.makeText(
this,
"SAFE MODE STARTED",
Toast.LENGTH_LONG
).show();

}
catch(Exception e){

TextView t=new TextView(this);
t.setText(e.toString());
t.setTextSize(20);
setContentView(t);

}
}

private void loadApps(){

Intent i=new Intent(Intent.ACTION_MAIN,null);
i.addCategory(Intent.CATEGORY_LAUNCHER);

launchableApps=
getPackageManager().queryIntentActivities(i,0);

}

private void setupSpinners(){

String[] delays={
"10","15","20","30","45","60"
};

ArrayAdapter<String> da=
new ArrayAdapter<>(
this,
android.R.layout.simple_spinner_item,
delays
);

da.setDropDownViewResource(
android.R.layout.simple_spinner_dropdown_item
);

delaySpinner.setAdapter(da);

List<String> names=new ArrayList<>();

for(ResolveInfo r:launchableApps){
names.add(
r.loadLabel(getPackageManager()).toString()
);
}

ArrayAdapter<String> aa=
new ArrayAdapter<>(
this,
android.R.layout.simple_spinner_item,
names
);

aa.setDropDownViewResource(
android.R.layout.simple_spinner_dropdown_item
);

appSpinner.setAdapter(aa);

}
}
