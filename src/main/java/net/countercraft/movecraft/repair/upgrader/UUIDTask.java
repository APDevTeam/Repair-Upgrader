package net.countercraft.movecraft.repair.upgrader;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.bukkit.plugin.Plugin;

import net.countercraft.movecraft.repair.upgrader.util.WEUtils;

public class UUIDTask implements Task {
    private final Plugin plugin;
    private final Queue<FileTask> states;

    public UUIDTask(Plugin plugin, File folder) {
        this.plugin = plugin;
        Set<FileTask> result = new HashSet<>();
        for (File file : folder.listFiles()) {
            if (!file.exists() || !file.isFile())
                continue; // Does not exist or is not file

            String name = file.getName();
            int index = name.lastIndexOf(".");
            if (index == -1)
                continue; // Can't find period

            String extension = name.substring(index + 1);
            if (!extension.equalsIgnoreCase(WEUtils.LEGACY_FORMAT.getPrimaryFileExtension()))
                continue; // Is not correct extension

            String baseName = name.substring(0, index);
            File newFile = new File(folder, baseName + "." + WEUtils.MODERN_FORMAT.getPrimaryFileExtension());
            result.add(new FileTask(plugin, file, newFile));
        }
        states = new LinkedList<>(result);
    }

    @Override
    public void run() {
        if (states.isEmpty())
            return;

        states.poll().run();
    }

    @Override
    public boolean isDone() {
        return states.isEmpty();
    }
}
