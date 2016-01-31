package toast.dungeonCrawler;

import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemKnife extends Item
{
    /// The damage this item deals.
    private float weaponDamage;
    /// This item's material.
    public final EnumToolMaterial toolMaterial;
    
	public ItemKnife(int id, EnumToolMaterial material) {
		super(id);
        weaponDamage = 2.0F + material.getDamageVsEntity();
        toolMaterial = material;
		setMaxDamage(material.getMaxUses());
        setCreativeTab(CreativeTabs.tabTools);
        setMaxStackSize(1);
        setFull3D();
	}
    
    /// Returns the String name to display for this item.
    @SideOnly(Side.CLIENT)
    @Override
    public String getItemDisplayName(ItemStack itemStack) {
        return ItemHelper.getMaterialName(toolMaterial) + " Knife";
    }
    
    /// Allows items to add custom lines of information to the mouseover description.
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean UNKNOWN) {
        // Explain special effects
    }
    
    /// Called whenever this item is equipped and the right mouse button is pressed.
    @Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		player.setItemInUse(itemStack, getMaxItemUseDuration(itemStack));
		return itemStack;
	}
    
    /// Returns the max duration this item can be used for.
    @Override
	public int getMaxItemUseDuration(ItemStack itemStack) {
		return 72000;
	}
    
    /// Gets the action that should be executed while this item is in use.
    @Override
	public EnumAction getItemUseAction(ItemStack itemStack) {
        return EnumAction.bow;
	}
    
    /** For when knives become throwable.
    /// Called when the player releases the use item button.
    @Override
    public void onPlayerStoppedUsing(ItemStack itemStack, World world, EntityPlayer player, int useRemaining) {
        int useDuration = getMaxItemUseDuration(itemStack) - useRemaining;
        float power = (float)useDuration / 20F;
        power = (power * power + power * 2F) / 3F;
        if (power < 0.1F)
            return;
        if (power > 1F)
            power = 1F;
        if (!player.capabilities.isCreativeMode)
            itemStack.stackSize--;
        ItemStack partialStack = itemStack.copy();
        partialStack.stackSize = 1;
        world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        if (!world.isRemote)
            world.spawnEntityInWorld(new EntityKnife(world, player, power + 0.1F, partialStack));
    }
    */
    
    /// Returns the strength of the stack against a given block. 1F base, (Quality+1)*2 if correct blocktype, 1.5F if sword.
    @Override
    public float getStrVsBlock(ItemStack itemStack, Block block) {
        return (block.blockID == Block.workbench.blockID || block.blockID == Block.web.blockID) ? 18F : 1.8F;
    }
    
    /// Returns true if the item can harvest results from the block type.
    @Override
    public boolean canHarvestBlock(Block block) {
        return block.blockID == Block.web.blockID;
    }
    
    /// Called when an entity is hit with this item.
    @Override
	public boolean hitEntity(ItemStack itemStack, EntityLivingBase entityHit, EntityLivingBase attacker) {
        itemStack.damageItem(1, attacker);
		return true;
	}
    
    /// Called when a block is broken with this item.
    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World world, int blockID, int x, int y, int z, EntityLivingBase entityUsing) {
        if (blockID != Block.workbench.blockID && (double)Block.blocksList[blockID].getBlockHardness(world, x, y, z) != 0.0)
            itemStack.damageItem(1, entityUsing);
        return true;
    }
    
    /// Returns a list of items with the same ID, but different metadata.
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs creativeTabs, List list) {
		list.add(ItemHelper.knife(ItemHelper.getMaterialIndex(toolMaterial)));
    }
    
    /// True if this Item has a container item (a.k.a. crafting result).
    @Override
    public boolean hasContainerItem() {
        return true;
    }
    
    /// ItemStack sensitive version of getContainerItem. Returns a full ItemStack instance of the result.
    @Override
    public ItemStack getContainerItemStack(ItemStack itemStack) {
        ItemStack container = itemStack.copy();
        container.setItemDamage(itemStack.getItemDamage() + 1);
        if (container.getItemDamage() > container.getMaxDamage())
            container.stackSize--;
        return container;
    }
    
    /// If false, the container item will not be thrown back into the main inventory.
    @Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack itemStack) {
        return false;
    }
    
    /// Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
    @Override
    public Multimap getItemAttributeModifiers() {
        Multimap multimap = super.getItemAttributeModifiers();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e /** common item UUID */, "Tool modifier", (double)weaponDamage, 0));
        return multimap;
    }
}