package forge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateDTO {
    public String phase;
    public StackDTO stack;

    public int playerLife;
    public FloatingManaDTO playerFloatingMana;
    public List<CardDTO> playerDeck;
    public List<CardDTO> playerHand;
    public List<CardDTO> playerBoard;
    public List<CardDTO> playerExile;
    public List<CardDTO> playerGrave;

    public int botLife;
    public FloatingManaDTO botFloatingMana;
    public List<CardDTO> botDeck;
    public List<CardDTO> botHand;
    public List<CardDTO> botBoard;
    public List<CardDTO> botExile;
    public List<CardDTO> botGrave;
}
