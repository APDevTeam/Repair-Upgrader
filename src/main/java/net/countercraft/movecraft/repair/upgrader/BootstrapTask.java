package net.countercraft.movecraft.repair.upgrader;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BootstrapTask extends BukkitRunnable {
    private final Plugin plugin;

    public BootstrapTask(Plugin plugin) {
        this.plugin = plugin;
        this.runTaskLaterAsynchronously(plugin, 600); // 30 seconds after init
    }

    @Override
    public void run() {
        // Grab set of UUIDs, start up the UUID manager running
        File repair = new File(plugin.getDataFolder().getParentFile(), "Movecraft-Repair");
        if (!repair.exists() || !repair.isDirectory()) {
            plugin.getLogger().info("Could not find Movecraft-Repair");
            return; // Does not exist
        }

        File states = new File(repair, "RepairStates");
        if (!states.exists() || !states.isDirectory()) {
            plugin.getLogger().info("Could not find RepairStates");
            return; // Does not exist
        }

        Set<File> folders = new HashSet<>();
        for (File file : states.listFiles()) {
            if (!file.exists() || !file.isDirectory())
                continue; // Does not exist or is not a folder

            folders.add(file);
        }
        plugin.getLogger().info(() -> "Found " + folders.size() + " UUID folders");

        // Manager task will handle a queue of UUIDs, continuing to run each UUID until completed
        new ManagerTask(plugin, folders);
    }
}
