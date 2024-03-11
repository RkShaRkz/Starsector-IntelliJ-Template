package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RingBandAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.ai.VayraLRMAI;
import data.scripts.ai.VayraSplinterAI;
import data.scripts.campaign.KadurBlueprintStocker;
import data.scripts.world.KadurGen;
import exerelin.campaign.SectorManager;
import java.awt.Color;
import static java.lang.Math.random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

public class KadurModPlugin extends BaseModPlugin {

    public static Logger log = Global.getLogger(KadurModPlugin.class);

    public static final String KADUR_ID = "kadur_remnant";

    public static final String KADUR_SRM = "vayra_partisanmis";
    public static final String KADUR_LRM = "vayra_jerichomis";
    public static final String KADUR_SLOWLRM = "vayra_slowlrm_copy";
    public static final String KADUR_SPLINTER = "vayra_splintergun_shot_copy";
    public static final String KADUR_ANTIFTR = "vayra_antifighter_mis";

    public static boolean VAYRA_DEBUG = false;

    public static boolean EXERELIN_LOADED;
    public static Set<String> EXERELIN_ACTIVE = new HashSet<>();

    @Override
    public void onApplicationLoad() throws Exception {
        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.lazywizard.lazylib.ModUtils");
        } catch (ClassNotFoundException lazy) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "LazyLib is required to run Kadur Remnant."
                    + System.lineSeparator() + System.lineSeparator()
                    + "You can download LazyLib at http://fractalsoftworks.com/forum/index.php?topic=5444"
                    + System.lineSeparator();
            throw new ClassNotFoundException(message);
        }

        try {
            Global.getSettings().getScriptClassLoader().loadClass("data.scripts.util.MagicTargeting");
        } catch (ClassNotFoundException magic) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "MagicLib is required to run Kadur Remnant."
                    + System.lineSeparator() + System.lineSeparator()
                    + "You can download MagicLib at http://fractalsoftworks.com/forum/index.php?topic=13718.0"
                    + System.lineSeparator();
            throw new ClassNotFoundException(message);
        }
        //VayraTags.checkSpecial();

        EXERELIN_LOADED = Global.getSettings().getModManager().isModEnabled("nexerelin");
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case KADUR_LRM:
                return new PluginPick<MissileAIPlugin>(new VayraLRMAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case KADUR_SPLINTER:
                return new PluginPick<MissileAIPlugin>(new VayraSplinterAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            default:
                return null;
        }
    }

    @Override
    public void onNewGame() {

        if (EXERELIN_LOADED) {
            if (!SectorManager.getCorvusMode()) {
                // return here because we don't want to generate our handcrafted systems if we're in an exerelin random sector
                return;
            }
        }
        genKadur();
    }

    @Override
    public void onNewGameAfterTimePass() {

        log.info("new game started, adding scripts");

        // add these scripts regardless of setting, since they all just return immediately if not activated
        // and this way they will activate if you activate the setting midgame
        Global.getSector().addScript(new KadurBlueprintStocker());

    }

    private static void genKadur() {
        new KadurGen().generate(Global.getSector());
    }

    public static float randomRange(float min, float max) {
        return (float) (random() * (max - min) + min);
    }

    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name,
            int size, ArrayList<String> marketConditions, ArrayList<String> submarkets, boolean WithJunkAndChatter, boolean PirateMode, boolean freePort) {

        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String entityId = primaryEntity.getId();
        String marketId = entityId + "market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketId, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", newMarket.getFaction().getTariffFraction());

        if (submarkets != null) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        newMarket.addCondition("population_" + size);
        if (marketConditions != null) {
            for (String condition : marketConditions) {
                try {
                    newMarket.addCondition(condition);
                } catch (RuntimeException e) {
                    newMarket.addIndustry(condition);
                }
            }
        }

        if (connectedEntities != null) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        globalEconomy.addMarket(newMarket, WithJunkAndChatter);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        createAdmin(newMarket);

        if (connectedEntities != null) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        if (PirateMode) {
            newMarket.setEconGroup(newMarket.getId());
            newMarket.setHidden(true);
            primaryEntity.setSensorProfile(1f);
            primaryEntity.setDiscoverable(true);
            primaryEntity.getDetectedRangeMod().modifyFlat("gen", 5000f);
            newMarket.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);
        }

        newMarket.setFreePort(freePort);

        for (MarketConditionAPI mc : newMarket.getConditions()) {
            mc.setSurveyed(true);
        }
        newMarket.setSurveyLevel(SurveyLevel.FULL);

        newMarket.reapplyIndustries();

        log.info("created " + factionID + " market " + name);

        return newMarket;
    }

    public static PersonAPI createAdmin(MarketAPI market) {
        FactionAPI faction = market.getFaction();
        PersonAPI admin = faction.createRandomPerson();
        int size = market.getSize();

        switch (size) {
            case 3:
            case 4:
                admin.setRankId(Ranks.GROUND_CAPTAIN);
                break;
            case 5:
                admin.setRankId(Ranks.GROUND_MAJOR);
                break;
            case 6:
                admin.setRankId(Ranks.GROUND_COLONEL);
                break;
            case 7:
            case 8:
            case 9:
            case 10:
                admin.setRankId(Ranks.GROUND_GENERAL);
                break;
            default:
                admin.setRankId(Ranks.GROUND_LIEUTENANT);
                break;
        }

        List<String> skills = Global.getSettings().getSortedSkillIds();

        int industries = 0;
        int defenses = 0;
        boolean military = market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY);

        for (Industry curr : market.getIndustries()) {
            if (curr.isIndustry()) {
                industries++;
            }
            if (curr.getSpec().hasTag(Industries.TAG_GROUNDDEFENSES)) {
                defenses++;
            }
        }

        admin.getStats().setSkipRefresh(true);

        int num = 0;
        if (industries >= 2 || (industries == 1 && defenses == 1)) {
            if (skills.contains(Skills.INDUSTRIAL_PLANNING)) {
                admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 2);
            }
            num++;
        }

        if (num == 0 || size >= 6) {
            if (military) {
                if (skills.contains(Skills.SPACE_OPERATIONS)) {
                    admin.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 2);
                }
            } else if (defenses > 0) {
                if (skills.contains(Skills.PLANETARY_OPERATIONS)) {
                    admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 2);
                }
            } else {
                // nothing else suitable, so just make sure there's at least one skill, if this wasn't already set
                if (skills.contains(Skills.INDUSTRIAL_PLANNING)) {
                    admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
                }
            }
        }

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        admin.getStats().setSkipRefresh(false);
        admin.getStats().refreshCharacterStatsEffects();
        admin.setPostId(Ranks.POST_ADMINISTRATOR);
        market.addPerson(admin);
        market.setAdmin(admin);
        market.getCommDirectory().addPerson(admin);
        ip.addPerson(admin);
        ip.getData(admin).getLocation().setMarket(market);
        ip.checkOutPerson(admin, "permanent_staff");

        log.info(String.format("Applying admin %s %s to market %s", market.getFaction().getRank(admin.getRankId()), admin.getNameString(), market.getName()));

        return admin;
    }

    public static String[] JSONArrayToStringArray(JSONArray jsonArray) {
        try {
            String[] ret = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                ret[i] = jsonArray.getString(i);
            }
            return ret;
        } catch (JSONException e) {
            log.warn(e);
            return new String[]{};
        }
    }

    public static String aOrAn(String input) {

        ArrayList<String> vowels = new ArrayList<>(Arrays.asList(
                "a",
                "e",
                "i",
                "o",
                "u"));

        String firstLetter = input.substring(0, 1).toLowerCase();

        if (vowels.contains(firstLetter)) {
            return "an";
        } else {
            return "a";
        }
    }

    public static float Rotate(float currAngle, float addAngle) {
        return (currAngle + addAngle) % 360;
    }

    public static void addAccretionDisk(PlanetAPI star, String name) {
        StarSystemAPI system = star.getStarSystem();
        float orbitRadius = star.getRadius() * 8f;
        float bandWidth = 256f;
        int numBands = 12;

        for (float i = 0; i < numBands; i++) {
            float radius = orbitRadius - i * bandWidth * 0.25f - i * bandWidth * 0.1f;
            float orbitDays = radius / (30f + 10f * Misc.random.nextFloat());
            WeightedRandomPicker<String> rings = new WeightedRandomPicker<>();
            rings.add("rings_dust0");
            rings.add("rings_ice0");
            String ring = rings.pick();
            RingBandAPI visual = system.addRingBand(star, "misc", ring, 256f, 0, Color.white, bandWidth,
                    radius + bandWidth / 2f, -orbitDays);
            float spiralFactor = 2f + Misc.random.nextFloat() * 5f;
            visual.setSpiral(true);
            visual.setMinSpiralRadius(star.getRadius());
            visual.setSpiralFactor(spiralFactor);
        }
        SectorEntityToken ring = system.addTerrain(Terrain.RING, new RingParams(orbitRadius, orbitRadius / 2f, star, name == null ? "Accretion Disk" : name));
        ring.addTag(Tags.ACCRETION_DISK);
        ring.setCircularOrbit(star, 0, 0, -100);
    }
}
