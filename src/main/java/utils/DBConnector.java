package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    Connection conn = null;
    public static Connection conDB()
    {
    	try {
        	try {
        		Connection conn = DriverManager.getConnection("jdbc:sqlite:owaccounts.db");
        		return conn;
              } catch ( SQLException e ) {
                  e.printStackTrace();
                  return null;
              }
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }            
    }
}