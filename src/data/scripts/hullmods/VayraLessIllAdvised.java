package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraLessIllAdvised extends BaseHullMod {
    private static final String LOGTAG = "VayraLessIllAdvised";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DEFAULT_WEAPON_MALFUNCTION_PENALTY = 0.05f;
    public static float WEAPON_MALFUNCTION_PENALTY = DEFAULT_WEAPON_MALFUNCTION_PENALTY;

    public static final float DEFAULT_WEAPON_TURN_RATE_BONUS = 0.1f;
    public static float WEAPON_TURN_RATE_BONUS = DEFAULT_WEAPON_TURN_RATE_BONUS;

    public static final float DEFAULT_ENERGY_WEAPON_RATE_OF_FIRE_BONUS = 0.1f;
    public static float ENERGY_WEAPON_RATE_OF_FIRE_BONUS = DEFAULT_ENERGY_WEAPON_RATE_OF_FIRE_BONUS;
    public static final float DEFAULT_BALLISTIC_WEAPON_RATE_OF_FIRE_BONUS = 0.1f;
    public static float BALLISTIC_WEAPON_RATE_OF_FIRE_BONUS = DEFAULT_BALLISTIC_WEAPON_RATE_OF_FIRE_BONUS;
    public static final float DEFAULT_MISSILE_WEAPON_RATE_OF_FIRE_BONUS = 0.1f;
    public static float MISSILE_WEAPON_RATE_OF_FIRE_BONUS = DEFAULT_MISSILE_WEAPON_RATE_OF_FIRE_BONUS;

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
        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float malfunctionChance = calculateWeaponMalfunctionMalus(stats.getVariant(), effect);
        float weaponTurnRateBonusMult = calculateTurnRateMultiplier(stats.getVariant(), effect);
        float ballisticWeaponRateOfFireMult = calculateRoFMultiplier(stats.getVariant(), effect, WeaponType.BALLISTIC);
        float energyWeaponRateOfFireMult = calculateRoFMultiplier(stats.getVariant(), effect, WeaponType.ENERGY);
        float missileWeaponRateOfFireMult = calculateRoFMultiplier(stats.getVariant(), effect, WeaponType.MISSILE);

        stats.getWeaponMalfunctionChance().modifyFlat(id, malfunctionChance);
        stats.getWeaponTurnRateBonus().modifyMult(id, weaponTurnRateBonusMult);
        stats.getBallisticRoFMult().modifyMult(id, ballisticWeaponRateOfFireMult);
        stats.getEnergyRoFMult().modifyMult(id, energyWeaponRateOfFireMult);
        stats.getMissileRoFMult().modifyMult(id, missileWeaponRateOfFireMult);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        ShipVariantAPI variant = null;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
            variant = ship.getVariant();
        }
        float malfunctionChance = calculateWeaponMalfunctionMalus(variant, effect);
        float weaponTurnRateBonus = calculateTurnRateBonus(variant, effect);
        float ballisticWeaponRateOfFireBonus = calculateRoFBonus(variant, effect, WeaponType.BALLISTIC);
        float energyWeaponRateOfFireBonus = calculateRoFBonus(variant, effect, WeaponType.ENERGY);
        float missileWeaponRateOfFireBonus = calculateRoFBonus(variant, effect, WeaponType.MISSILE);

        if (index == 0) {
            return Math.round(weaponTurnRateBonus * 100f) + "%";
        }
        if (index == 1) {
            return Math.round(ballisticWeaponRateOfFireBonus * 100f) + "%";
        }
        if (index == 2) {
            return Math.round(energyWeaponRateOfFireBonus * 100f) + "%";
        }
        if (index == 3) {
            return Math.round(missileWeaponRateOfFireBonus * 100f) + "%";
        }
        if (index >= 4) {
            return Math.round(malfunctionChance * 100f) + "%";
        }
        return null;
    }

    private float calculateWeaponMalfunctionMalus(ShipVariantAPI variant, float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_MALFUNCTION_PENALTY - WEAPON_MALFUNCTION_PENALTY * penaltyFactor;
        /**
         * will produce 0.05 for 100% dmod effect mult
         * 0.05 - 0.05 * (0) = 0.05
         * will produce 0.025 for 50% dmod effect mult
         * 0.05 - 0.05*0.5 = 0.025
         * will produce 0.1 for 200%
         * 0.05 - 0.05*(-1) = 0.1
         * will produce 0.15 for 300%
         * 0.05 - 0.05*-2 = 0.15
         *
         * and then half of that if we have "rugged"
         */

        return retVal;
    }

    private float calculateTurnRateMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, turn rate multiplier is going to be 1 + TurnRateBonus
        float retVal;
        float bonus = calculateTurnRateBonus(variant, baseEffect);
        retVal = 1f + bonus;

        return retVal;
    }

    private float calculateTurnRateBonus(ShipVariantAPI variant, float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5
        // We should leave the doubling of bonuses if we have rugged
        // so they remain consistent across having and not having rugged, but should get rid of halving for penalties
        // because that just ends up reducing them to quarter instead of just being half
        //
        // In this particular case, we should quadruple it so that there's a "double" effect of "rugged" ships vs non-rugged
        // effectivelly ending up with double the bonuses and half the penalties
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_TURN_RATE_BONUS - WEAPON_TURN_RATE_BONUS * penaltyFactor;
        /**
         * will produce 0.1 for 100% dmod effect mult
         * 0.1 - 0.1 * (0) = 0.1
         * will produce 0.05 for 50% dmod effect mult
         * 0.1 - 0.1*0.5 = 0.05
         * will produce 0.2 for 200%
         * 0.1 - 0.1*(-1) = 0.2
         * will produce 0.3 for 300%
         * 0.11 - 0.1*-2 = 0.3
         *
         * and then DOUBLE of that if we have "rugged" (well, quadruple)
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                // Multiplying by 2 makes it be the same as for other non-rugged ships
                // so let's multiply by 4 to make bonuses twice as good, while the penalties get halved
                retVal = retVal * 4;
            }
        }

        return retVal;
    }

    private float calculateRoFMultiplier(ShipVariantAPI variant, float baseEffect, WeaponType type) {
        // Basically, RoF multiplier is going to be 1 + RoFBonus
        float retVal;
        float bonus = calculateRoFBonus(variant, baseEffect, type);
        retVal = 1f + bonus;

        return retVal;
    }

    private float calculateRoFBonus(ShipVariantAPI variant, float baseEffect, WeaponType type) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5
        // We should leave the doubling of bonuses if we have rugged
        // so they remain consistent across having and not having rugged, but should get rid of halving for penalties
        // because that just ends up reducing them to quarter instead of just being half
        //
        // In this particular case, we should quadruple it so that there's a "double" effect of "rugged" ships vs non-rugged
        // effectivelly ending up with double the bonuses and half the penalties
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal;
        switch(type) {
            case BALLISTIC:
                retVal = BALLISTIC_WEAPON_RATE_OF_FIRE_BONUS - BALLISTIC_WEAPON_RATE_OF_FIRE_BONUS * penaltyFactor;
                break;
            case ENERGY:
                retVal = ENERGY_WEAPON_RATE_OF_FIRE_BONUS - ENERGY_WEAPON_RATE_OF_FIRE_BONUS * penaltyFactor;
                break;
            case MISSILE:
                retVal = MISSILE_WEAPON_RATE_OF_FIRE_BONUS - MISSILE_WEAPON_RATE_OF_FIRE_BONUS * penaltyFactor;
                break;
            default: throw new IllegalArgumentException("add support for "+type+" weapon type in VayraLessIllAdvised !!!");
        }
        /**
         * will produce 0.1 for 100% dmod effect mult
         * 0.1 - 0.1 * (0) = 0.1
         * will produce 0.05 for 50% dmod effect mult
         * 0.1 - 0.1*0.5 = 0.05
         * will produce 0.2 for 200%
         * 0.1 - 0.1*(-1) = 0.2
         * will produce 0.3 for 300%
         * 0.11 - 0.1*-2 = 0.3
         *
         * and then DOUBLE of that if we have "rugged" (well, quadruple)
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                // Multiplying by 2 makes it be the same as for other non-rugged ships
                // so let's multiply by 4 to make bonuses twice as good, while the penalties get halved
                retVal = retVal * 4;
            }
        }

        return retVal;
    }

    private enum WeaponType { BALLISTIC, ENERGY, MISSILE }
}
