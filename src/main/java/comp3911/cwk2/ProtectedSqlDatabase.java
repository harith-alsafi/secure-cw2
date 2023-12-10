package comp3911.cwk2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

public class ProtectedSqlDatabase {
    private static final String CONNECTION_URL = "jdbc:sqlite:db.sqlite3";
    private static final String AUTH_QUERY = "select * from user where username=? and password=?";
    private static final String SEARCH_QUERY = "select * from patient where surname=? collate nocase";

    private Connection database;

    public void connectToDatabase() throws ServletException {
        try {
            database = DriverManager.getConnection(CONNECTION_URL);
        } catch (SQLException error) {
            throw new ServletException(error.getMessage());
        }
    }

    public boolean isAuthenticated(String username, String hashedPassword) {
        try {
            PreparedStatement statement = database.prepareStatement(AUTH_QUERY);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            ResultSet results = statement.executeQuery();
            return results.next();
        } catch (SQLException error) {
            return false;
        }
    }

    public List<Record> searchResults(String surname) throws SQLException {
        List<Record> records = new ArrayList<>();
        PreparedStatement statement = database.prepareStatement(SEARCH_QUERY);
        statement.setString(1, surname);
        ResultSet results = statement.executeQuery();
        while (results.next()) {
            Record rec = new Record();
            rec.setSurname(results.getString(2));
            rec.setForename(results.getString(3));
            rec.setAddress(results.getString(4));
            rec.setDateOfBirth(results.getString(5));
            rec.setDoctorId(results.getString(6));
            rec.setDiagnosis(results.getString(7));
            records.add(rec);
        }

        return records;
    }
}
