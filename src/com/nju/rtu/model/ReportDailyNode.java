package com.nju.rtu.model;




/**
 * ReportDailyNode entity. @author MyEclipse Persistence Tools
 */

public class ReportDailyNode implements java.io.Serializable {

	// Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String nodeNo;
	private Double tempMedMax;
	private Double tempMedMin;
	private Double tempMedAvg;
	private Double tempEnvMax;
	private Double tempEnvMin;
	private Double tempEnvAvg;
	private Double tempDevAbs;
	private Double humidityMax;
	private Double humidityMin;
	private Double humidityAvg;
	private Integer smogAlert;
	private Double batteryVol;
	private Integer wirelessSig;
	private Integer status;
	private Integer alert;
	private String dayOfYear;
	private String monthOfYear;

	// Constructors

	/** default constructor */
	public ReportDailyNode() {
	}

	/** minimal constructor */
	public ReportDailyNode(String nodeNo, String dayOfYear, String monthYear) {
		this.nodeNo = nodeNo;
		this.dayOfYear = dayOfYear;
		this.monthOfYear = monthYear;
	}

	/** full constructor */
	public ReportDailyNode(String nodeNo, Double tempMedMax, Double tempMedMin,
			Double tempMedAvg, Double tempEnvMax, Double tempEnvMin,
			Double tempEnvAvg, Double tempDevAbs, Double humidityMax,
			Double humidityMin, Double humidityAvg, Integer smogAlert,
			Double batteryVol, Integer wirelessSig, Integer status,
			Integer alert, String dayOfYear, String monthYear) {
		this.nodeNo = nodeNo;
		this.tempMedMax = tempMedMax;
		this.tempMedMin = tempMedMin;
		this.tempMedAvg = tempMedAvg;
		this.tempEnvMax = tempEnvMax;
		this.tempEnvMin = tempEnvMin;
		this.tempEnvAvg = tempEnvAvg;
		this.tempDevAbs = tempDevAbs;
		this.humidityMax = humidityMax;
		this.humidityMin = humidityMin;
		this.humidityAvg = humidityAvg;
		this.smogAlert = smogAlert;
		this.batteryVol = batteryVol;
		this.wirelessSig = wirelessSig;
		this.status = status;
		this.alert = alert;
		this.dayOfYear = dayOfYear;
		this.monthOfYear = monthYear;
	}

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	public String getNodeNo() {
		return this.nodeNo;
	}

	public void setNodeNo(String nodeNo) {
		this.nodeNo = nodeNo;
	}

	public Double getTempMedMax() {
		return this.tempMedMax;
	}

	public void setTempMedMax(Double tempMedMax) {
		this.tempMedMax = tempMedMax;
	}

	public Double getTempMedMin() {
		return this.tempMedMin;
	}

	public void setTempMedMin(Double tempMedMin) {
		this.tempMedMin = tempMedMin;
	}

	public Double getTempMedAvg() {
		return this.tempMedAvg;
	}

	public void setTempMedAvg(Double tempMedAvg) {
		this.tempMedAvg = tempMedAvg;
	}

	public Double getTempEnvMax() {
		return this.tempEnvMax;
	}

	public void setTempEnvMax(Double tempEnvMax) {
		this.tempEnvMax = tempEnvMax;
	}

	public Double getTempEnvMin() {
		return this.tempEnvMin;
	}

	public void setTempEnvMin(Double tempEnvMin) {
		this.tempEnvMin = tempEnvMin;
	}

	public Double getTempEnvAvg() {
		return this.tempEnvAvg;
	}

	public void setTempEnvAvg(Double tempEnvAvg) {
		this.tempEnvAvg = tempEnvAvg;
	}

	public Double getTempDevAbs() {
		return this.tempDevAbs;
	}

	public void setTempDevAbs(Double tempDevAbs) {
		this.tempDevAbs = tempDevAbs;
	}

	public Double getHumidityMax() {
		return humidityMax;
	}

	public void setHumidityMax(Double humidityMax) {
		this.humidityMax = humidityMax;
	}

	public Double getHumidityMin() {
		return humidityMin;
	}

	public void setHumidityMin(Double humidityMin) {
		this.humidityMin = humidityMin;
	}

	public Double getHumidityAvg() {
		return humidityAvg;
	}

	public void setHumidityAvg(Double humidityAvg) {
		this.humidityAvg = humidityAvg;
	}

	public Integer getSmogAlert() {
		return this.smogAlert;
	}

	public void setSmogAlert(Integer smogAlert) {
		this.smogAlert = smogAlert;
	}

	public Double getBatteryVol() {
		return this.batteryVol;
	}

	public void setBatteryVol(Double batteryVol) {
		this.batteryVol = batteryVol;
	}

	public Integer getWirelessSig() {
		return this.wirelessSig;
	}

	public void setWirelessSig(Integer wirelessSig) {
		this.wirelessSig = wirelessSig;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getAlert() {
		return this.alert;
	}

	public void setAlert(Integer alert) {
		this.alert = alert;
	}

	public String getDayOfYear() {
		return this.dayOfYear;
	}

	public void setDayOfYear(String dayOfYear) {
		this.dayOfYear = dayOfYear;
	}

	public String getMonthOfYear() {
		return this.monthOfYear;
	}

	public void setMonthOfYear(String monthOfYear) {
		this.monthOfYear = monthOfYear;
	}

}
