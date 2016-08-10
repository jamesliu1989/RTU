package com.nju.rtu.model;

import java.sql.Timestamp;

/**
 * SmsHistory entity. @author MyEclipse Persistence Tools
 */

public class SmsHistory implements java.io.Serializable {

	// Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String smsReceiver;
	private String smsRecvPhone;
	private String smsContent;
	private Timestamp smsSendTime;

	// Constructors

	/** default constructor */
	public SmsHistory() {
	}

	/** minimal constructor */
	public SmsHistory(String smsReceiver, String smsRecvPhone,
			Timestamp smsSendTime) {
		this.smsReceiver = smsReceiver;
		this.smsRecvPhone = smsRecvPhone;
		this.smsSendTime = smsSendTime;
	}

	/** full constructor */
	public SmsHistory(String smsReceiver, String smsRecvPhone,
			String smsContent, Timestamp smsSendTime) {
		this.smsReceiver = smsReceiver;
		this.smsRecvPhone = smsRecvPhone;
		this.smsContent = smsContent;
		this.smsSendTime = smsSendTime;
	}

	// Property accessors
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSmsReceiver() {
		return this.smsReceiver;
	}

	public void setSmsReceiver(String smsReceiver) {
		this.smsReceiver = smsReceiver;
	}

	public String getSmsRecvPhone() {
		return this.smsRecvPhone;
	}

	public void setSmsRecvPhone(String smsRecvPhone) {
		this.smsRecvPhone = smsRecvPhone;
	}

	public String getSmsContent() {
		return this.smsContent;
	}

	public void setSmsContent(String smsContent) {
		this.smsContent = smsContent;
	}

	public Timestamp getSmsSendTime() {
		return this.smsSendTime;
	}

	public void setSmsSendTime(Timestamp smsSendTime) {
		this.smsSendTime = smsSendTime;
	}

}