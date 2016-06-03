package com.shimmerresearch.adapters;

import java.util.List;

import com.shimmerresearch.multishimmertemplate.R;
import com.shimmerresearch.multishimmertemplate.SelectedSensors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


public class CheckboxListAdapter extends BaseAdapter implements OnClickListener {

	
	private LayoutInflater inflator; // The inflator used to inflate the XML layout
	private  List<SelectedSensors> listOfSensors;
//	private int row;

	public CheckboxListAdapter(LayoutInflater inflator, List<SelectedSensors> sensors) {
		super();
		this.inflator = inflator;
		listOfSensors = sensors;
//		this.row = position;
	}

	@Override
	public int getCount() {
		return listOfSensors.size();
	}

	@Override
	public Object getItem(int position) {
		return listOfSensors.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {

		// We only create the view if its needed
		if (view == null) {
			view = inflator.inflate(R.layout.plot_sensor_list, null);

			// Set the click listener for the checkbox
			view.findViewById(R.id.checkBoxSignal).setOnClickListener(this);
		}

		SelectedSensors data = (SelectedSensors) getItem(position);

		// Set the example text and the state of the checkbox
		CheckBox cb = (CheckBox) view.findViewById(R.id.checkBoxSignal);
		cb.setChecked(data.isSelected());
		// We tag the data object to retrieve it on the click listener.
		cb.setTag(data);

		TextView tv = (TextView) view.findViewById(R.id.textSignal);
		tv.setText(data.getNameSensor());

		return view;
	}

	@Override
	/** Will be called when a checkbox has been clicked. */
	public void onClick(View view) {
		SelectedSensors sensor = (SelectedSensors) view.getTag();
		sensor.setSelected(((CheckBox) view).isChecked());
	}

}
