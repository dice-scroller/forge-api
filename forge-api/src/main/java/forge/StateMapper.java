package forge;

import forge.deck.Deck;
import forge.dto.CardDTO;
import forge.dto.FloatingManaDTO;
import forge.dto.GameStateDTO;
import forge.dto.StackDTO;
import forge.game.Game;
import forge.game.GameRules;
import forge.game.GameType;
import forge.game.Match;
import forge.game.card.Card;
import forge.game.card.CardCollectionView;
import forge.game.card.CardFactory;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
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

        game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER).setLife(state.playerLife, null); // TODO this should use a getter

        // Load

        var currentPlayer = game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER);
        currentPlayer.setFirstController(new ApiPlayerController(game, currentPlayer, lobbyPlayer));
        Assertions.assertNotNull(currentPlayer.getController());

        if (state.playerFloatingMana != null) {
            state.playerFloatingMana.addToPlayer(currentPlayer);
        }

        StateMapper.MapDtoToZone(state.playerBoard, ZoneType.Battlefield, game, currentPlayer);
        StateMapper.MapDtoToZone(state.playerHand, ZoneType.Hand, game, currentPlayer);
        StateMapper.MapDtoToZone(state.playerGrave, ZoneType.Graveyard, game, currentPlayer);
        StateMapper.MapDtoToZone(state.playerExile, ZoneType.Exile, game, currentPlayer);
        StateMapper.MapDtoToZone(state.playerDeck, ZoneType.Library, game, currentPlayer);

        // BOT PLAYER


        game.getPlayer(ApiPlayerEnum.BOT_PLAYER).setLife(state.playerLife, null); // TODO this should use a getter

        // Load

        var currentBotPlayer = game.getPlayer(ApiPlayerEnum.BOT_PLAYER);
        currentBotPlayer.setFirstController(new ApiPlayerController(game, currentBotPlayer, botLobbyPlayer));
        Assertions.assertNotNull(currentBotPlayer.getController());


        if (state.botFloatingMana != null) {
            state.botFloatingMana.addToPlayer(currentBotPlayer);
        }

        StateMapper.MapDtoToZone(state.botBoard, ZoneType.Battlefield, game, currentBotPlayer);
        StateMapper.MapDtoToZone(state.botHand, ZoneType.Hand, game, currentBotPlayer);
        StateMapper.MapDtoToZone(state.botGrave, ZoneType.Graveyard, game, currentBotPlayer);
        StateMapper.MapDtoToZone(state.botExile, ZoneType.Exile, game, currentBotPlayer);
        StateMapper.MapDtoToZone(state.botDeck, ZoneType.Library, game, currentBotPlayer);


        game.getPhaseHandler().devModeSet(MapPhase(state.phase), currentPlayer);
        System.out.println("Phase: " + game.getPhaseHandler().getPhase());

        if (state.getStack() != null && state.getStack().stack != null && !state.getStack().stack.isEmpty()) {
            state.getStack().applyToMagicStack(game);
        }

        System.out.println("### Done loading state ###");
        return game;
    }

    private static void MapDtoToZone(List<CardDTO> dtoZone, ZoneType zone, Game game, Player player) {
        if (dtoZone == null || dtoZone.isEmpty()) {
            return;
        }

        for (CardDTO dto : dtoZone) {
            Card card = CardDTOToCard(dto, game);
            card.setTapped(dto.isTapped);
            player.getZone(zone).add(card);
        }
    }

    public static GameStateDTO GameToState(Game game) {
        System.out.println("Mapping Game -> State");

        final var human = game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER);
        final var bot = game.getPlayer(ApiPlayerEnum.BOT_PLAYER);

        var phase = game.getPhaseHandler().getPhase().toString();
        var stack = StackDTO.fromStack(game.getStack());

        // Human stuff
        var humanLife = human.getLife();
        var humanMana = FloatingManaDTO.getFromPlayer(human);
        var humanLibrary = StateMapper.cardCollectionToList(human.getCardsIn(ZoneType.Library));
        var humanHand = StateMapper.cardCollectionToList(human.getCardsIn(ZoneType.Hand));
        var humanBattlefield = StateMapper.cardCollectionToList(human.getCardsIn(ZoneType.Battlefield));
        var humanGraveyard = StateMapper.cardCollectionToList(human.getCardsIn(ZoneType.Graveyard));
        var humanExile = StateMapper.cardCollectionToList(human.getCardsIn(ZoneType.Exile));


        // Bot stuff
        var botLife = bot.getLife();
        var botMana = FloatingManaDTO.getFromPlayer(bot);
        var botLibrary = StateMapper.cardCollectionToList(bot.getCardsIn(ZoneType.Library));
        var botHand = StateMapper.cardCollectionToList(bot.getCardsIn(ZoneType.Hand));
        var botBattlefield = StateMapper.cardCollectionToList(bot.getCardsIn(ZoneType.Battlefield));
        var botGraveyard = StateMapper.cardCollectionToList(bot.getCardsIn(ZoneType.Graveyard));
        var botExile = StateMapper.cardCollectionToList(bot.getCardsIn(ZoneType.Exile));

        // I HATE JAVA
        return new GameStateDTO(
                phase,
                stack,
                humanLife,
                humanMana,
                humanLibrary,
                humanHand,
                humanBattlefield,
                humanExile,
                humanGraveyard,
                botLife,
                botMana,
                botLibrary,
                botHand,
                botBattlefield,
                botExile,
                botGraveyard
        );
    }

    private static List<CardDTO> cardCollectionToList(CardCollectionView collection) {
        return collection.stream()
                .map(CardDTO::fromCard)
                .toList();
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