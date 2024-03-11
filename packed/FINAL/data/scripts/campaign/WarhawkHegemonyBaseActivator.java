package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import static data.scripts.campaign.bases.VayraRaiderBaseManager.RAIDERS;
import org.apache.log4j.Logger;

public class WarhawkHegemonyBaseActivator implements EveryFrameScript {

    public static Logger log = Global.getLogger(WarhawkHegemonyBaseActivator.class);
    private static boolean active = false;

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
        boolean show = Global.getSector().getFaction("warhawk_republic").isShowInIntelTab();
        if (!active && show) {
            RAIDERS.get("hegemony").enabled = true;
            active = true;
        } else if (active && !show) {
            RAIDERS.get("hegemony").enabled = false;
            active = false;
        }
    }
}
