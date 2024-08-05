package data.domain;

import com.fs.starfarer.api.impl.campaign.shared.PersonBountyEventData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class PersonBountyEventDataRepository {

    private static volatile PersonBountyEventDataRepository instance;

    // Private constructor to prevent instantiation
    private PersonBountyEventDataRepository() {
    }

    // Double-checked locking for thread-safe singleton instance
    public static PersonBountyEventDataRepository getInstance() {
        if (instance == null) {
            synchronized (PersonBountyEventDataRepository.class) {
                if (instance == null) {
                    instance = new PersonBountyEventDataRepository();
                }
            }
        }
        return instance;
    }

    public synchronized PersonBountyEventData getPersonBountyEventData() {
        return SharedData.getData().getPersonBountyEventData();
    }

    public synchronized List<String> getParticipatingFactions() {
        return Collections.synchronizedList(getPersonBountyEventData().getParticipatingFactions());
    }

    // Adds an item to the list
    public synchronized void addParticipatingFaction(String factionId) {
        getPersonBountyEventData().addParticipatingFaction(factionId);
    }

    // Removes an item from the list
    public synchronized void removeParticipatingFaction(String factionId) {
        getPersonBountyEventData().removeParticipatingFaction(factionId);
    }
}
