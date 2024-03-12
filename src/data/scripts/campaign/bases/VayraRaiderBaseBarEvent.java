package data.scripts.campaign.bases;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;

import java.util.Map;

public class VayraRaiderBaseBarEvent extends BaseBarEvent {

    protected VayraRaiderBaseIntel intel;

    public VayraRaiderBaseBarEvent(VayraRaiderBaseIntel intel) {
        this.intel = intel;
    }

    public VayraRaiderBaseBarEvent() {
        // this one trusts that something will come along and set our intel shortly
    }

    public void setIntel(VayraRaiderBaseIntel intel) {
        this.intel = intel;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        return intel.getTarget() == market.getContainingLocation();
    }

    @Override
    public boolean shouldRemoveEvent() {
        return intel.isEnding() || intel.isEnded() || intel.isPlayerVisible();
    }

    transient protected Gender gender;

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);

        gender = Gender.MALE;
        if ((float) Math.random() > 0.5f) {
            gender = Gender.FEMALE;
        }

        String himOrHer = "him";
        if (gender == Gender.FEMALE) {
            himOrHer = "her";
        }

        TextPanelAPI text = dialog.getTextPanel();
        text.addPara("A grizzled spacer sits at the bar, downing shots "
                + "of what looks like the cheapest liquor available.");

        dialog.getOptionPanel().addOption(
                "Approach the spacer and offer to buy " + himOrHer + " something more palatable",
                this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);

        String himOrHerSelf = "himself";
        if (gender == Gender.FEMALE) {
            himOrHerSelf = "herself";
        }

        TextPanelAPI text = dialog.getTextPanel();
        text.addPara("You keep the drinks going and mostly just listen, "
                + "letting the spacer unburden " + himOrHerSelf + ".");
        PersonAPI person;
        try {
            person = Global.getSector().getFaction(intel.getData().raiderFactionId).createRandomPerson(gender);
        } catch (NullPointerException ex) {
            person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(gender);
        }
        dialog.getVisualPanel().showPersonInfo(person, true);

        done = true;
        intel.makeKnown();
        intel.sendUpdate(VayraRaiderBaseIntel.DISCOVERED_PARAM, text);

        PortsideBarData.getInstance().removeEvent(this);
        // Removes this event from the bar so it isn't offered again
        BarEventManager.getInstance().notifyWasInteractedWith(this);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
    }

    @Override
    public boolean isDialogFinished() {
        return done;
    }

    protected boolean showCargoCap() {
        return false;
    }
}
