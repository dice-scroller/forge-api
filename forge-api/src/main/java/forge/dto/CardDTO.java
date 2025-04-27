package forge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import forge.game.card.Card;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CardDTO {
    public String name;
    public int id;
    public boolean IsTapped = false;

    public CardDTO() {} // Required by Jackson

    public static CardDTO fromCard(Card card) {

        var newCardDTO = new CardDTO();

        newCardDTO.name = card.getName();
        newCardDTO.id = card.getId();
        newCardDTO.IsTapped = card.isTapped();

        return newCardDTO;
    }
}
