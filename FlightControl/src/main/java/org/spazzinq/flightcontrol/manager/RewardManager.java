/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.object.Reward;
import org.spazzinq.flightcontrol.util.MathUtil;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.io.File;
import java.util.HashSet;
import java.util.List;

public class RewardManager {
    private final FlightControl pl;

    @Getter private CommentConf conf;
    @Getter private final File rewardFile;

    @Getter private final HashSet<BukkitTask> rewardTasks = new HashSet<>();

    public RewardManager() {
        pl = FlightControl.getInstance();
        rewardFile = new File(pl.getDataFolder(), "rewards.yml");
    }

    public void loadRewards() {
        conf = new CommentConf(rewardFile, pl.getResource("rewards.yml"));

        for (BukkitTask task : rewardTasks) {
            task.cancel();
        }
        rewardTasks.clear();

        for (String categoryName : conf.getKeys(false)) {
            Category category = null;
            long cooldown;
            List<String> commands;

            // Not very efficient but is single execution
            for (Category c : pl.getCategoryManager().getCategories()) {
                if (categoryName.equals(c.getName())) {
                    category = c;
                }
            }
            if (category == null && categoryName.equals(pl.getCategoryManager().getGlobal().getName())) {
                category = pl.getCategoryManager().getGlobal();
            }

            String cooldownStr = conf.getConfigurationSection(categoryName).getString("cooldown");
            cooldown = MathUtil.calculateDuration(cooldownStr) * 20; // Convert to ticks

            commands = conf.getConfigurationSection(categoryName).getStringList("commands");

            if (category != null) {
                rewardTasks.add(new Reward(category, commands).runTaskTimer(pl, 0, cooldown));
                loaded(categoryName, commands, PlayerUtil.durationToWords(cooldown * 50)); // Convert from ticks to ms
            } else {
                nonexistent(categoryName);
            }

        }
    }

    private void loaded(String categoryName, List<String> cmds, String cooldown) {
        loaded(categoryName, cmds, cooldown, "");
    }
    private void loaded(String categoryName, List<String> cmds, String cooldown, String extra) {
        pl.getLogger().info("Rewards for \"" + categoryName + "\" category loaded with a " + cooldown + " cooldown: " + cmds + extra);
    }

    private void nonexistent(String categoryName) {
        pl.getLogger().warning("Category name \"" + categoryName + "\" from rewards.yml does not exist in categories.yml!");
    }
}
