package toast.dungeonCrawler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Handles tasks that should be done differently depending on the side they are called from in a way that is safe
 * for any task to be called from either side.<br>
 * The CommonProxy class itself is exclusively used server-side, while ClientProxy is only used client-side.<br>
 * Mainly used to prevent errors due to references to client-side classes, which don't exist on the server.
 *
 * @see toast.apocalypse.client.ClientProxy ClientProxy
 */
public class CommonProxy {

    /** Registers renderers if this is the client side. */
    public void registerRenderers() {
        // Client-side method
    }

    /**
     * Returns a new render index to use and registers it with the game.
     * @param id The id of the texture. The filenames become id_1 (non-leg armor) and id_2 (leg armor).
     * @param defaultValue The render index to use if possible.
     * @return The render index assigned. This will be defaultValue if this is not the client side or if that index was available.
     */
    public int getRenderIndex(String id, int defaultValue) {
        return defaultValue;
    }

    /**
     * Handles trapping each tick.
     * @param itemStack The held item.
     * @param player The player that is  trapping.
     * @param useTime The amount of time the item has been used for.
     */
    public void trapping(ItemStack itemStack, EntityPlayer player, int useTime) {
        int damage = itemStack.getItemDamage();
        if (ItemHelper.isInRange(ItemHelper.TRAP, damage) && DungeonCrawlerMod.trapData.containsKey(player)) {
            int[] data = DungeonCrawlerMod.trapData.get(player);
            if (player.hurtTime > 0) {
            	DungeonCrawlerMod.trapData.remove(player);
                player.stopUsingItem();
            } else {
				data[4] += 1;
			}
        }
    }
}