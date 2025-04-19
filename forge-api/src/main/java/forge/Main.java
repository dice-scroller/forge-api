package forge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import forge.deck.Deck;
import forge.dto.InputAction;
import forge.game.Game;
import forge.game.GameRules;
import forge.game.GameType;
import forge.game.Match;
import forge.game.player.RegisteredPlayer;
import forge.util.Lang;
import io.javalin.Javalin;
import forge.util.Localizer;

public class Main {
    public static void main(String[] args) {
        // Init
        Localizer.getInstance().initialize("en-US", "forge-gui/res/languages");
        Lang.createInstance("en-US");
        FTrace.initialize();

        ImageKeys.initializeDirs(
                "res/pics/cards",
                Map.of(), // or a map of edition → folder if needed
                "res/pics/tokens",
                "res/pics/icons",
                "res/pics/boosters",
                "res/pics/fatpacks",
                "res/pics/boosterboxes",
                "res/pics/precons",
                "res/pics/tournamentpacks"
        );
        CardStorageReader customCardReader = null; // or a valid one if you support custom cards

        CardStorageReader cardReader = new CardStorageReader("forge-gui/res/cardsfolder", null, false);
        String editionFolder = "forge-gui/res/editions";
        String customEditionFolder = "forge-gui/res/editions_custom";
        String blockDataFolder = "forge-gui/res/blocks";
        String artPreference = "latest";
        boolean enableUnknownCards = true;
        boolean loadNonLegalCards = true;

        // Create and assign the singleton instance
        StaticData data = new StaticData(
                cardReader,
                customCardReader,
                editionFolder,
                customEditionFolder,
                blockDataFolder,
                artPreference,
                enableUnknownCards,
                loadNonLegalCards
        );
        Javalin app = Javalin.create().start(7000);

        app.get("/", ctx -> ctx.result("It works!"));

        app.post("/submit-action", ctx -> {
            InputAction action = ctx.bodyAsClass(InputAction.class);
            System.out.println(action);

            var game = StateMapper.StateToGame(action.state);

            System.out.println();
            var life = game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER).getLife();
            // Load Game state
            ctx.json(Map.of("status", "ok", "received", action, "life", life));
        });

        app.post("/load-game", ctx -> {
            var game = buildEmptyGameState();
            // Store the game in memory or return its ID
            ctx.status(201).json(Map.of("gameId", game.getId()));
        });

    }

    public static Game buildEmptyGameState() {
        System.out.println("Start loading state");
        Deck deck = new Deck();
        // PlayerProfile profile = new PlayerProfile("AI Player");
        List<RegisteredPlayer> players = new ArrayList<>();
        RegisteredPlayer player = new RegisteredPlayer(deck);
        player.setPlayer(new ApiLobbyPlayer("Test"));
        players.add(player);
        GameRules rules = new GameRules(GameType.Constructed);
        Match match = new Match(rules, players, "Title");
        System.out.println("Done loading state");
        return new Game(players, rules, match);
    }


}