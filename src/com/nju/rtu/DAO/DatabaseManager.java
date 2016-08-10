package com.nju.rtu.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nju.rtu.model.AreaAlertInfo;
import com.nju.rtu.model.AreaAlertParameter;
import com.nju.rtu.model.AreaInfo;
import com.nju.rtu.model.Controler;
import com.nju.rtu.model.NodeInfo;
import com.nju.rtu.model.RegularData;
import com.nju.rtu.model.ReportDailyArea;
import com.nju.rtu.model.ReportDailyNode;
import com.nju.rtu.model.SmsAlertInfo;
import com.nju.rtu.model.SmsInfo;
import com.nju.rtu.model.SystemConfig;

public class DatabaseManager {
private static final Logger LOG = LoggerFactory.getLogger(DatabaseManager.class);
	
	private volatile static Connection conn = null;
	private volatile static Statement statement = null;
	private volatile static PreparedStatement prepareStatement = null;
	// private static ResultSet rs=null;
	private static SimpleDateFormat simpleDataFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat simpleDataFormat2 = new SimpleDateFormat(
			"yyyy-MM-dd");

	public synchronized static boolean connectDB(String con, String username,
			String password) throws Exception {
		// 1. 注册驱动
		Class.forName("com.mysql.jdbc.Driver");// Mysql 的驱动
		// 2. 获取数据库的连接
		conn = java.sql.DriverManager.getConnection(con, username, password);
		if (conn == null) {
			LOG.info("连接数据库失败。");
			return false;
		}
		statement = conn.createStatement();
		LOG.info("成功连接数据库。");
		return true;
	}
	
	public static void close(Connection conn, Statement stmt, ResultSet rs){
		if(rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(stmt != null){
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void close(ResultSet rs){
		if(rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void disconnectDB() throws Exception {
		if (prepareStatement != null) {
			prepareStatement.close();
		}
		if (statement != null){
			statement.close();
		}
		if (conn != null){
		    conn.close();
		}
	}

	/**
	 * 插入常规数据
	 * @param data
	 */
	public static void insertRegularData(RegularData data){
		String sql = "INSERT INTO regular_data (CTRLER_NO, NODE_NO, TEMP_MED, TEMP_ENV, HUMIDITY, SMOG_ALERT, BATTERY_VOL, WIRELESS_SIG) VALUES (?, ? ,? ,? , ?, ?, ?, ?)";
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setInt(1, data.getCtrlerNo());
			prepareStatement.setString(2, data.getNodeNo());
			prepareStatement.setDouble(3, data.getTempMed());
			prepareStatement.setDouble(4, data.getTempEnv());
			prepareStatement.setDouble(5, data.getHumidity());
			prepareStatement.setInt(6, data.getSmogAlert());
			prepareStatement.setDouble(7, data.getBatteryVol());
			prepareStatement.setInt(8, data.getWirelessSig());
			int result = prepareStatement.executeUpdate();
			if (result == 1) {
				LOG.info("常规数据保存成功！");
			} else {
				LOG.info("常规数据保存失败！");
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
	}
	
	//获取控制器信息
	public static List<Controler> getControlerInfo(){
		String sql = "SELECT CTRLER_NO, NODE_NUM FROM CTRLER_INFO WHERE　STATUS　＝　１";
		ResultSet rs = null;
		List<Controler> ctrlers = new ArrayList<Controler>();
		try {
			prepareStatement = conn.prepareStatement(sql);
			rs = prepareStatement.executeQuery();
			while (rs.next()) {
				Controler c = new Controler();
                c.setCtrlerNo(rs.getInt(1));
                c.setNodeNum(rs.getInt(2));
                ctrlers.add(c);
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		return ctrlers;
	}
	
	//获取节点日报表信息
	public static boolean hasNodeDailyReport(String nodeNo, String dayOfYear){
		boolean has = false;
		String sql = "SELECT ID FROM report_daily_node WHERE NODE_NO = ? and day_of_year = ?";
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, nodeNo);
			prepareStatement.setString(2, dayOfYear);
			rs = prepareStatement.executeQuery();
			if(rs.next()){
				has = true;
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		return has;
	}
	
	//判断当天是否已经有区域日报
	public static boolean hasAreaDailyReport(String dayOfYear){
		boolean has = false;
		String sql = "SELECT ID FROM report_daily_area WHERE day_of_year = ?";
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, dayOfYear);
			rs = prepareStatement.executeQuery();
			if(rs.next()){
				has = true;
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		return has;
	}
	
	/**
	 * 根据节点编号获取节点
	 * @param nodeNo
	 * @return
	 */
	public static NodeInfo getNode(String nodeNo){
		NodeInfo node = null;
		String sql = "SELECT * FROM node_info WHERE NODE_NO = ?";
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, nodeNo);
			rs = prepareStatement.executeQuery();
			if(rs.next()){
				node = new NodeInfo(rs.getString("NODE_NO"),rs.getString("NODE_NAME"),rs.getString("AREA_NO"),
						rs.getInt("CTRLER_NO"),rs.getString("NODE_DESC"), rs.getInt("STATUS"), rs.getInt("ALERT"));
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		return node;
	}
	
	/**
	 * 获取常规数据
	 * @param nodeNo
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static List<RegularData> getNodeDailyReportByTime(String nodeNo,
			Timestamp startTime, Timestamp endTime) {
		String sql = "SELECT * FROM regular_data where NODE_NO = ? AND COLLECT_TIME BETWEEN ? AND ?";
		ResultSet rs = null;
		List<RegularData> dataList = new ArrayList<RegularData>();
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, nodeNo);
			prepareStatement.setTimestamp(2, startTime);
			prepareStatement.setTimestamp(3, endTime);
			rs = prepareStatement.executeQuery();
			while (rs.next()) {
				RegularData data = new RegularData(rs.getInt("CTRLER_NO"),
					rs.getString("NODE_NO"),rs.getDouble("TEMP_MED"),rs.getDouble("TEMP_ENV"), rs.getDouble("HUMIDITY"),
					rs.getInt("SMOG_ALERT"), rs.getDouble("BATTERY_VOL"), rs.getInt("WIRELESS_SIG"));
                dataList.add(data);
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		return dataList;
	}
	//保存节点日报表
	public static void insertNodeDailyReport(ReportDailyNode reportDailyNode) {
		String sql = "INSERT INTO `monitor`.`report_daily_node` (`NODE_NO`, `TEMP_MED_MAX`, `TEMP_MED_MIN`, `TEMP_MED_AVG`, `TEMP_ENV_MAX`, `TEMP_ENV_MIN`, `TEMP_ENV_AVG`, `TEMP_DEV_ABS`, `HUMIDITY_MAX`, `HUMIDITY_MIN`, `HUMIDITY_AVG`, `SMOG_ALERT`, `BATTERY_VOL`, `WIRELESS_SIG`, `STATUS`, `ALERT`, `day_of_year`, `month_of_year`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, reportDailyNode.getNodeNo());
			prepareStatement.setDouble(2, reportDailyNode.getTempMedMax());
			prepareStatement.setDouble(3, reportDailyNode.getTempMedMin());
			prepareStatement.setDouble(4, reportDailyNode.getTempMedAvg());
			prepareStatement.setDouble(5, reportDailyNode.getTempEnvMax());
			prepareStatement.setDouble(6, reportDailyNode.getTempEnvMin());
			prepareStatement.setDouble(7, reportDailyNode.getTempEnvAvg());
			prepareStatement.setDouble(8, reportDailyNode.getTempDevAbs());
			prepareStatement.setDouble(9, reportDailyNode.getHumidityMax());
			prepareStatement.setDouble(10, reportDailyNode.getHumidityMin());
			prepareStatement.setDouble(11, reportDailyNode.getHumidityAvg());
			prepareStatement.setInt(12, reportDailyNode.getSmogAlert());
			prepareStatement.setDouble(13, reportDailyNode.getBatteryVol());
			prepareStatement.setInt(14, reportDailyNode.getWirelessSig());
			prepareStatement.setInt(15, reportDailyNode.getStatus());
			prepareStatement.setInt(16, reportDailyNode.getAlert());
			prepareStatement.setString(17, reportDailyNode.getDayOfYear());
			prepareStatement.setString(18, reportDailyNode.getMonthOfYear());
			int result = prepareStatement.executeUpdate();
			if (result == 1) {
				LOG.info("节点日报保存成功！");
			} else {
				LOG.info("节点日报保存失败！");
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}		
	}
	
	//通过所有区域名称
	public static List<String> getAllAreas(){
		List<String> areas = new ArrayList<String>();
		String sql = "SELECT AREA_NO FROM AREA_info";
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);
			rs = prepareStatement.executeQuery();
			while(rs.next()){
				areas.add(rs.getString(1));
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
	    return areas;
	}
	
	//通过区域名称获活动取节点编号
	public static List<String> getNodeNoByArea(String areaNo){
		List<String> nodes = new ArrayList<String>();
		String sql = "SELECT NODE_NO FROM node_info WHERE AREA_NO = ? and STATUS =1";
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, areaNo);
			rs = prepareStatement.executeQuery();
			while(rs.next()){
				nodes.add(rs.getString(1));
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
	    return nodes;
	}
	
	//通过区域名称获活动取节点
	public static List<NodeInfo> getNodeByArea(String areaNo){
		List<NodeInfo> nodes = new ArrayList<NodeInfo>();
		String sql = "SELECT * FROM node_info WHERE AREA_NO = ? and STATUS =1";
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, areaNo);
			rs = prepareStatement.executeQuery();
			while(rs.next()){
				NodeInfo node = new NodeInfo(rs.getString("NODE_NO"),rs.getString("NODE_NAME"),rs.getString("AREA_NO"),
						rs.getInt("CTRLER_NO"),rs.getString("NODE_DESC"), rs.getInt("STATUS"), rs.getInt("ALERT"));
				nodes.add(node);
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
	    return nodes;
	}
	
	/**
	 * 找到节点最新数据
	 * @param nodeNo
	 * @return
	 */
	public static RegularData findLatestByNodeNo(String nodeNo) {
		RegularData data = null;
		String sql = "SELECT * FROM regular_data WHERE NODE_NO = ? ORDER BY COLLECT_TIME DESC";
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, nodeNo);
			rs = prepareStatement.executeQuery();
			if(rs.next()){
				data = new RegularData(rs.getInt("CTRLER_NO"),
						rs.getString("NODE_NO"),rs.getDouble("TEMP_MED"),rs.getDouble("TEMP_ENV"), rs.getDouble("HUMIDITY"),
						rs.getInt("SMOG_ALERT"), rs.getDouble("BATTERY_VOL"), rs.getInt("WIRELESS_SIG"));
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
	    return data;
	}
	
	//保存区域日报表
	public static void insertAreaDailyReport(ReportDailyArea reportDailyArea) {
		String sql = "INSERT INTO `monitor`.`report_daily_area` (`AREA_NO`, `TEMP_MED_MAX`, `TEMP_MED_MIN`, `TEMP_MED_AVG`, `TEMP_ENV_MAX`, `TEMP_ENV_MIN`, `TEMP_ENV_AVG`, `TEMP_DEV_ABS`, `HUMIDITY_MAX`, `HUMIDITY_MIN`, `HUMIDITY_AVG`, `SMOG_ALERT`, `BATTERY_VOL`, `WIRELESS_SIG`, `STATUS`, `ALERT`, `day_of_year`, `month_of_year`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, reportDailyArea.getAreaNo());
			prepareStatement.setDouble(2, reportDailyArea.getTempMedMax());
			prepareStatement.setDouble(3, reportDailyArea.getTempMedMin());
			prepareStatement.setDouble(4, reportDailyArea.getTempMedAvg());
			prepareStatement.setDouble(5, reportDailyArea.getTempEnvMax());
			prepareStatement.setDouble(6, reportDailyArea.getTempEnvMin());
			prepareStatement.setDouble(7, reportDailyArea.getTempEnvAvg());
			prepareStatement.setDouble(8, reportDailyArea.getTempDevAbs());
			prepareStatement.setDouble(9, reportDailyArea.getHumidityMax());
			prepareStatement.setDouble(10, reportDailyArea.getHumidityMin());
			prepareStatement.setDouble(11, reportDailyArea.getHumidityAvg());
			prepareStatement.setInt(12, reportDailyArea.getSmogAlert());
			prepareStatement.setDouble(13, reportDailyArea.getBatteryVol());
			prepareStatement.setInt(14, reportDailyArea.getWirelessSig());
			prepareStatement.setInt(15, reportDailyArea.getStatus());
			prepareStatement.setInt(16, reportDailyArea.getAlert());
			prepareStatement.setString(17, reportDailyArea.getDayOfYear());
			prepareStatement.setString(18, reportDailyArea.getMonthOfYear());
			int result = prepareStatement.executeUpdate();
			if (result == 1) {
				LOG.info("区域日报保存成功！");
			} else {
				LOG.info("区域日报保存失败！");
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}		
	}
	
	/**
	 * 更新区域信息
	 * @param areaInfo
	 */
	@Deprecated
	public static void updateAreaInfo(AreaInfo areaInfo) {
		String sql = "UPDATE monitor.area_info SET MAX_TEMP_MED=?, MIN_TEMP_MED=?, AVG_TEMP_MED=?, MAX_TEMP_ENV=?, MIN_TEMP_ENV=?, AVG_TEMP_ENV=?, ALERT=? WHERE AREA_NAME =?";
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setInt(7, areaInfo.getAlert());
			prepareStatement.setString(8, areaInfo.getAreaName());
			int result = prepareStatement.executeUpdate();
			if (result == 1) {
				LOG.info("区域信息更新成功！");
			} else {
				LOG.info("区域信息更新失败！");
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		
	}
	
	/**
	 * 更新区域信息
	 * @param areaInfo
	 */
	public static void updateAreaAlert(String areaNo, int alert) {
		String sql = "UPDATE monitor.area_info SET ALERT=? WHERE AREA_NO =?";
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setInt(1, alert);
			prepareStatement.setString(2, areaNo);
			int result = prepareStatement.executeUpdate();
			if (result == 1) {
				LOG.info("区域信息更新成功！");
			} else {
				LOG.info("区域信息更新失败！");
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		
	}
	
	public static AreaInfo getAreaInfo(String areaNo){
		String sql = "SELECT * FROM area_info WHERE AREA_NO = ?";
		AreaInfo areaInfo = null;
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);			
			prepareStatement.setString(1, areaNo);
			rs = prepareStatement.executeQuery();
			if (rs.next()) {
				areaInfo = new AreaInfo(rs.getString("AREA_NO"), rs.getString("AREA_NAME"), rs.getInt("CTRLER_NO"), rs.getString("AREA_DESC"), rs.getInt("ALERT"));
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		return areaInfo;
	}
	
	public static List<AreaAlertParameter> getAreaAlertPrmt(String areaNo){
		String sql = "SELECT * FROM area_alert_parameter WHERE AREA_NO = ? ORDER BY TYPE ASC";
		List<AreaAlertParameter> aaps = new ArrayList<AreaAlertParameter>();
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);			
			prepareStatement.setString(1, areaNo);
			rs = prepareStatement.executeQuery();
			while (rs.next()) {
				AreaAlertParameter aap = new AreaAlertParameter(rs.getString("AREA_NO"),
						rs.getDouble("TEMP_MED_MAX"), rs.getDouble("TEMP_MED_RATE"), rs.getDouble("TEMP_MED_DEV_ABS"), 
						rs.getDouble("TEMP_ENV_MAX"), rs.getDouble("TEMP_ENV_RATE"), rs.getDouble("TEMP_ENV_DEV_ABS"), 
					    rs.getDouble("HUMIDITY_MAX"), rs.getDouble("TEMP_DEV_ABS"), rs.getDouble("TEMP_AVG_DEV_ABS"), 
						rs.getDouble("MIN_BATTERY_VOL"), rs.getDouble("MIN_WIRELESS_SIG"), rs.getInt("TYPE"));
				aaps.add(aap);
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		return aaps;
	}
	
	/**
	 * 保存报警信息
	 */
	public static boolean insertAlert(AreaAlertInfo alert){
		String sql1 = "select id from area_alert_info where IS_READ = 0 and NODE_NO = ? and TYPE = ?";
		ResultSet rs = null;
		boolean inserted = false;   //最终是否报警
		try {
			prepareStatement = conn.prepareStatement(sql1);
			prepareStatement.setString(1, alert.getNodeNo());
			prepareStatement.setString(2, alert.getType());
			rs = prepareStatement.executeQuery();
			//如果该类报警不存在未解除报警，才会继续报警
		    if(!rs.next()){
		    	inserted = true;
				String sql = "INSERT INTO area_alert_info	(	AREA_NO,	NODE_NO,	ALERT_PROPERTY,	ALERT_TYPE,	ALERT_VALUE,	ALERT_MEASUREMENT,	ALERT_REMARKS, IS_READ, TYPE)	VALUES	(?,	?,	?,	?,	?,	?,	?, ?, ?)";
					prepareStatement = conn.prepareStatement(sql);
					prepareStatement.setString(1, alert.getAreaNo());
					prepareStatement.setString(2, alert.getNodeNo());
					prepareStatement.setInt(3, alert.getAlertProperty());
					prepareStatement.setString(4, alert.getAlertType());
					prepareStatement.setDouble(5, alert.getAlertValue());
					prepareStatement.setString(6, alert.getAlertMeasurement());
					prepareStatement.setString(7, alert.getAlertRemarks());
					prepareStatement.setInt(8, 0);
					prepareStatement.setString(9, alert.getType());
					
					int result = prepareStatement.executeUpdate();
					if (result == 1) {
						LOG.info("报警保存成功！");
					} else {
						LOG.info("报警保存失败！");
					}
		    }
		}catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}	
		return inserted;
	}
	//获取系统参数
	public static void getSystemConfig(){
		String sql = "SELECT * FROM system WHERE ID = 1";
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);			
			rs = prepareStatement.executeQuery();
			if (rs.next()) {
				SystemConfig.initial(rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getString(5), rs.getString(6));
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
	}
	
	/**
	 * 保存短信报警记录
	 */
	public static void insertSmsHistory(String receiver, String phone, String content){
		String sql = "INSERT INTO sms_history (SMS_RECEIVER, SMS_RECV_PHONE, SMS_CONTENT) VALUES (? , ? , ?)";
		try {
			prepareStatement = conn.prepareStatement(sql);
			prepareStatement.setString(1, receiver);
			prepareStatement.setString(2, phone);
			prepareStatement.setString(3, content);
			int result = prepareStatement.executeUpdate();
			if (result == 1) {
				LOG.info("短信插入成功！");
			} else {
				LOG.info("短信插入失败！");
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}		
	}
	
	/**
	 * 获取短信发送对象
	 * @return
	 */
	public static List<String> getRecvPhones(){
		String sql = "SELECT phone FROM user WHERE smsReceive = 1";
		List<String> phones = new ArrayList<String>();
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);			
			rs = prepareStatement.executeQuery();
			while (rs.next()) {
				phones.add(rs.getString("phone"));
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		return phones;
	}
	
	/**
	 * 获取短信报警内容
	 * @return
	 */
	public static SmsAlertInfo getSmsInfo(){
		String sql = "SELECT * FROM sms_info";
		SmsInfo type = null;
		SmsInfo content = null;
		ResultSet rs = null;
		try {
			prepareStatement = conn.prepareStatement(sql);			
			rs = prepareStatement.executeQuery();
			if (rs.next()) {
				content = new SmsInfo(rs.getString("SMS_TEMPLATE"), rs.getString("TYPE1"), rs.getString("TYPE2"), rs.getString("TYPE3"), rs.getString("TYPE4"),
						rs.getString("TYPE5"), rs.getString("TYPE6"), rs.getString("TYPE7"), rs.getString("TYPE8"), rs.getString("TYPE9"), rs.getString("TYPE10"),
						rs.getString("TYPE11"), rs.getString("TYPE12"));
			}
			if (rs.next()) {
				type = new SmsInfo("", rs.getString("TYPE1"), rs.getString("TYPE2"), rs.getString("TYPE3"), rs.getString("TYPE4"),
						rs.getString("TYPE5"), rs.getString("TYPE6"), rs.getString("TYPE7"), rs.getString("TYPE8"), rs.getString("TYPE9"), rs.getString("TYPE10"),
						rs.getString("TYPE11"), rs.getString("TYPE12"));
			}
		} catch (SQLException e) {
			LOG.error(e.toString());
			e.printStackTrace();
		}finally{
			  close(rs);
			  if(prepareStatement != null){
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				  }
		}
		SmsAlertInfo alertInfo = new SmsAlertInfo(type, content);
		return alertInfo;
	}
	
	public static void main(String[] args) throws Exception {
		DatabaseManager.connectDB("jdbc:mysql://localhost:3306/monitor", "root","root");
		System.out.print(getControlerInfo().get(0).getNodeNum());
	}





}
