package data.scripts.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.campaign.intel.bar.events.VayraDungeonMasterBarEvent.KEY_CHARACTER_SHEET;
import static data.scripts.campaign.intel.bar.events.VayraDungeonMasterBarEvent.armorName;
import static data.scripts.campaign.intel.bar.events.VayraDungeonMasterBarEvent.weaponName;
import data.scripts.campaign.intel.bar.events.VayraDungeonMasterData.VayraRPGCharacterSheetData;
import static data.scripts.campaign.intel.bar.events.VayraDungeonMasterData.XP;
import java.awt.Color;
import java.util.Set;

public class VayraRPGAliveCharacterIntel extends BaseIntelPlugin {

    public final String title;
    public static final String KEY = "$vayra_AliveRPGCharacterIntel";

    public VayraRPGCharacterSheetData data;

    // shared text settings
    Color h = Misc.getHighlightColor();
    Color p = Misc.getPositiveHighlightColor();
    Color n = Misc.getNegativeHighlightColor();
    Color g = Misc.getGrayColor();
    Color t = Misc.getTextColor();
    Color s = Misc.getStoryOptionColor();
    float pad = 3f;
    float opad = 10f;

    public VayraRPGAliveCharacterIntel() {
        this.title = "Current Character";
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static VayraRPGAliveCharacterIntel getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (VayraRPGAliveCharacterIntel) test;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> objTags = super.getIntelTags(map);
        objTags.add("Roleplaying Games");
        return objTags;
    }

    @Override
    public boolean isHidden() {
        data = (VayraDungeonMasterData.VayraRPGCharacterSheetData) Global.getSector().getPersistentData().get(KEY_CHARACTER_SHEET);
        return data == null || data.dead || data.retired;
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
        return "A" + getName();
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction(Factions.PLAYER);
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    protected void addBulletPoints(TooltipMakerAPI info) {

        float initPad = opad;

        bullet(info);

        data = (VayraDungeonMasterData.VayraRPGCharacterSheetData) Global.getSector().getPersistentData().get(KEY_CHARACTER_SHEET);

        if (data == null) {
            info.addPara("You do not currently have a living, non-retired character.", initPad);
        } else {
            info.addPara(data.name + ", " + data.className + " " + data.level, initPad, h, data.name, data.className + " " + data.level);
            if (data.level < 20) {
                int toLevel = XP.get(data.level + 1);
                info.addPara("XP: " + data.xp + "/" + toLevel, pad, h, data.xp + "/" + toLevel);
            } else {
                info.addPara("XP: " + data.xp + " (at maximum level)", pad, p, data.xp + " (at maximum level)");
            }
            info.addPara("HP: " + data.maxHp + "/" + data.maxHp + "    AC: " + data.getAc() + " (" + data.getMagicArmorString() + armorName(data.armor)
                    + data.getShieldString() + ")", pad, h, data.maxHp + "/" + data.maxHp, data.getAc() + "", data.getMagicArmorString());
            info.addPara("STR: " + data.textStr() + "  DEX: " + data.textDex() + "  INT: " + data.textInt() + "  CHA: " + data.textCha(),
                    pad, h, data.textStr(), data.textDex(), data.textInt(), data.textCha());
            String textAttack = (data.getToHit() >= 0 ? "+" : "") + data.getToHit();
            info.addPara(textAttack + " to hit, " + data.getMagicWeaponString() + weaponName(data.weapon) + ", " + data.getDamageString(),
                    pad, h, textAttack, data.getMagicWeaponString(), data.getDamageString());
            info.addPara(data.getInventoryString(), pad, h, data.gp + "", data.oil + "", data.scrolls + "", data.potions + "");
        }

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);

        info.addPara(getName(), c, 0f);

        if (data == null) {
            info.addPara("No current character", opad);
        } else {
            if (data.level < 20) {
                int toLevel = XP.get(data.level + 1);
                info.addPara(data.name + ", level " + data.level + " " + data.className, opad, h, data.name, data.level + "", data.className);
                info.addPara("XP: " + data.xp + "/" + toLevel, pad, h, data.xp + "/" + toLevel);
            } else {
                info.addPara(data.name + ", level " + data.level + " " + data.className, opad, p, data.name, data.level + "", data.className);
                info.addPara("XP: " + data.xp + " (max level)", pad, p, data.xp + " (max level)");
            }
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {

        addBulletPoints(info);
    }
}
