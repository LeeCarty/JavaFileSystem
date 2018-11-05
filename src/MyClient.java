

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
 
public class MyClient {
	/*
	 * description : create client
	 * members : what members does it have?
	 * function : What methods does it provide?
	 */
	
	static Socket clientSocket = null;
	private static DataInputStream dis;
	private static DataOutputStream dos;
	private static int reqMode;
	private static boolean loginStatus;
	
	static InformationFlows request = new InformationFlows();
	static int rt = 0;
	private static boolean endFlag = true;
	
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
    	
    	Properties properties = (new JFSProperties()).getProperties();
		
		String host = properties.getProperty("default_host");  // server host
    	int port = Integer.parseInt(properties.getProperty("default_port")); // server port
    	String commandLine = "";
    	
    	clientSocket = new Socket(host, port);
    	dos = new DataOutputStream(clientSocket.getOutputStream());
        dis = new DataInputStream(clientSocket.getInputStream());
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
    	
		while (!loginStatus) 
		{
			System.out.print(">>");
			commandLine = br.readLine().toLowerCase(); // "login 127.0.0.1:8899@admin/1"; 
    		request.stringHandle(commandLine);
    		reqMode = request.getCommandMode();
    		
    		switch(reqMode)
    		{
	    		case 1:
	    			int rtStatus = loginHandle(commandLine);
	    			if(rtStatus == 0)
	    			{
	    				loginStatus = true;
	    			}
	    			break;
	    		case 3:
	    		case 4:
	    		case 5:
	    		case 6:
	    		case 7:
	    			System.out.println("未登录!无法使用文件操作命令！");
	    			break;
	    		case 8:
	    			printHelpMessage();
	    			break;
	    		case 10:
	    			return;
	    		case -100:
	    			System.out.println((new Timestamp(new Date().getTime())).toString() + request.getErrorMsg());
	    			break;
	    		default:
	    			break;
    		}
    	}
    	
        while(endFlag ) {
        	System.out.print(">>");
        	commandLine = br.readLine().toLowerCase();
    		request.stringHandle(commandLine);
    		
    		reqMode = request.getCommandMode();
    		
    		// 发送请求类型给服务端
    		dos.writeInt(reqMode);
    		
    		switch(reqMode)
    		{
	    		case 3:
	    			printHistory();
	    			break;
	    		case 4:
	    			downloadHandle();
	    			break;
	    		case 5:
	    			uploadHandle(request.getUploadMode());
	    			break;
	    		case 6:
	    			deleteHandle();
	    			break;
	    		case 7:
	    			fileListHandle();
	    			break;
	    		case 8:
	    			printHelpMessage();
	    			break;
	    		case 10:
	    			endFlag = false;
	    			break;
	    		case -3:
	    		case -4:
	    		case -5:
	    		case -6:
	    		case -7:
	    		case -100:
	    			System.out.println((new Date()).toString() + ". [Error]：" + request.getErrorMsg());
	    			break;
	    		
    			default:
    				break;
    		}
    	}	
		if (clientSocket != null) 
		{
			System.out.println("断开服务器连接！");
			clientSocket.close();
			dis.close();
			dos.close();
		}
		System.out.println("客户端已退出！");
    }

	
	private static int loginHandle(String commandLine) throws IOException {

		dos.writeInt(reqMode);
		dos.writeUTF(commandLine);
		dos.writeUTF(request.getUserName());
		dos.writeUTF(request.getPsd());
		dos.flush();
		
		// 接收 登录验证信息
		int reqStatus = dis.readInt();
		String returnMsg = dis.readUTF();
		System.out.println(returnMsg);
		
		if (reqStatus != 1) {
			return -1;
		}
		return 0;
	}


	private static void printHistory() throws IOException {
		
		dos.writeUTF(request.getCommandLine());
		dos.writeInt(request.getHistoryLines());
		
		int len = dis.readInt();
		
		for(int i = 0; i < len; i++){
			System.out.println(dis.readUTF());
		}
		System.out.println("一共  " + len + " 条  操作历史记录。");
	}


	private static void fileListHandle() throws IOException {
		
		dos.writeUTF(request.getCommandLine());
		dos.writeUTF(request.getTargetPF());
		// server端 路径检测结果
		if(dis.readInt() != 7){
			System.out.println(dis.readUTF());
			return;
		}
		
		// 接收 fileNum
		int fileNum = dis.readInt();
		if(fileNum == 0){
			System.out.println("There are no files in this directory.");
		}
		
		// 接收 fileNameList
		for(int i = 0; i < fileNum; i++){
			System.out.println(dis.readUTF());
		}
		
	}


	private static void uploadHandle(int i) throws IOException {
		
		dos.writeUTF(request.getCommandLine());
		dos.writeInt(i);
		dos.writeUTF(request.getTargetPF());
		
		// 源文件存在检测，并发送检测结果码
		File file = new File(request.getSourcePF());
		if (!file.exists()) {
			dos.writeInt(-5);
			dos.writeUTF("源文件不存在！");
			dos.flush();
			System.out.println("upload操作失败！源文件不存在！");
			return;
		} else {
			dos.writeInt(5);
		}
		
		// 如果server端检测到目标文件已存在，并且uploadMode=1，报错退出
		int fileStatus = dis.readInt();
		if (i == 1 && fileStatus == -5){
			System.out.println(dis.readUTF());
			return;
		}
		
		// 文件传输
		InputStream inputFile = new FileInputStream(file);
		long fileLength = file.length();
		dos.writeLong(fileLength);
		
		// 文件内容传输
		byte[] bytes = new byte[1024];
		int len = 0;
//		long progress = 0;
		while((len = inputFile.read(bytes, 0, bytes.length)) != -1) {
			dos.write(bytes, 0, len);
			dos.flush();
//			progress += len;
		}
		System.out.println("----文件传输完成----");
		inputFile.close();
		
	}


	private static void deleteHandle() throws IOException {
		
		dos.writeUTF(request.getCommandLine());
		dos.writeUTF(request.getTargetPF());
		
		int reqStatus = dis.readInt();
		String returnMsg = dis.readUTF();
		
		System.out.println(returnMsg);
		return;
	}


	private static void downloadHandle() throws IOException {
		
    	dos.writeUTF(request.getCommandLine());
		dos.writeUTF(request.getSourcePF());
    	
    	int reqStatus = dis.readInt();
    	String returnMsg = dis.readUTF();
    	if (reqStatus != 4) {
    		System.out.println(returnMsg);
    		return;
    	}
    	
    	int fileLength = (int) dis.readLong();
    		
    	File file = new File(request.getTargetPF());
    	
    	FileOutputStream fos = new FileOutputStream(file);
    	
    	byte[] bytes = new byte[1024];
    	int len = 0;
    	int getLen = 0;
    	while ((len = dis.read(bytes, 0, bytes.length)) != -1) {
    		fos.write(bytes, 0, len);
    		getLen += len;
    		fos.flush();
    		if (getLen == fileLength){
    			break;
    		}
    	}
    	System.out.println("----文件接收完成--[File Name：" + file.getName() 
    	+ ", Length：" + fileLength + " b.] ----");
    	fos.close();
    	return;
	}
	

	private static void printHelpMessage() {
		// TODO Auto-generated method stub
		System.out.println("help message: to find what you want to do......");
		System.out.println("\thelp\tprint help messages.");
		System.out.println("\tq\texit the client.");
		System.out.println("\tquit\texit the client.");
		System.out.println("\tlogin ip:port@user/password\tlogin");
		System.out.println("\tdownload /data/data.txt d:/test/data.txt");
		System.out.println("\t\tdownload file:data.txt from server, error when file not exists.");
		System.out.println("\tupload [-f] d:/test/data.txt /data/data.txt");
		System.out.println("\t\tupload file:data.txt from server,");
		System.out.println("\t\twhen -f exists, cover existed file or error when file not exists.");
		System.out.println("\tdelete /data/data.txt");
		System.out.println("\t\tdelete file:data.txt from client to server, error when file not exists.");
		System.out.println("\tls /data\topen the directory.");
		System.out.println("\thistory [-n 100]\t-n 100 means the number of history is specified. ");
	}
    

}
