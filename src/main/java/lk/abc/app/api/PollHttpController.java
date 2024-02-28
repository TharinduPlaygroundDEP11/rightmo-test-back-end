package lk.abc.app.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.abc.app.to.PollTO;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.sql.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PollHttpController {
    private final HikariDataSource pool;

    public PollHttpController() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/developer_test");
        config.setUsername("root");
        config.setPassword("1234");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        pool = new HikariDataSource(config);
    }

    @PreDestroy
    private void destroy() {
        pool.close();
    }

    @PostMapping(value = "/polls/create", consumes = "application/json")
    public void createPoll(@RequestBody PollTO pollTO) {
        try (Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PreparedStatement stm = connection
                        .prepareStatement("INSERT INTO polls (title, category_id) VALUES (?, ?)",
                                Statement.RETURN_GENERATED_KEYS);
                stm.setString(1, pollTO.getTitle());
                stm.setInt(2, pollTO.getCategoryId());
                stm.executeUpdate();
                ResultSet generatedKeys = stm.getGeneratedKeys();
                generatedKeys.next();
                int id = generatedKeys.getInt(1);

                PreparedStatement stmOption = connection
                        .prepareStatement("INSERT INTO options (poll_id, option_text) VALUES (?, ?)");
                for (String option : pollTO.getOptions()) {
                    stmOption.setInt(1, id);
                    stmOption.setString(2, option);
                    stmOption.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Failed to add the data", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PatchMapping(value = "/polls/edit/{pollId}", consumes = "application/json")
    public void editPoll(@PathVariable int pollId, @RequestBody PollTO pollTO) {
        try (Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PreparedStatement stmExist = connection
                        .prepareStatement("SELECT * FROM polls WHERE poll_id = ?");
                stmExist.setInt(1, pollId);
                if (!stmExist.executeQuery().next()) {
                    System.out.println("No poll for that id");
                }

                PreparedStatement stmUpdate = connection
                        .prepareStatement("UPDATE polls SET title = ?, category_id = ?");
                stmUpdate.setString(1, pollTO.getTitle());
                stmUpdate.setInt(2, pollTO.getCategoryId());
                stmUpdate.executeUpdate();

                PreparedStatement stmOptionUpdate = connection
                        .prepareStatement("UPDATE options SET poll_id = ?, option_text = ?");
                for (String option : pollTO.getOptions()) {
                    stmOptionUpdate.setInt(1, pollId);
                    stmOptionUpdate.setString(2, option);
                    stmOptionUpdate.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Failed to update the data", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/polls/delete/{pollId}")
    public void deletePoll(@PathVariable int pollId) {
        try (Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PreparedStatement stmExist = connection
                        .prepareStatement("SELECT * FROM polls WHERE poll_id = ?");
                stmExist.setInt(1, pollId);
                if (!stmExist.executeQuery().next()) {
                    System.out.println("No poll for that id");
                }

                PreparedStatement stmOptionDelete = connection
                        .prepareStatement("DELETE FROM options WHERE poll_id = ?");
                stmOptionDelete.setInt(1, pollId);
                stmOptionDelete.executeUpdate();

                PreparedStatement stmDelete = connection
                        .prepareStatement("DELETE FROM polls WHERE poll_id = ?");
                stmDelete.setInt(1, pollId);
                stmDelete.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Failed to delete the data", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/polls/{pollId}")
    public void findPollById() {
        System.out.println("Find by Id");
    }

    @GetMapping("/polls")
    public void getAllPolls() {
        System.out.println("Get all polls");
    }

    @GetMapping("/categories")
    public void getAllCategories() {
        System.out.println("Get all categories");
    }

    @PostMapping("/polls/{pollId}/vote")
    public void voteToPoll() {
        System.out.println("Vote to poll");
    }
}
