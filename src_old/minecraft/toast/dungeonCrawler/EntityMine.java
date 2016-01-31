package toast.dungeonCrawler;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.StepSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityMine extends Entity implements ITrap
{
    /// The entity that set this mine.
	public Entity trapper;
    /// Ticks until this mine explodes.
    public int fuse = -1;
    /// Whether this can only be triggered by a player.
    public boolean playerOnly = false;
    
    public EntityMine(World world) {
        this(world, 0, 0, 0, 0);
    }
    
    public EntityMine(World world, Entity entity, int t, int x, int y, int z) {
		this(world, t, (double)x + 0.5, (double)y, (double)z + 0.5);
		trapper = entity;
    }
    
    public EntityMine(World world, Entity entity, int t, double x, double y, double z) {
		this(world, t, x, y, z);
		trapper = entity;
    }
    
    public EntityMine(World world, int t, int x, int y, int z) {
		this(world, t, (double)x + 0.5, (double)y, (double)z + 0.5);
    }
    
	public EntityMine(World world, int t, double x, double y, double z) {
        super(world);
		setSize(0.875F, 0.0625F);
        setLocationAndAngles(x, y, z, 0.0F, 0.0F);
		isImmuneToFire = true;
		setType(t);
	}
    
    /// Used to initialize dataWatcher variables.
    @Override
    protected void entityInit() {
        /// type; The ID of this mine.
        dataWatcher.addObject(31, 0);
    }
    
    /// Returns whether this entity is in range to render.
    @Override
    public boolean isInRangeToRenderDist(double d) {
        double d1 = boundingBox.getAverageEdgeLength() * 4.0;
        d1 *= 64.0;
        return d < d1 * d1;
    }
    
    /// Called every tick whie this entity exists.
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (fuse > 0) {
            if (--fuse <= 0)
                trigger();
        }
        else {
            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.0, 0.0, 0.0));
            for (int i = 0; i < list.size(); i++) {
                Entity entity = list.get(i);
                if (entity instanceof ITrap) {
                    dropAsItem();
                    break;
                }
                if (entity instanceof EntityPlayer || (!playerOnly && (entity instanceof EntityLivingBase || entity instanceof EntityItem))) {
                    fuse = 20;
                    break;
                }
            }
        }
        motionY -= 0.08;
        motionY *= (double)0.98F;
        moveEntity(motionX, motionY, motionZ);
	}
    
    /// Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
    @Override
    public boolean interactFirst(EntityPlayer player) {
        ItemStack itemStack = player.inventory.getCurrentItem();
        if (itemStack == null)
            return super.interactFirst(player);
        boolean canDisarm = false;
        if (itemStack.itemID == Item.shears.itemID)
            canDisarm = true;
        else for (int i = 0; i < _DungeonCrawler.knife.length; i++)
            if (itemStack.itemID == _DungeonCrawler.knife[i].itemID) {
                canDisarm = true;
                break;
            }
        if (canDisarm) {
            itemStack.damageItem(1, player);
            if (itemStack.stackSize <= 0)
                player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
            player.swingItem();
            dropAsItem();
            return true;
        }
        return super.interactFirst(player);
    }
    
    /// Called to detonate the trap.
    @Override
	public void trigger() {
		ItemHelper.explodeByType(this, getType());
		setDead();
	}
    
    /// Called to disarm the trap and drop itself as a resource.
    @Override
	public void dropAsItem() {
        if (!playerOnly && !worldObj.isRemote)
            entityDropItem(ItemHelper.mine(getType()), 0F);
		setDead();
	}
    
    /// Saves this entity to the NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
		tag.setByte("type", (byte)getType());
        tag.setInteger("fuse", fuse);
        tag.setBoolean("playerOnly", playerOnly);
        if (trapper instanceof EntityPlayer)
            tag.setString("trapper", ((EntityPlayer)trapper).username);
    }
    
    /// Loads this entity from the NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        setType((int)tag.getByte("type"));
        fuse = tag.getInteger("fuse");
        playerOnly = tag.getBoolean("playerOnly");
        if (tag.hasKey("trapper"))
            trapper = worldObj.getPlayerEntityByName(tag.getString("trapper"));
    }
    
    /// Get/set functions for type.
    public int getType() {
        return dataWatcher.getWatchableObjectInt(31);
    }
    public void setType(int type) {
        dataWatcher.updateObject(31, type);
    }
    
    /// Returns true if this entity can be hit.
    @Override
    public boolean canBeCollidedWith()  {
        return true;
    }
    
    /// Called to deal damage to this entity. Returns true if damage is dealt.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
		fuse = 10;
        return damageSource.isProjectile();
    }
    
    /// Called when the mob is falling. Calculates and applies fall damage.
    @Override
    protected void fall(float distance) {
        super.fall(distance);
        int damage = MathHelper.ceiling_float_int(distance - 3.0F);
        if (damage > 0) {
            attackEntityFrom(DamageSource.fall, damage);
            int blockID = worldObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(posY - (double)0.2F - (double)yOffset), MathHelper.floor_double(posZ));
            if (blockID > 0) {
                StepSound stepSound = Block.blocksList[blockID].stepSound;
                worldObj.playSoundAtEntity(this, stepSound.getStepSound(), stepSound.getVolume() * 0.5F, stepSound.getPitch() * 0.75F);
            }
        }
    }
    
    /// Returns the shadow size of this entity.
    @Override
    public float getShadowSize() {
        return 0F;
    }
}