package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.intel.VayraUniqueBountyManager.UniqueBountyData;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;

// script originally by DarkRevenant, used under license - which at the time of this writing is:
// Code is free to copy, modify, and redistribute. Attribution must be made to the original creator, DarkRevenant.
public class VayraUniqueBountyFleetEncounterContext extends FleetEncounterContext {

    public static Logger log = Global.getLogger(VayraUniqueBountyFleetEncounterContext.class);

    @Override
    public List<FleetMemberAPI> getRecoverableShips(BattleAPI battle, CampaignFleetAPI winningFleet, CampaignFleetAPI otherFleet) {

        log.info("triggering VayraUniqueBountyFleetEncounterContext");

        List<FleetMemberAPI> result = super.getRecoverableShips(battle, winningFleet, otherFleet);

        if (Misc.isPlayerOrCombinedContainingPlayer(otherFleet)) {
            return result;
        }

        DataForEncounterSide winnerData = getDataFor(winningFleet);

        float playerContribMult = computePlayerContribFraction();
        List<FleetMemberData> enemyCasualties = winnerData.getEnemyCasualties();

        List<String> uniques = new ArrayList<>();

        VayraUniqueBountyManager manager = VayraUniqueBountyManager.getInstance();
        if (manager != null) {
            manager.reload();
            for (String id : manager.getBountiesList()) {
                UniqueBountyData data = manager.getBounty(id);
                ShipVariantAPI variant = Global.getSettings().getVariant(data.flagshipVariantId);
                if (variant != null && variant.getHullSpec() != null) {
                    float chance = data.chanceToAutoRecover;
                    float roll = (float) Math.random();

                    if (VAYRA_DEBUG) {
                        log.info("rolled " + roll + " vs roll-under difficulty of " + chance + " to recover unique " + variant.getFullDesignationWithHullName());
                    }

                    if (roll <= chance) {
                        String hullId = variant.getHullSpec().getHullId();
                        uniques.add(hullId);

                        if (VAYRA_DEBUG) {
                            log.info("adding " + variant.getFullDesignationWithHullName() + " to uniques list for autorecovery");
                        }
                    }
                }
            }
        }

        for (FleetMemberData data : enemyCasualties) {

            if (data.getMember().getHullSpec().getHints().contains(ShipTypeHints.UNBOARDABLE)) {
                continue;
            }

            if ((data.getStatus() != Status.DISABLED) && (data.getStatus() != Status.DESTROYED)) {
                continue;
            }

            // Unique bounties only
            if (data.getMember() != null && data.getMember().getHullSpec() != null) {
                String hullId = data.getMember().getHullSpec().getHullId();
                if (!uniques.contains(hullId)) {
                    continue;
                }
            }

            // Don't double-add
            if (result.contains(data.getMember())) {
                continue;
            }

            if (playerContribMult > 0f) {
                data.getMember().setCaptain(Global.getFactory().createPerson());

                ShipVariantAPI variant = data.getMember().getVariant();
                variant = variant.clone();
                variant.setSource(VariantSource.REFIT);
                variant.setOriginalVariant(null);
                data.getMember().setVariant(variant, false, true);


                if (VAYRA_DEBUG) {
                    log.info("autorecovering " + variant.getFullDesignationWithHullName());
                }

                // Completely fuck this ship up
                Random dModRandom = new Random(1000000L * data.getMember().getId().hashCode() + Global.getSector().getPlayerBattleSeed());
                dModRandom = Misc.getRandom(dModRandom.nextLong(), 5);

                // set a number of D-mods here...
                float dp = data.getMember().getBaseDeployCost();
                int num = (int) (dp / 5f);
                if (num < 4) num = 4;

                DModManager.addDMods(data.getMember(), true, num, dModRandom);
                if (DModManager.getNumDMods(variant) > 0) {
                    DModManager.setDHull(variant);
                }

                float weaponProb = Global.getSettings().getFloat("salvageWeaponProb");
                float wingProb = Global.getSettings().getFloat("salvageWingProb");

                prepareShipForRecovery(
                        data.getMember(),
                        false,
                        true,
                        false, //SHARK added this
                        weaponProb,
                        wingProb,
                        getSalvageRandom()
                );

                result.add(data.getMember());
            }
        }

        return result;
    }
}
