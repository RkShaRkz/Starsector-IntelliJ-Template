package data.domain;

import com.fs.starfarer.api.impl.campaign.shared.PersonBountyEventData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Repository for streamlining access to {@link SharedData#getPersonBountyEventData()}
 */
public class PersonBountyEventDataRepository {

    private List<String> factionsCOWlist;

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

    /**
     * Synchronized wrapper around {@link SharedData#getData()} and subsequent {@link SharedData#getPersonBountyEventData()}
     * @return the underlying {@link PersonBountyEventData}
     */
    public synchronized PersonBountyEventData getPersonBountyEventData() {
        return SharedData.getData().getPersonBountyEventData();
    }

    /**
     * Replacement for {@link SharedData#getPersonBountyEventData()} and subsequent {@link PersonBountyEventData#getParticipatingFactions()}
     * underlying list
     * @return unmodifiable CopyOnWriteArrayList wrapping the underlying list provided by the game
     */
    public synchronized List<String> getParticipatingFactions() {

        if (factionsCOWlist == null) {
            instantiateInternalCOWList();
        }

        if (!underlyingListHasSameContents()) {
            instantiateInternalCOWList();
        }

        return factionsCOWlist;
    }

    /**
     * Method that instantiates a new unmodifiable {@link CopyOnWriteArrayList} holding all participating bounty event factions.
     * It's unmodifyable to make you not use it's iterator and remove stuff while iterating over it, but rather
     * use a different removal technique through this <i>Repository</i>
     */
    private synchronized void instantiateInternalCOWList() {
        factionsCOWlist = Collections.unmodifiableList(new CopyOnWriteArrayList<>(getPersonBountyEventData().getParticipatingFactions()));
    }

    /**
     * Method for checking whether the repository's internal list needs to be regenerated or not, by checking if both of them
     * contain all of the other list's elements
     * @return whether the repository's internal list contains all backing list's items and whether backing list contains all repository's list items
     */
    private synchronized boolean underlyingListHasSameContents() {
        // Check whether underlying list has all external list's contents and vice-versa
        List<String> backingList = getPersonBountyEventData().getParticipatingFactions();
        HashSet<String> backingHashSet = new HashSet<>(backingList);
        HashSet<String> ourListHashSet = new HashSet<>(factionsCOWlist);
        return backingHashSet.containsAll(factionsCOWlist) && ourListHashSet.containsAll(backingList);
    }

    /**
     * Shortcut for {@link PersonBountyEventData#addParticipatingFaction(String)}
     * @param factionId which faction ID to add to participating factions
     */
    public synchronized void addParticipatingFaction(String factionId) {
        getPersonBountyEventData().addParticipatingFaction(factionId);
    }

    /**
     * Shortcut for {@link PersonBountyEventData#removeParticipatingFaction(String)}
     * @param factionId which faction ID to remove from participating factions
     */
    public synchronized void removeParticipatingFaction(String factionId) {
        getPersonBountyEventData().removeParticipatingFaction(factionId);
    }
}
