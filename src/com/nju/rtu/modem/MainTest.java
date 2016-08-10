package com.nju.rtu.modem;

import java.util.Scanner;

public class MainTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Send a message synchronously.
		String receivePhones = "18662068486,18623412939";
		String[] phoneNums = receivePhones.split(",");
		SmsService sms = new SmsService();
		//sms.startService();
		SmsService.sendSms(phoneNums, "报警：环境温度超过上限!");
       // sms.closeService();
        
        Scanner scan = new Scanner(System.in);
        while(scan.hasNextInt()){
        	int c = scan.nextInt();
        	switch(c){
        	case 0:
                sms.closeService();
                System.exit(0);
        		break;
        	case 1:
        		sms.sendSms(phoneNums, "报警：环境温度超过上限!");
        		break;
        	}
        }
        scan.close();
	}

}
