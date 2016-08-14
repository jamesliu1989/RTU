package com.nju.rtu.model;

import com.nju.rtu.DAO.DatabaseManager;
import com.nju.rtu.util.Config;

/**
 * 系统config单例
 * @author james
 *
 */
public class SystemConfig implements java.io.Serializable {

	// Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = -8296827774683138381L;
	private static Integer id = 1;
	private static String comPort;
	private static String baudRate;
	private static Double collectCycle;
	private static String smsComPort;
	private static String smsBaudRate;
	private static Boolean showTempMed;
	private static Boolean showTempEnv;
	private static Boolean showHumidity;
	private static Boolean showSmogAlert;
	private static Boolean smsAlert;
	
	private static SystemConfig config = null;

	// Constructors

	/** default constructor */
	private SystemConfig() {
		// 1. Setup the parameters
        String dbHost = Config.getValue("dbHost");
        String dbPort = Config.getValue("dbPort");
        String dbUser = Config.getValue("dbUser");
        String dbPwd  = Config.getValue("dbPwd");
        try {
			DatabaseManager.connectDB("jdbc:mysql://"+ dbHost +":"+ dbPort +"/monitor", dbUser, dbPwd);
			DatabaseManager.getSystemConfig();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				DatabaseManager.disconnectDB();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        
	}

	/** initial */
	public static void initial(String comPort, String baudRate, Double collectCycle, 
			String smsComPort, String smsBaudRate, Boolean showTempMed, Boolean showTempEnv, Boolean showHumidity, Boolean showSmogAlert, Boolean smsAlert) {
		SystemConfig.comPort = comPort;
	    SystemConfig.baudRate = baudRate;
		SystemConfig.collectCycle = collectCycle;
		SystemConfig.smsComPort = smsComPort;
		SystemConfig.smsBaudRate = smsBaudRate;
		SystemConfig.showTempMed = showTempMed;
		SystemConfig.showTempEnv = showTempEnv;
		SystemConfig.showHumidity = showHumidity;
		SystemConfig.showSmogAlert = showSmogAlert;
		SystemConfig.smsAlert = smsAlert;
	}

	//返回config单例
	public static SystemConfig getInstance(){
		if(config == null){
			config = new SystemConfig();
		}
		return config;
	}
	
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getComPort() {
		return this.comPort;
	}

	public void setComPort(String comPort) {
		this.comPort = comPort;
	}

	public String getBaudRate() {
		return this.baudRate;
	}

	public void setBaudRate(String baudRate) {
		this.baudRate = baudRate;
	}

	public Double getCollectCycle() {
		return this.collectCycle;
	}

	public void setCollectCycle(Double collectCycle) {
		this.collectCycle = collectCycle;
	}

	public String getSmsComPort() {
		return smsComPort;
	}

	public void setSmsComPort(String smsComPort) {
		this.smsComPort = smsComPort;
	}

	public String getSmsBaudRate() {
		return smsBaudRate;
	}

	public void setSmsBaudRate(String smsBaudRate) {
		this.smsBaudRate = smsBaudRate;
	}

	public Boolean getShowTempMed() {
		return showTempMed;
	}

	public void setShowTempMed(Boolean showTempMed) {
		this.showTempMed = showTempMed;
	}

	public Boolean getShowTempEnv() {
		return showTempEnv;
	}

	public void setShowTempEnv(Boolean showTempEnv) {
		this.showTempEnv = showTempEnv;
	}

	public Boolean getShowHumidity() {
		return showHumidity;
	}

	public void setShowHumidity(Boolean showHumidity) {
		this.showHumidity = showHumidity;
	}

	public Boolean getShowSmogAlert() {
		return showSmogAlert;
	}

	public void setShowSmogAlert(Boolean showSmogAlert) {
		this.showSmogAlert = showSmogAlert;
	}

	public Boolean getSmsAlert() {
		return smsAlert;
	}

	public void setSmsAlert(Boolean smsAlert) {
		this.smsAlert = smsAlert;
	}
	
	

}
