

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Properties;

/*
 *	Author : LeeCarty
 *	Date   : 2018��8��27��, ����10:54:35
 */
 
public class MyServer {
	/*
	 * description : create server 
	 * members : what members does it have?
	 * function : What methods does it provide?
	 */	
	
	private static int server_port ;
	
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
    	
    	Properties properties = (new JFSProperties()).getProperties();
    	server_port = Integer.parseInt(properties.getProperty("default_port"));
    	@SuppressWarnings("resource")
		ServerSocket server = new ServerSocket(server_port);
    	System.out.println(" �ȴ��ͻ�������������...");
    	
    	while(true) {
    		// server ���Խ��ܿͻ���socket������accept()������ʽ��
    		Socket socket = server.accept();
    		
//    		new Thread(new TaskHandle(socket)).start();  // ���ַ�ʽ�������̣߳��߳���ʵ����Runnable�ӿ�
    		ServerHandle th = new ServerHandle(socket);  // ���ַ�ʽ�������̼̳߳���Thread
    		th.start();
    		
    	}
    	
    }
    
}
