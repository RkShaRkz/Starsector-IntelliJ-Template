package data.shipsystems.scripts;

import java.awt.Color;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.util.HashMap;
import java.util.Map;

public class vayra_AugOverdriveStats extends BaseShipSystemScript {

    public static final float SPEED_BONUS = 60f;          // flat bonus to top speed (x2 for accel/decel)
    public static final float TURN_BONUS = 60f;           // flat bonus to max turn rate (x2 for turn accel)
    public static final float FLUX_BONUS = 0.6f;          // bonus flux dissipation multiplier i.e. 1f = +100% flux dissipation
    public static final float ROF_BONUS = 0.5f;           // bonus ballistic rate of fire multiplier i.e. 1f = +100% ballistic rate of fire
    public static final float MASS_MULT = 2f;             // multiplier to ship mass

    private Map<String, Float> mass = new HashMap<>();

    private final Color color = new Color(33, 106, 109, 255);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        String hullId = ship.getHullStyleId();

        if (!mass.containsKey(hullId)) {
            mass.put(hullId, ship.getMass());
        }

        ship.getEngineController().fadeToOtherColor(this, color, new Color(255, 213, 133, 255), effectLevel, 0.67f);
        ship.getEngineController().extendFlame(this, 1.25f * effectLevel, 1.25f * effectLevel, 1.5f * effectLevel);

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
            stats.getAcceleration().modifyFlat(id, SPEED_BONUS * 2f * effectLevel);
            stats.getDeceleration().modifyFlat(id, SPEED_BONUS * 2f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * 2f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, TURN_BONUS * effectLevel);
            stats.getFluxDissipation().modifyMult(id, 1f + (FLUX_BONUS * effectLevel));
            stats.getBallisticRoFMult().modifyMult(id, 1f + (ROF_BONUS * effectLevel));
            stats.getHullDamageTakenMult().modifyMult(id, 0.75f);
            stats.getArmorDamageTakenMult().modifyMult(id, 0.75f);
            stats.getEmpDamageTakenMult().modifyMult(id, 0.5f);
            if (ship.getMass() == mass.get(hullId)) {
                ship.setMass(ship.getMass() * MASS_MULT);
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        String hullId = ship.getHullStyleId();

        if (!mass.containsKey(hullId)) {
            mass.put(hullId, ship.getMass());
        }

        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
        if (ship.getMass() != mass.get(hullId)) {
            ship.setMass(ship.getMass());
        }
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

        if (index == 3) {
            return new StatusData("+" + (int) SPEED_BONUS + " top speed", false);
        }
        if (index == 2) {
            return new StatusData("+" + (int) ((100f * ROF_BONUS) * effectLevel) + "% ballistic rate of fire", false);
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
