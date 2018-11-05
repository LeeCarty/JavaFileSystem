

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Properties;

/*
 *	Author : LeeCarty
 *	Date   : 2018年8月27日, 下午10:54:35
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
    	System.out.println(" 等待客户端连接请求中...");
    	
    	while(true) {
    		// server 尝试接受客户端socket的请求，accept()是阻塞式的
    		Socket socket = server.accept();
    		
//    		new Thread(new TaskHandle(socket)).start();  // 这种方式建立的线程，线程类实现了Runnable接口
    		ServerHandle th = new ServerHandle(socket);  // 这种方式建立的线程继承自Thread
    		th.start();
    		
    	}
    	
    }
    
}
