package com.nju.rtu.model;

public class SmsAlertInfo {

	public SmsInfo type; // ���ﱣ�汨������ѡ��
	public SmsInfo content; // ���ﱣ�汨������

	public SmsAlertInfo(SmsInfo type, SmsInfo content) {
		super();
		this.type = type;
		this.content = content;
	}

}
