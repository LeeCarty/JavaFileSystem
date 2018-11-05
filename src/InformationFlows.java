
import java.io.IOException;

/***
 *	Author : LeeCarty
 *	Date   : 2018��9��19��, ����9:16:42
 */

public class InformationFlows{
	
	/***
	 * default 0; 1--login; 2--register(δʵ��);  3--history;  4--download;  
	 * 			  5--upload;  6--delete;  7--ls;  8--help;  10--quit/q;
	 * 
	 * error-status: -1--login error;  -2--register error(δʵ��); -3--history error;  
	 * 				 -4--download error;  -5--upload error;  -6--delete error;  
	 * 				 -7--ls error;  -100 -- ��Ч�����
	 */
	private int			commandMode;			
	private String		commandLine;
	
	// login members
	private String		host;
	private int			port;
	private String		userName;
	private String		psd;
	
	private int 		uploadMode;    // 0--�ļ�����ʱ���ǣ�  1--�ļ�����ʱ�����ǡ�
	private String 		sourcePF;
	private String		targetPF;
	
	private int			historyLines;		// default -1 -- ���������� 
	
	private String		errorMsg;
	
	
	public InformationFlows()
	{
		this.setCommandMode(0);
		this.setCommandLine("");
		this.setSourcePF("");
		this.setTargetPF("");
		this.setHistoryLines(-1);
		this.setErrorMsg("");
	}
	
	/***
	 * ���������У�
	 * @param commandLine
	 * @return
	 * @throws IOException
	 */
	public int stringHandle(String commandLine) throws IOException {
		
		this.setCommandLine(commandLine);
		
		if("q".equals(commandLine) || "quit".equals(commandLine))
		{
			this.setCommandMode(10);
		}
		else if("help".equals(commandLine))
		{
			this.setCommandMode(8);
		}
		else if (commandLine.startsWith("login")) 
		{
			this.setCommandMode(1);
			
			String dlPF = commandLine.substring(5).trim();
			int beginIndex = dlPF.indexOf(":");
			String host = dlPF.substring(0, beginIndex);
			this.setHost(host); 
			
			String subStr = dlPF.substring(beginIndex + 1).trim();
			beginIndex = subStr.indexOf("@");
			int port = Integer.parseInt(subStr.substring(0, beginIndex));
			this.setPort(port);
			
			subStr = subStr.substring(beginIndex + 1).trim();
			beginIndex = subStr.indexOf("/");
			String name = subStr.substring(0, beginIndex);
			this.setUserName(name);
			
			subStr = subStr.substring(beginIndex + 1).trim();
			this.setPsd(subStr);
			
		}
		else if (commandLine.startsWith("download"))
		{
			// �������� download d:\path1\file1  e:\path2  �����ǰ����Ŀ���ļ�·�����ļ�����������Ŀ��·��
			this.setCommandMode(4);
			
			String dlPF = commandLine.substring(8).trim();
			int beginIndex = dlPF.indexOf(" ");
			if (beginIndex == -1) {  			// ���download��������ַ���������d:
				this.setCommandMode(-4);
				this.setErrorMsg("download��������޷�������Դ·����Ŀ��·����");
				return -1;
			}
			String sourcePF = dlPF.substring(0, beginIndex).trim();
			this.setSourcePF(sourcePF);
			
			String targetPF = dlPF.substring(beginIndex).trim();
			this.setTargetPF(targetPF);
			
		}
		else if (commandLine.startsWith("upload"))
		{
			// �������磺upload d:\path3 e:\path  or upload d:\file3  e:\file3  ������
			this.setCommandMode(5);
			
			String pfStr = commandLine.substring(6).trim();
			String tempStr = pfStr;
			if(pfStr.startsWith("-f")){
				this.setUploadMode(0);
				tempStr = pfStr.substring(2).trim();
			} else {
				this.setUploadMode(1);
			}
			
			int beginIndex = tempStr.indexOf(" ");
			if (beginIndex == -1 || tempStr == null) {  			// ���download��������ַ���������d:
				this.setCommandMode(-5);
				this.setErrorMsg("upload��������޷�������Դ·����Ŀ��·����");
				return -1;
			}
			
			String sourcePF = tempStr.substring(0, beginIndex).trim();
			if (sourcePF.indexOf(" ") != -1) {
				return -1;
			}
			this.setSourcePF(sourcePF);
			
			String targetPF = tempStr.substring(beginIndex).trim();
			this.setTargetPF(targetPF);
			
			
		}
		else if (commandLine.startsWith("delete"))
		{
			// �������� delete targetPF(d:\file) ������
			this.setCommandMode(6);
			
			String targetPF = commandLine.substring(6).trim();
			if (targetPF.indexOf(" ") != -1) {      // ���targetPF���пո���������˳�
				this.setCommandMode(-6);
				this.setErrorMsg("delete��������޷�������Ŀ��·�����ļ���");
				return -1;
			}
			this.setTargetPF(targetPF);	
			
		}
		else if (commandLine.startsWith("ls"))
		{
			String targetPF = commandLine.substring(2).trim();
			if(targetPF.indexOf(" ") != -1){
				this.setCommandMode(-7);
				this.setErrorMsg("ls ��������޷�������Ŀ��·����");
				return -1;
			}
			
			this.setCommandMode(7);
			this.setTargetPF(targetPF);
		}
		else if (commandLine.startsWith("history"))
		{
			this.setCommandMode(3);
			String lines = commandLine.substring(7).trim();
			
			setHistoryLines(-1);
			
			if(lines.startsWith("-n")){
				String temp = lines.substring(2).trim();
				if(temp.indexOf(" ") != -1){
					this.setCommandMode(-3);
					this.setErrorMsg("history �����������");
					return -1;
				}
				
				int historyLines = Integer.parseInt(temp);
				setHistoryLines(historyLines); 
			}
			
		}
		else 
		{
			this.setCommandMode(-100);
			this.setErrorMsg(commandLine + " -- ��Ч�����");
			return -1;
		}
		
		return 0;
	}
	
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPsd() {
		return psd;
	}

	public void setPsd(String psd) {
		this.psd = psd;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}

	public int getHistoryLines() {
		return historyLines;
	}

	public void setHistoryLines(int historyLines) {
		this.historyLines = historyLines;
	}

	public int getCommandMode() {
		return commandMode;
	}

	public void setCommandMode(int commandMode) {
		this.commandMode = commandMode;
	}

	public String getSourcePF() {
		return sourcePF;
	}

	public void setSourcePF(String sourcePF) {
		this.sourcePF = sourcePF;
	}

	public String getTargetPF() {
		return targetPF;
	}

	public void setTargetPF(String targetPF) {
		this.targetPF = targetPF;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public int getUploadMode() {
		return uploadMode;
	}

	public void setUploadMode(int uploadMode) {
		this.uploadMode = uploadMode;
	}
	
	
}
