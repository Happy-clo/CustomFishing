package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.FishingManager;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public abstract class AbstractGamingPlayer implements GamingPlayer, Runnable {

    private final FishingManager manager;
    private final long deadline;

    protected boolean success;
    protected CancellableTask task;
    protected Player player;
    protected GameSettings settings;
    protected FishHook fishHook;

    public AbstractGamingPlayer(Player player, FishHook hook, GameSettings settings) {
        this.player = player;
        this.fishHook = hook;
        this.settings = settings;
        this.manager = CustomFishingPlugin.get().getFishingManager();
        this.deadline = System.currentTimeMillis() + settings.getTime() * 1000L;
        this.arrangeTask();
    }

    public void arrangeTask() {
        this.task = CustomFishingPlugin.get().getScheduler().runTaskSyncTimer(this, fishHook.getLocation(), 1, 1);
    }

    @Override
    public void cancel() {
        if (task != null && !task.isCancelled())
            task.cancel();
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public boolean onRightClick() {
        endGame();
        return true;
    }

    @Override
    public boolean onLeftClick() {
        return false;
    }

    @Override
    public boolean onChat(String message) {
        return false;
    }

    @Override
    public boolean onSwapHand() {
        return false;
    }

    @Override
    public boolean onJump() {
        return false;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Effect getEffectReward() {
        return null;
    }

    @Override
    public void run() {
        timeOutCheck();
        switchItemCheck();
    }

    protected void endGame() {
        this.manager.processGameResult(this);
    }

    protected void setGameResult(boolean success) {
        this.success = success;
    }

    protected void timeOutCheck() {
        if (System.currentTimeMillis() > deadline) {
            cancel();
            endGame();
        }
    }

    protected void switchItemCheck() {
        PlayerInventory playerInventory = player.getInventory();
        if (playerInventory.getItemInMainHand().getType() != Material.FISHING_ROD
                && playerInventory.getItemInOffHand().getType() != Material.FISHING_ROD) {
            cancel();
            endGame();
        }
    }
}