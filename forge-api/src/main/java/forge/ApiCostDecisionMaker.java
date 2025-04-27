package forge;

import forge.game.cost.*;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;

public class ApiCostDecisionMaker extends CostDecisionMakerBase {
    public ApiCostDecisionMaker(Player player, SpellAbility sa) {
        super(player, false, sa, sa.getHostCard()); // "false" = not effect
    }

    @Override
    public boolean paysRightAfterDecision() {
        return true; // Immediately pay after deciding
    }

    @Override
    public PaymentDecision visit(CostBehold cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostGainControl cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostChooseColor cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostChooseCreatureType cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostCollectEvidence cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostDiscard cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostDamage cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostDraw cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostExile cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostExileFromStack cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostExiledMoveToGrave cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostExert cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostEnlist cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostFlipCoin cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostForage cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostRollDice cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostMill cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostAddMana cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostPayLife cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostPayEnergy cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostGainLife cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostPartMana cost) {
        var decision = new PaymentDecision(1); //FIXME This feels wrong

        return decision;
    }

    @Override
    public PaymentDecision visit(CostPromiseGift cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostPutCardToLib cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostTap cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostSacrifice cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostReturn cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostReveal cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostRevealChosen cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostRemoveAnyCounter cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostRemoveCounter cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostPutCounter cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostUntapType cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostUntap cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostUnattach cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostTapType cost) {
        return null;
    }

    @Override
    public PaymentDecision visit(CostPayShards cost) {
        return null;
    }
}
