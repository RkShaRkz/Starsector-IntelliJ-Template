package data.shipsystems.scripts;

// import java.awt.Color;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class vayra_StrikeFeedStats extends BaseShipSystemScript {

    // public static float SPEED_BONUS = 100f;
    // public static float TURN_BONUS = 100f;
    public static float BALLISTIC_BONUS = 1f;
    public static float ENERGY_BONUS_PERCENT = 100f;
    public static float FLUX_REDUCTION = 50f;

    //  private Color color = new Color(255,255,255,255);
    /**
     *
     * @param stats
     * @param id
     * @param state
     * @param effectLevel
     */
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        // ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0), effectLevel, 0.67f);
        // ship.getEngineController().extendFlame(this, 1.5f * effectLevel, 0f * effectLevel, 0f * effectLevel);
        if (state == ShipSystemStatsScript.State.OUT) {
            //    stats.getMaxSpeed().unmodify(id);
            //    stats.getMaxTurnRate().unmodify(id);
        } else {
            //	stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
            //	stats.getAcceleration().modifyPercent(id, SPEED_BONUS * 3f * effectLevel);
            //	stats.getDeceleration().modifyPercent(id, SPEED_BONUS * 3f * effectLevel);
            //	stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * effectLevel);
            //	stats.getTurnAcceleration().modifyPercent(id, TURN_BONUS * 5f * effectLevel);
            //	stats.getMaxTurnRate().modifyFlat(id, 15f);
            //	stats.getMaxTurnRate().modifyPercent(id, 100f);

            float bonusPercent = ENERGY_BONUS_PERCENT * effectLevel;
            float mult = 1f + BALLISTIC_BONUS * effectLevel;

            stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);
            stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);

            stats.getBallisticRoFMult().modifyMult(id, mult);
            stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
        }
    }

    /**
     *
     * @param stats
     * @param id
     */
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        //	stats.getMaxSpeed().unmodify(id);
        //	stats.getMaxTurnRate().unmodify(id);
        //	stats.getTurnAcceleration().unmodify(id);
        //	stats.getAcceleration().unmodify(id);
        //	stats.getDeceleration().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
    }

    /**
     *
     * @param index
     * @param state
     * @param effectLevel
     * @return
     */
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float mult = 1f + BALLISTIC_BONUS * effectLevel;
        float energyBonusPercent = ENERGY_BONUS_PERCENT * effectLevel;
        float ballisticBonusPercent = (int) ((mult - 1f) * 100f);

        // if (index == 4) {
        // 	return new StatusData("engines at maximum power", false);
        // } 
        if (index == 3) {
            return new StatusData("+" + (int) ballisticBonusPercent + "% ballistic rate of fire", false);
        }
        if (index == 2) {
            return new StatusData("+" + (int) energyBonusPercent + "% energy weapon damage", false);
        }
        if (index == 1) {
            return new StatusData("-" + (int) FLUX_REDUCTION + "% weapon flux", false);
        }

        if (state != null) {
            switch (state) {
                case IN:
                    if (index == 0) {
                        return new StatusData("time for my pound of flesh", false);
                    }
                    break;
                case ACTIVE:
                    if (index == 0) {
                        return new StatusData("dig the prowess, the capacity for violence", false);
                    }
                    break;
                case OUT:
                    if (index == 0) {
                        return new StatusData("i am the one who knocks", false);
                    }
                    break;
                case COOLDOWN:
                    break;
                case IDLE:
                    break;
                default:
                    break;
            }
        }
        return null;
    }
}
