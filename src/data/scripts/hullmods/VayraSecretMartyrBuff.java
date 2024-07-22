package data.scripts.hullmods;
// now partially nickescript (bastardized by me, any mistakes are my own, etc)

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.EnumSet;
import org.lazywizard.lazylib.MathUtils;

// i guess this also handles shield flickering now lol
public class VayraSecretMartyrBuff extends BaseHullMod {

    // shield default and flicker colors
    public static final Color SHIELD_INNER_COLOR = new Color(60, 190, 195, 75);
    public static final Color SHIELD_RING_COLOR = new Color(255, 255, 255, 150);
    public static final Color FLICKER_INNER_COLOR = new Color(60, 190, 195, 20);
    public static final Color FLICKER_RING_COLOR = new Color(255, 255, 255, 100);

    // shield flicker timing
    private static final float FLICKER_FLUX_MULT = 3f; // timer advance speed scales from 1x (min flux) to this (max flux)
    private static final float FLICKER_MAX_DELAY = 4.20f;
    private static final float FLICKER_MIN_DELAY = 0.6666f;
    private static final float FLICKER_MAX_DURATION = 0.1312f;
    private static final float FLICKER_MIN_DURATION = 0.0333f;

    // The ID we insert our flicker handlers with; only important part is that it's unique, nothing else really matters
    private static final String FLICKER_HANDLER_ID = "vayra_kadurShieldFlickerTimer";

    // "you shoulda seen the other guy"
    private static final String BUFF_ID = "vayra_forever_war_buff";
    private final String FOREVER_WAR_ICON = Global.getSettings().getSpriteName("vayra_forever_war_icon", "1");
    private final String FOREVER_WAR_TEXT = "For hate's sake, I spit my last breath at thee";
    private static final float BUFF_THRESHOLD = 0.33f;
    private static final float BUFF_MAX_BONUS_MULT = 0.5f;
    private static final float BUFF_BONUS_SPEED = 10f;
    private static final Color BUFF_ENGINE_COLOR = new Color(33, 106, 109, 255);
    private static final Color BUFF_GLOW_COLOR = new Color(255, 106, 0, 200);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null) {
            return;
        }

        String buffId = BUFF_ID + ship.getId();
        MutableShipStatsAPI stats = ship.getMutableStats();
        
        // shield flicker stuff
        ShieldAPI shield = ship.getShield();
        if (shield == null
                || shield.isOff()
                || shield.getType() == ShieldAPI.ShieldType.NONE
                || shield.getType() == ShieldAPI.ShieldType.PHASE
                || ship.getVariant().hasHullMod(HullMods.HARDENED_SHIELDS)) {
            // no flicker if we don't have shield or aren't using shield (duh)
            // or have un-shittified our shields with Hardened Shields
        } else {
            // tick our flicker handler
            if (Global.getCombatEngine().getCustomData().get(ship.getId() + FLICKER_HANDLER_ID) instanceof FlickerHandler) {
                ((FlickerHandler) Global.getCombatEngine().getCustomData().get(ship.getId() + FLICKER_HANDLER_ID)).tick(amount, ship);
            } // if we don't have a flicker handler yet, add one and tick it
            else {
                FlickerHandler handler = new FlickerHandler();
                Global.getCombatEngine().getCustomData().put(ship.getId() + FLICKER_HANDLER_ID, handler);
                handler.tick(amount, ship);
            }
        }

        // "you shoulda seen the other guy" stuff
        if (!ship.isHulk() && ship.getHullLevel() <= BUFF_THRESHOLD) {
            float effectLevel = MathUtils.clamp(1f - (ship.getHullLevel()*3), 0f, 1f);
            float buffMult = 1f + (BUFF_MAX_BONUS_MULT * effectLevel);

            // buffs		
            stats.getMaxSpeed().modifyFlat(buffId, BUFF_BONUS_SPEED * effectLevel);
            stats.getAcceleration().modifyFlat(buffId, BUFF_BONUS_SPEED * 2f * effectLevel);
            stats.getDeceleration().modifyFlat(buffId, BUFF_BONUS_SPEED * 2f * effectLevel);
            stats.getAcceleration().modifyMult(buffId, buffMult);
            stats.getDeceleration().modifyMult(buffId, buffMult);
            stats.getFluxDissipation().modifyMult(buffId, buffMult);
            stats.getBallisticRoFMult().modifyMult(buffId, buffMult);
            ship.getEngineController().fadeToOtherColor(this, BUFF_ENGINE_COLOR, null, effectLevel, effectLevel);
            ship.getEngineController().extendFlame(this, effectLevel, effectLevel, effectLevel);
            ship.setWeaponGlow(effectLevel, BUFF_GLOW_COLOR, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC));
            ship.setJitter(this, BUFF_GLOW_COLOR, effectLevel*0.2f, 3, 5f);
            ship.setJitterUnder(this, BUFF_GLOW_COLOR, effectLevel*0.2f, 7, 10f);

            if (ship == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(buffId, FOREVER_WAR_ICON, "Martyr's Blessing", FOREVER_WAR_TEXT, false);
            }

        } else {
            // unmodify
            stats.getMaxSpeed().unmodify(buffId);
            stats.getAcceleration().unmodify(buffId);
            stats.getDeceleration().unmodify(buffId);
            stats.getFluxDissipation().unmodify(buffId);
            stats.getBallisticRoFMult().unmodify(buffId);
            //stats.getEngineMalfunctionChance().unmodify(buffId);
            //stats.getWeaponMalfunctionChance().unmodify(buffId);
            //stats.getCriticalMalfunctionChance().unmodify(buffId);
            ship.getEngineController().fadeToOtherColor(this, BUFF_ENGINE_COLOR, null, 0f, 0f);
            ship.getEngineController().extendFlame(this, 0f, 0f, 0f);
            ship.setWeaponGlow(0f, BUFF_GLOW_COLOR, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC));
            ship.setJitter(this, BUFF_GLOW_COLOR, 0f, 0, 0f);
            ship.setJitterUnder(this, BUFF_GLOW_COLOR, 0f, 0, 0f);
        }
    }

    //Class for flicker handlers: handles everything flicker-related. All config is above; don't touch this one
    private class FlickerHandler {

        private final IntervalUtil triggerInterval = new IntervalUtil(FLICKER_MIN_DELAY, FLICKER_MAX_DELAY);
        private float currentFlickerDuration = 0f;

        void tick(float amount, ShipAPI ship) {
            
            ShieldAPI shield = ship.getShield();
            
            // trigger new flickers, and advance existing ones
            currentFlickerDuration -= amount;
            
            amount *= 1f + ((FLICKER_FLUX_MULT - 1f) * ship.getFluxLevel());
            triggerInterval.advance(amount);
            
            if (triggerInterval.intervalElapsed()) {
                currentFlickerDuration = MathUtils.getRandomNumberInRange(FLICKER_MIN_DURATION, FLICKER_MAX_DURATION);
            }

            // changes shield color depending on flicker status
            float flickerProgress = (float) Math.pow(Math.min(1f, Math.max(0f, currentFlickerDuration / FLICKER_MAX_DURATION)), 1.5f);
            shield.setInnerColor(Misc.interpolateColor(SHIELD_INNER_COLOR, FLICKER_INNER_COLOR, flickerProgress));
            shield.setRingColor(Misc.interpolateColor(SHIELD_RING_COLOR, FLICKER_RING_COLOR, flickerProgress));
        }
    }
}
