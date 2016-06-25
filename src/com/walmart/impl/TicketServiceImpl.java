package com.walmart.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.walmart.constants.WalmartConstants;
import com.walmart.dao.QueryData;
import com.walmart.dto.SeatHold;
import com.walmart.service.TicketService;
import com.walmart.startup.StartApplication;
import com.walmart.validator.EmailValidator;

/**
 * This class implements all the methods in the Ticket Service Interface
 * 
 * @author Harini Pasupuleti
 *
 */
public class TicketServiceImpl implements TicketService {

	static Logger logger = Logger.getLogger(TicketServiceImpl.class.getName());

	SeatHold seatHold = new SeatHold();
	HashMap<Integer, Integer> ticketList = new HashMap<Integer, Integer>();

	 /**
	   * Returns the number of seats available at the venue.
	   * @param venueLevel Optional parameter which contains the venue level  
	   * @return int
	   */
	@Override
	public int numSeatsAvailable(Optional<Integer> venueLevel) {
		logger.info("venue:" + venueLevel.isPresent());
		try {
			if (venueLevel.isPresent()) {
				return QueryData.readCurrentLevelStatus(venueLevel.get(), StartApplication.connection);
			} else {
				return QueryData.readCurrentStatus(StartApplication.connection);
			}
		} catch (SQLException e) {
			logger.error("Exception occured in numSeatsAvailable();" + e);
		}
		return 0;
	}

	 /**
	   * Used to reserve the preveiously held seats
	   * @param seatHoldId The unique seat hold ID generated
	   * @param customerEmail A valid customer email address 
	   * @return String The unique confirmation code is returned
	   */
	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) {
		String confirmationNumber = null;
		try {
			confirmationNumber = QueryData.confirmBooking(seatHoldId, customerEmail);
		} catch (SQLException se) {
			logger.error("Exception occured in reserveSeats();" + se);
		}
		return confirmationNumber;
	}

	 /**
	   * Used to find and hold seats-the idea is to hold the seats from the specified level(if present)
	   * @param numSeats The number of seats to be reserved
	   * @param minLevel Optional parameter which contains the minmimum venue level  
	   * @param maxLevel Optional parameter which contains the maximum venue level
	   * @param maxLevel Unique email ID of a customer
	   * @return SeatHold The seat hold object with the information of the seats reserved at 
	   * a given level
	   */
	@Override
	public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel,

			Optional<Integer> maxLevel, String customerEmail) {
		int target = numSeats;
		int maxLevelId = WalmartConstants.MAX_LEVEL_VENUE;
		int minLevelId = WalmartConstants.MIN_LEVEL_VENUE;
		try {
			//Before holding seats, clear the seats which have been held for a specific duration
			QueryData.clearHoldInformation();
			EmailValidator emailValidator = new EmailValidator();
			if (emailValidator.validate(customerEmail)) {
				boolean recordExists = QueryData.checkIfRecordExists(customerEmail);
				if (recordExists) {
					seatHold = new SeatHold();
					seatHold.setMessage("Duplicate record found with the same Email ID");
					return seatHold;
				}
				//Minimum level should be less than maximum level 
				if (minLevel.isPresent() && maxLevel.isPresent()) {
					if (minLevel.get() > maxLevel.get()) {
						seatHold = new SeatHold();
						seatHold.setMessage("Min level should be less than max level");
						return seatHold;
					} else {
						int minimumLevel = minLevel.get();
						int maximumLevel = maxLevel.get();
						while (minimumLevel <= maximumLevel && target > 0) {
							int currentGlobalCount = QueryData.readCurrentLevelStatus(minimumLevel);
							int newTarget = holdSeats(currentGlobalCount, target, minimumLevel, customerEmail);
							target = newTarget;
							minimumLevel++;
						}
					}

				} else if (minLevel.isPresent() && !maxLevel.isPresent()) {
					int minimumLevel = minLevel.get();
					while (minimumLevel <= maxLevelId && target > 0) {
						int currentGlobalCount = QueryData.readCurrentLevelStatus(minimumLevel);
						int newTarget = holdSeats(currentGlobalCount, target, minimumLevel, customerEmail);
						target = newTarget;
						minimumLevel++;
					}

				} else if (!minLevel.isPresent() && maxLevel.isPresent()) {

					int maximumLevel = maxLevel.get();
					while (maximumLevel >= minLevelId && target > 0) {
						int currentGlobalCount = QueryData.readCurrentLevelStatus(maximumLevel);
						int newTarget = holdSeats(currentGlobalCount, target, maximumLevel, customerEmail);
						target = newTarget;
						maximumLevel--;
					}

				} else if (!minLevel.isPresent() && !maxLevel.isPresent()) {
					while (maxLevelId > 0 && target > 0) {
						int currentGlobalCount = QueryData.readCurrentLevelStatus(maxLevelId);
						int newTarget = holdSeats(currentGlobalCount, target, maxLevelId, customerEmail);
						target = newTarget;
						maxLevelId--;

					}
					//Try to hold as many seats as possible for a given level request. 
					//If the requested levels are full, then send this message
					if (target > 0) {
						seatHold = new SeatHold();
						seatHold.setMessage("Unable to hold " + target + "seats due to unavailability");
					}
				}
			} else {
				logger.error("Invalid Email Address: Cannot hold a seat");
			}

		} catch (SQLException s) {
			logger.error("Exception occured in findAndHoldSeats();" + s);
		}
		return seatHold;

	}

	 /**
	   * Used to find and hold seats
	   * @param currentLevelCount The number of seats left at the current level
	   * @param target The number of seats requested
	   * @param level_id The current level which is being looked into
	   * @param email Unique email ID of a customer
	   * @return int The current target of seats to be met
	   */
	public int holdSeats(int currentLevelCount, int target, int level_id, String email)
			throws SQLException {
		if (currentLevelCount > 0 && target > 0) {
			if (target < currentLevelCount) {
				currentLevelCount = currentLevelCount - target;
				QueryData.updateCurrentStatusTable(currentLevelCount, target, level_id);
				QueryData.insertHoldInformation(level_id, target, email);
				ticketList.put(level_id, target);
				target = 0;
			} else {
				target = target - currentLevelCount;
				QueryData.updateCurrentStatusTable(0, currentLevelCount, level_id);
				QueryData.insertHoldInformation(level_id, currentLevelCount, email);
				ticketList.put(level_id, currentLevelCount);
			}
			seatHold.setMessage("Your seats have been Held Successfully");
			seatHold.setTicketList(ticketList);
		} else {
			seatHold = new SeatHold();
			logger.error("Unable to find seats in level " + level_id + "..Checking the other level");
		}
		return target;

	}

}
