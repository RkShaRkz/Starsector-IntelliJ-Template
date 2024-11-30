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
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class VayraLootedTpc extends BaseHullMod {
    private static final Logger logger = Global.getLogger(VayraLootedTpc.class);

    public static final String HULLMOD_ID = "vayra_looted_tpc";
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
        ShipVariantAPI variant = ship.getVariant();
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        boolean isSmod = isSMod(ship);
        boolean hasHullmod = MiscUtils.hasHullmodAny(ship.getVariant(), HULLMOD_ID);
        int WEAPON_OP_COST = getWeaponOPCost(ship);
        String GIVEN_WEAPON_ID = getWeaponID(ship);

        if (hasHullmod) {
            // If we S-Modded, we want to remove all non-cheap TPCs and "replace" them by placing new TPCs
            // in those empty slots below, but we'll use the S-Mod variant ('cheap');
            // otherwise, we want to remove *all* TPCs and "replace" them with non-S-Mod variants (not 'cheap')
            removeAllLootedTPCsFromShip(ship, !isSmod);

            // Place TPCs in empty slots
            if (stats != null && variant.getUnusedOP(stats) >= WEAPON_OP_COST) {
                for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                    WeaponSpecAPI lootedTPCspec = Global.getSettings().getWeaponSpec(GIVEN_WEAPON_ID);

                    boolean isSlotWeaponTypeHybrid = slot.getWeaponType().equals(WeaponAPI.WeaponType.HYBRID);
                    boolean isSlotSameSizeAsWeapon = slot.getSlotSize().equals(lootedTPCspec.getSize());
                    boolean hasEnoughFreeOPForLootedTPC = variant.getUnusedOP(stats) >= WEAPON_OP_COST;

                    if (isSlotWeaponTypeHybrid && isSlotSameSizeAsWeapon && hasEnoughFreeOPForLootedTPC) {
                        String slotId = slot.getId();
                        String currentWeapon = variant.getWeaponId(slotId);
                        if (currentWeapon == null) {
                            variant.addWeapon(slotId, GIVEN_WEAPON_ID);
                            break;
                        }
                    }

                    /**
                     * TPC Upgrading part
                     */
                /*
                if (isSlotWeaponTypeHybrid && isSlotSameSizeAsWeapon) {
                    String slotId = slot.getId();
                    String currentWeapon = variant.getWeaponId(slotId);
                    if (isSmod) {
                        // If we are S-modded, replace occurances of WEAPON_ID with SMOD_WEAPON_ID
                        if (currentWeapon != null && currentWeapon.equalsIgnoreCase(WEAPON_ID)) {
                            variant.clearSlot(slotId);
                            variant.addWeapon(slotId, SMOD_WEAPON_ID);
                        }
                    } else {
                        // If we're not S-modded, replace occurances of SMOD_WEAPON_ID with WEAPON_ID
                        if (currentWeapon != null && currentWeapon.equalsIgnoreCase(SMOD_WEAPON_ID)) {
                            variant.clearSlot(slotId);
                            variant.addWeapon(slotId, WEAPON_ID);
                        }
                    }
                }
                 */
                }
            }
        } else {
            // If we removed the hullmod, we want to get rid of all looted TPCs from the ship
            removeAllLootedTPCsFromShip(ship, true);
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

            if (HULL_ID.equals(member.getHullId()) && !alreadySet.containsKey(member)) {
                if (member.getVariant() != null) {
                    Boolean TPC = alreadySet.get(member);
                    if (TPC == null) {
                        TPC = Math.random() > CHANCE_NO_TPC;
                        alreadySet.put(member, TPC);
                    }
                    ShipVariantAPI variant = Global.getSettings().getVariant(VARIANT);
                    if (TPC && variant != null) {
                        member.setVariant(variant, false, true);
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
            return "significantly reduces extra crew and cargo capacity";
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
                if (currentWeapon == null) continue;

                if (currentWeapon.equalsIgnoreCase(WEAPON_ID)) {
                    variant.clearSlot(slotId);
                    break;
                }

                if (includeCheapTPC && currentWeapon.equalsIgnoreCase(SMOD_WEAPON_ID)) {
                    variant.clearSlot(slotId);
                    break;
                }
            }
        }
    }
}
