package toast.dungeonCrawler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemRockshot extends Item
{
    /// The icons used by this item.
    public static Icon[] icons = new Icon[2];
    
    public ItemRockshot(int id) {
        super(id);
		setMaxDamage(127);
        setCreativeTab(CreativeTabs.tabCombat);
        setMaxStackSize(1);
        setFull3D();
    }
    
    /// Updates the icons for this item.
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister) {
        String path = "rockshot";
        icons[0] = iconRegister.registerIcon(path);
        icons[1] = iconRegister.registerIcon(path + "_loaded");
    }
    
    /// Gets an icon based on an item's damage value.
    @SideOnly(Side.CLIENT)
    @Override
    public Icon getIconFromDamage(int damage) {
        return icons[0];
    }
    
    /// Returns the icon of the stack.
    @Override
    public Icon getIcon(ItemStack itemStack, int renderPass) {
        if (getAmmo(itemStack) == null)
            return icons[0];
        return icons[1];
    }
    
    /// Returns the String name to display for this item.
    @SideOnly(Side.CLIENT)
    @Override
    public String getItemDisplayName(ItemStack itemStack) {
        if (getInfAmmo(itemStack))
            return "Boomshot";
        return "Rockshot";
    }
    
    /// Allows items to add custom lines of information to the mouseover description.
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean UNKNOWN) {
        ItemStack ammo = getAmmo(itemStack);
        if (ammo != null)
            list.add(ammo.getDisplayName() + (ammo.stackSize > 1 ? " x " + ammo.stackSize : ""));
    }
    
    /// Called whenever this item is equipped and the right mouse button is pressed.
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        ItemStack ammo = getAmmo(itemStack);
        if (ammo != null) {
            ItemStack shot = ammo.copy();
            shot.stackSize = 1;
            boolean infAmmo = player.capabilities.isCreativeMode || getInfAmmo(itemStack);
            if (!infAmmo) {
                ammo.stackSize--;
                if (ammo.stackSize <= 0)
                    ammo = null;
                setAmmo(itemStack, ammo);
                itemStack.damageItem(1, player);
            }
            if (!world.isRemote) {
                if (shot.itemID == Item.arrow.itemID) {
                    EntityArrow arrow = new EntityArrow(world, player, 2F);
                    arrow.setIsCritical(true);
                    arrow.setDamage(arrow.getDamage() + 0.5D);
                    arrow.setKnockbackStrength(1);
                    if (infAmmo)
                        arrow.canBePickedUp = 2;
                    world.spawnEntityInWorld(arrow);
                }
                else if (shot.itemID == Item.fireballCharge.itemID) {
                    float sinYaw = MathHelper.sin(player.rotationYaw / 180F * (float)Math.PI);
                    float sinPitch = MathHelper.sin(player.rotationPitch / 180F * (float)Math.PI);
                    float cosYaw = MathHelper.cos(player.rotationYaw / 180F * (float)Math.PI);
                    float cosPitch = MathHelper.cos(player.rotationPitch / 180F * (float)Math.PI);
                    EntitySmallFireball fireball = new EntitySmallFireball(world, player.posX - cosYaw * 0.16D, player.posY + player.getEyeHeight() - 0.1D, player.posZ - sinYaw * 0.16D, -sinYaw * cosPitch + itemRand.nextGaussian() * 0.05D, -sinPitch + itemRand.nextGaussian() * 0.05D, cosYaw * cosPitch + itemRand.nextGaussian() * 0.05D);
                    fireball.shootingEntity = player;
                    world.spawnEntityInWorld(fireball);
                }
                else if (shot.itemID == Block.tnt.blockID) {
                    float sinYaw = MathHelper.sin(player.rotationYaw / 180F * (float)Math.PI);
                    float sinPitch = MathHelper.sin(player.rotationPitch / 180F * (float)Math.PI);
                    float cosYaw = MathHelper.cos(player.rotationYaw / 180F * (float)Math.PI);
                    float cosPitch = MathHelper.cos(player.rotationPitch / 180F * (float)Math.PI);
                    EntityTNTPrimed tnt = new EntityTNTPrimed(world, player.posX - cosYaw * 0.16D, player.posY + player.getEyeHeight() - 0.1D, player.posZ - sinYaw * 0.16D, player);
                    tnt.motionX += -sinYaw * cosPitch + itemRand.nextGaussian() * 0.05D;
                    tnt.motionY += -sinPitch + itemRand.nextGaussian() * 0.05D;
                    tnt.motionZ += cosYaw * cosPitch + itemRand.nextGaussian() * 0.05D;
                    tnt.fuse = itemRand.nextInt((int)(tnt.fuse / 4)) + (int)(tnt.fuse / 8);
                    world.spawnEntityInWorld(tnt);
                }
                else if (shot.itemID == Item.potion.itemID && ItemPotion.isSplash(shot.getItemDamage())) {
                    EntityPotion potion = new EntityPotion(world, player, shot);
                    potion.motionX *= 1.5D;
                    potion.motionZ *= 1.5D;
                    world.spawnEntityInWorld(potion);
                }
                else {
                    EntityRock rock = new EntityRock(world, player, shot);
                    world.spawnEntityInWorld(rock);
                }
            }
            world.playSoundAtEntity(player, "random.explode", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        }
        return itemStack;
    }
    
    /// Returns a list of items with the same ID, but different metadata.
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs creativeTabs, List list) {
        ItemStack rockshot = new ItemStack(_DungeonCrawler.rockshot);
		list.add(setAmmo(rockshot.copy(), null));
		list.add(setAmmo(rockshot.copy(), new ItemStack(Block.cobblestone)));
		list.add(setAmmo(rockshot.copy(), new ItemStack(Block.torchWood)));
		list.add(setAmmo(rockshot.copy(), new ItemStack(Item.arrow)));
        
        setInfAmmo(rockshot, true);
		list.add(setAmmo(rockshot.copy(), new ItemStack(Block.tnt)));
		list.add(setAmmo(rockshot.copy(), new ItemStack(Block.cake)));
    }
    
    /// Sets the NBTTagCompound that determines this rockshot's attack.
    public static ItemStack setAmmo(ItemStack rockshot, ItemStack ammo) {
        if (rockshot == null)
            return null;
        if (ammo == null) {
            if (rockshot.stackTagCompound != null)
                rockshot.stackTagCompound.removeTag("ammo");
            return rockshot;
        }
        if (rockshot.stackTagCompound == null)
            rockshot.setTagCompound(new NBTTagCompound());
        rockshot.stackTagCompound.setCompoundTag("ammo", ammo.writeToNBT(new NBTTagCompound()));
        return rockshot;
    }
    
    /// Gets the NBTTagCompound that determines this rockshot's attack.
    public static ItemStack getAmmo(ItemStack rockshot) {
        if (rockshot == null || rockshot.stackTagCompound == null || !rockshot.stackTagCompound.hasKey("ammo"))
            return null;
        return ItemStack.loadItemStackFromNBT(rockshot.stackTagCompound.getCompoundTag("ammo"));
    }
    
    /// Sets the NBTTagCompound that determines infinite ammo capability.
    public static ItemStack setInfAmmo(ItemStack rockshot, boolean inf) {
        if (rockshot == null)
            return null;
        if (rockshot.stackTagCompound == null)
            rockshot.setTagCompound(new NBTTagCompound());
        rockshot.stackTagCompound.setBoolean("inf", inf);
        return rockshot;
    }
    
    /// Gets the NBTTagCompound that determines infinite ammo capability.
    public static boolean getInfAmmo(ItemStack rockshot) {
        if (rockshot == null || rockshot.stackTagCompound == null || !rockshot.stackTagCompound.hasKey("inf"))
            return false;
        return rockshot.stackTagCompound.getBoolean("inf");
    }
}