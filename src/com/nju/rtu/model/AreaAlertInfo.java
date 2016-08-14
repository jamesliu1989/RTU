package com.nju.rtu.model;

import java.sql.Timestamp;

public class AreaAlertInfo implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4239363223729760357L;
	
	public static String TEMP_MED_NULL = "介质温度采集异常";           //TYPE1
	public static String TEMP_ENV_NULL = "环境温度采集异常";           //TYPE1
	public static String TEMP_MED_MAX = "介质温度超上限";             //TYPE2
	public static String TEMP_MED_RATE = "介质温升速率超上限";         //TYPE3
	public static String TEMP_MED_DEV_ABS = "介质温度极均差值超上限";   //TYPE4
	public static String TEMP_ENV_MAX = "环境温度超上限";            //TYPE5
	public static String TEMP_ENV_RATE = "环境温升速率超上限";         //TYPE6
	public static String TEMP_ENV_DEV_ABS = "环境温度极均差值超上限";    //TYPE7
	public static String TEMP_DEV_ABS = "单支温度差超上限";            //TYPE8
	public static String TEMP_AVG_DEV_ABS = "温度均差超上限";         //TYPE9
	public static String HUMUDITY_MAX = "湿度超上限";                //TYPE10
	public static String MIN_BATTERY_VOL = "电压低于下限";           //TYPE11
	public static String MIN_WIRELESS_SIG = "无线信号低于下限";       //TYPE12
	public static String HUMUDITY_NULL = "湿度采集异常";       //TYPE13
	
	public static int ALERT_PROPERTY_PRE_1 = 1;
	public static int ALERT_PROPERTY_2 = 2;
    
 // Fields
	private Integer id;
	private String areaNo;
	private String nodeNo;
	private Integer alertProperty;
	private String alertType;
	private Double alertValue;
	private String alertMeasurement;
	private Timestamp alertTime;
	private String alertRemarks;
	private boolean isRead;
    private String type;
	// Constructors

	/** default constructor */
	public AreaAlertInfo() {
	}

	/** minimal constructor */
	public AreaAlertInfo(String areaNo, String nodeNo, Integer alertProperty,
			String alertType, Double alertValue, Timestamp alertTime) {
		this.areaNo = areaNo;
		this.nodeNo = nodeNo;
		this.alertProperty = alertProperty;
		this.alertType = alertType;
		this.alertValue = alertValue;
		this.alertTime = alertTime;
	}

	/** full constructor */
	public AreaAlertInfo(String areaNo, String nodeNo, Integer alertProperty,
			String alertType, Double alertValue, String alertMeasurement,
			String alertRemarks, String type) {
		this.areaNo = areaNo;
		this.nodeNo = nodeNo;
		this.alertProperty = alertProperty;
		this.alertType = alertType;
		this.alertValue = alertValue;
		this.alertMeasurement = alertMeasurement;
		this.alertRemarks = alertRemarks;
		this.type = type;
	}

	// Property accessors
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAreaNo() {
		return this.areaNo;
	}

	public void setAreaNo(String areaNo) {
		this.areaNo = areaNo;
	}

	public String getNodeNo() {
		return this.nodeNo;
	}

	public void setNodeNo(String nodeNo) {
		this.nodeNo = nodeNo;
	}

	public Integer getAlertProperty() {
		return this.alertProperty;
	}

	public void setAlertProperty(Integer alertProperty) {
		this.alertProperty = alertProperty;
	}

	public String getAlertType() {
		return this.alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public Double getAlertValue() {
		return this.alertValue;
	}

	public void setAlertValue(Double alertValue) {
		this.alertValue = alertValue;
	}

	public String getAlertMeasurement() {
		return this.alertMeasurement;
	}

	public void setAlertMeasurement(String alertMeasurement) {
		this.alertMeasurement = alertMeasurement;
	}

	public Timestamp getAlertTime() {
		return this.alertTime;
	}

	public void setAlertTime(Timestamp alertTime) {
		this.alertTime = alertTime;
	}
	public String getAlertRemarks() {
		return this.alertRemarks;
	}

	public void setAlertRemarks(String alertRemarks) {
		this.alertRemarks = alertRemarks;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	

}
