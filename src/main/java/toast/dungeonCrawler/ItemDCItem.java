package toast.dungeonCrawler;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemDCItem extends Item {

    /** The icons used by this item. */
    public static IIcon[][] icons = new IIcon[ItemHelper.DCItemNames.length][];
    /** The items that use different tabs than the generic item. If the tab is set to tabMisc, the item will not appear in any tab. */
    public static final int[][] tabExceptions = new int[CreativeTabs.creativeTabArray.length][];
    static {
        ItemDCItem.setTabExceptions(CreativeTabs.tabCombat, "pebble");
        ItemDCItem.setTabExceptions(CreativeTabs.tabMaterials, "chain");
    }

    public ItemDCItem() {
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.setMaxStackSize(64);
        this.setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister) {
        for (int i = 0; i < ItemDCItem.icons.length; i++) {
            int length;
            if (ItemHelper.DCItemNames[i] == null || (length = ItemHelper.DCItemNames[i].length) < 2) {
                if (i == 0) {
					ItemDCItem.icons[0] = new IIcon[] { iconRegister.registerIcon("pebble") };
				} else {
					ItemDCItem.icons[i] = null;
				}
                continue;
            }
            ItemDCItem.icons[i] = new IIcon[length - 1];
            for (int j = 1; j < length; j++) {
				ItemDCItem.icons[i][j - 1] = iconRegister.registerIcon(ItemHelper.DCItemNames[i][j]);
			}
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int damage) {
        try {
            return ItemDCItem.icons[damage][0];
        }
        catch (Exception ex) {
            DungeonCrawlerMod.logDebug("Invalid DCItem: " + damage);
        }
        return ItemDCItem.icons[0][0];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getItemDisplayName(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage < 0 || damage >= ItemHelper.DCItemNames.length || ItemHelper.DCItemNames[damage] == null) {
        	DungeonCrawlerMod.logError("Tried to get name for DCItem with invalid damage value (" + Integer.toString(damage) + ")!");
            return "Invalid Item";
        }
        return ItemHelper.DCItemNames[damage][0];
    }

    /* Don't need this yet.
    // Returns true if the purple glow animation should be rendered.
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack itemStack, int pass) {
        int damage = itemStack.getItemDamage();
        if (damage > 31 && damage < 64)
            return true;
        return super.hasEffect(itemStack, int pass);
    }
    */

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        int damage = itemStack.getItemDamage();
        if (damage < 0 || damage >= ItemHelper.DCItemNames.length || ItemHelper.DCItemNames[damage] == null) {
        	DungeonCrawlerMod.logError("Tried to interact using a DCItem with invalid damage value (" + Integer.toString(damage) + ")!");
            return itemStack;
        }
        if (ItemHelper.DCItemNames[damage][1].equals("pebble")) {
            if (!player.capabilities.isCreativeMode) {
				itemStack.stackSize--;
			}
            world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (Item.itemRand.nextFloat() * 0.4F + 0.8F));
            if (!world.isRemote) {
				world.spawnEntityInWorld(new EntityPebble(world, player));
			}
        }
        else if (ItemHelper.isInRange(ItemHelper.GRENADE, damage)) {
            if (!player.capabilities.isCreativeMode) {
				itemStack.stackSize--;
			}
            world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (Item.itemRand.nextFloat() * 0.4F + 0.8F));
            if (!world.isRemote) {
				world.spawnEntityInWorld(new EntityGrenade(world, player, damage - ItemHelper.GRENADE));
			}
        }
        return itemStack;
    }

    @Override
	public int getMaxItemUseDuration(ItemStack itemStack) {
		return 32;
	}

    @Override
	public EnumAction getItemUseAction(ItemStack itemStack) {
        return EnumAction.bow;
	}

    @Override
    public void onUsingItemTick(ItemStack itemStack, EntityPlayer player, int useTime) {
    	DungeonCrawlerMod.proxy.trapping(itemStack, player, useTime);
    }

    @Override
    public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player) {
        int damage = itemStack.getItemDamage();
        if (ItemHelper.isInRange(ItemHelper.TRAP, damage) && DungeonCrawlerMod.trapData.containsKey(player)) {
            int[] data = DungeonCrawlerMod.trapData.get(player);
            if (data[4] < 26 || world.isRemote)
                return itemStack;
            Block block = world.getBlock(data[0], data[1], data[2]);
            if (block != null && block.getMaterial().blocksMovement()) {
                data[0] += Facing.offsetsXForSide[data[3]];
                data[1] += Facing.offsetsYForSide[data[3]];
                data[2] += Facing.offsetsZForSide[data[3]];
                block = world.getBlock(data[0], data[1], data[2]);
                if (block != null && block.getMaterial().blocksMovement())
                    return itemStack;
            }
            double yOffset = 0D;
            block = world.getBlock(data[0], data[1] - 1, data[2]);
            if (block == null || block instanceof BlockFence || !block.canPlaceTorchOnTop(world, data[0], data[1] - 1, data[2]))
                return itemStack;
            if (block instanceof BlockSlab && block.getBlockBoundsMaxY() == 0.5) {
				yOffset = -0.5D;
			}
            EntityTrap trap = new EntityTrap(world, damage - ItemHelper.TRAP, data[0] + 0.5, data[1] + yOffset, data[2] + 0.5);
            world.spawnEntityInWorld(trap);
            world.playSoundAtEntity(trap, Blocks.planks.stepSound.getBreakSound(), Blocks.planks.stepSound.getVolume(), Blocks.planks.stepSound.getPitch());
            player.stopUsingItem();
            player.swingItem();
            DungeonCrawlerMod.trapData.remove(player);
            if (!player.capabilities.isCreativeMode) {
				itemStack.stackSize--;
			}
        }
        return itemStack;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        int damage = itemStack.getItemDamage();
        if (ItemHelper.isInRange(ItemHelper.BASIC, damage)) {
			// Do nothing
		}
        else if (ItemHelper.isInRange(ItemHelper.TRAP, damage)) {
            player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
            DungeonCrawlerMod.trapData.put(player, new int[] { x, y, z, side, 1 });
        }
        else if (ItemHelper.isInRange(ItemHelper.BOMB, damage) || ItemHelper.isInRange(ItemHelper.MINE, damage)) {
            if (world.isRemote)
                return true;
            Block block = world.getBlock(x, y, z);
            if (block != null && block.getMaterial().blocksMovement()) {
                x += Facing.offsetsXForSide[side];
                y += Facing.offsetsYForSide[side];
                z += Facing.offsetsZForSide[side];
                block = world.getBlock(x, y, z);
                if (block != null && block.getMaterial().blocksMovement())
                    return true;
            }
            if (ItemHelper.isInRange(ItemHelper.BOMB, damage)) {
                double yOffset = 0.5;
                block = world.getBlock(x, y - 1, z);
                if (block instanceof BlockSlab) {
					yOffset = 0.0;
				} else if (block instanceof BlockFence) {
					yOffset = 1.0;
				}
                world.spawnEntityInWorld(new EntityBomb(world, player, damage - ItemHelper.BOMB, x + 0.5, y + yOffset, z + 0.5, side));
                world.playSoundAtEntity(player, Blocks.tnt.stepSound.getBreakSound(), Blocks.tnt.stepSound.getVolume(), Blocks.tnt.stepSound.getPitch());
            }
            else if (ItemHelper.isInRange(ItemHelper.MINE, damage)) {
                world.spawnEntityInWorld(new EntityMine(world, player, damage - ItemHelper.MINE, x, y, z));
                world.playSoundAtEntity(player, Blocks.stone.stepSound.getBreakSound(), Blocks.stone.stepSound.getVolume(), Blocks.stone.stepSound.getPitch());
            }
            if (!player.capabilities.isCreativeMode) {
				itemStack.stackSize--;
			}
            return true;
        }
        return false;
    }

    @Override
    public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[] {
            CreativeTabs.tabMisc,
            CreativeTabs.tabCombat,
            CreativeTabs.tabMaterials
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs creativeTabs, List list) {
        if (creativeTabs == CreativeTabs.tabMisc) {
            for (int i = 0; i < ItemHelper.DCItemNames.length; i++) if (ItemHelper.DCItemNames[i] != null && !ItemDCItem.isTabException(i)) {
				list.add(new ItemStack(id, 1, i));
			}
        }
        else if (creativeTabs != null) {
            int tab = creativeTabs.getTabIndex();
            if (ItemDCItem.tabExceptions[tab] != null) {
				for (int i = ItemDCItem.tabExceptions[tab].length; i-- > 0;) if (ItemHelper.DCItemNames[ItemDCItem.tabExceptions[tab][i]] != null) {
					list.add(new ItemStack(id, 1, ItemDCItem.tabExceptions[tab][i]));
				}
			}
        } else {
			for (int i = 0; i < ItemHelper.DCItemNames.length; i++) if (ItemHelper.DCItemNames[i] != null) {
				list.add(new ItemStack(id, 1, i));
			}
		}
    }

    // Marks the given items as creative tab exceptions.
    private static void setTabExceptions(CreativeTabs creativeTabs, String... exceptions) {
        if (exceptions.length <= 0)
            return;
        int allowed = 0;
        int[] ids = new int[exceptions.length];
        for (int i = exceptions.length; i-- > 0;) if ((ids[i] = ItemHelper.getDamageFor(exceptions[i])) >= 0) {
			allowed++;
		}
        int[] exceptionIDs = new int[allowed];
        for (int i = exceptions.length; i-- > 0 && allowed > 0;) if (ids[i] >= 0) {
			exceptionIDs[--allowed] = ids[i];
		}
        try {
            ItemDCItem.tabExceptions[creativeTabs.getTabIndex()] = exceptionIDs;
        }
        catch (Error err) {
        	// Do nothing
        }
    }

    // Returns true if the item is on the list of tab exceptions.
    @SideOnly(Side.CLIENT)
    public static boolean isTabException(int id) {
        for (int tab = ItemDCItem.tabExceptions.length; tab-- > 0;) if (ItemDCItem.tabExceptions[tab] != null) {
			for (int i = ItemDCItem.tabExceptions[tab].length; i-- > 0;) if (ItemDCItem.tabExceptions[tab][i] == id)
                return true;
		}
        return false;
    }
}