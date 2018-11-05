import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/***
 * 
 * @author LeeCarty
 *
 */
public class JFSProperties {
	
	private static Properties properties = null;
	
	/***
	 * get Properties object
	 * @return
	 */
	public Properties getProperties() {
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.properties");
		
		Properties properties = new Properties();
		
		try {
			properties.load(is);
		} catch (IOException e) {
			System.out.println("Properties load() error! " + e.getMessage());
		}
		
		try {
			is.close();
		} catch (IOException e) {
			System.out.println("InputStream close() error! " + e.getMessage());
		}
		
		return properties;
	}
		
	/***
	 *  just for test
	 * @param argu
	 * @throws IOException
	 */
	public static void main(String[] argu) throws IOException
	{
		JFSProperties p = new JFSProperties();
		properties = p.getProperties();
		System.out.println("default_host:" + properties.getProperty("default_host"));
		System.out.println("default_port:" + properties.getProperty("default_port"));
	}

	
}
