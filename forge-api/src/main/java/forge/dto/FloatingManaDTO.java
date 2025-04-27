package forge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import forge.card.mana.ManaAtom;
import forge.game.card.Card;
import forge.game.mana.Mana;
import forge.game.player.Player;
import lombok.Data;

import java.util.ArrayList;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class FloatingManaDTO {
    public int w = 0;
    public int u = 0;
    public int b = 0;
    public int r = 0;
    public int g = 0;
    public int c = 0;

    /**
     * Adds the Mana of the instance to the Player's mana pool
     *
     * @param player
     */
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

    /**
     *
     * @param player
     * @return Floating mana the player currently has
     */
    public static FloatingManaDTO getFromPlayer(Player player) {

        // TODO refactor
        var newFloating = new FloatingManaDTO();

        var w = player.getManaPool().getAmountOfColor(ManaAtom.fromName("w"));
        var u = player.getManaPool().getAmountOfColor(ManaAtom.fromName("u"));
        var b = player.getManaPool().getAmountOfColor(ManaAtom.fromName("b"));
        var r = player.getManaPool().getAmountOfColor(ManaAtom.fromName("r"));
        var g = player.getManaPool().getAmountOfColor(ManaAtom.fromName("g"));
        var c = player.getManaPool().getAmountOfColor(ManaAtom.fromName("c"));

        newFloating.setW(w);
        newFloating.setU(u);
        newFloating.setB(b);
        newFloating.setR(r);
        newFloating.setG(g);
        newFloating.setC(c);

        return newFloating;
    }
}
