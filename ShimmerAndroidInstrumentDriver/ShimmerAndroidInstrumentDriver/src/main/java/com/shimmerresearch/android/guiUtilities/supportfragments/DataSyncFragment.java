package com.shimmerresearch.android.guiUtilities.supportfragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import com.shimmerresearch.androidinstrumentdriver.R;


public class DataSyncFragment extends Fragment {

    static Context context;
    public static EditText editTextParticipantName;
    public static EditText editTextTrialName;
    public static TextView TextViewPayloadIndex;
    public static TextView TextViewSpeed;
    public static TextView TextViewDirectory;

    public DataSyncFragment() {
        // Required empty public constructor
    }

    public static DataSyncFragment newInstance() {
        DataSyncFragment fragment = new DataSyncFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.data_sync, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        editTextParticipantName = (EditText) getView().findViewById(R.id.participantName);
        editTextTrialName = (EditText) getView().findViewById(R.id.trialName);
        TextViewPayloadIndex = (TextView) getView().findViewById(R.id.payloadIndex);
        TextViewSpeed = (TextView) getView().findViewById(R.id.speed);
        TextViewDirectory = (TextView) getView().findViewById(R.id.directory);

        super.onActivityCreated(savedInstanceState);
    }
}