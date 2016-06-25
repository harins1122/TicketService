package com.walmart.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.walmart.startup.StartApplication;

/**
 * This class has all the methods to read data from and write to the database
 * @author Harini Pasupuleti
 *
 */
public class QueryData {

	static Logger logger = Logger.getLogger(QueryData.class.getName());
	
	/**
	 * To populate the initial seat status at the various levels
	 * @param conn
	 * @throws SQLException
	 */
	public static void populateInitialSeatStatus(Connection conn) throws SQLException {

		// create the java statement
		Statement statement = conn.createStatement();
		String queryCount = "select count(*) from currentStatus";
		ResultSet rsCount = statement.executeQuery(queryCount);
		while (rsCount.next()) {
			int count = rsCount.getInt(1);
			logger.info("count:::" + count);
			if (count <= 0) {
				String query = "SELECT level_id, no_of_rows,seats_in_row from venue";

				// execute the query, and get a java resultset
				ResultSet resultSet = statement.executeQuery(query);

				// iterate through the java resultset
				while (resultSet.next()) {
					int levelId = resultSet.getInt("level_id");
					int noOfRows = resultSet.getInt("no_of_rows");
					int seatsInRow = resultSet.getInt("seats_in_row");
					// the mysql insert statement
					String queryInsert = " insert into currentStatus (level_id, seatsLeft, seatsHeld, seatsReserved)"
							+ " values (?, ?, ?, ?)";

					// create the mysql insert preparedstatement
					PreparedStatement preparedStmt = conn.prepareStatement(queryInsert);
					preparedStmt.setInt(1, levelId);
					preparedStmt.setInt(2, noOfRows * seatsInRow);
					preparedStmt.setInt(3, 0);
					preparedStmt.setInt(4, 0);

					// execute the preparedstatement
					preparedStmt.execute();

				}
			}

		}
		rsCount.close();
		statement.close();
	}

	/**
	 * This is method is used to read the status at a current level
	 * @param level 
	 * @param connection
	 * @return int
	 */
	public static int readCurrentLevelStatus(int level, Connection connection) throws SQLException {
		int currentSeats = 0;
		try {
			Statement statement = StartApplication.connection.createStatement();
			String query = "select seatsLeft from currentStatus where level_id=" + level;
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				currentSeats = resultSet.getInt(1);
			}
			resultSet.close();
			statement.close();
		} catch (SQLException s) {
			throw s;
		} catch (Exception e) {
			throw e;
		}
		return currentSeats;
	}

	/**
	 * This is method is used to read the sum of status at all the levels
	 * @param connection
	 * @return int
	 */
	public static int readCurrentStatus(Connection connection) throws SQLException {
		int currentSeats = 0;
		try {
			Statement statement = StartApplication.connection.createStatement();
			String query = "select sum(seatsLeft) from currentStatus";
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				currentSeats = resultSet.getInt(1);
			}
			resultSet.close();
			statement.close();
		} catch (SQLException s) {
			throw s;
		} catch (Exception e) {
			throw e;
		}
		return currentSeats;
	}

	/**
	 * This is method is used to clear the records which were added before 10 minutes
	 */
	public static void clearHoldInformation() throws SQLException {
		try {
			Statement st = StartApplication.connection.createStatement();
			String query = "select level_id,seatCount from holdInformation where time < date_sub(now(),interval 10 minute)";
			ResultSet resulSet = st.executeQuery(query);
			boolean deleteNow = false;
			// There are at least some records
			while (resulSet.next() == true) {
				int level_id = resulSet.getInt(1);
				int seatCount = resulSet.getInt(2);
				int count = readCurrentLevelStatus(level_id);
				query = "update currentStatus set seatsLeft = ? where level_id = ?";
				PreparedStatement preparedStmt = StartApplication.connection.prepareStatement(query);
				preparedStmt.setInt(1, count + seatCount);
				preparedStmt.setInt(2, level_id);
				preparedStmt.executeUpdate();
				int previousValue = getPreviousHeldValue(level_id);
				query = "update currentStatus set seatsHeld = ? where level_id = ?";
				preparedStmt = StartApplication.connection.prepareStatement(query);
				preparedStmt.setInt(1, previousValue - seatCount);
				preparedStmt.setInt(2, level_id);
				preparedStmt.executeUpdate();
				deleteNow = true;
			}
			if (deleteNow) {
				query = "delete from holdInformation where time < date_sub(now(),interval 10 minute)";
				PreparedStatement preparedStmt = StartApplication.connection.prepareStatement(query);
				boolean records = preparedStmt.execute();
				if (records) {
					logger.info("Old records deleted");
				}
			} else {
				logger.info("No records to clear from holdInformation Table::::");
			}
			resulSet.close();
			st.close();
		} catch (SQLException s) {
			throw s;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * This is method is used to read the current level status
	 * @param level
	 * @return int
	 */
	
	public static int readCurrentLevelStatus(int level) throws SQLException {
		int currentSeats = 0;
		try {
			Statement statement = StartApplication.connection.createStatement();
			String query = "select seatsLeft from currentStatus where level_id=" + level;
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				currentSeats = resultSet.getInt(1);
			}
			resultSet.close();
			statement.close();
		} catch (SQLException s) {
			throw s;
		} catch (Exception e) {
			throw e;
		}
		return currentSeats;
	}

	/**
	 * This is method is used to update the current status table
	 * @param currentLevelCount
	 * @param target
	 * @param level_id
	 */
	public static void updateCurrentStatusTable(int currentLevelCount, int target, int level_id) throws SQLException {
		try {
			Statement statement = StartApplication.connection.createStatement();
			String query = "update currentStatus set seatsLeft = ? where level_id = ?";
			PreparedStatement preparedStmt = StartApplication.connection.prepareStatement(query);
			preparedStmt.setInt(1, currentLevelCount);
			preparedStmt.setInt(2, level_id);
			preparedStmt.executeUpdate();
			int previousValue = getPreviousHeldValue(level_id);
			query = "update currentStatus set seatsHeld = ? where level_id = ?";
			preparedStmt = StartApplication.connection.prepareStatement(query);
			preparedStmt.setInt(1, target + previousValue);
			preparedStmt.setInt(2, level_id);
			preparedStmt.executeUpdate();
			statement.close();
			preparedStmt.close();
		} catch (SQLException s) {
			throw s;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * This is method is used to get the previous held value
	 * @param level_id
	 * @return int
	 */
	
	public static int getPreviousHeldValue(int level_id) throws SQLException {
		Statement statement = StartApplication.connection.createStatement();
		String query = "select seatsHeld from currentstatus where level_id=" + level_id;
		ResultSet resultSet = statement.executeQuery(query);
		int previousValue = 0;
		// There are at least some records
		while (resultSet.next()) {
			previousValue = resultSet.getInt(1);
		}
		statement.close();
		resultSet.close();
		return previousValue;
	}

	/**
	 * This is method is used to insert a record in the holdInformation table
	 * @param level_id
	 * @param target
	 * @param email
	 */
	public static void insertHoldInformation(int level_id, int target, String email) throws SQLException {
		String query = " insert into holdInformation (level_id, createdDate, seatCount, email)" + " values (?, ?, ?,?)";

		PreparedStatement preparedStmt = StartApplication.connection.prepareStatement(query);
		preparedStmt.setInt(1, level_id);
		Calendar calendar = Calendar.getInstance();
		preparedStmt.setDate(2, new Date(calendar.getTime().getTime()));
		preparedStmt.setInt(3, (target));
		preparedStmt.setString(4, (email));
		preparedStmt.execute();
		preparedStmt.close();
		logger.info("Record successfully inserted");
	}

	/**
	 * This is used to check if a record exists
	 * @param customerEmail
	 * @return boolean
	 */
	public static boolean checkIfRecordExists(String customerEmail) throws SQLException {
		Statement statement = StartApplication.connection.createStatement();
		String query = "select * from holdInformation where email='" + customerEmail + "'";
		ResultSet resultSet = statement.executeQuery(query);
		// There are at least some records
		while (resultSet.next()) {
			return true;
		}
		resultSet.close();
		statement.close();
		return false;
	}

	/**
	 * This is used to confirm the booking
	 * @param seatHoldId
	 * @param customerEmail
	 * @return String
	 */
	public static String confirmBooking(int seatHoldId, String customerEmail) throws SQLException {
		Statement statement = StartApplication.connection.createStatement();
		String queryUpdate = "update currentStatus set seatsHeld = ? where level_id = ?";
		PreparedStatement preparedStmt = StartApplication.connection.prepareStatement(queryUpdate);
		String query = "select seatCount,level_id from holdInformation where email='" + customerEmail + "' and hold_id="
				+ seatHoldId + " group by level_id";
		ResultSet resultSet = statement.executeQuery(query);
		// There are at least some records
		boolean deleteNow = false;
		PreparedStatement preparedStmtTwo = null;
		String confirmationNumber = null;
		while (resultSet.next() == true) {
			int seatCount = resultSet.getInt(1);
			int level_id = resultSet.getInt(2);
			// Update the seatHeld to 0 and set the value to reserved
			preparedStmt.setInt(1, 0);
			preparedStmt.setInt(2, level_id);
			preparedStmt.executeUpdate();
			preparedStmt.close();
			String queryUpdateReserved = "update currentStatus set seatsReserved = ? where level_id = ?";
			preparedStmtTwo = StartApplication.connection.prepareStatement(queryUpdateReserved);
			preparedStmtTwo.setInt(1, seatCount);
			preparedStmtTwo.setInt(2, level_id);
			preparedStmtTwo.executeUpdate();
			preparedStmtTwo.close();
			deleteNow = true;
		}
		if (deleteNow) {
			// Delete the records from the hold Information table
			query = "delete from holdInformation where email='" + customerEmail + "' and hold_id=" + seatHoldId;
			PreparedStatement preparedStmtThree = StartApplication.connection.prepareStatement(query);
			preparedStmtThree.execute();
			// Generate a unique confirmation number
			confirmationNumber = java.util.UUID.randomUUID().toString();
		}else{
			logger.info("No records found with this Hold ID "+seatHoldId+" and email ID "+customerEmail+" combination.");
		}
		statement.close();
		preparedStmt.close();
		return confirmationNumber;
	}
}
