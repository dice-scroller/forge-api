package forge.dto;

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

    public static StackDTO fromStack(MagicStack mStack) {
        System.out.println("Mapping stack");
        var array = new ArrayList<SpellAbilityDTO>();
        mStack.forEach(
                spellAbilityStackInstance ->
                        array.add(new SpellAbilityDTO(
                                spellAbilityStackInstance.getSpellAbility().toString(),
                                spellAbilityStackInstance.getSourceCard().getId(),
                                spellAbilityStackInstance.getTargetChoices().getTargetCards().getFirst().getId() // FIXME this only works for right now, needs to be updated for multiple targets
                        )
                )
        );

        return new StackDTO(array);
    }
}
