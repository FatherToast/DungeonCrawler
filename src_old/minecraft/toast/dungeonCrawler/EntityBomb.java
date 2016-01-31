package toast.dungeonCrawler;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.StepSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityBomb extends Entity implements ITrap
{
    /// The entity that set this bomb.
	public Entity trapper;
    /// Ticks until this bomb explodes.
    public int fuse = 80;
    /// The ID of the block this is attached to. -1 if not attached.
    public int blockId = -1;
    /// The block this bomb is attached to.
    public int blockX, blockY, blockZ;
    
    public EntityBomb(World world) {
        this(world, 0, 0, 0, 0, 0);
    }
    
    public EntityBomb(World world, Entity entity, int type, int x, int y, int z, int side) {
		this(world, type, (double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, side);
		trapper = entity;
    }
    
    public EntityBomb(World world, Entity entity, int type, double x, double y, double z, int side) {
		this(world, type, x, y, z, side);
		trapper = entity;
    }
    
    public EntityBomb(World world, int type, int x, int y, int z, int side) {
		this(world, type, (double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, side);
    }
    
	public EntityBomb(World world, int type, double x, double y, double z, int side) {
        super(world);
		setSize(0.5F, 0.5F);
        setLocationAndAngles(x, y, z, 0F, 0F);
		isImmuneToFire = true;
		setType(type);
        blockX = MathHelper.floor_double(x) - Facing.offsetsXForSide[side];
        blockY = MathHelper.floor_double(y) - Facing.offsetsYForSide[side];
        blockZ = MathHelper.floor_double(z) - Facing.offsetsZForSide[side];
        Block block = Block.blocksList[world.getBlockId(blockX, blockY, blockZ)];
        if (block != null && block.blockMaterial.blocksMovement())
            blockId = block.blockID;
        else
            blockId = -1;
	}
    
    /// Used to initialize dataWatcher variables.
    @Override
    protected void entityInit() {
        /// type; The ID of this bomb.
        dataWatcher.addObject(31, 0);
    }
    
    /// Returns whether this entity is in range to render.
    @Override
    public boolean isInRangeToRenderDist(double d) {
        double d1 = boundingBox.getAverageEdgeLength() * 4D;
        d1 *= 64D;
        return d < d1 * d1;
    }
    
    /// Called every tick while this entity exists.
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (--fuse <= 0)
            trigger();
        /* Code to make bombs fall when attached block is changed. Currently OOC.
        if (blockId > 0) if (blockId != worldObj.getBlockId(blockX, blockY, blockZ))
            blockId = -1;
        else {
            motionY -= 0.08D;
            motionY *= 0.98D;
            moveEntity(motionX, motionY, motionZ);
        }
        */
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
        if (!worldObj.isRemote)
            entityDropItem(ItemHelper.impBomb(getType()), 0F);
		setDead();
	}
    
    /// Saves this entity to the NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
		tag.setByte("type", (byte)getType());
        tag.setInteger("fuse", fuse);
        tag.setByte("blockId", (byte)blockId);
        if (blockId > 0) {
            tag.setInteger("blockX", blockX);
            tag.setInteger("blockY", blockY);
            tag.setInteger("blockZ", blockZ);
        }
        if (trapper instanceof EntityPlayer)
            tag.setString("trapper", ((EntityPlayer)trapper).username);
    }
    
    /// Loads this entity from the NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        setType((int)tag.getByte("type"));
        fuse = tag.getInteger("fuse");
        blockId = tag.getByte("blockId");
        if (blockId > 0) {
            blockX = tag.getInteger("blockX");
            blockY = tag.getInteger("blockY");
            blockZ = tag.getInteger("blockZ");
        }
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
        return false;
    }
    
    /// Called to deal damage to this entity. Returns true if damage is dealt.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
		fuse -= (int)damage << 3;
        return damageSource.isProjectile();
    }
    
    /// Called when the mob is falling. Calculates and applies fall damage.
    @Override
    protected void fall(float distance) {
        super.fall(distance);
        int damage = MathHelper.ceiling_float_int(distance - 3F);
        if (damage > 0) {
            attackEntityFrom(DamageSource.fall, damage);
            Block block = Block.blocksList[worldObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(posY - 0.2D - (double)yOffset), MathHelper.floor_double(posZ))];
            if (block != null) {
                StepSound stepSound = block.stepSound;
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