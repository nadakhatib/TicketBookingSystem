import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Program entry point.
 * Simulates several users (Threads) trying to book seats on the same
 * flight at the exact same moment, using a database to ensure
 * real concurrent safety.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        final int TOTAL_SEATS = 10;   // Number of seats available on the flight
        final int TOTAL_USERS = 30;   // Number of users who will try to book

        System.out.println("================================================");
        System.out.println("✈️  CONCURRENT TICKET BOOKING SYSTEM");
        System.out.println("================================================\n");

        Flight flight = new Flight("Alpha Airlines - Flight AF202", TOTAL_SEATS);

        // A thread pool that runs concurrently
        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_USERS);

        System.out.println("\n🚀 Starting simulation: " + TOTAL_USERS + " users competing for "
                + TOTAL_SEATS + " seats only...");
        System.out.println("💾 Using H2 Database for atomic booking operations\n");

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= TOTAL_USERS; i++) {
            final String userName = "User-" + i;
            // Each user tries to book a seat between 1 and TOTAL_SEATS
            final int seatChoice = (i % TOTAL_SEATS) + 1;

            executor.submit(() -> flight.requestBooking(userName, seatChoice));
        }

        executor.shutdown();
        // Wait for all threads to finish before printing the final summary
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        
        if (!finished) {
            System.out.println("⚠️  Some threads did not finish in time!");
        }
        
        flight.printSummary();
        System.out.println("\n⏱️  Total execution time: " + (endTime - startTime) + " ms");
        
        // Clean up database connection
        flight.closeConnection();
        
        System.out.println("\n✅ Simulation completed successfully!");
    }
}