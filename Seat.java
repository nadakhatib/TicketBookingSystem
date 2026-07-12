/**
 * Represents a single seat (one ticket) in the booking system.
 * Each seat is a "shared resource" that more than one user (Thread)
 * may try to book at the exact same moment, so it must be protected
 * with a synchronization mechanism.
 */
public class Seat {

    private final int seatNumber;
    private boolean isBooked;
    private String bookedBy;

    public Seat(int seatNumber) {
        this.seatNumber = seatNumber;
        this.isBooked = false;
        this.bookedBy = null;
    }

    /**
     * Attempts to book this seat.
     * The "synchronized" keyword is the core of the solution:
     * it guarantees that only ONE thread at a time can enter this
     * method for this particular seat (Mutual Exclusion / Critical Section).
     *
     * @param userName the name of the user attempting the booking
     * @return true if the booking succeeded, false if the seat was already booked
     */
    public synchronized boolean bookSeat(String userName) {
        if (isBooked) {
            return false; // Seat already booked - booking fails
        }
        // Simulate a realistic processing delay (e.g. a database call)
        // to clearly reveal the importance of synchronization when a
        // delay exists between checking and updating the state.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        isBooked = true;
        bookedBy = userName;
        return true;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public String getBookedBy() {
        return bookedBy;
    }
}
