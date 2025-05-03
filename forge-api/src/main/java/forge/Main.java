package forge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import forge.ai.ComputerUtilCost;
import forge.deck.Deck;
import forge.dto.*;
import forge.game.*;
import forge.game.card.Card;
import forge.game.card.CardUtil;
import forge.game.combat.CombatUtil;
import forge.game.cost.CostPayment;
import forge.game.player.Player;
import forge.game.player.PlayerPredicates;
import forge.game.player.RegisteredPlayer;
import forge.game.spellability.SpellAbility;
import forge.util.Lang;
import io.javalin.Javalin;
import forge.util.Localizer;
import org.junit.jupiter.api.Assertions;

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

        /*
         * Simple check to verify API is running
         */
        app.get("/", ctx -> ctx.result("It works!"));

        /*
         * Endpoint to evaluate a given action.
         * Should produce a new, changed game state.
         */
        app.post("/act", ctx -> {
            final var action = ctx.bodyAsClass(Act.class);
            System.out.println("Got request to act: " + action.action + " at target: " + action.target);

            final var game = StateMapper.StateToGame(action.state);
            final var humanPlayer = game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER);

            Card card = game.getCardsInGame().stream()
                    .filter(c -> c.getId() == action.source)
                    .findFirst()
                    .orElse(null);

            if (card == null) {
                ctx.json(Map.of("status", "Error, card not found", "received", action));
                return;
            }


            var actions = card.getAllSpellAbilities();

            SpellAbility sa = null;

            for (int i = 0; i < actions.size(); i++) {
                var name = actions.get(i).toString();
                System.out.println(name);
                if (Objects.equals(action.action, name)) {
                    var id = actions.get(i).getId();
                    System.out.println("ID: " + id);
                    sa = actions.get(i);
                }
            }

            sa = actions.get(action.actionId);

            System.out.println("Action:" + action.action);
            Assertions.assertNotNull(sa);

            sa.setActivatingPlayer(humanPlayer);

            var target = game.getCardsInGame().stream()
                    .filter(c -> c.getId() == action.target)
                    .findFirst()
                    .orElse(null);

            sa.setTargetCard(target);

            var payment = new CostPayment(sa.getPayCosts(), sa);

            var paid = payment.payCost(new ApiCostDecisionMaker(humanPlayer, sa));

            if (paid) {
                var before = game.getStack().size();
                game.getStack().add(sa);


                var stackUnchanged = game.getStack().size() == before;
                if (stackUnchanged) {
                    // don't resolve
                    System.out.println("Stack unchanged, not resolving");
                } else {
                    System.out.println("Put on stack");
                    System.out.println(game.getStack().peekAbility());
                    // game.getStack().resolveStack();
                }

                game.getAction().checkStateEffects(false);
            } else {
                System.out.println("Nope");
            }

            var newState = StateMapper.GameToState(game);

            ctx.json(Map.of("status", "Ok", "state", newState));

        });

        /*
         * Endpoint to evaluate possible actions that can be taken.
         * Request contains a source card of which possible actions to be taken should be returned
         *
         * TODO This should work for creatures wanting to attack etc. as well
         */
        app.post("/submit-action", ctx -> {
            InputAction action = ctx.bodyAsClass(InputAction.class);
            System.out.println(action);

            ctx.json(Map.of("status", "Error, card not found", "received", action));
            var game = StateMapper.StateToGame(action.state);
            var humanPlayer = game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER);

            Card card = game.getCardsInGame().stream()
                    .filter(c -> c.getId() == action.source)
                    .findFirst()
                    .orElse(null);

            if (card != null) {
                System.out.println("Selected: " + card.getName());
                var actions = card.getAllSpellAbilities();

                List<String> options = actions.stream()
                        .map(SpellAbility::toString)
                        .toList();
                System.out.println("Options: " + options);


                List<ReponseAction> responseActions = new ArrayList<>();

                var actionIndex = 0;
                for (SpellAbility sb : actions) {
                    System.out.println("Cost: " + sb.getCostDescription());
                    System.out.println("SpellAbility: " + sb.toString());
                    System.out.println("Can play mechanically: " + sb.canPlay());

                    var newAction = new ReponseAction(
                            sb.toString(),
                            sb.canPlay(),
                            sb.getHostCard().getId(),
                            actionIndex
                    );
                    responseActions.add(newAction);
                    actionIndex ++;

                    if (sb.usesTargeting()) {
                        var cardTargets = CardUtil.getValidCardsToTarget(sb);

                        var targetList = new ArrayList<CardDTO>();
                        for (int i = 0; i < cardTargets.size(); i++) {
                            targetList.add(CardDTO.fromCard(cardTargets.get(i)));
                        }
                        newAction.setCardTargets(targetList);
                        System.out.println("Possible Card Targets: " + cardTargets);

                        var targetablePlayers = game.getPlayers().filter(PlayerPredicates.isTargetableBy(sb));
                        newAction.setPlayerTargets(targetablePlayers.stream().map(Player::toString).toList());
                        System.out.println("Player Targets: " + targetablePlayers);
                    }
                    System.out.println("Uses targeting: " + sb.usesTargeting());

                    var manaCost = sb.getPayCosts().getTotalMana();
                    System.out.println("Mana Cost: " + manaCost);
                    var canPay = ComputerUtilCost.canPayCost(sb, humanPlayer, false);

                    System.out.println("Can pay: " + canPay);

                    System.out.println(newAction);
                }

                if (card.isCreature()) {
                    Player defender = game.getPlayer(ApiPlayerEnum.BOT_PLAYER);
                    // Player attacker = game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER);

                    card.setSickness(false); // TODO this should be default - probably overwrite if true based on state json
                    boolean canAttack = CombatUtil.canAttack(card, defender);
                    System.out.println("Can Attack: " + canAttack);
                }
                ctx.json(Map.of("actions", responseActions));
            }

        });

        app.post("/resolve-stack", ctx -> {
            var state = ctx.bodyAsClass(GameStateDTO.class);
            System.out.println(state);

            var game = StateMapper.StateToGame(state);
            // var humanPlayer = game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER);

            if (game.getStack().isEmpty()) {
                ctx.json(Map.of("status", "Error, stack is empty", "received", state));
                return;
            }

            game.getStack().resolveStack();
            game.getAction().checkStateEffects(false);

            var newState = StateMapper.GameToState(game);
            ctx.json(Map.of("status", "Ok", "state", newState));
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