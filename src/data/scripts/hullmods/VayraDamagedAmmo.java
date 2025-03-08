package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedAmmo extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedAmmo";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final int DEFAULT_FRAGMENTATION_CHANCE = 50;
    public static int FRAGMENTATION_CHANCE = DEFAULT_FRAGMENTATION_CHANCE;

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
     * Convert to Fragmentation only 50% of the time, scaled with Dmod efficiency.
     * If the ship has the "Rugged Construction" hullmod, cut that chance in half and convert only about 25% of the time.
     *
     * Throws a die, and returns whether we should convert this shot to Fragmentation or not by comparing against the chance.
     * @param ship the ship to query for the hullmod
     * @return whether we should convert this shot to Fragmentation or not
     * @see #calculateFragmentationChance(ShipAPI)
     * @see #FRAGMENTATION_CHANCE
     */
    private boolean shouldConvertProjectileToFragmentationDamage(ShipAPI ship) {
        boolean retVal; //assume true
        int dieRoll = MiscUtils.generateRandomInt(100);
        float chance = calculateFragmentationChance(ship);
        // Now that we know the chance, see if our die is under the chance; if it is - we won't convert to Fragmentation
        // if it's not - oh well, better luck next time.
        retVal = dieRoll >= (int) chance;

        return retVal;
    }

    private float calculateFragmentationChance(ShipAPI ship) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0 so we don't need to check for rugged
        float retVal;
        if (ship != null) {
            float effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
            retVal = FRAGMENTATION_CHANCE * effect;
        } else {
            retVal = FRAGMENTATION_CHANCE;
        }

        return retVal;
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float chance = calculateFragmentationChance(ship);
        if (index == 0) {
            return "Fragmentation";
        }
        if (index == 1) {
            return Math.round(chance) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }
}
