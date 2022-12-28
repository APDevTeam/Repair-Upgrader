package net.countercraft.movecraft.repair.upgrader;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ManagerTask extends BukkitRunnable {
    private final Plugin plugin;
    private final Queue<UUIDTask> folders;
    private UUIDTask running = null;

    public ManagerTask(Plugin plugin, Set<File> folders) {
        this.plugin = plugin;
        Set<UUIDTask> result = new HashSet<>();;
        for (File folder : folders) {
            result.add(new UUIDTask(plugin, folder));
        }
        this.folders = new LinkedList<>(result);
        this.runTaskTimerAsynchronously(plugin, 200, 1); // 10 seconds after init, every tick
    }

    @Override
    public void run() {
        if (running == null && folders.isEmpty())
            return; // We're already done

        if (running == null || running.isDone()) {
            running = folders.poll();
            if (running == null) {
                // We hit the end of the queue
                plugin.getLogger().info("Conversion completed!");
                return;
            }
        }

        running.run();
    }
}
