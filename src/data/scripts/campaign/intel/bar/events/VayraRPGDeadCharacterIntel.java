package data.scripts.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.intel.bar.events.VayraDungeonMasterData.VayraRPGCharacterSheetData;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static data.scripts.campaign.intel.bar.events.VayraDungeonMasterBarEvent.KEY_RETIRED;

public class VayraRPGDeadCharacterIntel extends BaseIntelPlugin {

    public final String title;
    public static final String KEY = "$vayra_DeadRPGCharacterIntel";

    public List<VayraRPGCharacterSheetData> list;
    public int dead = 0;
    public boolean hasDead = false;

    // shared text settings
    Color h = Misc.getHighlightColor();
    Color p = Misc.getPositiveHighlightColor();
    Color n = Misc.getNegativeHighlightColor();
    Color g = Misc.getGrayColor();
    Color t = Misc.getTextColor();
    Color s = Misc.getStoryOptionColor();
    float pad = 3f;
    float opad = 10f;

    public VayraRPGDeadCharacterIntel() {
        this.title = "Dead Characters";
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static VayraRPGDeadCharacterIntel getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (VayraRPGDeadCharacterIntel) test;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> objTags = super.getIntelTags(map);
        objTags.add("Roleplaying Games");
        return objTags;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isHidden() {
        list = (List<VayraRPGCharacterSheetData>) Global.getSector().getPersistentData().get(KEY_RETIRED);
        if (list == null) return true;
        dead = 0;
        for (VayraRPGCharacterSheetData data : list) {
            if (data.dead) {
                dead++;
                hasDead = true;
            }
        }
        return !hasDead;
    }

    @Override
    public boolean isEnding() {
        return false;
    }

    @Override
    public boolean isEnded() {
        return false;
    }

    @Override
    public String getIcon() {
        return Global.getSector().getPlayerPerson().getPortraitSprite();
    }

    public String getName() {
        return title;
    }

    @Override
    public String getSortString() {
        return "C" + getName();
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction(Factions.PLAYER);
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @SuppressWarnings("unchecked")
    protected void addBulletPoints(TooltipMakerAPI info) {

        float initPad = opad;

        list = (List<VayraRPGCharacterSheetData>) Global.getSector().getPersistentData().get(KEY_RETIRED);
        dead = 0;
        for (VayraRPGCharacterSheetData data : list) {
            if (data.dead) {
                dead++;
                hasDead = true;
            }
        }

        if (!hasDead) {
            info.addPara("You do not currently have any dead characters.", initPad);
        } else {
            Collections.sort(list);
            Collections.reverse(list);
            for (VayraRPGCharacterSheetData data : list) {
                if (data.dead) {
                    info.addPara(data.name + ", level " + data.level + " " + data.className, opad, h, data.name, data.level + "", data.className);
                    bullet(info);
                    info.addPara("Score: " + data.xp, pad, h, data.xp + "");
                    info.addPara("Descended " + data.maxDepth + " rooms into the dungeon and was " + data.fate + ".", pad, n, data.maxDepth + "", data.fate);
                    unindent(info);
                }
            }
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);

        info.addPara(getName(), c, 0f);

        list = (List<VayraRPGCharacterSheetData>) Global.getSector().getPersistentData().get(KEY_RETIRED);
        dead = 0;
        for (VayraRPGCharacterSheetData data : list) {
            if (data.dead) {
                dead++;
                hasDead = true;
            }
        }
        if (!hasDead) {
            info.addPara("No dead characters", opad);
        } else {
            info.addPara(dead + " dead character" + (dead > 1 ? "s" : ""), opad, h, dead + "");
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {

        addBulletPoints(info);
    }
}
