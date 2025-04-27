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
import forge.game.phase.PhaseType;
import forge.game.player.RegisteredPlayer;
import forge.game.zone.ZoneType;
import forge.item.PaperCard;
import org.junit.jupiter.api.Assertions;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.ArrayList;
import java.util.List;

public class StateMapper {
    public static Game StateToGame(GameStateDTO state) {
        System.out.println("### Start loading state ###");
        List<RegisteredPlayer> players = new ArrayList<>();

        // HUMAN PLAYER

        var deck = new Deck();
        var player = new RegisteredPlayer(deck);
        var lobbyPlayer = new ApiLobbyPlayer("API");
        player.setPlayer(lobbyPlayer);
        players.add(player);


        // BOT PLAYER

        var botDeck = new Deck();
        var botPlayer = new RegisteredPlayer(botDeck);
        var botLobbyPlayer = new ApiLobbyPlayer("BOT");
        botPlayer.setPlayer(botLobbyPlayer);
        players.add(botPlayer);

        // GENERAL

        var rules = new GameRules(GameType.Constructed);
        var match = new Match(rules, players, "Title");
        var game =  new Game(players, rules, match);


        // HUMAN

        game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER).setLife(state.PlayerLife, null); // TODO this should use a getter

        // Load

        var currentPlayer = game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER);
        currentPlayer.setFirstController(new ApiPlayerController(game, currentPlayer, lobbyPlayer));
        Assertions.assertNotNull(currentPlayer.getController());

        if (state.PlayerFloatingMana != null) {
            state.PlayerFloatingMana.addToPlayer(currentPlayer);
        }

        List<CardDTO> battlefield = state.PlayerBoard;

        for (CardDTO dto : battlefield) {
            System.out.println("Mapping Battlefield: " + dto.name);
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.IsTapped);
            currentPlayer.getZone(ZoneType.Battlefield).add(card);
        }

        List<CardDTO> hand = state.PlayerHand;
        for (CardDTO dto : hand) {
            System.out.println("Mapping Hand: " + dto.name);
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.IsTapped);
            currentPlayer.getZone(ZoneType.Hand).add(card);
        }


        List<CardDTO> grave = state.PlayerGrave;
        for (CardDTO dto : grave) {
            System.out.println("Mapping Graveyard: " + dto.name);
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.IsTapped);
            currentPlayer.getZone(ZoneType.Graveyard).add(card);
        }


        List<CardDTO> exile = state.PlayerExile;
        for (CardDTO dto : exile) {
            System.out.println("Mapping Exile: " + dto.name);
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.IsTapped);
            currentPlayer.getZone(ZoneType.Exile).add(card);
        }





        // BOT PLAYER


        game.getPlayer(ApiPlayerEnum.BOT_PLAYER).setLife(state.PlayerLife, null); // TODO this should use a getter

        // Load

        var currentBotPlayer = game.getPlayer(ApiPlayerEnum.BOT_PLAYER);
        currentBotPlayer.setFirstController(new ApiPlayerController(game, currentBotPlayer, botLobbyPlayer));
        Assertions.assertNotNull(currentBotPlayer.getController());


        if (state.BotFloatingMana != null) {
            state.BotFloatingMana.addToPlayer(currentBotPlayer);
        }

        List<CardDTO> botBattlefield = state.BotBoard;

        for (CardDTO dto : botBattlefield) {
            System.out.println("Mapping Battlefield: " + dto.name);
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.IsTapped);
            currentBotPlayer.getZone(ZoneType.Battlefield).add(card);
        }

        List<CardDTO> botHand = state.BotHand;
        for (CardDTO dto : botHand) {
            System.out.println("Mapping Hand: " + dto.name);
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.IsTapped);
            currentBotPlayer.getZone(ZoneType.Hand).add(card);
        }


        List<CardDTO> botGrave = state.BotGrave;
        for (CardDTO dto : botGrave) {
            System.out.println("Mapping Graveyard: " + dto.name);
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.IsTapped);
            currentBotPlayer.getZone(ZoneType.Graveyard).add(card);
        }


        List<CardDTO> botExile = state.BotExile;
        for (CardDTO dto : botExile) {
            System.out.println("Mapping Exile: " + dto.name);
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.IsTapped);
            currentBotPlayer.getZone(ZoneType.Exile).add(card);
        }

        game.getPhaseHandler().devModeSet(MapPhase(state.Phase), currentPlayer);
        System.out.println("Phase: " + game.getPhaseHandler().getPhase());

        System.out.println("### Done loading state ###");
        return game;
    }

    public static GameStateDTO GameToState(Game game) {
        // TODO
        return null;
    }

    public static Card CardDTOToCard(CardDTO cardDTO, Game game){
        assertNotNull(cardDTO); // crash shit
        PaperCard paperCard = StaticData.instance().getCommonCards().getCard(cardDTO.name);

        return CardFactory.getCard(paperCard, game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER), cardDTO.id, game); // TODO this needs a try / catch
    }

    public static PhaseType MapPhase(String phase) {

        return switch (phase) {
            case "UNTAP" -> PhaseType.UNTAP;
            case "UPKEEP" -> PhaseType.UPKEEP;
            case "DRAW" -> PhaseType.DRAW;
            case "MAIN1" -> PhaseType.MAIN1;
            case "COMBAT_BEGIN" -> PhaseType.COMBAT_BEGIN;
            case "COMBAT_DECLARE_ATTACKERS" -> PhaseType.COMBAT_DECLARE_ATTACKERS;
            case "COMBAT_DECLARE_BLOCKERS" -> PhaseType.COMBAT_DECLARE_BLOCKERS;
            case "COMBAT_FIRST_STRIKE_DAMAGE" -> PhaseType.COMBAT_FIRST_STRIKE_DAMAGE;
            case "COMBAT_DAMAGE" -> PhaseType.COMBAT_DAMAGE;
            case "COMBAT_END" -> PhaseType.COMBAT_END;
            case "MAIN2" -> PhaseType.MAIN2;
            case "END_OF_TURN" -> PhaseType.END_OF_TURN;
            case "CLEANUP" -> PhaseType.CLEANUP;
            default -> throw new RuntimeException("Unknow Phase: " + phase);
        };
    }
}