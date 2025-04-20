package forge.dto;

import forge.card.mana.ManaAtom;
import forge.game.card.Card;
import forge.game.mana.Mana;
import forge.game.player.Player;

import java.util.ArrayList;
import java.util.Map;

public class FloatingManaDTO {
    public int w;
    public int u;
    public int b;
    public int r;
    public int g;
    public int c;

    public void addToPlayer(Player player) {
        var card = new Card(-1, player.getGame()); // I hope this doesn't break things
        var manaList = new ArrayList<Mana>();

        Map<String, Integer> manaMap = Map.of(
                "w", this.w,
                "u", this.u,
                "b", this.b,
                "r", this.r,
                "g", this.g,
                "c", this.c
        );

        for (Map.Entry<String, Integer> entry : manaMap.entrySet()) {
            String color = entry.getKey();
            int amount = entry.getValue();
            byte manaType = ManaAtom.fromName(color);

            for (int i = 0; i < amount; i++) {
                manaList.add(new Mana(manaType, card, null));
            }
            System.out.println("Adding mana: "+ amount + color);
        }

        player.getManaPool().add(manaList);
    }
}
