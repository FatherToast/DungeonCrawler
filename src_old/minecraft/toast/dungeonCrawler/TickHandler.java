package toast.dungeonCrawler;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.world.World;

public class TickHandler implements ITickHandler
{
    private static byte ticks = Byte.MIN_VALUE;
    
    public TickHandler() {
        TickRegistry.registerTickHandler(this, Side.SERVER);
    }
    
    /// Called at the "start" phase of a tick.
    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        if (++ticks < Byte.MAX_VALUE)
            return;
        ticks = Byte.MIN_VALUE;
        //World world = (World)tickData[0];
        Iterator itr = _DungeonCrawler.trapData.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<EntityPlayer, int[]> entry = (Entry)itr.next();
            if (entry.getKey() == null || !entry.getKey().isUsingItem())
                itr.remove();
        }
    }
    
    /// Called at the "end" phase of a tick.
    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
    }
    
    /// Returns the list of ticks this tick handler is interested in receiving.
    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.WORLD);
    }
    
    /// Returns a profiling label for this tick manager.
    @Override
    public String getLabel() {
        return "DungeonCrawlerTickHandler";
    }
}