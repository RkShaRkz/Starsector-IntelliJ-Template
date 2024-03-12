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

public class VayraRPGRetiredCharacterIntel extends BaseIntelPlugin {

    public final String title;
    public static final String KEY = "$vayra_RetiredRPGCharacterIntel";

    public List<VayraRPGCharacterSheetData> list;
    public int retired = 0;
    public boolean hasRetired = false;

    // shared text settings
    Color h = Misc.getHighlightColor();
    Color p = Misc.getPositiveHighlightColor();
    Color n = Misc.getNegativeHighlightColor();
    Color g = Misc.getGrayColor();
    Color t = Misc.getTextColor();
    Color s = Misc.getStoryOptionColor();
    float pad = 3f;
    float opad = 10f;

    public VayraRPGRetiredCharacterIntel() {
        this.title = "Retired Characters";
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static VayraRPGRetiredCharacterIntel getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (VayraRPGRetiredCharacterIntel) test;
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
        if (list == null) {
            return true;
        }
        retired = 0;
        for (VayraRPGCharacterSheetData data : list) {
            if (data.retired) {
                retired++;
                hasRetired = true;
            }
        }
        return !hasRetired;
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
        return "B" + getName();
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

        list = (List<VayraRPGCharacterSheetData>) Global.getSector().getPersistentData().get(KEY_RETIRED);
        retired = 0;
        boolean hasLevel20 = false;
        for (VayraRPGCharacterSheetData data : list) {
            if (data.retired) {
                retired++;
                hasRetired = true;
                if (data.level >= 20) {
                    hasLevel20 = true;
                }
            }
        }

        if (!hasRetired) {
            info.addPara("You do not currently have any retired characters.", opad);
        } else {
            if (hasLevel20) {
                info.addPara("You have received arcane secrets of engineering from one of your dungeon masters.", opad, h, "arcane secrets");
                bullet(info);
                info.addPara("+5%% Ordnance Points to all ships", pad, p, "+5%");
                unindent(info);
            }
            Collections.sort(list);
            Collections.reverse(list);
            for (VayraRPGCharacterSheetData data : list) {
                if (data.retired) {
                    info.addPara(data.name + ", level " + data.level + " " + data.className, opad, p, data.name, data.level + "", data.className);
                    bullet(info);
                    info.addPara("Score: " + data.xp, pad, p, data.xp + "");
                    info.addPara("Descended " + data.maxDepth + " rooms into the dungeon and " + data.fate + ".", pad, p, data.maxDepth + "", data.fate);
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
        retired = 0;
        for (VayraRPGCharacterSheetData data : list) {
            if (data.retired) {
                retired++;
                hasRetired = true;
            }
        }
        if (!hasRetired) {
            info.addPara("No retired characters", opad);
        } else {
            info.addPara(retired + " retired character" + (retired > 1 ? "s" : ""), opad, h, retired + "");
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {

        addBulletPoints(info);
    }
}
