import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a flight (or an event/show) that contains a number of seats.
 * Also keeps a log of every successful and failed booking attempt so the
 * results can be analyzed once the simulation finishes.
 */
public class Flight {

    private final String flightName;
    private final Seat[] seats;
    private final List<String> log; // Thread-safe log

    public Flight(String flightName, int numberOfSeats) {
        this.flightName = flightName;
        this.seats = new Seat[numberOfSeats];
        for (int i = 0; i < numberOfSeats; i++) {
            seats[i] = new Seat(i + 1);
        }
        // CopyOnWriteArrayList is suitable here because it is safe to use
        // from multiple threads at the same time without an extra manual lock
        this.log = new CopyOnWriteArrayList<>();
    }

    /**
     * A given user requests to book a specific seat.
     */
    public void requestBooking(String userName, int seatNumber) {
        if (seatNumber < 1 || seatNumber > seats.length) {
            log.add("ERROR: invalid seat number (" + seatNumber + ") requested by " + userName);
            return;
        }

        Seat seat = seats[seatNumber - 1];
        boolean success = seat.bookSeat(userName);

        String message = success
                ? String.format("SUCCESS | Seat %d | User: %s", seatNumber, userName)
                : String.format("FAILED  | Seat %d | User: %s (already booked by %s)",
                        seatNumber, userName, seat.getBookedBy());

        log.add(message);
    }

    public void printSummary() {
        System.out.println("\n================ Booking Log - " + flightName + " ================");
        for (String entry : log) {
            System.out.println(entry);
        }

        int bookedCount = 0;
        for (Seat s : seats) {
            if (s.isBooked()) bookedCount++;
        }

        System.out.println("\n----------------- Final Result -----------------");
        System.out.println("Total seats     : " + seats.length);
        System.out.println("Booked seats    : " + bookedCount);
        System.out.println("(This number must equal the total number of seats exactly, never more,");
        System.out.println(" even though many users competed for the same seats at the same time)");
    }

    public List<String> getLog() {
        return log;
    }
}
