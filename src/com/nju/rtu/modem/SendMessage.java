// SendMessage.java - Sample application.
//
// This application shows you the basic procedure for sending messages.
// You will find how to send synchronous and asynchronous messages.
//
// For asynchronous dispatch, the example application sets a callback
// notification, to see what's happened with messages.

package com.nju.rtu.modem;

import org.smslib.AGateway;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.Message.MessageEncodings;
import org.smslib.modem.SerialModemGateway;

public class SendMessage
{
	public void doIt() throws Exception
	{
		OutboundNotification outboundNotification = new OutboundNotification();
		System.out.println("Example: Send message from a serial gsm modem.");
		System.out.println(Library.getLibraryDescription());
		System.out.println("Version: " + Library.getLibraryVersion());
		SerialModemGateway gateway = new SerialModemGateway("modem.com7", "COM7", 115200, "Cinterion", "MC52iR3");
		gateway.setInbound(true);     //����true����ʾ�����ؿ��Խ��ն���,���������޸�  
		gateway.setOutbound(true);    //����true����ʾ�����ؿ��Է��Ͷ���,���������޸�  
		gateway.setSimPin("1234");    //sim������һ��Ĭ��Ϊ0000��1234 
		// Explicit SMSC address set is required for some modems.
		// Below is for VODAFONE GREECE - be sure to set your own!
		gateway.setSmscNumber("+8613800515500");    //���ŷ������ĺ���  
		Service.getInstance().setOutboundMessageNotification(outboundNotification);  //���Ͷ��ųɹ���Ļص�������
		Service.getInstance().addGateway(gateway);   //��������ӵ�����è������
		Service.getInstance().startService();    //�������񣬽�����ŷ��;���״̬  
		System.out.println();
		//��ӡ�豸��Ϣ  
		System.out.println("Modem Information:");
		System.out.println("  Manufacturer: " + gateway.getManufacturer());
		System.out.println("  Model: " + gateway.getModel());
		System.out.println("  Serial No: " + gateway.getSerialNo());
		System.out.println("  SIM IMSI: " + gateway.getImsi());
		System.out.println("  Signal Level: " + gateway.getSignalLevel() + " dBm");
		System.out.println("  Battery Level: " + gateway.getBatteryLevel() + "%");
		System.out.println();
		// Send a message synchronously.
		String receivePhones = "18662068486,18626412939";
		String[] phoneNums = receivePhones.split(",");
		for(String phoneNum : phoneNums){
			OutboundMessage msg = new OutboundMessage(phoneNum, "�����������¶ȳ�������!");  //����1���ֻ����� ����2����������  
			msg.setEncoding(MessageEncodings.ENCUCS2); // ����
			Service.getInstance().sendMessage(msg);   //ִ�з��Ͷ���  
			System.out.println(msg);
		}				
		// Or, send out a WAP SI message.
		//OutboundWapSIMessage wapMsg = new OutboundWapSIMessage("306974000000",  new URL("http://www.smslib.org/"), "Visit SMSLib now!");
		//Service.getInstance().sendMessage(wapMsg);
		//System.out.println(wapMsg);
		// You can also queue some asynchronous messages to see how the callbacks
		// are called...
		//msg = new OutboundMessage("309999999999", "Wrong number!");
		//srv.queueMessage(msg, gateway.getGatewayId());
		//msg = new OutboundMessage("308888888888", "Wrong number!");
		//srv.queueMessage(msg, gateway.getGatewayId());
		System.out.println("Now Sleeping - Hit <enter> to terminate.");
		System.in.read();
		Service.getInstance().stopService();
	}

	public class OutboundNotification implements IOutboundMessageNotification
	{
		public void process(AGateway gateway, OutboundMessage msg)
		{
			System.out.println("Outbound handler called from Gateway: " + gateway.getGatewayId());
			System.out.println(msg);
		}
	}

	public static void main(String args[])
	{
		SendMessage app = new SendMessage();
		try
		{
			app.doIt();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
