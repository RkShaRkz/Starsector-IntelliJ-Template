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
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class VayraProjHandlerPlugin extends BaseEveryFrameCombatPlugin {

    /////////////////////////////GENERAL/////////////////////////////
    public static final Map<String, ProjData> PROJ_IDS = new HashMap<>();

    // spec ID of projectile to replace, spec ID of WEAPON to replace it with the projectile of
    // if the new weapon spec ID is null, projectile won't be replaced -- can use this to trigger effects
    // also some special muzzle flash/fx stuff
    static {
        PROJ_IDS.put("vayra_biorifle_goo", new ProjData(null));
        PROJ_IDS.put("vayra_biorifle_goo_copy", new ProjData(null));
        PROJ_IDS.put("vayra_shortcircuit_shot", new ProjData(null));
    }

    /////////////////////////////CONFIG/////////////////////////////
    private static final String EMP_SOUND = "tachyon_lance_emp_impact"; // standard EMP arc sound for everything

    private Map<DamagingProjectileAPI, Vector2f> biorifleGoo = new HashMap<>();
    private Map<DamagingProjectileAPI, Integer> shortcircuitArcs = new HashMap<>();

    //////////////////////////////BITS//////////////////////////////
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
            this.pColor = pColor != null ? pColor : new Color(255, 255, 255, 180);
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
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.isPaused()) {
            return;
        }
        
        List<DamagingProjectileAPI> projToRemove= new ArrayList<>();
        for (DamagingProjectileAPI test : shortcircuitArcs.keySet()) {
            if (!engine.isEntityInPlay(test)) projToRemove.add(test);
        }
        for (DamagingProjectileAPI test : projToRemove) {
            shortcircuitArcs.remove(test);
        }

        // catch all projectiles and loop through them
        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        int size = projectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projectiles.get(i);
            String projId = proj.getProjectileSpecId();

            // handle all the special stuff for special projs we caught
            if (engine.isEntityInPlay(proj) && PROJ_IDS.containsKey(projId)) {

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

                    // handle goo gooing
                    case "vayra_biorifle_goo":

                        if (weapon == null) {
                            // if we don't have a weapon none of this code works so fuck it give up
                            break;
                        }

                        String copyId = "vayra_biorifle_copy";
                        Vector2f biorifleTargetLoc = null;

                        // if we don't have a source ship we don't have a target so ignore this part (and explode at max range later)
                        if (source != null) {
                            // if we already have a saved target, use that, otherwise get the mouse location
                            if (biorifleGoo.get(proj) != null) {
                                biorifleTargetLoc = biorifleGoo.get(proj);
                            } else {
                                biorifleTargetLoc = new Vector2f(source.getMouseTarget());
                                biorifleGoo.put(proj, biorifleTargetLoc);
                            }

                            // if we have a ship target just use that instead, even if it overrides the saved mouse target
                            if (source.getShipTarget() != null) {
                                biorifleTargetLoc = source.getShipTarget().getLocation();
                            }
                        }

                        // spawn some random bits all (half) the time
                        if (Math.random() <= 0.5f) {
                            float partSize = (float) ((20f * 0.2f) + (Math.random() * 20f * 0.8f));
                            float partSpeed = (float) (25f + (Math.random() * 25f));
                            float partAngle = (float) (Math.random() * 360f);
                            Vector2f partVel = translatePolar(zero, partSpeed, partAngle);
                            float partBright = (float) (0.75f + (0.25f * Math.random()));
                            float partTTL = (float) (0.5f + (0.5f * Math.random()));
                            engine.addHitParticle(loc, partVel, partSize, partBright, partTTL, new Color(100, 255, 100, 255));
                        }

                        // handles explosion and scattering
                        float biorifleProxFuseRange = 25f; // in units
                        float biorifleMaxRange = weapon.getRange();
                        float biorifleCurrDist = MathUtils.getDistance(proj, weapon.getLocation());
                        boolean biorifleDoExplode = false;
                        if (biorifleCurrDist >= biorifleMaxRange) {
                            biorifleDoExplode = true;
                        } else if (biorifleTargetLoc != null) {
                            float distFromTar = MathUtils.getDistance(proj, biorifleTargetLoc);
                            if (distFromTar <= biorifleProxFuseRange) {
                                biorifleDoExplode = true;
                            }
                        }
                        if (biorifleDoExplode) {
                            // handle visuals
                            engine.spawnExplosion(loc, zero, new Color(100, 255, 100, 255), 250f, 2f);
                            Global.getSoundPlayer().playSound("mine_explosion", 1.5f, 0.4f, loc, zero);
                            for (int p = 0; p < 56; p++) {
                                float partSize = (float) ((20f * 0.2f) + (Math.random() * 20f * 0.8f));
                                float partSpeed = (float) (50f + (Math.random() * 50f));
                                float partAngle = (float) (Math.random() * 360f);
                                Vector2f partVel = translatePolar(zero, partSpeed, partAngle);
                                float partBright = (float) (0.75f + (0.25f * Math.random()));
                                float partTTL = (float) (1f + (4f * Math.random()));
                                engine.addHitParticle(loc, partVel, partSize, partBright, partTTL, new Color(100, 255, 100, 255));
                            }
                            engine.removeEntity(proj);

                            // spawn child projectiles
                            int childCount = 7;
                            for (int p = 0; p < childCount; p++) {
                                float childAngle = 360f / childCount;
                                childAngle *= (p + 1f);
                                float randAngle = 50f;
                                childAngle -= (randAngle / 2f);
                                childAngle += (randAngle * Math.random());
                                Vector2f childSpawnVel = new Vector2f(vel);
                                childSpawnVel.normalise();
                                childSpawnVel.scale(50f);
                                CombatEntityAPI newProj = engine.spawnProjectile(source, weapon, copyId, loc, childAngle, childSpawnVel);
                                float newSpeed = newProj.getVelocity().length();
                                newProj.getVelocity().normalise();
                                newProj.getVelocity().scale((newSpeed - (newSpeed * 0.69f * (float) Math.random())));
                            }
                        }
                        break;
                    case "vayra_biorifle_goo_copy":
                        float bioCopyBaseSpeed = 100f;
                        float bioSpeedTarget = bioCopyBaseSpeed / 5f; // target speed in su/sec
                        float startSlowingTime = .25f; // in seconds

                        // spawn some random bits all (half) the time
                        if (Math.random() <= 0.5f) {
                            float partSize = (float) ((20f * 0.2f) + (Math.random() * 20f * 0.8f));
                            float partSpeed = (float) (25f + (Math.random() * 25f));
                            float partAngle = (float) (Math.random() * 360f);
                            Vector2f partVel = translatePolar(zero, partSpeed, partAngle);
                            float partBright = (float) (0.75f + (0.25f * Math.random()));
                            float partTTL = (float) (0.25f + (0.5f * Math.random()));
                            engine.addHitParticle(loc, partVel, partSize, partBright, partTTL, new Color(100, 255, 100, 255));
                        }

                        // handle slowdown
                        if (proj.getElapsed() >= startSlowingTime) {
                            float slowMult = Math.min(1f, proj.getElapsed() - startSlowingTime); // positive fractional multiplier
                            float newSpeed = bioCopyBaseSpeed - bioSpeedTarget; // get the difference between current and target speed (still using squares)
                            newSpeed *= (1f - slowMult); // multiply the difference by the inverse of the slowMult
                            newSpeed += bioSpeedTarget; // add the target (minimum) speed
                            vel.normalise();
                            vel.scale(newSpeed);
                        }
                        break;

                    case "vayra_shortcircuit_shot":
                        float shortcircuitRange = 100f;
                        int shortcircuitBaseArcCount = 1;
                        float shortcircuitDamage = 50f;
                        float shortcircuitEmp = 400f;
                        Color shortcircuitCore = new Color(235, 235, 255, 220);
                        Color shortcircuitFringe = new Color(110, 170, 255, 175);

                        // spawn some random bits all (half) the time
                        if (Math.random() <= 0.5f) {
                            float partSize = (float) ((20f * 0.2f) + (Math.random() * 20f * 0.8f));
                            Vector2f partVel = MathUtils.getRandomPointOnCircumference(zero, shortcircuitRange);
                            float partBright = (float) (0.75f + (0.25f * Math.random()));
                            float partTTL = (float) (0.25f + (0.5f * Math.random()));
                            engine.addHitParticle(loc, partVel, partSize, partBright, partTTL, shortcircuitFringe);
                        }

                        // handle short circuiting
                        if (!shortcircuitArcs.containsKey(proj) && engine.isEntityInPlay(proj)) {
                            shortcircuitArcs.put(proj, shortcircuitBaseArcCount);
                        }
                        int shortcircuitArcCount = shortcircuitArcs.get(proj);
                        if (shortcircuitArcCount > 0 && CombatUtils.getEntitiesWithinRange(loc, shortcircuitRange).size() > 0) {
                            List<CombatEntityAPI> potentialTargets = CombatUtils.getEntitiesWithinRange(loc, shortcircuitRange);
                            List<ShipAPI> targetShips = new ArrayList<>();
                            List<MissileAPI> targetMissiles = new ArrayList<>();
                            for (CombatEntityAPI p : potentialTargets) {
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
                                        shortcircuitDamage,
                                        shortcircuitEmp,
                                        9999f,
                                        EMP_SOUND,
                                        20f,
                                        shortcircuitFringe,
                                        shortcircuitCore);
                                shortcircuitArcCount--;
                                shortcircuitArcs.put(proj, shortcircuitArcCount);
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
