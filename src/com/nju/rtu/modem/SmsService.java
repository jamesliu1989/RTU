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
		//��ȡϵͳ����
		SystemConfig config = SystemConfig.getInstance();
		String comPort = config.getSmsComPort();
		int baudRete = Integer.parseInt(config.getSmsBaudRate());
		
        //modem.com1:����ID��������è�˿ڱ�ţ�
        //COM4:�������ƣ���window����COMXX��ʾ�˿����ƣ���linux,unixƽ̨����ttyS0-N��ttyUSB0-N��ʾ�˿����ƣ���ͨ���˿ڼ�����õ����õĶ˿�
        //115200������ÿ�뷢�����ݵ�bitλ��,����������ȷ�ſ����������Ͷ��ţ���ͨ��������м�⡣���õ���115200��9600
        //Huawei������è�������̣���ͬ�Ķ���è��������smslib����װ��ATָ��ӿڻ᲻һ�£�����������ȷ.��������Huawei��wavecom�ȳ���
        //���һ��������ʾ�豸���ͺţ���ѡ
		
		SerialModemGateway gateway = new SerialModemGateway("modem.com1", comPort, baudRete, "Cinterion", "MC52iR3");
		gateway.setOutbound(true); // ����true����ʾ�����ؿ��Է��Ͷ���,���������޸�
		gateway.setSimPin("1234"); // sim������һ��Ĭ��Ϊ0000��1234
		// Explicit SMSC address set is required for some modems.
		// Below is for VODAFONE GREECE - be sure to set your own!
		gateway.setSmscNumber("+8613800515500"); // ���ŷ������ĺ���

		Service.getInstance().setOutboundMessageNotification(
				new IOutboundMessageNotification() {
					public void process(AGateway gateway, OutboundMessage msg) {
						
					}
				}); // ���Ͷ��ųɹ���Ļص�������
		
		try {
			Service.getInstance().addGateway(gateway); // ��������ӵ�����è������
			Service.getInstance().startService(); // �������񣬽�����ŷ��;���״̬
		} catch (SMSLibException | IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void sendSms(String[] phoneNums, String content) {

		for (String phoneNum : phoneNums) {
			OutboundMessage msg = new OutboundMessage(phoneNum, content); // ����1���ֻ�����
																			// ����2����������
			msg.setEncoding(MessageEncodings.ENCUCS2); // ����
			try {
				Service.getInstance().sendMessage(msg);
			} catch (TimeoutException | GatewayException | IOException
					| InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // ִ�з��Ͷ���
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
