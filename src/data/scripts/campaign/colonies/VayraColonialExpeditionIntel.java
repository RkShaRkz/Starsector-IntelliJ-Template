package data.scripts.campaign.colonies;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidDelegate;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Random;
import java.util.Set;

import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;
import static data.scripts.VayraMergedModPlugin.aOrAn;

public class VayraColonialExpeditionIntel extends RaidIntel implements RaidDelegate {

    public static Logger log = Global.getLogger(VayraColonialExpeditionIntel.class);

    public enum KadurColonialExpeditionOutcome {
        TARGET_ALREADY_COLONIZED,
        EXPEDITION_DESTROYED,
        COLONY_ESTABLISHED, // yeah boiiiiiiiiii
    }

    public static final Object MADE_HOSTILE_UPDATE = new Object();
    public static final Object ENTERED_SYSTEM_UPDATE = new Object();
    public static final Object OUTCOME_UPDATE = new Object();

    protected VayraColonialExpeditionStage4Colonize action;

    protected MarketAPI target;
    protected MarketAPI from;

    protected FactionAPI colonyFaction;

    protected boolean enteredSystem = false;
    protected KadurColonialExpeditionOutcome outcome;

    protected Random random = new Random();

    @Override
    public FactionAPI getFaction() {
        return colonyFaction;
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public VayraColonialExpeditionIntel(FactionAPI faction, MarketAPI from, MarketAPI target, float fleetPoints) {
        super(target.getStarSystem(), faction, null);
        this.colonyFaction = faction;
        this.delegate = this;
        this.from = from;
        this.target = target;

        float orgDur = VAYRA_DEBUG ? 1f : 20f + 10f * (float) Math.random();
        addStage(new VayraColonialExpeditionStage1Organize(this, from, orgDur));

        SectorEntityToken gather = from.getPrimaryEntity();

        float successMult = 0.5f;

        VayraColonialExpeditionStage2Assemble assemble = new VayraColonialExpeditionStage2Assemble(this, gather);
        assemble.addSource(from);
        assemble.setSpawnFP(fleetPoints);
        assemble.setAbortFP(fleetPoints * successMult);
        addStage(assemble);

        SectorEntityToken expeditionJump = RouteLocationCalculator.findJumpPointToUse(getFactionForUIColors(), target.getPrimaryEntity());

        VayraColonialExpeditionStage3Travel travel = new VayraColonialExpeditionStage3Travel(this, gather, expeditionJump, false);
        travel.setAbortFP(fleetPoints * successMult);
        addStage(travel);

        action = new VayraColonialExpeditionStage4Colonize(this, target);
        action.setAbortFP(fleetPoints * successMult);
        addStage(action);

        addStage(new VayraColonialExpeditionStage5Defend(this));

        Global.getSector().getIntelManager().addIntel(this);
    }

    @Override
    public CampaignFleetAPI spawnFleet(RouteData route) {

        Random thisRandom = route.getRandom();

        MarketAPI market = route.getMarket();
        CampaignFleetAPI fleet = createFleet(colonyFaction.getId(), route, market, null, thisRandom);

        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        fleet.addScript(createAssignmentAI(fleet, route));

        return fleet;
    }

    @Override
    @SuppressWarnings("null")
    public CampaignFleetAPI createFleet(String factionId, RouteData route, MarketAPI market, Vector2f locInHyper, Random random) {
        if (random == null) {
            random = new Random();
        }

        RouteManager.OptionalFleetData extra = route.getExtra();

        float combat = extra.fp;
        float tanker = extra.fp * (0.1f + random.nextFloat() * 0.05f);
        float transport = extra.fp * (0.1f + random.nextFloat() * 0.05f);
        float freighter = extra.fp * (0.1f + random.nextFloat() * 0.05f);
        float liner = extra.fp * (0.1f + random.nextFloat() * 0.05f);
        float utility = extra.fp * (random.nextFloat() * 0.05f);

        FleetParamsV3 params = new FleetParamsV3(
                market,
                locInHyper,
                factionId,
                route == null ? null : route.getQualityOverride(),
                extra.fleetType,
                combat, // combatPts
                freighter, // freighterPts 
                tanker, // tankerPts
                transport, // transportPts
                liner, // linerPts
                utility, // utilityPts
                0f // qualityMod, won't get used since routes mostly have quality override set
        );
        if (route != null) {
            params.timestamp = route.getTimestamp();
        }
        params.random = random;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_WAR_FLEET, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_RAIDER, true);

        if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        }

        String postId = Ranks.POST_PATROL_COMMANDER;
        String rankId = Ranks.SPACE_COMMANDER;

        fleet.getCommander().setPostId(postId);
        fleet.getCommander().setRankId(rankId);

        return fleet;
    }

    public Random getRandom() {
        return random;
    }

    public MarketAPI getTarget() {
        return target;
    }

    public MarketAPI getFrom() {
        return from;
    }

    @Override
    public RouteFleetAssignmentAI createAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
        VayraColonialExpeditionAssignmentAI expeditionAI = new VayraColonialExpeditionAssignmentAI(fleet, route, action);
        return expeditionAI;
    }

    public boolean isEnteredSystem() {
        return enteredSystem;
    }

    public void setEnteredSystem(boolean enteredSystem) {
        this.enteredSystem = enteredSystem;
    }

    public KadurColonialExpeditionOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(KadurColonialExpeditionOutcome outcome) {
        this.outcome = outcome;
    }

    @Override
    protected void advanceImpl(float amount) {
        super.advanceImpl(amount);
    }

    protected transient ReputationAdjustmentResult repResult = null;

    public void makeHostile() {
        boolean hostile = getFaction().isHostileTo(Factions.PLAYER);
        if (!hostile) {
            repResult = Global.getSector().adjustPlayerReputation(new RepActionEnvelope(RepActions.MAKE_HOSTILE_AT_BEST,
                            null, null, null, false, true),
                    colonyFaction.getId());
        }
    }

    public void sendInSystemUpdate() {
        sendUpdateIfPlayerHasIntel(ENTERED_SYSTEM_UPDATE, false, true);
    }

    public void applyRepPenalty(float delta) {
        CustomRepImpact impact = new CustomRepImpact();
        impact.delta = delta;
        repResult = Global.getSector().adjustPlayerReputation(
                new RepActionEnvelope(RepActions.CUSTOM,
                        impact, null, null, false, false),
                getFaction().getId());
    }

    public void sendOutcomeUpdate() {
        sendUpdateIfPlayerHasIntel(OUTCOME_UPDATE, false, true);
        log.info(String.format("Sending outcome update %s for expedition in %s", outcome.name(), target.getStarSystem().getNameWithLowercaseType()));
    }

    @Override
    public String getName() {
        String base = Misc.ucFirst(colonyFaction.getEntityNamePrefix()) + " Colonial Expedition";
        if (outcome == KadurColonialExpeditionOutcome.EXPEDITION_DESTROYED
                || outcome == KadurColonialExpeditionOutcome.TARGET_ALREADY_COLONIZED) {
            return base + " - Failed";

        } else if (outcome == KadurColonialExpeditionOutcome.COLONY_ESTABLISHED) {
            return base + " - Completed";
        }
        return base;
    }

    @Override
    @SuppressWarnings("UnusedAssignment")
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) {
            initPad = opad;
        }

        Color tc = getBulletColorForMode(mode);

        bullet(info);
        boolean isUpdate = getListInfoParam() != null;

        if (getListInfoParam() == ENTERED_SYSTEM_UPDATE) {
            info.addPara("Target: %s", initPad, tc,
                    g, target.getName());
            initPad = 0f;
            info.addPara("Arrived in-system", tc, initPad);
            log.info(String.format("Colonial expedition has arrived in %s", system.getNameWithLowercaseType()));
            return;
        }

        if (getListInfoParam() == MADE_HOSTILE_UPDATE) {
            info.addPara("Target: %s", initPad, tc,
                    g, target.getName());
            initPad = 0f;
            info.addPara(faction.getDisplayName() + " arrived to discover their destination already colonized", initPad, tc,
                    faction.getBaseUIColor(), faction.getDisplayName());
            initPad = 0f;
            CoreReputationPlugin.addAdjustmentMessage(repResult.delta, faction, null,
                    null, null, info, tc, isUpdate, initPad);
            return;
        }

        if (getListInfoParam() == OUTCOME_UPDATE) {
            if (outcome == KadurColonialExpeditionOutcome.COLONY_ESTABLISHED) {
            }
            initPad = 0f;
            info.addPara("Target: %s", initPad, tc,
                    g, target.getName());
            initPad = 0f;
            info.addPara("Colony Established", tc, initPad);
            return;
        }

        float eta = getETA();

        info.addPara("Target: %s", initPad, tc,
                g, target.getName());
        initPad = 0f;

        if (eta > 1 && outcome == null) {
            String days = getDaysString(eta);
            info.addPara("Estimated %s " + days + " until arrival",
                    initPad, tc, h, "" + Math.round(eta));
            initPad = 0f;

        } else if (outcome == null && action.getElapsed() > 0) {
            info.addPara("Colonization under way", tc, initPad);
            initPad = 0f;
        }

        unindent(info);
    }

    @Override
    public VayraColonialExpeditionStage4Colonize getActionStage() {
        for (RaidStage stage : stages) {
            if (stage instanceof VayraColonialExpeditionStage4Colonize) {
                return (VayraColonialExpeditionStage4Colonize) stage;
            }
        }
        return null;
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        super.createIntelInfo(info, mode);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        info.addImage(getFactionForUIColors().getLogo(), width, 128, opad);

        String has = colonyFaction.getDisplayNameHasOrHave();
        String is = colonyFaction.getDisplayNameIsOrAre();

        String strDesc = getRaidStrDesc();

        String articleOfPlanet = aOrAn(target.getPlanetEntity().getTypeNameWithLowerCaseWorld());

        LabelAPI label = info.addPara(Misc.ucFirst(colonyFaction.getDisplayNameWithArticle()) + " " + is
                        + " sending a colonial expedition to %s, " + articleOfPlanet + " %s in the %s."
                        + " The expedition is projected to be " + strDesc + ".",
                opad, colonyFaction.getBaseUIColor(), target.getName(), target.getPlanetEntity().getTypeNameWithLowerCaseWorld().toLowerCase(), target.getStarSystem().getNameWithLowercaseType());
        label.setHighlight(colonyFaction.getDisplayNameWithArticleWithoutArticle(), target.getName(), strDesc);
        label.setHighlightColors(colonyFaction.getBaseUIColor(), target.getFaction().getBaseUIColor(), h);

        info.addSectionHeading("Status",
                colonyFaction.getBaseUIColor(), colonyFaction.getDarkUIColor(), Alignment.MID, opad);

        for (RaidStage stage : stages) {
            stage.showStageInfo(info);
            if (getStageIndex(stage) == failStage) {
                break;
            }
        }

    }

    @Override
    public void sendUpdateIfPlayerHasIntel(Object listInfoParam, boolean onlyIfImportant, boolean sendIfHidden) {

        if (listInfoParam == UPDATE_RETURNING) {
            // we're using sendOutcomeUpdate() to send an end-of-event update instead
            return;
        }

        super.sendUpdateIfPlayerHasIntel(listInfoParam, onlyIfImportant, sendIfHidden);
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {

        Set<String> tags = super.getIntelTags(map);
        tags.remove(Tags.INTEL_MILITARY);
        tags.add(Tags.INTEL_EXPLORATION);
        tags.add("Colonial Expeditions");
        tags.add(getFaction().getId());
        return tags;
    }

    @Override
    public void notifyRaidEnded(RaidIntel raid, RaidStageStatus status) {
        if (outcome == null && failStage >= 0) {
            if (target.hasIndustry(Industries.POPULATION) || target.isPlayerOwned()) {
                outcome = KadurColonialExpeditionOutcome.TARGET_ALREADY_COLONIZED;
            } else {
                outcome = KadurColonialExpeditionOutcome.EXPEDITION_DESTROYED;
            }
        }
    }

    @Override
    public String getIcon() {
        return colonyFaction.getCrest();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (target != null && target.isInEconomy() && target.getPrimaryEntity() != null) {
            return target.getPrimaryEntity();
        }
        return super.getMapLocation(map);
    }
}
