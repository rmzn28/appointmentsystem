import java.sql.*;

public class UserDAO {

    public void addUser(User user) {
        String insertUser = "INSERT INTO users (username, name, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);

                if (user instanceof Instructor) {
                    addInstructor(userId, user.getUsername(), user.getName());
                } else if (user instanceof Student) {
                    addStudent(userId, user.getUsername(), user.getName());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addInstructor(int userId, String username, String name) throws SQLException {
        String query = "INSERT INTO instructors (id, username, name) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, name);

            stmt.executeUpdate();
        }
    }

    private void addStudent(int userId, String username, String name) throws SQLException {
        String query = "INSERT INTO students (id, username, name) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, name);

            stmt.executeUpdate();
        }
    }
}
