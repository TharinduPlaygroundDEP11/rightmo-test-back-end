package lk.abc.app.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;

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

    @PostMapping(value = "/polls/create")
    public void createPoll() {
        System.out.println("Create Poll");
    }

    @PatchMapping("/polls/edit/{pollId}")
    public void editPoll(){
        System.out.println("Edit Poll");
    }

    @DeleteMapping("/polls/delete/{pollId}")
    public void deletePoll() {
        System.out.println("Delete Poll");
    }

    @GetMapping("/polls/{pollId}")
    public void findPollById(){
        System.out.println("Find by Id");
    }

    @GetMapping("/polls")
    public void getAllPolls(){
        System.out.println("Get all polls");
    }

    @GetMapping("/categories")
    public void getAllCategories(){
        System.out.println("Get all categories");
    }

    @PostMapping("/polls/{pollId}/vote")
    public void voteToPoll() {
        System.out.println("Vote to poll");
    }
}
