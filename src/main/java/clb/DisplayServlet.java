package clb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/index")
public class DisplayServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/clb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "728728";
    private static final Logger LOGGER = Logger.getLogger(DisplayServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            LoggingSetup.setupLogging();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to set up logging", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String searchId = request.getParameter("searchId");

        if (searchId != null && !searchId.isEmpty()) {
            try (Connection conn = getConnection()) {
                String sql = "SELECT id, name1, name2 FROM credentials WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(searchId));
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Credentials credentials = new Credentials();
                    credentials.setId(rs.getInt("id"));
                    credentials.setFirstName(rs.getString("name1"));
                    credentials.setLastName(rs.getString("name2"));

                    request.setAttribute("searchResult", credentials);
                    LOGGER.log(Level.INFO, "Found credentials for ID {0}", searchId);
                } else {
                    request.setAttribute("searchResult", null); // Clear previous search results if any
                    LOGGER.log(Level.WARNING, "No details found for ID {0}", searchId);
                }
            } catch (SQLException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Database error", e);
                request.setAttribute("searchError", true);
            }
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String submitAction = request.getParameter("submitAction");

        if (submitAction != null && submitAction.equals("insert")) {
            String id = request.getParameter("id");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");

            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO credentials (id, name1, name2) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(id));
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.executeUpdate();

                request.setAttribute("insertionSuccess", true);
                LOGGER.log(Level.INFO, "Inserted credentials for ID {0}", id);
            } catch (SQLException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Insertion error", e);
                request.setAttribute("insertionError", true);
            }
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    private Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
