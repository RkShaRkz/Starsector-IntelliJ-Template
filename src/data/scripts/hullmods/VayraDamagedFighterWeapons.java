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

public class VayraDamagedFighterWeapons extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedFighterWeapons";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float FIGHTER_DAMAGE_PENALTY = 0.2f;
    public static final float FIGHTER_ACCURACY_PENALTY = 0.3f;
    public static final float FIGHTER_RECOIL_PENALTY = 0.6f;

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
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        // Do nothing if it's been disabled
        int owner = ship.getOwner();
        if (DISABLE_FOR_ENEMY && MiscUtils.OWNER_ENEMY == owner) return;
        if (DISABLE_FOR_PLAYER && MiscUtils.OWNER_PLAYER == owner) return;

        // Carry on as usual
        MutableShipStatsAPI shipStats = ship.getMutableStats();
        // Doesn't make sense to proceed if we don't have these stats since we can't extract necessary multipliers
        if (shipStats == null) return;
        float effect = shipStats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float damageMult = calculateFighterDamageMultiplier(ship.getVariant(), effect);
        float accuracyMult = calculateFighterAccuracyMultiplier(ship.getVariant(), effect);
        float recoilMult = calculateFighterRecoilMultiplier(ship.getVariant(), effect);

        MutableShipStatsAPI fighterStats = fighter.getMutableStats();
        if (fighterStats != null) {
            fighterStats.getBallisticWeaponDamageMult().modifyMult(id, damageMult);
            fighterStats.getEnergyWeaponDamageMult().modifyMult(id, damageMult);
            fighterStats.getMissileWeaponDamageMult().modifyMult(id, damageMult);
            fighterStats.getAutofireAimAccuracy().modifyMult(id, accuracyMult);
            fighterStats.getRecoilPerShotMult().modifyMult(id, recoilMult);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        ShipVariantAPI variant = null;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
            variant = ship.getVariant();
        }
        float damageMalus = calculateFighterDamageMalus(variant, effect);
        float accuracyMalus = calculateFighterAccuracyMalus(variant, effect);
        float recoilMalus = calculateFighterRecoilMalus(variant, effect);

        if (index == 0) {
            return Math.round(damageMalus * 100f) + "%";
        }
        if (index == 1) {
            return Math.round(accuracyMalus * 100f) + "%";
        }
        if (index == 2) {
            return Math.round(recoilMalus * 100f) + "%";
        }
        if (index >= 3) {
            return CompromisedStructure.getCostDescParam(index, 3);
        }
        return null;
    }

    private float calculateFighterDamageMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, damage multiplier is going to be 1 - DamageMalus
        float retVal;
        float malus = calculateFighterDamageMalus(variant, baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateFighterDamageMalus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = FIGHTER_DAMAGE_PENALTY - FIGHTER_DAMAGE_PENALTY * penaltyFactor;
        /**
         * will produce 0.2 for 100% dmod effect mult
         * 0.2 - 0.2 * (0) = 0.2
         * will produce 0.1 for 50% dmod effect mult
         * 0.2 - 0.2*0.5 = 0.25
         * will produce 0.4 for 200%
         * 0.2 - 0.2*(-1) = 0.4
         * will produce 0.6 for 300%
         * 0.2 - 0.2*-2 = 0.6
         *
         * and then half of that if we have "rugged"
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        return retVal;
    }

    private float calculateFighterAccuracyMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, damage multiplier is going to be 1 - AccuracyMalus
        float retVal;
        float malus = calculateFighterAccuracyMalus(variant, baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateFighterAccuracyMalus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = FIGHTER_ACCURACY_PENALTY - FIGHTER_ACCURACY_PENALTY * penaltyFactor;
        /**
         * will produce 0.3 for 100% dmod effect mult
         * 0.3 - 0.3 * (0) = 0.3
         * will produce 0.15 for 50% dmod effect mult
         * 0.3 - 0.3*0.5 = 0.15
         * will produce 0.6 for 200%
         * 0.3 - 0.3*(-1) = 0.6
         * will produce 0.9 for 300%
         * 0.3 - 0.3*-2 = 0.9
         *
         * and then half of that if we have "rugged"
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        return retVal;
    }

    private float calculateFighterRecoilMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, recoil multiplier is going to be 1 + RecoilMalus
        float retVal;
        float malus = calculateFighterRecoilMalus(variant, baseEffect);
        retVal = 1f + malus;

        return retVal;
    }

    private float calculateFighterRecoilMalus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = FIGHTER_RECOIL_PENALTY - FIGHTER_RECOIL_PENALTY * penaltyFactor;
        /**
         * will produce 0.6 for 100% dmod effect mult
         * 0.6 - 0.6 * (0) = 0.6
         * will produce 0.3 for 50% dmod effect mult
         * 0.6 - 0.6*0.5 = 0.3
         * will produce 1.2 for 200%
         * 0.6 - 0.6*(-1) = 1.2
         * will produce 1.8 for 300%
         * 0.6 - 0.6*-2 = 1.8
         *
         * and then half of that if we have "rugged"
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        return retVal;
    }
}
