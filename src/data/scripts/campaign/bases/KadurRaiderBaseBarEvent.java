package data.scripts.campaign.bases;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.util.Misc;
import static com.fs.starfarer.api.util.Misc.random;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static data.scripts.KadurModPlugin.KADUR_ID;
import static java.lang.Math.random;

public class KadurRaiderBaseBarEvent extends VayraRaiderBaseBarEvent {

    public KadurRaiderBaseBarEvent(VayraRaiderBaseIntel intel) {
        super(intel);
    }

    public KadurRaiderBaseBarEvent() {
        super();  // this one trusts that something will come along and set our intel shortly
    }

    transient protected Gender kgender;
    transient protected String pronoun;
    transient protected String pronoun2;
    transient protected String pronounself;
    transient protected String pronouner;
    transient protected String maskmat;

    public void addPromptAndOption(InteractionDialogAPI dialog) {

        WeightedRandomPicker<Gender> genders = new WeightedRandomPicker<>(random);
        genders.add(Gender.FEMALE, 1f);
        genders.add(Gender.MALE, 0.5f);
        kgender = genders.pick();

        pronoun = "them";
        if (kgender == Gender.FEMALE) {
            pronoun = "her";
        }
        if (kgender == Gender.MALE) {
            pronoun = "him";
        }

        pronoun2 = "they";
        if (kgender == Gender.FEMALE) {
            pronoun2 = "she";
        }
        if (kgender == Gender.MALE) {
            pronoun2 = "he";
        }

        pronounself = "themselves";
        if (kgender == Gender.FEMALE) {
            pronounself = "herself";
        }
        if (kgender == Gender.MALE) {
            pronounself = "himself";
        }

        pronouner = "their";
        if (kgender == Gender.FEMALE) {
            pronouner = "her";
        }
        if (kgender == Gender.MALE) {
            pronouner = "his";
        }

        maskmat = "ivory";
        if (random() > 0.5f) {
            maskmat = "brass";
        }

        TextPanelAPI text = dialog.getTextPanel();
        text.addPara("A masked figure in a black robe sits at a corner table, obviously uncomfortable despite the near-featureless " + maskmat + " plate covering " + pronouner + " face.");

        dialog.getOptionPanel().addOption(
                "Approach the robed, masked figure and ask " + pronoun + " if " + pronoun2 + " needs any help",
                this);
    }

    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;

        FactionAPI faction = Global.getSector().getFaction(KADUR_ID);

        PersonAPI person = faction.createRandomPerson(kgender);
        dialog.getVisualPanel().showPersonInfo(person, true);

        TextPanelAPI text = dialog.getTextPanel();
        text.addPara("The figure turns to regard you beneath " + pronouner + " impassive " + maskmat
                + " mask before speaking rapidly and distressedly in an unfamiliar language."
                + " You fumble awkwardly with your TriPad, activating the translation autosoft.");
        text.addPara(Misc.ucFirst(pronoun2) + " introduces " + pronounself + " as " + person.getRank() + " " + person.getNameString()
                + ", an ambassador from " + faction.getDisplayNameLongWithArticle() + ". Apparently, " + pronoun2
                + " travelled here to deliver a diplomatic missive, but " + pronouner + " message was refused and " + pronoun2
                + " couldn't gain audience with anyone who had the authority to respond. Now " + faction.getDisplayNameLongWithArticle()
                + " stands poised to attack " + dialog.getInteractionTarget().getName() + " -- " + Misc.ucFirst(pronoun2) + " has failed in " + pronouner
                + " duty as a diplomat, and war is at hand.");

        done = true;
        intel.makeKnown();
        intel.sendUpdate(VayraRaiderBaseIntel.DISCOVERED_PARAM, text);

        PortsideBarData.getInstance().removeEvent(this);
        // Removes this event from the bar so it isn't offered again
        BarEventManager.getInstance().notifyWasInteractedWith(this);
    }

}
