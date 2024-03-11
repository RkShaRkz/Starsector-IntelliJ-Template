package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.weapons.KadurBoomerangShieldGuidance;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class KadurProjHandlerPlugin extends BaseEveryFrameCombatPlugin {

    /////////////////////////////GENERAL/////////////////////////////
    public static final Map<String, ProjData> PROJ_IDS = new HashMap<>();

    // spec ID of projectile to replace, spec ID of WEAPON to replace it with the projectile of
    // if the new weapon spec ID is null, projectile won't be replaced -- can use this to trigger effects
    // also some special muzzle flash/fx stuff
    static {
        PROJ_IDS.put("vayra_slowlrm", new ProjData("vayra_slowlrm_copy"));
        PROJ_IDS.put("vayra_jinn", new ProjData("vayra_jinn_copy"));
        PROJ_IDS.put("vayra_scatterblast_shot", new ProjData(null));
        PROJ_IDS.put("vayra_capacitive_dummy", new ProjData("vayra_rod_launcher_copy"));
        PROJ_IDS.put("vayra_capacitive_rod", new ProjData(null));
        PROJ_IDS.put("vayra_jordan_bomb", new ProjData(null));
        PROJ_IDS.put("vayra_canister_shot", new ProjData(null));
        PROJ_IDS.put("vayra_shockweb_canister", new ProjData(null));
        PROJ_IDS.put("vayra_spike_torpedo", new ProjData(null));
        PROJ_IDS.put("vayra_kadur_chaff", new ProjData(null));
        PROJ_IDS.put("vayra_light_boomerangshield", new ProjData(null));
        PROJ_IDS.put("vayra_medium_boomerangshield", new ProjData(null));
        PROJ_IDS.put("vayra_light_boomerangshield_copy", new ProjData(null));
        PROJ_IDS.put("vayra_medium_boomerangshield_copy", new ProjData(null));
    }

    /////////////////////////////CONFIG/////////////////////////////
    private static final String EMP_SOUND = "tachyon_lance_emp_impact"; // standard EMP arc sound for everything

    // spike torpedo and chaff stuff
    private static final String CHAFF_WEAPON_ID = "vayra_kadur_chaff_launcher";
    private static final int CHAFF_COUNT = 12;
    private static final float HP_PER_CHAFF = 80f;
    private static final float CHAFF_RANDOM_SPEED = 150f; // +/- 50%

    // canister stuff
    private static final float CANISTER_LIFETIME = 0.4f; // in seconds
    private static final int CANISTER_SPIKES = 8;
    private static final float CANISTER_SPREAD = 20f; // maximum radius of spread (arc = this * 2)

    // rods and LCPKVs use the same arcs
    private static final float ROD_ARC_RANGE = 100f; // check for ships whose collision radius intersects or is within circle
    private static final float LPCKV_ARC_RANGE = 100f; // check for ships whose collision radius intersects or is within circle
    private static final float ROD_ARC_DAMAGE = 100f;
    private static final float ROD_ARC_EMP = 400f;

    // boomerang shield reload range
    private static final float SHIELD_RELOAD_RANGE = 33f; // shield must get at least this close to its fighter to disappear

    private Map<DamagingProjectileAPI, Float> canisters = new HashMap<>();
    private Map<DamagingProjectileAPI, IntervalUtil> scatterBlasts = new HashMap<>();
    private Map<DamagingProjectileAPI, List<CombatEntityAPI>> capacitiveRodTargets = new HashMap<>();
    private Map<DamagingProjectileAPI, Integer> spikeTorpedoes = new HashMap<>();
    private List<DamagingProjectileAPI> boomerangShields = new ArrayList<>();

    //////////////////////////////BITS//////////////////////////////
    public static final Color KADUR_TEAL = new Color(33, 103, 109, 150);
    public static final Color KADUR_TEAL_BRIGHT = new Color(60, 190, 195, 150);

    public static class ProjData {

        public String newWeaponId; // can be null, in which case projectile is not replaced
        public boolean muzzleFlash; // only needed if true (for flash)
        public int particles; // only needed for flash
        public Color pColor; // only needed for flash
        public float pMaxSize; // only needed for flash, min size = 20%
        public float pMaxSpeed; // only needed for flash, min speed = 50%
        public float pMaxAngle; // only needed for flash, full arc width = this * 2
        public float pDur; // only needed for flash, in seconds
        public float pBright; // only needed for flash, 0.0 to 1.0

        public ProjData(String newWeaponId) {
            add(newWeaponId, false, null, null, null, null, null, null, null);
        }

        public ProjData(String newWeaponId, boolean flash, Integer particles, Color pColor, Float pMaxSize, Float pMaxSpeed, Float pMaxAngle, Float pDur, Float pBright) {
            add(newWeaponId, flash, particles, pColor, pMaxSize, pMaxSpeed, pMaxAngle, pDur, pBright);
        }

        private void add(String newWeaponId, boolean flash, Integer particles, Color pColor, Float pMaxSize, Float pMaxSpeed, Float pMaxAngle, Float pDur, Float pAlpha) {
            this.newWeaponId = newWeaponId;
            this.muzzleFlash = flash;
            this.particles = particles != null ? particles : 15;
            this.pColor = pColor != null ? pColor : KADUR_TEAL;
            this.pMaxSize = pMaxSize != null ? pMaxSize : 15f;
            this.pMaxSpeed = pMaxSpeed != null ? pMaxSpeed : 250f;
            this.pMaxAngle = pMaxAngle != null ? pMaxAngle : 7f;
            this.pDur = pDur != null ? pDur : 1.5f;
            this.pBright = pAlpha != null ? pAlpha : 0.420f;
        }
    }

    private CombatEngineAPI engine;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        capacitiveRodTargets.clear();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.isPaused()) {
            return;
        }

        List<DamagingProjectileAPI> projToRemove = new ArrayList<>();
        for (DamagingProjectileAPI test : capacitiveRodTargets.keySet()) {
            if (!engine.isEntityInPlay(test)) {
                projToRemove.add(test);
            }
        }
        for (DamagingProjectileAPI test : canisters.keySet()) {
            if (!engine.isEntityInPlay(test)) {
                projToRemove.add(test);
            }
        }
        for (DamagingProjectileAPI test : spikeTorpedoes.keySet()) {
            if (!engine.isEntityInPlay(test)) {
                projToRemove.add(test);
            }
        }
        for (DamagingProjectileAPI test : boomerangShields) {
            if (!engine.isEntityInPlay(test)) {
                projToRemove.add(test);
            }
        }
        for (DamagingProjectileAPI test : projToRemove) {
            capacitiveRodTargets.remove(test);
            canisters.remove(test);
            spikeTorpedoes.remove(test);
            boomerangShields.remove(test);
        }

        // catch all projectiles and loop through them
        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        int size = projectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projectiles.get(i);
            String projId = proj.getProjectileSpecId();

            // handle all the special stuff for special projs we caught
            if (engine.isEntityInPlay(proj)
                    && PROJ_IDS.containsKey(projId)
                    && !proj.didDamage()
                    && !proj.isFading()) {

                // setup stuff
                ProjData data = PROJ_IDS.get(projId);
                ShipAPI source = proj.getSource();
                WeaponAPI weapon = proj.getWeapon();
                Vector2f loc = proj.getLocation();
                Vector2f vel = proj.getVelocity();
                float angle = proj.getFacing();
                Vector2f shipVel = proj.getSource().getVelocity();
                Vector2f zero = new Vector2f(0, 0);

                // general proj replacement handler
                if (data.newWeaponId != null) {
                    engine.removeEntity(proj);
                    engine.spawnProjectile(source, weapon, data.newWeaponId, loc, angle, shipVel);

                    // general muzzle flash handler (in here so no permanent-flash on non-replaced projs)
                    if (data.muzzleFlash) {
                        engine.spawnExplosion(loc, shipVel, data.pColor, 20f, 0.25f);
                        for (int p = 0; p < data.particles; p++) {
                            float partSize = (float) ((data.pMaxSize * 0.2f) + (Math.random() * data.pMaxSize * 0.8f));
                            float partSpeed = (float) (data.pMaxSpeed + (Math.random() * data.pMaxSpeed));
                            float partAngle = weapon.getCurrAngle() + (float) (-data.pMaxAngle + (Math.random() * data.pMaxAngle * 2f));
                            Vector2f partVel = translatePolar(shipVel, partSpeed, partAngle);
                            engine.addHitParticle(loc, partVel, partSize, data.pDur, data.pBright, data.pColor);
                        }
                    }
                }

                // special case-by-case switcher
                switch (projId) {

                    // handle boomerang shields
                    case "vayra_light_boomerangshield":
                    case "vayra_medium_boomerangshield":
                        if (!boomerangShields.contains(proj)) {
                            boomerangShields.add(proj);
                            engine.addPlugin(new KadurBoomerangShieldGuidance(proj, source.getShipTarget()));
                        }
                        break;

                    case "vayra_light_boomerangshield_copy":
                    case "vayra_medium_boomerangshield_copy":
                        if (Misc.getDistance(loc, source.getLocation()) < SHIELD_RELOAD_RANGE) {
                            engine.removeEntity(proj);
                            String shieldSound = "vayra_shieldreturn";
                            if (projId.equals("vayra_light_boomerangshield_copy")) {
                                shieldSound += "_light";
                            }
                            Global.getSoundPlayer().playSound(shieldSound, 1f, 1f, loc, Misc.ZERO);
                        }
                        break;

                    // handle spike torpedo chaff spawning
                    case "vayra_spike_torpedo":

                        if (proj.didDamage()) {
                            break;
                        }

                        int chaffLeft = CHAFF_COUNT;
                        if (spikeTorpedoes.containsKey(proj)) {
                            chaffLeft = spikeTorpedoes.get(proj);
                        }

                        float spikeTorpedoHP = proj.getHitpoints();
                        if (chaffLeft > 0 && spikeTorpedoHP < (chaffLeft * HP_PER_CHAFF)) {
                            float chaffSpeed = (float) ((CHAFF_RANDOM_SPEED / 2f) + (CHAFF_RANDOM_SPEED * Math.random()));
                            Vector2f chaffVel = MathUtils.getRandomPointOnCircumference(Misc.ZERO, chaffSpeed);
                            chaffVel.x += vel.x;
                            chaffVel.y += vel.y;
                            engine.spawnProjectile(source, weapon, CHAFF_WEAPON_ID, loc, angle, chaffVel);
                            chaffLeft--;
                        }

                        spikeTorpedoes.put(proj, chaffLeft);

                        break;

                    // handle canister firing
                    case "vayra_shockweb_canister":
                    case "vayra_canister_shot":

                        if (proj.didDamage() || proj.isFading()) {
                            break;
                        }

                        float canisterTime = CANISTER_LIFETIME;
                        if (canisters.containsKey(proj)) {
                            canisterTime = canisters.get(proj);
                        }

                        canisterTime -= amount;
                        canisters.put(proj, canisterTime);

                        if (canisterTime <= 0f && weapon.getId() != null) {
                            for (int canisterSpike = 0; canisterSpike < CANISTER_SPIKES; canisterSpike++) {
                                float canisterAngle = (float) (Math.random() * CANISTER_SPREAD);
                                if (Math.random() > 0.5f) {
                                    canisterAngle *= -1f;
                                }
                                engine.spawnProjectile(source, weapon, weapon.getId() + "_copy", loc, angle + canisterAngle, Misc.ZERO);
                            }
                            Color canisterFlash = new Color(125, 200, 255, 255);
                            engine.spawnExplosion(loc, vel, canisterFlash, 18f, 0.33f);
                            for (int p = 0; p < (int) (CANISTER_SPIKES * 3); p++) {
                                float partSize = (float) ((data.pMaxSize * 0.2f) + (Math.random() * data.pMaxSize * 0.8f));
                                float partSpeed = (float) (data.pMaxSpeed + (Math.random() * data.pMaxSpeed));
                                float partAngle = angle + (float) (-CANISTER_SPREAD + (Math.random() * CANISTER_SPREAD * 2f));
                                Vector2f partVel = translatePolar(vel, partSpeed, partAngle);
                                engine.addHitParticle(loc, partVel, partSize, 1f, data.pBright, canisterFlash);
                            }
                            Global.getSoundPlayer().playSound("vayra_jezail_battery_noise", 1f, 1f, loc, vel);
                            engine.removeEntity(proj);
                        }
                        break;

                    // handle scatterblast scattering
                    case "vayra_scatterblast_shot":
                        IntervalUtil scatterInterval;
                        if (!scatterBlasts.containsKey(proj)) {
                            scatterInterval = new IntervalUtil(0.1f, 0.25f);
                            scatterBlasts.put(proj, scatterInterval);
                        } else {
                            scatterInterval = scatterBlasts.get(proj);
                        }
                        scatterInterval.advance(amount);
                        if (scatterInterval.intervalElapsed()) {
                            //do stuff
                        }
                        break;

                    // handle rod AND/OR CPKV arcing
                    case "vayra_jordan_bomb":
                    case "vayra_capacitive_rod":

                        if (proj.didDamage() || proj.isFading()) {
                            break;
                        }

                        float capacitiveArcRange = 0f;
                        if ("vayra_jordan_bomb".equals(projId)) {
                            capacitiveArcRange = LPCKV_ARC_RANGE;
                        } else if ("vayra_capacitive_rod".equals(projId)) {
                            capacitiveArcRange = ROD_ARC_RANGE;
                        }

                        Color capacitiveArcCore = new Color(235, 235, 255, 220);
                        Color capacitiveArcFringe = new Color(110, 170, 255, 175);

                        // spawn some random bits all (half) the time
                        if (Math.random() <= 0.5f) {
                            float partSize = (float) ((20f * 0.2f) + (Math.random() * 20f * 0.8f));
                            Vector2f partVel = MathUtils.getRandomPointOnCircumference(zero, capacitiveArcRange);
                            float partBright = (float) (0.75f + (0.25f * Math.random()));
                            float partTTL = (float) (0.25f + (0.5f * Math.random()));
                            engine.addHitParticle(loc, partVel, partSize, partBright, partTTL, capacitiveArcFringe);
                        }

                        // handle arcs
                        if (!capacitiveRodTargets.containsKey(proj) && engine.isEntityInPlay(proj)) {
                            capacitiveRodTargets.put(proj, new ArrayList<CombatEntityAPI>());
                        }
                        if (CombatUtils.getEntitiesWithinRange(loc, capacitiveArcRange).size() > 0) {
                            List<CombatEntityAPI> potentialTargets = CombatUtils.getEntitiesWithinRange(loc, capacitiveArcRange);
                            List<ShipAPI> targetShips = new ArrayList<>();
                            List<MissileAPI> targetMissiles = new ArrayList<>();
                            for (CombatEntityAPI p : potentialTargets) {
                                if (capacitiveRodTargets.get(proj).contains(p)) {
                                    continue;
                                }
                                if (p.getOwner() != source.getOwner() && p instanceof ShipAPI) {
                                    targetShips.add((ShipAPI) p);
                                } else if (p.getOwner() != source.getOwner() && p instanceof MissileAPI) {
                                    targetMissiles.add((MissileAPI) p);
                                }
                            }
                            CombatEntityAPI target = null;
                            if (!targetShips.isEmpty()) {
                                target = targetShips.get(0);
                            } else if (!targetMissiles.isEmpty()) {
                                target = targetMissiles.get(0);
                            }
                            if (target != null) {
                                engine.spawnEmpArc(
                                        source,
                                        loc,
                                        proj,
                                        target,
                                        DamageType.ENERGY,
                                        ROD_ARC_DAMAGE,
                                        ROD_ARC_EMP,
                                        9999f,
                                        EMP_SOUND,
                                        20f,
                                        capacitiveArcFringe,
                                        capacitiveArcCore);
                                capacitiveRodTargets.get(proj).add(target);
                            }
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private static Vector2f translatePolar(Vector2f center, float radius, float angle) {
        float radians = (float) Math.toRadians(angle);
        return new Vector2f(
                (float) FastTrig.cos(radians) * radius + (center == null ? 0f : center.x),
                (float) FastTrig.sin(radians) * radius + (center == null ? 0f : center.y)
        );
    }
}
