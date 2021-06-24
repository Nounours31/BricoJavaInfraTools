package ce.sections.mySQL.codeSample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ce.sections.brico.batch.infra.SBricoException;


public class SBricoMySQLFacade 
{
	private static Connection _connect = null;

	public void openDB() throws Exception 
	{
		if (_connect == null)
		{
			try 
			{
				Class.forName("com.mysql.jdbc.Driver");
				_connect = DriverManager.getConnection("jdbc:mysql://localhost/sbrico_restoration?"
						+ "user=root&password=");
				// + "user=root&password=sqluserpw");

			} 
			catch (Exception e) 
			{
				throw e;
			}
		}
	}

	// close DB
	public void closeDB()
	{
		try {
			if (_connect != null)
				_connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally
		{
			_connect = null;
		}
	}


	// SELECT
	public Document selectDB (String sql) throws Exception 
	{
		Statement statement = null;
		ResultSet resultSet = null;

		try 
		{
			if (_connect == null)
				throw new SBricoException("Open DB first");
			Connection connect = _connect;
			statement = connect.createStatement();
			resultSet = statement.executeQuery(sql);
			return writeResultSetToXML(resultSet);
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}
		}

	}


	// insert
	public void insertDB(String tableName, String[] coloneName, String[] ColonesValues) throws Exception 
	{
		// insert into table (c1,c2) values (v1, v2) 
		executeUpdateDB ("insert", tableName, coloneName,  ColonesValues);
	}

	// update
	public void updateDB(String tableName, String[] coloneName, String[] ColonesValues) throws Exception 
	{
		// update table set c1=v1, c2=v2 
		executeUpdateDB ("update",  tableName, coloneName, ColonesValues);
	}

	// delete
	public void deleteDB(String tableName, String[] coloneName, String[] ColonesValues) throws Exception 
	{
		// delete from table where ((c1=v1) and (c2=v2))
		executeUpdateDB ("delete",  tableName,  coloneName,  ColonesValues);
	}

	// INSERT, UPDATE or DELETE
	private void executeUpdateDB(String type, String tableName, String[] coloneName, String[] ColonesValues) throws Exception 
	{
		PreparedStatement preparedStatement = null;

		try 
		{
			StringBuilder sb = new StringBuilder();
			if (_connect == null)
				throw new SBricoException("Open DB first");
			Connection connect = _connect;

			if (type.equals("insert"))
			{
				// insert into table (c1,c2) values (v1, v2) 
				StringBuilder sbValues = new StringBuilder(" (");
				StringBuilder sbColones = new StringBuilder(" (");
				for (int i = 0; i < coloneName.length; i++)
				{
					sbColones.append(coloneName[i]);
					sbValues.append("?");
					if (i < (coloneName.length - 1))
					{
						sbColones.append(",");
						sbValues.append(",");
					}
				}
				sbColones.append(") ");
				sbValues.append(") ");

				sb.append("insert into ");
				sb.append(tableName);
				sb.append(sbColones.toString() + " values ");
				sb.append(sbValues.toString());
			}
			else if (type.equals("update"))
			{		
				// update table set c1=v1, c2=v2 
				StringBuilder sbValues = new StringBuilder(" ");
				for (int i = 0; i < coloneName.length; i++)
				{
					sbValues.append(coloneName[i] + "=?");
					if (i < (coloneName.length - 1))
					{
						sbValues.append(",");
					}
				}	
				sb.append("update  ");
				sb.append(tableName + " set ");
				sb.append(sbValues.toString());
			}
			else if (type.equals("delete"))
			{
				// delete from table where ((c1=v1) and (c2=v2))
				StringBuilder sbValues = new StringBuilder("(");
				for (int i = 0; i < coloneName.length; i++)
				{
					sbValues.append("(" + coloneName[i] + "=?)");
					if (i < (coloneName.length - 1))
					{
						sbValues.append(" and ");
					}
				}
				sbValues.append(")");

				sb.append("delete from ");
				sb.append(tableName);
				sb.append(" where " + sbValues.toString());
			}
			preparedStatement = connect.prepareStatement(sb.toString());
			for (int i = 0; i < ColonesValues.length; i++)
				preparedStatement.setString(i+1, ColonesValues[i]);
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			throw e;
		}
		finally {
			preparedStatement.close();
		}
	}

	// INSERT, UPDATE or DELETE
	public long executeUpdateDB(String sql) throws Exception 
	{
		long uidRC = -1;
		PreparedStatement preparedStatement = null;

		try 
		{
			if (_connect == null)
				throw new SBricoException("Open DB first");
			Connection connect = _connect;

			preparedStatement = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			int affectedRows = preparedStatement.executeUpdate();	
			if (affectedRows == 0) {
				throw new SQLException("Creating user failed, no rows affected.");
			}

			try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) 
			{
				if (generatedKeys.next()) 
				{
					uidRC = generatedKeys.getLong(1);
				}
				else 
				{
					throw new SQLException("Creating user failed, no ID obtained.");
				}
			}
		} 
		catch (Exception e) 
		{
			System.out.println(sql);
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		finally {
			preparedStatement.close();
		}
		this.close();
		return uidRC;
	}




	private Document writeResultSetToXML(ResultSet resultSet) throws SQLException, ParserConfigurationException 
	{
		Document document = null;
		DocumentBuilderFactory fabrique = null;
		fabrique = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = fabrique.newDocumentBuilder();
		document = builder.newDocument();
		Element racine = (Element) document.createElement("SQL");

		if (resultSet == null)
			return document;

		ResultSetMetaData metadata = resultSet.getMetaData();
		int nbCol = metadata.getColumnCount();
		int nbRow = 0;
		while (resultSet.next()) 
		{
			Element row = document.createElement("row");
			nbRow++;
			for (int iIndice = 1; iIndice <= nbCol; iIndice++)
			{
				String val = "";
				int colType  = metadata.getColumnType(iIndice);
				switch (colType)
				{
				case java.sql.Types.BIGINT:
				case java.sql.Types.INTEGER:	val = Integer.toString(resultSet.getInt(iIndice)); break;
				case java.sql.Types.FLOAT:
				case java.sql.Types.DOUBLE: 	val = Double.toString (resultSet.getDouble(iIndice)); break;
				case java.sql.Types.VARCHAR: 	
				case java.sql.Types.NVARCHAR: 	
				case java.sql.Types.NCHAR: 	
				case java.sql.Types.LONGNVARCHAR: 	
				case java.sql.Types.LONGVARCHAR: 	val = resultSet.getString(iIndice); break;
				case java.sql.Types.TIME: 		
				case java.sql.Types.DATE: 		val = resultSet.getDate(iIndice).toString(); break;
				default:		val = "Unknown"; break;
				}
				Node info = document.createTextNode(val);
				Element tag = document.createElement(metadata.getColumnName(iIndice));
				tag.appendChild(info);
				row.appendChild(tag);
			}
			racine.appendChild(row);
		}

		document.appendChild(racine);
		racine.setAttribute("NbRows", Integer.toString(nbRow));

		return document;
	}


	// You need to close the resultSet
	private void close() {
		try {

			if (_connect != null) {
				_connect.close();
			}
		} catch (Exception e) 
		{

		}
	}
}
