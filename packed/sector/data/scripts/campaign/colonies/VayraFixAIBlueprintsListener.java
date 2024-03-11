package data.scripts.campaign.colonies;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import static data.scripts.campaign.colonies.VayraColonialManager.INDUSTRY_CORE_ESCAPE_CHANCE;
import java.util.ArrayList;
import java.util.List;

public class VayraFixAIBlueprintsListener implements ShowLootListener {

    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        SectorEntityToken entity;
        MarketAPI market = null;
        entity = dialog.getInteractionTarget();
        if (entity != null) {
            market = entity.getMarket();
        }
        if (market != null && market.getFactionId() != null && market.getFactionId().equals("science_fuckers")) {
            List<CargoStackAPI> stacksCopy = loot.getStacksCopy();
            List<Object> remove = new ArrayList<>();
            for (CargoStackAPI copy : stacksCopy) {
                if (copy.isSpecialStack()) {
                    SpecialItemData data = copy.getSpecialDataIfSpecial();
                    if (data == null) {
                        return;
                    }

                    if (data.getId().equals("ship_bp")) {
                        try {
                            if (Global.getSettings().getHullSpec(data.getData()).getHints().contains(ShipTypeHints.UNBOARDABLE)) {
                                remove.add(copy.getData());
                            }
                        } catch (NullPointerException ex) {
                            //ugh
                        }

                    } else if (data.getId().equals("fighter_bp")) {
                        try {
                            if (Global.getSettings().getFighterWingSpec(data.getData()).getTags().contains("no_sell")) {
                                remove.add(copy.getData());
                            }
                        } catch (NullPointerException ex) {
                            //ugh
                        }
                    } else if (data.getId().equals(Commodities.ALPHA_CORE) && Math.random() < INDUSTRY_CORE_ESCAPE_CHANCE) {
                        remove.add(copy.getData());
                    }
                }
            }
            for (Object data : remove) {
                loot.removeItems(CargoAPI.CargoItemType.SPECIAL, data, 999);
            }
        }
    }
}
