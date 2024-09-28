package data.scripts.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.campaign.colonies.VayraColonialManager;
import data.util.LoggerLogLevel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static data.scripts.VayraMergedModPlugin.*;
import static data.scripts.util.MiscUtils.log;

public class VayraPopularFrontManager implements EveryFrameScript, ColonyPlayerHostileActListener {

    public static Logger log = Global.getLogger(VayraPopularFrontManager.class);

    public static final String JOINT_FACTION = "communist_clouds";

    public static final List<String> POSSIBLE_ALLIES = new ArrayList<>(Arrays.asList(
            "communist_clouds",
            "shadow_industry",
            "dassault_mikoyan",
            "kadur_remnant",
            "junk_pirates",
            "pack",
            "air"));

    private final IntervalUtil timer = new IntervalUtil(30f, 60f);
    public MarketAPI interstellarStation = null;

    public static final String STATION_ID = "interstellar_station";

    public static int POPULAR_FRONT_STAGE = 0;

    public static List<FactionAPI> ALLIES = new ArrayList<>();
    public static List<FactionAPI> ENEMIES = new ArrayList<>();

    public static boolean LOG_TO_CONSOLE = false;

    public static final AtomicBoolean FORCE_SPAWN = new AtomicBoolean(false);

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (FORCE_SPAWN.get()) {
            if (interstellarStation == null) {
                log(LoggerLogLevel.INFO, log, "[FORCE_SPAWN] didn't find interstellaire, force creating one");
                makeStation();
            } else {
                log(LoggerLogLevel.INFO, log, "[FORCE_SPAWN] interstellaire exists, not doing anything");
            }
        }

        if (!POPULAR_FRONT_ENABLED) {
            return;
        }

        if (ALLIES.isEmpty()) {
            init();
        }
        if (ALLIES.isEmpty()) {
            return;
        }

        if (Global.getSector().isPaused()) {
            return;
        }

        // add ourselves as a listener to the sector listener manager
        ListenerManagerAPI listenerManager = Global.getSector().getListenerManager();
        if (listenerManager != null && !listenerManager.hasListener(this)) {
            listenerManager.addListener(this);
        }

        float days = Global.getSector().getClock().convertToDays(amount);
        if (VAYRA_DEBUG) {
            days *= 30f;
        }
        timer.advance(days);
        if (timer.intervalElapsed()) {
            checkRelations();

            if (interstellarStation == null) {
                SectorEntityToken test = Global.getSector().getEntityById(STATION_ID);
                if (test != null && test.getMarket() != null) {
                    interstellarStation = test.getMarket();
                    log(LoggerLogLevel.INFO, log, "found preexisting interstellaire, setting existing to this one");
                } else if (VAYRA_DEBUG || Global.getSector().getClock().getCycle() >= POPULAR_FRONT_TIMEOUT) {
                    log(LoggerLogLevel.INFO, log, "didn't find interstellaire, creating one");
                    makeStation();
                }
            } else {
                log(LoggerLogLevel.INFO, log, "progressing existing interstellaire");
                POPULAR_FRONT_STAGE++;
                VayraColonialManager manager = VayraColonialManager.getInstance();
                if (manager != null) {
                    manager.pickNextUpgrade(interstellarStation);
                }
            }
        }
    }

    private void init() {
        for (String id : POSSIBLE_ALLIES) {
            FactionAPI faction = Global.getSector().getFaction(id);
            if (faction != null && !ALLIES.contains(faction)) {
                ALLIES.add(faction);
                // other inclusions/exclusions should take care of themselves since the factions won't exist at all if the mods are disabled
                // this would be a real lonely popular front if you don't have Kadur, JP, SRA, or DME active
                log(LoggerLogLevel.INFO, log, "added " + id + " to list of socialist allies");
            }
        }
    }

    private void checkRelations() {
        for (FactionAPI ally : ALLIES) {
            ally.ensureAtWorst(JOINT_FACTION, RepLevel.NEUTRAL);
        }
        for (FactionAPI enemy : ENEMIES) {
            enemy.ensureAtBest(JOINT_FACTION, RepLevel.HOSTILE);
        }
    }

    private void makeStation() {
        log(LoggerLogLevel.INFO, log, "--> makeStation()\tinterstellarStation: "+interstellarStation);
        // new generation method (piggyback off colony shit):
        VayraColonialManager manager = VayraColonialManager.getInstance();
        if (manager == null) {
            log(LoggerLogLevel.ERROR, log, "can't find VayraColonialManager");
            return;
        }
        FactionAPI faction = Global.getSector().getFaction(JOINT_FACTION);
        if (faction == null) {
            log(LoggerLogLevel.ERROR, log, "can't find communist_clouds");
            return;
        }
        MarketAPI target = manager.pickTarget(manager.pickSource(faction), faction);
        if (target == null) {
            log(LoggerLogLevel.ERROR, log, "can't find a place to put l'interstellaire");
            return;
        }
        LocationAPI loc = target.getContainingLocation();
        if (loc == null) {
            log(LoggerLogLevel.ERROR, log, "can't find the place that the place to put l'interstellaire is in");
            return;
        }
        SectorEntityToken newInterstellarStation = loc.addCustomEntity(STATION_ID, "L'Interstellaire", "station_side07", JOINT_FACTION);
        if (newInterstellarStation == null) {
            log(LoggerLogLevel.ERROR, log, "can't find l'interstellaire after making it");
            return;
        }
        SectorEntityToken entity = target.getPrimaryEntity();
        if (entity == null) {
            log(LoggerLogLevel.ERROR, log, "can't find the thing l'interstellaire is supposed to orbit, eat shit");
            return;
        }
        float orbitDist = 333f;
        newInterstellarStation.setCircularOrbitWithSpin(entity, 0, entity.getRadius() + orbitDist, 265, 25, 100);

        WeightedRandomPicker<String> stations = new WeightedRandomPicker<>();
        stations.add(Industries.ORBITALSTATION);
        stations.add(Industries.ORBITALSTATION_MID);
        if (Global.getSettings().getModManager().isModEnabled("shadow_ships")) {
            stations.add("ms_orbitalstation");
        }
        if (Global.getSettings().getModManager().isModEnabled("istl_dam")) {
            stations.add(Industries.ORBITALSTATION_HIGH);
        }
        String station = stations.pick();

        this.interstellarStation = addMarketplace(JOINT_FACTION, newInterstellarStation, null,
                "L'Interstellaire", // name of the market
                3, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_condition and/or industry ids
                                "frontier",
                                "headquarters",
                                "stealth_minefields",
                                "hydroponics_complex",
                                Conditions.FARMLAND_ADEQUATE,
                                Industries.FARMING,
                                "vayra_popular_front",
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.GROUNDDEFENSES,
                                Industries.PATROLHQ,
                                Industries.WAYSTATION,
                                station
                        )
                ),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE
                        )
                ),
                true, // with junk and chatter?
                true, // pirate mode? (i.e. hidden)
                true  // free port
        );

        if (FORCE_SPAWN.get()) {
            // Clear the force-spawn flag to avoid recreating it multiple times
            FORCE_SPAWN.compareAndSet(true, false);
        }

        log(LoggerLogLevel.INFO, log, "<-- makeStation()\tinterstellarStation: "+interstellarStation);
    }

    @Override
    public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, CargoAPI cargo) {
    }

    @Override
    public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, Industry industry) {
    }

    @Override
    public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
    }

    // this method removes the NO_DECIV_KEY memory key from the market if it was a hidden market and got sat-bombed
    // and also manually decivilizes it if it woulda been decivilized
    @Override
    public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        if (market != null && market == interstellarStation) {
            market.getMemoryWithoutUpdate().unset(DecivTracker.NO_DECIV_KEY);
            market.getPrimaryEntity().setDiscoverable(false);
            if (market.getSize() <= 4 && market.getStabilityValue() < 1f) {
                DecivTracker.decivilize(market, false);
            }
            if (Misc.getFactionMarkets(market.getFaction()).size() <= 0 && market.getFaction().isShowInIntelTab()) {
                market.getFaction().setShowInIntelTab(false);
            }
        }
    }
}
