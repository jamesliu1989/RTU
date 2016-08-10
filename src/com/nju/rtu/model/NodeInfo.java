package com.nju.rtu.model;


/**
 * NodeInfo entity. @author MyEclipse Persistence Tools
 */
public class NodeInfo implements java.io.Serializable {

	// Fields

	private Integer id;
	private String nodeNo;
	private String nodeName;
	private String areaNo;
	private Integer ctrlerNo;
	private String nodeDesc;
	private Integer status;
	private Integer alert;

	// Constructors

	/** default constructor */
	public NodeInfo() {
	}

	/** minimal constructor */
	public NodeInfo(String nodeNo, String nodeName, String areaNo,
			Integer ctrlerNo) {
		this.nodeNo = nodeNo;
		this.nodeName = nodeName;
		this.areaNo = areaNo;
		this.ctrlerNo = ctrlerNo;
	}

	/** full constructor */
	public NodeInfo(String nodeNo, String nodeName, String areaNo,
			Integer ctrlerNo, String nodeDesc, Integer status, Integer alert) {
		this.nodeNo = nodeNo;
		this.nodeName = nodeName;
		this.areaNo = areaNo;
		this.ctrlerNo = ctrlerNo;
		this.nodeDesc = nodeDesc;
		this.status = status;
		this.alert = alert;
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

	public String getNodeName() {
		return this.nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getAreaNo() {
		return this.areaNo;
	}

	public void setAreaNo(String areaNo) {
		this.areaNo = areaNo;
	}

	public Integer getCtrlerNo() {
		return this.ctrlerNo;
	}

	public void setCtrlerNo(Integer ctrlerNo) {
		this.ctrlerNo = ctrlerNo;
	}

	public String getNodeDesc() {
		return this.nodeDesc;
	}

	public void setNodeDesc(String nodeDesc) {
		this.nodeDesc = nodeDesc;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getAlert() {
		return alert;
	}

	public void setAlert(Integer alert) {
		this.alert = alert;
	}

}
