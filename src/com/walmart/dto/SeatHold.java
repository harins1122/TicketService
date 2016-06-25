package com.walmart.dto;

import java.util.HashMap;
/**
 * This class is the model object for the SeatHold Details. It contains a meesage
 * and the map with level_id and the seats reserved in that level
 * @author Harini Pasupuleti
 *
 */
public class SeatHold {

	String message; 
	HashMap<Integer, Integer> ticketList= new HashMap<Integer,Integer>();
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public HashMap<Integer, Integer> getTicketList() {
		return ticketList;
	}
	public void setTicketList(HashMap<Integer, Integer> ticketList) {
		this.ticketList = ticketList;
	}
	
}
