package com.nju.rtu.model;



/**
 * User entity. @author MyEclipse Persistence Tools
 */
public class SmsReceiver implements java.io.Serializable {

	// Fields
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7120924753850162031L;
	/**
	 * 
	 */
	private Integer id;
	private String name;
	private String phone;
	private Integer active = 1;   //0：禁用， 1：激活
	private Integer flag;         //0：普通用户，1：一般管理员，2：超级管理员
	private Integer smsReceive;         //0：不接收，1：接收

	// Constructors

	/** default constructor */
	public SmsReceiver() {
	}

	/** minimal constructor */
	public SmsReceiver(String name,  String phone,
			Integer active, Integer flag, Integer smsReceive) {
		this.name = name;
		this.phone = phone;
		this.active = active;
		this.flag = flag;
		this.smsReceive = smsReceive;
	}

	// Property accessors
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Integer getActive() {
		return this.active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}
	
	public Integer getFlag() {
		return this.flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
	}

	public Integer getSmsReceive() {
		return smsReceive;
	}

	public void setSmsReceive(Integer smsReceive) {
		this.smsReceive = smsReceive;
	}
}