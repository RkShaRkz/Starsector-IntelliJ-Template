package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.intel.VayraPlayerBountyIntel.PlayerBountyData;
import org.apache.log4j.Logger;

public class VayraPlayerBountyListener extends BaseCampaignEventListener implements FleetEventListener, ColonyPlayerHostileActListener {

    public static Logger log = Global.getLogger(VayraPlayerBountyListener.class);
    public VayraPlayerBountyIntel intel;

    public static final String RAIDED_KEY = "$vayra_playerRaided";
    public static final String SATBOMBED_KEY = "$vayra_playerSatBombed";
    public static final String SATBOMBED_TARGET_KEY = "$vayra_playerSatBombedTarget";
    
    public static final int PLAYER_BOUNTY_BASE_COMBAT = 750;
    public static final int PLAYER_BOUNTY_BASE_RAID = 250000;
    public static final int PLAYER_BOUNTY_BASE_BOMB = 500000;

    public VayraPlayerBountyListener(boolean permaRegister, VayraPlayerBountyIntel intel) {
        super(permaRegister);
        this.intel = intel;
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if (fleet != null && Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().equals(fleet)) {
            intel.bountiesPosted.clear();
            intel.bountiesToPost.clear();
            log.info("cleared bounties on player on account of their death or fleet despawn");
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

        if (!battle.isPlayerInvolved() || !battle.isPlayerPrimary() || !primaryWinner.equals(Global.getSector().getPlayerFleet())) {
            return;
        }

        for (CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
            float killedFP = 0;

            if (otherFleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT)
                    || otherFleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_NO_REP_IMPACT)) {
                return;
            }

            for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(otherFleet)) {
                if (loss.getHullSpec().getHints().contains(ShipTypeHints.CIVILIAN)) {
                    killedFP += (loss.getFleetPointCost() * 3f);
                } else {
                    killedFP += loss.getFleetPointCost();
                }
            }

            int value = (int) (PLAYER_BOUNTY_BASE_COMBAT * killedFP);
            value *= battle.getPlayerInvolvementFraction();
            float mult = 1f;
            if (otherFleet.getFaction().getRelToPlayer().isAtBest(RepLevel.VENGEFUL)) {
                mult += 0.5f;
            }
            value *= mult;
            int minToCare = (int) (intel.getBountyValue() * 0.2f);
            log.info("might post a bounty on player for " + value);
            if (value >= minToCare) {
                PlayerBountyData data = new PlayerBountyData(otherFleet.getFaction().getId(), value);
                log.info("readying " + value + "cr bounty on player for murder");
                intel.addBounty(data);
            } else {
                log.info("decided it wasn't worth the trouble");
            }
        }
    }

    @Override
    public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, CargoAPI cargo) {
        int value = PLAYER_BOUNTY_BASE_RAID;
        float mult = 1f;
        if (market.getFaction().getRelToPlayer().isAtBest(RepLevel.VENGEFUL)) {
            mult += 0.5f;
        }
        if (market.getSize() >= 5) {
            mult += (market.getSize() * 0.1f);
        }
        value *= mult;
        PlayerBountyData data = new PlayerBountyData(market.getFactionId(), value);
        log.info("readying " + value + "cr bounty on player for theft");
        intel.addBounty(data);
        Global.getSector().getMemoryWithoutUpdate().set(RAIDED_KEY, true, 90);
    }

    @Override
    public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, Industry industry) {
        int value = PLAYER_BOUNTY_BASE_RAID * 2;
        float mult = 1f;
        if (market.getFaction().getRelToPlayer().isAtBest(RepLevel.VENGEFUL)) {
            mult += 0.5f;
        }
        if (market.getSize() >= 5) {
            mult += (market.getSize() * 0.1f);
        }
        value *= mult;
        PlayerBountyData data = new PlayerBountyData(market.getFactionId(), value);
        log.info("readying " + value + "cr bounty on player for general shenanigans");
        intel.addBounty(data);
    }

    @Override
    public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        int value = PLAYER_BOUNTY_BASE_BOMB;
        float mult = 1f;
        if (market.getFaction().getRelToPlayer().isAtBest(RepLevel.VENGEFUL)) {
            mult += 0.5f;
        }
        if (market.getSize() >= 5) {
            mult += (market.getSize() * 0.1f);
        }
        value *= mult;
        PlayerBountyData data = new PlayerBountyData(market.getFactionId(), value);
        log.info("readying " + value + "cr bounty on player for terrorism");
        intel.addBounty(data);
    }

    @Override
    public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        int value = PLAYER_BOUNTY_BASE_BOMB * 3;
        float mult = 1f;
        if (market.getFaction().getRelToPlayer().isAtBest(RepLevel.VENGEFUL)) {
            mult += 0.5f;
        }
        if (market.getSize() >= 5) {
            mult += (market.getSize() * 0.1f);
        }
        value *= mult;
        PlayerBountyData data = new PlayerBountyData(market.getFactionId(), value);
        log.info("readying " + value + "cr bounty on player for mass murder, genocide, war crimes");
        intel.addBounty(data);
        Global.getSector().getMemoryWithoutUpdate().set(SATBOMBED_KEY, true, 365);
        Global.getSector().getMemoryWithoutUpdate().set(SATBOMBED_TARGET_KEY, market.getName());
    }
}
