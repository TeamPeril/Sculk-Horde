package com.github.sculkhorde.util;

import com.github.sculkhorde.core.SculkHorde;

import net.minecraft.nbt.CompoundTag;

public class StatisticsData {

    private long totalUnitDeaths;
    private long totalUnitsSpawned;
    private long totalVictimsInfested;
    private long totalBlocksInfested;
    private long totalRaidsOccured;
    private long totalMassFromBees;
    private long totalMassFromBurrowed;

    private long totalMassFromDiseasedCysts;
    private long totalMassFromNodes;
    private long totalMassRemovedFromHorde;

    public StatisticsData() {
        this.totalUnitDeaths = 0;
        this.totalUnitsSpawned = 0;
        this.totalVictimsInfested = 0;
        this.totalBlocksInfested = 0;
        this.totalRaidsOccured = 0;
        this.totalMassFromBees = 0;
        this.totalMassFromBurrowed = 0;
        this.totalMassFromDiseasedCysts = 0;
        this.totalMassFromNodes = 0;
        this.totalMassRemovedFromHorde = 0;
    }

    public long getTotalUnitDeaths() {
        return totalUnitDeaths;
    }

    public void setTotalUnitDeaths(long totalUnitDeaths) {
        this.totalUnitDeaths = totalUnitDeaths;
    }

    public void incrementTotalUnitDeaths() {
        this.totalUnitDeaths++;
    }

    public long getTotalUnitsSpawned() {
        return totalUnitsSpawned;
    }

    public void setTotalUnitsSpawned(long totalUnitsSpawned) {
        this.totalUnitsSpawned = totalUnitsSpawned;
    }

    public void incrementTotalUnitsSpawned() {
        this.totalUnitsSpawned++;
    }

    public long getTotalVictimsInfested() {
        return totalVictimsInfested;
    }

    public void setTotalVictimsInfested(long totalVictimsInfested) {
        this.totalVictimsInfested = totalVictimsInfested;
    }

    public void incrementTotalVictimsInfested() {
        this.totalVictimsInfested++;
    }

    public long getTotalBlocksInfested() {
        return totalBlocksInfested;
    }

    public void setTotalBlocksInfested(long totalBlocksInfested) {
        this.totalBlocksInfested = totalBlocksInfested;
    }

    public void incrementTotalBlocksInfested() {
        this.totalBlocksInfested++;
    }

    public long getTotalRaidsOccured() {
        return totalRaidsOccured;
    }

    public void setTotalRaidsOccured(long totalRaidsOccured) {
        this.totalRaidsOccured = totalRaidsOccured;
    }

    public void incrementTotalRaidsOccured() {
        this.totalRaidsOccured++;
    }

    public long getTotalMassFromBees() {
        return totalMassFromBees;
    }

    public void setTotalMassFromBees(long totalMassFromBees) {
        this.totalMassFromBees = totalMassFromBees;
    }

    public void addTotalMassFromBees(int value) {
        this.totalMassFromBees += value;
    }

    public long getTotalMassFromBurrowed() {
        return totalMassFromBurrowed;
    }

    public void setTotalMassFromBurrowed(long totalMassFromBurrowed) {
        this.totalMassFromBurrowed = totalMassFromBurrowed;
    }

    public void addTotalMassFromBurrowed(int value) {
        this.totalMassFromBurrowed += value;
    }

    public long getTotalMassFromDiseasedCysts() {
        return totalMassFromDiseasedCysts;
    }

    public void setTotalMassFromDiseasedCysts(long totalMassFromDiseasedCysts) {
        this.totalMassFromDiseasedCysts = totalMassFromDiseasedCysts;
    }

    public void addTotalMassFromDiseasedCysts(int value) {
        this.totalMassFromDiseasedCysts += value;
    }

    public long getTotalMassFromNodes() {
        return totalMassFromNodes;
    }

    public void setTotalMassFromNodes(long totalMassFromNodes) {
        this.totalMassFromNodes = totalMassFromNodes;
    }

    public void addTotalMassFromNodes(int value) {
        this.totalMassFromNodes += value;
    }

    public long getTotalMassRemovedFromHorde() {
        return totalMassRemovedFromHorde;
    }

    public void setTotalMassRemovedFromHorde(long totalMassRemovedFromHorde) {
        this.totalMassRemovedFromHorde = totalMassRemovedFromHorde;
    }

    public void addTotalMassRemovedFromHorde(int value) {
        this.totalMassRemovedFromHorde += value;
    }

    public static StatisticsData getStatsData()
    {
        return SculkHorde.statisticsData;
    }

    public static void save(CompoundTag tag)
    {
        tag.putLong("totalUnitDeaths", getStatsData().getTotalUnitDeaths());
        tag.putLong("totalUnitsSpawned", getStatsData().getTotalUnitsSpawned());
        tag.putLong("totalVictimsInfested", getStatsData().getTotalVictimsInfested());
        tag.putLong("totalBlocksInfested", getStatsData().getTotalBlocksInfested());
        tag.putLong("totalRaidsOccured", getStatsData().getTotalRaidsOccured());
        tag.putLong("totalMassFromBees", getStatsData().getTotalMassFromBees());
        tag.putLong("totalMassFromBurrowed", getStatsData().getTotalMassFromBurrowed());
        tag.putLong("totalMassFromDiseasedCysts", getStatsData().getTotalMassFromDiseasedCysts());
        tag.putLong("totalMassFromNodes", getStatsData().getTotalMassFromNodes());
        tag.putLong("totalMassRemovedFromHorde", getStatsData().getTotalMassRemovedFromHorde());
    }

    public static void load(CompoundTag tag)
    {
        getStatsData().setTotalUnitDeaths(tag.getLong("totalUnitDeaths"));
        getStatsData().setTotalUnitsSpawned(tag.getLong("totalUnitsSpawned"));
        getStatsData().setTotalVictimsInfested(tag.getLong("totalVictimsInfested"));
        getStatsData().setTotalBlocksInfested(tag.getLong("totalBlocksInfested"));
        getStatsData().setTotalRaidsOccured(tag.getLong("totalRaidsOccured"));
        getStatsData().setTotalMassFromBees(tag.getLong("totalMassFromBees"));
        getStatsData().setTotalMassFromBurrowed(tag.getLong("totalMassFromBurrowed"));
        getStatsData().setTotalMassFromDiseasedCysts(tag.getLong("totalMassFromDiseasedCysts"));
        getStatsData().setTotalMassFromNodes(tag.getLong("totalMassFromNodes"));
        getStatsData().setTotalMassRemovedFromHorde(tag.getLong("totalMassRemovedFromHorde"));
    }
}
