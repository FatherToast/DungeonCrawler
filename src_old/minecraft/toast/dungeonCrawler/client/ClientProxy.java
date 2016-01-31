package toast.dungeonCrawler.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.MinecraftForgeClient;
import toast.dungeonCrawler._DungeonCrawler;
import toast.dungeonCrawler.CommonProxy;
import toast.dungeonCrawler.EntityBomb;
import toast.dungeonCrawler.EntityGrenade;
import toast.dungeonCrawler.EntityKnife;
import toast.dungeonCrawler.EntityMine;
import toast.dungeonCrawler.EntityPebble;
import toast.dungeonCrawler.EntityRock;
import toast.dungeonCrawler.EntityTrap;
import toast.dungeonCrawler.ItemHelper;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    /// Registers render files if this is the client side.
    @Override
    public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityRock.class, new RenderRock());
		RenderingRegistry.registerEntityRenderingHandler(EntityPebble.class, new RenderDCItem(_DungeonCrawler.DCItem, ItemHelper.getDamageFor(ItemHelper.BASIC, "pebble")));
		RenderingRegistry.registerEntityRenderingHandler(EntityBomb.class, new RenderBomb());
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class, new RenderGrenade());
		RenderingRegistry.registerEntityRenderingHandler(EntityMine.class, new RenderMine());
		RenderingRegistry.registerEntityRenderingHandler(EntityTrap.class, new RenderTrap());
    }
    
    /// Adds a new armor set texture and returns its render index.
    @Override
    public int newArmor(String prefix) {
        return RenderingRegistry.addNewArmourRendererPrefix(prefix);
    }
    
    /// Handles trapping.
    @Override
    public void trapping(ItemStack itemStack, EntityPlayer player, int useTime) {
        int damage = itemStack.getItemDamage();
        if (ItemHelper.isInRange(ItemHelper.TRAP, damage) && FMLClientHandler.instance().getClient().thePlayer == player && _DungeonCrawler.trapData.containsKey(player)) {
            MovingObjectPosition object = FMLClientHandler.instance().getClient().objectMouseOver;
            int[] data = _DungeonCrawler.trapData.get(player);
            if (object == null || object.entityHit != null || object.sideHit != data[3] || object.blockY != data[1] || object.blockX != data[0] || object.blockZ != data[2] || player.hurtTime > 0) {
                _DungeonCrawler.trapData.remove(player);
                player.stopUsingItem();
            }
            else
                data[4] += 1;
        }
    }
}