package forge.dto;

import java.util.List;

public class GameStateDTO {
    public int PlayerLife;
    public int BotLife;

    public List<CardDTO> PlayerDeck;
    public List<CardDTO> PlayerHand;
    public List<CardDTO> PlayerBoard;
    public List<CardDTO> PlayerExile;
    public List<CardDTO> PlayerGrave;

    public List<CardDTO> BotDeck;
    public List<CardDTO> BotHand;
    public List<CardDTO> BotBoard;
    public List<CardDTO> BotExile;
    public List<CardDTO> BotGrave;

    public GameStateDTO() {} // Required by Jackson
}
