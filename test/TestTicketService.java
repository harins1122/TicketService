import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.walmart.dto.SeatHold;
import com.walmart.impl.TicketServiceImpl;

public class TestTicketService {

	TicketServiceImpl ticketService = null;
	@Before
	public void init(){
		ticketService = new TicketServiceImpl();
	}
	
	@Test
	public void testNumSeatsAvailable_1(){
		Integer value1 = null;
		Optional<Integer> levelNull = Optional.ofNullable(value1);
		int count = ticketService.numSeatsAvailable(levelNull);
		Assert.assertNotNull("Current count returned for all levels"+count);
	}
	
	@Test
	public void testNumSeatsAvailable_2(){
		Integer value2 = new Integer(1);
		Optional<Integer> levelNull = Optional.ofNullable(value2);
		int count = ticketService.numSeatsAvailable(levelNull);
		Assert.assertNotNull("Current count returned for level "+levelNull, count);
	}
	
	@Test
	public void testReserveSeats_1(){
		String confirmationNumber = ticketService.reserveSeats(10000, "harins1122@gmail.com");
		Assert.assertNull("Hold ID not found in the database", confirmationNumber);
	}
	
	@Test
	public void testReserveSeats_2(){
		String confirmationNumber = ticketService.reserveSeats(102, "harins1122@gmail.com");
		Assert.assertNotNull("Confirmation number is:", confirmationNumber);
	}
	
	@Test
	public void testFindHoldSeats_1(){
		SeatHold seathold = ticketService.findAndHoldSeats(10, null, null, "hari123@.com");
		Assert.assertSame("Invalid email ID","Invalid Email Address: Cannot hold a seat", seathold.getMessage());
	}
	
}
