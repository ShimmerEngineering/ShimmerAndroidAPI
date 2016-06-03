package com.shimmerresearch.multishimmertemplate;

public class SelectedSensors {

	private String nameSensor;
	private boolean selected;

	public SelectedSensors(){
		super();
	}
	
	public SelectedSensors(String nameSensor, boolean selected) {
		super();
		this.nameSensor = nameSensor;
		this.selected = selected;
	}

	public String getNameSensor() {
		return nameSensor;
	}

	public void setNameSensor(String nameSensor) {
		this.nameSensor = nameSensor;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
