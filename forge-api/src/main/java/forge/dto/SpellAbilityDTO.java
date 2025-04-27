package forge.dto;

import forge.game.spellability.SpellAbility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class SpellAbilityDTO {
    public String name;
    /**
     * The source should be in the form of a card id
     */
    public int source;


    /**
     * The target should be card id for now
     * TODO implement targeting players and effects etc
     */
    public int target;

    public static SpellAbilityDTO fromSpellAbility(SpellAbility sa) {
        return new SpellAbilityDTO(sa.toString(), sa.getId(), sa.getTargetCard().getId());
    }
}
