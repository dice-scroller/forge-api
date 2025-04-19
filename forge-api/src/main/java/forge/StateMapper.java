package forge;

import forge.deck.Deck;
import forge.dto.CardDTO;
import forge.dto.GameStateDTO;
import forge.game.Game;
import forge.game.GameRules;
import forge.game.GameType;
import forge.game.Match;
import forge.game.card.Card;
import forge.game.card.CardFactory;
import forge.game.player.RegisteredPlayer;
import forge.game.zone.ZoneType;
import forge.item.PaperCard;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.ArrayList;
import java.util.List;

public class StateMapper {
    public static Game StateToGame(GameStateDTO state) {
        System.out.println("Start loading state");
        Deck deck = new Deck();
        // PlayerProfile profile = new PlayerProfile("AI Player");
        List<RegisteredPlayer> players = new ArrayList<>();
        RegisteredPlayer player = new RegisteredPlayer(deck);
        player.setPlayer(new DummyLobbyPlayer("Test"));
        players.add(player);
        GameRules rules = new GameRules(GameType.Constructed);
        Match match = new Match(rules, players, "Title");
        var game =  new Game(players, rules, match);

        game.getPlayer(0).setLife(state.PlayerLife, null); // TODO this should use a getter

        // Load battlefield

        List<CardDTO> battlefield = state.PlayerBoard;

        for (CardDTO dto : battlefield) {
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.IsTapped);
            var currentPlayer = game.getPlayer(0);
            currentPlayer.getZone(ZoneType.Battlefield).add(card);
        }



        System.out.println("Done loading state");
        return game;
    }

    public static GameStateDTO GameToState(Game game) {
        // TODO
        return null;
    }

    public static Card CardDTOToCard(CardDTO cardDTO, Game game){
        assertNotNull(cardDTO); // crash shit
        PaperCard paperCard = StaticData.instance().getCommonCards().getCard(cardDTO.name);

        return CardFactory.getCard(paperCard, game.getPlayer(0), cardDTO.id, game);
    }
}