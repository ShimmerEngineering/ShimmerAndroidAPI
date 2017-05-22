package com.shimmerresearch.shimmerserviceexample;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorsEnabledFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SensorsEnabledFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorsEnabledFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    Context context;
    ShimmerDevice shimmerDevice;
    ListView lv;


    public SensorsEnabledFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SensorsEnabledFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SensorsEnabledFragment newInstance(String param1, String param2) {
        SensorsEnabledFragment fragment = new SensorsEnabledFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_sensors_enabled_list, container, false);
//    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
/*
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Displays a default message when the fragment is first displayed
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] values = new String[] {"No Shimmer selected", "Sensors unavailable"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Call this method to display the sensors from the selected Shimmer Device
     * @param device
     * @param activityContext
     */
    public void setShimmerDevice(ShimmerDevice device, Context activityContext) {
        shimmerDevice = device;
        context = activityContext;
        buildSensorsEnabled();
    }

    private void buildSensorsEnabled() {

        final List<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Map<Integer, SensorDetails> sensorMap = shimmerDevice.getSensorMap();
        int count = 0;
        for (SensorDetails sd : sensorMap.values()) {
            if (shimmerDevice.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
                count++;
            }

        }
        String[] arraySensors = new String[count];
        final boolean[] listEnabled = new boolean[count];
        final int[] sensorKeys = new int[count];
        count = 0;

        for (int key : sensorMap.keySet()) {
            SensorDetails sd = sensorMap.get(key);
            if (shimmerDevice.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
                arraySensors[count] = sd.mSensorDetailsRef.mGuiFriendlyLabel;
                listEnabled[count] = sd.isEnabled();
                sensorKeys[count] = key;
                count++;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, arraySensors);
        setListAdapter(adapter);

        ListView listView = getListView();

        //Create button in the ListView footer to save changes made
        Button button = new Button(context);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Toast.makeText(context, "Writing config...", Toast.LENGTH_SHORT).show();

            }
        });
        button.setText("Write config");
        button.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT, ListView.LayoutParams.WRAP_CONTENT));
        listView.addFooterView(button);

//        View v = listView.findViewById(android.R.id.text1);
//        v.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
//                CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
//                if(checkedTextView.isChecked()) {
//                    checkedTextView.setChecked(false);
//                }
//                else {
//                    checkedTextView.setChecked(true);
//                }
//            }
//        });

//        //Set the sensors which are already enabled to be checked in the ListView
//        for (int i = 0; i < count; i++) {
//            View v = listView.getChildAt(i);
//            CheckedTextView checkedTextView = (CheckedTextView) v.findViewById(android.R.id.text1);
//
//            if (listEnabled[i] == true) {
//                if(checkedTextView != null) {
//                    checkedTextView.setChecked(true);
//                }
//            }
//        }
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        for(int i=0; i<count; i++) {
            View v = getViewByPosition(i, listView);
            CheckedTextView cTextView = (CheckedTextView) v.findViewById(android.R.id.text1);
            if(listEnabled[i]) {
                if(cTextView != null) {
                    cTextView.setChecked(true);
                    cTextView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
                    Log.e("JOS", "CheckedTextView has been checked!!!");
                    Log.e("JOS", "Text in CheckedTextView: " + cTextView.getText());
                    Log.e("JOS", "CheckedTextView isChecked: " + cTextView.isChecked());
                    listView.setItemChecked(i, true);
                }
                else {
                    Log.e("JOS", "CheckedTextView is null!!!");
                }
            }
        }
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }



}
