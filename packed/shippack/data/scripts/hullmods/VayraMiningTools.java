package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VayraMiningTools extends BaseHullMod {

    public static final List<String> WEAPON_IDS = new ArrayList<>(Arrays.asList(
            "vayra_lr_mining_laser",
            "vayra_mining_lance"));
    public static final List<String> ACTIVE_LIST = new ArrayList<>();
    public static final int WEAPON_OP = 2;

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        ShipVariantAPI variant = ship.getVariant();
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();

        if (ACTIVE_LIST.isEmpty()) {
            for (String weaponId : WEAPON_IDS) {
                ACTIVE_LIST.add(weaponId);
            }
        }

        if (stats != null && variant.getUnusedOP(stats) >= WEAPON_OP) {
            String weaponId = ACTIVE_LIST.get(0);
            ACTIVE_LIST.remove(0);
            for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                WeaponSpecAPI weapon = Global.getSettings().getWeaponSpec(weaponId);
                if (slot.getWeaponType().equals(weapon.getType())
                        && slot.getSlotSize().equals(weapon.getSize())
                        && variant.getUnusedOP(stats) >= WEAPON_OP) {
                    String slotId = slot.getId();
                    String currentWeapon = variant.getWeaponId(slotId);
                    if (currentWeapon == null) {
                        variant.addWeapon(slotId, weaponId);
                        break;
                    }
                }
            }
        }

        /* // Shouldn't need this now that we have VayraInventoryStripper
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet != null) {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            for (String weaponId : WEAPON_IDS) {
                while (cargo.getNumWeapons(weaponId) > 0) {
                    cargo.removeWeapons(weaponId, 1);
                }
            }
        }*/
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "semi-modular modified mining lasers";
        }
        if (index == 1) {
            return "automatically be equipped";
        }
        if (index == 2) {
            return WEAPON_OP + " ordnance points";
        }
        if (index == 3) {
            return "cannot be attached to any other ship";
        }
        return null;
    }
}
