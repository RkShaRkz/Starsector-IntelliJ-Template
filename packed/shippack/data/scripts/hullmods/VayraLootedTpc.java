package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import java.util.HashMap;
import java.util.Map;

public class VayraLootedTpc extends BaseHullMod {

    public static final String WEAPON_ID = "vayra_looted_tpc";
    public static final String HULL_ID = "vayra_mudskipper_xiv";
    public static final String VARIANT = "vayra_mudskipper_xiv_rd";
    public static final int WEAPON_OP = 20;
    public static final float CHANCE_NO_TPC = 0.1312f;
    public static final float CAPACITY_MULT = 0.5f;
    public static final String ALREADY_SET_LIST_KEY = "vayra_already_looted_tpc";

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

        if (stats != null && variant.getUnusedOP(stats) >= WEAPON_OP) {
            for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                WeaponSpecAPI weapon = Global.getSettings().getWeaponSpec(WEAPON_ID);
                if (slot.getWeaponType().equals(WeaponType.HYBRID)
                        && slot.getSlotSize().equals(weapon.getSize())
                        && variant.getUnusedOP(stats) >= WEAPON_OP) {
                    String slotId = slot.getId();
                    String currentWeapon = variant.getWeaponId(slotId);
                    if (currentWeapon == null) {
                        variant.addWeapon(slotId, WEAPON_ID);
                        break;
                    }
                }
            }
        }
        /* // Shouldn't need this now that we have VayraInventoryStripper
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet != null) {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            while (cargo.getNumWeapons(WEAPON_ID) > 0) {
                cargo.removeWeapons(WEAPON_ID, 1);
            }
        }*/
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        CampaignFleetAPI fleet = member.getFleetData() == null ? null : member.getFleetData().getFleet();
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        Map data = Global.getSector().getPersistentData();
        Map<FleetMemberAPI, Boolean> alreadySet = (Map<FleetMemberAPI, Boolean>) data.get(ALREADY_SET_LIST_KEY);
        if (alreadySet == null) {
            alreadySet = new HashMap<>();
            data.put(ALREADY_SET_LIST_KEY, alreadySet);
        }

        if (HULL_ID.equals(member.getHullId()) && !alreadySet.containsKey(member)) {
            if (member.getVariant() != null) {
                if (playerFleet != null && !playerFleet.equals(fleet)) {
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

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "Thermal Pulse Cannon";
        }
        if (index == 1) {
            return "automatically be equipped";
        }
        if (index == 2) {
            return WEAPON_OP + " ordnance points";
        }
        if (index == 3) {
            return "prevents attachment to any other ship";
        }
        if (index == 4) {
            return "significantly reduces extra crew and cargo capacity";
        }
        return null;
    }
}
