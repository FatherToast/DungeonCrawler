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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemDCItem extends Item
{
    /// The icons used by this item.
    public static Icon[][] icons = new Icon[ItemHelper.DCItemNames.length][];
    /// The items that use different tabs than the generic item. If the tab is set to tabMisc, the item will not appear in any tab.
    public static final int[][] tabExceptions = new int[CreativeTabs.creativeTabArray.length][];
    static {
        setTabExceptions(CreativeTabs.tabCombat, "pebble");
        setTabExceptions(CreativeTabs.tabMaterials, "chain");
    }
    
    public ItemDCItem(int id) {
        super(id);
        setCreativeTab(CreativeTabs.tabMisc);
        setMaxStackSize(64);
        setHasSubtypes(true);
    }
    
    /// Updates the icons for this item.
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister) {
        for (int i = 0; i < icons.length; i++) {
            int length;
            if (ItemHelper.DCItemNames[i] == null || (length = ItemHelper.DCItemNames[i].length) < 2) {
                if (i == 0)
                    icons[0] = new Icon[] { iconRegister.registerIcon("pebble") };
                else
                    icons[i] = null;
                continue;
            }
            icons[i] = new Icon[length - 1];
            for (int j = 1; j < length; j++)
                icons[i][j - 1] = iconRegister.registerIcon(ItemHelper.DCItemNames[i][j]);
        }
    }
    
    /// Gets an icon based on an item's damage value.
    @SideOnly(Side.CLIENT)
    @Override
    public Icon getIconFromDamage(int damage) {
        try {
            return icons[damage][0];
        }
        catch (Exception ex) {
            _DungeonCrawler.debugConsole("Invalid DCItem: " + damage);
        }
        return icons[0][0];
    }
    
    /// Returns the String name to display for this item.
    @SideOnly(Side.CLIENT)
    @Override
    public String getItemDisplayName(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage < 0 || damage >= ItemHelper.DCItemNames.length || ItemHelper.DCItemNames[damage] == null) {
            _DungeonCrawler.debugException("Tried to get name for DCItem with invalid damage value (" + Integer.toString(damage) + ")!");
            return "Invalid Item";
        }
        return ItemHelper.DCItemNames[damage][0];
    }
    
    /** Don't need this yet.
    /// Returns true if the purple glow animation should be rendered.
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack itemStack, int pass) {
        int damage = itemStack.getItemDamage();
        if (damage > 31 && damage < 64)
            return true;
        return super.hasEffect(itemStack, int pass);
    }
    */
    
    /// Called whenever this item is equipped and the right mouse button is pressed.
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        int damage = itemStack.getItemDamage();
        if (damage < 0 || damage >= ItemHelper.DCItemNames.length || ItemHelper.DCItemNames[damage] == null) {
            _DungeonCrawler.debugException("Tried to interact using a DCItem with invalid damage value (" + Integer.toString(damage) + ")!");
            return itemStack;
        }
        if (ItemHelper.DCItemNames[damage][1].equals("pebble")) {
            if (!player.capabilities.isCreativeMode)
                itemStack.stackSize--;
            world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            if (!world.isRemote)
                world.spawnEntityInWorld(new EntityPebble(world, player));
        }
        else if (ItemHelper.isInRange(ItemHelper.GRENADE, damage)) {
            if (!player.capabilities.isCreativeMode)
                itemStack.stackSize--;
            world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            if (!world.isRemote)
                world.spawnEntityInWorld(new EntityGrenade(world, player, damage - ItemHelper.GRENADE));
        }
        return itemStack;
    }
    
    /// Returns the max duration this item can be used for.
    @Override
	public int getMaxItemUseDuration(ItemStack itemStack) {
		return 32;
	}
    
    /// Gets the action that should be executed while this item is in use.
    @Override
	public EnumAction getItemUseAction(ItemStack itemStack) {
        return EnumAction.bow;
	}
    
    /// Called each tick while using an item.
    @Override
    public void onUsingItemTick(ItemStack itemStack, EntityPlayer player, int useTime) {
        _DungeonCrawler.proxy.trapping(itemStack, player, useTime);
    }
    
    /// Called when item use time has been used up.
    @Override
    public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player) {
        int damage = itemStack.getItemDamage();
        if (ItemHelper.isInRange(ItemHelper.TRAP, damage) && _DungeonCrawler.trapData.containsKey(player)) {
            int[] data = _DungeonCrawler.trapData.get(player);
            if (data[4] < 26 || world.isRemote)
                return itemStack;
            Block block = Block.blocksList[world.getBlockId(data[0], data[1], data[2])];
            if (block != null && block.blockMaterial.blocksMovement()) {
                data[0] += Facing.offsetsXForSide[data[3]];
                data[1] += Facing.offsetsYForSide[data[3]];
                data[2] += Facing.offsetsZForSide[data[3]];
                block = Block.blocksList[world.getBlockId(data[0], data[1], data[2])];
                if (block != null && block.blockMaterial.blocksMovement())
                    return itemStack;
            }
            double yOffset = 0D;
            block = Block.blocksList[world.getBlockId(data[0], data[1] - 1, data[2])];
            if (block == null || block instanceof BlockFence || !block.canPlaceTorchOnTop(world, data[0], data[1] - 1, data[2]))
                return itemStack;
            if (block instanceof BlockHalfSlab && block.getBlockBoundsMaxY() == 0.5D)
                yOffset = -0.5D;
            EntityTrap trap = new EntityTrap(world, damage - ItemHelper.TRAP, (double)data[0] + 0.5D, (double)data[1] + yOffset, (double)data[2] + 0.5D);
            world.spawnEntityInWorld(trap);
            world.playSoundAtEntity(trap, Block.planks.stepSound.getStepSound(), Block.planks.stepSound.getVolume(), Block.planks.stepSound.getPitch());
            player.stopUsingItem();
            player.swingItem();
            _DungeonCrawler.trapData.remove(player);
            if (!player.capabilities.isCreativeMode)
                itemStack.stackSize--;
        }
        return itemStack;
    }
    
    /// Called when a block is right-clicked with the item.
    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        int damage = itemStack.getItemDamage();
        if (ItemHelper.isInRange(ItemHelper.BASIC, damage));
        else if (ItemHelper.isInRange(ItemHelper.TRAP, damage)) {
            player.setItemInUse(itemStack, getMaxItemUseDuration(itemStack));
            _DungeonCrawler.trapData.put(player, new int[] { x, y, z, side, 1 });
        }
        else if (ItemHelper.isInRange(ItemHelper.BOMB, damage) || ItemHelper.isInRange(ItemHelper.MINE, damage)) {
            if (world.isRemote)
                return true;
            Block block = Block.blocksList[world.getBlockId(x, y, z)];
            if (block != null && block.blockMaterial.blocksMovement()) {
                x += Facing.offsetsXForSide[side];
                y += Facing.offsetsYForSide[side];
                z += Facing.offsetsZForSide[side];
                block = Block.blocksList[world.getBlockId(x, y, z)];
                if (block != null && block.blockMaterial.blocksMovement())
                    return true;
            }
            if (ItemHelper.isInRange(ItemHelper.BOMB, damage)) {
                double yOffset = 0.5D;
                block = Block.blocksList[world.getBlockId(x, y - 1, z)];
                if (block instanceof BlockHalfSlab)
                    yOffset = 0D;
                else if (block instanceof BlockFence)
                    yOffset = 1D;
                world.spawnEntityInWorld(new EntityBomb(world, player, damage - ItemHelper.BOMB, (double)x + 0.5D, (double)y + yOffset, (double)z + 0.5D, side));
                world.playSoundAtEntity(player, Block.tnt.stepSound.getStepSound(), Block.tnt.stepSound.getVolume(), Block.tnt.stepSound.getPitch());
            }
            else if (ItemHelper.isInRange(ItemHelper.MINE, damage)) {
                world.spawnEntityInWorld(new EntityMine(world, player, damage - ItemHelper.MINE, x, y, z));
                world.playSoundAtEntity(player, Block.stone.stepSound.getStepSound(), Block.stone.stepSound.getVolume(), Block.stone.stepSound.getPitch());
            }
            if (!player.capabilities.isCreativeMode)
                itemStack.stackSize--;
            return true;
        }
        return false;
    }
    
    /// Gets a list of tabs that items belonging to this class can display on.
    @Override
    public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[] {
            CreativeTabs.tabMisc,
            CreativeTabs.tabCombat,
            CreativeTabs.tabMaterials
        };
    }
    
    /// Returns a list of items with the same ID, but different metadata.
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs creativeTabs, List list) {
        if (creativeTabs == CreativeTabs.tabMisc) {
            for (int i = 0; i < ItemHelper.DCItemNames.length; i++) if (ItemHelper.DCItemNames[i] != null && !isTabException(i))
                list.add(new ItemStack(id, 1, i));
        }
        else if (creativeTabs != null) {
            int tab = creativeTabs.getTabIndex();
            if (tabExceptions[tab] != null)
                for (int i = tabExceptions[tab].length; i-- > 0;) if (ItemHelper.DCItemNames[tabExceptions[tab][i]] != null)
                    list.add(new ItemStack(id, 1, tabExceptions[tab][i]));
        }
        else for (int i = 0; i < ItemHelper.DCItemNames.length; i++) if (ItemHelper.DCItemNames[i] != null)
            list.add(new ItemStack(id, 1, i));
    }
    
    /// Marks the given items as creative tab exceptions.
    private static void setTabExceptions(CreativeTabs creativeTabs, String... exceptions) {
        if (exceptions.length <= 0)
            return;
        int allowed = 0;
        int[] ids = new int[exceptions.length];
        for (int i = exceptions.length; i-- > 0;) if ((ids[i] = ItemHelper.getDamageFor(exceptions[i])) >= 0)
            allowed++;
        int[] exceptionIDs = new int[allowed];
        for (int i = exceptions.length; i-- > 0 && allowed > 0;) if (ids[i] >= 0)
            exceptionIDs[--allowed] = ids[i];
        try {
            tabExceptions[creativeTabs.getTabIndex()] = exceptionIDs;
        }
        catch (Error err) {
        }
    }
    
    /// Returns true if the item is on the list of tab exceptions.
    @SideOnly(Side.CLIENT)
    public static boolean isTabException(int id) {
        for (int tab = tabExceptions.length; tab-- > 0;) if (tabExceptions[tab] != null)
            for (int i = tabExceptions[tab].length; i-- > 0;) if (tabExceptions[tab][i] == id)
                return true;
        return false;
    }
}