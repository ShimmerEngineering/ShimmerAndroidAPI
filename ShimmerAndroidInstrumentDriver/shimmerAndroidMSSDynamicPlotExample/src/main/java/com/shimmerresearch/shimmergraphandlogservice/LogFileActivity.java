package com.shimmerresearch.shimmergraphandlogservice;

import com.shimmerresearch.shimmergraphandlogservice.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class LogFileActivity extends Activity {
	boolean mEnableLogging=false;
	CheckBox mCheckBox;
	Button mButtonCommitChanges;
	EditText mEdit;
	String mFileName;
	
	
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
    	setContentView(R.layout.logfile);
    	
    	mCheckBox = (CheckBox) findViewById(R.id.checkBox1);
    	mButtonCommitChanges= (Button) findViewById(R.id.button1);
    	mEdit   = (EditText)findViewById(R.id.editText1);

//    	mEdit.setText("Default"); //leave it as Default and the Service will name it automatically
    	
    	Bundle extras = getIntent().getExtras();
        mEnableLogging = extras.getBoolean("LogFileEnableLogging");
        mFileName = extras.getString("LogFileName");
        
        mCheckBox.setChecked(mEnableLogging);
        mEdit.setText(mFileName);
        
    	mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if (arg1==true){
					mEnableLogging=true;
				} else {
					mEnableLogging=false;
				}
			}
	});
    	
    	mButtonCommitChanges.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
		        intent.putExtra("LogFileEnableLogging", mEnableLogging);
		        // Set result and finish this Activity
		        intent.putExtra("LogFileName",mEdit.getText().toString());
		        setResult(Activity.RESULT_OK, intent);
		        finish();
			}
    		
    	});
    	
}
}
