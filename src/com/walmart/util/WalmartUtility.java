package com.walmart.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.walmart.constants.WalmartConstants;

/**
 * This is the utility class to place the common method calls and functionality
 * @author Harini Pasupuleti
 *
 */
public class WalmartUtility {

	static Logger logger = Logger.getLogger(WalmartUtility.class.getName());

	/**
	 * This method is used to connect to the database
	 * @return Connection The database connection object
	 */
	public static Connection connectToDatabase(){
		logger.info("Loading driver...");

		try {
		    Class.forName(WalmartConstants.DRIVER_NAME);
		    logger.info("Driver loaded!");
		} catch (ClassNotFoundException e) {
		    throw new IllegalStateException("Cannot find the driver in the classpath!", e);
		}
		logger.info("Connecting database...");
		try{
			Connection connection =
				       DriverManager.getConnection(WalmartConstants.DB_CONNECTION_DETAILS);
			return connection;
		}catch(SQLException e){
			
		}
		return null;
	}
}
