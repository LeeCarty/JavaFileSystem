

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;

public class ServerHandle extends Thread {
	/*
	 * description : do what? 
	 * members : what members does it have?
	 * function : What methods does it provide?
	 */

	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private int reqStatus;
	private String returnMsg;
	private String user;
	private DBManager dbm;
	
	public ServerHandle(Socket socket) throws IOException, ClassNotFoundException, SQLException {
		super();
		this.socket = socket;
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.reqStatus = 0;
		this.returnMsg = "";
		this.dbm = new DBManager();
	}
	
	@Override
	public void run() {
		try {
			handleScoket();
		} 
		catch(SocketException e) {
			closeAll();
//			System.out.println("SocketException.  客户端（port: " + socket.getPort() + ")  断开连接。" + e.getMessage());
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void closeAll() {
		try {
			this.dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleScoket() throws IOException, ClassNotFoundException, SQLException  {
		
		while (true) {
			int reqMode = 0;
			try {
				reqMode = dis.readInt();
			} catch (EOFException e) {
				System.out.println("EOFException. 客户端（port: " + socket.getPort() + ")  断开连接。" + e.getMessage());
				break;
			}
			
			switch(reqMode) {
				case 1:
					loginHandle();
					break;
				case 3:
					getHistory();
					break;
				case 4:
					// 发送文件
					sendFile();
					break;
				case 5:
					uploadFile();
					break;
				case 6:
					deleteFile();
					break;
				case 7:
					getFileList();
					break;
				default:
					break;
			}
			
		}
		
	}

	private void getHistory() throws IOException, ClassNotFoundException, SQLException {
		
//		DBManager dbm = new DBManager();
		
		String commandLine = dis.readUTF();
		int historyLines = dis.readInt();
		
		ArrayList<String> historyList = new ArrayList<String>();
		
		historyList = dbm.selectHistory(historyLines);
		
		setReqStatus(3);
		setReturnMsg("");
		
		int len = historyList.size();
		dos.writeInt(len);
		for(int i = 0; i < len; i++){
			dos.writeUTF(historyList.get(i));
		}
		if(len == 0){
			setReturnMsg("无历史记录！");
		}
		
//		dbm.closeConnection();
	}

	private void loginHandle() throws IOException, ClassNotFoundException, SQLException {
		
		String commandLine = dis.readUTF();
		String userName = dis.readUTF();
		String psd = dis.readUTF();
		
		setUser(userName);
//		DBManager dbm = new DBManager();
		
		setReqStatus(1);
		setReturnMsg("用户：" + userName + " 登录成功！");
		
		if (!dbm.checkUser(userName, psd)) {
			setReqStatus(-1);
			setReturnMsg("错误的用户名或密码！");
		} 
		
		dos.writeInt(getReqStatus());
		dos.writeUTF(getReturnMsg());
		dos.flush();
		
		dbm.createHistory(userName, commandLine, getReqStatus(), getReturnMsg());
//		dbm.closeConnection();
	}

	private void getFileList() throws IOException, ClassNotFoundException, SQLException {
		
//		DBManager dbm = new DBManager();
		
		String commandLine = dis.readUTF();
		String targetPF1 = dis.readUTF();
		
		File file = new File(targetPF1);
		// 如果targetPF1不是路径，报错返回
		if(!file.isDirectory()){
			setReqStatus(-7); 
			setReturnMsg(targetPF1 + " 不是一个合法的路径！");
			dos.writeInt(getReqStatus());
			dos.writeUTF(getReturnMsg());
			dos.flush();
			dbm.createHistory(getUser(), commandLine, getReqStatus(), getReturnMsg());
			return;
		} else {
			// 是合法路径
			setReqStatus(7); 
			setReturnMsg("");
			dos.writeInt(7);
			dos.flush();
		}
		
		// 获取 fileNameList
		ArrayList<String> fileList = new ArrayList<String>();
		File[] lst = file.listFiles();
		for(int i = 0; i < lst.length; i++){
			if(lst[i].isFile()){
				fileList.add(lst[i].getName());
			}
		}
		
		int fileNum = fileList.size();
		// 发送 fileNum 给客户端
		dos.writeInt(fileNum);
		
		// 发送 fileNameList
		for(int i = 0; i < fileNum; i++){
			dos.writeUTF(fileList.get(i));
		}
		dos.flush();
		
		dbm.createHistory(getUser(), commandLine, getReqStatus(), getReturnMsg());
//		dbm.closeConnection();
	}

	private void uploadFile() throws IOException, ClassNotFoundException, SQLException {

//		DBManager dbm = new DBManager();
		
		String commandLine = dis.readUTF();
		int uploadMode = dis.readInt();
		String targetPF = dis.readUTF();
		
		// 源文件是否存在
    	int reqStatus = dis.readInt();
    	if (reqStatus != 5) {
    		String returnMsg = dis.readUTF();
    		dbm.createHistory(getUser(), commandLine, reqStatus, returnMsg);
    		return;
    	}
    	
		File uploadFile = new File(targetPF); 
		// 目标文件检测
		if (uploadMode == 1 && uploadFile.exists()){
			setReqStatus(-5);
			setReturnMsg("文件已存在，无法完成upload操作！请使用 upload -f 重试！");
			dos.writeInt(getReqStatus());
			dos.writeUTF(getReturnMsg());
			dbm.createHistory(getUser(), commandLine, getReqStatus(), getReturnMsg());
			return;
		} else {
			setReqStatus(5);
			setReturnMsg("");
			dos.writeInt(5);
		}
    	
    	// 文件接收
    	int fileLength = (int)dis.readLong();
    	FileOutputStream fos = new FileOutputStream(uploadFile);
    	
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
//    	System.out.println("----文件接收完成--[File Name：" + uploadFile.getName() + ", Length：" + fileLength + " b.] ----");
    	fos.close();
    	
    	dbm.createHistory(getUser(), commandLine, getReqStatus(), getReturnMsg());
//		dbm.closeConnection();
	}

	private void deleteFile() throws IOException, ClassNotFoundException, SQLException {
		
//		DBManager dbm = new DBManager();
		
		String commandLine = dis.readUTF();
		String targetPF = dis.readUTF();
		
		setReqStatus(6);
		setReturnMsg("文件删除成功！");
		
		File deleteFile = new File(targetPF);
		if (!deleteFile.exists()){
			setReqStatus(-6);
			setReturnMsg("目标文件不存在！");
		}
		else if (!deleteFile.delete()){
			setReqStatus(-6);
			setReturnMsg("删除文件时出现错误！");
		}
		dos.writeInt(getReqStatus());
		dos.writeUTF(getReturnMsg());
		dos.flush();
		
		dbm.createHistory(getUser(), commandLine, getReqStatus(), getReturnMsg());
//		dbm.closeConnection();
	}

	private void sendFile() throws IOException, SQLException, ClassNotFoundException {
		
//		DBManager dbm = new DBManager();
		
		String commandLine = dis.readUTF();
		String sourcePF = dis.readUTF();
		
		File file = new File(sourcePF);
		// 检测源文件是否存在
		if (!file.exists()) {
			setReqStatus(-4);
			setReturnMsg(sourcePF + ": 源文件不存在！");
			dos.writeInt(getReqStatus());
			dos.writeUTF(getReturnMsg());
			dos.flush();
			dbm.createHistory(getUser(), commandLine, getReqStatus(), getReturnMsg());
			return;
		} else {
			setReqStatus(4);
			setReturnMsg("开始传输文件！");
			dos.writeInt(getReqStatus());
			dos.writeUTF(getReturnMsg());
			dos.flush();
		}
		
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
//			System.out.println("|  " + (100 * progress/fileLength) + "%  |");
		}
//		System.out.println("----文件传输完成----");
		inputFile.close();
		
		setReturnMsg("文件传输完成！");
		dbm.createHistory(getUser(), commandLine, getReqStatus(), getReturnMsg());
//		dbm.closeConnection();
	}

	public int getReqStatus() {
		return reqStatus;
	}

	public void setReqStatus(int reqStatus) {
		this.reqStatus = reqStatus;
	}

	public String getReturnMsg() {
		return returnMsg;
	}

	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	
}
