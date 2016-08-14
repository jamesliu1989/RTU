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
    private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy��MM��dd��HHʱmm��ss��");
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
				//��ȡ���п�������Ϣ
				List<Controler> ctrlers = DatabaseManager.getControlerInfo();
				for(Controler ctrl : ctrlers){
					int slaveId = ctrl.getCtrlerNo();
					int nodeNum = ctrl.getNodeNum();
					try {
						//�����ɼ�
/*                        int toFetchModes = nodeNum;						
						int fetchNodesNextCycle = Math.min(toFetchModes, MAX_NODE_PER_CYCLE);
						for(int fetched=0; toFetchModes > 0; fetched += fetchNodesNextCycle, toFetchModes -=fetchNodesNextCycle){
						    fetchNodesNextCycle = Math.min(toFetchModes, MAX_NODE_PER_CYCLE);
						    data = msm.readMultipleRegisters(slaveId, 2 * REGISTER_NUM_PER_RECORD * fetched, REGISTER_NUM_PER_RECORD * fetchNodesNextCycle);*/
						    data = msm.readMultipleRegisters(slaveId, 0, REGISTER_NUM_PER_RECORD * 12);
							if (data != null) {
								LOG.info("���ݲɼ��ɹ���");
								for(int i = 0, j = 0; i < data.length; i += RECORD_BYTES, j++){
									String nodeNo = "C"+slaveId+"-"+j;
									NodeInfo node = DatabaseManager.getNode(nodeNo);
									//������ڸýڵ㣬����ʹ��״̬����ɼ��õ�����
									if(node != null && node.getStatus() ==1){
											RegularData regularData = new RegularData(
													slaveId, nodeNo, 
													DataParser.hex2Double(data[i], data[i + 1]),       //�����¶�
													DataParser.hex2Double(data[i + 2],data[i + 3]),    //�����¶�
													DataParser.hex2Double(data[i + 7]) * 10,           //ʪ��
													DataParser.hex2IntHigh(data[i + 4]),               //������
													DataParser.hex2Double(data[i + 5]),                //��ص�ѹ
													DataParser.hex2IntLow(data[i + 4]));               //�����ź�ǿ��
											System.err.println("************************"+nodeNo+"**************************");
											if(data[i] == ERROR_BYTE && data[i + 1] == ERROR_BYTE){
												LOG.error("�¶ȴ�����A����");
											}
											if(data[i + 2] == ERROR_BYTE && data[i + 3] == ERROR_BYTE){
												LOG.error("�¶ȴ�����B����");
											}
											if(data[i + 7] == ERROR_BYTE){
												LOG.error("ʪ�ȴ���������");
											}
											LOG.info("�¶�A��"+ DataParser.hex2Double(data[i],data[i + 1]));
											LOG.info("�¶�B��"+ DataParser.hex2Double(data[i + 2],data[i + 3]));
											LOG.info("ʪ�ȣ�"+ DataParser.hex2Double(data[i + 7]) * 10);
											LOG.info("��������"+ DataParser.hex2IntHigh(data[i + 4]));
											LOG.info("�����ź���ȣ�"+ DataParser.hex2IntLow(data[i + 4]));
											LOG.info("��ص�ѹ��"+ DataParser.hex2Double(data[i + 5]));
											
											// Ԥ��������������
											processAlert(regularData);
											
											// �������ݳ־û������ڱ�������֮��
											DatabaseManager.insertRegularData(regularData);
											
											// �ڵ��ձ������
											nodeDailyReport(nodeNo);
											
											//���Ͷ��ű���
											if(config.getSmsAlert()){
											  sendAlertSms();
											}
									}
								}
							} else {
								LOG.error("������:"+slaveId+"���ݲɼ�ʧ�ܣ�");
							}
/*							//���������ɼ����ʱ��
							Thread.sleep(5 * 1000);
						}*/
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("������:"+slaveId+"���ݲɼ�ʧ�ܣ�");
						continue;
					}
				   
			}
				//�����ձ������
				areaDailyReport();	
				//�ر����ݿ�
				DatabaseManager.disconnectDB();
				//�ɼ���϶ʱ��
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
     * ���㵱ǰʱ���뵱���ֹʱ��ķֲ�
     * @return
     */
    public static long diffMinutes(){
    	Date now = new Date();
		Calendar calsettle = Calendar.getInstance();
		calsettle.setTime(now);
		calsettle.set(Calendar.HOUR_OF_DAY, 23);
		calsettle.set(Calendar.MINUTE, 59);
		calsettle.set(Calendar.SECOND, 59); // ���ɵ������ֹʱ��
		//���㵱ǰʱ���뵱���ֹʱ��ķֲ�
		long diffMinutes = (calsettle.getTime().getTime() - now.getTime()) / (1000 * 60); 
		return diffMinutes;
    }
    
    /**
     * ����ڵ��ձ���
     * @param nodeNo
     */
    public static void nodeDailyReport(String nodeNo){
    	String today = sdf.format(new Date());
    	long diffMinutes = diffMinutes();
   
    	//�����Ȼ�ڵ��ձ�δ����,�Ҿ൱���ֹ����10����
    	if(diffMinutes >= 0 && diffMinutes < 10 && !DatabaseManager.hasNodeDailyReport(nodeNo, today)){ 
    		//���ɽڵ��ձ���
    		processDailyReportNode(nodeNo, today);
	 }
    }
    
    /**
     * ���������ձ���
     * @param
     */
    public static void areaDailyReport(){
    	String today = sdf.format(new Date());
    	long diffMinutes = diffMinutes();
    	//�����Ȼ�ڵ��ձ�δ����,�Ҿ൱���ֹ����10����
    	if(diffMinutes >= 0 && diffMinutes < 10 && !DatabaseManager.hasAreaDailyReport(today)){ 
    		//���������ձ�
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

		//���ɽڵ��ձ���
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
					// ��Сֵ��ʼ��
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
												
				//ֻҪ��һ����������������������
				if(data.getSmogAlert() == 0){
					reportDailyNode.setSmogAlert(0);	
				}
				//��������һ����¼
				if(count == dataList.size()-1){					
					reportDailyNode.setBatteryVol(data.getBatteryVol());
					reportDailyNode.setWirelessSig(data.getWirelessSig());							                   
				}					
				count ++;
			}				
		}
		//����ڵ���Ϣ�����ڣ���״̬��Ϊ0
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
	//���������ձ���
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
					// ��Сֵ��ʼ��
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
   
    /**���ú����Ѿ�������
     * ����ʵʱ������Ϣ
     * @return
     */
    @Deprecated
	public static void generateRealTimeAreaInfo() {
		//��ȡ������������
		List<String> areaList = DatabaseManager.getAllAreas();
		for(String areaNo : areaList){
			AreaInfo areaInfo = new AreaInfo(areaNo);
			double tempMedMax = 0.0, tempMedMin = 0.0, tempMedSum = 0.0, tempEnvMax = 0.0, tempEnvMin = 0.0, tempEnvSum = 0.0;
			int count = 0;
			int alert = 0;
			//�õ����������������µ�ʵʱ����
			List<NodeInfo> nodeInfos = DatabaseManager.getNodeByArea(areaNo);
			for (NodeInfo node : nodeInfos){
				RegularData data = DatabaseManager.findLatestByNodeNo(node.getNodeNo());
				if(data != null){
					count ++;
					double tm = data.getTempMed();
					double te = data.getTempEnv(); 
					//��Сֵ��ʼ��
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
	 * ����Ԥ������������Ϣ
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
		//�滻����������Ϣ���������ж���һ��
	    String alertTime = sdf3.format(new Date());
	    //��ϵͳ������#��������#-#��������#��#�ڵ�����#-#�ڵ�����#��#ʱ��#����#��������#����ǰֵΪ#��ǰֵ#��#�������##�趨ֵ#)
	    String smsTemplate = smsAlertInfo.content.getSmsTemplate()	    		
	            .replace("#��������#", areaInfo.getAreaName())
				.replace("#��������#", areaInfo.getAreaDesc())
				.replace("#�ڵ�����#", nodeInfo.getNodeName())
				.replace("#�ڵ�����#", nodeInfo.getNodeDesc())
				.replace("#ʱ��#", alertTime);
		
		if(aap == null || aap.size()!=2){
			return;
		}
		if(config.getShowTempMed()){ 
		//�¶Ȳɼ��쳣  TYPE1
		if(data.getTempMed() == ERROR_TMP){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.TEMP_MED_NULL, 999.0, "��", "", "TYPE1"));
			DatabaseManager.updateAreaAlert(areaNo, 1);
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType1().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType1())
						.replace("#��ǰֵ#", "")
						.replace("#�趨ֵ#", "");		
				smsContentList.add(smsContent);
			}
			return;
		}
		}
		
		if(config.getShowTempEnv()){ 
		//�¶Ȳɼ��쳣  TYPE1
		if(data.getTempEnv() == ERROR_TMP){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.TEMP_ENV_NULL, 999.0, "��", "", "TYPE1"));
			DatabaseManager.updateAreaAlert(areaNo, 1);
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType1().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType1())
						.replace("#��ǰֵ#", "")
						.replace("#�趨ֵ#", "");		
				smsContentList.add(smsContent);
			}
			return;
		}
		}
		
        if(config.getShowHumidity()){
		//ʪ�Ȳɼ��쳣  TYPE13
		if(data.getHumidity() == ERROR_HU){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.HUMUDITY_NULL, 999.9, "%RH", "", "TYPE13"));
			DatabaseManager.updateAreaAlert(areaNo, 1);
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType13().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType13())
						.replace("#��ǰֵ#", "")
						.replace("#�趨ֵ#", "");		
				smsContentList.add(smsContent);
			}
			return;
		}
        }
        if(config.getShowTempMed()){
		//�����¶����ޱ��� TYPE2			
		if(data.getTempMed() > aap.get(1).getTempMedMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.TEMP_MED_MAX+"("+aap.get(1).getTempMedMax()+")", data.getTempMed(), "��", "", "TYPE2"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType2().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType2())
						.replace("#��ǰֵ#", data.getTempMed()+"��")
						.replace("#�趨ֵ#", aap.get(1).getTempMedMax()+"��");		
				smsContentList.add(smsContent);
			}
		}else if(data.getTempMed() > aap.get(0).getTempMedMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
					AreaAlertInfo.TEMP_MED_MAX+"("+aap.get(0).getTempMedMax()+")", data.getTempMed(), "��", "", "TYPE2"));
			alert = 1;
			
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType2().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType2())
						.replace("#��ǰֵ#", data.getTempMed()+"��")
						.replace("#�趨ֵ#", aap.get(0).getTempMedMax()+"��");		
				smsContentList.add(smsContent);
			}			
		}
        }
        
        if(config.getShowTempEnv()){
		//�����¶����ޱ��� TYPE5
		if(data.getTempEnv() > aap.get(1).getTempEnvMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.TEMP_ENV_MAX+"("+aap.get(1).getTempEnvMax()+")", data.getTempEnv(), "��", "", "TYPE5"));
		    alert = 1;
		  //��Ӷ��ű���
			if(success && smsAlertInfo.type.getType5().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType5())
						.replace("#��ǰֵ#", data.getTempEnv()+"��")
						.replace("#�趨ֵ#", aap.get(1).getTempEnvMax()+"��");		
				smsContentList.add(smsContent);
			}
		}else if(data.getTempEnv() > aap.get(0).getTempEnvMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
					AreaAlertInfo.TEMP_ENV_MAX+"("+aap.get(0).getTempEnvMax()+")", data.getTempEnv(), "��", "", "TYPE5"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType5().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType5())
						.replace("#��ǰֵ#", data.getTempEnv()+"��")
						.replace("#�趨ֵ#", aap.get(0).getTempEnvMax()+"��");		
				smsContentList.add(smsContent);
			}
		}
        }
		
        if(config.getShowHumidity()){
		//ʪ�����ޱ��� TYPE10
		if(data.getHumidity() > aap.get(1).getHumidityMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
					AreaAlertInfo.HUMUDITY_MAX+"("+aap.get(1).getHumidityMax()+")", data.getHumidity(), "%RH", "", "TYPE10"));
		    alert = 1;
		  //��Ӷ��ű���
			if(success && smsAlertInfo.type.getType10().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType10())
						.replace("#��ǰֵ#", data.getHumidity()+"%RH")
						.replace("#�趨ֵ#", aap.get(1).getHumidityMax()+"%RH");		
				smsContentList.add(smsContent);
			}
		}else if(data.getHumidity() > aap.get(0).getHumidityMax()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
					AreaAlertInfo.HUMUDITY_MAX+"("+aap.get(0).getHumidityMax()+")", data.getHumidity(), "%RH", "", "TYPE10"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType10().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType10())
						.replace("#��ǰֵ#", data.getHumidity()+"%RH")
						.replace("#�趨ֵ#", aap.get(0).getHumidityMax()+"%RH");		
				smsContentList.add(smsContent);
			}
		}
        }
		
		RegularData latestData = DatabaseManager.findLatestByNodeNo(nodeNO);
		
		//���û����ʷ���ݣ�����������
		if(latestData == null){
			return;
		}
		double deltaMedRate = Double.parseDouble(df.format((data.getTempMed() - latestData.getTempMed())/config.getCollectCycle()));
		double deltaEnvRate = Double.parseDouble(df.format((data.getTempEnv() - latestData.getTempEnv())/config.getCollectCycle()));
		if(config.getShowTempMed()){
		//�������������ޱ��� TYPE3
		if(deltaMedRate > aap.get(1).getTempMedRate()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_MED_RATE+"("+aap.get(1).getTempMedRate()+")", deltaMedRate, "��/��", "", "TYPE3"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType3().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType3())
						.replace("#��ǰֵ#", deltaMedRate+"")
						.replace("#�趨ֵ#", aap.get(1).getTempMedRate()+"");		
				smsContentList.add(smsContent);
			}
		}else if(deltaMedRate > aap.get(0).getTempMedRate()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_MED_RATE+"("+aap.get(0).getTempMedRate()+")", deltaMedRate, "��/��", "", "TYPE3"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType3().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType3())
						.replace("#��ǰֵ#", deltaMedRate+"")
						.replace("#�趨ֵ#", aap.get(0).getTempMedRate()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
			
		if(config.getShowTempEnv()){
		//�����������ޱ��� TYPE6
		if(deltaEnvRate > aap.get(1).getTempEnvRate()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_ENV_RATE+"("+aap.get(1).getTempEnvRate()+")", deltaEnvRate, "��/��", "", "TYPE6"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType6().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType6())
						.replace("#��ǰֵ#", deltaEnvRate+"")
						.replace("#�趨ֵ#", aap.get(1).getTempEnvRate()+"");		
				smsContentList.add(smsContent);
			}
		}else if(deltaEnvRate > aap.get(0).getTempEnvRate()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_ENV_RATE+"("+aap.get(0).getTempEnvRate()+")", deltaEnvRate, "��/��", "", "TYPE6"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType6().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType6())
						.replace("#��ǰֵ#", deltaEnvRate+"")
						.replace("#�趨ֵ#", aap.get(0).getTempEnvRate()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		if(config.getShowTempMed() && config.getShowTempEnv()){
		//��֧�²����ޱ��� TYPE8
		double deltaTemp = Double.parseDouble(df.format(Math.abs(data.getTempMed() - data.getTempEnv())));
		if(deltaTemp > aap.get(1).getTempDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_DEV_ABS+"("+aap.get(1).getTempDevAbs()+")", deltaTemp, "��", "", "TYPE8"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType8().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType8())
						.replace("#��ǰֵ#", deltaTemp+"��")
						.replace("#�趨ֵ#", aap.get(1).getTempDevAbs()+"��");		
				smsContentList.add(smsContent);
			}
		}else if(deltaTemp > aap.get(0).getTempDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_DEV_ABS+"("+aap.get(0).getTempDevAbs()+")", deltaTemp, "��", "", "TYPE8"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType8().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType8())
						.replace("#��ǰֵ#", deltaTemp+"��")
						.replace("#�趨ֵ#", aap.get(0).getTempDevAbs()+"��");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		
		//��ѹ���ޱ��� type11
		if(data.getBatteryVol() < aap.get(0).getMinBatteryVol()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.MIN_BATTERY_VOL+"("+aap.get(0).getMinBatteryVol()+")", data.getBatteryVol(), "V", "", "TYPE11"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType11().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType11())
						.replace("#��ǰֵ#", data.getBatteryVol()+"V")
						.replace("#�趨ֵ#", aap.get(0).getMinBatteryVol()+"V");		
				smsContentList.add(smsContent);
			}
		}else if(data.getBatteryVol() < aap.get(1).getMinBatteryVol()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.MIN_BATTERY_VOL+"("+aap.get(1).getMinBatteryVol()+")", data.getBatteryVol(), "V", "", "TYPE11"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType11().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType11())
						.replace("#��ǰֵ#", data.getBatteryVol()+"V")
						.replace("#�趨ֵ#", aap.get(1).getMinBatteryVol()+"V");		
				smsContentList.add(smsContent);
			}
		}
		
		//�ź����ޱ���  type12
		if(data.getWirelessSig() < aap.get(0).getMinWirelessSig()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.MIN_WIRELESS_SIG+"("+aap.get(0).getMinWirelessSig()+")", new Double(data.getWirelessSig()), "dB", "", "TYPE12"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType12().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType12())
						.replace("#��ǰֵ#", data.getWirelessSig()+"dB")
						.replace("#�趨ֵ#", aap.get(0).getMinWirelessSig()+"dB");		
				smsContentList.add(smsContent);
			}
		}else if(data.getWirelessSig() < aap.get(1).getMinWirelessSig()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.MIN_WIRELESS_SIG+"("+aap.get(1).getMinWirelessSig()+")", new Double(data.getWirelessSig()), "dB", "", "TYPE12"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType12().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType12())
						.replace("#��ǰֵ#", data.getWirelessSig()+"dB")
						.replace("#�趨ֵ#", aap.get(1).getMinWirelessSig()+"dB");		
				smsContentList.add(smsContent);
			}
		}
		
		double areaTempMedSum = 0.0, areaTempEnvSum = 0.0, areaTempDevSum = 0.0;
		int count = 0;
		//�õ����������������µ�ʵʱ����
		List<NodeInfo> nodeInfos = DatabaseManager.getNodeByArea(areaNo);
		for (NodeInfo node : nodeInfos){
			RegularData latestData2 = DatabaseManager.findLatestByNodeNo(node.getNodeNo());
			if(latestData2 != null){
				areaTempMedSum += latestData2.getTempMed();
				areaTempEnvSum += latestData2.getTempEnv();
				areaTempDevSum += Math.abs(latestData2.getTempMed() - latestData2.getTempEnv());
				//0ֵ���ݹ���
				if(latestData2.getTempMed() != 0 && latestData2.getTempEnv() != 0){
				    count++;
				}
			}			
		}

		double tempMedDevAbs = Double.parseDouble(df.format(Math.abs(data.getTempMed() - ((count != 0) ?  areaTempMedSum/count : 0))));
		double tempEnvDevAbs = Double.parseDouble(df.format(Math.abs(data.getTempEnv() - ((count != 0) ?  areaTempEnvSum/count : 0))));
		double tempAvgDevAbs = Double.parseDouble(df.format(Math.abs(Math.abs(data.getTempMed() - data.getTempEnv()) - ((count != 0) ?  areaTempDevSum/count : 0))));
			
		if(config.getShowTempMed()){
		//�����¶ȼ�����ֵ���ޱ���  type4
		if(tempMedDevAbs > aap.get(1).getTempMedDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_MED_DEV_ABS+"("+aap.get(1).getTempMedDevAbs()+")", tempMedDevAbs, "��", "", "TYPE4"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType4().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType4())
						.replace("#��ǰֵ#", tempMedDevAbs+"")
						.replace("#�趨ֵ#", aap.get(1).getTempMedDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}else if(tempMedDevAbs > aap.get(0).getTempMedDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_MED_DEV_ABS+"("+aap.get(0).getTempMedDevAbs()+")", tempMedDevAbs, "��", "", "TYPE4"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType4().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType4())
						.replace("#��ǰֵ#", tempMedDevAbs+"")
						.replace("#�趨ֵ#", aap.get(0).getTempMedDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		if(config.getShowTempEnv()){
		//�����¶ȼ�����ֵ���ޱ���  type7
		if(tempEnvDevAbs > aap.get(1).getTempEnvDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_ENV_DEV_ABS+"("+aap.get(1).getTempEnvDevAbs()+")", tempEnvDevAbs, "��", "", "TYPE7"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType7().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType7())
						.replace("#��ǰֵ#", tempEnvDevAbs+"")
						.replace("#�趨ֵ#", aap.get(1).getTempEnvDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}else if(tempEnvDevAbs > aap.get(0).getTempEnvDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_ENV_DEV_ABS+"("+aap.get(0).getTempEnvDevAbs()+")", tempEnvDevAbs, "��", "", "TYPE7"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType7().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType7())
						.replace("#��ǰֵ#", tempEnvDevAbs+"")
						.replace("#�趨ֵ#", aap.get(0).getTempEnvDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		if(config.getShowTempMed() && config.getShowTempEnv()){
		//�¶Ȳ�������ޱ��� type9
		if(tempAvgDevAbs > aap.get(1).getTempAvgDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_2,
			AreaAlertInfo.TEMP_AVG_DEV_ABS+"("+aap.get(1).getTempAvgDevAbs()+")", tempAvgDevAbs, "��", "", "TYPE9"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType9().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "����")
						.replace("#�������#", smsAlertInfo.content.getType9())
						.replace("#��ǰֵ#", tempAvgDevAbs+"")
						.replace("#�趨ֵ#", aap.get(1).getTempAvgDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}else if(tempAvgDevAbs > aap.get(0).getTempAvgDevAbs()){
			boolean success = DatabaseManager.insertAlert(new AreaAlertInfo(areaNo, nodeNO, AreaAlertInfo.ALERT_PROPERTY_PRE_1,
			AreaAlertInfo.TEMP_AVG_DEV_ABS+"("+aap.get(0).getTempAvgDevAbs()+")", tempAvgDevAbs, "��", "", "TYPE9"));
			alert = 1;
			//��Ӷ��ű���
			if(success && smsAlertInfo.type.getType9().equals("1")){
				String smsContent = smsTemplate						
						.replace("#��������#", "Ԥ����")
						.replace("#�������#", smsAlertInfo.content.getType9())
						.replace("#��ǰֵ#", tempAvgDevAbs+"")
						.replace("#�趨ֵ#", aap.get(0).getTempAvgDevAbs()+"");		
				smsContentList.add(smsContent);
			}
		}
		}
		
		if(alert ==1 ){
			DatabaseManager.updateAreaAlert(areaNo, 1);
		}
		
	}
	/**
	 * �������ŷ���
	 */
	public static void sendAlertSms(){
		List<String> phoneNums = DatabaseManager.getRecvPhones();
	    for(String smsContent : smsContentList){
	    	SmsService.sendSms(phoneNums.toArray(new String[phoneNums.size()]), smsContent);
	    	DatabaseManager.insertSmsHistory("all", "all", smsContent);
	    }
	}
    
}