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
    public CardDTO source;
    public CardDTO target;

    public static SpellAbilityDTO fromSpellAbility(SpellAbility sa) {
        return new SpellAbilityDTO(sa.toString(), CardDTO.fromCard(sa.getHostCard()), CardDTO.fromCard(sa.getTargetCard()));
    }
}
