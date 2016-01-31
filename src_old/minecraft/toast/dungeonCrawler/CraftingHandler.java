package toast.dungeonCrawler;

import cpw.mods.fml.common.ICraftingHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CraftingHandler implements ICraftingHandler
{
    public CraftingHandler() {
        GameRegistry.registerCraftingHandler(this);
    }
    
    /// Called when an item is crafted.
    @Override
    public void onCrafting(EntityPlayer player, ItemStack itemStack, IInventory craftMatrix) {
        int knifeSlot = -1;
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null)
                continue;
            Item item = Item.itemsList[ingredient.itemID];
            if (item instanceof ItemKnife) {
                if (knifeSlot < 0)
                    knifeSlot = i;
                else {
                    craftMatrix.decrStackSize(knifeSlot, 1);
                    craftMatrix.decrStackSize(i, 1);
                }
            }
        }
    }

    /// Called when an item is smelted.
    @Override
    public void onSmelting(EntityPlayer player, ItemStack itemStack) {
    }
}