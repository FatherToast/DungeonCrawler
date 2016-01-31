package toast.dungeonCrawler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.DamageSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

public class ItemBoneArmor extends ItemArmor implements ISpecialArmor
{
	public ItemBoneArmor(int id, int render, int type) {
		super(id, _DungeonCrawler.boneArmorMaterial, render, type);
	}
    
    /// Returns the String name to display for this item.
    @SideOnly(Side.CLIENT)
    @Override
    public String getItemDisplayName(ItemStack itemStack) {
        String name = "Bone ";
        try {
            switch (((ItemArmor)itemStack.getItem()).armorType) {
                case 0:
                    return name + "Head";
                case 1:
                    return name + "Ribcage";
                case 2:
                    return name + "Legs";
                case 3:
                    return name + "Feet";
            }
        }
        catch (Exception ex) {
        }
        return name + "Armor";
    }
    
    /// Allows items to add custom lines of information to the mouseover description.
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean UNKNOWN) {
        // Explain special effects
    }
    
    /// The texture file for this armor while it is being worn.
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        if (slot == 2)
            return "textures/models/armor/bone_2.png";
        return "textures/models/armor/bone_1.png";
    }
    
    /// Returns a list of items with the same ID, but different metadata.
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs creativeTabs, List list) {
		list.add(ItemHelper.boneArmor(armorType));
    }
    
    /// Retrieves the modifiers to be used when calculating armor damage. Armors with higher priority will have damage applied to them before lower priority ones. If there are multiple pieces of armor with the same priority, damage will be distributed between them based on their absorption ratios.
    @Override
    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase wearer, ItemStack armor, DamageSource damageSource, double damage, int slot) {
        // Possibly alter this effect
        return new ISpecialArmor.ArmorProperties(0, (double)(damageReduceAmount + (damageSource.isProjectile() ? 3 : 0)) / 25.0, getMaxDamage() + 1 - armor.getItemDamage());
    }

    /// Get the displayed effective armor.
    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return damageReduceAmount;
    }

    /// Applies damage to the ItemStack.
    @Override
    public void damageArmor(EntityLivingBase wearer, ItemStack armor, DamageSource damageSource, int itemDamage, int slot) {
        armor.damageItem(itemDamage, wearer);
    }
}