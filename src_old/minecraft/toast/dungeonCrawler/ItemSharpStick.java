package toast.dungeonCrawler;

import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemSharpStick extends Item
{
	public ItemSharpStick(int id) {
		super(id);
		setMaxDamage(15);
        setCreativeTab(CreativeTabs.tabCombat);
        setMaxStackSize(1);
        setFull3D();
	}
    
    /// Returns the String name to display for this item.
    @SideOnly(Side.CLIENT)
    @Override
    public String getItemDisplayName(ItemStack itemStack) {
        return "Stick";
    }
    
    /// Allows items to add custom lines of information to the mouseover description.
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean UNKNOWN) {
        // Explain special effects
    }
    
    /// Called when an entity is hit with this item.
    @Override
	public boolean hitEntity(ItemStack itemStack, EntityLivingBase entityHit, EntityLivingBase attacker) {
        itemStack.damageItem(1, attacker);
		return true;
	}
    
    /// Returns a list of items with the same ID, but different metadata.
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs creativeTabs, List list) {
		list.add(ItemHelper.sharpStick());
    }
    
    /// Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
    @Override
    public Multimap getItemAttributeModifiers() {
        Multimap multimap = super.getItemAttributeModifiers();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e /** common item UUID */, "Weapon modifier", 3.0, 0));
        return multimap;
    }
}