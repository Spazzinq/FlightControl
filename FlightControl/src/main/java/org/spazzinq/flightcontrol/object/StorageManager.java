/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.object;

import lombok.Getter;
import org.spazzinq.flightcontrol.FlightControl;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public abstract class StorageManager {
    protected final FlightControl pl;

    @Getter protected CommentConf conf;
    protected final File confFile;
    protected final String fileName;

    protected boolean reloadIgnored;

    public StorageManager(String fileName) {
        this.fileName = fileName;
        pl = FlightControl.getInstance();
        confFile = new File(pl.getDataFolder(), fileName);
    }

    public boolean load() {
        boolean initIgnored = !reloadIgnored;

        if (initIgnored) {
            ignoreReload();

            initializeConf();
            migrateFromOldVersion();
            updateFormatting();
            initializeValues();
        }

        return initIgnored;
    }

    protected abstract void initializeConf();

    protected abstract void initializeValues();

    protected abstract void updateFormatting();

    protected abstract void migrateFromOldVersion();

    public void set(String path, Object value) {
        ignoreReload();
        conf.set(path, value);
        conf.save();
    }

    private void ignoreReload() {
        reloadIgnored = true;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                reloadIgnored = false;
            }
        }, 500);
    }
}
