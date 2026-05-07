package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class SkillSanitizingDoctrine implements FactionDoctrineAPI {
    private static Logger logger = Global.getLogger(SkillSanitizingDoctrine.class);

    private @NotNull FactionDoctrineAPI delegate;
    private final @NotNull HashSet<String> validSkillIdSet;

    public SkillSanitizingDoctrine(@NotNull FactionDoctrineAPI originalDoctrine) {
        delegate = originalDoctrine;
        validSkillIdSet = new HashSet<>(Global.getSettings().getSkillIds());
    }

    @Override
    public int getWarships() {
        return delegate.getWarships();
    }

    @Override
    public void setWarships(int warships) {
        delegate.setWarships(warships);
    }

    @Override
    public int getCarriers() {
        return delegate.getCarriers();
    }

    @Override
    public void setCarriers(int carriers) {
        delegate.setCarriers(carriers);
    }

    @Override
    public int getPhaseShips() {
        return delegate.getPhaseShips();
    }

    @Override
    public void setPhaseShips(int phaseShips) {
        delegate.setPhaseShips(phaseShips);
    }

    @Override
    public int getOfficerQuality() {
        return delegate.getOfficerQuality();
    }

    @Override
    public void setOfficerQuality(int officerQuality) {
        delegate.setOfficerQuality(officerQuality);
    }

    @Override
    public int getShipQuality() {
        return delegate.getShipQuality();
    }

    @Override
    public void setShipQuality(int shipQuality) {
        delegate.setShipQuality(shipQuality);
    }

    @Override
    public int getNumShips() {
        return delegate.getNumShips();
    }

    @Override
    public void setNumShips(int numShips) {
        delegate.setNumShips(numShips);
    }

    @Override
    public int getShipSize() {
        return delegate.getShipSize();
    }

    @Override
    public void setShipSize(int shipSize) {
        delegate.setShipSize(shipSize);
    }

    @Override
    public int getAggression() {
        return delegate.getAggression();
    }

    @Override
    public void setAggression(int aggression) {
        delegate.setAggression(aggression);
    }

    @Override
    public int getFleets() {
        return delegate.getFleets();
    }

    @Override
    public void setFleets(int fleets) {
        delegate.setFleets(fleets);
    }

    @Override
    public float getCombatFreighterProbability() {
        return delegate.getCombatFreighterProbability();
    }

    @Override
    public void setCombatFreighterProbability(float combatFreighterProbability) {
        delegate.setCombatFreighterProbability(combatFreighterProbability);
    }

    @Override
    public float getCommanderSkillsShuffleProbability() {
        return delegate.getCommanderSkillsShuffleProbability();
    }

    @Override
    public void setCommanderSkillsShuffleProbability(float commanderSkillsShuffleProbability) {
        delegate.setCommanderSkillsShuffleProbability(commanderSkillsShuffleProbability);
    }

    @Override
    public List<String> getCommanderSkills() {
        List<String> skills = delegate.getCommanderSkills();
        sanitize(skills);
        return skills;
    }

    @Override
    public float getShipQualityContribution() {
        return delegate.getShipQualityContribution();
    }

    @Override
    public FactionDoctrineAPI clone() {
        return new SkillSanitizingDoctrine(delegate.clone());
    }

    @Override
    public float getCombatFreighterCombatUseFraction() {
        return delegate.getCombatFreighterCombatUseFraction();
    }

    @Override
    public void setCombatFreighterCombatUseFraction(float combatFreighterCombatUseFraction) {
        delegate.setCombatFreighterCombatUseFraction(combatFreighterCombatUseFraction);
    }

    @Override
    public float getCombatFreighterCombatUseFractionWhenPriority() {
        return delegate.getCombatFreighterCombatUseFractionWhenPriority();
    }

    @Override
    public void setCombatFreighterCombatUseFractionWhenPriority(float combatFreighterCombatUseFractionWhenPriority) {
        delegate.setCombatFreighterCombatUseFractionWhenPriority(combatFreighterCombatUseFractionWhenPriority);
    }

    @Override
    public float getAutofitRandomizeProbability() {
        return delegate.getAutofitRandomizeProbability();
    }

    @Override
    public void setAutofitRandomizeProbability(float autofitRandomizeProbability) {
        delegate.setAutofitRandomizeProbability(autofitRandomizeProbability);
    }

    @Override
    public int getTotalStrengthPoints() {
        return delegate.getTotalStrengthPoints();
    }

    @Override
    public boolean isStrictComposition() {
        return delegate.isStrictComposition();
    }

    @Override
    public void setStrictComposition(boolean strictComposition) {
        delegate.setStrictComposition(strictComposition);
    }

    @Override
    public float getOfficerSkillsShuffleProbability() {
        return delegate.getOfficerSkillsShuffleProbability();
    }

    @Override
    public List<String> getOfficerSkills() {
        List<String> skills = delegate.getOfficerSkills();
        sanitize(skills);
        return skills;
    }

    // Filter out skill IDs that do not exist in the current game version
    private void sanitize(List<String> skillIds) {
        Iterator<String> iter = skillIds.iterator();
        while (iter.hasNext()) {
            String id = iter.next();
            // Do two checks:
            // 1. check against the set containing valid skill IDs
            // 2. try fetching the skill spec of the skill ID
            // If either of these two fail, consider the skill invalid
            boolean hashSetContainsId = false;
            boolean skillSpecIsNonNull = false;
            boolean exceptionHappened = false;
            try {
                hashSetContainsId = validSkillIdSet.contains(id);
                skillSpecIsNonNull = Global.getSettings().getSkillSpec(id) != null;
            } catch (NullPointerException npe) {
                // if we caught a NPE that means the skill surely isn't valid, so remove it
                exceptionHappened = true;
                iter.remove();
                logger.error("NullPointerException happened while testing skill: " + id + ", removing it!", npe);
            } finally {
                // If no exception happened, do NOT remove skill only if both are true,
                // if exception did happen - we already removed it, so do nothing
                boolean combinedCheck = hashSetContainsId && skillSpecIsNonNull;
                if (!exceptionHappened) {
                    // no exception happened, check combinedCheck
                    if (combinedCheck) {
                        // combined check passes, do nothing
                    } else {
                        // combinedCheck is failing, remove
                        iter.remove();
                        logger.warn("Combined check failed while testing skill: " + id + ", removing it!");
                    }
                } else {
                    // exception happened, something caused a NPE while checking the skill, the catch() block already
                    // removed the skill - do nothing and carry on.
                }
            }
        }
    }
}
