package data.scripts.campaign.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import static com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.pickVariant;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.campaign.events.VayraGhostShipSpecial.VayraGhostShipSpecialData;
import java.util.Random;
import org.apache.log4j.Logger;

public class VayraGhostShipDistressCall implements EveryFrameScript {

    public static Logger log = Global.getLogger(VayraGhostShipDistressCall.class);
    public static final String EVENT_ID = "$vayra_ghostship"; // needs to match distress_call_id in distress_call_data.csv
    public static final int MIN_CREW_FOR_GHOST_SHIP = 150;

    private float duration = 60f; // days
    private boolean setup = false;
    private boolean done = false;
    private SectorEntityToken entity;

    @Override
    public void advance(float amount) {

        // if we haven't done the thing, do the thing
        if (!setup) {
            setup();
            setup = true;
        }

        // make sure to clean up after yourself...
        float days = Global.getSector().getClock().convertToDays(amount);
        duration -= days;
        if (duration <= 0) {
            done = true;
            Misc.makeUnimportant(entity, EVENT_ID);
        }
    }

    // get the StarSystemAPI from the distress call manager so we know where to do what we're doing
    protected void setup() {
        VayraDistressCallManager manager = VayraDistressCallManager.getInstance();
        if (manager != null) {
            StarSystemAPI system = manager.getSystem(EVENT_ID);
            log.info("setting up distress call in " + system);
            spawnWreck(system);
        } else {
            log.error("couldn't get EVENT_ID from distress call manager, aborting");
        }
    }

    protected void spawnWreck(StarSystemAPI system) {
        SectorEntityToken jumpPoint = Misc.getDistressJumpPoint(system);
        if (jumpPoint == null) {
            return;
        }

        WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(null, system.getLocation(), 20f, 0, 0);
        factions.add(Factions.INDEPENDENT, 2f);
        factions.add(Factions.SCAVENGERS, 3f);
        factions.add(Factions.PIRATES, 2f);
        factions.add(Factions.HEGEMONY, 3f);
        factions.add(Factions.PERSEAN, 1f);
        factions.add(Factions.TRITACHYON, 2f);
        String variantId = null;
        ShipVariantAPI variant = null;
        int counter = 0;
        int max = 100;
        while (variant == null && counter < max) {
            String faction = factions.pick();
            variantId = pickVariant(faction, new Random(),
                    ShipRoles.COMBAT_LARGE,
                    ShipRoles.COMBAT_CAPITAL,
                    ShipRoles.COMBAT_CAPITAL,
                    ShipRoles.CARRIER_MEDIUM,
                    ShipRoles.CARRIER_LARGE
            );
            ShipVariantAPI test = Global.getSettings().getVariant(variantId);
            if (test != null && test.getHullSpec().getMinCrew() >= MIN_CREW_FOR_GHOST_SHIP) {
                variant = test;
            }
            counter++;
        }

        if (variant == null) {
            log.error("tried " + max + " times to pick new faction and ghost ship and failed, aborting");
            return;
        }

        ShipCondition condition = Math.random() > 0.5f ? ShipCondition.PRISTINE : ShipCondition.GOOD;
        PerShipData shipData = new PerShipData(variantId, condition);
        DerelictShipData params = new DerelictShipData(shipData, false);

        params.durationDays = duration;
        entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        entity.addTag(Tags.EXPIRES);

        DerelictShipEntityPlugin plugin = new DerelictShipEntityPlugin();
        plugin.init(entity, params);

        float radius = 400f + 400f * (float) Math.random();
        float maxRadius = Math.max(300, jumpPoint.getCircularOrbitRadius() * 0.33f);
        if (radius > maxRadius) {
            radius = maxRadius;
        }

        float orbitDays = radius / (5f + StarSystemGenerator.random.nextFloat() * 20f);
        float angle = (float) Math.random() * 360f;
        entity.setCircularOrbit(jumpPoint, angle, radius, orbitDays);

        if (shipData.variant == null) {
            log.info("variant was null, using variantId " + shipData.variantId + " instead");
            shipData.variant = Global.getSettings().getVariant(shipData.variantId);
            shipData.variantId = null;
            log.info("set variant to " + shipData.variant + " and variantId to null");
        }
        shipData.variant = shipData.variant.clone();

        try {
            if (shipData.variant.getHints().contains(ShipTypeHints.UNBOARDABLE) || shipData.variant.getHullSpec().getMinCrew() <= 0) {
                log.error("ghost ship was UNBOARDABLE or had <= 0 min crew, aborting, goddamnit");
                return;
            }
        } catch (NullPointerException npx) {
            log.error("shipData.variant or its .getHints or .getHullSpec was null, not aborting, we'll see this nightmare though");
        }

        VayraGhostShipSpecialData special = new VayraGhostShipSpecialData((CustomCampaignEntityAPI) entity, shipData);
        Misc.setSalvageSpecial(entity, (Object) special);

        Misc.makeImportant(entity, EVENT_ID);

        log.info("added entity " + entity.getName() + " in system " + system);
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

}
