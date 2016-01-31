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
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityKnife extends EntityThrowable
{
    public EntityKnife(World world) {
        super(world);
        setSize(0.5F, 0.5F);
    }
    
    public EntityKnife(World world, EntityLivingBase throwingEntity, float power, ItemStack knife) {
        super(world, throwingEntity);
        setItemStack(knife);
        setSize(0.5F, 0.5F);
        motionX *= (double)power;
        motionZ *= (double)power;
    }
    public EntityKnife(World world, double x, double y, double z, float power, ItemStack knife) {
        super(world, x, y, z);
        setItemStack(knife);
        setSize(0.5F, 0.5F);
        motionX *= (double)power;
        motionZ *= (double)power;
    }
    
    /// Used to initialize dataWatcher variables.
    @Override
    protected void entityInit() {
        super.entityInit();
        /// itemStack; The item stack this entity knife represents.
        getDataWatcher().addObjectByDataType(31, 5);
    }
    
    /// Called when this EntityThrowable hits a block or entity.
    @Override
    protected void onImpact(MovingObjectPosition object) {
        ItemStack knife = getItemStack();
        if (object.entityHit != null) {
            ServersideAttributeMap attributeMap = new ServersideAttributeMap();
            attributeMap.applyAttributeModifiers(knife.getAttributeModifiers());
            AttributeInstance attributeInstance = attributeMap.getAttributeInstance(SharedMonsterAttributes.attackDamage);
            float damage = attributeInstance == null ? 0.0F : (float)attributeInstance.getAttributeValue();
            
            if (object.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), damage))
                knife.damageItem(1, getThrower());
            if (knife.stackSize <= 0)
                setDead();
            setItemStack(knife);
        }
        else {
            int tile = worldObj.getBlockId(object.blockX, object.blockY, object.blockZ);
            NBTTagCompound tag = new NBTTagCompound();
            writeEntityToNBT(tag);
            tag.setShort("xTile", (short)object.blockX);
            tag.setShort("yTile", (short)object.blockY);
            tag.setShort("zTile", (short)object.blockZ);
            tag.setByte("inTile", (byte)tile);
            readEntityFromNBT(tag);
            motionX = (double)((float)(object.hitVec.xCoord - posX));
            motionY = (double)((float)(object.hitVec.yCoord - posY));
            motionZ = (double)((float)(object.hitVec.zCoord - posZ));
            float v = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
            posX -= motionX / (double)v * 0.05D;
            posY -= motionY / (double)v * 0.05D;
            posZ -= motionZ / (double)v * 0.05D;
            playSound("random.bowhit", 1F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
            inGround = true;
            throwableShake = 7;
            if (tile != 0)
                Block.blocksList[tile].onEntityCollidedWithBlock(worldObj, object.blockX, object.blockY, object.blockZ, this);
        }
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
        ItemStack knife = getItemStack();
        if (knife == null || knife.stackSize <= 0)
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