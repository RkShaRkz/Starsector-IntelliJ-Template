package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Vayra
 */
public class NGCKadurMinerStartScript extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
        //final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);

        data.addScript(new Script() {
            @Override
            public void run() {

                String parentId = Factions.INDEPENDENT;
                FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
                FactionAPI parent = Global.getSector().getFaction(parentId);
                for (FactionAPI other : Global.getSector().getAllFactions()) {
                    player.setRelationship(other.getId(), parent.getRelationship(other.getId()));
                }
                player.setRelationship(parentId, RepLevel.WELCOMING);
                player.setRelationship(Factions.PERSEAN, RepLevel.WELCOMING);

            }

        });
        return true;
    }

}
