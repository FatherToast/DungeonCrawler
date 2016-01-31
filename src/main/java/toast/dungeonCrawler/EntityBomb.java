package toast.dungeonCrawler;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityBomb extends Entity implements ITrap {

    // The entity that set this bomb.
	public Entity trapper;
    // Ticks until this bomb explodes.
    public int fuse = 80;
    // The block this is attached to. Blocks.air if not attached.
    public Block blockAttached = Blocks.air;
    // The block this bomb is attached to.
    public int blockX, blockY, blockZ;

    public EntityBomb(World world) {
        this(world, 0, 0, 0, 0, 0);
    }

    public EntityBomb(World world, Entity entity, int type, int x, int y, int z, int side) {
		this(world, type, x + 0.5, y + 0.5, z + 0.5, side);
		this.trapper = entity;
    }

    public EntityBomb(World world, Entity entity, int type, double x, double y, double z, int side) {
		this(world, type, x, y, z, side);
		this.trapper = entity;
    }

    public EntityBomb(World world, int type, int x, int y, int z, int side) {
		this(world, type, x + 0.5, y + 0.5, z + 0.5, side);
    }

	public EntityBomb(World world, int type, double x, double y, double z, int side) {
        super(world);
		this.setSize(0.5F, 0.5F);
        this.setLocationAndAngles(x, y, z, 0.0F, 0.0F);
		this.isImmuneToFire = true;
		this.setType(type);
        this.blockX = MathHelper.floor_double(x) - Facing.offsetsXForSide[side];
        this.blockY = MathHelper.floor_double(y) - Facing.offsetsYForSide[side];
        this.blockZ = MathHelper.floor_double(z) - Facing.offsetsZForSide[side];
        this.blockAttached = world.getBlock(this.blockX, this.blockY, this.blockZ);
	}

    @Override
    protected void entityInit() {
        // type; The ID of this bomb.
        this.dataWatcher.addObject(31, 0);
    }

    @Override
    public boolean isInRangeToRenderDist(double d) {
        double d1 = this.boundingBox.getAverageEdgeLength() * 4.0;
        d1 *= 64.0;
        return d < d1 * d1;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (--this.fuse <= 0) {
			this.trigger();
		}
        /* Code to make bombs fall when attached block is changed. Currently OOC.
        if (blockId > 0) if (blockId != worldObj.getBlockId(blockX, blockY, blockZ))
            blockId = -1;
        else {
            motionY -= 0.08;
            motionY *= 0.98;
            moveEntity(motionX, motionY, motionZ);
        }
        */
	}

    @Override
	public void trigger() {
		ItemHelper.explodeByType(this, this.getType());
		this.setDead();
	}

    @Override
	public void dropAsItem() {
        if (!this.worldObj.isRemote) {
			this.entityDropItem(ItemHelper.impBomb(this.getType()), 0F);
		}
		this.setDead();
	}

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
		tag.setByte("type", (byte)this.getType());
        tag.setInteger("fuse", this.fuse);
        tag.setInteger("blockId", Block.getIdFromBlock(this.blockAttached));
        if (this.blockAttached != Blocks.air) {
            tag.setInteger("blockX", this.blockX);
            tag.setInteger("blockY", this.blockY);
            tag.setInteger("blockZ", this.blockZ);
        }
        if (this.trapper instanceof EntityPlayer) {
			tag.setString("trapper", ((EntityPlayer) this.trapper).getCommandSenderName());
		}
    }
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        this.setType(tag.getByte("type"));
        this.fuse = tag.getInteger("fuse");
        this.blockAttached = Block.getBlockById(tag.getInteger("blockId"));
        if (this.blockAttached != Blocks.air) {
            this.blockX = tag.getInteger("blockX");
            this.blockY = tag.getInteger("blockY");
            this.blockZ = tag.getInteger("blockZ");
        } else {
			this.blockX = this.blockY = this.blockZ = 0;
		}
        if (tag.hasKey("trapper")) {
			this.trapper = this.worldObj.getPlayerEntityByName(tag.getString("trapper"));
		}
    }

    // Get/set functions for type.
    public int getType() {
        return this.dataWatcher.getWatchableObjectInt(31);
    }
    public void setType(int type) {
        this.dataWatcher.updateObject(31, type);
    }

    @Override
    public boolean canBeCollidedWith()  {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
		this.fuse -= (int)damage << 3;
        return damageSource.isProjectile();
    }

    @Override
    protected void fall(float distance) {
        super.fall(distance);
        int damage = MathHelper.ceiling_float_int(distance - 3.0F);
        if (damage > 0) {
            this.attackEntityFrom(DamageSource.fall, damage);
            Block block = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY - 0.2 - this.yOffset), MathHelper.floor_double(this.posZ));
            if (block != null) {
                SoundType stepSound = block.stepSound;
                this.worldObj.playSoundAtEntity(this, stepSound.getBreakSound(), stepSound.getVolume() * 0.5F, stepSound.getPitch() * 0.75F);
            }
        }
    }

    @Override
    public float getShadowSize() {
        return 0.0F;
    }
}