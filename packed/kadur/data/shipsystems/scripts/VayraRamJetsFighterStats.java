package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;

public class VayraRamJetsFighterStats extends BaseShipSystemScript {

    private final Color color = new Color(33, 106, 109, 255);

    public static final Float RAM_JET_SPEED = 150f;

    public static final float DMG_TAKEN = 0.5f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        ship.getEngineController().fadeToOtherColor(this, color, new Color(255, 213, 133, 255), effectLevel, 0.67f);
        ship.getEngineController().extendFlame(this, 1.5f * effectLevel, 1.5f * effectLevel, 2f * effectLevel);

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 200f);
        } else {
            stats.getMaxSpeed().modifyFlat(id, RAM_JET_SPEED * effectLevel);
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 150f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 100f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 50f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 300f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 25f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getHullDamageTakenMult().modifyMult(id, DMG_TAKEN);
            stats.getArmorDamageTakenMult().modifyMult(id, DMG_TAKEN);
            stats.getEmpDamageTakenMult().modifyMult(id, 0f);
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
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
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
        if (index == 1) {
            return new StatusData("if i am banned from jangala for hollering at the luddites", false);
        }
        if (index == 0) {
            return new StatusData("i will face god and walk backwards into hell", false);
        }
        return null;
    }
}
