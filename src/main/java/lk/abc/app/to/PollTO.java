package lk.abc.app.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollTO implements Serializable {
    private int pollId;
    private String title;
    private int categoryId;
    private List<String> options;
}
