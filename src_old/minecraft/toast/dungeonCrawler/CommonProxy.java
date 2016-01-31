package toast.dungeonCrawler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class CommonProxy
{
    /// Registers render files if this is the client side.
    public void registerRenderers() {
    }
    
    /// Adds a new armor set texture and returns its render index.
    public int newArmor(String prefix) {
        return 5;
    }
    
    /// Handles trapping.
    public void trapping(ItemStack itemStack, EntityPlayer player, int useTime) {
        int damage = itemStack.getItemDamage();
        if (ItemHelper.isInRange(ItemHelper.TRAP, damage) && _DungeonCrawler.trapData.containsKey(player)) {
            int[] data = _DungeonCrawler.trapData.get(player);
            if (player.hurtTime > 0) {
                _DungeonCrawler.trapData.remove(player);
                player.stopUsingItem();
            }
            else
                data[4] += 1;
        }
    }
}