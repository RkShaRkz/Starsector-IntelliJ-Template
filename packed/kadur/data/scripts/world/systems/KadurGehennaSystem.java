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
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin.CoronaParams;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.KadurModPlugin.KADUR_ID;
import static data.scripts.KadurModPlugin.addAccretionDisk;
import static data.scripts.KadurModPlugin.addMarketplace;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class KadurGehennaSystem implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {

        boolean historical = Global.getSector().getClock().getCycle() < 196;

        StarSystemAPI system = sector.createStarSystem("Gehenna");
        system.getLocation().set(1800, -21700);
        system.setBackgroundTextureFilename("graphics/backgrounds/kadur_background5_thanks_tartiflette.jpg");

        // coords go in starmap.json now apparently
        // this was where Mirage was originally
        // system.getLocation().set(-2623, 10823);
        // this was where Oasis was originally
        // system.getLocation().set(7121, 7723);
        // this was where Kadur was originally
        // system.getLocation().set(1332, 14495);
        // primary star (j/k its a black hole)
        PlanetAPI star = system.initStar("vayra_gehenna_star", StarTypes.BLACK_HOLE, 200f, 0);
        star.getSpec().setBlackHole(true);
        system.setLightColor(new Color(135, 110, 110));
        // this does NOT generate an event horizon... lol
        // guess i have to make one myself
        StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(star);
        if (coronaPlugin != null) {
            system.removeEntity(coronaPlugin.getEntity());
        }
        StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), false);
        float corona = (float) (star.getRadius() * (starData.getCoronaMult() + starData.getCoronaVar() * (Math.random() - 0.5f)));
        if (corona < starData.getCoronaMin()) {
            corona = starData.getCoronaMin();
        }
        SectorEntityToken eventHorizon = system.addTerrain(Terrain.EVENT_HORIZON,
                new CoronaParams(
                        star.getRadius() + corona,
                        (star.getRadius() + corona) / 2f,
                        star,
                        starData.getSolarWind(),
                        0f,
                        Math.max(starData.getCrLossMult(), 25f)));
        eventHorizon.setCircularOrbit(star, 0, 0, 100);

        // accretion disk
        addAccretionDisk(star, "River of Souls");

        // just-substellar gas giant ignited by accretion disk or something; either way its on fire and angry red
        // PlanetAPI gehennaI = system.addPlanet("gehennaI", star, "Angra Mainyu", "star_browndwarf", 0, 307, 1312, 66);
        PlanetAPI gehennaI = system.addPlanet("gehennaI", star, "Angra Mainyu", "gas_giant", 0, 307, 2000, 66);
        gehennaI.getSpec().setPlanetColor(new Color(255, 16, 16, 255));
        gehennaI.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
        gehennaI.getSpec().setGlowColor(new Color(8, 38, 235, 195));
        gehennaI.getSpec().setUseReverseLightForGlow(true);
        gehennaI.getSpec().setAtmosphereThickness(0.5f);
        gehennaI.getSpec().setCloudRotation(15f);
        gehennaI.getSpec().setAtmosphereColor(new Color(255, 118, 138, 245));
        gehennaI.applySpecChanges();
        gehennaI.setCustomDescriptionId("vayra_gehenna_angra_mainyu");
        Misc.initConditionMarket(gehennaI);
        gehennaI.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
        gehennaI.getMarket().addCondition(Conditions.EXTREME_WEATHER);
        gehennaI.getMarket().addCondition(Conditions.VERY_HOT);
        gehennaI.getMarket().addCondition(Conditions.HIGH_GRAVITY);
        gehennaI.getMarket().addCondition(Conditions.POOR_LIGHT);
        gehennaI.getMarket().addCondition(Conditions.IRRADIATED);
        gehennaI.getMarket().addCondition(Conditions.VOLATILES_PLENTIFUL);

        system.addCorona(gehennaI, Terrain.CORONA_AKA_MAINYU,
                400f, // radius outside planet
                5f, // burn level of "wind"
                1f, // flare probability
                1.5f // CR loss mult while in it
        );

        SectorEntityToken vayra_angramainyu_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldTerrainPlugin.MagneticFieldParams(gehennaI.getRadius() + 200f, // terrain effect band width 
                        (gehennaI.getRadius() + 200f) / 2f, // terrain effect middle radius
                        gehennaI, // entity that it's around
                        gehennaI.getRadius() + 50f, // visual band start
                        gehennaI.getRadius() + 50f + 350f, // visual band end
                        new Color(66, 20, 100, 40), // base color
                        1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(140, 100, 235),
                        new Color(180, 110, 210),
                        new Color(150, 140, 190),
                        new Color(140, 190, 210),
                        new Color(90, 200, 170),
                        new Color(65, 230, 160),
                        new Color(20, 220, 70)
                ));
        vayra_angramainyu_field.setCircularOrbit(gehennaI, 0, 0, 24);

        // perdition station, a hellhole built for and by death-seeking psychopaths
        // in Angra Mainyu's L3, shielded from it (does that make sense? maybe not lol) by the event horizon
        SectorEntityToken vayra_gehenna_perdition = system.addCustomEntity("vayra_gehenna_perdition", "Perdition Outpost", "station_pirate_type", "pirates");
        vayra_gehenna_perdition.setInteractionImage("illustrations", "facility_explosion");
        vayra_gehenna_perdition.setCircularOrbitPointingDown(star, 0 + 180, 1800, 66);
        vayra_gehenna_perdition.setCustomDescriptionId("vayra_gehenna_perdition");

        MarketAPI vayra_gehenna_perditionmarket = addMarketplace("pirates", vayra_gehenna_perdition, null,
                "Perdition Outpost", // name of the market
                3, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_conditions ids
                                "decivilized",
                                "meteor_impacts",
                                Conditions.ORE_MODERATE,
                                Conditions.RARE_ORE_SPARSE,
                                Industries.POPULATION,
                                Industries.MINING,
                                Industries.GROUNDDEFENSES,
                                Industries.SPACEPORT,
                                Industries.PATROLHQ,
                                Industries.WAYSTATION,
                                Industries.ORBITALSTATION,
                                "poor_light")),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                true); // freeport

        // inner system jump point  (initial position in degrees, distance in pixels, orbit speed in days)
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("gehenna_inner_jump", "Road to Perdition");
        jumpPoint.setCircularOrbit(star, 0 - 60, 2000, 66);
        jumpPoint.setRelatedPlanet(gehennaI);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        // big asteroid belt
        system.addAsteroidBelt(star, 450, 3000, 500, 100, 200, Terrain.ASTEROID_BELT, "The Damned");
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 3000 - 200f, 305f, null, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 3000 + 150f, 125f, null, null);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 3, Color.white, 256f, 3000 - 0f, 225f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 3, Color.white, 256f, 3000 + 0f, 295f, null, null);

        // Melek Taus, the peacock angel
        PlanetAPI gehennaII = system.addPlanet("gehennaII", star, "Melek Taus", "vayra_bread", 45, 120, 4800, 99);
        Misc.initConditionMarket(gehennaII);
        gehennaII.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
        gehennaII.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
        gehennaII.getMarket().addCondition(Conditions.EXTREME_WEATHER);
        gehennaII.getMarket().addCondition(Conditions.VERY_HOT);
        gehennaII.getMarket().addCondition(Conditions.DARK);
        gehennaII.getMarket().addCondition(Conditions.METEOR_IMPACTS);
        gehennaII.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);
        gehennaII.getMarket().addCondition(Conditions.IRRADIATED);
        gehennaII.getMarket().addCondition(Conditions.RARE_ORE_ULTRARICH);
        gehennaII.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);

        SectorEntityToken vayra_gehennaIImagfield = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldTerrainPlugin.MagneticFieldParams(gehennaII.getRadius() + 200f, // terrain effect band width 
                        (gehennaII.getRadius() + 200f) / 2f, // terrain effect middle radius
                        gehennaII, // entity that it's around
                        gehennaII.getRadius() + 50f, // visual band start
                        gehennaII.getRadius() + 50f + 250f, // visual band end
                        new Color(138, 43, 226, 40), // base color
                        0.8f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(140, 100, 235),
                        new Color(180, 110, 210),
                        new Color(150, 140, 190),
                        new Color(140, 190, 210),
                        new Color(90, 200, 170),
                        new Color(65, 230, 160),
                        new Color(20, 220, 70)
                ));
        vayra_gehennaIImagfield.setCircularOrbit(gehennaII, 0, 0, 100);

        if (!historical) {
            generateRequiem(gehennaII, system);
        }

        // big external ring/asteroid system with Tortuga hidden away in it
        system.addAsteroidBelt(star, 450, 7666, 500, 100, 200, Terrain.ASTEROID_BELT, "The Stygians");
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 6666 - 200f, 99f, null, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 6666 + 250f, 130f, null, null);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 1, Color.white, 256f, 6666 - 0f, 220f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 2, Color.white, 256f, 6666 + 0f, 320f, null, null);

        // tortuga, haven for pirates
        SectorEntityToken vayra_gehenna_tortuga = system.addCustomEntity("vayra_gehenna_tortuga", "Tortuga Station", "station_sporeship_derelict", "pirates");
        vayra_gehenna_tortuga.setInteractionImage("illustrations", "urban01");
        vayra_gehenna_tortuga.setCustomDescriptionId("vayra_gehenna_tortuga");
        vayra_gehenna_tortuga.setCircularOrbitPointingDown(star, 222, 7666, 365);

        MarketAPI vayra_gehenna_tortugamarket = addMarketplace("pirates", vayra_gehenna_tortuga, null,
                "Tortuga Station", // name of the market
                6, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_conditions ids
                                "organized_crime",
                                "dissident",
                                "trade_center",
                                "headquarters",
                                Industries.POPULATION,
                                Industries.REFINING,
                                Industries.MEGAPORT,
                                Industries.HEAVYBATTERIES,
                                Industries.LIGHTINDUSTRY,
                                Industries.ORBITALWORKS,
                                Industries.MILITARYBASE,
                                Industries.WAYSTATION,
                                Industries.STARFORTRESS,
                                "dark")),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                true); // freeport

        // fuck nebulas but he3re's one anyway hope it doesn't put one over the black hole or soemthing dumbb
        StarSystemGenerator.addSystemwideNebula(system, StarAge.OLD);

        system.autogenerateHyperspaceJumpPoints(true, true, true);

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 2.0f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

    }

    private SectorEntityToken generateRequiem(PlanetAPI focus, StarSystemAPI system) {        // the Kadur are reduced to hiding in their heavily-guarded star fortress, towed here through hyperspace following the final battle
        // and plotting their retribution...
        SectorEntityToken vayra_refugestation = system.addCustomEntity("vayra_refugestation", "Star Fortress Requiem", "station_side06", KADUR_ID);
        vayra_refugestation.setInteractionImage("illustrations", "vayra_requiem");
        vayra_refugestation.setCustomDescriptionId("vayra_requiem");
        vayra_refugestation.setCircularOrbitPointingDown(focus, 0, 220, 70);

        MarketAPI vayra_refugestationmarket = addMarketplace(KADUR_ID, vayra_refugestation, null,
                "Star Fortress Requiem", // name of the market
                5, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_condition and/or industry ids
                                "headquarters",
                                "stealth_minefields",
                                "vayra_kadur_refugees",
                                "vayra_kadur_majority",
                                "kadur_hardened_populace",
                                "hydroponics_complex",
                                Conditions.FARMLAND_ADEQUATE, // domain-era hydroponics
                                Conditions.DARK,
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.HEAVYBATTERIES,
                                Industries.MILITARYBASE,
                                Industries.WAYSTATION,
                                Industries.FARMING,
                                Industries.STARFORTRESS_MID,
                                "vayra_kadurholyfleet")),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        // can't figure out how to add items inside my addmarketplace, too complicated, just brute force it separately
        vayra_refugestationmarket.addIndustry(Industries.ORBITALWORKS, new ArrayList<>(Arrays.asList(Items.PRISTINE_NANOFORGE)));

        // blueprints added to military market       
        vayra_refugestationmarket.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo().addSpecial(new SpecialItemData("kadur_missile_package", null), 1f);
        vayra_refugestationmarket.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo().addSpecial(new SpecialItemData("kadur_rail_package", null), 1f);
        vayra_refugestationmarket.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo().addSpecial(new SpecialItemData("kadur_fission_package", null), 1f);

        return vayra_refugestation;
    }
}
