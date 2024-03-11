package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class NGCKadurCommieStartScript extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        data.addScript(new Script() {
            @Override
            public void run() {

                SharedData.getData().getPersonBountyEventData().addParticipatingFaction(Factions.PIRATES);

                String parentId = Factions.PIRATES;
                FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
                FactionAPI parent = Global.getSector().getFaction(parentId);
                for (FactionAPI other : Global.getSector().getAllFactions()) {
                    player.setRelationship(other.getId(), parent.getRelationship(other.getId()));
                }
                
                // pirates
                player.setRelationship(Factions.PIRATES, RepLevel.NEUTRAL);
                
                // PDPRC allies
                player.setRelationship("shadow_industry", RepLevel.FAVORABLE);
                player.setRelationship("junk_pirates", RepLevel.FAVORABLE);
                player.setRelationship("pack", RepLevel.FAVORABLE);
                player.setRelationship("dassault_mikoyan", RepLevel.FAVORABLE);
                player.setRelationship("kadur_remnant", RepLevel.FAVORABLE);
                
                // PDPRC enemies that aren't hostile to pirates
                player.setRelationship(Factions.LUDDIC_PATH, RepLevel.HOSTILE);
                player.setRelationship("tahlan_legioinfernalis", RepLevel.VENGEFUL);

                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                CargoAPI cargo = fleet.getCargo();
                cargo.initPartialsIfNeeded();
                cargo.addCommodity(Commodities.HAND_WEAPONS, 50);

            }

        });
        return true;
    }

}
