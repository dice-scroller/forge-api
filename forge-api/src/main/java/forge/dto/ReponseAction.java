package forge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReponseAction {
    public String name;
    public boolean canActivate;
    public List<Integer> cardTargets;
    public List<String> playerTargets;

    public List<Integer> getCardTargets() {
        return cardTargets;
    }

    public void setCardTargets(List<Integer> cardTargets) {
        this.cardTargets = cardTargets;
    }

    public List<String> getPlayerTargets() {
        return playerTargets;
    }

    public void setPlayerTargets(List<String> playerTargets) {
        this.playerTargets = playerTargets;
    }

    public ReponseAction(String name, boolean canActivate) {
        this.name = name;
        this.canActivate = canActivate;
    }
}
