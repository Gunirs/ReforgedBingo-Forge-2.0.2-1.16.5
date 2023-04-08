package com.github.gunirs.reforgedbingo;

import com.gamerforea.eventhelper.nexus.ModNexus;
import com.gamerforea.eventhelper.nexus.ModNexusFactory;
import com.gamerforea.eventhelper.nexus.NexusUtils;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

@ModNexus(name = "ReforgedBingo")
public class ModUtils {
    public static final ModNexusFactory NEXUS_FACTORY = NexusUtils.getFactory();

    public static FakePlayer getFakePlayer(World world) {
        return NEXUS_FACTORY.getFake(world);
    }
}