package data.scripts.campaign.bases;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.VayraModPlugin.RAIDER_BASE_REAPER_ENABLED;
import static data.scripts.campaign.bases.VayraRaiderBaseManager.RAIDERS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class VayraRaiderBaseReaper implements EveryFrameScript {

    public static Logger log = Global.getLogger(VayraRaiderBaseReaper.class);

    protected static float checkInterval = 1f;
    protected float timeSinceLastCheck = 0f;
    private final List<String> deadFactions = new ArrayList<>();
    private final Map<String, Integer> nonHiddenMarketCounts = new HashMap<>();  // String factionId, int count
    private final Map<String, List<MarketAPI>> storedBases = new HashMap<>(); // String factionId, List<MarketAPI> bases

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (!RAIDER_BASE_REAPER_ENABLED) {
            return;
        }

        if (!deadFactions.isEmpty()) {
            for (String factionId : deadFactions) {

                if (RAIDERS.keySet().contains(factionId)) {
                    List<VayraRaiderBaseIntel> vayraIntel = (List) Global.getSector().getIntelManager().getIntel(VayraRaiderBaseIntel.class);
                    for (VayraRaiderBaseIntel intel : vayraIntel) {
                        if (intel.getData().raiderFactionId.equals(factionId) && !storedBases.get(factionId).contains(intel.getMarket())) {
                            Misc.fadeAndExpire(intel.getEntity());
                            intel.endImmediately();
                            log.info(String.format("%s are dead, killing a non-preexisting raider base.", factionId));
                        }
                    }
                }

                if (factionId.equals(Factions.LUDDIC_PATH)) {
                    List<LuddicPathBaseIntel> patherIntel = (List) Global.getSector().getIntelManager().getIntel(LuddicPathBaseIntel.class);
                    for (LuddicPathBaseIntel intel : patherIntel) {
                        if (!storedBases.get(factionId).contains(intel.getMarket())) {
                            Misc.fadeAndExpire(intel.getEntity());
                            intel.endImmediately();
                            log.info(String.format("Path are on deadlist: Killing a non-preexisting Pather raider base."));
                        }
                    }
                }

                if (factionId.equals(Factions.PIRATES)) {
                    List<PirateBaseIntel> pirateIntel = (List) Global.getSector().getIntelManager().getIntel(PirateBaseIntel.class);
                    for (PirateBaseIntel intel : pirateIntel) {
                        if (!storedBases.get(factionId).contains(intel.getMarket())) {
                            Misc.fadeAndExpire(intel.getEntity());
                            intel.endImmediately();
                            log.info(String.format("Pirates are on deadlist: Killing a non-preexisting Pirate raider base."));
                        }
                    }
                }
            }
        }

        float days = Global.getSector().getClock().convertToDays(amount);
        timeSinceLastCheck += days;

        if (timeSinceLastCheck >= checkInterval) {
            timeSinceLastCheck = 0;

            for (String id : RAIDERS.keySet()) {
                VayraRaiderBaseManager.RaiderData data = RAIDERS.get(id);
                if (data.onlySpawnWhenVisibleInIntelTab) {
                    nonHiddenMarketCounts.put(id, 0);
                } else {
                    nonHiddenMarketCounts.put(id, 1); // if set to do so, always count faction as possessing a market for raider base spawning purposes
                }
            }
            nonHiddenMarketCounts.put(Factions.LUDDIC_PATH, 0);
            nonHiddenMarketCounts.put(Factions.PIRATES, 0);
            
            List<MarketAPI> hiddenMarkets = new ArrayList<>();

            for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {

                if (market.isPlanetConditionMarketOnly()) {
                    continue;
                }
                if (market.isHidden()) {
                    hiddenMarkets.add(market);
                    continue;
                }

                if (nonHiddenMarketCounts.containsKey(market.getFactionId())) {
                    int count = nonHiddenMarketCounts.get(market.getFactionId());
                    count += 1;
                    nonHiddenMarketCounts.put(market.getFactionId(), count);
                }
            }
            
            for (String id : RAIDERS.keySet()) {
                if (!deadFactions.contains(id) && nonHiddenMarketCounts.get(id) <= 0) {
                    deadFactions.add(id);
                    List<MarketAPI> existingBases = new ArrayList<>();
                    for (MarketAPI market : hiddenMarkets) {
                        if (market.getFactionId().equals(id)) {
                            existingBases.add(market);
                        }
                    }
                    storedBases.put(id, existingBases);
                    log.info(String.format("Putting %s on the deadlist, storing all their extant hidden markets.", id));
                } else if (deadFactions.contains(id) && nonHiddenMarketCounts.get(id) > 0) {
                    deadFactions.remove(id);
                    log.info(String.format("%s off the deadlist. Death to tyrants and kings.", id));
                }
            }

            if (!deadFactions.contains(Factions.LUDDIC_PATH) && nonHiddenMarketCounts.get(Factions.LUDDIC_PATH) <= 0) {
                deadFactions.add(Factions.LUDDIC_PATH);
                List<MarketAPI> existingBases = new ArrayList<>();
                for (MarketAPI market : hiddenMarkets) {
                    if (market.getFactionId().equals(Factions.LUDDIC_PATH)) {
                        existingBases.add(market);
                    }
                }
                storedBases.put(Factions.LUDDIC_PATH, existingBases);
                log.info(String.format("Putting the Luddic Path on the deadlist, storing all their extant hidden markets."));
            } else if (deadFactions.contains(Factions.LUDDIC_PATH) && nonHiddenMarketCounts.get(Factions.LUDDIC_PATH) > 0) {
                deadFactions.remove(Factions.LUDDIC_PATH);
                log.info(String.format("Luddic Path off the deadlist. Death to the faithless."));
            }

            if (!deadFactions.contains(Factions.PIRATES) && nonHiddenMarketCounts.get(Factions.PIRATES) <= 0) {
                deadFactions.add(Factions.PIRATES);
                List<MarketAPI> existingBases = new ArrayList<>();
                for (MarketAPI market : hiddenMarkets) {
                    if (market.getFactionId().equals(Factions.PIRATES)) {
                        existingBases.add(market);
                    }
                }
                storedBases.put(Factions.PIRATES, existingBases);
                log.info(String.format("Putting pirates on the deadlist, storing all their extant hidden markets."));
            } else if (deadFactions.contains(Factions.PIRATES) && nonHiddenMarketCounts.get(Factions.PIRATES) > 0) {
                deadFactions.remove(Factions.PIRATES);
                log.info(String.format("Pirates off the deadlist. We comin for that toothbrush also yt."));
            }
        }
    }
}
