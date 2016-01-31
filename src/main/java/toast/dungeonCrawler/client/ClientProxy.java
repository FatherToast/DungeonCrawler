package toast.dungeonCrawler.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import toast.dungeonCrawler.CommonProxy;
import toast.dungeonCrawler.DungeonCrawlerMod;
import toast.dungeonCrawler.EntityBomb;
import toast.dungeonCrawler.EntityGrenade;
import toast.dungeonCrawler.EntityMine;
import toast.dungeonCrawler.EntityPebble;
import toast.dungeonCrawler.EntityRock;
import toast.dungeonCrawler.EntityTrap;
import toast.dungeonCrawler.ItemHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Handles tasks that should be done differently depending on the side they are called from in a way that is safe
 * for any task to be called from either side.<br>
 * The ClientProxy is only used client-side, while CommonProxy is used exclusively server-side.<br>
 * Mainly used to prevent errors due to references to client-side classes, which don't exist on the server.
 *
 * @see CommonProxy
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityRock.class, new RenderRock());
		RenderingRegistry.registerEntityRenderingHandler(EntityPebble.class, new RenderDCItem(DungeonCrawlerMod.DCItem, ItemHelper.getDamageFor(ItemHelper.BASIC, "pebble")));
		RenderingRegistry.registerEntityRenderingHandler(EntityBomb.class, new RenderBomb());
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class, new RenderGrenade());
		RenderingRegistry.registerEntityRenderingHandler(EntityMine.class, new RenderMine());
		RenderingRegistry.registerEntityRenderingHandler(EntityTrap.class, new RenderTrap());
    }

    @Override
    public int getRenderIndex(String id, int defaultValue) {
        return RenderingRegistry.addNewArmourRendererPrefix(id);
    }

    @Override
    public void trapping(ItemStack itemStack, EntityPlayer player, int useTime) {
        int damage = itemStack.getItemDamage();
        if (ItemHelper.isInRange(ItemHelper.TRAP, damage) && FMLClientHandler.instance().getClient().thePlayer == player && DungeonCrawlerMod.trapData.containsKey(player)) {
            MovingObjectPosition object = FMLClientHandler.instance().getClient().objectMouseOver;
            int[] data = DungeonCrawlerMod.trapData.get(player);
            if (object == null || object.entityHit != null || object.sideHit != data[3] || object.blockY != data[1] || object.blockX != data[0] || object.blockZ != data[2] || player.hurtTime > 0) {
            	DungeonCrawlerMod.trapData.remove(player);
                player.stopUsingItem();
            } else {
				data[4] += 1;
			}
        }
    }
}