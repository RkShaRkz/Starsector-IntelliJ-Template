package data.scripts.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.AutoDespawnScript;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.campaign.intel.VayraPersonBountyManager.RareBountyFlagshipData;
import data.scripts.campaign.intel.VayraPlayerBountyIntel.PlayerBountyData;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import java.util.Random;

import static data.scripts.VayraMergedModPlugin.*;
import static data.scripts.campaign.intel.VayraPersonBountyIntel.getPersonBountyEventDataFromRepository;

public class VayraPlayerBountyHunter implements EveryFrameScript {

    public static Logger log = Global.getLogger(VayraPlayerBountyHunter.class);

    protected long seed;
    protected VayraPlayerBountyIntel intel;
    protected FactionAPI faction = null;
    protected FactionAPI postedByFaction = null;

    public VayraPlayerBountyHunter(VayraPlayerBountyIntel source) {
        seed = Misc.genRandomSeed();
        intel = source;
        faction = pickFaction(source);
        if (faction == null) {
            try {
                faction = source.bountiesPosted.values().iterator().next().postedByFaction;
            } catch (NullPointerException npx) {
                log.error("couldn't get bounty hunter faction, or intel, fml");
            }
        } else {
            log.info("hello i am here, gonna try to track down the player");
        }
    }

    protected boolean sentFleet = false;
    protected StarSystemAPI systemPlayerIsIn = null;
    protected float daysInSystem = 0f;
    protected float daysOutOfRange = 0f;

    @Override
    public void advance(float amount) {
        if (sentFleet) {
            return;
        }

        if (intel == null) {
            log.error("oops, no VayraPlayerBountyIntel, how'd that happen? aborting");
            sentFleet = true;
            return;
        }

        if (faction == null) {
            faction = pickFaction(intel);
            if (faction == null) {
                log.error("couldn't pick a bounty hunter faction, at all, aborting");
                sentFleet = true;
                return;
            }
        }

        if (!intel.hasBounty()) {
            log.error("no current bounties on player, aborting (usually means bounty expired)");
            sentFleet = true;
            return;
        }

        float days = Misc.getDays(amount);

        final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        float distFromCore = playerFleet.getLocationInHyperspace().length();
        if (distFromCore > (PLAYER_BOUNTY_MAX_RANGE * Math.max(intel.timerMultBecauseOfCrimes, 0.5f))) {
            daysInSystem = 0f;
            daysOutOfRange += days;
            if (daysOutOfRange >= PLAYER_BOUNTY_RANGE_DAYS) {
                sentFleet = true;
                log.info("player was in hiding for long enough to lose us, aborting");
            }
            systemPlayerIsIn = null;
            return;
        }

        if (!(playerFleet.getContainingLocation() instanceof StarSystemAPI)) {
            if ((daysInSystem > PLAYER_BOUNTY_SYSTEM_DAYS || VAYRA_DEBUG) && systemPlayerIsIn != null) {
                float dist = Misc.getDistance(systemPlayerIsIn.getLocation(), playerFleet.getLocationInHyperspace());
                if (dist < PLAYER_BOUNTY_SPAWN_RANGE) {
                    if (intel != null) {
                        sendFleet(intel);
                    } else {
                        log.error("can't find intel for vayraPlayerBountyHunter, aborting");
                        sentFleet = true;
                        return;
                    }
                }
            }
            daysInSystem = 0f;
            systemPlayerIsIn = null;
            return;
        }

        systemPlayerIsIn = (StarSystemAPI) playerFleet.getContainingLocation();
        daysInSystem += days;
    }

    protected void sendFleet(VayraPlayerBountyIntel source) {
        if (sentFleet) {
            return;
        }
        sentFleet = true;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        CampaignFleetAPI hunter = createBountyHunter(source);
        if (hunter != null) {
            Global.getSector().getHyperspace().addEntity(hunter);
            Vector2f hunterLoc = Misc.getPointAtRadius(playerFleet.getLocationInHyperspace(), PLAYER_BOUNTY_SPAWN_RANGE);
            hunter.setLocation(hunterLoc.x, hunterLoc.y);

            hunter.getAI().addAssignmentAtStart(FleetAssignment.INTERCEPT, playerFleet, 1000f, null);

            Misc.giveStandardReturnToSourceAssignments(hunter, false);

            hunter.getMemoryWithoutUpdate().set("$vayra_playerBountyHunter", true);
        }
    }

    private FactionAPI pickFaction(VayraPlayerBountyIntel source) {
        WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<>();
        for (PlayerBountyData data : source.bountiesPosted.values()) {
            float value = data.value;
            picker.add(data.postedByFaction, value);
        }
        FactionAPI first = picker.pick();
        picker.clear();
        picker.add(first, 1f);
        postedByFaction = first;

        for (FactionAPI hunterFaction : Global.getSector().getAllFactions()) {
            if (getPersonBountyEventDataFromRepository().isParticipating(hunterFaction.getId()) && (hunterFaction.equals(first)
                    || (hunterFaction.getRelToPlayer().isAtBest(RepLevel.NEUTRAL) && hunterFaction.getRelationshipLevel(first).isAtWorst(RepLevel.NEUTRAL)))) {
                float size = 0f;
                for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
                    if (!market.isPlanetConditionMarketOnly() && !market.isHidden() && market.getFaction().equals(hunterFaction)) {
                        size += market.getSize();
                    }
                }
                float value = size;
                float repMult = 0f - hunterFaction.getRelToPlayer().getRel();
                value *= repMult;
                if (hunterFaction.equals(first)) {
                    value *= 1.5f;
                }
                picker.add(hunterFaction, value);
            }
        }
        FactionAPI last = picker.pick();

        if (first == null || last == null) {
            log.error("posting faction selected was " + first + " and hunting faction selected was " + last + ", returning null");
            return null;
        }

        return last;
    }

    private float pickFleetPoints() {
        try {
            float fp = Global.getSector().getPlayerFleet().getFleetPoints();
            log.info("player fleet has " + fp + " fleet points");
            log.info("PLAYER_BOUNTY_FP_SCALING is " + PLAYER_BOUNTY_FP_SCALING);
            fp = fp * PLAYER_BOUNTY_FP_SCALING;
            return fp;
        } catch (NullPointerException ex) {
            log.error("weird null, probably didn't have a VayraPlayerBountyIntel or the faction id was malformed");
            return 200f; // default same as vanilla bounty hunters i guess
        }
    }

    protected CampaignFleetAPI createBountyHunter(VayraPlayerBountyIntel source) {

        if (source == null) {
            log.error("VayraPlayerBountyIntel was null, aborting");
            return null;
        }

        float pts = pickFleetPoints();
        log.info("player fleet FP * FP scaling variable = " + pts + " base fleet points");
        pts *= 0.75f + ((float) Math.random() * 0.5f);
        log.info("rolled " + pts + " total FP");
        pts += 5f;
        log.info("added 5 more FP just because. fuck you");

        FleetParamsV3 params = new FleetParamsV3(
                null,
                Global.getSector().getPlayerFleet().getLocationInHyperspace(),
                faction.getId(),
                1f,
                FleetTypes.MERC_BOUNTY_HUNTER,
                pts * 0.9f, // combatPts
                0f, // freighterPts 
                pts * 0.1f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
        params.officerNumberBonus = (int) (pts / 50f);
        params.officerLevelBonus = (int) (pts / 20f);
        params.random = new Random(seed);
        params.ignoreMarketFleetSizeMult = true;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        FleetMemberAPI currFlag = fleet.getFlagship();
        float flagshipRoll = (float) Math.random();
        log.info("rolled for rare flagship: " + flagshipRoll);
        if (flagshipRoll <= (VAYRA_DEBUG ? 1f : RARE_BOUNTY_FLAGSHIP_CHANCE)) {
            RareBountyFlagshipData rareFlagship = VayraPersonBountyManager.pickRareFlagship(currFlag, faction);

            if (rareFlagship != null) {
                PersonAPI person = fleet.getCommander();
                currFlag.setFlagship(false);
                String flagVariant = rareFlagship.variantId;
                FactoryAPI factory = Global.getFactory();
                FleetMemberAPI newFlagship = factory.createFleetMember(FleetMemberType.SHIP, flagVariant);
                // not gonna fuck with IBB-style forced recovery, just give it the smart recovery hullmod
                newFlagship.getVariant().addMod("vayra_bounty_recoverable");
                fleet.getFleetData().addFleetMember(newFlagship);
                fleet.getFleetData().removeFleetMember(currFlag);
                fleet.getFleetData().setFlagship(newFlagship);
                newFlagship.setFlagship(true);
                newFlagship.setCaptain(person);
                fleet.setCommander(person);
                fleet.forceSync();
                newFlagship.getRepairTracker().setCR(newFlagship.getRepairTracker().getMaxCR());
                log.info(String.format("picked rare flagship %s for bounty hunter %s", flagVariant, person.getNameString()));
            } else {
                log.info("couldn't find a rare bounty hunter flagship RIP");
            }
        }

        Misc.makeLowRepImpact(fleet, "$vayra_playerBountyHunter");

        fleet.addScript(new AutoDespawnScript(fleet));

        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        memory.set("$vayra_playerBountyFaction", Misc.ucFirst(postedByFaction.getDisplayNameWithArticle()));

        log.info("spawned bounty hunter " + fleet.getNameWithFaction() + " with " + fleet.getFleetPoints() + " FP to hunt and kill the player for their crimes");

        return fleet;
    }

    @Override
    public boolean isDone() {
        return sentFleet;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
