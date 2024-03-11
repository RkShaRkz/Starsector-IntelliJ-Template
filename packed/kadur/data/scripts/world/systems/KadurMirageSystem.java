package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static data.scripts.KadurModPlugin.KADUR_ID;
import static data.scripts.KadurModPlugin.addMarketplace;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class KadurMirageSystem implements SectorGeneratorPlugin {

    private static final float BURKAAN_ORBIT = 2500;
    private static final float INNER_RING = 4000;
    private static final float OASIS_ORBIT = 5250;
    private static final float KADUR_ORBIT = 8000;
    private static final float MIDDLE_RING = 9500;
    private static final float YAKCHAL_ORBIT = 12500;
    private static final float OUTER_RING = 15500;

    /**
     *
     * @param system
     * @param variantId
     * @param condition
     * @param recoverable
     * @return
     */
    private SectorEntityToken addDerelictShip(StarSystemAPI system,
            String variantId,
            ShipRecoverySpecial.ShipCondition condition,
            boolean recoverable) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
        return ship;
    }

    @Override
    public void generate(SectorAPI sector) {

        boolean historical = Global.getSector().getClock().getCycle() < 196;
        boolean bigmags = Global.getSettings().getModManager().isModEnabled("bigmags");

        StarSystemAPI system = sector.createStarSystem("Mirage");
        system.getLocation().set(4000, -18000);
        system.setBackgroundTextureFilename("graphics/backgrounds/kadur_background2.jpg");

        // this was where Mirage was originally
        // system.getLocation().set(-2623, 10823);
        // this was where Oasis was originally
        // system.getLocation().set(7121, 7723);
        // this was where Kadur was originally
        // system.getLocation().set(1332, 14495);
        // primary star
        PlanetAPI star = system.initStar("vayra_mirage_star", StarTypes.YELLOW, 750f, 600);
        system.setLightColor(new Color(255, 213, 133));
        star.setCustomDescriptionId("vayra_mirage_star");

        // secondary (initial position in degrees, size in pixels, distance in pixels, orbit speed in days)
        PlanetAPI star2 = system.addPlanet("vayra_truth_star", star, "Truth", StarTypes.WHITE_DWARF, 225, 400, 1500, 45);
        Misc.initConditionMarket(star2);
        system.addCorona(star2, 150, 2f, 0f, 1f);
        star2.setCustomDescriptionId("vayra_truth_star");

        PlanetAPI mirageI = system.addPlanet("mirageI", star, "Burkaan", "lava", 0, 75, BURKAAN_ORBIT, 88);
        SectorEntityToken vayra_burkaanfield = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(mirageI.getRadius() + 200f, // terrain effect band width 
                        (mirageI.getRadius() + 200f) / 2f, // terrain effect middle radius
                        mirageI, // entity that it's around
                        mirageI.getRadius() + 50f, // visual band start
                        mirageI.getRadius() + 50f + 250f, // visual band end
                        new Color(50, 20, 100, 40), // base color
                        0.5f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(140, 100, 235),
                        new Color(180, 110, 210),
                        new Color(150, 140, 190),
                        new Color(140, 190, 210),
                        new Color(90, 200, 170),
                        new Color(65, 230, 160),
                        new Color(20, 220, 70)
                ));
        vayra_burkaanfield.setCircularOrbit(mirageI, 0, 0, 100);
        if (!historical) {
            // Burkaan, hot metallic planet close to the suns (initial position in degrees, size in pixels, distance in pixels, orbit speed in days)
            mirageI.setFaction("independent");
            mirageI.setInteractionImage("illustrations", "pirate_station");
            mirageI.setCustomDescriptionId("vayra_burkaan");

            MarketAPI mirageImarket = addMarketplace("independent", mirageI, null,
                    "Burkaan", // name of the market
                    3, // size of the market
                    new ArrayList<>(
                            Arrays.asList( // list of market_conditions ids
                                    "volatiles_diffuse",
                                    "tectonic_activity",
                                    "very_hot",
                                    "ore_moderate",
                                    "rare_ore_moderate",
                                    Industries.POPULATION,
                                    Industries.SPACEPORT,
                                    Industries.MINING)),
                    new ArrayList<>(
                            Arrays.asList( // which submarkets to generate
                                    Submarkets.SUBMARKET_BLACK,
                                    Submarkets.SUBMARKET_OPEN,
                                    Submarkets.SUBMARKET_STORAGE)),
                    true, // with junk and chatter?
                    false, // pirate mode? (i.e. hidden)
                    true); // freeport
        }

        // the oasis belt provides much-needed shade from the twin suns allowing Oasis (and formerly, Kadur) to remain habitable
        system.addAsteroidBelt(star, 450, INNER_RING - 100, 666, 100, 200, Terrain.ASTEROID_BELT, "The Seraphim");
        system.addRingBand(star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, INNER_RING - 250, 130f, null, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, INNER_RING - 50, 400f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 1024f, 3, Color.white, 1024f, INNER_RING - 150, 666f, null, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, INNER_RING + 150, 69f, null, null);
        system.addAsteroidBelt(star, 100, INNER_RING, 256, 150, 250, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, INNER_RING - 200, 128, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, INNER_RING - 100, 188, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, INNER_RING - 150, 256, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, INNER_RING - 350, 80f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, INNER_RING - 150, 120f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, INNER_RING + 150, 160f);
        SectorEntityToken ring = system.addTerrain(Terrain.RING, new RingParams(666, INNER_RING - 100, null, "Seraphim Ring"));
        ring.setCircularOrbit(star, 0, 0, 100);

        // Oasis was seized by the Hegemony following the subjugation of Kadur
        // (initial position in degrees, size in pixels, distance in pixels, orbit speed in days)
        PlanetAPI mirageII = system.addPlanet("mirageII", star, "Oasis", "jungle", 120, 220, OASIS_ORBIT, 365);
        if (historical) {
            mirageII.setFaction("kadur_remnant");
        } else {
            mirageII.setFaction("hegemony");
        }
        mirageII.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
        mirageII.getSpec().setGlowColor(new Color(255, 215, 185, 255));
        mirageII.getSpec().setUseReverseLightForGlow(true);
        mirageII.applySpecChanges();
        mirageII.setInteractionImage("illustrations", "eventide");
        mirageII.setCustomDescriptionId("vayra_oasis");

        MarketAPI mirageIImarket = addMarketplace("hegemony", mirageII, null,
                "Oasis", // name of the market
                8, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of conditions.IDs and Industries.IDs
                                "habitable",
                                "organics_common",
                                "regional_capital",
                                "mild_climate",
                                "farmland_rich",
                                "vayra_kadur_refugees",
                                "vayra_kadur_majority",
                                Industries.POPULATION,
                                Industries.FARMING,
                                Industries.SPACEPORT,
                                Industries.STARFORTRESS,
                                Industries.GROUNDDEFENSES,
                                Industries.HEAVYINDUSTRY,
                                Industries.MILITARYBASE,
                                Industries.WAYSTATION,
                                Industries.MINING,
                                "dissident")),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        // nex storyline compatibility
        mirageIImarket.getMemoryWithoutUpdate().set("$startingFactionId", KADUR_ID);

        // L4 relay  (initial position in degrees, distance in pixels, orbit speed in days)
        SectorEntityToken relay = system.addCustomEntity("mirage_relay", // unique id
                "Mirage Relay", // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "hegemony"); // faction
        relay.setCircularOrbitPointingDown(star, 120 - 60, OASIS_ORBIT, 365);

        // L5 jump point  (initial position in degrees, distance in pixels, orbit speed in days)
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("mirage_ii_jump", "Oasis Jump Point");
        jumpPoint.setCircularOrbit(star, 120 + 60, OASIS_ORBIT, 365);
        jumpPoint.setRelatedPlanet(mirageII);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        // Kadur was decimated by a Planet-Killer class virus bomb  (initial position in degrees, size in pixels, distance in pixels, orbit speed in days)
        PlanetAPI mirageIII = system.addPlanet("mirageIII", star, "Kadur", "toxic", 180, 200, KADUR_ORBIT, 400);
        mirageIII.setInteractionImage("illustrations", "vayra_fort_toxx");
        mirageIII.setCustomDescriptionId("vayra_kadur");
        system.addRingBand(mirageIII, "misc", "rings_dust0", 256f, 2, Color.green, 256f, 215, 115f);
        system.addRingBand(mirageIII, "misc", "rings_dust0", 256f, 2, Color.green, 256f, 220, 125f);
        Misc.initConditionMarket(mirageIII);
        mirageIII.getMarket().addCondition(Conditions.ORE_ULTRARICH);
        mirageIII.getMarket().addCondition(Conditions.RARE_ORE_ABUNDANT);
        mirageIII.getMarket().addCondition(Conditions.FARMLAND_POOR);
        mirageIII.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);
        mirageIII.getMarket().addCondition("vayra_fake_ruins");
        mirageIII.getMarket().addCondition("vayra_virus_bomb");
        mirageIII.getMarket().addCondition("vayra_tomb_world");
        mirageIII.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        for (MarketConditionAPI condition : mirageIII.getMarket().getConditions()) {
            condition.setSurveyed(true);
        }
        mirageIII.getSpec().setAtmosphereThickness(1f);
        mirageIII.getSpec().setCloudRotation(10f);
        mirageIII.getSpec().setAtmosphereColor(new Color(210, 255, 138, 180));
        mirageIII.applySpecChanges();
        // nex storyline compatibility
        mirageIII.getMarket().getMemoryWithoutUpdate().set("$startingFactionId", KADUR_ID);

        // Qamar, the useless moon, former home of the Exiles and Insurgency
        PlanetAPI mirageIIIA = system.addPlanet("mirageIIIA", mirageIII, "Qamar", "barren-bombarded", 180, 60, 470, 35);
        Misc.initConditionMarket(mirageIIIA);
        mirageIIIA.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        mirageIIIA.getMarket().addCondition(Conditions.METEOR_IMPACTS);

        // Pirates hang out in the wreckage over Kadur
        SectorEntityToken vayra_kadur_toxx = system.addCustomEntity("vayra_kadur_toxx", "Fort Toxx", "station_sporeship_derelict", "pirates");
        vayra_kadur_toxx.setInteractionImage("illustrations", "vayra_fort_toxx");
        vayra_kadur_toxx.setCircularOrbitPointingDown(mirageIII, 60, 300, 70);
        vayra_kadur_toxx.setCustomDescriptionId("vayra_kadur_toxx");

        MarketAPI vayra_kadur_toxxmarket = addMarketplace("pirates", vayra_kadur_toxx, null,
                "Fort Toxx", // name of the market
                3, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_conditions ids
                                "vayra_space_wreck",
                                Industries.HEAVYINDUSTRY,
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                "no_atmosphere")),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                true); // freeport

        // There's a whooole bunch of shit up here and it's real dangerous, higher XP/difficulty than normal for salvage
        DebrisFieldParams params = new DebrisFieldParams(
                666f, // field radius - should not go above 1000 for performance reasons
                2f, // density, visual - affects number of debris pieces
                66642069f, // duration in days 
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldSource.MIXED;
        params.baseSalvageXP = 1000; // base XP for scavenging in field
        SectorEntityToken debris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
        SalvageSpecialAssigner.assignSpecialForDebrisField(debris);
        debris.setSensorProfile(null);
        debris.setDiscoverable(null);
        debris.setCircularOrbit(star, 180, KADUR_ORBIT, 400);

        SectorEntityToken toxxCamel = addDerelictShip(system, "vayra_camel_shotgun", ShipRecoverySpecial.ShipCondition.AVERAGE, true);
        toxxCamel.setCircularOrbit(mirageIII, 69, 240, 69f);

        SectorEntityToken toxxFalchion = addDerelictShip(system, "vayra_falchion_assault", ShipRecoverySpecial.ShipCondition.BATTERED, (Math.random() > 0.75f));
        toxxFalchion.setCircularOrbit(mirageIII, 150, 160, 69f);

        SectorEntityToken toxxSphinx = addDerelictShip(system, "vayra_sphinx_artillery", ShipRecoverySpecial.ShipCondition.WRECKED, false);
        toxxSphinx.setCircularOrbit(mirageIII, 303, 303, 69f);

        // An intermediary smaller belt shades Yakchal, guaranteeing it remains frozen
        system.addAsteroidBelt(star, 450, MIDDLE_RING - 100, 666, 100, 200, Terrain.ASTEROID_BELT, "Arrad Belt");
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, MIDDLE_RING - 50, 400f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 1024f, 3, Color.white, 1024f, MIDDLE_RING - 150, 666f, null, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, MIDDLE_RING + 150, 69f, null, null);
        system.addAsteroidBelt(star, 100, MIDDLE_RING, 256, 150, 250, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, MIDDLE_RING - 100, 188, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, MIDDLE_RING - 150, 256, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, MIDDLE_RING - 150, 120f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, MIDDLE_RING + 150, 160f);
        SectorEntityToken middle_ring = system.addTerrain(Terrain.RING, new RingParams(666, MIDDLE_RING - 100, null, "Arrad Ring"));
        middle_ring.setCircularOrbit(star, 0, 0, 100);

        // Yakchal is rich in water ice, needed for starship fuel
        PlanetAPI mirageIV = system.addPlanet("mirageIV", star, "Yakchal", "ice_giant", 240, 600, YAKCHAL_ORBIT, 778);
        PlanetAPI mirageIVB = system.addPlanet("mirageIVB", mirageIV, "Jalid", "frozen3", 60, 60, 1352, 28);
        Misc.initConditionMarket(mirageIVB);
        mirageIVB.getMarket().addCondition(Conditions.POOR_LIGHT);
        PlanetAPI mirageIVD = system.addPlanet("mirageIVD", mirageIV, "Barda", "cryovolcanic", 220, 80, 2192, 36);
        Misc.initConditionMarket(mirageIVD);
        mirageIVD.getMarket().addCondition(Conditions.POOR_LIGHT);
        mirageIV.setFaction("tritachyon");
        mirageIV.setInteractionImage("illustrations", "vacuum_colony");
        mirageIV.setCustomDescriptionId("vayra_yakchal");

        MarketAPI mirageIVmarket = addMarketplace("tritachyon", mirageIV, null,
                "Yakchal", // name of the market
                4, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_conditions ids
                                "outpost",
                                "ice",
                                "cold",
                                "volatiles_abundant",
                                "vayra_ice_fuel",
                                "poor_light",
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.FUELPROD,
                                Industries.MINING,
                                Industries.WAYSTATION,
                                "high_gravity")),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                true); // freeport

        system.addRingBand(mirageIV, "misc", "rings_dust0", 1024f, 3, Color.white, 1024f, 1375, 1150f, Terrain.RING, "Yakchal's Embrace");
        system.addRingBand(mirageIV, "misc", "rings_dust0", 256f, 3, Color.yellow, 256f, 800, 70f);
        system.addRingBand(mirageIV, "misc", "rings_dust0", 256f, 3, Color.yellow, 256f, 850, 90f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 3, Color.white, 256f, 900, 110f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 0, Color.cyan, 256f, 1200, 50f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 0, Color.white, 256f, 1150, 50f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 1, Color.blue, 256f, 1200, 70f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 0, Color.cyan, 256f, 1200, 80f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 0, Color.white, 256f, 1250, 90f);
        system.addRingBand(mirageIV, "misc", "rings_dust0", 256f, 3, Color.yellow, 256f, 1500, 70f);
        system.addRingBand(mirageIV, "misc", "rings_dust0", 256f, 3, Color.yellow, 256f, 1550, 90f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 3, Color.white, 256f, 1600, 110f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 0, Color.cyan, 256f, 1900, 50f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 0, Color.white, 256f, 1850, 50f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 1, Color.blue, 256f, 1900, 70f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 0, Color.cyan, 256f, 1900, 80f);
        system.addRingBand(mirageIV, "misc", "rings_ice0", 256f, 0, Color.white, 256f, 1950, 90f);

        // Yakchal L4 and L5 trojans and dead gate
        SectorEntityToken gate1 = system.addCustomEntity("vayra_mirage_gate", // unique id
                "Victory Gate", // name - if null, defaultName from custom_entities.json will be used
                "inactive_gate", // type of object, defined in custom_entities.json
                null); // faction
        gate1.setCircularOrbit(star, 240 - 60, YAKCHAL_ORBIT, 778);
        DebrisFieldParams params2 = new DebrisFieldParams(
                420f, // field radius - should not go above 1000 for performance reasons
                1f, // density, visual - affects number of debris pieces
                66642069f, // duration in days 
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldSource.MIXED;
        params.baseSalvageXP = 500; // base XP for scavenging in field
        SectorEntityToken debris2 = Misc.addDebrisField(system, params2, StarSystemGenerator.random);
        SalvageSpecialAssigner.assignSpecialForDebrisField(debris2);
        debris2.setSensorProfile(null);
        debris2.setDiscoverable(null);
        debris2.setCircularOrbit(star, 240 - 60, YAKCHAL_ORBIT, 778);

        SectorEntityToken m4L4 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius 
                        16f, // max asteroid radius
                        "The Worshippers")); // null for default name	
        m4L4.setCircularOrbit(star, 240 - 60, YAKCHAL_ORBIT, 778);

        SectorEntityToken m4L5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius 
                        16f, // max asteroid radius
                        "The Acolytes")); // null for default name
        m4L5.setCircularOrbit(star, 240 + 60, YAKCHAL_ORBIT, 778);

        // the Revenant Gestalt, the derelict remains of the Kadur's great fleet centered around the burned-out husk of a Caliph-class dreadnought 
        // home to millions! of refugees, those who managed to escape Oasis before the occupying troops landed
        // need to sprite some garbage for this, later
        WeightedRandomPicker<String> gestaltNames = new WeightedRandomPicker<>();
        gestaltNames.add("the Revenant Gestalt", 10000f);
        gestaltNames.add("the Reverend Gestalt", 100f);
        gestaltNames.add("the Revetment Gestalt", 100f);
        gestaltNames.add("Coldsteel the Hedgehog", 1f);
        String gestaltName = gestaltNames.pick();

        SectorEntityToken vayra_kadur_revenant = system.addCustomEntity("vayra_kadur_revenant", gestaltName, "station_side06", KADUR_ID);
        vayra_kadur_revenant.setInteractionImage("illustrations", "vayra_revenant_gestalt_caliph");
        vayra_kadur_revenant.setCircularOrbitPointingDown(star, 240 + 180, YAKCHAL_ORBIT, 778);
        vayra_kadur_revenant.setCustomDescriptionId("vayra_kadur_revenant");

        MarketAPI vayra_kadur_revenantmarket = addMarketplace(KADUR_ID, vayra_kadur_revenant, null,
                gestaltName, // name of the market
                5, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_condition ids and industry ids
                                "vayra_space_wreck",
                                "vayra_kadur_refugees",
                                "vayra_kadur_majority",
                                "kadur_hardened_populace",
                                "no_atmosphere",
                                Industries.WAYSTATION,
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.PATROLHQ,
                                Industries.HEAVYINDUSTRY,
                                Industries.LIGHTINDUSTRY,
                                "dark",
                                "vayra_qamarheadquarters")),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        vayra_kadur_revenantmarket.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo().addSpecial(new SpecialItemData("kadur_support_package", null), 1f);

        SectorEntityToken revHyena = addDerelictShip(system, "vayra_hyena_rod", ShipRecoverySpecial.ShipCondition.AVERAGE, true);
        revHyena.setCircularOrbit(vayra_kadur_revenant, 69, 240, 69f);

        SectorEntityToken revSirocco = addDerelictShip(system, "vayra_sirocco_logistics", ShipRecoverySpecial.ShipCondition.BATTERED, (Math.random() > 0.5f));
        revSirocco.setCircularOrbit(vayra_kadur_revenant, 150, 160, 69f);

        SectorEntityToken revRukh = addDerelictShip(system, "vayra_rukh_standard", ShipRecoverySpecial.ShipCondition.WRECKED, false);
        revRukh.setCircularOrbit(vayra_kadur_revenant, 303, 303, 69f);

        system.autogenerateHyperspaceJumpPoints(true, true);

        // An external belt just looks helpful
        system.addAsteroidBelt(star, 450, OUTER_RING - 100, 666, 800, 900, Terrain.ASTEROID_BELT, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, OUTER_RING - 50, 900f, null, null);
        system.addAsteroidBelt(star, 100, OUTER_RING, 256, 800, 900, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, OUTER_RING - 100, 188, 800, 900, Terrain.ASTEROID_BELT, null);
        SectorEntityToken outer_ring = system.addTerrain(Terrain.RING, new RingParams(666, OUTER_RING - 100, null, "The Zabaniyya"));
        outer_ring.setCircularOrbit(star, 0, 0, 750);

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
}
