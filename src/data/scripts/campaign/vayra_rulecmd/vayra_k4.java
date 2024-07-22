package data.scripts.campaign.vayra_rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;


public class vayra_k4 extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
                 if (params.size() > 0) {Misc.fadeAndExpire(dialog.getInteractionTarget());return true;}
                ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                ShipVariantAPI variant = Global.getSettings().getVariant("vayra_targe_crystal").clone();
                data.addShip(new ShipRecoverySpecial.PerShipData(variant, ShipRecoverySpecial.ShipCondition.GOOD, "KHS Ceto", Factions.HEGEMONY, 0f));
                Misc.setSalvageSpecial(dialog.getInteractionTarget(), data);
                return true;
	}
}

