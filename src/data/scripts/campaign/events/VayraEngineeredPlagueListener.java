package data.scripts.campaign.events;

import data.scripts.campaign.intel.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import static data.scripts.campaign.intel.VayraGhostShipIntel.PLAGUE_PLANET;
import static data.scripts.hullmods.VayraGhostShip.MYSTERY_PLAGUE_HULLMOD;
import static data.scripts.hullmods.VayraGhostShip.PLAGUE_CONDITION;
import static data.scripts.hullmods.VayraGhostShip.PLAGUE_HULLMOD;
import org.apache.log4j.Logger;

public class VayraEngineeredPlagueListener extends BaseCampaignEventListener implements ColonyPlayerHostileActListener {

    public static Logger log = Global.getLogger(VayraEngineeredPlagueListener.class);

    public VayraEngineeredPlagueListener(boolean permaRegister) {
        super(permaRegister);
    }

    @Override
    public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, CargoAPI cargo) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI test : fleet.getFleetData().getMembersInPriorityOrder()) {
            if (test.getVariant() != null 
                    && !market.hasCondition(PLAGUE_CONDITION)
                    && (test.getVariant().getHullMods().contains(PLAGUE_HULLMOD) 
                    || test.getVariant().getHullMods().contains(MYSTERY_PLAGUE_HULLMOD))) {
                market.addCondition(PLAGUE_CONDITION);
                market.setSurveyLevel(SurveyLevel.FULL);
                market.getCondition(PLAGUE_CONDITION).setSurveyed(true);
                
                log.info(market.getName() + " has the plague now");

                boolean known = true;
                if (test.getVariant().hasHullMod(MYSTERY_PLAGUE_HULLMOD)) {
                    test.getVariant().removePermaMod(MYSTERY_PLAGUE_HULLMOD);
                    test.getVariant().addPermaMod(PLAGUE_HULLMOD);
                    known = false;
                }
                VayraGhostShipIntel intel = new VayraGhostShipIntel(PLAGUE_PLANET, test, known, market);
                Global.getSector().getIntelManager().addIntel(intel);
            }
        }
    }

    @Override
    public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, Industry industry) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI test : fleet.getFleetData().getMembersInPriorityOrder()) {
            if (test.getVariant() != null 
                    && !market.hasCondition(PLAGUE_CONDITION)
                    && (test.getVariant().getHullMods().contains(PLAGUE_HULLMOD) 
                    || test.getVariant().getHullMods().contains(MYSTERY_PLAGUE_HULLMOD))) {
                market.addCondition(PLAGUE_CONDITION);
                market.setSurveyLevel(SurveyLevel.FULL);
                market.getCondition(PLAGUE_CONDITION).setSurveyed(true);
                
                log.info(market.getName() + " has the plague now");

                boolean known = true;
                if (test.getVariant().hasHullMod(MYSTERY_PLAGUE_HULLMOD)) {
                    test.getVariant().removePermaMod(MYSTERY_PLAGUE_HULLMOD);
                    test.getVariant().addPermaMod(PLAGUE_HULLMOD);
                    known = false;
                }
                VayraGhostShipIntel intel = new VayraGhostShipIntel(PLAGUE_PLANET, test, known, market);
                Global.getSector().getIntelManager().addIntel(intel);
            }
        }
    }

    @Override
    public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        // nothing doing
    }

    @Override
    public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        // nothing doing
    }
}
