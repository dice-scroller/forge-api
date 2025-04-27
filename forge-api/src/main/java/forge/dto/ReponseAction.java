package forge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ReponseAction {
    public String name;
    public int actionId;
    public int source;
    public boolean canActivate;
    public List<CardDTO> cardTargets;
    public List<String> playerTargets;

    public ReponseAction(String name, boolean canActivate, int source, int actionId) {
        this.name = name;
        this.canActivate = canActivate;
        this.source = source;
        this.actionId = actionId;
    }
}
