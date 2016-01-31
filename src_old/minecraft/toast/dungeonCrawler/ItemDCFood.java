package toast.dungeonCrawler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockHalfSlab;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Facing;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemDCFood extends ItemFood
{
    /// The food stats for this item.
    public static final int[] healAmounts = new int[ItemHelper.DCFoodNames.length];
    public static final float[] saturationModifiers = new float[ItemHelper.DCFoodNames.length];
    public static final boolean[] alwaysEdible = new boolean[ItemHelper.DCFoodNames.length];
    /// The icons used by this item.
    public static Icon[] icons = new Icon[ItemHelper.DCFoodNames.length];
    
    public ItemDCFood(int id) {
        super(id, 0, 0.0F, false);
        setMaxStackSize(64);
        setHasSubtypes(true);
    }
    
    /// Updates the icons for this item.
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister) {
        for (int i = 0; i < icons.length; i++) {
            if (ItemHelper.DCFoodNames[i] == null || ItemHelper.DCFoodNames[i].length != 2) {
                if (i == 0)
                    icons[0] = iconRegister.registerIcon("cookie_golden");
                else
                    icons[i] = null;
                continue;
            }
            icons[i] = iconRegister.registerIcon(ItemHelper.DCFoodNames[i][1]);
        }
    }
    
    /// Gets an icon based on an item's damage value.
    @SideOnly(Side.CLIENT)
    @Override
    public Icon getIconFromDamage(int damage) {
        try {
            return icons[damage];
        }
        catch (Exception ex) {
            _DungeonCrawler.debugConsole("Invalid DCFood: " + damage);
        }
        return icons[0];
    }
    
    /// Returns the String name to display for this item.
    @SideOnly(Side.CLIENT)
    @Override
    public String getItemDisplayName(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage < 0 || damage >= ItemHelper.DCFoodNames.length || ItemHelper.DCFoodNames[damage] == null) {
            _DungeonCrawler.debugException("Tried to get name for DCFood with invalid damage value (" + Integer.toString(damage) + ")!");
            return "Invalid Food";
        }
        return ItemHelper.DCFoodNames[damage][0];
    }
    
    /** Not yet needed.
    /// Returns true if the purple glow animation should be rendered.
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack itemStack, int pass) {
        int damage = itemStack.getItemDamage();
        if (damage > 31 && damage < 64)
            return true;
        return super.hasEffect(itemStack, pass);
    }
    */
    
    /// Called whenever this item is equipped and the right mouse button is pressed.
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        int damage = itemStack.getItemDamage();
        if (damage < 0 || damage >= ItemHelper.DCFoodNames.length || ItemHelper.DCFoodNames[damage] == null) {
            _DungeonCrawler.debugException("Tried to interact using a DCFood with invalid damage value (" + Integer.toString(damage) + ")!");
            return itemStack;
        }
        if (_DungeonCrawler.debug || player.canEat(alwaysEdible[damage]))
            player.setItemInUse(itemStack, getMaxItemUseDuration(itemStack));
        return itemStack;
    }
    
    /// Called when item use time has been used up.
    @Override
    public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player) {
        int damage = itemStack.getItemDamage();
        if (_DungeonCrawler.debug || !player.capabilities.isCreativeMode)
            itemStack.stackSize--;
        
        if (damage < 0 || damage >= ItemHelper.DCFoodNames.length || ItemHelper.DCFoodNames[damage] == null) {
            _DungeonCrawler.debugException("Tried to eat a DCFood with invalid damage value (" + Integer.toString(damage) + ")!");
            return itemStack;
        }
        player.getFoodStats().addStats(healAmounts[damage], saturationModifiers[damage]);
        world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
        if (!world.isRemote) {
            if (ItemHelper.DCFoodNames[damage][1].equals("zombie_meat")) {
                if (world.rand.nextFloat() < 0.1F)
                    player.addPotionEffect(new PotionEffect(Potion.hunger.id, 600, 0));
            }
            else if (ItemHelper.DCFoodNames[damage][1].equals("spider_soup") || ItemHelper.DCFoodNames[damage][1].equals("spider_bucket")) {
                switch (world.rand.nextInt(5)) {
                case 1:
                    player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 200, 0));
                    break;
                case 2:
                    player.addPotionEffect(new PotionEffect(Potion.damageBoost.id, 200, 0));
                    break;
                case 3:
                    player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
                    break;
                case 4:
                    player.addPotionEffect(new PotionEffect(Potion.invisibility.id, 200, 0));
                }
            }
            else if (ItemHelper.DCFoodNames[damage][1].equals("cookie_golden"))
                player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 200, 1));
        }
        
        if (_DungeonCrawler.debug || !player.capabilities.isCreativeMode) {
            if (ItemHelper.DCFoodNames[damage][1].equals("spider_soup")) {
                if (itemStack.stackSize <= 0)
                    return new ItemStack(Item.bowlEmpty);
                player.inventory.addItemStackToInventory(new ItemStack(Item.bowlEmpty));
            }
            else if (ItemHelper.DCFoodNames[damage][1].equals("spider_bucket") || ItemHelper.DCFoodNames[damage][1].equals("mushroom_bucket")) {
                if (itemStack.stackSize <= 0)
                    return new ItemStack(Item.bucketEmpty);
                player.inventory.addItemStackToInventory(new ItemStack(Item.bucketEmpty));
            }
        }
        return itemStack;
    }
    
    /// Returns a list of items with the same ID, but different metadata.
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs creativeTabs, List list) {
        for (int i = 0; i < ItemHelper.DCFoodNames.length; i++) if (ItemHelper.DCFoodNames[i] != null)
            list.add(new ItemStack(id, 1, i));
    }
}