package com.xingyi.autofarmer.menu;

import com.xingyi.autofarmer.registry.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AutoFarmerMenu extends AbstractContainerMenu {

    private final SimpleContainer machineInventory;
    private final ContainerData data;

    public AutoFarmerMenu(int syncId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(syncId, playerInv, new SimpleContainer(6), new ContainerData() {
            @Override public int get(int idx) { return 0; }
            @Override public void set(int idx, int val) {}
            @Override public int getCount() { return 1; }
        });
    }

    public AutoFarmerMenu(int syncId, Inventory playerInv, SimpleContainer machineInv, ContainerData data) {
        super(ModBlocks.AUTOFARMER.get(), syncId);
        this.machineInventory = machineInv;
        this.data = data;

        // Machine input slots (5 slots, rows 0-1) — aligned with GUI texture at y=34
        int slotsX = 8;
        int slotsY = 34;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int idx = row * 3 + col;
                if (idx < 5) {
                    this.addSlot(new Slot(machineInv, idx, slotsX + col * 18, slotsY + row * 18));
                }
            }
        }
        // Output slot (right side) — aligned with GUI texture at (104, 34)
        this.addSlot(new Slot(machineInv, 5, 104, 34));

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    public boolean stillValid(Player player) {
        return machineInventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack result = ItemStack.EMPTY;
        Slot slotObj = this.slots.get(slot);
        if (slotObj == null || !slotObj.hasItem()) return ItemStack.EMPTY;

        ItemStack itemStack = slotObj.getItem();
        result = itemStack.copy();

        if (slot < 6) {
            // From machine to player
            if (!this.moveItemStackTo(itemStack, 6, 6 + 36, true))
                return ItemStack.EMPTY;
        } else {
            // From player to machine
            if (!this.moveItemStackTo(itemStack, 0, 5, false))
                return ItemStack.EMPTY;
        }

        if (itemStack.isEmpty()) {
            slotObj.set(ItemStack.EMPTY);
        } else {
            slotObj.setChanged();
        }

        return result;
    }
}
