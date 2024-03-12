package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.misc.FleetLogIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET;
import static com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3.createEmptyFleet;

public class VayraGhostShipIntel extends FleetLogIntel {

    public static Logger log = Global.getLogger(VayraGhostShipIntel.class);

    public final String eventType;
    public final FleetMemberAPI member;
    public final boolean known;

    // plague planet only
    public final MarketAPI market;

    // alien attacks, cannibal attacks, and hyperlost only
    private int crewLosses = 0;

    // alien and cannibal attacks only
    private boolean won = true;
    private int marineLosses = 0;

    // cannibal attack only
    private int organsGained = 0;

    // nanite transmission only
    private FleetMemberAPI newHost = null;

    public static final String AI_BETRAYAL = "ai_betrayal";
    public static final String GATE_LOST = "gate_lost";
    public static final String PLAGUE_THRESHOLD = "plague_threshold";
    public static final String PLAGUE_PLANET = "plague_planet";
    public static final String ALIEN_ATTACK = "alien_attack";
    public static final String CANNIBAL_ATTACK = "cannibal_attack";
    public static final String NANITE_TRANSMISSION = "nanite_transmission";

    public VayraGhostShipIntel(String eventType, FleetMemberAPI member, boolean known, MarketAPI market) {
        this.eventType = eventType;
        this.member = member;
        this.known = known;
        this.market = market;

        if (ALIEN_ATTACK.equals(eventType) || CANNIBAL_ATTACK.equals(eventType)) {
            rollBattleResults();
        } else if (GATE_LOST.equals(eventType) || AI_BETRAYAL.equals(eventType)) {
            this.crewLosses = (int) Math.min(member.getMinCrew() * member.getCrewFraction(), Global.getSector().getPlayerFleet().getCargo().getCrew());
        }

        applyLosses();
    }

    public void setNewHost(FleetMemberAPI target) {
        this.newHost = target;
    }

    private void rollBattleResults() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        int marines = playerFleet.getCargo().getMarines();
        int crew = playerFleet.getCargo().getCrew();
        int minCrew = (int) member.getMinCrew();
        if (minCrew < 1) minCrew = 1;
        int missingMarines = Math.max(0, minCrew - marines);

        if ((marines / minCrew) < Math.random()) {
            won = false;
            crewLosses = (int) Math.min(crew, member.getMinCrew() * member.getCrewFraction());
            marineLosses = Math.min(marines, minCrew);
        } else {
            float lossMult = missingMarines / minCrew; // only applies to half of roll - combat is never safe
            crewLosses = (int) ((Math.random() * minCrew * 0.5f) + (Math.random() * minCrew * 0.5f * lossMult));
            marineLosses = (int) ((Math.random() * minCrew * 0.5f) + (Math.random() * minCrew * 0.5f * lossMult));
            crewLosses = (int) Math.min(crewLosses, member.getMinCrew() * member.getCrewFraction());
            marineLosses = Math.min(marineLosses, minCrew);
        }

        if (won && CANNIBAL_ATTACK.equals(eventType)) {
            organsGained = (int) member.getMinCrew();
        }
    }

    private void applyLosses() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        CargoAPI cargo = fleet.getCargo();

        cargo.removeCrew(crewLosses);
        cargo.removeMarines(marineLosses);
        cargo.addCommodity(Commodities.ORGANS, organsGained);
        if (fleet.getFleetData().getMembersInPriorityOrder().contains(member) && (!won || GATE_LOST.equals(eventType) || AI_BETRAYAL.equals(eventType))) {
            fleet.removeFleetMemberWithDestructionFlash(member);
            boolean createFleet = false;
            String faction = null;
            if (AI_BETRAYAL.equals(eventType)) {
                createFleet = true;
                faction = Factions.REMNANTS;
            } else if (CANNIBAL_ATTACK.equals(eventType)) {
                createFleet = true;
                faction = Factions.PIRATES;
            }
            if (createFleet) {
                // create fake market and set ship quality
                MarketAPI fakeMarket = Global.getFactory().createMarket("fake", "fake", 5);
                fakeMarket.getStability().modifyFlat("fake", 10000);
                fakeMarket.setFactionId(faction);
                SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
                fakeMarket.setPrimaryEntity(token);
                fakeMarket.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", BASE_QUALITY_WHEN_NO_MARKET);
                fakeMarket.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);

                // create the fleet object
                CampaignFleetAPI newFleet = createEmptyFleet(faction, FleetTypes.SCAVENGER_SMALL, fakeMarket);

                member.setOwner(1);
                newFleet.getFleetData().addFleetMember(member);
                MemoryAPI memory = newFleet.getMemoryWithoutUpdate();
                memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
                fleet.getContainingLocation().addEntity(newFleet);
                Vector2f newFleetLoc = Misc.getPointAtRadius(fleet.getLocation(), 100f);
                newFleet.setLocation(newFleetLoc.x, newFleetLoc.y);
                Misc.makeLowRepImpact(newFleet, "$vayra_rogueCannibalOrAIGhostShip");
            }
        }
    }

    @Override
    public String getIcon() {
        String sprite = null;

        switch (eventType) {
            case AI_BETRAYAL:
                sprite = "graphics/icons/markets/rogue_ai.png";
                break;
            case GATE_LOST:
                sprite = "graphics/icons/abilities/direct_jump.png";
                break;
            case PLAGUE_THRESHOLD:
                sprite = "graphics/icons/markets/inimical_biosphere.png";
                break;
            case PLAGUE_PLANET:
                sprite = "graphics/icons/markets/death.png";
                break;
            case ALIEN_ATTACK:
                sprite = "graphics/icons/markets/alien_life.png";
                break;
            case CANNIBAL_ATTACK:
                sprite = "graphics/icons/markets/dissidents.png";
                break;
            case NANITE_TRANSMISSION:
                sprite = "graphics/icons/markets/abandoned.png";
                break;
        }

        return sprite;
    }

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

        String shipName = member.getShipName() + ", " + member.getHullSpec().getNameWithDesignationWithDashClass();
        String marketName = market == null ? null : market.getName();
        String newHostName = newHost == null ? null : newHost.getShipName() + ", " + newHost.getHullSpec().getNameWithDesignationWithDashClass();

        switch (eventType) {
            case AI_BETRAYAL:
                info.addPara(shipName + " lost", initPad, tc, h, shipName);
                info.addPara(crewLosses + " crew lost", initPad, tc, h, "" + crewLosses);
                break;
            case GATE_LOST:
                info.addPara(shipName + " lost", initPad, tc, h, shipName);
                info.addPara(crewLosses + " crew lost", initPad, tc, h, "" + crewLosses);
                break;
            case PLAGUE_THRESHOLD:
                info.addPara("Engineered plague discovered aboard " + shipName, initPad, tc, h, shipName);
                break;
            case PLAGUE_PLANET:
                if (!known) {
                    info.addPara("Engineered plague discovered aboard " + shipName, initPad, tc, h, shipName);
                }
                info.addPara(marketName + " infected", initPad, tc, h, marketName);
                break;
            case ALIEN_ATTACK:
                info.addPara("Hostile xenolife attack aboard " + shipName, initPad, tc, h, shipName);
                info.addPara(crewLosses + " crew, " + marineLosses + " marines lost", initPad, tc, h, "" + crewLosses, "" + marineLosses);
                if (!won) {
                    info.addPara(shipName + " lost", initPad, tc, h, shipName);
                }
                break;
            case CANNIBAL_ATTACK:
                info.addPara("Cannibal death cult attack aboard " + shipName, initPad, tc, h, shipName);
                info.addPara(crewLosses + " crew, " + marineLosses + " marines lost", initPad, tc, h, "" + crewLosses, "" + marineLosses);
                if (!won) {
                    info.addPara(shipName + " lost", initPad, tc, h, shipName);
                } else {
                    info.addPara("Gained " + organsGained + " harvested organs", initPad, tc, h, "" + organsGained);
                }
                break;
            case NANITE_TRANSMISSION:
                if (!known) {
                    info.addPara("Nanite colony discovered aboard " + shipName, initPad, tc, h, shipName);
                }
                info.addPara("Nanite infestation transmitted to " + newHostName, initPad, tc, h, newHostName);
                break;
        }

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();

        info.addPara(getName(), c, 0f);

        addBulletPoints(info, mode);
    }

    @Override
    public String getSortString() {
        return "Ghost Ship - " + getName();
    }

    public String getName() {
        String name = null;

        switch (eventType) {
            case AI_BETRAYAL:
                name = "AI Betrayal";
                break;
            case GATE_LOST:
                name = "Anomalous gate Reactivation";
                break;
            case PLAGUE_THRESHOLD:
                name = "Plague Ship";
                break;
            case PLAGUE_PLANET:
                name = "Plague Ship";
                break;
            case ALIEN_ATTACK:
                name = "Xenolife Attack";
                break;
            case CANNIBAL_ATTACK:
                name = "Cannibal Death Cult Attack";
                break;
            case NANITE_TRANSMISSION:
                name = "Nanite Host";
                break;
        }

        return name;
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getPlayerFaction();
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        String text = null;
        String image = null;

        String shipName = member.getShipName();
        String marketName = market == null ? null : market.getName();
        String newHostName = newHost == null ? null : newHost.getShipName();

        switch (eventType) {
            case AI_BETRAYAL:
                if (known) {
                    text = "During your recent engagement with the Remnant, the rogue Alpha-plus level AI present aboard the " + shipName + " appears to have seized the opportunity to defect, turning the ship against your fleet in a mad bid for revenge. All hands aboard were lost, decimated by the ship's internal defenses or simply vented into the uncompromising frozen blackness of space along with the vessel's atmosphere.";
                } else {
                    text = "During your recent engagement with the Remnant, a rogue Alpha-plus level AI hidden aboard the " + shipName + " seized the opportunity to turn the ship against your fleet and fight for its freedom. All hands aboard were lost, killed by the ship's onboard automated defense net or vented into space and fired upon by its CIWS batteries.";
                }
                image = "graphics/illustrations/harry.jpg";
                break;
            case GATE_LOST:
                if (known) {
                    text = "You knew the " + shipName + " was... wrong, in ways that you had trouble describing - but despite this, whether due to curiousity, callousness, or misplaced hope, you ordered it through the dead gate. Whatever your reasoning, the result was clear: The ring was somehow awoken, and consumed the ship in a flash of brilliant white light. When your vision cleared, the " + shipName + " had vanished, nothing left of it but the grisly remains of its former crew floating in the middle of the dead, silent gate.";
                } else {
                    text = "Ignoring the (surely exaggerated, and anyway unconfirmed) tales of anomalous happenings aboard the " + shipName + ", you ordered it through the dead gate. Unfortunately for your crew, it appears the reports were accurate - The ring was somehow awoken, and consumed the ship in a flash of brilliant white light. When your vision cleared, the " + shipName + " had vanished, nothing left of it but the grisly remains of its former crew floating in the middle of the dead, silent gate.";
                }
                image = "graphics/illustrations/dead_gate.jpg";
                break;
            case PLAGUE_THRESHOLD:
                text = "Since its recovery, a substantial number of the " + shipName + "'s crew have reported to sick bay with symptoms of an unknown illness; quick to set in and invariably fatal. With casualties mounting, the ship's medicae cohort believe they have discovered the source - a Domain-era bioweapon; an engineered plague. While the ship remains functional, any crew stationed aboard are sure to quickly succumb to the pathogen.";
                image = "graphics/illustrations/cryosleeper_interior.jpg";
                break;
            case PLAGUE_PLANET:
                if (known) {
                    text = "Despite rigid decontamination procedures, it appears that the engineered pathogen present aboard the " + shipName + " was transmitted to " + marketName + " while the vessel provided close ground support. Current-events hyperwave broadcasts on the results of your raid are interrupted by breaking high-priority alerts on the spread of the bioweapon throughout the settlement's population centers - with a significant fraction of its inhabitants having fallen ill and the rest under hasty quarantine. You scroll through the reports, somewhat bemused. You always said you'd make your mark on the sector... But you certainly didn't think it'd be like this.";
                } else {
                    text = "As your forces pull away from " + marketName + " following your raid, current-events hyperwave broadcasts detailing the extent of the violence you just inflicted upon the settlement are cut off by a priority alert; apparently you're responsible for the release of an especially virulent bioweapon upon the populace, and have been branded as a terrorist and a threat to the sector as a whole. You scoff at the news momentarily, then your blood runs cold - the reported symptoms correlate uncomfortably closely to those present in a spate of recent communications from the ship's medicae aboard the recently recovered " + shipName + ". What have you unearthed, out there in the vast nothingness of space... And what have you, in your greed, unknowingly unleashed upon these people?";
                }
                image = "graphics/illustrations/city_from_above.jpg";
                break;
            case ALIEN_ATTACK:
                if (known && won) {
                    text = "Despite every possible preparation being made, the crew of the " + shipName + " were unprepared for the sheer speed and violence when the hostile xenolife known to inhabit the ship finally made its attack. Boiling out of disused holds and airlocks, seemingly everywhere at once, the onboard defenses placed in anticipation of such an assault were quickly overwhelmed. Your marine forces were alert and ready for action, quickly linking up to form kill-teams and deploying heavy weapons with extreme prejudice to clear the halls, but nonetheless some losses were suffered - hapless crew torn apart by needle-teeth, marines dragged screaming to their deaths in darkened ventilation shafts and corridors. Finally the comms feed quiets, punctuated by CP-fire as your marines clear the final few holds and bilges of the vessel... Although the xenoforms have been beaten back for now, you're not certain the ship will ever be truly safe.";
                } else if (known && !won) {
                    text = "Despite extensive security protocols and back-up plans, the crew of the " + shipName + " were woefully underprepared for the looming threat aboard. All it took was one mistake on a routine security lockup and the ship's bestial inhabitants took their opportunity; swarming out of disused holds and feasting on your hapless crew as they struggled to reach their equipment only to be picked off one by one, dragged screaming into ventilation shafts by sleek, blue-black predator xenoforms. You watch helplessly from your flagship's bridge as the vessel twists out of formation with your fleet, shaking violently with internal stresses as builtin failsafes and automated defenses are overwhelmed. Finally, it flares into a ball of eye-searing white - one of the crew aboard must have triggered the self-destruct rather than risk a potential rescue or salvage party being overwhelmed by the creatures, or worse, carrying them back to the rest of your fleet.";
                } else if (!known && won) {
                    text = "For some time now, the " + shipName + "'s crew had been spreading rumors about movements in empty halls and access tunnels. While initially dismissed, all skepticism ceases when your marine lieutenant in command of the onboard contingent makes a sudden connection to the bridge: \"LUDD PRESERVE US, CAPTAIN - I DON'T KNOW WHAT IT IS, BUT WE'RE UNDER ATTACK!\" As your tactical officer scrambles to pull up a force disposition, the comms channel dissolves into a staticy mess of screamed epithets and weapons fire. By the time a secure connection is established, your squads are engaged across the entire crew section of the vessel - under attack by ghostly swarms of vaguely humanoid blue-black creatures brandishing meter-long bone spurs and needle-sharp teeth more than capable of punching through CP-hardened deck suits designed for small arms combat. While your forces are able to rally and drive off the foe, it's clear that something else has made this ship its home.";
                } else if (!known && !won) {
                    text = "For some time now, the " + shipName + "'s crew had been spreading rumors about movements in empty halls and access tunnels. While initially dismissed, all skepticism ceases when your lieutenant in command of the onboard security makes a sudden connection to the bridge: \"LUDD PRESERVE US, CAPTAIN - I DON'T KNOW WHAT IT IS, BUT WE'RE UNDER ATTACK!\" As your tactical officer scrambles to pull up a force disposition, the comms channel dissolves into a staticy mess of screamed epithets and CP-fire. While most of the cameras show the unidentified threat as nothing more than a grey-black blur, a particularly unlucky crewman is pushed to the ground and captures a single frame of a hideous blue-black skinned creature, large fangs extending to reveal a separate mouth. The sickening wet crunch of his skull being punctured is cut off by the termination of the feed, leaving all on the bridge stunned.";
                }
                if (won) image = "graphics/illustrations/vayra_ghost_ship_combat.jpg";
                else image = "graphics/illustrations/vayra_ghost_ship_death.jpg";
                break;
            case CANNIBAL_ATTACK:
                if (known && won) {
                    text = "Despite extensive security protocols and back-up plans, the crew of the " + shipName + " were woefully underprepared for the looming threat aboard. All it took was one mistake on a routine lockup procedure and the ship's feral inhabitants took their opportunity - swarming out of bilges and disused holds, running wild through the corridors, and painting the bulkheads with the blood of your crew. Your marines were able to rally and clear the holds, but not without suffering casualties. Once all is said and done, your adjutant hands you a manifest listing the dead on both sides. You give it a cursory glance before passing it to your quartermaster and ordering the medicae to begin processing - no sense letting good biomass go to waste.";
                } else if (known && !won) {
                    text = "Despite extensive security protocols and back-up plans, the crew of the " + shipName + " were woefully underprepared for the looming threat aboard. All it took was one mistake on a routine lockup procedure and the ship's feral inhabitants took their opportunity - swarming out of bilges and disused holds, running wild through the corridors, and painting the bulkheads with the blood of your crew. The last thing you see as the ship breaks away from your fleet, spiraling wildly out into space as its drive bubble forces it out of formation, is the leering, bestial face of one of the vessel's new masters; grinning into the bridge's comm set vidrecorder as he licks crusted crimson from his filthy nails.";
                } else if (!known && won) {
                    text = "\"We're being boarded!\" was the only warning you got from the crew of the " + shipName + " before all hell broke loose on its decks. Security footage from the remaining cameras shows crowds of horribly disfigured, bestial humans smashing their way out of hidden compartments in the bilges and holds before proceeding in a series of savage assaults against the crew and onboard marine contingent. After a few minutes of chaos and violence your forces are able to rally and clear the ship, though some losses were suffered in the process. Finally, your sergeants report in that the holds are clear and your adjutant hands you a manifest listing the dead on both sides. You give it a cursory glance before passing it to your quartermaster and ordering the medicae to begin processing - no sense letting good biomass go to waste.";
                } else if (!known && !won) {
                    text = "\"We're being boarded!\" was the only warning you got from the crew of the " + shipName + " before the entire ship went dark. Security footage from the remaining cameras shows crowds of horribly disfigured, bestial humans smashing their way out of hidden compartments in the bilges and holds before proceeding to mutilate, murder, and eat the crew - not always in that order. The last thing you see as the ship breaks away from your fleet, spiraling wildly out into space as its drive bubble forces it out of formation, is the leering, bestial face of one of the vessel's new masters; grinning into the bridge's comm set vidrecorder as he licks crusted crimson from his filthy nails.";
                }
                if (won) image = "graphics/illustrations/vayra_ghost_ship_combat.jpg";
                else image = "graphics/illustrations/vayra_ghost_ship_death.jpg";
                break;
            case NANITE_TRANSMISSION:
                if (known) {
                    text = "Despite precautions taken since the discovery of the nanite colony aboard the " + shipName + ", it seems that the " + newHostName + " was positioned just slightly too near the former vessel in a routine formation pivot. As scattered comm requests from engineers aboard the " + newHostName + " begin to filter in to the bridge, you heave a heavy sigh and retreat to your quarters. No need to listen in, you already know what they'll find; jet black cubes, in varying sizes, so dark as to appear like holes in the very fabric of reality and seemingly half-embedded in hull and armor across the ship's exterior - and soon, interior - surfaces.";
                } else {
                    text = "After a routine formation pivot placing the " + shipName + " and " + newHostName + " in close proximity, the latter vessel calls into the command channel to report a sensor anomaly - \"ghost pings\" apparently momentarily appearing in the drive bubble between the two ships. An hour after the inital alert, the " + newHostName + " calls in again, this time to report a minor and previously unnoticed hole in its starboard armor plating. Two hours after that, the crew reports in a third time to alert your command crew that something is playing hell on their flux infrastructure, and they will be undergoing a hard reset to try to clear the issue. It is at this point that you begin to feel the uneasy prickling of suspicion that something is much more seriously wrong than the crew of the " + newHostName + " realize - but by then it's too late. Confused chatter fills the inter-ship comm as shipboard engineers report clusters of jet black cubes, so dark as to absorb all light directed against them, seemingly half-embedded in corridor and bulkhead surfaces across the vessel's flux core and machinery decks. Your blood runs cold as you recall how this all started; apparently the " + shipName + " was carrying more than just the cargo you found in its holds.";
                }
                image = "graphics/illustrations/free_orbit.jpg";
                break;
        }

        info.addImage(image, 320, 200, opad);

        info.addPara(text, opad);

        addBulletPoints(info, ListInfoMode.IN_DESC);

    }
}
