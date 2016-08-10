package com.nju.rtu.util;

/**
 * ����ת���࣬�ṩ��̬������������ת����byte�������������ͣ�
 * 
 * @author james
 * */
public class DataParser {
	/**
	 * ��16����byte�ĸ�4λת��Ϊint
	 * @param b
	 * @return
	 */
	public static int hex2IntHigh(byte b){
		return new Integer((b >> 4) & 0x0f);
	}
	
	/**
	 * ��16����byte�ĵ�4λת��Ϊint
	 * @param b
	 * @return
	 */
	public static int hex2IntLow(byte b){
		return new Integer(b & 0x0f);
	}
	
	/**
	 * ��byteת����double
	 * @param a
	 * @return
	 */
	public static double hex2Double(byte a){
		return (double) ((a & 0xff)) / 10;
	}
	
	/**
	 * ��byte[2]ת����double
	 * @param a
	 * @param b
	 * @return
	 */
	public static double hex2Double(byte a, byte b){
		double v = (double) (((a & 0xff) << 8) | (b & 0xff));
	    return v / 10;
	}


	/** ��Ԫ���� */
	public static void main(String[] args) {
		byte b1 = (byte) 0x01;		
		byte b2 = (byte) 0x3c;
		byte b3 = (byte) 0x01;
		byte b4 = (byte) 0x27;
		byte b5 = (byte) 0x56;
		byte b6 = (byte) 0x34;
		
		byte b7 = (byte) 0xFF;
		// 01 3c 01 2c 57 34
		// 01 3c 01 36 a8 37
		System.out.println(hex2Double(b7, b7));
		System.out.println(hex2Double(b7));
		
	}

}