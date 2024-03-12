package data.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import java.util.Calendar;

import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;

public class VayraSunPusher implements EveryFrameScript {

    PlanetHolder jangala = null;
    IntervalUtil timer = new IntervalUtil(0.1f, 0.1f);

    public static class PlanetHolder {

        SectorEntityToken planet;
        float initialRadius;
        float initialDays;

        PlanetHolder(SectorEntityToken planet) {
            this.planet = planet;
            this.initialRadius = planet.getCircularOrbitRadius();
            this.initialDays = planet.getCircularOrbitPeriod();
        }

        public void pushInto(SectorEntityToken sun, float amount) {
            float angle = planet.getCircularOrbitAngle();
            float radius = planet.getCircularOrbitRadius();
            float days = planet.getCircularOrbitPeriod();
            radius -= amount;
            days -= Misc.interpolate(0, initialDays, radius / initialRadius);
            planet.setCircularOrbit(sun, angle, radius, days);
        }
    }

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
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (VAYRA_DEBUG || (month == 4 && day == 1)) {
            if (jangala == null) {
                // get jangala
                SectorEntityToken maybeJangala = Global.getSector().getEntityById("jangala");
                if (maybeJangala != null) {
                    jangala = new PlanetHolder(maybeJangala);
                }
            } else {
                // push it into the sun
                timer.advance(amount);
                SectorEntityToken sun = Global.getSector().getStarSystem("Corvus").getStar();
                if (sun != null) {
                    jangala.pushInto(sun, amount * (1 / timer.getIntervalDuration()));
                }
            }
        }
    }
}
