package lk.abc.app.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteTO implements Serializable {
    private int voteId;
    private int pollId;
    private int optionId;
}
