import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Formatter;
import java.util.Properties;

public class InitUtenti 
{
	private static Connection connection = null;
	private static final String PROP_FILE_NAME = "config.properties";
	
	public static void main(String[] args) 
	{
		if(args.length != 3)
		{
			System.out.println("Usage: param 1: userName, param 2: password, param 3: baseDir");
		}
		else
		{
			openConnection();
			
			try
			{
				insertUtente(args[0], args[1], args[2]);
			}
			catch(Exception e)
			{
				System.out.println("Errore in insertUtente() " + e.getMessage());
			}
			
			closeConnection();
		}
	}

	protected static void insertUtente(String userName, String password, String baseDir) throws Exception
	{
		String queryInsertUtente = getProperty("query.insertUtente");
		PreparedStatement statement = connection.prepareStatement(queryInsertUtente);
		statement.setString(1, userName);
		statement.setString(2, getPasswordHashCode(password));
		statement.setString(3, baseDir);
		statement.executeUpdate();
	}

	protected static String getPasswordHashCode(String password) throws Exception
	{
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		char random = (char)(Math.random() * 128);
		String pwd = new String(password + random);
		md.digest(pwd.getBytes());
		byte[] data = md.digest();
		return byteArray2Hex(data);
	}	
	
	protected static String byteArray2Hex(byte[] hash) 
	{
	    Formatter formatter = new Formatter();
	    
	    for(byte b : hash) 
	    {
	        formatter.format("%02x", b);
	    }
	    
	    String result = formatter.toString();
	    formatter.close();
	    return result;
	}
	
	protected static String getProperty(String key)
	{
		Properties prop = new Properties();
		InputStream input = null;
		String value = null;

		try
		{
			input = InitUtenti.class.getClassLoader().getResourceAsStream(PROP_FILE_NAME);
			
			if(input == null)
			{
				System.out.println("File di properties non trovato " + PROP_FILE_NAME);
				return null;
			}

			prop.load(input);
			value = prop.getProperty(key);
		}
		catch(IOException e)
		{
			System.out.println("Errore di lettura dal file di properties: " + e.getMessage());
		}
		finally
		{
			if(input != null)
			{
				try 
				{
					input.close();
				} 
				catch(IOException e) 
				{
					System.out.println("Errore di chiusura input stream: " + e.getMessage());
				}
			}
		}
		
		return value;
	}
	
	protected static String decodeBase64(String enc)
	{
		byte[] decodedBytes = Base64.getDecoder().decode(enc);
		return new String(decodedBytes);
	}
	
	protected static void openConnection()
	{
		if(connection == null)
		{
			try 
			{
				Class.forName("com.mysql.jdbc.Driver");
				
				String connectionString = getProperty("connectionString");
				String userName = decodeBase64(getProperty("userName"));
				String password = decodeBase64(getProperty("password"));
				
				connection = DriverManager.getConnection(connectionString, userName, password);
				System.out.println("Connessione riuscita");
			}
			catch(SQLException e) 
			{
				System.out.println("Errore di connessione: " + e.getMessage());
			} 
			catch(ClassNotFoundException e) 
			{
				System.out.println("Errore di connessione: " + e.getMessage());
			}
		}
	}
	
	protected static void closeConnection()
	{
		if(connection != null)
		{
			try 
			{
				connection.close();
				connection = null;
				System.out.println("Connessione chiusa");
			} 
			catch(SQLException e) 
			{
				System.out.println("Errore di chiusura connessione: " + e.getMessage());
			}
		}
	}
}
