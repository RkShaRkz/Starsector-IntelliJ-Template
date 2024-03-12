package data.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

import java.util.List;
import java.util.Map;

public class NGCKadurTrySetStartingLocation extends BaseCommandPlugin {


    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        String id = params.get(0).getString(memoryMap);
        float x = params.get(1).getFloat(memoryMap);
        float y = params.get(2).getFloat(memoryMap);

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        if (id.equals("Kumari")) {
            data.setStartingLocationName("Kumari Kandam");
            data.getStartingCoordinates().set(x, y);
        } else {
            data.setStartingLocationName(id);
            data.getStartingCoordinates().set(x, y);
        }

        return true;
    }

}
