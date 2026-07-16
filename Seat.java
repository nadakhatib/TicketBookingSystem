import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a single seat (one ticket) in the booking system.
 * Each seat is stored in a database to ensure real concurrent safety
 * using atomic SQL updates.
 */
public class Seat {

    private final int seatNumber;
    private final Connection dbConnection; // Database connection

    public Seat(int seatNumber, Connection dbConnection) {
        this.seatNumber = seatNumber;
        this.dbConnection = dbConnection;
    }

    /**
     * Attempts to book this seat using an atomic database update.
     * The SQL UPDATE with a WHERE condition ensures that even if
     * 1000 threads try at the same time, only one will succeed.
     *
     * @param userName the name of the user attempting the booking
     * @return true if the booking succeeded, false if the seat was already booked
     */
    public boolean bookSeat(String userName) {
        // Atomic database update: only updates if seat is NOT booked yet
        String sql = "UPDATE seats SET booked = TRUE, booked_by = ? WHERE seat_number = ? AND booked = FALSE";
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, userName);
            stmt.setInt(2, seatNumber);
            
            int rowsUpdated = stmt.executeUpdate();
            
            // If rowsUpdated == 1, the update succeeded (seat was free)
            // If rowsUpdated == 0, the seat was already booked
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            System.err.println("Database error while booking seat " + seatNumber + ": " + e.getMessage());
            return false;
        }
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    /**
     * Check if a seat is booked by querying the database
     */
    public boolean isBooked() {
        String sql = "SELECT booked FROM seats WHERE seat_number = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setInt(1, seatNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("booked");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the name of the user who booked this seat
     */
    public String getBookedBy() {
        String sql = "SELECT booked_by FROM seats WHERE seat_number = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setInt(1, seatNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("booked_by");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}