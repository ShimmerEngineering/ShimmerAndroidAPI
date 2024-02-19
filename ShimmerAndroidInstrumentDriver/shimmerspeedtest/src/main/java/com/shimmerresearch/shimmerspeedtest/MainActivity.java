package com.shimmerresearch.shimmerspeedtest;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.view.Menu;
import android.view.MenuItem;

import com.clj.fastble.BleManager;
import com.shimmerresearch.androidradiodriver.Shimmer3BleAndroidRadioByteCommunication;
import com.shimmerresearch.androidradiodriver.Shimmer3RAndroidRadioByteCommunication;
import com.shimmerresearch.androidradiodriver.ShimmerSerialPortAndroid;
import com.shimmerresearch.androidradiodriver.VerisenseBleAndroidRadioByteCommunication;
import com.shimmerresearch.comms.SerialPortByteCommunication;
import com.shimmerresearch.comms.TestRadioSerialPort;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.shimmer3.communication.SpeedTestProtocol;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    SpeedTestProtocol protocol;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BleManager.getInstance().init(getApplication());
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //ShimmerSerialPortAndroid portspp = new ShimmerSerialPortAndroid("E8:EB:1B:71:3E:36",true);
        //TestRadioSerialPort portspp = new TestRadioSerialPort();
        Shimmer3BleAndroidRadioByteCommunication port = new Shimmer3BleAndroidRadioByteCommunication("E8:EB:1B:71:3E:36");
        //Shimmer3RAndroidRadioByteCommunication port = new Shimmer3RAndroidRadioByteCommunication("E8:EB:1B:71:3E:36");
        //SerialPortByteCommunication port = new SerialPortByteCommunication(portspp);
        protocol = new SpeedTestProtocol(port);
        try {
            protocol.connect();

            //portspp.rxBytes(100);

        } catch (ShimmerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        protocol.startSpeedTest();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}