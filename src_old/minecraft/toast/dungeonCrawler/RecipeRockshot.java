package toast.dungeonCrawler;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeRockshot implements IRecipe
{
    /// The array of all item IDs that can legally be loaded into a rockshot.
    public static final int[] loadableIDs = {
        Block.cobblestone.blockID, Block.dirt.blockID, Block.torchWood.blockID, Block.torchRedstoneActive.blockID, Block.gravel.blockID, Block.sand.blockID, Block.netherrack.blockID, Item.arrow.itemID, Item.fireballCharge.itemID, Item.potion.itemID, Block.tnt.blockID, Block.slowSand.blockID, Block.cobblestoneMossy.blockID, Block.glowStone.blockID, Block.stoneBrick.blockID, Block.blockIron.blockID, Block.stone.blockID, Block.grass.blockID
    };
    
    public static boolean isLoadable(int id) {
        for (int i = loadableIDs.length; i-- > 0;) if (id == loadableIDs[i])
            return true;
        return false;
    }
    
    /// Used to check if a recipe matches current crafting inventory.
    @Override
    public boolean matches(InventoryCrafting craftMatrix, World world) {
        byte status = 0;
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null)
                continue;
            if ((status & 1) == 0 && ingredient.itemID == _DungeonCrawler.rockshot.itemID) {
                if (ItemRockshot.getInfAmmo(ingredient))
                    return false;
                status |= 1;
            }
            else if ((status & 2) == 0 && ingredient.itemID == Item.gunpowder.itemID)
                status |= 2;
            else if ((status & 4) == 0 && isLoadable(ingredient.itemID)) {
                if (ingredient.itemID == Item.potion.itemID && !ItemPotion.isSplash(ingredient.getItemDamage()))
                    return false;
                status |= 4;
            }
            else
                return false;
        }
        return (status & 7) == 7;
    }

    /// Returns an item stack that is the result of this recipe.
    @Override
    public ItemStack getCraftingResult(InventoryCrafting craftMatrix) {
        ItemStack result = null;
        ItemStack ammo = null;
        byte status = 0;
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null)
                continue;
            if ((status & 1) == 0 && ingredient.itemID == _DungeonCrawler.rockshot.itemID) {
                result = ingredient.copy();
                status |= 1;
            }
            else if ((status & 2) == 0 && ingredient.itemID == Item.gunpowder.itemID)
                status |= 2;
            else if ((status & 4) == 0 && isLoadable(ingredient.itemID)) {
                ammo = ingredient.copy();
                status |= 4;
            }
            else
                return null;
        }
        if (result == null || ammo == null)
            return null;
        result.stackSize = 1;
        ammo.stackSize = 1;
        ItemRockshot.setAmmo(result, ammo);
        return result;
    }

    /// Returns the size of the recipe area.
    @Override
    public int getRecipeSize() {
        return 9;
    }
    
    /// Returns the output for the recipe.
    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }
}