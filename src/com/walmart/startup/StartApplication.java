package com.walmart.startup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.walmart.dao.QueryDAO;
import com.walmart.dto.SeatHold;
import com.walmart.impl.TicketServiceImpl;
import com.walmart.util.WalmartUtility;

/**
 * This is the main class to run the ticket service methods
 * 
 * @author Harini Pasupuleti
 *
 */
public class StartApplication {

	public static Connection connection = null;
	public static int levelOneCount = 0;
	public static int levelTwoCount = 0;
	public static int levelThreeCount = 0;
	public static int levelFourCount = 0;
	static Logger logger = Logger.getLogger(StartApplication.class.getName());
	static {
		connection = WalmartUtility.connectToDatabase();
	}

	public static void main(String args[]) {
		try {

			if (connection != null) {
				QueryDAO.populateInitialSeatStatus(connection);
				// To check the number of available seats
				TicketServiceImpl implementService = new TicketServiceImpl();
				Integer value1 = null;
				Integer value2 = new Integer(1);
				Optional<Integer> levelNull = Optional.ofNullable(value1);
				int count = implementService.numSeatsAvailable(levelNull);
				logger.info("Current Available Seats with no level information:" + count);
				Optional<Integer> levelId = Optional.of(value2);
				levelOneCount = implementService.numSeatsAvailable(levelId);
				logger.info("Current Available Seats with level information:" + levelOneCount);
				value2 = new Integer(2);
				levelId = Optional.of(value2);
				levelTwoCount = implementService.numSeatsAvailable(levelId);
				logger.info("Current Available Seats with level information:" + levelTwoCount);

				value2 = new Integer(3);
				levelId = Optional.of(value2);
				levelThreeCount = implementService.numSeatsAvailable(levelId);
				logger.info("Current Available Seats with level information:" + levelThreeCount);

				value2 = new Integer(4);
				levelId = Optional.of(value2);
				levelFourCount = implementService.numSeatsAvailable(levelId);
				logger.info("Current Available Seats with level information:" + levelFourCount);

				// To hold and find the seats
				String customerEmail = "ha2222@gmail.com";
				Integer value = null;
				SeatHold seathold = implementService.findAndHoldSeats(1000, Optional.ofNullable(value),
						Optional.ofNullable(value), customerEmail);
				displaySeatHold(seathold);
				seathold = implementService.findAndHoldSeats(500, Optional.of(3), Optional.ofNullable(value),
						customerEmail);
				displaySeatHold(seathold);

				seathold = implementService.findAndHoldSeats(10, Optional.ofNullable(value), Optional.of(2),
						customerEmail);
				displaySeatHold(seathold);

				seathold = implementService.findAndHoldSeats(1000, Optional.of(1), Optional.of(3), customerEmail);
				displaySeatHold(seathold);

				// To Reserve the seats
				String confirmationNumber = implementService.reserveSeats(99, "ha2222@gmail.com");
				logger.info("confirmationNumber:" + confirmationNumber);

				connection.close();
			} else {
				logger.error("Unable to connect to database...");
			}
		} catch (SQLException se) {
			logger.error("Exception occured in main method", se);
		}
	}

	/**
	 * To display the Seat Hold as a Json Object
	 * 
	 * @param SeatHold
	 */
	public static void displaySeatHold(SeatHold seathold) {
		Gson gson = new Gson();
		String json = gson.toJson(seathold);
		System.out.printf("JSON: %s", json.toString());
	}

}
