// Based on the sylphon Veritas script by Nicke535
package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import static data.scripts.KadurModPlugin.VAYRA_DEBUG;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;

// HEY IDIOT NEXT TIME YOU THINK ABOUT CONSOLIDATING THIS
// REMEMBER IT HAS TO HAVE ALL THIS WEIRD SHIT IN IT
// TO HANDLE YOUR FANCY DISPLAY-LOADED-NEEDLES FUCKERY
public class VayraSplinterPlugin extends BaseEveryFrameCombatPlugin {

    private static final Set<String> SPLINTERPROJ_ID = new HashSet<>(1);
    static {
        SPLINTERPROJ_ID.add("vayra_splintergun_shot");
    }
    public boolean secondShot = false;

    private static final Color COLOR = new Color(33, 103, 109, 150);
    private final List<Map<String, Object>> splinters = new ArrayList<>();
    private final List<Map<String, Object>> splintersToRemove = new ArrayList<>();
    private final List<Map<String, Object>> arcs = new ArrayList<>();
    private final List<Map<String, Object>> arcsToRemove = new ArrayList<>();
    private final Map<Float, ShipAPI> targets = new HashMap<>();
    private float targetShipCounter = 0f;
    private CombatEngineAPI engine;
    private final SpriteAPI splinterSprite = Global.getSettings().getSprite("vayra_splintergun_shot", "1");

    private static final float MUZZLE_OFFSET = 27f; // y offset of muzzles
    private static final float BARREL_OFFSET = 3.5f; // x offset of each barrel (+/-)
    
    public static Logger log = Global.getLogger(VayraSplinterPlugin.class);

    @Override
    public void init(CombatEngineAPI engine) {
        engine.getCustomData().put("KadurSplinterPlugin", this);
        splinters.clear();
        targets.clear();
        targetShipCounter = 0f;
        this.engine = engine;
    }

    private static Vector2f translatePolar(Vector2f center, float radius, float angle) {
        float radians = (float) Math.toRadians(angle);
        return new Vector2f(
            (float) FastTrig.cos(radians) * radius + (center == null ? 0f : center.x),
            (float) FastTrig.sin(radians) * radius + (center == null ? 0f : center.y)
        );
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.isPaused()) {
            return;
        }

        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        int size = projectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projectiles.get(i);
            if (SPLINTERPROJ_ID.contains(proj.getProjectileSpecId()) && engine.isEntityInPlay(proj)) {
                ShipAPI source = proj.getSource();
                WeaponAPI weapon = proj.getWeapon();
                float angle = weapon.getCurrAngle();
                Vector2f weaponLoc = weapon.getLocation();
                float x = weaponLoc.x + MUZZLE_OFFSET;
                float y = weaponLoc.y;
                String spec = proj.getWeapon().getSpec().getWeaponId() + "_copy";
                // proj.getLocation().set(69420f, 69420f); // WHY THE FUCK THEY DON'T GO WAY
                engine.removeEntity(proj);
                if (secondShot) {
                    y += BARREL_OFFSET;
                    secondShot = false;
                } else {
                    y -= BARREL_OFFSET;
                    secondShot = true;
                }
                
                Vector2f offset = new Vector2f(x, y);
                Vector2f loc = VectorUtils.rotateAroundPivot(offset, weapon.getLocation(), angle);
                // Vector2f muzzleFlashOffset = translatePolar(weaponLoc, MUZZLE_OFFSET, angle - BARREL_OFFSET);
                // Vector2f loc, Vector2f vel, Color color, float size, float maxDuration
                engine.spawnExplosion(loc, source.getVelocity(), COLOR, 20f, 0.25f);
                
                for (int p = 0; p < 15; p++) {
                    float partSize = (float) (3f + (Math.random() * 12f));
                    float partSpeed = (float) (150f + (Math.random() * 100f));
                    float partAngle = weapon.getCurrAngle() + (float) (-7f + (Math.random() * 14f));
                    Vector2f partVel = translatePolar(source.getVelocity(), partSpeed, partAngle);
                    // Vector2f loc, Vector2f vel, float size, float brightness, float duration, Color color
                    engine.addHitParticle(loc, partVel, partSize, 1.5f, 0.420f, COLOR);
                }
                        
                // ShipAPI ship, WeaponAPI weapon, String weaponId, Vector2f point, float angle, Vector2f shipVelocity
                engine.spawnProjectile(source, weapon, spec, loc, angle, source.getVelocity());
            }
        }                
    }

    public static void addSplinter(Map<String, Object> data, ShipAPI ship) {
        if (Global.getCombatEngine() == null) {
            return;
        } else if (!(Global.getCombatEngine().getCustomData().get("KadurSplinterPlugin") instanceof VayraSplinterPlugin)) {
            return;
        }
        
        VayraSplinterPlugin plugin = (VayraSplinterPlugin) Global.getCombatEngine().getCustomData().get("KadurSplinterPlugin");
        
        plugin.addData(data, ship);
    }
    
    private void addData(Map<String, Object> data, ShipAPI ship) {
                
        data.put("targetship", targetShipCounter);
        targets.put(targetShipCounter, ship);
        targetShipCounter = targetShipCounter + 1f;
        splinters.add(data);
        arcs.add(data);

        if (targetShipCounter > 69420f) {
            targetShipCounter = 0f;
        }
        
        if (VAYRA_DEBUG) {
            log.info(String.format("stuck a splinter in fucko [%s]", ship.getHullSpec().getHullName()));
        }
    }

    @Override
    public void renderInWorldCoords(ViewportAPI view) {
        if (engine == null) {
            return;
        }

        if (!splinters.isEmpty()) {
            float amount = (engine.isPaused() ? 0f : engine.getElapsedInLastFrame());

            for (Map<String, Object> entry : splinters) {

                ShipAPI ship = targets.get((float) entry.get("targetship"));
                if (ship == null) {
                    splintersToRemove.add(entry);
                    targets.remove((float) entry.get("targetship"));
                    continue;
                }

                float time = (float) entry.get("ttl");
                float baseDamage = (float) entry.get("basedmg");
                float damageMult;

                List<ShipAPI> list = new ArrayList<>();
                for (Map<String, Object> compare : splinters) {
                    ShipAPI ship2 = targets.get((float) compare.get("targetship"));
                    if (ship2 == ship) {
                        float time2 = (float) compare.get("ttl");
                        time = Math.max(time, time2);
                        list.add(ship);
                    }
                }

                time = time - amount;
                entry.put("ttl", time);

                int combineCount = list.size() / 7;
                int bonusCombineCount = Math.max(combineCount - 1, 0);
                if (bonusCombineCount > 0) {
                    damageMult = 2f + (float) (3.5 - 3.4 / (1 + Math.pow((bonusCombineCount / 3.5), 1.1)));
                } else {
                    damageMult = combineCount + 1f;
                }
                float damage = baseDamage * damageMult;
                if (damage > (float) entry.get("explosiondmg")) {
                    entry.put("explosiondmg", damage);
                }

                if (time <= 0 || ship.isPiece() || ship.isHulk() || ship.isPhased()) {
                    explode(entry, ship);
                    splintersToRemove.add(entry);
                    targets.remove((float) entry.get("targetship"));
                } else {
                    renderSprite(
                            splinterSprite,
                            (float) entry.get("width"),
                            (float) entry.get("height"),
                            (float) entry.get("relativeangle"),
                            (float) entry.get("relativex"),
                            (float) entry.get("relativey"),
                            ship
                    );
                }
            }
            if (!splintersToRemove.isEmpty()) {
                for (Map<String, Object> w : splintersToRemove) {
                    splinters.remove(w);
                }
                splintersToRemove.clear();
            }
        }
    }

    private void renderSprite(SpriteAPI sprite, float width, float height, float angle, float x, float y, ShipAPI ship) {
        sprite.setAlphaMult(1f);
        sprite.setSize(width, height);
        sprite.setAngle(angle - 90 + ship.getFacing());

        Vector2f renderPos = new Vector2f(x, y);
        renderPos = VectorUtils.rotateAroundPivot(renderPos, new Vector2f(0f, 0f), ship.getFacing(), new Vector2f(0f, 0f));
        renderPos.x += ship.getLocation().x;
        renderPos.y += ship.getLocation().y;

        sprite.renderAtCenter(renderPos.x, renderPos.y);

        for (Map<String, Object> entry : arcs) {
            float arcX = (float) entry.get("relativex");
            float arcY = (float) entry.get("relativey");
            Vector2f arcLoc = new Vector2f(arcX, arcY);
            arcLoc = VectorUtils.rotateAroundPivot(arcLoc, new Vector2f(0f, 0f), ship.getFacing(), new Vector2f(0f, 0f));
            arcLoc.x += ship.getLocation().x;
            arcLoc.y += ship.getLocation().y;
            SimpleEntity arc = new SimpleEntity(arcLoc);

            for (Map<String, Object> spike : splinters) {
                ShipAPI ship2 = targets.get((float) spike.get("targetship"));

                if (ship2 == ship) {
                    float spikeX = (float) spike.get("relativex");
                    float spikeY = (float) spike.get("relativey");
                    Vector2f loc = new Vector2f(spikeX, spikeY);
                    loc = VectorUtils.rotateAroundPivot(loc, new Vector2f(0f, 0f), ship.getFacing(), new Vector2f(0f, 0f));
                    loc.x += ship.getLocation().x;
                    loc.y += ship.getLocation().y;

                    if (Math.random() < (0.69f / arcs.size())) {
                        engine.spawnEmpArcPierceShields(
                                (ShipAPI) spike.get("source"),
                                loc,
                                ship,
                                arc,
                                DamageType.OTHER,
                                0f,
                                0f,
                                69420f,
                                "tachyon_lance_emp_impact",
                                (float) spike.get("explosiondmg") / 10f,
                                COLOR,
                                COLOR.brighter()
                        );
                    }
                }
            }
            arcsToRemove.add(entry);
        }

        if (!arcsToRemove.isEmpty()) {
            for (Map<String, Object> w : arcsToRemove) {
                arcs.remove(w);
            }
            arcsToRemove.clear();
        }
    }

    private void explode(Map<String, Object> spike, ShipAPI ship) {
        if (ship != null) {
            if (VAYRA_DEBUG) {
                log.info(String.format("this splinter blowing up right now deals [%s] damage to [%s]", spike.get("explosiondmg"), ship.getHullSpec().getHullName()));
            }
            float x = (float) spike.get("relativex");
            float y = (float) spike.get("relativey");
            float damage = (float) spike.get("explosiondmg");
            Vector2f explosionLoc = new Vector2f(x, y);
            explosionLoc = VectorUtils.rotateAroundPivot(explosionLoc, new Vector2f(0f, 0f), ship.getFacing(), new Vector2f(0f, 0f));
            explosionLoc.x += ship.getLocation().x;
            explosionLoc.y += ship.getLocation().y;
            engine.applyDamage(ship, explosionLoc, damage, DamageType.HIGH_EXPLOSIVE, 0, true, false, spike.get("source"), true);
            // Vector2f loc, Vector2f vel, Color color, float size, float maxDuration
            engine.spawnExplosion(explosionLoc, new Vector2f(0f, 0f), COLOR, (float) (Math.pow(damage, 0.33) * 25f), (float) (Math.pow(damage, 0.33) / 10f));
            Global.getSoundPlayer().playSound("vayra_needlercrit", 1f, 1f, explosionLoc, new Vector2f(0f, 0f));
        }
    }
}
