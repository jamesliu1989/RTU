package com.nju.rtu.model;

public class AreaAlertParameter implements java.io.Serializable {

	// Fields

	private Integer id;
	private String areaNo;
	private Double tempMedMax = 0.0;
	private Double tempMedRate = 0.0;
	private Double tempMedDevAbs = 0.0;
	private Double tempEnvMax = 0.0;
	private Double tempEnvRate = 0.0;
	private Double tempEnvDevAbs = 0.0;
	private Double humidityMax = 0.0;
	private Double tempDevAbs = 0.0;
	private Double tempAvgDevAbs = 0.0;
	private Double minBatteryVol = 0.0;
	private Double minWirelessSig = 0.0;
	private Integer type;

	// Constructors

	/** default constructor */
	public AreaAlertParameter() {
	}

	/** minimal constructor */
	public AreaAlertParameter(String areaNo) {
		this.areaNo = areaNo;
	}

	/** full constructor */
	public AreaAlertParameter(String areaNo, Double tempMedMax,
			Double tempMedRate, Double tempMedDevAbs, Double tempEnvMax,
			Double tempEnvRate, Double tempEnvDevAbs, Double humidityMax,
			Double tempDevAbs, Double tempAvgDevAbs, Double minBatteryVol,
			Double minWirelessSig, Integer type) {
		super();
		this.areaNo = areaNo;
		this.tempMedMax = tempMedMax;
		this.tempMedRate = tempMedRate;
		this.tempMedDevAbs = tempMedDevAbs;
		this.tempEnvMax = tempEnvMax;
		this.tempEnvRate = tempEnvRate;
		this.tempEnvDevAbs = tempEnvDevAbs;
		this.humidityMax = humidityMax;
		this.tempDevAbs = tempDevAbs;
		this.tempAvgDevAbs = tempAvgDevAbs;
		this.minBatteryVol = minBatteryVol;
		this.minWirelessSig = minWirelessSig;
		this.type = type;
	}

	// Property accessors
	public String getAreaNo() {
		return this.areaNo;
	}

	public void setAreaNo(String areaNo) {
		this.areaNo = areaNo;
	}

	public Double getTempMedMax() {
		return this.tempMedMax;
	}

	public void setTempMedMax(Double tempMedMax) {
		this.tempMedMax = tempMedMax;
	}

	public Double getTempMedRate() {
		return this.tempMedRate;
	}

	public void setTempMedRate(Double tempMedRate) {
		this.tempMedRate = tempMedRate;
	}

	public Double getTempMedDevAbs() {
		return this.tempMedDevAbs;
	}

	public void setTempMedDevAbs(Double tempMedDevAbs) {
		this.tempMedDevAbs = tempMedDevAbs;
	}

	public Double getTempEnvMax() {
		return this.tempEnvMax;
	}

	public void setTempEnvMax(Double tempEnvMax) {
		this.tempEnvMax = tempEnvMax;
	}

	public Double getTempEnvRate() {
		return this.tempEnvRate;
	}

	public void setTempEnvRate(Double tempEnvRate) {
		this.tempEnvRate = tempEnvRate;
	}

	public Double getTempEnvDevAbs() {
		return this.tempEnvDevAbs;
	}

	public void setTempEnvDevAbs(Double tempEnvDevAbs) {
		this.tempEnvDevAbs = tempEnvDevAbs;
	}

	public Double getTempDevAbs() {
		return this.tempDevAbs;
	}

	public void setTempDevAbs(Double tempDevAbs) {
		this.tempDevAbs = tempDevAbs;
	}

	public Double getTempAvgDevAbs() {
		return this.tempAvgDevAbs;
	}

	public void setTempAvgDevAbs(Double tempAvgDevAbs) {
		this.tempAvgDevAbs = tempAvgDevAbs;
	}

	public Double getMinBatteryVol() {
		return this.minBatteryVol;
	}

	public void setMinBatteryVol(Double minBatteryVol) {
		this.minBatteryVol = minBatteryVol;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	public Double getMinWirelessSig() {
		return minWirelessSig;
	}

	public void setMinWirelessSig(Double minWirelessSig) {
		this.minWirelessSig = minWirelessSig;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Double getHumidityMax() {
		return humidityMax;
	}
	
	public void setHumidityMax(Double humidityMax) {
		this.humidityMax = humidityMax;
	}
	

}