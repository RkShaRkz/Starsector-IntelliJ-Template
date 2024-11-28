package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedAmmo extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedAmmo";
    public static boolean DISABLE_FOR_PLAYER = false;
    public static boolean DISABLE_FOR_ENEMY = false;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // Do nothing if it's been disabled
        if (stats.getFleetMember() != null) {
            int owner = stats.getFleetMember().getOwner();
            if (owner == MiscUtils.OWNER_ENEMY && DISABLE_FOR_ENEMY) return;
            if (owner == MiscUtils.OWNER_PLAYER && DISABLE_FOR_PLAYER) return;
        } else {
            MiscUtils.log(LoggerLogLevel.WARN, LOGTAG, "Encountered a ship with NULL fleetmember in 'applyEffectsBeforeShipCreation' so settings to disable it won't work");
        }

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        // Do nothing if it's been disabled
        int owner = ship.getOwner();
        if (owner == MiscUtils.OWNER_ENEMY && DISABLE_FOR_ENEMY) return;
        if (owner == MiscUtils.OWNER_PLAYER && DISABLE_FOR_PLAYER) return;

        // Carry on as usual
        MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();

        if (stats == null || engine == null || engine.isPaused()) {
            return;
        }

        for (DamagingProjectileAPI p : engine.getProjectiles()) {
            if (ship.equals(p.getSource()) && p.getWeapon() != null) {
                if (p.getDamageType() != DamageType.FRAGMENTATION
                        && p.getDamageType() != DamageType.OTHER
                        && (p.getWeapon().getType() == WeaponType.BALLISTIC
                        || p.getWeapon().getType() == WeaponType.MISSILE)) {
                    p.getDamage().setType(DamageType.FRAGMENTATION);
                }
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {

        if (index == 0) {
            return "Fragmentation";
        }
        if (index >= 1) {
            return CompromisedStructure.getCostDescParam(index, 1);
        }
        return null;
    }
}
