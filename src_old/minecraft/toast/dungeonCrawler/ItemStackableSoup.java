package toast.dungeonCrawler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/// This class is only used to override mushroom stew with an identical, but stackable, item.
public class ItemStackableSoup extends ItemSoup
{
    public ItemStackableSoup(int id, int stamina) {
        super(id, stamina);
        setMaxStackSize(64);
    }
    
    /// Called when item use time has been used up.
    @Override
    public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player) {
        super.onEaten(itemStack, world, player);
        if (itemStack.stackSize <= 0)
            return new ItemStack(Item.bowlEmpty);
        player.inventory.addItemStackToInventory(new ItemStack(Item.bowlEmpty));
        return itemStack;
    }
}