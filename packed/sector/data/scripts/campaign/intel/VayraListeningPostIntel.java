package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.impl.campaign.intel.misc.FleetLogIntel;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.VayraLoreObjectsFramework.LoreObjectData;
import java.awt.Color;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

public class VayraListeningPostIntel extends FleetLogIntel {

    public static Logger log = Global.getLogger(VayraListeningPostIntel.class);

    public final String dataId;
    public final String factionId;
    public final String title;
    public final String text;
    public final List<String> tags;

    public VayraListeningPostIntel(LoreObjectData object) {
        this.dataId = object.uniqueId;
        this.factionId = object.factionId;
        this.title = object.title;
        this.text = object.text;
        this.tags = object.tags;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> objTags = super.getIntelTags(map);
        objTags.add("Listening Posts");
        if (!"neutral".equals(factionId)) objTags.add(factionId);
        for (String tag : tags) {
            objTags.add(tag);
        }
        return objTags;
    }

    @Override
    public String getIcon() {
        String sprite = Global.getSector().getFaction(factionId).getCrest();
        return sprite;
    }

    public String getName() {
        return title;
    }

    @Override
    public String getSortString() {
        return "Listening Post - " + getName();
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction(factionId);
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
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

        info.addPara("New intel recorded", initPad);

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
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        info.addImage(getIcon(), width, 128, opad);

        info.addPara(text, opad);
    }
}
