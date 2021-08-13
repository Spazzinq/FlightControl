/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2020 Spazzinq
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.object.Reward;
import org.spazzinq.flightcontrol.util.MathUtil;

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

            // FIXME Inefficient
            for (Category c : pl.getCategoryManager().getCategories()) {
                if (categoryName.equals(c.getName())) {
                    category = c;
                }
            }
            if (categoryName.equals(pl.getCategoryManager().getGlobal().getName())) {
                category = pl.getCategoryManager().getGlobal();
            }

            String cooldownStr = conf.getConfigurationSection(categoryName).getString("cooldown");
            cooldown = MathUtil.calculateDuration(cooldownStr) / 50; // Convert to ticks

            commands = conf.getConfigurationSection(categoryName).getStringList("commands");

            if (category != null) {
                rewardTasks.add(new Reward(category, commands).runTaskTimer(pl, 0, cooldown));
            } else {
                pl.getLogger().warning("Category name \"" + categoryName + "\" from rewards.yml does not exist in categories.yml!");
            }

        }
    }


}
