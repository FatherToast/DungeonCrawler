package toast.dungeonCrawler;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityRock extends EntityThrowable
{
    public EntityRock(World world) {
        super(world);
        setSize(0.5F, 0.5F);
    }
    
    public EntityRock(World world, EntityLivingBase throwingEntity, ItemStack rock) {
        super(world, throwingEntity);
        setItemStack(rock);
        setSize(0.5F, 0.5F);
    }
    public EntityRock(World world, double x, double y, double z, ItemStack rock) {
        super(world, x, y, z);
        setItemStack(rock);
        setSize(0.5F, 0.5F);
    }
    
    /// Used to initialize dataWatcher variables.
    @Override
    protected void entityInit() {
        super.entityInit();
        /// itemStack; The item stack this entity rock represents.
        getDataWatcher().addObjectByDataType(31, 5);
    }
    
    /// Called when this EntityThrowable hits a block or entity.
    @Override
    protected void onImpact(MovingObjectPosition object) {
        ItemStack rock = getItemStack();
        if (object.entityHit != null) {
            ServersideAttributeMap attributeMap = new ServersideAttributeMap();
            attributeMap.applyAttributeModifiers(rock.getAttributeModifiers());
            AttributeInstance attributeInstance = attributeMap.getAttributeInstance(SharedMonsterAttributes.attackDamage);
            float damage = attributeInstance == null ? 0.0F : (float)attributeInstance.getAttributeValue();
            
            Block block = null;
            if (rock.itemID < Block.blocksList.length)
                block = Block.blocksList[rock.itemID];
            if (block != null) {
                Material material = block.blockMaterial;
                if (material == Material.rock) {
                    damage += 6.0F;
                    if (rock.itemID == Block.netherrack.blockID) {
                        object.entityHit.setFire(8);
                        damage -= 2.0F;
                    }
                }
                else if (material == Material.circuits) {
                    damage += 2.0F;
                    object.entityHit.setFire(5);
                }
                else if (material == Material.sand) {
                    damage += 3.0F;
                    if (rock.itemID == Block.slowSand.blockID && object.entityHit instanceof EntityLivingBase)
                        _DungeonCrawler.stackEffect((EntityLivingBase)object.entityHit, Potion.moveSlowdown, 160, 0, 5);
                }
                else if (material == Material.grass || material == Material.ground)
                    damage += 4.0F;
                else if (material == Material.glass)
                    damage += 4.0F;
                else if (material == Material.iron)
                    damage += 7.0F;
                else if (material == Material.wood)
                    damage += 5.0F;
                else if (material == Material.cake)
                    damage += 9001.0F;
                
                for (int i = 0; i < 16; i++)
                    worldObj.spawnParticle("largesmoke", posX + (rand.nextDouble() - 0.5), posY + (rand.nextDouble() - 0.5), posZ + (rand.nextDouble() - 0.5), 0.0, 0.0, 0.0);
                worldObj.playSoundAtEntity(this, block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch());
            }
            object.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), damage);
        }
        else {
            if (getThrower() instanceof EntityPlayer)
                if (!Item.itemsList[rock.itemID].onItemUse(rock, (EntityPlayer)getThrower(), worldObj, object.blockX, object.blockY, object.blockZ, object.sideHit, 0.0F, 0.0F, 0.0F))
                    entityDropItem(rock, 0.0F);
            for (int i = 0; i < 16; i++)
                worldObj.spawnParticle("largesmoke", object.blockX + rand.nextDouble() + Facing.offsetsXForSide[object.sideHit], object.blockY + rand.nextDouble() + Facing.offsetsYForSide[object.sideHit], object.blockZ + rand.nextDouble() + Facing.offsetsZForSide[object.sideHit], 0.0, 0.0, 0.0);
        }
        if (!worldObj.isRemote)
            setDead();
    }
    
    /// Protected helper method to write subclass entity data to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        if (getItemStack() != null)
            tag.setCompoundTag("Item", getItemStack().writeToNBT(new NBTTagCompound()));
    }

    /// Protected helper method to read subclass entity data from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setItemStack(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Item")));
        ItemStack rock = getItemStack();
        if (rock == null || rock.stackSize <= 0)
            setDead();
    }
    
    /// Get/set functions for itemStack.
    public ItemStack getItemStack() {
        return getDataWatcher().getWatchableObjectItemStack(31);
    }
    public void setItemStack(ItemStack itemStack) {
        getDataWatcher().updateObject(31, itemStack);
        getDataWatcher().setObjectWatched(31);
    }
}