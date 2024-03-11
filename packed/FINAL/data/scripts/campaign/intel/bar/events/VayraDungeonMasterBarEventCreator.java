package data.scripts.campaign.intel.bar.events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.DeliveryBarEventCreator;

public class VayraDungeonMasterBarEventCreator extends DeliveryBarEventCreator {

    @Override
    public PortsideBarEvent createBarEvent() {
        return new VayraDungeonMasterBarEvent();
    }
}
