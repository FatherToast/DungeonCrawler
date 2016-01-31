package toast.dungeonCrawler;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityGasCloud extends Entity
{
    public int ticksAlive;

    public EntityGasCloud(World world) {
        this(world, 0.0, 0.0, 0.0);
    }

    public EntityGasCloud(Entity entity) {
		this(entity.worldObj, Math.floor(entity.posX) + 0.5, Math.floor(entity.posY) + 0.5, Math.floor(entity.posZ) + 0.5);
    }

    public EntityGasCloud(World world, int x, int y, int z) {
		this(world, x + 0.5, y + 0.5, z + 0.5);
    }

	public EntityGasCloud(World world, double x, double y, double z) {
        super(world);
		this.setSize(0.875F, 0.875F);
        this.setLocationAndAngles(x, y, z, 0.0F, 0.0F);
		this.isImmuneToFire = true;
        this.ticksAlive = 0;
	}

    @Override
    protected void entityInit() {
    	// Do nothing
    }

    /// Returns whether this entity is in range to render.
    @Override
    public boolean isInRangeToRenderDist(double d) {
        return false;
    }

    /// Called every tick whie this entity exists.
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.ticksAlive++ % 10 == 0 && this.ticksAlive > 20) {
            List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(2.0, 2.0, 2.0));
            for (Entity entity : entities)
                if (entity instanceof EntityLivingBase && entity.boundingBox.maxY > this.posY) {
					entity.attackEntityFrom(DamageSource.drown, 1);
				}
        }
        if (this.ticksAlive > 200) {
			this.setDead();
		}
        for (int i = 0; i < 32; i++) {
			this.worldObj.spawnParticle("largesmoke", this.posX, this.posY, this.posZ, (this.rand.nextDouble() - 0.5) * 0.3, -0.125, (this.rand.nextDouble() - 0.5) * 0.3);
		}
	}

    /// Saves this entity to the NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("ticksAlive", this.ticksAlive);
    }

    /// Loads this entity from the NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        this.ticksAlive = tag.getInteger("ticksAlive");
    }

    /// Called to deal damage to this entity. Returns true if damage is dealt.
    @Override
    public boolean attackEntityFrom(DamageSource ds, float i) {
        return false;
    }

    /// Returns the shadow size of this entity.
    @Override
    public float getShadowSize() {
        return 0.0F;
    }
}