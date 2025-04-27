package forge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import forge.ai.ComputerUtil;
import forge.ai.ComputerUtilAbility;
import forge.ai.ComputerUtilCost;
import forge.card.mana.ManaAtom;
import forge.deck.Deck;
import forge.dto.Act;
import forge.dto.InputAction;
import forge.dto.ReponseAction;
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
                if (Objects.equals(action.action, name)) {
                    var id = actions.get(i).getId();
                    System.out.println("ID: " + id);
                    sa = actions.get(i);
                }
            }

            System.out.println(humanPlayer.getManaPool().getAmountOfColor(ManaAtom.fromName("r")));
            sa.setActivatingPlayer(humanPlayer);

            var target = game.getCardsInGame().stream()
                    .filter(c -> c.getId() == action.target)
                    .findFirst()
                    .orElse(null);

            sa.setTargetCard(target);

            var payment = new CostPayment(sa.getPayCosts(), sa);

            var paid = payment.payCost(new ApiCostDecisionMaker(humanPlayer, sa));

            if (paid) {
                game.getStack().add(sa);
                System.out.println("Put on stack");
                System.out.println(game.getStack().peekAbility());
                game.getStack().resolveStack();
            } else {
                System.out.println("Nope");
            }

            System.out.println("Activated: " + sa.getActivationsThisTurn()); // IDK what this does, did it not get cast
            System.out.println(humanPlayer.getManaPool().getAmountOfColor(ManaAtom.fromName("r"))); // Mana was paid correctly (?)

            System.out.println("Toughness: " + target.getCurrentToughness()); //FIXME this didn't actual do damage?

            ctx.json(Map.of("status", "Ok", "received", action));

        });

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

                for (SpellAbility sb : actions) {
                    System.out.println("Cost: " + sb.getCostDescription());
                    System.out.println("SpellAbility: " + sb.toString());
                    System.out.println("Can play mechanically: " + sb.canPlay());

                    var newAction = new ReponseAction(
                            sb.toString(),
                            sb.canPlay()
                    );
                    responseActions.add(newAction);

                    if (sb.usesTargeting()) {
                        var cardTargets = CardUtil.getValidCardsToTarget(sb);
                        newAction.setCardTargets(
                                cardTargets.stream()
                                        .map(Card::getId)
                                        .toList()
                        );
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