package com.nju.rtu.model;

import java.util.Date;

public class RegularData {
	private int ctrlerNo;
	private String nodeNo;
	private double tempMed;
	private double tempEnv;
	private double humidity;
	private int smogAlert;
	private double batteryVol;
	private int wirelessSig;
	private Date collectTime;
	
	
	public RegularData(){
		
	}

	public RegularData(int ctrlerNo, String nodeNo, double tempMed,
			double tempEnv, double humidity, int smogAlert, double batteryVol,
			int wirelessSig) {
		super();
		this.ctrlerNo = ctrlerNo;
		this.nodeNo = nodeNo;
		this.tempMed = tempMed;
		this.tempEnv = tempEnv;
		this.humidity = humidity;
		this.smogAlert = smogAlert;
		this.batteryVol = batteryVol;
		this.wirelessSig = wirelessSig;
	}

	public int getCtrlerNo() {
		return ctrlerNo;
	}
	public void setCtrlerNo(int ctrlerNo) {
		this.ctrlerNo = ctrlerNo;
	}
	public String getNodeNo() {
		return nodeNo;
	}
	public void setNodeNo(String nodeNo) {
		this.nodeNo = nodeNo;
	}
	public double getTempMed() {
		return tempMed;
	}
	public void setTempMed(double tempMed) {
		this.tempMed = tempMed;
	}
	public double getTempEnv() {
		return tempEnv;
	}
	public void setTempEnv(double tempEnv) {
		this.tempEnv = tempEnv;
	}
	public double getHumidity() {
		return humidity;
	}
	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}
	public int getSmogAlert() {
		return smogAlert;
	}
	public void setSmogAlert(int smogAlert) {
		this.smogAlert = smogAlert;
	}
	public double getBatteryVol() {
		return batteryVol;
	}
	public void setBatteryVol(double batteryVol) {
		this.batteryVol = batteryVol;
	}
	public int getWirelessSig() {
		return wirelessSig;
	}
	public void setWirelessSig(int wirelessSig) {
		this.wirelessSig = wirelessSig;
	}
	public Date getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(Date collectTime) {
		this.collectTime = collectTime;
	}
	
	
}
