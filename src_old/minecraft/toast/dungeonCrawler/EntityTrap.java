package toast.dungeonCrawler;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHalfSlab;
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

public class EntityTrap extends Entity implements ITrap
{
    /// The entity that set this trap.
	public Entity trapper;
    /// Whether this can only be triggered by a player.
    public boolean playerOnly;
    /// How many times this trap can be triggered.
    public int durability;
    /// The co-ordinates of the block this trap resides in.
    public int blockX, blockY, blockZ;
    
    public EntityTrap(World world) {
        this(world, 0, 0, 0, 0);
    }
    
    public EntityTrap(World world, Entity entity, int t, int x, int y, int z) {
		this(world, t, (double)x + 0.5D, (double)y, (double)z + 0.5D);
		trapper = entity;
    }
    
    public EntityTrap(World world, Entity entity, int t, double x, double y, double z) {
		this(world, t, x, y, z);
		trapper = entity;
    }
    
    public EntityTrap(World world, int t, int x, int y, int z) {
		this(world, t, (double)x + 0.5D, (double)y, (double)z + 0.5D);
    }
    
	public EntityTrap(World world, int t, double x, double y, double z) {
        super(world);
		setSize(0.875F, 0.0625F);
        setLocationAndAngles(x, y, z, 0F, 0F);
		isImmuneToFire = true;
		setType(t);
        playerOnly = false;
        blockX = (int)Math.floor(x);
        blockY = (int)Math.floor(y);
        blockZ = (int)Math.floor(z);
        durability = t == ItemHelper.getTypeFor(ItemHelper.TRAP, "trap_spike") || t == ItemHelper.getTypeFor(ItemHelper.TRAP, "trap_spike_poison") ? 16 : -1;
        setRandomTick(t == ItemHelper.getTypeFor(ItemHelper.TRAP, "trap_fire"));
	}
    
    /// Used to initialize dataWatcher variables.
    @Override
    protected void entityInit() {
        /// randomTick; (boolean) Whether the randomUpdate() function should be called.
        dataWatcher.addObject(29, (byte)0);
        /// isSet; (boolean) Whether this trap is set.
        dataWatcher.addObject(30, (byte)1);
        /// type; The ID of this trap.
        dataWatcher.addObject(31, 0);
    }
    
    /// Returns whether this entity is in range to render.
    @Override
    public boolean isInRangeToRenderDist(double d) {
        double d1 = boundingBox.getAverageEdgeLength() * 4D;
        d1 *= 64D;
        return d < d1 * d1;
    }
    
    /// Called every tick whie this entity exists.
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (durability == 0 && rand.nextInt(200) == 0) {
            setDead();
            return;
        }
        if (!worldObj.isRemote) {
            Block block = Block.blocksList[worldObj.getBlockId(blockX, blockY, blockZ)];
            if (block != null && block.blockMaterial.blocksMovement() && !(block instanceof BlockHalfSlab))
                setDead();
            block = Block.blocksList[worldObj.getBlockId(blockX, blockY - 1, blockZ)];
            if (block == null || !block.canPlaceTorchOnTop(worldObj, blockX, blockY - 1, blockZ))
                setDead();
        }
        if (getRandomTick() && rand.nextInt(8) == 0)
            randomUpdate();
        if (getIsSet()) {
            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0D, 0D, 0D));
            for (int i = 0; i < list.size(); i++) {
                Entity entity = list.get(i);
                if (entity instanceof ITrap) {
                    setDead();
                    break;
                }
                if (entity instanceof EntityPlayer || (!playerOnly && (entity instanceof EntityLivingBase || entity instanceof EntityItem))) {
                    trigger();
                    break;
                }
            }
        }
	}
    
    /// Called randomly, roughly every 8 ticks.
    public void randomUpdate() {
        if (getIsSet()) {
            worldObj.spawnParticle("smoke", posX, posY + 0.125D, posZ, 0D, 0D, 0D);
            worldObj.spawnParticle("flame", posX, posY + 0.125D, posZ, 0D, 0D, 0D);
        }
    }
    
    /// Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (durability == 0) {
            setDead();
            return false;
        }
        ItemStack itemStack = player.getCurrentItemOrArmor(0);
        if (!getIsSet()) {
            List<Entity> list = (List<Entity>)worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0D, 0D, 0D));
            for (Entity entity : list) if (entity instanceof EntityPlayer || (!playerOnly && (entity instanceof EntityLivingBase || entity instanceof EntityItem)))
                return super.interactFirst(player);
            boolean reset = false;
            int type = ItemHelper.TRAP + getType();
            if (type == ItemHelper.getDamageFor(ItemHelper.TRAP, "trap_fire")) {
                if (itemStack != null && itemStack.itemID == Item.coal.itemID) {
                    if (!player.capabilities.isCreativeMode)
                        itemStack.stackSize--;
                    if (itemStack.stackSize <= 0)
                        player.setCurrentItemOrArmor(0, itemStack = null);
                    playSound("random.click", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
                    reset = true;
                }
            }
            else if (type == ItemHelper.getDamageFor(ItemHelper.TRAP, "trap_end")) {
                if (itemStack != null && itemStack.itemID == Item.enderPearl.itemID) {
                    if (!player.capabilities.isCreativeMode)
                        itemStack.stackSize--;
                    if (itemStack.stackSize <= 0)
                        player.setCurrentItemOrArmor(0, itemStack = null);
                    playSound("random.click", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
                    reset = true;
                }
            }
            else if (type == ItemHelper.getDamageFor(ItemHelper.TRAP, "trap_screamer")) {
                if (itemStack != null && itemStack.itemID == Item.reed.itemID) {
                    if (!player.capabilities.isCreativeMode)
                        itemStack.stackSize--;
                    if (itemStack.stackSize <= 0)
                        player.setCurrentItemOrArmor(0, itemStack = null);
                    playSound("mob.ghast.affectionate scream", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
                    reset = true;
                }
            }
            else if (type == ItemHelper.getDamageFor(ItemHelper.TRAP, "trap_launcher")) {
                playSound("tile.piston.in", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
                reset = true;
            }
            else {
                playSound("random.click", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
                reset = true;
            }
            if (reset) {
                player.swingItem();
                setIsSet(true);
                return true;
            }
        }
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
        if (getIsSet())
            ItemHelper.triggerTrapByType(this, getType());
		setIsSet(false);
	}
    
    /// Called to disarm the trap and drop itself as a resource.
    @Override
	public void dropAsItem() {
        if (!playerOnly && !worldObj.isRemote && durability < 0)
            entityDropItem(ItemHelper.trap(getType()), 0F);
		setDead();
	}
    
    /// Saves this entity to the NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
		tag.setByte("type", (byte)getType());
		tag.setByte("uses", (byte)durability);
        tag.setBoolean("set", getIsSet());
        tag.setInteger("blockX", blockX);
        tag.setInteger("blockY", blockY);
        tag.setInteger("blockZ", blockZ);
        tag.setBoolean("playerOnly", playerOnly);
        if (trapper instanceof EntityPlayer)
            tag.setString("trapper", ((EntityPlayer)trapper).username);
    }
    
    /// Loads this entity from the NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        setType((int)tag.getByte("type"));
        durability = (int)tag.getByte("uses");
        setIsSet(tag.getBoolean("set"));
        blockX = tag.getInteger("blockX");
        blockY = tag.getInteger("blockY");
        blockZ = tag.getInteger("blockZ");
        playerOnly = tag.getBoolean("playerOnly");
        if (tag.hasKey("trapper"))
            trapper = worldObj.getPlayerEntityByName(tag.getString("trapper"));
        setRandomTick(getType() == ItemHelper.getTypeFor(ItemHelper.TRAP, "trap_fire"));
    }
    
    /// Functions controlling this entity's durability.
    public void damageTrap() {
        if (durability > 0)
            durability--;
    }
    
    /// Get/set functions for randomTick.
    public boolean getRandomTick() {
        return dataWatcher.getWatchableObjectByte(29) == 1;
    }
    public void setRandomTick(boolean tick) {
        dataWatcher.updateObject(29, tick ? (byte)1 : (byte)0);
    }
    
    /// Get/set functions for isSet.
    public boolean getIsSet() {
        return dataWatcher.getWatchableObjectByte(30) == 1;
    }
    public void setIsSet(boolean tick) {
        dataWatcher.updateObject(30, tick ? (byte)1 : (byte)0);
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
        if (damageSource.getSourceOfDamage() instanceof EntityPlayer && ((EntityPlayer)damageSource.getSourceOfDamage()).capabilities.isCreativeMode)
            setDead();
        return false;
    }
    
    /// Returns the shadow size of this entity.
    @Override
    public float getShadowSize() {
        return 0F;
    }
}