package data.script;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.skills.*;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class ConstraintChangerModPlugin extends BaseModPlugin {

    public static Logger logger = Global.getLogger(ConstraintChangerModPlugin.class);

    public static final boolean HAVE_LUNALIB = Global.getSettings().getModManager().isModEnabled("lunalib");
    public static final String MOD_ID = "Shark_ConstraintChanger";

    /**************************
     * OFFICER LUNAKEYS BELOW *
     **************************/

    public static final String FIELD_MAX_OFFICER_COUNT = "constraintchanger_officerNumber";
    public static final String FIELD_OFFICER_MAX_LEVEL = "constraintchanger_officerMaxLevel";
    public static final String FIELD_MAX_ELITE_SKILLS = "constraintchanger_officerMaxEliteSkills";
    public static final String FIELD_MAX_AI_OFFICER_COUNT = "constraintchanger_officerAIMax";
    public static final String FIELD_MAX_OFFICERS_IN_AI_FLEET = "constraintchanger_maxOfficersInAIFleet";
    public static final String FIELD_MERC_OFFICER_MIN_LEVEL = "constraintchanger_officerMercMinLevel";
    public static final String FIELD_MERC_OFFICER_MAX_LEVEL = "constraintchanger_officerMercMaxLevel";
    public static final String FIELD_MERC_OFFICER_PAY_MULT = "constraintchanger_officerMercPayMult";
    public static final String FIELD_MERC_OFFICER_CONTRACT_DURATION = "constraintchanger_officerMercContractDur";

    /*************************
     * COMBAT LUNAKEYS BELOW *
     *************************/

    public static final String FIELD_MAX_SHIPS_IN_FLEET = "constraintchanger_maxShipsInFleet";
    public static final String FIELD_MAX_SHIPS_IN_AI_FLEET = "constraintchanger_maxShipsInAIFleet";
    public static final String FIELD_MIN_BATTLE_SIZE = "constraintchanger_minBattleSize";
    public static final String FIELD_DEFAULT_BATTLE_SIZE = "constraintchanger_defaultBattleSize";
    public static final String FIELD_MAX_BATTLE_SIZE = "constraintchanger_maxBattleSize";

    /***********************
     * MISC LUNAKEYS BELOW *
     ***********************/

    public static final String FIELD_CAMPAIGN_SPEEDUP_MULT = "constraintchanger_campaignSpeedupMult";


    public static final String FIELD_MIN_COMBAT_ZOOM = "constraintchanger_minCombatZoom";
    public static final String FIELD_MAX_COMBAT_ZOOM = "constraintchanger_maxCombatZoom";
    public static final String FIELD_MIN_CAMPAIGN_ZOOM = "constraintchanger_minCampaignZoom";
    public static final String FIELD_MAX_CAMPAIGN_ZOOM = "constraintchanger_maxCampaignZoom";

    /************************
     * SKILL LUNAKEYS BELOW *
     ************************/

    /**************************
     * LEADERSHIP SKILLS FIELDS *
     **************************/

    public static final String FIELD_TACTICAL_DRILLS_OP_THRESHOLD = "constraintchanger_skill_leadership_TacticalDrills_OPThreshold";
    public static final String FIELD_TACTICAL_DRILLS_DAMAGE_PERCENT = "constraintchanger_skill_leadership_TacticalDrills_DamagePercent";
    public static final String FIELD_TACTICAL_DRILLS_ATTACK_BONUS = "constraintchanger_skill_leadership_TacticalDrills_AttackBonus";
    public static final String FIELD_TACTICAL_DRILLS_CASUALTIES_MULTIPLIER = "constraintchanger_skill_leadership_TacticalDrills_CasualtiesMultiplier";

    public static final String FIELD_CREW_TRAINING_PEAK_SECONDS = "constraintchanger_skill_leadership_CrewTraining_PeakSeconds";
    public static final String FIELD_CREW_TRAINING_CR_PERCENT = "constraintchanger_skill_leadership_CrewTraining_CRPercent";

    public static final String FIELD_FIGHTER_UPLINK_MAX_SPEED_PERCENT = "constraintchanger_skill_leadership_FighterUplink_MaxSpeedPercent";
    public static final String FIELD_FIGHTER_UPLINK_CREW_LOSS_PERCENT = "constraintchanger_skill_leadership_FighterUplink_CrewLossPercent";
    public static final String FIELD_FIGHTER_UPLINK_TARGET_LEADING_BONUS = "constraintchanger_skill_leadership_FighterUplink_TargetLeadingBonus";
    public static final String FIELD_FIGHTER_UPLINK_OFFICER_MULT = "constraintchanger_skill_leadership_FighterUplink_OfficerMultiplier";

    /**************************
     * TECHNOLOGY SKILLS FIELDS *
     **************************/
    public static final String FIELD_PHASE_COIL_TUNING_OP_THRESHOLD = "constraintchanger_skill_technology_PhaseCoilTuning_OPThreshold";
    public static final String FIELD_PHASE_COIL_SPEED_BONUS = "constraintchanger_skill_technology_PhaseCoilTuning_SpeedBonus";
    public static final String FIELD_PHASE_COIL_PEAK_TIME_BONUS = "constraintchanger_skill_technology_PhaseCoilTuning_PeakTimeBonus";
    public static final String FIELD_PHASE_COIL_SENSOR_BONUS_PERCENT = "constraintchanger_skill_technology_PhaseCoilTuning_SensorBonusPercent";

    public static final String FIELD_USE_DYNAMIC_AUTOMATED_SHIPS_OP_THRESHOLD = "constraintchanger_skill_technology_AutomatedShips_useDynamicOpThreshold";
    public static final String FIELD_AUTOMATED_SHIPS_OP_THRESHOLD = "constraintchanger_skill_technology_AutomatedShips_OPThreshold";

    public static final String FIELD_DERELICT_OPERATIONS_DP_COST_REDUCTION_PER_DMOD = "constraintchanger_skill_technology_DerelictOperations_DPCostReductionPerDmod";

    /**************************
     * INDUSTRY SKILLS FIELDS *
     **************************/

    public static final String FIELD_BULK_TRANSPORT_CARGO_CAPACITY_MAX_PERCENTAGE = "constraintchanger_skill_industry_BulkTransport_CargoCapacityMaxPercent";
    public static final String FIELD_BULK_TRANSPORT_CARGO_CAPACITY_THRESHOLD = "constraintchanger_skill_industry_BulkTransport_CargoCapacityThreshold";
    public static final String FIELD_BULK_TRANSPORT_FUEL_CAPACITY_MAX_PERCENTAGE = "constraintchanger_skill_industry_BulkTransport_FuelCapacityMaxPercent";
    public static final String FIELD_BULK_TRANSPORT_FUEL_CAPACITY_THRESHOLD = "constraintchanger_skill_industry_BulkTransport_FuelCapacityThreshold";
    public static final String FIELD_BULK_TRANSPORT_PERSONNEL_CAPACITY_MAX_PERCENTAGE = "constraintchanger_skill_industry_BulkTransport_PersonnelCapacityMaxPercent";
    public static final String FIELD_BULK_TRANSPORT_PERSONNEL_CAPACITY_THRESHOLD = "constraintchanger_skill_industry_BulkTransport_PersonnelCapacityThreshold";
    public static final String FIELD_BULK_TRANSPORT_BURN_BONUS = "constraintchanger_skill_industry_BulkTransport_BurnBonus";

    public static final String FIELD_FIELD_REPAIRS_MIN_HULL = "constraintchanger_skill_industry_FieldRepairs_MinHull";
    public static final String FIELD_FIELD_REPAIRS_MAX_HULL = "constraintchanger_skill_industry_FieldRepairs_MaxHull";
    public static final String FIELD_FIELD_REPAIRS_MIN_CR = "constraintchanger_skill_industry_FieldRepairs_MinCR";
    public static final String FIELD_FIELD_REPAIRS_MAX_CR = "constraintchanger_skill_industry_FieldRepairs_MaxCR";
    public static final String FIELD_FIELD_REPAIRS_REPAIR_RATE_BONUS = "constraintchanger_skill_industry_FieldRepairs_RepairRateBonus";
    public static final String FIELD_FIELD_REPAIRS_INSTA_REPAIR_PERCENT = "constraintchanger_skill_industry_FieldRepairs_InstaRepairPercent";

    public static final String FIELD_CONTAINMENT_PROCEDURES_CREW_LOSS_REDUCTION = "constraintchanger_skill_industry_ContainmentProcedures_CrewLossReduction";
    public static final String FIELD_CONTAINMENT_PROCEDURES_FUEL_SALVAGE_BONUS = "constraintchanger_skill_industry_ContainmentProcedures_FuelSalvageBonus";
    public static final String FIELD_CONTAINMENT_PROCEDURES_FUEL_USE_REDUCTION_MAX_PERCENT = "constraintchanger_skill_industry_ContainmentProcedures_FuelUseReductionMaxPercent";
    public static final String FIELD_CONTAINMENT_PROCEDURES_FUEL_USE_REDUCTION_MAX_FUEL = "constraintchanger_skill_industry_ContainmentProcedures_FuelUseReductionMaxFuel";


    /************************
     * SHARED SKILLS FIELDS *
     ************************/

    public static final String FIELD_SHARED_CREW_TRAINING_AND_FLUX_REGULATION_OP_THRESHOLD = "constraintchanger_skill_shared_OPThreshold";
    public static final String FIELD_SHARED_FIGHTER_BAYS = "constraintchanger_skill_shared_FighterBays";
    public static final String FIELD_SHARED_FIELD_REPAIRS_AND_CONTAINMENT_PROCEDURES_OP_THRESHOLD = "constraintchanger_skill_shared_OPThreshold2";

    /**
     * Map used to map LunaSettings keys to their actual starsector-core/data/config/settings.json keys
     */
    private static final HashMap<String, String> LunaToRealKeymap = new HashMap<>();

    static {
        // Officers
        LunaToRealKeymap.put(FIELD_MAX_OFFICER_COUNT, "baseNumOfficers");
        LunaToRealKeymap.put(FIELD_OFFICER_MAX_LEVEL, "officerMaxLevel");
        LunaToRealKeymap.put(FIELD_MAX_ELITE_SKILLS, "officerMaxEliteSkills");
        LunaToRealKeymap.put(FIELD_MAX_AI_OFFICER_COUNT, "officerAIMax");
        LunaToRealKeymap.put(FIELD_MAX_OFFICERS_IN_AI_FLEET, "maxOfficersInAIFleet");
        LunaToRealKeymap.put(FIELD_MERC_OFFICER_MIN_LEVEL, "officerMercMinLevel");
        LunaToRealKeymap.put(FIELD_MERC_OFFICER_MAX_LEVEL, "officerMercMaxLevel");
        LunaToRealKeymap.put(FIELD_MERC_OFFICER_PAY_MULT, "officerMercPayMult");
        LunaToRealKeymap.put(FIELD_MERC_OFFICER_CONTRACT_DURATION, "officerMercContractDur");
        // Combat
        LunaToRealKeymap.put(FIELD_MAX_SHIPS_IN_FLEET, "maxShipsInFleet");
        LunaToRealKeymap.put(FIELD_MAX_SHIPS_IN_AI_FLEET, "maxShipsInAIFleet");
        LunaToRealKeymap.put(FIELD_MIN_BATTLE_SIZE, "minBattleSize");
        LunaToRealKeymap.put(FIELD_DEFAULT_BATTLE_SIZE, "defaultBattleSize");
        LunaToRealKeymap.put(FIELD_MAX_BATTLE_SIZE, "maxBattleSize");
        // Misc
        LunaToRealKeymap.put(FIELD_CAMPAIGN_SPEEDUP_MULT, "campaignSpeedupMult");
        LunaToRealKeymap.put(FIELD_MIN_COMBAT_ZOOM, "minCombatZoom");
        LunaToRealKeymap.put(FIELD_MAX_COMBAT_ZOOM, "maxCombatZoom");
        LunaToRealKeymap.put(FIELD_MIN_CAMPAIGN_ZOOM, "minCampaignZoom");
        LunaToRealKeymap.put(FIELD_MAX_CAMPAIGN_ZOOM, "maxCampaignZoom");
        // Skills
        // Leadership
        LunaToRealKeymap.put(FIELD_TACTICAL_DRILLS_OP_THRESHOLD, "RKZ_skill_leadership_TacticalDrills_OPThreshold");
        LunaToRealKeymap.put(FIELD_TACTICAL_DRILLS_DAMAGE_PERCENT, "RKZ_skill_leadership_TacticalDrills_DamagePercent");
        LunaToRealKeymap.put(FIELD_TACTICAL_DRILLS_ATTACK_BONUS, "RKZ_skill_leadership_TacticalDrills_AttackBonus");
        LunaToRealKeymap.put(FIELD_TACTICAL_DRILLS_CASUALTIES_MULTIPLIER, "RKZ_skill_leadership_TacticalDrills_CasualtiesMultiplier");

        LunaToRealKeymap.put(FIELD_CREW_TRAINING_PEAK_SECONDS, "RKZ_skill_leadership_CrewTraining_PeakSeconds");
        LunaToRealKeymap.put(FIELD_CREW_TRAINING_CR_PERCENT, "RKZ_skill_leadership_CrewTraining_CRPercent");

        LunaToRealKeymap.put(FIELD_FIGHTER_UPLINK_MAX_SPEED_PERCENT, "RKZ_skill_leadership_FighterUplink_MaxSpeedPercent");
        LunaToRealKeymap.put(FIELD_FIGHTER_UPLINK_CREW_LOSS_PERCENT, "RKZ_skill_leadership_FighterUplink_CrewLossPercent");
        LunaToRealKeymap.put(FIELD_FIGHTER_UPLINK_TARGET_LEADING_BONUS, "RKZ_skill_leadership_FighterUplink_TargetLeadingBonus");
        LunaToRealKeymap.put(FIELD_FIGHTER_UPLINK_OFFICER_MULT, "RKZ_skill_leadership_FighterUplink_OfficerMultiplier");

        // Technology
        LunaToRealKeymap.put(FIELD_PHASE_COIL_TUNING_OP_THRESHOLD, "RKZ_skill_technology_PhaseCoilTuning_OPThreshold");
        LunaToRealKeymap.put(FIELD_PHASE_COIL_SPEED_BONUS, "RKZ_skill_technology_PhaseCoilTuning_SpeedBonus");
        LunaToRealKeymap.put(FIELD_PHASE_COIL_PEAK_TIME_BONUS, "RKZ_skill_technology_PhaseCoilTuning_PeakTimeBonus");
        LunaToRealKeymap.put(FIELD_PHASE_COIL_SENSOR_BONUS_PERCENT, "RKZ_skill_technology_PhaseCoilTuning_SensorBonusPercent");

        LunaToRealKeymap.put(FIELD_USE_DYNAMIC_AUTOMATED_SHIPS_OP_THRESHOLD, "RKZ_skill_technology_AutomatedShips_useDynamicOpThreshold");
        LunaToRealKeymap.put(FIELD_AUTOMATED_SHIPS_OP_THRESHOLD, "RKZ_skill_technology_AutomatedShips_OPThreshold");
        // Industry
        LunaToRealKeymap.put(FIELD_BULK_TRANSPORT_CARGO_CAPACITY_MAX_PERCENTAGE, "RKZ_skill_industry_BulkTransport_CargoCapacityMaxPercent");
        LunaToRealKeymap.put(FIELD_BULK_TRANSPORT_CARGO_CAPACITY_THRESHOLD, "RKZ_skill_industry_BulkTransport_CargoCapacityThreshold");
        LunaToRealKeymap.put(FIELD_BULK_TRANSPORT_FUEL_CAPACITY_MAX_PERCENTAGE, "RKZ_skill_industry_BulkTransport_FuelCapacityMaxPercent");
        LunaToRealKeymap.put(FIELD_BULK_TRANSPORT_FUEL_CAPACITY_THRESHOLD, "RKZ_skill_industry_BulkTransport_FuelCapacityThreshold");
        LunaToRealKeymap.put(FIELD_BULK_TRANSPORT_PERSONNEL_CAPACITY_MAX_PERCENTAGE, "RKZ_skill_industry_BulkTransport_PersonnelCapacityMaxPercent");
        LunaToRealKeymap.put(FIELD_BULK_TRANSPORT_PERSONNEL_CAPACITY_THRESHOLD, "RKZ_skill_industry_BulkTransport_PersonnelCapacityThreshold");
        LunaToRealKeymap.put(FIELD_BULK_TRANSPORT_BURN_BONUS, "RKZ_skill_industry_BulkTransport_BurnBonus");

        LunaToRealKeymap.put(FIELD_FIELD_REPAIRS_MIN_HULL, "RKZ_skill_industry_FieldRepairs_MinHull");
        LunaToRealKeymap.put(FIELD_FIELD_REPAIRS_MAX_HULL, "RKZ_skill_industry_FieldRepairs_MaxHull");
        LunaToRealKeymap.put(FIELD_FIELD_REPAIRS_MIN_CR, "RKZ_skill_industry_FieldRepairs_MinCR");
        LunaToRealKeymap.put(FIELD_FIELD_REPAIRS_MAX_CR, "RKZ_skill_industry_FieldRepairs_MaxCR");
        LunaToRealKeymap.put(FIELD_FIELD_REPAIRS_REPAIR_RATE_BONUS, "RKZ_skill_industry_FieldRepairs_RepairRateBonus");
        LunaToRealKeymap.put(FIELD_FIELD_REPAIRS_INSTA_REPAIR_PERCENT, "RKZ_skill_industry_FieldRepairs_InstaRepairPercent");

        LunaToRealKeymap.put(FIELD_CONTAINMENT_PROCEDURES_CREW_LOSS_REDUCTION, "RKZ_skill_industry_ContainmentProcedures_CrewLossReduction");
        LunaToRealKeymap.put(FIELD_CONTAINMENT_PROCEDURES_FUEL_SALVAGE_BONUS, "RKZ_skill_industry_ContainmentProcedures_FuelSalvageBonus");
        LunaToRealKeymap.put(FIELD_CONTAINMENT_PROCEDURES_FUEL_USE_REDUCTION_MAX_PERCENT, "RKZ_skill_industry_ContainmentProcedures_FuelUseReductionMaxPercent");
        LunaToRealKeymap.put(FIELD_CONTAINMENT_PROCEDURES_FUEL_USE_REDUCTION_MAX_FUEL, "RKZ_skill_industry_ContainmentProcedures_FuelUseReductionMaxFuel");

        LunaToRealKeymap.put(FIELD_DERELICT_OPERATIONS_DP_COST_REDUCTION_PER_DMOD, "RKZ_skill_industry_DerelictOperations_DPCostReductionPerDmod");

        // Shared
        LunaToRealKeymap.put(FIELD_SHARED_FIELD_REPAIRS_AND_CONTAINMENT_PROCEDURES_OP_THRESHOLD, "RKZ_skill_industry_FieldRepairs_and_ContainmentProcedures_OPThreshold");
        LunaToRealKeymap.put(FIELD_SHARED_CREW_TRAINING_AND_FLUX_REGULATION_OP_THRESHOLD, "RKZ_skill_leadership_CrewTraining_and_FluxRegulation_OPThreshold");
        LunaToRealKeymap.put(FIELD_SHARED_FIGHTER_BAYS, "RKZ_skill_leadership_FighterUplink_and_CarrierGroup_FighterBays");

    }

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

//        logger.info(">>>> Settings JSON: \n" + Global.getSettings().getSettingsJSON());

        if (HAVE_LUNALIB) {
            LunaSettings.addSettingsListener(new MyLunaSettinsListener());
        }
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        // Add your code here, or delete this method (it does nothing unless you add code)
    }

    /**
     * The {@link LunaSettingsListener} used for updating the backing settings whenever something is changed
     * in Luna that has to do with our mod.
     *
     * Then, the corresponding starsector-core's settings.json keys are also updated (overwritten) with the new values.
     */
    private static class MyLunaSettinsListener implements LunaSettingsListener {

        @Override
        public void settingsChanged(@NotNull String modId) {
            if (modId.equalsIgnoreCase(MOD_ID)) {
                // Officers
                handleOfficerTabFields();
                // Combat
                handleCombatTabFields();
                // Misc
                handleMiscTabFields();
                // Skills
                handleSkillsTabFields();
            }
        }

        private void handleOfficerTabFields() {
            writeLunaSettingToRealSetting(FIELD_OFFICER_MAX_LEVEL);
            writeLunaSettingToRealSetting(FIELD_MAX_OFFICER_COUNT);
            writeLunaSettingToRealSetting(FIELD_MAX_ELITE_SKILLS);
            writeLunaSettingToRealSetting(FIELD_MAX_AI_OFFICER_COUNT);
            writeLunaSettingToRealSetting(FIELD_MAX_OFFICERS_IN_AI_FLEET);
            writeLunaSettingToRealSetting(FIELD_MERC_OFFICER_MIN_LEVEL);
            writeLunaSettingToRealSetting(FIELD_MERC_OFFICER_MAX_LEVEL);
            writeLunaSettingToRealSetting(FIELD_MERC_OFFICER_PAY_MULT);
            writeLunaSettingToRealSetting(FIELD_MERC_OFFICER_CONTRACT_DURATION);
        }

        private void handleCombatTabFields() {
            writeLunaSettingToRealSetting(FIELD_MAX_SHIPS_IN_FLEET);
            writeLunaSettingToRealSetting(FIELD_MAX_SHIPS_IN_AI_FLEET);
            writeLunaSettingToRealSetting(FIELD_MIN_BATTLE_SIZE);
            writeLunaSettingToRealSetting(FIELD_DEFAULT_BATTLE_SIZE);
            writeLunaSettingToRealSetting(FIELD_MAX_BATTLE_SIZE);
        }

        private void handleMiscTabFields() {
            writeLunaSettingToRealSetting(FIELD_CAMPAIGN_SPEEDUP_MULT);

            writeLunaSettingToRealSetting(FIELD_MIN_COMBAT_ZOOM);
            writeLunaSettingToRealSetting(FIELD_MAX_COMBAT_ZOOM);
            writeLunaSettingToRealSetting(FIELD_MIN_CAMPAIGN_ZOOM);
            writeLunaSettingToRealSetting(FIELD_MAX_CAMPAIGN_ZOOM);
        }

        private void handleSkillsTabFields() {
            // Combat
            handleSkillsTabCombatSkillsFields();

            // Leadership
            handleSkillsTabLeadershipSkillsFields();

            // Technology
            handleSkillsTabTechnologySkillsFields();

            // Industry
            handleSkillsTabIndustrySkillsFields();
        }

        private void handleSkillsTabCombatSkillsFields() {
            //Helmsmanship
            //CombatEndurance
            //ImpactMitigation
            //DamageControl
            //FieldModulation
            //PointDefense
            //TargetAnalysis
            //BallisticMastery
            //SystemsExpertise
            //MissileSpecialization
        }

        private void handleSkillsTabLeadershipSkillsFields() {
            //TacticalDrills
            handleTacticalDrills();
            //CoordinatedManeuvers
            //WolfpackTactics
            //CrewTraining
            handleCrewTraining();
            //CarrierGroup
            //Fighter Uplink
            handleFighterUplink();
            //OfficerTraining
            //OfficerManagement
            //BestOfTheBest
            //SupportDoctrine
        }

        private void handleSkillsTabTechnologySkillsFields() {
            //Navigation
            //Sensors
            //GunneryImplants
            //EnergyWeaponMastery
            //ElectronicWarfare
            //FluxRegulation
            //PhaseCoil
            handlePhaseCoil();
            //NeuralLink - nope, will remain two ships only
            //CyberneticAugmentation
            //AutomatedShips
            handleAutomatedShips();
        }

        private void handleSkillsTabIndustrySkillsFields() {
            //BulkTransport
            handleBulkTransport();
            //Salvaging
            //FieldRepairs
            handleFieldRepairs();
            //OrdnanceExpertise
            //PolarizedArmor
            //ContainmentProcedures
            handleContainmentProcedures();
            //MakeshiftEquipment
            //IndustrialPlanning
            //HullRestoration
            //DerelictOperations
            handleDerelictOperations();
        }

        private void handleTacticalDrills() {
            BaseSkillEffectDescription.OP_LOW_THRESHOLD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_TACTICAL_DRILLS_OP_THRESHOLD));

            writeLunaSettingToRealSetting(FIELD_TACTICAL_DRILLS_OP_THRESHOLD);
        }

        private void handleCrewTraining() {
            BaseSkillEffectDescription.OP_THRESHOLD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_SHARED_CREW_TRAINING_AND_FLUX_REGULATION_OP_THRESHOLD));
            CrewTraining.CR_PERCENT = safeUnboxing(LunaSettings.getFloat(MOD_ID, FIELD_CREW_TRAINING_CR_PERCENT));
            CrewTraining.PEAK_SECONDS = safeUnboxing(LunaSettings.getFloat(MOD_ID, FIELD_CREW_TRAINING_PEAK_SECONDS));

            writeLunaSettingToRealSetting(FIELD_SHARED_CREW_TRAINING_AND_FLUX_REGULATION_OP_THRESHOLD);
            writeLunaSettingToRealSetting(FIELD_CREW_TRAINING_CR_PERCENT);
            writeLunaSettingToRealSetting(FIELD_CREW_TRAINING_PEAK_SECONDS);
        }

        private void handleFighterUplink() {
            BaseSkillEffectDescription.FIGHTER_BAYS_THRESHOLD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_SHARED_FIGHTER_BAYS));
            FighterUplink.MAX_SPEED_PERCENT = safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIGHTER_UPLINK_MAX_SPEED_PERCENT));
            FighterUplink.CREW_LOSS_PERCENT = safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIGHTER_UPLINK_CREW_LOSS_PERCENT));
            FighterUplink.TARGET_LEADING_BONUS = safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIGHTER_UPLINK_TARGET_LEADING_BONUS));
            FighterUplink.OFFICER_MULT = safeUnboxing(LunaSettings.getFloat(MOD_ID, FIELD_FIGHTER_UPLINK_OFFICER_MULT));

            writeLunaSettingToRealSetting(FIELD_SHARED_FIGHTER_BAYS);
            writeLunaSettingToRealSetting(FIELD_FIGHTER_UPLINK_MAX_SPEED_PERCENT);
            writeLunaSettingToRealSetting(FIELD_FIGHTER_UPLINK_CREW_LOSS_PERCENT);
            writeLunaSettingToRealSetting(FIELD_FIGHTER_UPLINK_TARGET_LEADING_BONUS);
            writeLunaSettingToRealSetting(FIELD_FIGHTER_UPLINK_OFFICER_MULT);
        }

        private void handlePhaseCoil() {
            BaseSkillEffectDescription.PHASE_OP_THRESHOLD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_PHASE_COIL_TUNING_OP_THRESHOLD));
            PhaseCorps.PHASE_SPEED_BONUS = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_PHASE_COIL_SPEED_BONUS));
            PhaseCorps.PEAK_TIME_BONUS = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_PHASE_COIL_PEAK_TIME_BONUS));
            PhaseCorps.PHASE_SHIP_SENSOR_BONUS_PERCENT = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_PHASE_COIL_SENSOR_BONUS_PERCENT));

            writeLunaSettingToRealSetting(FIELD_PHASE_COIL_TUNING_OP_THRESHOLD);
            writeLunaSettingToRealSetting(FIELD_PHASE_COIL_SPEED_BONUS);
            writeLunaSettingToRealSetting(FIELD_PHASE_COIL_PEAK_TIME_BONUS);
            writeLunaSettingToRealSetting(FIELD_PHASE_COIL_SENSOR_BONUS_PERCENT);
        }

        private void handleAutomatedShips() {
            // Automated ships need a bit more love
            // Since these two "real" settings are actually made up, we don't even need to save both of them
            // figure out whether we're using dynamic, save both of the made up settings, but only save what really matters
            // to the real thing controlling the autoships threshold - into BaseSkillEffectDescription
            boolean useDynamicAutoshipOP = safeUnboxing(LunaSettings.getBoolean(MOD_ID, FIELD_USE_DYNAMIC_AUTOMATED_SHIPS_OP_THRESHOLD));
            writeLunaSettingToRealSetting(FIELD_USE_DYNAMIC_AUTOMATED_SHIPS_OP_THRESHOLD);
            writeLunaSettingToRealSetting(FIELD_AUTOMATED_SHIPS_OP_THRESHOLD);
            if (useDynamicAutoshipOP) {
                // get default battle size, use 40% of that for automated points OP
                int defaultBattleSize = safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_DEFAULT_BATTLE_SIZE));
                BaseSkillEffectDescription.AUTOMATED_POINTS_THRESHOLD = Math.round((defaultBattleSize * 4) / 10f);
            } else {
                BaseSkillEffectDescription.AUTOMATED_POINTS_THRESHOLD = safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_AUTOMATED_SHIPS_OP_THRESHOLD));
            }
        }

        private void handleBulkTransport() {
            BulkTransport.CARGO_CAPACITY_MAX_PERCENT = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_BULK_TRANSPORT_CARGO_CAPACITY_MAX_PERCENTAGE));
            BulkTransport.CARGO_CAPACITY_THRESHOLD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_BULK_TRANSPORT_CARGO_CAPACITY_THRESHOLD));
            BulkTransport.FUEL_CAPACITY_MAX_PERCENT = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_BULK_TRANSPORT_FUEL_CAPACITY_MAX_PERCENTAGE));
            BulkTransport.FUEL_CAPACITY_THRESHOLD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_BULK_TRANSPORT_FUEL_CAPACITY_THRESHOLD));
            BulkTransport.PERSONNEL_CAPACITY_MAX_PERCENT = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_BULK_TRANSPORT_PERSONNEL_CAPACITY_MAX_PERCENTAGE));
            BulkTransport.PERSONNEL_CAPACITY_THRESHOLD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_BULK_TRANSPORT_PERSONNEL_CAPACITY_THRESHOLD));
            BulkTransport.BURN_BONUS = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_BULK_TRANSPORT_BURN_BONUS));

            writeLunaSettingToRealSetting(FIELD_BULK_TRANSPORT_CARGO_CAPACITY_MAX_PERCENTAGE);
            writeLunaSettingToRealSetting(FIELD_BULK_TRANSPORT_CARGO_CAPACITY_THRESHOLD);
            writeLunaSettingToRealSetting(FIELD_BULK_TRANSPORT_FUEL_CAPACITY_MAX_PERCENTAGE);
            writeLunaSettingToRealSetting(FIELD_BULK_TRANSPORT_FUEL_CAPACITY_THRESHOLD);
            writeLunaSettingToRealSetting(FIELD_BULK_TRANSPORT_PERSONNEL_CAPACITY_MAX_PERCENTAGE);
            writeLunaSettingToRealSetting(FIELD_BULK_TRANSPORT_PERSONNEL_CAPACITY_THRESHOLD);
            writeLunaSettingToRealSetting(FIELD_BULK_TRANSPORT_BURN_BONUS);
        }

        private void handleFieldRepairs() {
            logger.info("----> handleFieldRepairs()");
            BaseSkillEffectDescription.OP_ALL_THRESHOLD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_SHARED_FIELD_REPAIRS_AND_CONTAINMENT_PROCEDURES_OP_THRESHOLD));
            // Since this can't just work, and I can't just make a method to which I can pass the "FieldRepairs.MIN_HULL" field
            // but have to do the longer version instead, lets just try and do all of them in the same try/catch block and log
//            FieldRepairs.MIN_HULL = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIELD_REPAIRS_MIN_HULL));
            try {
                modifyFinalField(FieldRepairs.class.getDeclaredField("MIN_HULL"), (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIELD_REPAIRS_MIN_HULL)));
                modifyFinalField(FieldRepairs.class.getDeclaredField("MAX_HULL"), (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIELD_REPAIRS_MAX_HULL)));
                modifyFinalField(FieldRepairs.class.getDeclaredField("MIN_CR"), (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIELD_REPAIRS_MIN_CR)));
                modifyFinalField(FieldRepairs.class.getDeclaredField("MAX_CR"), (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIELD_REPAIRS_MAX_CR)));
                modifyFinalField(FieldRepairs.class.getDeclaredField("REPAIR_RATE_BONUS"), (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIELD_REPAIRS_REPAIR_RATE_BONUS)));
                modifyFinalField(FieldRepairs.class.getDeclaredField("INSTA_REPAIR_PERCENT"), (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_FIELD_REPAIRS_INSTA_REPAIR_PERCENT)));
            } catch (NoSuchFieldException e) {
//                throw new RuntimeException(e);
                logger.error("handleFieldRepairs failed with "+e+" due to modifying one of the final fields");
            }
            writeLunaSettingToRealSetting(FIELD_SHARED_FIELD_REPAIRS_AND_CONTAINMENT_PROCEDURES_OP_THRESHOLD);
            writeLunaSettingToRealSetting(FIELD_FIELD_REPAIRS_MIN_HULL);
            writeLunaSettingToRealSetting(FIELD_FIELD_REPAIRS_MAX_HULL);
            writeLunaSettingToRealSetting(FIELD_FIELD_REPAIRS_MIN_CR);
            writeLunaSettingToRealSetting(FIELD_FIELD_REPAIRS_MAX_CR);
            writeLunaSettingToRealSetting(FIELD_FIELD_REPAIRS_REPAIR_RATE_BONUS);
            writeLunaSettingToRealSetting(FIELD_FIELD_REPAIRS_INSTA_REPAIR_PERCENT);
            logger.info("<---- handleFieldRepairs()");
        }

        private void handleContainmentProcedures() {
            BaseSkillEffectDescription.OP_ALL_THRESHOLD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_SHARED_FIELD_REPAIRS_AND_CONTAINMENT_PROCEDURES_OP_THRESHOLD));
            ContainmentProcedures.CREW_LOSS_REDUCTION = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_CONTAINMENT_PROCEDURES_CREW_LOSS_REDUCTION));
            ContainmentProcedures.FUEL_SALVAGE_BONUS = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_CONTAINMENT_PROCEDURES_FUEL_SALVAGE_BONUS));
            ContainmentProcedures.FUEL_USE_REDUCTION_MAX_PERCENT = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_CONTAINMENT_PROCEDURES_FUEL_USE_REDUCTION_MAX_PERCENT));
            ContainmentProcedures.FUEL_USE_REDUCTION_MAX_FUEL = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_CONTAINMENT_PROCEDURES_FUEL_USE_REDUCTION_MAX_FUEL));

            writeLunaSettingToRealSetting(FIELD_SHARED_FIELD_REPAIRS_AND_CONTAINMENT_PROCEDURES_OP_THRESHOLD);
            writeLunaSettingToRealSetting(FIELD_CONTAINMENT_PROCEDURES_CREW_LOSS_REDUCTION);
            writeLunaSettingToRealSetting(FIELD_CONTAINMENT_PROCEDURES_FUEL_SALVAGE_BONUS);
            writeLunaSettingToRealSetting(FIELD_CONTAINMENT_PROCEDURES_FUEL_USE_REDUCTION_MAX_PERCENT);
            writeLunaSettingToRealSetting(FIELD_CONTAINMENT_PROCEDURES_FUEL_USE_REDUCTION_MAX_FUEL);
        }

        private void handleDerelictOperations() {
            DerelictContingent.MINUS_DP_PERCENT_PER_DMOD = (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_DERELICT_OPERATIONS_DP_COST_REDUCTION_PER_DMOD));
            //TODO add other fields and saving
        }

        /**
         * Method that fetches the <i>lunaKey</i> {@link LunaSettings} value, safely unboxes it via {@link #safeUnboxing(Integer)}
         * then finally overwrites the default starsector-data/settings.json key with the new value after getting the actual
         * key with {@link #convertLunaToRealKey(String)}
         *
         * @param lunaKey Luna key to fetch, convert to real key, then overwrite the real key's value with the fetched one
         */
        private void writeLunaSettingToRealSetting(String lunaKey) {

            Global
                    .getSettings()
                    .setFloat(
                            convertLunaToRealKey(lunaKey),
                            (float) safeUnboxing(LunaSettings.getInt(MOD_ID, lunaKey))
                    );
        }

        /**
         * Method for fetching the actual starsector-core settings.json key name that this <i>lunaKey</i> shadows
         *
         * @param lunaKey the Luna key to fetch the settings.json key for
         * @return the backing settings.json key
         */
        private String convertLunaToRealKey(String lunaKey) {
            return LunaToRealKeymap.get(lunaKey);
        }

        /**
         * Method for safe unboxing. Since we're mainly dealing with Integers here, and can only write
         * to global settings using Floats, the problem of unsafe unboxing shows up in a similar piece of code
         * {@snippet :
         *             Global
         *                     .getSettings()
         *                     .setFloat(
         *                             convertLunaToRealKey(lunaKey),
         *                             new Float(LunaSettings.getInt(MOD_ID, lunaKey))
         *                     );
         *}
         * Namely, if the {@link LunaSettings#getInt(String, String)} method returns null due to having no value
         * for the passed <i>lunaKey</i>, trying to instantiate a new Float out of that would, of course, also fail
         * and crash the program.
         *
         * So this utility method simply checks whether the <i><b>object</b></i> is null, and returns 0 if it is.
         * @param object {@link Integer} object to check and safely unbox and convert to <b>int</b>
         * @return 0 if the <b>object</b> was null, <b>object</b>'s value if it was non-null
         */
        private int safeUnboxing(Integer object) {
            int retVal;
            if (object == null) {
                retVal = 0;
            } else {
                retVal = object;
            }

            return retVal;
        }

        private boolean safeUnboxing(Boolean object) {
            boolean retVal;
            if (object == null) {
                retVal = false;
            } else {
                retVal = object;
            }

            return retVal;
        }

        private float safeUnboxing(Float object) {
            float retVal;
            if (object == null) {
                retVal = 0;
            } else {
                retVal = object;
            }

            return retVal;
        }
    }

    /*
    public static void modifyFinalField(Class<?> clazz, String fieldName, float newValue) {
        // Get the field
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            logger.error("modifyFinalField() failed due to " + e + " on field " + fieldName);
            return;
        }

        // Ensure the field is accessible
        field.setAccessible(true);

        // Remove the final modifier
        Field modifiersField = null;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) {
            logger.error("modifyFinalField() failed due to " + e + " while trying to populate modifiersField of field " + fieldName);
            return;
        }

        modifiersField.setAccessible(true);
        int modifiers = field.getModifiers();
        try {
            modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
        } catch (IllegalAccessException e) {
            logger.error("modifyFinalField() failed due to " + e + " on modifiersField of field " + fieldName);
            return;
        }

        // Set the new value
        try {
            field.set(null, newValue);
        } catch (IllegalAccessException e) {
            logger.error("modifyFinalField() failed due to " + e + " while trying to set value " + newValue + " on field");
            return;
        }

        // Restore the final modifier
        try {
            modifiersField.setInt(field, modifiers);
        } catch (IllegalAccessException e) {
            logger.error("modifyFinalField() failed due to " + e + " while trying to restore original modifiers on field");
        }
    } */

    public static void modifyFinalField(Field field, float newValue) {
        // Ensure the field is accessible
        field.setAccessible(true);

        // Remove the final modifier
        Field modifiersField;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            int modifiers = field.getModifiers();
            modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // The field doesn't have 'final' modifier, do nothing
            logger.error("modifyFinalField() failed due to " + e + " while trying to unset 'final' from field " + field.getName());
        }

        // Set the new value
        try {
            field.set(null, newValue);
        } catch (IllegalAccessException e) {
            logger.error("modifyFinalField() failed due to " + e + " while trying to set new value (" + newValue + ") on field " + field.getName());
        }

        // Restore the final modifier
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            int modifiers = field.getModifiers();
            modifiersField.setInt(field, modifiers | Modifier.FINAL);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // The field doesn't have 'modifiers' field, do nothing
            logger.error("modifyFinalField() failed due to " + e + " while trying to re-set 'final' to field " + field.getName());
        }
    }
}
