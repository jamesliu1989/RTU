package com.nju.rtu.model;

public class SmsAlertInfo {

	public SmsInfo type; // 用语保存报警类型选择
	public SmsInfo content; // 用语保存报警内容

	public SmsAlertInfo(SmsInfo type, SmsInfo content) {
		super();
		this.type = type;
		this.content = content;
	}

}
