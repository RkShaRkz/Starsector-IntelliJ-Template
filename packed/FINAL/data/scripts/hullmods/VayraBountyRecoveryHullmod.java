package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;
import org.apache.log4j.Logger;

public class VayraBountyRecoveryHullmod extends BaseHullMod {

    public static Logger log = Global.getLogger(VayraBountyRecoveryHullmod.class);

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        MutableShipStatsAPI stats = ship.getMutableStats();
        FleetMemberAPI member = ship.getFleetMember();

        if (member != null && stats != null) {
            CampaignFleetAPI fleet = member.getFleetData() == null ? null : member.getFleetData().getFleet();
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (fleet != null && playerFleet != null && !fleet.equals(playerFleet)) {
                stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
                stats.getBreakProb().modifyMult(id, 0f);
                if (VAYRA_DEBUG) {
                    log.info("ship " + ship.getHullStyleId() + " in fleet " + fleet.getNameWithFaction() + " set to full recovery");
                }
            } else {
                stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).unmodify(id);
                stats.getBreakProb().unmodify(id);
                if (VAYRA_DEBUG) {
                    log.info("ship " + ship.getHullStyleId() + " in player or nonexistant fleet, recovery chance unmodified");
                }
            }
        }
    }
}
