package data.shipsystems.scripts;

import java.awt.Color;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class vayra_FluxOverdriveStats extends BaseShipSystemScript {

    public static final float SPEED_BONUS = 60f;          // flat bonus to top speed (x2 for accel/decel)
    public static final float TURN_BONUS = 60f;           // flat bonus to max turn rate (x2 for turn accel)
    public static final float FLUX_BONUS = 0.6f;          // bonus flux dissipation multiplier i.e. 1f = +100% flux dissipation
    
    private final Color color = new Color(33,106,109,255);
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

        ship.getEngineController().fadeToOtherColor(this, color, new Color(255,213,133,255), effectLevel, 0.67f);
        ship.getEngineController().extendFlame(this, 1.5f * effectLevel, 1.5f * effectLevel, 2f * effectLevel);
        
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
            stats.getAcceleration().modifyFlat(id, SPEED_BONUS * 2f * effectLevel);
            stats.getDeceleration().modifyFlat(id, SPEED_BONUS * 2f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * 2f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, TURN_BONUS * effectLevel);
            stats.getFluxDissipation().modifyMult(id, 1f + (FLUX_BONUS * effectLevel));
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
        
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
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

        if (index == 2) {
         	return new StatusData("+" + (int) SPEED_BONUS + " top speed", false);
        } 
        if (index == 1) {
            return new StatusData("+" + (int) ((100f * FLUX_BONUS) * effectLevel) + "% flux dissipation", false);
        }

        if (state != null) {
            switch (state) {
                case IN:
                    if (index == 0) {
                        return new StatusData("let's get this party started", false);
                    }
                    break;
                case ACTIVE:
                    if (index == 0) {
                        return new StatusData("who's a man and a half? i'm a man and a half", false);
                    }
                    break;
                case OUT:
                    if (index == 0) {
                        return new StatusData("we can't stop here, this is bat country", false);
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
