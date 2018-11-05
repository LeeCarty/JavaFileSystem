
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/***
 *	Author : LeeCarty
 *	Date   : 2018年10月8日, 下午9:48:53
 */

public class DMDBManager {
	
	// 数据库字符串常量
	private String jdbcDriverString = "dm.jdbc.driver.DmDriver";
	private String urlString = "jdbc:dm://localhost:5236";
	private String userName = "SYSDBA";
	private String password = "SYSDBA";
	
	static String sql = ""; // sql 串
	
	static Connection conn = null;  // 全局连接
	
	static Statement stmt = null;  // 全局语句句柄
	
	public DMDBManager() throws ClassNotFoundException, SQLException{	
		// 加载驱动，建立非自动提交的连接
		Class.forName(jdbcDriverString); // 加载驱动
		conn = DriverManager.getConnection(urlString, userName, password); // 建立连接
		conn.setAutoCommit(false); // 设置非自动提交
		
		stmt = conn.createStatement(); // 创建 语句句柄
		
		// 初始化系统表
		// table: file_user 和  history
		try{
			sql = "create table file_user("
					+ "id int identity(1, 1) not null, "
					+ "name char(20) primary key, "
					+ "psd varchar(20) not null, "
					+ "create_time datetime(6) default getdate(), "
					+ "last_login_time datetime(6) "
					+ ");";
			stmt.addBatch(sql);
			
			sql = "create table history(id int identity(1, 1) primary key, user_name char(20) foreign key references file_user(name),command varchar not null,status int not null,rtmsg varchar not null,create_time datetime(6) default getdate());";
			stmt.addBatch(sql);
			stmt.executeBatch();
			
			stmt.executeUpdate("insert into file_user(name, psd) values('admin', '1');");
			stmt.executeUpdate("insert into file_user(name, psd) values('leecarty', '111111');");
			
			conn.commit();
		} catch (Exception e){
			
		}
	}
	
	public void closeConnection() throws SQLException{
		stmt.close();
		conn.close();
	}
	
	public boolean checkUser(String userName, String psd) throws SQLException {
		// 验证 登录用户
		sql = "select * from file_user where name = '" + userName + "' and psd = '" + psd + "'"; 
		
		ResultSet rs = stmt.executeQuery(sql);

		if (!rs.next()){
			return false; // 无结果 返回false
		}
		
		// 更新 用户最近一次的登录时间
		sql = "update file_user set last_login_time = '" + 
				(new Timestamp(new Date().getTime())).toString() + 
				"' where name = '" + userName + "' and psd = '" + psd + "'";
		stmt.executeUpdate(sql);
		conn.commit();
		
		return true;
	}

	public void createHistory(String userName2, String commandLine, int reqStatus, String returnMsg) throws SQLException {
		// 创建一条操作历史记录
		sql = "insert into history(user_name, command, status, rtmsg) "
				+ "values('" + userName2 + "', '" + commandLine + "', " 
				+ reqStatus + ", '" + returnMsg + "')";
		
		stmt.executeUpdate(sql);
		conn.commit();
	}

	public ArrayList<String> selectHistory(int historyLines) throws SQLException {
		
		ArrayList<String> historyList = new ArrayList<String>();
		
		if(historyLines == -1){  // 全部的历史记录
			sql = "select user_name, command, status, rtmsg from history";
		} else {
			sql = "select top " + historyLines + " user_name, command, status, rtmsg from history";
		}
		
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()){
//			System.out.println(rs.getString(0)); 
			String historyLine = " user_name：" + rs.getString("user_name").trim() + "; " 
							+ "commandLine：" + rs.getString("command") +  "; "
							+ "command_status：" + rs.getString("status")+ "。";
			historyList.add(historyLine);
		}
		
		return historyList;
	}

	
}
