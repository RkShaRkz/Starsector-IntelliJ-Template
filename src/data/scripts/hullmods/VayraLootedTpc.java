package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.sun.javafx.beans.annotations.NonNull;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class VayraLootedTpc extends BaseHullMod {
    private static final Logger logger = Global.getLogger(VayraLootedTpc.class);

    public static final String WEAPON_ID = "vayra_looted_tpc";
    public static final String SMOD_WEAPON_ID = "vayra_looted_tpc_cheaper";
    public static final String HULL_ID = "vayra_mudskipper_xiv";
    public static final String VARIANT = "vayra_mudskipper_xiv_rd";
    public static final int WEAPON_OP = 20;
    public static final int SMOD_WEAPON_OP = 15;
    public static final float CHANCE_NO_TPC = 0.1312f;
    public static final float CAPACITY_MULT = 0.5f;
    public static final String ALREADY_SET_LIST_KEY = "vayra_already_looted_tpc";

    public static final IntervalUtil INTERVAL = new IntervalUtil(1f, 2f);

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getCargoMod().modifyMult(id, CAPACITY_MULT);
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship != null) {
            float diff = ship.getHullSpec().getMaxCrew() - ship.getHullSpec().getMinCrew();
            stats.getMaxCrewMod().modifyFlat(id, -diff);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        boolean isSmod = isSMod(ship);
        int GIVEN_WEAPON_OP_COST = getWeaponOPCost(ship);
        String GIVEN_WEAPON_ID = getWeaponID(ship);

        // If we S-Modded, we want to remove all non-cheap TPCs and "replace" them by placing new TPCs
        // in those empty slots below, but we'll use the S-Mod variant ('cheap');
        // otherwise, we want to remove *all* TPCs and "replace" them with non-S-Mod variants (not 'cheap')
        removeAllLootedTPCsFromShip(ship, !isSmod);

        // Get stats, either from the ship in one of the 4 possible ways that returned valid OP results, or fallback to PlayerStats :/
        MutableCharacterStatsAPI stats = getNonNullStats(ship);

        // Place TPCs in empty slots, we won't check the OP here because it will be checked inside the method
        if (stats != null) {
            int addedTPCs = fillAllEmptyLargeHybridSlotsWithLootedTPC(ship, stats, GIVEN_WEAPON_ID, GIVEN_WEAPON_OP_COST);
            if (addedTPCs > 0) {
                MiscUtils.log(LoggerLogLevel.INFO, logger, String.format("Added %s LootedTPC weapons to ship slots!", addedTPCs), false);
            }
        } else {
            MiscUtils.log(LoggerLogLevel.ERROR, logger, "Didn't call fillAllEmptyLargeHybridSlotsWithLootedTPC() because stats were NULL", false);
        }

        // Finally, remove the looted TPC and cheaper looted TPC from the inventory, if any
        int removedTPCs = removeAllLootedTPCsFromInventory();
        if (removedTPCs > 0) {
            MiscUtils.log(LoggerLogLevel.INFO, logger, String.format("Removed %s LootedTPC weapons from inventory!", removedTPCs), false);
        }
    }


    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        Map<String, Object> data = Global.getSector().getPersistentData();

        INTERVAL.advance(amount);
        if (INTERVAL.intervalElapsed()) {
            Map<FleetMemberAPI, Boolean> alreadySet = getOrInitializeAlreadySetMap(data);

            if (!alreadySet.containsKey(member)) {
                if (member.getVariant() != null) {
                    Boolean TPC = alreadySet.get(member);
                    if (TPC == null) {
                        TPC = Math.random() > CHANCE_NO_TPC;
                        alreadySet.put(member, TPC);
                    }

                    // I believe this part of code is here to prevent you from removing the TPC from the mudskipper
                    // I don't know what this part really does, but it doesn't interfere with this hullmod on any other
                    // ship, so instead of deleting it, let's just leave it in, since it's mudskipper-exclusive *shrug*
                    // and in that variant, it has *only* the looted TPC mounted on itself
                    if (HULL_ID.equals(member.getHullId())) {
                        ShipVariantAPI variant = Global.getSettings().getVariant(VARIANT);
                        if (TPC && variant != null) {
                            member.setVariant(variant, false, true);
                        }
                    }
                }
            }
        }
    }

    private synchronized Map<FleetMemberAPI, Boolean> getOrInitializeAlreadySetMap(Map<String, Object> persistentData) {
        // Just in case many different calls get made to this method, since there's only one instance of this class,
        // lets be safe about this map.
        //
        // Try fetching it
        Map<FleetMemberAPI, Boolean> retVal = (Map<FleetMemberAPI, Boolean>) persistentData.get(ALREADY_SET_LIST_KEY);
        if (retVal == null) {
            synchronized (VayraLootedTpc.class) {
                // IF there was none, initialize it, and put it in the persisted data
                if (retVal == null) {
                    retVal = new HashMap<>();
                    persistentData.put(ALREADY_SET_LIST_KEY, retVal);
                }

                // Finally, return the thing from persisted data
                retVal = (Map<FleetMemberAPI, Boolean>) persistentData.get(ALREADY_SET_LIST_KEY);
            }
        }
        return retVal;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "Thermal Pulse Cannon";
        }
        if (index == 1) {
            return "automatically be equipped";
        }
        if (index == 2) {
            return getWeaponOPCost(ship) + " ordnance points";
        }
        if (index == 3) {
            return "prevents attachment to any other ship";
        }
        if (index == 4) {
            return "significantly reduces extra crew (forced to remain on min. crew) and cargo capacity (-" + CAPACITY_MULT + "%)";
        }
        return null;
    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float oPad = 10f;
        Color good = Misc.getPositiveHighlightColor();

        tooltip.addPara("Replaces every blank LARGE slot with a Looted Thermal Pulse Cannon costing %s OP.", oPad, good, String.valueOf(getWeaponOPCost(ship)));
    }

    @Override
    public void addSModEffectSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {
        float oPad = 10;
        Color good = Misc.getPositiveHighlightColor();

        tooltip.addPara("Lowers the Looted Thermal Pulse Cannon cost to %s.", oPad, good, String.valueOf(SMOD_WEAPON_OP));
    }

    private int getWeaponOPCost(ShipAPI ship) {
        int retVal = WEAPON_OP;

        if (ship != null) {
            retVal = isSMod(ship) ? SMOD_WEAPON_OP : WEAPON_OP;
        }

        return retVal;
    }

    private String getWeaponID(ShipAPI ship) {
        if (ship != null) {
            return isSMod(ship) ? SMOD_WEAPON_ID : WEAPON_ID;
        } else {
            return WEAPON_ID;
        }
    }

    private int removeAllLootedTPCsFromInventory() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        int removedWeapons = 0;
        if (playerFleet != null) {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            // Lets at least try to get rid of all of them in one go
            while (cargo.getNumWeapons(WEAPON_ID) > 0) {
                int howMany = cargo.getNumWeapons(WEAPON_ID);
                cargo.removeWeapons(WEAPON_ID, howMany);
                removedWeapons += howMany;
            }
            while (cargo.getNumWeapons(SMOD_WEAPON_ID) > 0) {
                int howMany = cargo.getNumWeapons(SMOD_WEAPON_ID);
                cargo.removeWeapons(SMOD_WEAPON_ID, howMany);
                removedWeapons += howMany;
            }
        }

        return removedWeapons;
    }

    private void removeAllLootedTPCsFromShip(ShipAPI ship, boolean includeCheapTPC) {
        ShipVariantAPI variant = ship.getVariant();
        // If we S-modded, we should also replace all occurances of old TPCs into new cheaper ones
        for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            WeaponSpecAPI lootedTPCspec = Global.getSettings().getWeaponSpec(SMOD_WEAPON_ID);

            boolean isSlotWeaponTypeHybrid = slot.getWeaponType().equals(WeaponAPI.WeaponType.HYBRID);
            boolean isSlotSameSizeAsWeapon = slot.getSlotSize().equals(lootedTPCspec.getSize());

            if (isSlotWeaponTypeHybrid && isSlotSameSizeAsWeapon) {
                String slotId = slot.getId();
                String currentWeapon = variant.getWeaponId(slotId);
                // Sanity check
                if (currentWeapon == null || currentWeapon.isEmpty()) continue;

                if (currentWeapon.equalsIgnoreCase(WEAPON_ID)) {
                    variant.clearSlot(slotId);
                }

                if (includeCheapTPC && currentWeapon.equalsIgnoreCase(SMOD_WEAPON_ID)) {
                    variant.clearSlot(slotId);
                }
            }
        }
    }

    private int fillAllEmptyLargeHybridSlotsWithLootedTPC(ShipAPI ship, MutableCharacterStatsAPI stats, String GIVEN_WEAPON_ID, int GIVEN_WEAPON_OP_COST) {
        int retVal = 0;
        ShipVariantAPI variant = ship.getVariant();
        WeaponSpecAPI lootedTPCspec = Global.getSettings().getWeaponSpec(GIVEN_WEAPON_ID);
        // Iterate through all weapon slots and fit them with looted TPCs if they match size, type and we have free OP
        for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            int unusedOP = variant.getUnusedOP(stats);
            boolean isSlotWeaponTypeHybrid = slot.getWeaponType().equals(WeaponAPI.WeaponType.HYBRID);
            boolean isSlotSameSizeAsWeapon = slot.getSlotSize().equals(lootedTPCspec.getSize());
            boolean hasEnoughFreeOPForLootedTPC = unusedOP >= GIVEN_WEAPON_OP_COST;


            if (isSlotWeaponTypeHybrid && isSlotSameSizeAsWeapon && hasEnoughFreeOPForLootedTPC) {
                String slotId = slot.getId();
                String currentWeapon = variant.getWeaponId(slotId);
                if (currentWeapon == null || currentWeapon.isEmpty()) {
                    variant.addWeapon(slotId, GIVEN_WEAPON_ID);
                    retVal++;
                }
            }
        }

        return retVal;
    }

    /**
     * Method that tries getting various Stats from the ship, and finally falls back to PlayerStats in case all of these fail
     *
     * It will try getting stats in this order:
     * - ship.getFleetCommander().getFleetCommanderStats()
     * - ship.getFleetCommander().getStats();
     * - ship.getCaptain().getFleetCommanderStats();
     * - ship.getCaptain().getStats();
     * - Global.getSector().getPlayerStats();
     *
     * @param ship the ship from which to get stats
     * @return a non-null instance of stats
     */
    private @NonNull MutableCharacterStatsAPI getNonNullStats(ShipAPI ship) {
        MutableCharacterStatsAPI retVal = null;

        MutableCharacterStatsAPI fallback1 = null;
        MutableCharacterStatsAPI fallback2 = null;
        MutableCharacterStatsAPI fallback3 = null;
        MutableCharacterStatsAPI fallback4 = null;

        if (ship.getFleetCommander() != null) {
            fallback1 = ship.getFleetCommander().getFleetCommanderStats();
            fallback2 = ship.getFleetCommander().getStats();
        }

        if (fallback1 != null) retVal = fallback1;
        if (retVal != null) return retVal;

        if (fallback2 != null) retVal = fallback2;
        if (retVal != null) return retVal;

        if (ship.getCaptain() != null) {
            fallback3 = ship.getCaptain().getFleetCommanderStats();;
            fallback4 = ship.getCaptain().getStats();
        }

        if (fallback3 != null) retVal = fallback3;
        if (retVal != null) return retVal;

        if (fallback4 != null) retVal = fallback4;
        if (retVal != null) return retVal;

        // Finally, if all of these failed, then fuck it and revert to using PlayerStats
        return Global.getSector().getPlayerStats();
    }
}
