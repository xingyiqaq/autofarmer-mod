package com.xingyi.autofarmer.registry;

import com.xingyi.autofarmer.AutoFarmerMod;
import com.xingyi.autofarmer.menu.AutoFarmerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, AutoFarmerMod.MOD_ID);

    public static final RegistryObject<MenuType<AutoFarmerMenu>> AUTOFARMER =
        MENUS.register("autofarmer_menu",
            () -> IForgeMenuType.create((id, inv, data) -> new AutoFarmerMenu(id, inv, data)));
}
