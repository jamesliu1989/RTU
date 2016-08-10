package com.nju.rtu.model;


/**
 * User entity. @author MyEclipse Persistence Tools
 */
public class SmsInfo implements java.io.Serializable {

	// Fields
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5879355550874149944L;
	/**
	 * 
	 */
	private Integer id;
	private String smsTemplate;
	private String type1;
	private String type2;
	private String type3;
	private String type4;
	private String type5;
	private String type6;
	private String type7;
	private String type8;
	private String type9;
	private String type10;
	private String type11;
	private String type12;
	private String type13;


	// Constructors

	/** default constructor */
	public SmsInfo() {
	}

	/** minimal constructor */
	public SmsInfo(Integer id, String smsTemplate, String type1, String type2,
			String type3, String type4, String type5, String type6,
			String type7, String type8, String type9, String type10,
			String type11, String type12, String type13) {
		super();
		this.id = id;
		this.smsTemplate = smsTemplate;
		this.type1 = type1;
		this.type2 = type2;
		this.type3 = type3;
		this.type4 = type4;
		this.type5 = type5;
		this.type6 = type6;
		this.type7 = type7;
		this.type8 = type8;
		this.type9 = type9;
		this.type10 = type10;
		this.type11 = type11;
		this.type12 = type12;
		this.type13 = type13;
	}

		public SmsInfo(String smsTemplate, String type1, String type2,
			String type3, String type4, String type5, String type6,
			String type7, String type8, String type9, String type10,
			String type11, String type12) {
		this.smsTemplate = smsTemplate;
		this.type1 = type1;
		this.type2 = type2;
		this.type3 = type3;
		this.type4 = type4;
		this.type5 = type5;
		this.type6 = type6;
		this.type7 = type7;
		this.type8 = type8;
		this.type9 = type9;
		this.type10 = type10;
		this.type11 = type11;
		this.type12 = type12;
	}

	// Property accessors
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSmsTemplate() {
		return smsTemplate;
	}

	public String getType1() {
		return type1;
	}

	public String getType2() {
		return type2;
	}

	public String getType3() {
		return type3;
	}

	public String getType4() {
		return type4;
	}

	public String getType5() {
		return type5;
	}

	public String getType6() {
		return type6;
	}

	public String getType7() {
		return type7;
	}

	public String getType8() {
		return type8;
	}

	public String getType9() {
		return type9;
	}

	public String getType10() {
		return type10;
	}

	public String getType11() {
		return type11;
	}

	public String getType12() {
		return type12;
	}

	public void setSmsTemplate(String smsTemplate) {
		this.smsTemplate = smsTemplate;
	}

	public void setType1(String type1) {
		this.type1 = type1;
	}

	public void setType2(String type2) {
		this.type2 = type2;
	}

	public void setType3(String type3) {
		this.type3 = type3;
	}

	public void setType4(String type4) {
		this.type4 = type4;
	}

	public void setType5(String type5) {
		this.type5 = type5;
	}

	public void setType6(String type6) {
		this.type6 = type6;
	}

	public void setType7(String type7) {
		this.type7 = type7;
	}

	public void setType8(String type8) {
		this.type8 = type8;
	}

	public void setType9(String type9) {
		this.type9 = type9;
	}

	public void setType10(String type10) {
		this.type10 = type10;
	}

	public void setType11(String type11) {
		this.type11 = type11;
	}

	public void setType12(String type12) {
		this.type12 = type12;
	}

	public String getType13() {
		return type13;
	}

	public void setType13(String type13) {
		this.type13 = type13;
	}
	

}
