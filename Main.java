import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Program entry point.
 * Simulates several users (Threads) trying to book seats on the same
 * flight at the exact same moment, in order to prove that the booking
 * system prevents conflicts (double booking) thanks to "synchronized".
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        final int TOTAL_SEATS = 10;   // Number of seats available on the flight
        final int TOTAL_USERS = 30;   // Number of users who will try to book

        Flight flight = new Flight("Alpha Airlines - Flight AF202", TOTAL_SEATS);

        // A thread pool that runs concurrently
        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_USERS);

        System.out.println("Starting simulation: " + TOTAL_USERS + " users competing for "
                + TOTAL_SEATS + " seats only...\n");

        for (int i = 1; i <= TOTAL_USERS; i++) {
            final String userName = "User-" + i;
            // Each user tries to book a seat between 1 and TOTAL_SEATS
            final int seatChoice = (i % TOTAL_SEATS) + 1;

            executor.submit(() -> flight.requestBooking(userName, seatChoice));
        }

        executor.shutdown();
        // Wait for all threads to finish before printing the final summary
        executor.awaitTermination(1, TimeUnit.MINUTES);

        flight.printSummary();
    }
}
