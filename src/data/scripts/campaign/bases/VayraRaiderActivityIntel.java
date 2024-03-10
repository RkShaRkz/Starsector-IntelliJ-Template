package data.scripts.campaign.bases;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.campaign.bases.VayraRaiderBaseIntel.log;
import java.io.IOException;

public class VayraRaiderActivityIntel extends BaseIntelPlugin {

    protected StarSystemAPI system;
    protected VayraRaiderBaseIntel source;
    private transient boolean loaded = false;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public VayraRaiderActivityIntel(StarSystemAPI system, VayraRaiderBaseIntel source) {
        this.system = system;
        this.source = source;

        boolean hasPlayerMarkets = false;
        for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(system)) {
            hasPlayerMarkets |= curr.isPlayerOwned();
        }
        if (!hasPlayerMarkets) {
            setPostingLocation(system.getCenter());
        }

        Global.getSector().addScript(this);

        if (!Misc.getMarketsInLocation(system, Factions.PLAYER).isEmpty()) {
            Global.getSector().getIntelManager().addIntel(this);
        } else {
            Global.getSector().getIntelManager().queueIntel(this);
        }
    }

    @Override
    public boolean canMakeVisibleToPlayer(boolean playerInRelayRange) {
        if (DebugFlags.SEND_UPDATES_WHEN_NO_COMM && source.isPlayerVisible()) {
            return true;
        }
        return super.canMakeVisibleToPlayer(playerInRelayRange);
    }

    public VayraRaiderBaseIntel getSource() {
        return source;
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().removeScript(this);
    }

    @Override
    protected void notifyEnding() {
        super.notifyEnding();

        for (MarketAPI curr : source.getAffectedMarkets(system)) {
            if (curr.hasCondition(source.data.raiderActivityConditionId)) {
                curr.removeCondition(source.data.raiderActivityConditionId);
                log.info(String.format("Removing raider activity condition in [%s], market: %s", system.getName(), curr.getName()));
            }
        }
    }

    @Override
    protected void advanceImpl(float amount) {
        super.advanceImpl(amount);

        if (source.isEnding() || source.getTarget() != system) {
            endAfterDelay();
            if (DebugFlags.SEND_UPDATES_WHEN_NO_COMM || Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay()) {
                sendUpdateIfPlayerHasIntel(new Object(), false);
            }
            return;
        }

        for (MarketAPI curr : source.getAffectedMarkets(system)) {
            if (!curr.hasCondition(source.data.raiderActivityConditionId)) {
                curr.addCondition(source.data.raiderActivityConditionId, source);
                log.info(String.format("Adding raider activity condition in [%s], market: %s", system.getName(), curr.getName()));
            }
        }
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
        boolean isUpdate = getListInfoParam() != null;

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
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

        FactionAPI faction = Global.getSector().getFaction(source.data.raiderFactionId);

        info.addImage(getFactionForUIColors().getLogo(), width, 128, opad);

        info.addPara(faction.getDisplayName() + " raiders have been targeting colonies and shipping "
                + "in the " + system.getNameWithLowercaseType() + ".", opad);

        if (source.isPlayerVisible()) {
            info.addPara(faction.getDisplayNameWithArticle() + " raiders are based out of "
                    + source.getMarket().getName() + " in the " + source.getSystem().getNameWithLowercaseType() + ".", opad);
        } else {
            float distLY = Misc.getDistanceLY(system.getLocation(), source.getSystem().getLocation());
            if (source.getSystem().equals(system)) {
                info.addPara("The " + faction.getDisplayName() + " base is located in the same system as its target.", opad);
            } else if (distLY < 10) {
                info.addPara("The location of the " + faction.getDisplayName() + " base is unknown, but it's likely to be somewhere nearby.", opad);
            } else {
                info.addPara("The location of the " + faction.getDisplayName() + " base is unknown, but there are indications that it's quite distant.", opad);
            }
        }

        info.addSectionHeading("Colonies affected", getFactionForUIColors().getBaseUIColor(),
                getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);

        MarketConditionAPI condition = null;
        float initPad = opad;
        for (MarketAPI curr : source.getAffectedMarkets(system)) {
            if (condition == null) {
                condition = curr.getCondition(source.data.raiderActivityConditionId);
            }

            addMarketToList(info, curr, initPad);
            initPad = 0f;
        }

        if (condition != null) {
            MarketConditionPlugin plugin = condition.getPlugin();

            ((VayraRaiderActivityCondition) plugin).createTooltipAfterDescription(info, true);
        }

    }

    public StarSystemAPI getSystem() {
        return system;
    }

    @Override
    public String getIcon() {

        String spritePath = source.data.raiderActivityIntelIcon;
        
        SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
        // Load sprite if it hasn't been loaded yet - not needed if you add it to settings.json
        if (!loaded) {
            try {
                Global.getSettings().loadTexture(spritePath);
                loaded = true;
            } catch (IOException ex) {
                throw new RuntimeException("Failed to load sprite '" + spritePath + "'!", ex);
            }
        }

        return spritePath;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(source.data.raiderFactionId);

        if (!Misc.getMarketsInLocation(system, Factions.PLAYER).isEmpty()) {
            tags.add(Tags.INTEL_COLONIES);
        }

        return tags;
    }

    @Override
    public String getSortString() {
        String base = Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix());
        return base + " C";
    }

    public String getName() {
        String base = source.data.raiderActivityString;
        if (isEnding()) {
            return base + " - Over";
        }
        return base + " - " + system.getBaseName();
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return source.getFactionForUIColors();
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return system.getCenter();
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMinorMessage();
    }

}
