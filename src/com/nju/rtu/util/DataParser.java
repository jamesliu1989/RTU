package com.nju.rtu.util;

/**
 * 数据转换类，提供静态方法进行数据转换（byte到所需数据类型）
 * 
 * @author james
 * */
public class DataParser {
	/**
	 * 将16进制byte的高4位转换为int
	 * @param b
	 * @return
	 */
	public static int hex2IntHigh(byte b){
		return new Integer((b >> 4) & 0x0f);
	}
	
	/**
	 * 将16进制byte的低4位转换为int
	 * @param b
	 * @return
	 */
	public static int hex2IntLow(byte b){
		return new Integer(b & 0x0f);
	}
	
	/**
	 * 将byte转换成double
	 * @param a
	 * @return
	 */
	public static double hex2Double(byte a){
		return (double) ((a & 0xff)) / 10;
	}
	
	/**
	 * 将byte[2]转换成double
	 * @param a
	 * @param b
	 * @return
	 */
	public static double hex2Double(byte a, byte b){
		double v = (double) (((a & 0xff) << 8) | (b & 0xff));
	    return v / 10;
	}


	/** 单元测试 */
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