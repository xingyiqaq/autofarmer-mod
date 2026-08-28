package com.xingyi.autofarmer.menu;

import com.xingyi.autofarmer.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AutoFarmerMenu extends AbstractContainerMenu {

    private final SimpleContainer machineInventory;
    private final ContainerData data;

    public AutoFarmerMenu(int syncId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(syncId, playerInv, new SimpleContainer(6));
    }

    public AutoFarmerMenu(int syncId, Inventory playerInv, SimpleContainer items) {
        super(ModMenus.AUTOFARMER.get(), syncId);
        this.machineInventory = items;
        this.data = new ContainerData() {
            public int get(int idx) { return 0; }
            public void set(int idx, int val) {}
            public int getCount() { return 1; }
        };

        int slotsX = 8;
        int slotsY = 34;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int idx = row * 3 + col;
                if (idx < 5) {
                    this.addSlot(new Slot(machineInventory, idx, slotsX + col * 18, slotsY + row * 18));
                }
            }
        }
        this.addSlot(new Slot(machineInventory, 5, 104, 34));

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
        return true;
    }

    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack result = ItemStack.EMPTY;
        Slot slotObj = this.slots.get(slot);
        if (slotObj == null || !slotObj.hasItem()) return ItemStack.EMPTY;

        ItemStack itemStack = slotObj.getItem();
        result = itemStack.copy();

        if (slot < 6) {
            if (!this.moveItemStackTo(itemStack, 6, 6 + 36, true))
                return ItemStack.EMPTY;
        } else {
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
