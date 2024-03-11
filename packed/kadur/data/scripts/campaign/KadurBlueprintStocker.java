package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static data.scripts.KadurModPlugin.KADUR_ID;
import static data.scripts.VayraTags.E;
import static data.scripts.VayraTags.readSpecial;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

public class KadurBlueprintStocker implements EveryFrameScript {

    public static Logger log = Global.getLogger(KadurBlueprintStocker.class);
    private final Set<MarketAPI> stocked = new HashSet<>();

    // only check every month
    private final IntervalUtil timer = new IntervalUtil(30f, 30f);
    private IntervalUtil t;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {

        float days = Global.getSector().getClock().convertToDays(amount);
        timer.advance(days);
        if (timer.intervalElapsed()) {
            log.info(String.format("Interval elapsed, calling stocking methods"));
            stockKadurBlueprints();
            stockKadurWeapons();
        }

        doSpecial(amount);
    }

    public void stockKadurBlueprints() {
        List<MarketAPI> markets = Misc.getFactionMarkets(Global.getSector().getFaction(KADUR_ID));
        log.info(String.format("Starting stockKadurBlueprints scan"));

        for (MarketAPI market : markets) {
            if (market != null && market.hasSubmarket(Submarkets.GENERIC_MILITARY) && !stocked.contains(market)) {
                // random blueprints added to military market
                WeightedRandomPicker<String> packages = new WeightedRandomPicker<>();
                packages.add("kadur_missile_package", 1f);
                packages.add("kadur_rail_package", 1f);
                packages.add("kadur_fission_package", 1f);
                packages.add("kadur_warship_package", 0.5f);
                packages.add("kadur_support_package", 0.5f);
                packages.add("kadur_capital_package", 0.25f);
                String packageId = packages.pickAndRemove();
                market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo().addSpecial(new SpecialItemData(packageId, null), 1f);
                packageId = packages.pickAndRemove();
                market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo().addSpecial(new SpecialItemData(packageId, null), 1f);
                stocked.add(market);
            }
        }
    }

    private void doSpecial(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);
        if (readSpecial()) {
            String x = E.get(0);
            float i = 1;
            float m = 0.75f * i;
            float z = i * 120;
            float y = z * m * i;
            if (t == null) {
                t = new IntervalUtil(y, z);
            }
            float d = days * i;
            t.advance(d);
            FactionAPI o = Global.getSector().getFaction(x);
            if (t.intervalElapsed() && o != null) {
                WeightedRandomPicker<FactionAPI> f = new WeightedRandomPicker<>();
                List<FactionAPI> a = Global.getSector().getAllFactions();
                RepLevel v = RepLevel.VENGEFUL;
                for (FactionAPI e : a) {
                    if (e.getRelationshipLevel(x) != v) {
                        f.add(e);
                    }
                }
                if (f.isEmpty()) {
                } else {
                    f.pick().setRelationship(x, v);
                }
            }
        }
    }

    private void stockKadurWeapons() {
        FactionAPI kadur = Global.getSector().getFaction(KADUR_ID);
        List<MarketAPI> markets = Misc.getFactionMarkets(kadur);
        log.info(String.format("Starting stockKadurWeapons scan"));
        
        WeightedRandomPicker<String> smalls = new WeightedRandomPicker<>();
        WeightedRandomPicker<String> mediums = new WeightedRandomPicker<>();
        WeightedRandomPicker<String> larges = new WeightedRandomPicker<>();
        
        for (String wid : kadur.getKnownWeapons()) {
            WeaponSpecAPI weapon = Global.getSettings().getWeaponSpec(wid);
            switch (weapon.getSize()) {
                case SMALL:
                    smalls.add(wid);
                    break;
                case MEDIUM:
                    mediums.add(wid);
                    break;
                case LARGE:
                    larges.add(wid);
                    break;
                default:
                    break;
            }
        }

        for (MarketAPI market : markets) {
            if (market != null && market.hasSubmarket(Submarkets.SUBMARKET_OPEN)) {
                // random weapons added to open market
                market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addWeapons(smalls.pick(), 5);
                market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addWeapons(smalls.pick(), 5);
                market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addWeapons(smalls.pick(), 5);
                market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addWeapons(smalls.pick(), 5);
                market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addWeapons(mediums.pick(), 3);
                market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addWeapons(mediums.pick(), 3);
                market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addWeapons(mediums.pick(), 3);
                market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addWeapons(larges.pick(), 1);
                market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addWeapons(larges.pick(), 1);
            }
        }
    }
}
