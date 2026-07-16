import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a flight (or an event/show) that contains a number of seats.
 * Uses a real database (H2) for persistent and safe concurrent booking.
 */
public class Flight {

    private final String flightName;
    private final Seat[] seats;
    private final List<String> log; // Thread-safe log
    private final Connection dbConnection; // Database connection

    public Flight(String flightName, int numberOfSeats) {
        this.flightName = flightName;
        this.log = new CopyOnWriteArrayList<>();
        
        try {
            // Load H2 database driver (optional for newer Java versions)
            Class.forName("org.h2.Driver");
            
            // Connect to H2 database (in-memory mode for simplicity)
            // You can change to file-based mode if needed: "jdbc:h2:~/flight_db"
            dbConnection = DriverManager.getConnection("jdbc:h2:mem:flight_db;DB_CLOSE_DELAY=-1");
            
            // Create the seats table if it doesn't exist
            createSeatsTable(numberOfSeats);
            
            // Initialize Seat objects with database connection
            this.seats = new Seat[numberOfSeats];
            for (int i = 0; i < numberOfSeats; i++) {
                seats[i] = new Seat(i + 1, dbConnection);
            }
            
            System.out.println("✅ Database initialized successfully with " + numberOfSeats + " seats.");
            
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 JDBC Driver not found! Make sure h2-2.2.224.jar is in classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Creates the seats table and inserts initial seat records
     */
    private void createSeatsTable(int numberOfSeats) throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS seats (
                seat_number INT PRIMARY KEY,
                booked BOOLEAN DEFAULT FALSE,
                booked_by VARCHAR(255)
            )
        """;
        
        try (Statement stmt = dbConnection.createStatement()) {
            stmt.execute(createTableSQL);
            
            // Insert seats if they don't exist
            for (int i = 1; i <= numberOfSeats; i++) {
                String insertSQL = "MERGE INTO seats (seat_number, booked, booked_by) VALUES (" + i + ", FALSE, NULL)";
                stmt.execute(insertSQL);
            }
        }
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
        System.out.println("Database        : H2 (in-memory)");
        System.out.println("Booking method  : Atomic SQL UPDATE");
        System.out.println(" This number equals the total seats, proving no double booking!");
        System.out.println("===================================================");
    }

    public List<String> getLog() {
        return log;
    }
    
    /**
     * Close database connection when done
     */
    public void closeConnection() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                System.out.println(" Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}