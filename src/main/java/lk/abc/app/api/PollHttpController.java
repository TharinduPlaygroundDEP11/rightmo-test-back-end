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

    @PatchMapping("/polls/edit/{pollId}")
    public void editPoll() {
        System.out.println("Edit Poll");
    }

    @DeleteMapping("/polls/delete/{pollId}")
    public void deletePoll() {
        System.out.println("Delete Poll");
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
