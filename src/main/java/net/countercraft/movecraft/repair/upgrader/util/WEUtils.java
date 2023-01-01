package net.countercraft.movecraft.repair.upgrader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import net.countercraft.movecraft.repair.upgrader.RepairUpgrader;

public class WEUtils {
    public static final ClipboardFormat LEGACY_FORMAT = BuiltInClipboardFormat.MCEDIT_SCHEMATIC;
    public static final ClipboardFormat MODERN_FORMAT = BuiltInClipboardFormat.SPONGE_SCHEMATIC;

    @Nullable
    public static Clipboard loadSchematic(File file) throws IOException {
        ClipboardReader reader = LEGACY_FORMAT.getReader(new FileInputStream(file));
        return reader.read();
    }

    public static void saveSchematic(File file, Clipboard clipboard) throws IOException {
        ClipboardWriter writer = MODERN_FORMAT.getWriter(new FileOutputStream(file));
        writer.write(clipboard);
        writer.close();
    }

    @NotNull
    public static Clipboard convertClipboard(Clipboard input) {
        Clipboard output = new BlockArrayClipboard(input.getRegion());
        output.setOrigin(input.getOrigin());
        for (BlockVector3 location : input.getRegion()) {
            BaseBlock inputBlock = input.getFullBlock(location);

            RepairUpgrader.getInstance().getLogger().info(() -> "Checking " + location);
            CompoundTag nbt = upgradeBlockContents(inputBlock);
            BaseBlock outputBlock;
            if (nbt != null) {
                outputBlock = inputBlock.toBaseBlock(nbt);
            }
            else {
                outputBlock = inputBlock;
            }

            try {
                output.setBlock(location, outputBlock);
            }
            catch (WorldEditException e) {
                RepairUpgrader.getInstance().getLogger().warning(() -> "Failed to convert " + location);
                e.printStackTrace();
            }
        }
        return output;
    }

    @Nullable
    private static CompoundTag upgradeBlockContents(BaseBlock block) {
        CompoundTag inputNBT = block.getNbtData();
        if (inputNBT == null) {
            RepairUpgrader.getInstance().getLogger().info("Null NBT");
            return null;
        }

        Map<String, Tag> result = new HashMap<>();
        boolean foundItems = false;
        for (Map.Entry<String, Tag> entry : inputNBT.getValue().entrySet()) {
            if (!entry.getKey().equals("Items")) {
                result.put(entry.getKey(), entry.getValue());
                continue;
            }

            Tag items = entry.getValue();
            if (!(items instanceof ListTag)) {
                RepairUpgrader.getInstance().getLogger().info("Invalid Items");
                result.put(entry.getKey(), entry.getValue());
                continue;
            }

            foundItems = true;
            result.put(entry.getKey(), updateItems((ListTag) items));
        }
        if (!foundItems) {
            RepairUpgrader.getInstance().getLogger().info("No Items");
            return null;
        }

        return new CompoundTag(result);
    }

    @NotNull
    private static ListTag updateItems(ListTag items) {
        List<Tag> result = new ArrayList<>();
        for (Tag item : items.getValue()) {
            if (!(item instanceof CompoundTag)) {
                RepairUpgrader.getInstance().getLogger().info(() -> "Skipping " + item);
                result.add(item);
                continue;
            }

            result.add(updateItem((CompoundTag) item));
        }
        return new ListTag(CompoundTag.class, result);
    }

    @NotNull
    private static CompoundTag updateItem(CompoundTag item) {
        Map<String, Tag> result = new HashMap<>();
        for (Map.Entry<String, Tag> entry : item.getValue().entrySet()) {
            if (!entry.getKey().equals("id")) {
                result.put(entry.getKey(), entry.getValue());
                continue;
            }

            short shortID = item.getShort("id");
            BlockState blockStateID = LegacyMapper.getInstance().getBlockFromLegacy(shortID);
            if (blockStateID == null) {
                RepairUpgrader.getInstance().getLogger().severe(() -> "Failed to convert " + shortID);
                continue;
            }
            BlockType blockType = blockStateID.getBlockType();
            String resultID = blockType.getId();
            result.put("id", new StringTag(resultID));
            RepairUpgrader.getInstance().getLogger().info(() -> "Converted " + shortID + " to " + resultID + " (via " + blockStateID + " & " + blockType + ")");
        }
        return new CompoundTag(result);
    }
}
