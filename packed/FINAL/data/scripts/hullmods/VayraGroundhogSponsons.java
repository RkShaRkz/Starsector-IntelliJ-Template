package data.scripts.hullmods;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class VayraGroundhogSponsons extends BaseHullMod {

    public static final String LIGHT_BEAM = "vayra_groundhog_light_beam";
    public static final String LIGHT_PROJ = "vayra_groundhog_light_proj";
    public static final String HEAVY_BEAM = "vayra_groundhog_heavy_beam";
    public static final String HEAVY_PROJ = "vayra_groundhog_heavy_proj";

    public static final String LEFT = "_left";
    public static final String RIGHT = "_right";

    public static final String LEFT_SLOT_ID = "LEFT_SPONSON";
    public static final String RIGHT_SLOT_ID = "RIGHT_SPONSON";
    public static final String LEFT_COVER_SLOT_ID = "LEFT_COVER";
    public static final String RIGHT_COVER_SLOT_ID = "RIGHT_COVER";

    public static final int OP_THRESHOLD = 10;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (ship != null) {
            applyEffectsAfterShipCreation(ship, null);
        }

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        ShipVariantAPI variant = ship.getVariant();
        MutableShipStatsAPI shipStats = ship.getMutableStats();
        MutableCharacterStatsAPI captainStats = ship.getCaptain() == null ? null : ship.getCaptain().getStats();

        if (variant == null) {
            return;
        }

        WeaponSpecAPI left = variant.getWeaponSpec(LEFT_SLOT_ID);
        WeaponSpecAPI right = variant.getWeaponSpec(RIGHT_SLOT_ID);

        if (left != null) {
            if (left.isBeam() && left.getOrdnancePointCost(captainStats, shipStats) < OP_THRESHOLD) {
                variant.clearSlot(LEFT_COVER_SLOT_ID);
                variant.addWeapon(LEFT_COVER_SLOT_ID, LIGHT_BEAM + LEFT);
            } else if (!left.isBeam() && left.getOrdnancePointCost(captainStats, shipStats) < OP_THRESHOLD) {
                variant.clearSlot(LEFT_COVER_SLOT_ID);
                variant.addWeapon(LEFT_COVER_SLOT_ID, LIGHT_PROJ + LEFT);
            } else if (left.isBeam() && left.getOrdnancePointCost(captainStats, shipStats) >= OP_THRESHOLD) {
                variant.clearSlot(LEFT_COVER_SLOT_ID);
                variant.addWeapon(LEFT_COVER_SLOT_ID, HEAVY_BEAM + LEFT);
            } else if (!left.isBeam() && left.getOrdnancePointCost(captainStats, shipStats) >= OP_THRESHOLD) {
                variant.clearSlot(LEFT_COVER_SLOT_ID);
                variant.addWeapon(LEFT_COVER_SLOT_ID, HEAVY_PROJ + LEFT);
            }
        } else {
            variant.clearSlot(LEFT_COVER_SLOT_ID);
        }

        if (right != null) {
            if (right.isBeam() && right.getOrdnancePointCost(captainStats, shipStats) < OP_THRESHOLD) {
                variant.clearSlot(RIGHT_COVER_SLOT_ID);
                variant.addWeapon(RIGHT_COVER_SLOT_ID, LIGHT_BEAM + RIGHT);
            } else if (!right.isBeam() && right.getOrdnancePointCost(captainStats, shipStats) < OP_THRESHOLD) {
                variant.clearSlot(RIGHT_COVER_SLOT_ID);
                variant.addWeapon(RIGHT_COVER_SLOT_ID, LIGHT_PROJ + RIGHT);
            } else if (right.isBeam() && right.getOrdnancePointCost(captainStats, shipStats) >= OP_THRESHOLD) {
                variant.clearSlot(RIGHT_COVER_SLOT_ID);
                variant.addWeapon(RIGHT_COVER_SLOT_ID, HEAVY_BEAM + RIGHT);
            } else if (!right.isBeam() && right.getOrdnancePointCost(captainStats, shipStats) >= OP_THRESHOLD) {
                variant.clearSlot(RIGHT_COVER_SLOT_ID);
                variant.addWeapon(RIGHT_COVER_SLOT_ID, HEAVY_PROJ + RIGHT);
            }
        } else {
            variant.clearSlot(RIGHT_COVER_SLOT_ID);
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "impossible";
        }
        return null;
    }
}
