package com.nju.rtu.model;

public class AreaInfo implements java.io.Serializable {

	// Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = -4852219054132117385L;
	private Integer id;
	private String areaNo;
	private String areaName;
	private Integer nodeNum = 0;
	private Integer ctrlerNo;
	private String areaDesc;
	private Integer alert = 0;  //区域报警。0：无报警，1：温度报警，2：温差报警，3：烟雾报警

	// Constructors

	/** default constructor */
	public AreaInfo() {
	}

	public AreaInfo(String areaNo) {
		this.areaNo = areaNo;
	}
	/** minimal constructor */
	public AreaInfo(String areaNo, String areaName, Integer nodeNum,
			Integer ctrlerNo, String areaDesc, Integer alert) {
		this.areaNo = areaNo;
		this.areaName = areaName;
		this.nodeNum = nodeNum;
		this.ctrlerNo = ctrlerNo;
		this.areaDesc = areaDesc;
		this.alert = alert;
	}
	
	public AreaInfo(String areaNo, String areaName,
			Integer ctrlerNo, String areaDesc, Integer alert) {
		this.areaNo = areaNo;
		this.areaName = areaName;
		this.ctrlerNo = ctrlerNo;
		this.areaDesc = areaDesc;
		this.alert = alert;
	}

	/** full constructor */
	public AreaInfo(String areaNo, String areaName, Integer nodeNum, Integer alert) {
		this.areaNo = areaNo;
		this.areaName = areaName;
		this.nodeNum = nodeNum;
		this.alert = alert;
	}

	// Property accessors
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAreaName() {
		return this.areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public Integer getNodeNum() {
		return this.nodeNum;
	}

	public void setNodeNum(Integer nodeNum) {
		this.nodeNum = nodeNum;
	}

	public Integer getAlert() {
		return this.alert;
	}

	public void setAlert(Integer alert) {
		this.alert = alert;
	}

	public String getAreaNo() {
		return areaNo;
	}

	public void setAreaNo(String areaNo) {
		this.areaNo = areaNo;
	}

	public Integer getCtrlerNo() {
		return ctrlerNo;
	}

	public void setCtrlerNo(Integer ctrlerNo) {
		this.ctrlerNo = ctrlerNo;
	}

	public String getAreaDesc() {
		return areaDesc;
	}

	public void setAreaDesc(String areaDesc) {
		this.areaDesc = areaDesc;
	}
	
	
}