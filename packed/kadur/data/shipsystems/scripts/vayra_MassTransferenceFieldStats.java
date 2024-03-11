package data.shipsystems.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import static com.fs.starfarer.api.plugins.ShipSystemStatsScript.State.ACTIVE;
import static data.scripts.KadurModPlugin.Rotate;
import java.io.IOException;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

import static org.lwjgl.opengl.GL11.*;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class vayra_MassTransferenceFieldStats extends BaseShipSystemScript {

    /////////////////////CONFIG/////////////////////
    // effect radius, both visual and mechanical
    public static final float EFFECT_RADIUS = 2000f;
    
    public static final String KADUR_IFF_HULLMOD = "vayra_kadur_iff";

    // sprite path - necessary if loaded here and not in settings.json
    public static final String SPRITE_PATH = "graphics/fx/shields256.png";
    // a lighter version of this will be used for particles and arcs
    public static final Color COLOR = new Color(33, 106, 109, 185);
    // rotation in degrees/second
    public static final float ROTATION_SPEED = 20f;
    // sound id, must be in sounds.json
    public static final String DEBUFF_SOUND_ID = "system_entropy_loop";

    // debuffs to fighters under effect
    public static final float DAMAGE_INCREASE_PERCENT = 25;
    public static final float MANEUVERABILITY_DECREASE_PERCENT = 50;
    public static final float SPEED_DECREASE_PERCENT = 25;
    // EMP arc to affected fighters every X seconds
    // does energy and EMP damage, both equal to MIN_DAMAGE + (MAX_DAMAGE * random())
    public static final float ARC_TIMER = 5f;
    public static final float ARC_MIN_DAMAGE = 25f;
    public static final float ARC_MAX_DAMAGE = 75f;

    /////////////////////INTERNAL/////////////////////
    public static final Object KEY_JITTER = new Object();
    private float effectTimer = 0f;
    private boolean loaded = false;
    private float rotation = 0f;
    private SpriteAPI sprite = null;
    private final Map<ShipAPI, Float> fighters = new HashMap<>();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {

        ShipAPI ship;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        if (sprite == null) {
            // Load sprite if it hasn't been loaded yet - not needed if you add it to settings.json
            if (!loaded) {
                try {
                    Global.getSettings().loadTexture(SPRITE_PATH);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to load sprite '" + SPRITE_PATH + "'!", ex);
                }

                loaded = true;
            }
            sprite = Global.getSettings().getSprite(SPRITE_PATH);
        }

        if (effectLevel <= 0.001f) {
            effectTimer = 0f;
            fighters.clear();
            return;
        }

        final Vector2f loc = ship.getLocation();
        final ViewportAPI view = Global.getCombatEngine().getViewport();
        if (view.isNearViewport(loc, EFFECT_RADIUS)) {
            glPushAttrib(GL_ENABLE_BIT);
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glViewport(0, 0, Display.getWidth(), Display.getHeight());
            glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_BLEND);
            final float radius = (EFFECT_RADIUS * 2f * effectLevel) / view.getViewMult();
            sprite.setSize(radius, radius);
            sprite.setColor(COLOR);
            sprite.setAlphaMult(effectLevel * 0.5f);
            sprite.renderAtCenter(view.convertWorldXtoScreenX(loc.x), view.convertWorldYtoScreenY(loc.y));
            sprite.setAngle(rotation);
            glPopMatrix();
            glPopAttrib();
        }

        // Spin it
        float amount = engine.getElapsedInLastFrame();
        rotation = Rotate(rotation, (ROTATION_SPEED * amount));

        // Increment the timer
        effectTimer += amount;

        // Create particles inside the circle
        float size, speed, brightness, duration;
        Vector2f location, velocity;
        // Create more particles as time goes on
        int particle_count_this_frame = (int) (1f + (effectTimer));
        for (int x = 0; x < particle_count_this_frame; x++) {
            size = (float) (3f + (Math.random() * 12f));
            speed = (float) (100f + (Math.random() * 200f));
            brightness = (float) (1f + (Math.random() * 1f));
            duration = (float) (0.4f + (Math.random() * 0.6f));
            location = MathUtils.getRandomPointInCircle(ship.getLocation(), EFFECT_RADIUS);
            velocity = MathUtils.getPointOnCircumference(location, speed, VectorUtils.getAngle(location, ship.getLocation()));
            engine.addSmoothParticle(location, velocity, size, brightness, duration, COLOR.brighter());
        }

        // Setup fighter visuals
        float jitterLevel = effectLevel;
        float maxRangeBonus = 10f;
        float jitterRangeBonus = jitterLevel * maxRangeBonus;

        // Apply fighter effects
        for (ShipAPI fighter : getOtherFightersWithinRange(ship, effectLevel)) {

            if (fighter.isHulk() || fighter.getWing() == null || fighter.getWing().getSourceShip() == null) {
                continue;
            }

            // Ignore fighters launched by non-Seraph Kadur carriers
            if (fighter.getWing().getSourceShip().getVariant().hasHullMod(KADUR_IFF_HULLMOD)) {
                continue;
            }

            // increment timer, spawn EMP arcs if beyond timer
            if (engine.isPaused()) return;
            float time;
            if (!fighters.containsKey(fighter)) {
                time = 1f;
            } else {
                time = fighters.get(fighter);
            }
            time += amount;
            if (time >= ARC_TIMER) {
                spawnArc(ship, fighter, engine);
                time = (float) Math.random();
            }
            fighters.put(fighter, time);

            MutableShipStatsAPI fStats = fighter.getMutableStats();

            // apply stat mods to affected fighters
            fStats.getArmorDamageTakenMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
            fStats.getHullDamageTakenMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
            fStats.getAcceleration().modifyMult(id, 1f - 0.01f * MANEUVERABILITY_DECREASE_PERCENT * effectLevel);
            fStats.getDeceleration().modifyMult(id, 1f - 0.01f * MANEUVERABILITY_DECREASE_PERCENT * effectLevel);
            fStats.getTurnAcceleration().modifyMult(id, 1f - 0.01f * MANEUVERABILITY_DECREASE_PERCENT * effectLevel);
            fStats.getMaxTurnRate().modifyMult(id, 1f - 0.01f * MANEUVERABILITY_DECREASE_PERCENT * effectLevel);
            fStats.getMaxSpeed().modifyMult(id, 1f - 0.01f * SPEED_DECREASE_PERCENT * effectLevel);

            // display jitter effects and play sound over affected fighters
            if (jitterLevel > 0) {
                fighter.setJitterUnder(KEY_JITTER, COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
                fighter.setJitter(KEY_JITTER, COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
                Global.getSoundPlayer().playLoop(DEBUFF_SOUND_ID, ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());

            }
        }
    }

    // actually spawn the EMP arcs
    private void spawnArc(ShipAPI ship, ShipAPI target, CombatEngineAPI engine) {
        float bonusMult = (float) Math.random();
        float bonusDamage = (ARC_MAX_DAMAGE - ARC_MIN_DAMAGE) * bonusMult;
        float damage = ARC_MIN_DAMAGE + bonusDamage;
        float maxRange = 10000f;
        String impactSoundId = "tachyon_lance_emp_impact";
        float thickness = 10f + bonusMult * 20f;
        engine.spawnEmpArcPierceShields(
                ship,
                ship.getLocation(),
                ship,
                target,
                DamageType.ENERGY,
                damage,
                damage,
                maxRange,
                impactSoundId,
                thickness,
                COLOR.brighter().brighter(),
                COLOR.brighter());
    }

    // gets all fighters not launched by this carrier
    private List<ShipAPI> getOtherFightersWithinRange(ShipAPI carrier, float effectLevel) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI fighter : CombatUtils.getShipsWithinRange(carrier.getLocation(), EFFECT_RADIUS * effectLevel)) {
            if (!fighter.isFighter()) {
                continue;
            }

            if (fighter.getWing() == null) {
                continue;
            }

            if (fighter.getWing().getSourceShip() == null) {
                continue;
            }

            if (!fighter.getWing().getSourceShip().equals(carrier)) {
                result.add(fighter);
            }
        }

        return result;
    }

    // apparently this doesn't get called b/c i set the script to run while idle
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
            if (fighter.isHulk()) {
                continue;
            }
            if (!fighter.isFighter()) {
                continue;
            }
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getArmorDamageTakenMult().unmodify(id);
            fStats.getHullDamageTakenMult().unmodify(id);
            fStats.getAcceleration().unmodify(id);
            fStats.getDeceleration().unmodify(id);
            fStats.getTurnAcceleration().unmodify(id);
            fStats.getMaxTurnRate().unmodify(id);
            fStats.getMaxSpeed().unmodify(id);
        }
    }

    // status data for player
    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (state == ACTIVE && index == 0) {
            return new ShipSystemStatsScript.StatusData("fighters +" + (int) (DAMAGE_INCREASE_PERCENT * effectLevel) + "% damage taken", false);
        }
        if (state == ACTIVE && index == 1) {
            return new ShipSystemStatsScript.StatusData("fighters -"
                    + (int) (SPEED_DECREASE_PERCENT * effectLevel)
                    + "/" + (int) (MANEUVERABILITY_DECREASE_PERCENT * effectLevel) + "% speed/maneuverability", false);
        }
        if (state == ACTIVE && index == 2) {
            return new ShipSystemStatsScript.StatusData("welcome to my DEATH MACHINE, interlopers", false);
        }
        return null;
    }

}
