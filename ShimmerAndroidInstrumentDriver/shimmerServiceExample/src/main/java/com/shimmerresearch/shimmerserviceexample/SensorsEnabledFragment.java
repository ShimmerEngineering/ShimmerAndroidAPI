package com.shimmerresearch.shimmerserviceexample;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

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
    ShimmerDevice cloneDevice;
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
    public void setShimmerDevice(final ShimmerDevice device, final Context activityContext) {
        //sDevice = device;
        //context = activityContext;
        //buildSensorsEnabled(device);

        cloneDevice = device.deepClone();

        final List<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        Map<Integer, SensorDetails> sensorMap = device.getSensorMap();
        int count = 0;
        for (SensorDetails sd : sensorMap.values()) {
            if (device.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
                count++;
            }

        }
        String[] arraySensors = new String[count];
        final boolean[] listEnabled = new boolean[count];
        final int[] sensorKeys = new int[count];
        count = 0;

        for (int key : sensorMap.keySet()) {
            SensorDetails sd = sensorMap.get(key);
            if (device.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
                arraySensors[count] = sd.mSensorDetailsRef.mGuiFriendlyLabel;
                listEnabled[count] = sd.isEnabled();
                sensorKeys[count] = key;
                count++;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activityContext, android.R.layout.simple_list_item_multiple_choice, arraySensors);
        setListAdapter(adapter);

        ListView listView = getListView();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        //Create button in the ListView footer
        Button button = new Button(activityContext);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Toast.makeText(activityContext, "Writing config to Shimmer...", Toast.LENGTH_SHORT).show();
                ShimmerDevice shimmerDeviceClone = device.deepClone();
                if(device == null) { Toast.makeText(activityContext, "Error! ShimmerDevice is null!", Toast.LENGTH_SHORT).show(); }
                if(shimmerDeviceClone != null) {
                    for (int selected : mSelectedItems) {
                        shimmerDeviceClone.setSensorEnabledState((int) sensorKeys[selected], listEnabled[selected]);
                    }

                    List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                    cloneList.add(0, shimmerDeviceClone);
                    AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);

                    if (device instanceof Shimmer) {
                        //((Shimmer)device).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                            /*try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                        ((Shimmer) device).writeEnabledSensors(shimmerDeviceClone.getEnabledSensors());

                    } else if (device instanceof Shimmer4Android) {
                        //((Shimmer4Android)device).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                    }
                }
                else {
                    Toast.makeText(activityContext, "Error! shimmerDeviceClone is null!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        button.setText("Write config");
        button.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT, ListView.LayoutParams.WRAP_CONTENT));
        listView.addFooterView(button);

        //Set sensors which are already enabled in the Shimmer to be checked in the ListView
        for(int i=0; i<count; i++) {
            View v = getViewByPosition(i, listView);
            CheckedTextView cTextView = (CheckedTextView) v.findViewById(android.R.id.text1);
            if(listEnabled[i]) {
                if(cTextView != null) {
                    listView.setItemChecked(i, true);
                }
                else {
                    Log.e("SHIMMER", "CheckedTextView is null!");
                }
            }
        }

        //Set the listener for ListView item clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                cloneDevice.getEnabledSensors();
                List<SensorDetails> enabledSensorsList = cloneDevice.getListOfEnabledSensors();
                CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
                CharSequence cs = checkedTextView.getText();
                String sensorName = cs.toString();

                for(SensorDetails sd : enabledSensorsList) {
                    if(sensorName == sd.toString()) {
                        Log.e("JOS", "Sensor you clicked is already enabled!");
                    }
                }



                if(mSelectedItems.contains(position)) {
                    mSelectedItems.remove(Integer.valueOf(position));
                } else {
                    mSelectedItems.add(position);
                }
                if(listEnabled[position]) {
                    listEnabled[position] = false;
                } else {
                    listEnabled[position] = true;
                }

            }
        });

    }

//    private void buildSensorsEnabled(final ShimmerDevice shimmerDevice) {
//
//        final List<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        Map<Integer, SensorDetails> sensorMap = shimmerDevice.getSensorMap();
//        int count = 0;
//        for (SensorDetails sd : sensorMap.values()) {
//            if (shimmerDevice.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
//                count++;
//            }
//
//        }
//        String[] arraySensors = new String[count];
//        final boolean[] listEnabled = new boolean[count];
//        final int[] sensorKeys = new int[count];
//        count = 0;
//
//        for (int key : sensorMap.keySet()) {
//            SensorDetails sd = sensorMap.get(key);
//            if (shimmerDevice.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
//                arraySensors[count] = sd.mSensorDetailsRef.mGuiFriendlyLabel;
//                listEnabled[count] = sd.isEnabled();
//                sensorKeys[count] = key;
//                count++;
//            }
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, arraySensors);
//        setListAdapter(adapter);
//
//        ListView listView = getListView();
//        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
//
//        //Create button in the ListView footer
//        Button button = new Button(context);
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                Toast.makeText(context, "Writing config to Shimmer...", Toast.LENGTH_SHORT).show();
//                ShimmerDevice shimmerDeviceClone = shimmerDevice.deepClone();
//                if(shimmerDevice == null) { Toast.makeText(context, "Error! ShimmerDevice is null!", Toast.LENGTH_SHORT).show(); }
//                if(shimmerDeviceClone != null) {
//                    for (int selected : mSelectedItems) {
//                        shimmerDeviceClone.setSensorEnabledState((int) sensorKeys[selected], listEnabled[selected]);
//                    }
//
//
////                    for(int i=0; i<mSelectedItems.size(); i++) {
////                        shimmerDeviceClone.setSensorEnabledState((int) sensorKeys[mSelectedItems.get(i)], listEnabled[mSelectedItems.get(i)]);
////                    }
//
//
//                    List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
//                    cloneList.add(0, shimmerDeviceClone);
//                    AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);
//
////                    final boolean[] listEnabled3 = listEnabled2(shimmerDeviceClone);
////                    final boolean[] listEnabled4 = listEnabled3;
//
//
//                    if (shimmerDevice instanceof Shimmer) {
//                        //((Shimmer)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
//                            /*try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }*/
//                        ((Shimmer) shimmerDevice).writeEnabledSensors(shimmerDeviceClone.getEnabledSensors());
//                    } else if (shimmerDevice instanceof Shimmer4Android) {
//                        //((Shimmer4Android)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
//                    }
//                }
//                else {
//                    Toast.makeText(context, "Error! shimmerDeviceClone is null!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        button.setText("Write config");
//        //button.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, ListView.LayoutParams.FILL_PARENT));
//        listView.addFooterView(button);
//
//        //Set sensors which are already enabled in the Shimmer to be checked in the ListView
//        for(int i=0; i<count; i++) {
//            View v = getViewByPosition(i, listView);
//            CheckedTextView cTextView = (CheckedTextView) v.findViewById(android.R.id.text1);
//            if(listEnabled[i]) {
//                if(cTextView != null) {
//                    listView.setItemChecked(i, true);
//                }
//                else {
//                    Log.e("SHIMMER", "CheckedTextView is null!");
//                }
//            }
//        }
//
//        //Set the listener for ListView item clicks
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if(mSelectedItems.contains(position)) {
//                    mSelectedItems.remove(Integer.valueOf(position));
//                } else {
//                    mSelectedItems.add(position);
//                }
//                if(listEnabled[position] == true) {
//                    listEnabled[position] = false;
//                } else {
//                    listEnabled[position] = true;
//                }
//            }
//        });
//    }


    /**
     * Method to get the View from a position in the ListView, taking into account the constantly
     * changing index of the ListView as it is scrolled.
     * @param pos
     * @param listView
     * @return
     */
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
