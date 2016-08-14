package com.nju.rtu.model;

import java.sql.Timestamp;

public class AreaAlertInfo implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4239363223729760357L;
	
	public static String TEMP_MED_NULL = "�����¶Ȳɼ��쳣";           //TYPE1
	public static String TEMP_ENV_NULL = "�����¶Ȳɼ��쳣";           //TYPE1
	public static String TEMP_MED_MAX = "�����¶ȳ�����";             //TYPE2
	public static String TEMP_MED_RATE = "�����������ʳ�����";         //TYPE3
	public static String TEMP_MED_DEV_ABS = "�����¶ȼ�����ֵ������";   //TYPE4
	public static String TEMP_ENV_MAX = "�����¶ȳ�����";            //TYPE5
	public static String TEMP_ENV_RATE = "�����������ʳ�����";         //TYPE6
	public static String TEMP_ENV_DEV_ABS = "�����¶ȼ�����ֵ������";    //TYPE7
	public static String TEMP_DEV_ABS = "��֧�¶Ȳ����";            //TYPE8
	public static String TEMP_AVG_DEV_ABS = "�¶Ⱦ������";         //TYPE9
	public static String HUMUDITY_MAX = "ʪ�ȳ�����";                //TYPE10
	public static String MIN_BATTERY_VOL = "��ѹ��������";           //TYPE11
	public static String MIN_WIRELESS_SIG = "�����źŵ�������";       //TYPE12
	public static String HUMUDITY_NULL = "ʪ�Ȳɼ��쳣";       //TYPE13
	
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
