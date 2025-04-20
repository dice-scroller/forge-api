package forge.dto;

import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.player.PlayerCollection;

import java.util.ArrayList;
import java.util.List;

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
