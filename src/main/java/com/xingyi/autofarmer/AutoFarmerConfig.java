package com.xingyi.autofarmer;

import net.minecraftforge.common.ForgeConfigSpec;

public class AutoFarmerConfig {

    public static final ForgeConfigSpec spec;
    public static ForgeConfigSpec.IntValue cooldownTicks;
    public static ForgeConfigSpec.IntValue treeSearchRadius;
    public static ForgeConfigSpec.IntValue treeHarvestRadius;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("AutoFarmer Common Config").push("common");
        cooldownTicks = builder
                .comment("Cooldown in ticks between operations (default: 40 = 2 seconds)")
                .defineInRange("cooldownTicks", 40, 10, 200);
        treeSearchRadius = builder
                .comment("Search radius around planted sapling to find grown tree (default: 2)")
                .defineInRange("treeSearchRadius", 2, 1, 5);
        treeHarvestRadius = builder
                .comment("Extra radius beyond connected logs to harvest leaves (default: 2)")
                .defineInRange("treeHarvestRadius", 2, 1, 5);
        builder.pop();
        spec = builder.build();
    }
}
