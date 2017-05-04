package com.shimmerresearch.shimmergrpcclientexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.grpc.ShimmerStreamClientGrpc;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShimmerStreamClientGrpc hwc;
        hwc = new ShimmerStreamClientGrpc("192.168.0.107",50051);
        ObjectCluster ojc = new ObjectCluster();
        ojc.setShimmerName("From Client to Server");
        hwc.sendOJCToServer(ojc.buildProtoBufMsg());
    }
}
