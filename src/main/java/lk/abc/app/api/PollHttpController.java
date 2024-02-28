package lk.abc.app.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.abc.app.to.PollTO;
import lk.abc.app.to.VoteTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PreDestroy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found!");
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
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found!");
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

    @GetMapping(value = "/polls/{pollId}", produces = "application/json")
    public PollTO findPollById(@PathVariable int pollId) {
        try (Connection connection = pool.getConnection()) {
                PreparedStatement stmExist = connection
                        .prepareStatement("SELECT * FROM polls WHERE poll_id = ?");
                stmExist.setInt(1, pollId);
                if (!stmExist.executeQuery().next()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found!");
                }
            PreparedStatement stmOptions = connection
                    .prepareStatement("SELECT * FROM options WHERE poll_id = ?");
            stmOptions.setInt(1, pollId);
            ResultSet rst1 = stmOptions.executeQuery();
            List<String> optionList = new ArrayList<>();
            while (rst1.next()) {
                String option = rst1.getString("option_text");
                optionList.add(option);
            }
            PollTO pollTO = null;
            ResultSet rst = stmExist.executeQuery();
                while (rst.next()) {
                    int categoryId = rst.getInt("category_id");
                    String title = rst.getString("title");
                    pollTO = new PollTO(pollId, title, categoryId, optionList);
                }
            return pollTO;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/polls")
    public List<PollTO> getAllPolls() {
        try (Connection connection = pool.getConnection()) {
            List<PollTO> pollTOList = new ArrayList<>();
            PreparedStatement stm = connection
                    .prepareStatement("SELECT * FROM polls");
            ResultSet rst = stm.executeQuery();
            while (rst.next()) {
                int pollId = rst.getInt("poll_id");
                PreparedStatement stmOptions = connection
                        .prepareStatement("SELECT * FROM options WHERE poll_id = ?");
                stmOptions.setInt(1, pollId);
                ResultSet rst1 = stmOptions.executeQuery();
                List<String> optionList = new ArrayList<>();
                while (rst1.next()) {
                    String option = rst1.getString("option_text");
                    optionList.add(option);
                }
                String title = rst.getString("title");
                int categoryId = rst.getInt("category_id");
                pollTOList.add(new PollTO(pollId, title, categoryId, optionList));
            }
            return pollTOList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/categories")
    public List<String> getAllCategories() {
        try (Connection connection = pool.getConnection()) {
            List<String> categoryList = new ArrayList<>();
            PreparedStatement stm = connection
                    .prepareStatement("SELECT * FROM categories");
            ResultSet rst = stm.executeQuery();
            while (rst.next()) {
                String category = rst.getString("category_name");
                categoryList.add(category);
            }
            return categoryList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/polls/{pollId}/vote", consumes = "application/json")
    public void voteToPoll(@PathVariable int pollId, @RequestBody VoteTO voteTO) {
        try (Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PreparedStatement stm = connection
                        .prepareStatement("INSERT INTO votes (poll_id, option_id) VALUES (?, ?)");
                stm.setInt(1, pollId);
                stm.setInt(2, voteTO.getOptionId());
                stm.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Failed to add the vote", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
