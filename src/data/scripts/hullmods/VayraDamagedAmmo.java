package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

import java.util.Random;

public class VayraDamagedAmmo extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedAmmo";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // Do nothing if it's been disabled
        if (stats.getFleetMember() != null) {
            int owner = stats.getFleetMember().getOwner();
            if (DISABLE_FOR_ENEMY && MiscUtils.OWNER_ENEMY == owner) return;
            if (DISABLE_FOR_PLAYER && MiscUtils.OWNER_PLAYER == owner) return;
        } else {
            MiscUtils.log(LoggerLogLevel.WARN, LOGTAG, "Encountered a ship with NULL fleetmember in 'applyEffectsBeforeShipCreation' so settings to disable it won't work");
        }

        // Carry on as usual
        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        // Do nothing if it's been disabled
        int owner = ship.getOwner();
        if (DISABLE_FOR_ENEMY && MiscUtils.OWNER_ENEMY == owner) return;
        if (DISABLE_FOR_PLAYER && MiscUtils.OWNER_PLAYER == owner) return;

        // Carry on as usual
        MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();

        if (stats == null || engine == null || engine.isPaused()) {
            return;
        }

        for (DamagingProjectileAPI projectile : engine.getProjectiles()) {
            if (ship.equals(projectile.getSource()) && projectile.getWeapon() != null) {
                boolean isNotFragmentationDamage = projectile.getDamageType() != DamageType.FRAGMENTATION;
                boolean isNotOtherDamage = projectile.getDamageType() != DamageType.OTHER;
                boolean isBallisticOrMissileWeapon =
                        (projectile.getWeapon().getType() == WeaponType.BALLISTIC
                        || projectile.getWeapon().getType() == WeaponType.MISSILE);

                if (isNotFragmentationDamage && isNotOtherDamage && isBallisticOrMissileWeapon) {
                    if (shouldConvertProjectileToFragmentationDamage(ship)) {
                        projectile.getDamage().setType(DamageType.FRAGMENTATION);
                    }
                }
            }
        }
    }

    /**
     * If the ship has the "Rugged Construction" hullmod, throw a die, and convert to fragmentation only 50% of the time.
     * Otherwise, convert to fragmentation all the time
     * @param ship the ship to query for the hullmod
     */
    private boolean shouldConvertProjectileToFragmentationDamage(ShipAPI ship) {
        boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(ship.getVariant());
        boolean retVal; //assume true
        int dieRoll = MiscUtils.generateRandomInt(100);
        if (hasRugged) {
            // do not turn into FRAGMENTATION if we rolled less than 75
            retVal = dieRoll >= 75;
        } else {
            retVal = dieRoll >= 50;
        }

        return retVal;
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(ship.getVariant());
        if (index == 0) {
            return "Fragmentation";
        }
        if (index == 1) {
            return hasRugged ? "25%" : "50%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }
}
