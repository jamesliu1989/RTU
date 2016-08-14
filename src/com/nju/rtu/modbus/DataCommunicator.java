package com.nju.rtu.modbus;


import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.nju.rtu.DAO.DatabaseManager;
import com.nju.rtu.model.AreaAlertInfo;
import com.nju.rtu.model.AreaAlertParameter;
import com.nju.rtu.model.AreaInfo;
import com.nju.rtu.model.Controler;
import com.nju.rtu.model.NodeInfo;
import com.nju.rtu.model.RegularData;
import com.nju.rtu.model.ReportDailyArea;
import com.nju.rtu.model.ReportDailyNode;
import com.nju.rtu.model.SmsAlertInfo;
import com.nju.rtu.model.SystemConfig;
import com.nju.rtu.modem.SmsService;
import com.nju.rtu.util.Config;
import com.nju.rtu.util.DataParser;

// -Djava.library.path="/usr/lib/jni/"
public class DataCommunicator {
	
	private static final Logger LOG = LoggerFactory.getLogger(DataCommunicator.class);
	
	private static DecimalFormat df =new DecimalFormat("#####0.0");
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
    private static SystemConfig config = SystemConfig.getInstance();
    
    //totla bytes for one record
    private static final int RECORD_BYTES = 8;
       
    //registers for one record
    private static final int REGISTER_NUM_PER_RECORD = 4;
    
    //max NODEs for one cycle to fetch
    private static final int MAX_NODE_PER_CYCLE = 10;
    
    //error byte from device
    private static final byte ERROR_BYTE = (byte) 0xFF;
    
    private static final double ERROR_TMP = 6553.5;
    
    private static final double ERROR_HU = 255;
    
    private static List<String> smsContentList = new ArrayList<String>();
    
    public static void main(String[] args) throws Exception {
	
		ModbusSerialMaster msm = null;
		
		// 1. Setup the parameters
        String dbHost = Config.getValue("dbHost");
        String dbPort = Config.getValue("dbPort");
        String dbUser = Config.getValue("dbUser");
        String dbPwd  = Config.getValue("dbPwd");
			
		try {
			// 2. Setup serial parameters
			SerialParameters params = new SerialParameters();
			params.setPortName(config.getComPort());
			params.setBaudRate(config.getBaudRate());
			params.setDatabits(8);
			params.setParity("None");
			params.setStopbits(1);
			params.setEncoding("rtu");
			params.setEcho(false);		

			// 3. Create the master facade
			msm = new ModbusSerialMaster(params);
			msm.connect();

			while (true){
				DatabaseManager.connectDB("jdbc:mysql://"+ dbHost +":"+ dbPort +"/monitor", dbUser, dbPwd);
				byte[] data = null;
				//获取所有控制器信息
				List<Controler> ctrlers = DatabaseManager.getControlerInfo();
				for(Controler ctrl : ctrlers){
					int slaveId = ctrl.getCtrlerNo();
					int nodeNum = ctrl.getNodeNum();
					try {
						//分批采集
/*                        int toFetchModes = nodeNum;						
						int fetchNodesNextCycle = Math.min(toFetchModes, MAX_NODE_PER_CYCLE);
						for(int fetched=0; toFetchModes > 0; fetched += fetchNodesNextCycle, toFetchModes -=fetchNodesNextCycle){
						    fetchNodesNextCycle = Math.min(toFetchModes, MAX_NODE_PER_CYCLE);
						    data = msm.readMultipleRegisters(slaveId, 2 * REGISTER_NUM_PER_RECORD * fetched, REGISTER_NUM_PER_RECORD * fetchNodesNextCycle);*/
						    data = msm.readMultipleRegisters(slaveId, 0, REGISTER_NUM_PER_RECORD * 12);
							if (data != null) {
								LOG.info("数据采集成功！");
								for(int i = 0, j = 0; i < data.length; i += RECORD_BYTES, j++){
									String nodeNo = "C"+slaveId+"-"+j;
									NodeInfo node = DatabaseManager.getNode(nodeNo);
									//如果存在该节点，且是使用状态，则采集该点数据
									if(node != null && node.getStatus() ==1){
											RegularData regularData = new RegularData(
													slaveId, nodeNo, 
													DataParser.hex2Double(data[i], data[i + 1]),       //介质温度
													DataParser.hex2Double(data[i + 2],data[i + 3]),    //环境温度
													DataParser.hex2Double(data[i + 7]) * 10,           //湿度
													DataParser.hex2IntHigh(data[i + 4]),               //烟雾报警
													DataParser.hex2Double(data[i + 5]),                //电池电压
													DataParser.hex2IntLow(data[i + 4]));               //无线信号强度
											System.err.println("************************"+nodeNo+"**************************");
											if(data[i] == ERROR_BYTE && data[i + 1] == ERROR_BYTE){
												LOG.error("温度传感器A出错！");
											}
											if(data[i + 2] == ERROR_BYTE && data[i + 3] == ERROR_BYTE){
												LOG.error("温度传感器B出错！");
											}
											if(data[i + 7] == ERROR_BYTE){
												LOG.error("湿度传感器出错！");
											}
											LOG.info("温度A："+ DataParser.hex2Double(data[i],data[i + 1]));
											LOG.info("温度B："+ DataParser.hex2Double(data[i + 2],data[i + 3]));
											LOG.info("湿度："+ DataParser.hex2Double(data[i + 7]) * 10);
											LOG.info("烟雾报警："+ DataParser.hex2IntHigh(data[i + 4]));
											LOG.info("无线信号轻度："+ DataParser.hex2IntLow(data[i + 4]));
											LOG.info("电池电压："+ DataParser.hex2Double(data[i + 5]));
											
											// 预报警、报警计算
											processAlert(regularData);
											
											// 常规数据持久化必须在报警处理之后
											DatabaseManager.insertRegularData(regularData);
											
											// 节点日报表计算
											nodeDailyReport(nodeNo);
											
											//发送短信报警
											if(config.getSmsAlert()){
											  sendAlertSms();
											}
									}
								}
							} else {
								LOG.error("控制器:"+slaveId+"数据采集失败！");
							}
/*							//多借点连续采集间隔时间
							Thread.sleep(5 * 1000);
						}*/
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("控制器:"+slaveId+"数据采集失败！");
						continue;
					}
				   
			}
				//区域日报表计算
				areaDailyReport();	
				//关闭数据库
				DatabaseManager.disconnectDB();
				//采集间隙时间
				Double cycle = config.getCollectCycle() * 0.9 * 60 * 1000;			    
				Thread.sleep(cycle.intValue());								
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			msm.disconnect();
		}
    }
    
    /**
     * 计算当前时间与当天截止时间的分差
     * @return
     */
    public static long diffMinutes(){
    	Date now = new Date();
		Calendar calsettle = Calendar.getInstance();
		calsettle.setTime(now);
		calsettle.set(Calendar.HOUR_OF_DAY, 23);
		calsettle.set(Calendar.MINUTE, 59);
		calsettle.set(Calendar.SECOND, 59); // 生成当天出截止时间
		//计算当前时间与当天截止时间的分差
		long diffMinutes = (calsettle.getTime().getTime() - now.getTime()) / (1000 * 60); 
		return diffMinutes;
    }
    
    /**
     * 处理节点日报表
     * @param nodeNo
     */
    public static void nodeDailyReport(String nodeNo){
    	String today = sdf.format(new Date());
    	long diffMinutes = diffMinutes();
   
    	//如果当然节点日报未生成,且距当天截止不到10分钟
    	if(diffMinutes >= 0 && diffMinutes < 10 && !DatabaseManager.hasNodeDailyReport(nodeNo, today)){ 
    		//生成节点日报表
    		processDailyReportNode(nodeNo, today);
	 }
    }
    
    /**
     * 处理区域日报表
     * @param
     */
    public static void areaDailyReport(){
    	String today = sdf.format(new Date());
    	long diffMinutes = diffMinutes();
    	//如果当然节点日报未生成,且距当天截止不到10分钟
    	if(diffMinutes >= 0 && diffMinutes < 10 && !DatabaseManager.hasAreaDailyReport(today)){ 
    		//生产区域日报
    		processDailyReportArea(today);
	    }
    }
       
    public static void processDailyReportNode(String nodeNo, String day){
    	Timestamp startTime = null, endTime = null;
		try {
			startTime = new Timestamp(sdf2.parse(day+" 00:00:00").getTime());
			endTime = new Timestamp(sdf2.parse(day+" 23:59:59").getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		//生成节点日报表
		ReportDailyNode reportDailyNode = new ReportDailyNode();   
		reportDailyNode.setSmogAlert(1);
    	List<RegularData> dataList = DatabaseManager.getNodeDailyReportByTime(nodeNo, startTime, endTime);
        NodeInfo node = DatabaseManager.getNode(nodeNo);           
		double tempMedMax = 0.0, tempMedMin = 0.0, tempMedSum = 0.0, tempEnvMax = 0.0, tempEnvMin = 0.0, tempEnvSum = 0.0, humidityMax = 0.0, humidityMin = 0.0, humiditySum = 0.0;
		int count = 0, tmCount = 0, teCount = 0, huCount = 0; //use for error data condition
		for (RegularData data : dataList) {
			if(data != null){				
				double tm = data.getTempMed();
				double te = data.getTempEnv();
				double hu = data.getHumidity();

				if (tm != ERROR_TMP) {
					// 最小值初始化
					if (count == 0) {
						tempMedMin = tm;
					}
					if (tm > tempMedMax) {
						tempMedMax = tm;
					}
					if (tm < tempMedMin) {
						tempMedMin = tm;
					}
					tempMedSum += tm;
				}else{
					tmCount--;
				}
				
				if (te != ERROR_TMP) {
					if (count == 0) {
						tempEnvMin = te;
					}
					if (te > tempEnvMax) {
						tempEnvMax = te;
					}
					if (te < tempEnvMin) {
						tempEnvMin = te;
					}
					tempEnvSum += te;
				}else{
					teCount--;
				}
				
				if (hu != ERROR_HU) {
					if (count == 0) {
						humidityMin = hu;
					}
					if (hu > humidityMax) {
						humidityMax = hu;
					}
					if (hu < humidityMin) {
						humidityMin = hu;
					}
					humiditySum += hu;
				} else {
					huCount--;
				}
												
				//只要有一次烟雾报警，则当天有烟雾报警
				if(data.getSmogAlert() == 0){
					reportDailyNode.setSmogAlert(0);	
				}
				//如果是最后一条记录
				if(count == dataList.size()-1){					
					reportDailyNode.setBatteryVol(data.getBatteryVol());
					reportDailyNode.setWirelessSig(data.getWirelessSig());							                   
				}					
				count ++;
			}				
		}
		//如果节点信息不存在，则状态置为0
        if(node != null){
        	reportDailyNode.setStatus(node.getStatus());
        	reportDailyNode.setAlert(node.getAlert());
        }else{
        	reportDailyNode.setStatus(0);
        	reportDailyNode.setAlert(0);
		}
		reportDailyNode.setNodeNo(nodeNo);
		reportDailyNode.setTempMedMax(tempMedMax);
		reportDailyNode.setTempMedMin(tempMedMin);
		reportDailyNode.setTempMedAvg((tempMedSum==0) ? 0.0 : Double.parseDouble(df.format(tempMedSum/(count + tmCount))));
		reportDailyNode.setTempEnvMax(tempEnvMax);
		reportDailyNode.setTempEnvMin(tempEnvMin);
		reportDailyNode.setTempEnvAvg((tempEnvSum==0) ? 0.0 : Double.parseDouble(df.format(tempEnvSum/(count + teCount))));
		reportDailyNode.setHumidityMax(humidityMax);
		reportDailyNode.setHumidityMin(humidityMin);
		reportDailyNode.setHumidityAvg((humiditySum==0) ? 0.0 : Double.parseDouble(df.format(humiditySum/(count + huCount))));
		reportDailyNode.setTempDevAbs(Double.parseDouble(df.format(Math.abs(reportDailyNode.getTempMedAvg()-reportDailyNode.getTempEnvAvg()))));
		reportDailyNode.setDayOfYear(day);
		reportDailyNode.setMonthOfYear(day.substring(0, 7));
		
		DatabaseManager.insertNodeDailyReport(reportDailyNode);
	
    } 
    
    public static void processDailyReportArea(String day){
    	Timestamp startTime = null, endTime = null;
		try {
			startTime = new Timestamp(sdf2.parse(day+" 00:00:00").getTime());
			endTime = new Timestamp(sdf2.parse(day+" 23:59:59").getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	//生成区域日报表
	List<String> areas = DatabaseManager.getAllAreas();
	for(String areaNo : areas){
		List<NodeInfo> nodes = DatabaseManager.getNodeByArea(areaNo);
		ReportDailyArea reportDailyArea = new ReportDailyArea();
		reportDailyArea.setSmogAlert(1);
		reportDailyArea.setStatus(1);
  	    reportDailyArea.setAlert(0);
		double tempMedMax = 0.0, tempMedMin = 0.0, tempMedSum = 0.0, tempEnvMax = 0.0, tempEnvMin = 0.0, tempEnvSum = 0.0, humidityMax = 0.0, humidityMin = 0.0, humiditySum = 0.0;
		int count = 0, tmCount = 0, teCount = 0, huCount = 0; //use for error data condition;
		double batteryVol = 0.0;
		int wirelessSig = 0;
	  for(NodeInfo node : nodes){		   
    	List<RegularData> dataList = DatabaseManager.getNodeDailyReportByTime(node.getNodeNo(), startTime, endTime);         
		for (RegularData data : dataList) {
			if(data != null){					
				double tm = data.getTempMed();
				double te = data.getTempEnv();
				double hu = data.getHumidity();
				
				if (tm != ERROR_TMP) {
					// 最小值初始化
					if (count == 0) {
						tempMedMin = tm;
					}
					if (tm > tempMedMax) {
						tempMedMax = tm;
					}
					if (tm < tempMedMin) {
						tempMedMin = tm;
					}
					tempMedSum += tm;
				}else{
					tmCount--;
				}
				
				if (te != ERROR_TMP) {
					if (count == 0) {
						tempEnvMin = te;
					}
					if (te > tempEnvMax) {
						tempEnvMax = te;
					}
					if (te < tempEnvMin) {
						tempEnvMin = te;
					}
					tempEnvSum += te;
				}else{
					teCount--;
				}
				
				if (hu != ERROR_HU) {
					if (count == 0) {
						humidityMin = hu;
					}
					if (hu > humidityMax) {
						humidityMax = hu;
					}
					if (hu < humidityMin) {
						humidityMin = hu;
					}
					humiditySum += hu;
				} else {
					huCount--;
				}
				
				if(data.getSmogAlert() == 0){
					reportDailyArea.setSmogAlert(0);
				}else{
					
				}					
				count ++;
				batteryVol = data.getBatteryVol();
				wirelessSig = data.getWirelessSig();
			}				
		}
		if(node.getStatus() == 0){
		  reportDailyArea.setStatus(0);
		}
		if(node.getAlert() != 0){
    	  reportDailyArea.setAlert(node.getAlert());
		}
		
	  } 
		reportDailyArea.setBatteryVol(batteryVol);
		reportDailyArea.setWirelessSig(wirelessSig);		
		reportDailyArea.setAreaNo(areaNo);
		reportDailyArea.setTempMedMax(tempMedMax);
		reportDailyArea.setTempMedMin(tempMedMin);
		reportDailyArea.setTempMedAvg((tempMedSum==0) ? 0.0 : Double.parseDouble(df.format(tempMedSum/(count + tmCount))));
		reportDailyArea.setTempEnvMax(tempEnvMax);
		reportDailyArea.setTempEnvMin(tempEnvMin);
		reportDailyArea.setTempEnvAvg((tempEnvSum==0) ? 0.0 : Double.parseDouble(df.format(tempEnvSum/(count + teCount))));
		reportDailyArea.setHumidityMax(humidityMax);
		reportDailyArea.setHumidityMin(humidityMin);
		reportDailyArea.setHumidityAvg((humiditySum==0) ? 0.0 : Double.parseDouble(df.format(humiditySum/(count + huCount))));
		reportDailyArea.setTempDevAbs(Double.parseDouble(df.format(Math.abs(reportDailyArea.getTempMedAvg()-reportDailyArea.getTempEnvAvg()))));
		reportDailyArea.setDayOfYear(day);
		reportDailyArea.setMonthOfYear(day.substring(0, 7));

		DatabaseManager.insertAreaDailyReport(reportDailyArea);
	
	}
   }
   
    /**【该函数已经废弃】
     * 生产实时区域信息
     * @return
     */
    @Deprecated
	public static void generateRealTimeAreaInfo() {
		//获取所有区域名称
		List<String> areaList = DatabaseManager.getAllAreas();
		for(String areaNo : areaList){
			AreaInfo areaInfo = new AreaInfo(areaNo);
			double tempMedMax = 0.0, tempMedMin = 0.0, tempMedSum = 0.0, tempEnvMax = 0.0, tempEnvMin = 0.0, tempEnvSum = 0.0;
			int count = 0;
			int alert = 0;
			//拿到各个区域里面最新的实时数据
			List<NodeInfo> nodeInfos = DatabaseManager.getNodeByArea(areaNo);
			for (NodeInfo node : nodeInfos){
				RegularData data = DatabaseManager.findLatestByNodeNo(node.getNodeNo());
				if(data != null){
					count ++;
					double tm = data.getTempMed();
					double te = data.getTempEnv(); 
					//最小值初始化
					if(count==1){
						tempMedMin = tm;
						tempEnvMin = te;
					}
					if(tm > tempMedMax){
						tempMedMax = tm;
					}
					if(tm < tempMedMin){
						tempMedMin = tm;
					}
					if(te > tempEnvMax){
						tempEnvMax = te;
					}
					if(te < tempEnvMin){
						tempEnvMin = te;
					}
					tempMedSum += tm;
					tempEnvSum += te;

				}				
			}
/*			areaInfo.setMaxTempMed(tempMedMax);
			areaInfo.setMinTempMed(tempMedMin);
			areaInfo.setAvgTempMed((tempMedSum==0) ? 0.0 : Double.parseDouble(df.format(tempMedSum/count)));
			
			areaInfo.setMaxTempEnv(tempEnvMax);
			areaInfo.setMinTempEnv(tempEnvMax);
			areaInfo.setAvgTempEnv((tempEnvSum==0) ? 0.0 : Double.parseDouble(df.format(tempEnvSum/count)));*/
			
			areaInfo.setAlert(alert);						
			DatabaseManager.updateAreaInfo(areaInfo);
		}					
	}
	/**
	 * 处理预报警、报警信息
	 * @param nodeNo
	 */
	public static void processAlert(RegularData data){
		int alert = 0;		
		String nodeNO = data.getNodeNo();
		NodeInfo nodeInfo = DatabaseManager.getNode(nodeNO);
		String areaNo = nodeInfo.getAreaNo();
		AreaInfo areaInfo = DatabaseManager.getAreaInfo(areaNo);
		List<AreaAlertParameter> aap = DatabaseManager.getAreaAlertPrmt(areaNo);
		
		SmsAlertInfo smsAlertInfo = DatabaseManager.getSmsInfo();
		smsContentList.clear();
		//替换报警基本信息，保存所有短信一致
	    String alertTime = sdf3.format(new Date());
	    //【系统报警】#区域名称#-#区域描述#，#节点名称#-#节点描述#于#时间#发生#报警级别#：当前值为#当前值#，#报警类别##设定值#)
	    String smsTemplate = smsAlertInfo.content.getSmsTemplate()	    		
	            .replace("#区域名称#", areaInfo.getAreaName())
				.replace("#区域描述#", areaInfo.getAreaDesc())
				.replace("#节点名称#", nodeInfo.getNodeName())
				.replace("#节点描述#", nodeInfo.getNodeDesc())
				.replace("#时间#", alertTime);
		
		if(aap == null || aap.size()!=2){
			return;
		}
		if(config.getShowTempMed()){ 
		//温度采集异常  TYPE1
		if(data.getTempMed() == ERROR_TMP){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.TEMP_MED_NULL, 999.0, "℃", "", "TYPE1"));
			DatabaseManager.updateAreaAlert(areaNo, 1);
			//添加短信报警
			if(success && smsAlertInfo.type.getType1().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType1())
						.replace("#当前值#", "")
						.replace("#设定值#", "");		
				smsContentList.add(smsContent);
			}
			return;
		}
		}
		
		if(config.getShowTempEnv()){ 
		//温度采集异常  TYPE1
		if(data.getTempEnv() == ERROR_TMP){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.TEMP_ENV_NULL, 999.0, "℃", "", "TYPE1"));
			DatabaseManager.updateAreaAlert(areaNo, 1);
			//添加短信报警
			if(success && smsAlertInfo.type.getType1().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType1())
						.replace("#当前值#", "")
						.replace("#设定值#", "");		
				smsContentList.add(smsContent);
			}
			return;
		}
		}
		
        if(config.getShowHumidity()){
		//湿度采集异常  TYPE13
		if(data.getHumidity() == ERROR_HU){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.HUMUDITY_NULL, 999.9, "%RH", "", "TYPE13"));
			DatabaseManager.updateAreaAlert(areaNo, 1);
			//添加短信报警
			if(success && smsAlertInfo.type.getType13().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType13())
						.replace("#当前值#", "")
						.replace("#设定值#", "");		
				smsContentList.add(smsContent);
			}
			return;
		}
        }
        if(config.getShowTempMed()){
		//介质温度上限报警 TYPE2			
		if(data.getTempMed() > aap.get(1).getTempMedMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.TEMP_MED_MAX+"("+aap.get(1).getTempMedMax()+")", data.getTempMed(), "℃", "", "TYPE2"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType2().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType2())
						.replace("#当前值#", data.getTempMed()+"℃")
						.replace("#设定值#", aap.get(1).getTempMedMax()+"℃");		
				smsContentList.add(smsContent);
			}
		}else if(data.getTempMed() > aap.get(0).getTempMedMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
					AreaAlertInfo.TEMP_MED_MAX+"("+aap.get(0).getTempMedMax()+")", data.getTempMed(), "℃", "", "TYPE2"));
			alert = 1;
			
			//添加短信报警
			if(success && smsAlertInfo.type.getType2().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType2())
						.replace("#当前值#", data.getTempMed()+"℃")
						.replace("#设定值#", aap.get(0).getTempMedMax()+"℃");		
				smsContentList.add(smsContent);
			}			
		}
        }
        
        if(config.getShowTempEnv()){
		//环境温度上限报警 TYPE5
		if(data.getTempEnv() > aap.get(1).getTempEnvMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.TEMP_ENV_MAX+"("+aap.get(1).getTempEnvMax()+")", data.getTempEnv(), "℃", "", "TYPE5"));
		    alert = 1;
		  //添加短信报警
			if(success && smsAlertInfo.type.getType5().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType5())
						.replace("#当前值#", data.getTempEnv()+"℃")
						.replace("#设定值#", aap.get(1).getTempEnvMax()+"℃");		
				smsContentList.add(smsContent);
			}
		}else if(data.getTempEnv() > aap.get(0).getTempEnvMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
					AreaAlertInfo.TEMP_ENV_MAX+"("+aap.get(0).getTempEnvMax()+")", data.getTempEnv(), "℃", "", "TYPE5"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType5().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType5())
						.replace("#当前值#", data.getTempEnv()+"℃")
						.replace("#设定值#", aap.get(0).getTempEnvMax()+"℃");		
				smsContentList.add(smsContent);
			}
		}
        }
		
        if(config.getShowHumidity()){
		//湿度上限报警 TYPE10
		if(data.getHumidity() > aap.get(1).getHumidityMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.HUMUDITY_MAX+"("+aap.get(1).getHumidityMax()+")", data.getHumidity(), "%RH", "", "TYPE10"));
		    alert = 1;
		  //添加短信报警
			if(success && smsAlertInfo.type.getType10().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType10())
						.replace("#当前值#", data.getHumidity()+"%RH")
						.replace("#设定值#", aap.get(1).getHumidityMax()+"%RH");		
				smsContentList.add(smsContent);
			}
		}else if(data.getHumidity() > aap.get(0).getHumidityMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
					AreaAlertInfo.HUMUDITY_MAX+"("+aap.get(0).getHumidityMax()+")", data.getHumidity(), "%RH", "", "TYPE10"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType10().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType10())
						.replace("#当前值#", data.getHumidity()+"%RH")
						.replace("#设定值#", aap.get(0).getHumidityMax()+"%RH");		
				smsContentList.add(smsContent);
			}
		}
        }
		
		RegularData latestData = DatabaseManager.findLatestByNodeNo(nodeNO);
		
		//如果没有历史数据，报警不处理
		if(latestData == null){
			return;
		}
		double deltaMedRate = Double.parseDouble(df.format((data.getTempMed() - latestData.getTempMed())/config.getCollectCycle()));
		double deltaEnvRate = Double.parseDouble(df.format((data.getTempEnv() - latestData.getTempEnv())/config.getCollectCycle()));
		if(config.getShowTempMed()){
		//介质温温升上限报警 TYPE3
		if(deltaMedRate > aap.get(1).getTempMedRate()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_MED_RATE+"("+aap.get(1).getTempMedRate()+")", deltaMedRate, "℃/分", "", "TYPE3"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType3().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType3())
						.replace("#当前值#", deltaMedRate+"")
						.replace("#设定值#", aap.get(1).getTempMedRate()+"");		
				smsContentList.add(smsContent);
			}
		}else if(deltaMedRate > aap.get(0).getTempMedRate()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_MED_RATE+"("+aap.get(0).getTempMedRate()+")", deltaMedRate, "℃/分", "", "TYPE3"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType3().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType3())
						.replace("#当前值#", deltaMedRate+"")
						.replace("#设定值#", aap.get(0).getTempMedRate()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
			
		if(config.getShowTempEnv()){
		//环境温升上限报警 TYPE6
		if(deltaEnvRate > aap.get(1).getTempEnvRate()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_ENV_RATE+"("+aap.get(1).getTempEnvRate()+")", deltaEnvRate, "℃/分", "", "TYPE6"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType6().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType6())
						.replace("#当前值#", deltaEnvRate+"")
						.replace("#设定值#", aap.get(1).getTempEnvRate()+"");		
				smsContentList.add(smsContent);
			}
		}else if(deltaEnvRate > aap.get(0).getTempEnvRate()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_ENV_RATE+"("+aap.get(0).getTempEnvRate()+")", deltaEnvRate, "℃/分", "", "TYPE6"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType6().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType6())
						.replace("#当前值#", deltaEnvRate+"")
						.replace("#设定值#", aap.get(0).getTempEnvRate()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		if(config.getShowTempMed() && config.getShowTempEnv()){
		//单支温差上限报警 TYPE8
		double deltaTemp = Double.parseDouble(df.format(Math.abs(data.getTempMed() - data.getTempEnv())));
		if(deltaTemp > aap.get(1).getTempDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_DEV_ABS+"("+aap.get(1).getTempDevAbs()+")", deltaTemp, "℃", "", "TYPE8"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType8().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType8())
						.replace("#当前值#", deltaTemp+"℃")
						.replace("#设定值#", aap.get(1).getTempDevAbs()+"℃");		
				smsContentList.add(smsContent);
			}
		}else if(deltaTemp > aap.get(0).getTempDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_DEV_ABS+"("+aap.get(0).getTempDevAbs()+")", deltaTemp, "℃", "", "TYPE8"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType8().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType8())
						.replace("#当前值#", deltaTemp+"℃")
						.replace("#设定值#", aap.get(0).getTempDevAbs()+"℃");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		
		//电压下限报警 type11
		if(data.getBatteryVol() < aap.get(0).getMinBatteryVol()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.MIN_BATTERY_VOL+"("+aap.get(0).getMinBatteryVol()+")", data.getBatteryVol(), "V", "", "TYPE11"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType11().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType11())
						.replace("#当前值#", data.getBatteryVol()+"V")
						.replace("#设定值#", aap.get(0).getMinBatteryVol()+"V");		
				smsContentList.add(smsContent);
			}
		}else if(data.getBatteryVol() < aap.get(1).getMinBatteryVol()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.MIN_BATTERY_VOL+"("+aap.get(1).getMinBatteryVol()+")", data.getBatteryVol(), "V", "", "TYPE11"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType11().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType11())
						.replace("#当前值#", data.getBatteryVol()+"V")
						.replace("#设定值#", aap.get(1).getMinBatteryVol()+"V");		
				smsContentList.add(smsContent);
			}
		}
		
		//信号下限报警  type12
		if(data.getWirelessSig() < aap.get(0).getMinWirelessSig()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.MIN_WIRELESS_SIG+"("+aap.get(0).getMinWirelessSig()+")", new Double(data.getWirelessSig()), "dB", "", "TYPE12"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType12().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType12())
						.replace("#当前值#", data.getWirelessSig()+"dB")
						.replace("#设定值#", aap.get(0).getMinWirelessSig()+"dB");		
				smsContentList.add(smsContent);
			}
		}else if(data.getWirelessSig() < aap.get(1).getMinWirelessSig()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.MIN_WIRELESS_SIG+"("+aap.get(1).getMinWirelessSig()+")", new Double(data.getWirelessSig()), "dB", "", "TYPE12"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType12().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType12())
						.replace("#当前值#", data.getWirelessSig()+"dB")
						.replace("#设定值#", aap.get(1).getMinWirelessSig()+"dB");		
				smsContentList.add(smsContent);
			}
		}
		
		double areaTempMedSum = 0.0, areaTempEnvSum = 0.0, areaTempDevSum = 0.0;
		int count = 0;
		//拿到各个区域里面最新的实时数据
		List<NodeInfo> nodeInfos = DatabaseManager.getNodeByArea(areaNo);
		for (NodeInfo node : nodeInfos){
			RegularData latestData2 = DatabaseManager.findLatestByNodeNo(node.getNodeNo());
			if(latestData2 != null){
				areaTempMedSum += latestData2.getTempMed();
				areaTempEnvSum += latestData2.getTempEnv();
				areaTempDevSum += Math.abs(latestData2.getTempMed() - latestData2.getTempEnv());
				//0值数据过滤
				if(latestData2.getTempMed() != 0 && latestData2.getTempEnv() != 0){
				    count++;
				}
			}			
		}

		double tempMedDevAbs = Double.parseDouble(df.format(Math.abs(data.getTempMed() - ((count != 0) ?  areaTempMedSum/count : 0))));
		double tempEnvDevAbs = Double.parseDouble(df.format(Math.abs(data.getTempEnv() - ((count != 0) ?  areaTempEnvSum/count : 0))));
		double tempAvgDevAbs = Double.parseDouble(df.format(Math.abs(Math.abs(data.getTempMed() - data.getTempEnv()) - ((count != 0) ?  areaTempDevSum/count : 0))));
			
		if(config.getShowTempMed()){
		//介质温度极均差值上限报警  type4
		if(tempMedDevAbs > aap.get(1).getTempMedDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_MED_DEV_ABS+"("+aap.get(1).getTempMedDevAbs()+")", tempMedDevAbs, "℃", "", "TYPE4"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType4().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType4())
						.replace("#当前值#", tempMedDevAbs+"")
						.replace("#设定值#", aap.get(1).getTempMedDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}else if(tempMedDevAbs > aap.get(0).getTempMedDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_MED_DEV_ABS+"("+aap.get(0).getTempMedDevAbs()+")", tempMedDevAbs, "℃", "", "TYPE4"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType4().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType4())
						.replace("#当前值#", tempMedDevAbs+"")
						.replace("#设定值#", aap.get(0).getTempMedDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		if(config.getShowTempEnv()){
		//环境温度极均差值上限报警  type7
		if(tempEnvDevAbs > aap.get(1).getTempEnvDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_ENV_DEV_ABS+"("+aap.get(1).getTempEnvDevAbs()+")", tempEnvDevAbs, "℃", "", "TYPE7"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType7().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType7())
						.replace("#当前值#", tempEnvDevAbs+"")
						.replace("#设定值#", aap.get(1).getTempEnvDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}else if(tempEnvDevAbs > aap.get(0).getTempEnvDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_ENV_DEV_ABS+"("+aap.get(0).getTempEnvDevAbs()+")", tempEnvDevAbs, "℃", "", "TYPE7"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType7().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType7())
						.replace("#当前值#", tempEnvDevAbs+"")
						.replace("#设定值#", aap.get(0).getTempEnvDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		if(config.getShowTempMed() && config.getShowTempEnv()){
		//温度差均差上限报警 type9
		if(tempAvgDevAbs > aap.get(1).getTempAvgDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_AVG_DEV_ABS+"("+aap.get(1).getTempAvgDevAbs()+")", tempAvgDevAbs, "℃", "", "TYPE9"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType9().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "报警")
						.replace("#报警类别#", smsAlertInfo.content.getType9())
						.replace("#当前值#", tempAvgDevAbs+"")
						.replace("#设定值#", aap.get(1).getTempAvgDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}else if(tempAvgDevAbs > aap.get(0).getTempAvgDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_AVG_DEV_ABS+"("+aap.get(0).getTempAvgDevAbs()+")", tempAvgDevAbs, "℃", "", "TYPE9"));
			alert = 1;
			//添加短信报警
			if(success && smsAlertInfo.type.getType9().equals("1")){
				String smsContent = smsTemplate						
						.replace("#报警级别#", "预报警")
						.replace("#报警类别#", smsAlertInfo.content.getType9())
						.replace("#当前值#", tempAvgDevAbs+"")
						.replace("#设定值#", aap.get(0).getTempAvgDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		if(alert ==1 ){
			DatabaseManager.updateAreaAlert(areaNo, 1);
		}
		
	}
	/**
	 * 报警短信发送
	 */
	public static void sendAlertSms(){
		List<String> phoneNums = DatabaseManager.getRecvPhones();
	    for(String smsContent : smsContentList){
	    	SmsService.sendSms(phoneNums.toArray(new String[phoneNums.size()]), smsContent);
	    	DatabaseManager.insertSmsHistory("all", "all", smsContent);
	    }
	}
    
}