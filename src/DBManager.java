
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

/***
 *	Author : LeeCarty
 *	Date   : 2018��10��8��, ����9:48:53
 */

public class DBManager {
	
	// ���ݿ��ַ�������
//	private String jdbcDriverString = "jdbc:mysql://localhost:3306/jfstable?useSSL=false&serverTimezone=UTC";
//	private String jdbcDriverString = "jdbc:mysql://localhost:3306/jfstable"
//			+ "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
		
	private String jdbcDriver;
	private String user;
	private String psd;
	
	static String sql = ""; // sql ��
	
	static Connection conn = null;  // ȫ������
	
	static Statement stmt = null;  // ȫ�������
	
	public DBManager() throws ClassNotFoundException, SQLException{	
		// �����������������Զ��ύ������
		//		Class.forName(jdbcDriverString); // ��������
		//		conn = DriverManager.getConnection(urlString, userName, password); // ��������
		
		Properties p = (new JFSProperties()).getProperties();
		jdbcDriver = p.getProperty("mysql_driver");
		user = p.getProperty("mysql_user");
		psd = p.getProperty("mysql_psd");
		
		
		conn = DriverManager.getConnection(jdbcDriver, user, psd);
		conn.setAutoCommit(false); // ���÷��Զ��ύ
		
		stmt = conn.createStatement(); // ���� �����
		
		// ��ʼ��ϵͳ��
		// table: file_user ��  history
		try{
			sql = "create table file_user("
					+ "id int identity(1, 1) not null, "
					+ "name char(20) primary key, "
					+ "psd varchar(20) not null, "
					+ "create_time datetime(6) default getdate(), "
					+ "last_login_time datetime(6) "
					+ ");";
			stmt.addBatch(sql);
			
			sql = "create table history("
					+ " id int identity(1, 1) primary key,"
					+ " user_name char(20) foreign key references file_user(name), "
					+ " command varchar not null,"
					+ " status int not null,"
					+ " rtmsg varchar not null,"
					+ " create_time datetime(6) default getdate());";
			stmt.addBatch(sql);
			stmt.executeBatch();
			
			stmt.executeUpdate("insert into file_user(name, psd) values('admin', '1');");
			stmt.executeUpdate("insert into file_user(name, psd) values('leecarty', '111111');");
			
			conn.commit();
		} catch (Exception e){
			
		}
		
		// MySQL ��  ������䣺
//		CREATE TABLE `file_user` (
//				  `id` int(11) NOT NULL AUTO_INCREMENT,
//				  `name` char(20) NOT NULL,
//				  `psd` varchar(20) NOT NULL,
//				  `create_time` datetime(6) DEFAULT NULL,
//				  `last_login_time` datetime(6) DEFAULT NULL,
//				  PRIMARY KEY (`id`)
//				) ;
//
//				create table history
//				(
//				id int primary key auto_increment, 
//				user_id int(11),
//				command varchar(30),
//				status int,
//				rtmsg varchar(50),
//				create_time datetime(6),
//				foreign key (user_id) references file_user(id)
//				);
	}
	
	public void closeConnection() throws SQLException{
		stmt.close();
		conn.close();
	}
	
	public boolean checkUser(String userName, String psd) throws SQLException {
		// ��֤ ��¼�û�
		sql = "select * from file_user where name = '" + userName + "' and psd = '" + psd + "'"; 
		
		ResultSet rs = stmt.executeQuery(sql);

		if (!rs.next()){
			return false; // �޽�� ����false
		}
		
		// ���� �û����һ�εĵ�¼ʱ��
		sql = "update file_user set last_login_time = '" + 
				(new Timestamp(new Date().getTime())).toString() + 
				"' where name = '" + userName + "' and psd = '" + psd + "'";
		stmt.executeUpdate(sql);
		conn.commit();
		
		return true;
	}

	public void createHistory(String userName2, String commandLine, int reqStatus, String returnMsg) throws SQLException {
		// ����һ��������ʷ��¼
		sql = "insert into history(user_name, command, status, rtmsg) "
				+ "values('" + userName2 + "', '" + commandLine + "', " 
				+ reqStatus + ", '" + returnMsg + "')";
		
		stmt.executeUpdate(sql);
		conn.commit();
	}

	public ArrayList<String> selectHistory(int historyLines) throws SQLException {
		
		ArrayList<String> historyList = new ArrayList<String>();
		
		if(historyLines == -1){  // ȫ������ʷ��¼
			sql = "select user_name, command, status, rtmsg from history";
		} else {
			sql = "select top " + historyLines + " user_name, command, status, rtmsg from history";
		}
		
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()){
//			System.out.println(rs.getString(0)); 
			String historyLine = " user_name��" + rs.getString("user_name").trim() + "; " 
							+ "commandLine��" + rs.getString("command") +  "; "
							+ "command_status��" + rs.getString("status")+ "��";
			historyList.add(historyLine);
		}
		
		return historyList;
	}

	
}
