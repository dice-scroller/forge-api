package forge.dto;

import forge.ApiPlayerEnum;
import forge.StateMapper;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.spellability.SpellAbility;
import forge.game.zone.MagicStack;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * The MTG stack
 */
@NoArgsConstructor
public class StackDTO {
    public List<SpellAbilityDTO> stack;

    public StackDTO(List<SpellAbilityDTO> stack) {
        this.stack = stack;
    }

    private SpellAbility findAbilityByName(SpellAbilityDTO spellAbilityDTO, Game game) {
        Card card = game.getCardsInGame().stream()
                .filter(c -> c.getId() == spellAbilityDTO.getSource().id)
                .findFirst()
                .orElse(null);

        assert card != null;
        var actions = card.getAllSpellAbilities();

        for (SpellAbility sa : actions) {
            if (sa.toString().trim().equals(spellAbilityDTO.name.trim())) {
                return sa;
            }
        }

        return null;
    }

    public void applyToMagicStack(Game game) {
        System.out.println("Mapping to MagicStack");
        var mStack = game.getStack();

        if (stack == null || stack.isEmpty()) {
            System.out.println("Stack is empty");
            return;
        }

        for (SpellAbilityDTO action : this.stack) {

            var sa = findAbilityByName(action, game);
            // assert sa != null;
            sa.setActivatingPlayer(game.getPlayer(ApiPlayerEnum.HUMAN_PLAYER)); // FIXME!!! - THIS WILL MEAN THAT ALL SPELLS ARE CAST MY THE PLAYER FOR NOW
            System.out.println(sa);

            // targeting logic
            if (sa.usesTargeting()) {
                sa.setTargetCard(StateMapper.CardDTOToCard(action.getTarget(), game));
            }

            mStack.add(sa);
        }
    };

    public static StackDTO fromStack(MagicStack mStack) {
        System.out.println("Mapping stack");
        var array = new ArrayList<SpellAbilityDTO>();
        mStack.forEach(
            spellAbilityStackInstance ->
                array.add(new SpellAbilityDTO(
                    spellAbilityStackInstance.getSpellAbility().toString(),
                    CardDTO.fromCard(spellAbilityStackInstance.getSourceCard()),
                    CardDTO.fromCard(spellAbilityStackInstance.getTargetChoices().getTargetCards().getFirst()) // FIXME this only works for right now, needs to be updated for multiple targets
                )
            )
        );

        return new StackDTO(array);
    }
}
