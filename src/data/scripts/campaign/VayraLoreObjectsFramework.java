package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static data.scripts.VayraMergedModPlugin.MOD_ID;

public class VayraLoreObjectsFramework implements EveryFrameScript {

    public static Logger log = Global.getLogger(VayraLoreObjectsFramework.class);
    public static final String KEY = "$vayra_loreObjectsFramework";

    public static class LoreObjectData {

        public final String uniqueId;
        public final String factionId;
        public final String title;
        public final String text;
        public final List<String> tags;

        public LoreObjectData(
                String uniqueId,
                String factionId,
                String title,
                String text,
                List<String> tags
        ) {
            this.uniqueId = uniqueId;
            this.factionId = factionId;
            this.title = title;
            this.text = text;
            this.tags = tags;
        }
    }

    public static final String ENTITY_LIST_PATH = "data/config/vayraProcgenEntities/lore_objects.csv";

    public Map<String, LoreObjectData> loreObjects = new HashMap<>();

    private final WeightedRandomPicker<StarSystemAPI> systems = new WeightedRandomPicker<>();
    private boolean finishedSetup = false;

    public VayraLoreObjectsFramework() {
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static VayraLoreObjectsFramework getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (VayraLoreObjectsFramework) test;
    }

    public LoreObjectData getDataFor(SectorEntityToken entity) {
        return getDataFor(entity.getId());
    }

    public LoreObjectData getDataFor(String id) {
        return loreObjects.get(id);
    }

    @Override
    public void advance(float amount) {

        // set up everything, if we haven't already
        if (!finishedSetup) {
            log.info("setting up...");
            loadObjects();
            placeObjects();

            // once we're set up, don't do it again
            finishedSetup = true;
        }
    }

    // loader for CSV file
    private void loadObjects() {

        try {
            JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("unique_id", ENTITY_LIST_PATH, MOD_ID);

            for (int i = 0; i < spreadsheet.length(); i++) {
                JSONObject row = spreadsheet.getJSONObject(i);

                // get lore object ID
                String uniqueId;
                if (row.has("unique_id") && row.getString("unique_id") != null && !row.getString("unique_id").isEmpty()) {
                    uniqueId = row.getString("unique_id");
                    log.info("loading lore object " + uniqueId);
                } else {
                    log.info("hit empty line, lore object loading ended");
                    continue;
                }

                // create tag list
                List<String> tags = new ArrayList<>();
                String tagListString = row.optString("tags");
                if (tagListString != null && !tagListString.isEmpty()) {
                    tags = new ArrayList<>(Arrays.asList(tagListString.split("\\s*(,\\s*)+")));
                }

                // create LoreObjectData object
                String factionId = row.getString("faction");
                String title = row.getString("title");
                String text = row.getString("text");
                LoreObjectData data = new LoreObjectData(
                        uniqueId,
                        factionId,
                        title,
                        text,
                        tags
                );

                // add LoreObjectData to map, or don't
                if (Global.getSector().getFaction(factionId) != null) {
                    loreObjects.put(uniqueId, data);
                    log.info("loaded lore object id " + uniqueId);
                } else {
                    log.warn("faction ID " + factionId + " returned null, skipping " + uniqueId);
                }

            }
        } catch (IOException | JSONException ex) {
            log.error("lore_objects.csv loading crashed");
        }
    }

    private void stockSystems() {

        log.info("making list of systems to stock with lore objects...");

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float mult = 0.0001f;

            if (system.getPlanets().isEmpty()) {
                continue;
            }

            if (system.hasTag(Tags.THEME_MISC_SKIP)) {
                mult = 1f;
            } else if (system.hasTag(Tags.THEME_MISC)) {
                mult = 3f;
            } else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
                mult = 3f;
            } else if (system.hasTag(Tags.THEME_RUINS)) {
                mult = 5f;
            } else if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) {
                mult = 3f;
            } else if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
                mult = 1f;
            }

            systems.add(system, mult);
        }
    }

    private void placeObjects() {
        WeightedRandomPicker<LoreObjectData> objects = new WeightedRandomPicker<>();

        for (String id : loreObjects.keySet()) {
            objects.add(loreObjects.get(id));
        }

        stockSystems();

        for (LoreObjectData object : objects.getItems()) {
            if (systems.isEmpty()) {
                log.warn("ran out of star systems (HOW?!), restocking...");
                stockSystems();
            }

            StarSystemAPI system = systems.pickAndRemove();
            WeightedRandomPicker<PlanetAPI> planets = new WeightedRandomPicker<>();
            planets.addAll(system.getPlanets());
            PlanetAPI planet = planets.pick();

            SectorEntityToken entity = system.addCustomEntity(object.uniqueId, null, "vayra_listening_post", object.factionId);
            float radius = planet.getRadius();
            float orbitRadius = (float) (radius * (1.5f + Math.random()));
            entity.setCircularOrbitPointingDown(planet, (float) Math.random() * 360f, orbitRadius, radius * 0.2f);

            log.info("placed lore object " + object.uniqueId + " in " + system.getNameWithLowercaseType());
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
