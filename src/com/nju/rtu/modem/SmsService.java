package com.nju.rtu.modem;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;
import org.smslib.SMSLibException;
import org.smslib.Service;
import org.smslib.Message.MessageEncodings;
import org.smslib.TimeoutException;
import org.smslib.modem.SerialModemGateway;

import com.nju.rtu.model.SystemConfig;

public class SmsService {
	private static final Logger LOG = LoggerFactory.getLogger(SmsService.class);

	static  {
		//读取系统设置
		SystemConfig config = SystemConfig.getInstance();
		String comPort = config.getSmsComPort();
		int baudRete = Integer.parseInt(config.getSmsBaudRate());
		
        //modem.com1:网关ID（即短信猫端口编号）
        //COM4:串口名称（在window中以COMXX表示端口名称，在linux,unix平台下以ttyS0-N或ttyUSB0-N表示端口名称），通过端口检测程序得到可用的端口
        //115200：串口每秒发送数据的bit位数,必须设置正确才可以正常发送短信，可通过程序进行检测。常用的有115200、9600
        //Huawei：短信猫生产厂商，不同的短信猫生产厂商smslib所封装的AT指令接口会不一致，必须设置正确.常见的有Huawei、wavecom等厂商
        //最后一个参数表示设备的型号，可选
		
		SerialModemGateway gateway = new SerialModemGateway("modem.com1", comPort, baudRete, "Cinterion", "MC52iR3");
		gateway.setOutbound(true); // 设置true，表示该网关可以发送短信,根据需求修改
		gateway.setSimPin("1234"); // sim卡锁，一般默认为0000或1234
		// Explicit SMSC address set is required for some modems.
		// Below is for VODAFONE GREECE - be sure to set your own!
		gateway.setSmscNumber("+8613800515500"); // 短信服务中心号码

		Service.getInstance().setOutboundMessageNotification(
				new IOutboundMessageNotification() {
					public void process(AGateway gateway, OutboundMessage msg) {
						
					}
				}); // 发送短信成功后的回调函方法
		
		try {
			Service.getInstance().addGateway(gateway); // 将网关添加到短信猫服务中
			Service.getInstance().startService(); // 启动服务，进入短信发送就绪状态
		} catch (SMSLibException | IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void sendSms(String[] phoneNums, String content) {

		for (String phoneNum : phoneNums) {
			OutboundMessage msg = new OutboundMessage(phoneNum, content); // 参数1：手机号码
																			// 参数2：短信内容
			msg.setEncoding(MessageEncodings.ENCUCS2); // 中文
			try {
				Service.getInstance().sendMessage(msg);
			} catch (TimeoutException | GatewayException | IOException
					| InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 执行发送短信
			LOG.info(msg.toString());
		}
	}
	
	public static void closeService(){
		try {
			Service.getInstance().stopService();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GatewayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SMSLibException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
