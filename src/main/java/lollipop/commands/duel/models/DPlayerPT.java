package lollipop.commands.duel.models;

import net.dv8tion.jda.api.entities.Member;

/**
 * Player Model for duel games
 */
public class DPlayerPT {

    private final String name;
    private final Member member;
    private boolean hasMultiplier;

    private int healthPoints;
    private int strengthPoints;
    private boolean isDefending;

    private long timeoutStart;
    private long timeoutDuration;

    public DPlayerPT(Member member) {
        this.member = member;
        this.name = this.member == null ? "CPU" : this.member.getEffectiveName();
        this.hasMultiplier = false;

        this.healthPoints = 100;
        this.strengthPoints = 0;
        this.isDefending = false;

        this.timeoutStart = -1;
        this.timeoutDuration = -1;
    }

    public String getName() {
        return name;
    }

    public Member getMember() {
        return member;
    }

    public boolean isCPU() {
        return this.member == null;
    }

    public boolean hasMultiplier() {
        return hasMultiplier;
    }

    public void setMultiplierStatus(boolean hasMultiplier) {
        this.hasMultiplier = hasMultiplier;
    }

    public int getHP() {
        return healthPoints;
    }

    public void setHP(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public void updateHP(int healthPoints) {
        this.healthPoints += healthPoints;
    }

    public int getSP() {
        return strengthPoints;
    }

    public void setSP(int strengthPoints) {
        this.strengthPoints = strengthPoints;
    }

    public void updateSP(int strengthPoints) {
        this.strengthPoints += strengthPoints;
    }

    public boolean isDefending() {
        return isDefending;
    }

    public void setDefending(boolean defending) {
        isDefending = defending;
    }

    public long getTimeoutStart() {
        return timeoutStart;
    }

    public void setTimeoutStart(long timeoutStart) {
        this.timeoutStart = timeoutStart;
    }

    public long getTimeoutDuration() {
        return timeoutDuration;
    }

    public void setTimeoutDuration(long timeoutDuration) {
        this.timeoutDuration= timeoutDuration;
    }

    /**
     * Checks whether a player is currently timed out
     * @return boolean
     */
    public boolean isTimedOut() {
        return (double)(System.currentTimeMillis()-timeoutStart)/1000 <= timeoutDuration;
    }

}