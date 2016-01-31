package toast.dungeonCrawler;

import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;

public abstract class ItemHelper
{
    /// The UUID for secondary item attributes.
    public static final UUID SECONDARY_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5D0");
    /// Array of all materials.
    public static final EnumToolMaterial[] materials = {
        EnumToolMaterial.WOOD, EnumToolMaterial.STONE, EnumToolMaterial.IRON, EnumToolMaterial.EMERALD, EnumToolMaterial.GOLD
    };
    /// Array of item IDs corresponding to each material.
    public static final int[] materialIDs = {
        Block.planks.blockID, Block.cobblestone.blockID, Item.ingotIron.itemID, Item.diamond.itemID, Item.ingotGold.itemID
    };
    /// DCItem type start points.
        public static final int BASIC = 0;
        public static final int BOMB = 1 << 6;
        public static final int GRENADE = 2 << 6; // NI
        public static final int MINE = 3 << 6;
        public static final int TRAP = 4 << 6;
    /// Array of item names and icon names for DCItem.
    public static final String[][] DCItemNames = new String[5 << 6][];
    
    /// DCFood type start points.
        public static final int FOOD_BASIC = 0;
        public static final int FOOD_SMELT = 1 << 6;
        public static final int FOOD_CRAFT = 2 << 6;
    /// Array of item names and icon names for DCFood.
    public static final String[][] DCFoodNames = new String[3 << 6][];
    
    /// Executes a bomb/mine's explosion.
    public static void explodeByType(Entity exploder, int type) {
        float power = type % 3 + 1.0F;
        if (exploder instanceof EntityMine)
            exploder.posY += 0.0625;
        if (type / 3.0F < 1.0F)
            _DungeonCrawler.explode(exploder, power);
        else if (type / 3.0F < 2.0F)
            _DungeonCrawler.explodeFire(exploder, power);
        else if (type / 3.0F < 3.0F)
            _DungeonCrawler.explodeWarp(exploder, power);
        else if (type == getTypeFor(MINE, "mine_gas")) {
            EntityGasCloud gasCloud = new EntityGasCloud(exploder);
            exploder.worldObj.spawnEntityInWorld(gasCloud);
            _DungeonCrawler.explosion(exploder, power).doExplosionB(true);
        }
    }
    
    /// Executes a trap's trigger effect.
    public static void triggerTrapByType(EntityTrap trap, int type) {
        type += TRAP;
        List<Entity> list = (List<Entity>)trap.worldObj.getEntitiesWithinAABBExcludingEntity(trap, trap.boundingBox.expand(0.125, 1.0, 0.125));
        for (int i = 0; i < list.size(); i++) {
            Entity entity = list.get(i);
            if (entity instanceof ITrap)
                ((ITrap)entity).trigger();
            else if (entity instanceof EntityItem)
                entity.setDead();
        }
        if (type == getDamageFor(TRAP, "trap_spike")) {
            for (Entity entity : list) {
                if (entity instanceof EntityLivingBase && (entity.attackEntityFrom(DamageSource.cactus, 9) || _DungeonCrawler.debug))
                    if (entity.motionY < 0.36)
                        entity.motionY = Math.min(entity.motionY + 0.36, 0.36);
            }
            trap.worldObj.playSoundAtEntity(trap, "tile.piston.out", 0.5F, 0.4F / (_DungeonCrawler.random.nextFloat() * 0.4F + 0.8F));
            trap.damageTrap();
        }
        else if (type == getDamageFor(TRAP, "trap_spike_poison")) {
            for (Entity entity : list) {
                if (entity instanceof EntityLivingBase && (entity.attackEntityFrom(DamageSource.cactus, 4) || _DungeonCrawler.debug)) {
                    _DungeonCrawler.stackEffect((EntityLivingBase)entity, Potion.poison, 200, 0, 2);
                    if (entity.motionY < 0.36)
                        entity.motionY = Math.min(entity.motionY + 0.36, 0.36);
                }
            }
            trap.worldObj.playSoundAtEntity(trap, "tile.piston.out", 0.5F, 0.4F / (_DungeonCrawler.random.nextFloat() * 0.4F + 0.8F));
            trap.damageTrap();
        }
        else if (type == getDamageFor(TRAP, "trap_fire")) {
            for (int x = -1; x <= 1; x++)
                for (int y = -1; y <= 1; y++)
                    for (int z = -1; z <= 1; z++) {
                        Block block = Block.blocksList[trap.worldObj.getBlockId(trap.blockX + x, trap.blockY + y, trap.blockZ + z)];
                        if (block == null || block.blockMaterial.isReplaceable())
                            trap.worldObj.setBlock(trap.blockX + x, trap.blockY + y, trap.blockZ + z, Block.fire.blockID, 0, 2);
                    }
            trap.worldObj.playSoundAtEntity(trap, "mob.ghast.fireball", 0.5F, 0.4F / (_DungeonCrawler.random.nextFloat() * 0.4F + 0.8F));
        }
        else if (type == getDamageFor(TRAP, "trap_end")) {
            for (Entity entity : list) {
                if (entity instanceof EntityLivingBase) {
                    for (int j = 0; j < 60; j++)
                        if (_DungeonCrawler.teleportRandomly(entity))
                            break;
                    entity.fallDistance = 0.0F;
                    entity.attackEntityFrom(DamageSource.fall, 5);
                }
            }
        }
        else if (type == getDamageFor(TRAP, "trap_screamer")) {
            for (Entity entity : list) {
                if (entity instanceof EntityLivingBase) {
                    _DungeonCrawler.stackEffect((EntityLivingBase)entity, Potion.moveSlowdown, 160, 1, 5);
                    _DungeonCrawler.stackEffect((EntityLivingBase)entity, Potion.weakness, 160, 1, 4);
                    _DungeonCrawler.stackEffect((EntityLivingBase)entity, Potion.confusion, 200, 0, 0);
                }
            }
            trap.worldObj.playSoundAtEntity(trap, "mob.ghast.scream", 10.0F, 0.4F / (_DungeonCrawler.random.nextFloat() * 0.4F + 0.8F));
        }
        else if (type == getDamageFor(TRAP, "trap_launcher")) {
            for (Entity entity : list) {
                if (entity instanceof EntityLivingBase && entity.motionY < 1.6)
                    entity.motionY = Math.min(entity.motionY + 1.6, 1.6);
            }
            trap.worldObj.playSoundAtEntity(trap, "tile.piston.out", 0.5F, 0.4F / (_DungeonCrawler.random.nextFloat() * 0.4F + 0.8F));
        }
        else if (type == getDamageFor(TRAP, "trap_crippling")) {
            for (Entity entity : list) {
                if (entity instanceof EntityLivingBase && (entity.attackEntityFrom(DamageSource.generic, 5) || _DungeonCrawler.debug)) {
                    _DungeonCrawler.stackEffect((EntityLivingBase)entity, Potion.moveSlowdown, 160, 5, 5);
                    if (entity.motionY > -1.0)
                        entity.motionY = Math.max(entity.motionY - 1.0, -1.0);
                }
            }
            trap.worldObj.playSoundAtEntity(trap, "tile.piston.in", 0.5F, 0.4F / (_DungeonCrawler.random.nextFloat() * 0.4F + 0.8F));
        }
    }
    
    /// Returns an ItemStack knife based on the argument(s) given.
    public static ItemStack knife(String materialName) {
        return knife(materialName, false);
    }
    public static ItemStack knife(int materialIndex) {
        return knife(materialIndex, false);
    }
    public static ItemStack knife(String materialName, boolean ingredient) {
        return knife(getMaterialIndex(materialName), ingredient);
    }
    public static ItemStack knife(int materialIndex, boolean ingredient) {
        if (materialIndex < 0 || materialIndex >= materials.length) {
            _DungeonCrawler.debugException("Tried to create knife with invalid material index (" + Integer.toString(materialIndex) + ")!");
            materialIndex = 0;
        }
        return new ItemStack(_DungeonCrawler.knife[materialIndex], 1, ingredient ? 32767 : 0);
    }
    
    /// Returns an ItemStack sharp stick based on the argument given.
    public static ItemStack sharpStick() {
        return sharpStick(false);
    }
    public static ItemStack sharpStick(boolean ingredient) {
        return new ItemStack(_DungeonCrawler.sharpStick, 1, ingredient ? 32767 : 0);
    }
    
    /// Returns an ItemStack bone club based on the argument given.
    public static ItemStack boneClub() {
        return boneClub(false);
    }
    public static ItemStack boneClub(boolean ingredient) {
        return new ItemStack(_DungeonCrawler.boneClub, 1, ingredient ? 32767 : 0);
    }
    
    /// Returns an ItemStack piece of bone armor based on the argument(s) given.
    public static ItemStack boneArmor(String armorName) {
        return boneArmor(armorName, false);
    }
    public static ItemStack boneArmor(int armorType) {
        return boneArmor(armorType, false);
    }
    public static ItemStack boneArmor(String armorName, boolean ingredient) {
        String[] armorNames = { "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS" };
        for (int i = 0; i < armorNames.length; i++)
            if (armorNames[i].equalsIgnoreCase(armorName))
                return boneArmor(i, ingredient);
        armorNames = new String[] { "HELM", "PLATE", "LEGS", "FEET" };
        for (int i = 0; i < armorNames.length; i++)
            if (armorNames[i].equalsIgnoreCase(armorName))
                return boneArmor(i, ingredient);
        _DungeonCrawler.debugException("Tried to create bone armor with invalid armor name (" + armorName + ")!");
        return boneArmor(0, ingredient);
    }
    public static ItemStack boneArmor(int armorType, boolean ingredient) {
        if (armorType < 0 || armorType > 3) {
            _DungeonCrawler.debugException("Tried to create bone armor with invalid armor type (" + Integer.toString(armorType) + ")!");
            armorType = 0;
        }
        return new ItemStack(_DungeonCrawler.boneArmor[armorType], 1, ingredient ? 32767 : 0);
    }
    
    /// Returns an ItemStack item based on the argument given.
    public static ItemStack item(String itemName) {
        return item(itemName, 1);
    }
    public static ItemStack item(String itemName, int stackSize) {
        return new ItemStack(_DungeonCrawler.DCItem, stackSize, getDamageFor(itemName));
    }
    
    /// Returns an ItemStack bone club based on the argument given.
    public static ItemStack pebble() {
        return pebble(1);
    }
    public static ItemStack pebble(int stackSize) {
        return new ItemStack(_DungeonCrawler.DCItem, stackSize, getDamageFor(BASIC, "pebble"));
    }
    
    /// Returns an ItemStack bone club based on the argument given.
    public static ItemStack chainLinks() {
        return chainLinks(1);
    }
    public static ItemStack chainLinks(int stackSize) {
        return new ItemStack(_DungeonCrawler.DCItem, stackSize, getDamageFor(BASIC, "chain"));
    }
    
    /// Returns an ItemStack bomb based on the argument(s) given.
    public static ItemStack impBomb(int type) {
        return impBomb(type, 1);
    }
    public static ItemStack impBomb(int type, int stackSize) {
        return new ItemStack(_DungeonCrawler.DCItem, stackSize, BOMB + type);
    }
    
    /// Returns an ItemStack grenade based on the argument(s) given.
    public static ItemStack grenade(int type) {
        return grenade(type, 1);
    }
    public static ItemStack grenade(int type, int stackSize) {
        return new ItemStack(_DungeonCrawler.DCItem, stackSize, GRENADE + type);
    }
    
    /// Returns an ItemStack mine based on the argument(s) given.
    public static ItemStack mine(int type) {
        return mine(type, 1);
    }
    public static ItemStack mine(int type, int stackSize) {
        return new ItemStack(_DungeonCrawler.DCItem, stackSize, MINE + type);
    }
    
    /// Returns an ItemStack gas mine based on the argument given.
    public static ItemStack gasMine() {
        return gasMine(1);
    }
    public static ItemStack gasMine(int stackSize) {
        return new ItemStack(_DungeonCrawler.DCItem, stackSize, getDamageFor(MINE, "mine_gas"));
    }
    
    /// Returns an ItemStack trap based on the argument(s) given.
    public static ItemStack trap(String trapName) {
        return trap(trapName, 1);
    }
    public static ItemStack trap(int type) {
        return trap(type, 1);
    }
    public static ItemStack trap(String trapName, int stackSize) {
        return new ItemStack(_DungeonCrawler.DCItem, stackSize, getDamageFor(TRAP, trapName));
    }
    public static ItemStack trap(int type, int stackSize) {
        return new ItemStack(_DungeonCrawler.DCItem, stackSize, TRAP + type);
    }
    
    /// Returns an ItemStack smeltable food item based on the argument(s) given.
    public static ItemStack foodSmelt(String foodName) {
        return foodSmelt(foodName, 1);
    }
    public static ItemStack foodSmelt(int type) {
        return foodSmelt(type, 1);
    }
    public static ItemStack foodSmelt(String foodName, int stackSize) {
        return food(FOOD_SMELT, foodName, stackSize);
    }
    public static ItemStack foodSmelt(int type, int stackSize) {
        return food(FOOD_SMELT, type, stackSize);
    }
    
    /// Returns an ItemStack craftable food item based on the argument(s) given.
    public static ItemStack foodCraft(String foodName) {
        return foodCraft(foodName, 1);
    }
    public static ItemStack foodCraft(int type) {
        return foodCraft(type, 1);
    }
    public static ItemStack foodCraft(String foodName, int stackSize) {
        return food(FOOD_CRAFT, foodName, stackSize);
    }
    public static ItemStack foodCraft(int type, int stackSize) {
        return food(FOOD_CRAFT, type, stackSize);
    }
    
    /// Returns an ItemStack food item based on the argument(s) given.
    public static ItemStack food(String foodName) {
        return food(foodName, 1);
    }
    public static ItemStack food(int type) {
        return food(type, 1);
    }
    public static ItemStack food(String foodName, int stackSize) {
        return food(FOOD_BASIC, foodName, stackSize);
    }
    public static ItemStack food(int type, int stackSize) {
        return food(FOOD_BASIC, type, stackSize);
    }
    public static ItemStack food(int typeIndex, String foodName, int stackSize) {
        return new ItemStack(_DungeonCrawler.DCFood, stackSize, getDamageForFood(typeIndex, foodName));
    }
    public static ItemStack food(int typeIndex, int type, int stackSize) {
        return new ItemStack(_DungeonCrawler.DCFood, stackSize, typeIndex + type);
    }
    
    /// Returns true if the damage is within range of the type index.
    public static boolean isInRange(int typeIndex, int damage) {
        damage -= typeIndex;
        return damage >= 0 && damage < 64;
    }
    
    /// Returns the damage index for DCItem associated with the given string.
    public static boolean isEnabled(String iconName) {
        int length = DCItemNames.length;
        for (int i = 0; i < length; i++)
            if (DCItemNames[i] != null && iconName.equalsIgnoreCase(DCItemNames[i][1]))
                return true;
        return false;
    }
    public static boolean isEnabled(int typeIndex, String iconName) {
        int end = typeIndex + 64;
        for (int i = typeIndex; i < end; i++)
            if (DCItemNames[i] != null && iconName.equalsIgnoreCase(DCItemNames[i][1]))
                return true;
        return false;
    }
    public static boolean isEnabled(int damageIndex) {
        return DCItemNames[damageIndex] != null;
    }
    
    /// Returns the damage index for DCItem associated with the given string.
    public static int getDamageFor(String iconName) {
        int length = DCItemNames.length;
        for (int i = 0; i < length; i++)
            if (DCItemNames[i] != null && iconName.equalsIgnoreCase(DCItemNames[i][1]))
                return i;
        return -1;
    }
    public static int getDamageFor(int typeIndex, String iconName) {
        int end = typeIndex + 64;
        for (int i = typeIndex; i < end; i++)
            if (DCItemNames[i] != null && iconName.equalsIgnoreCase(DCItemNames[i][1]))
                return i;
        return -1;
    }
    public static int getTypeFor(int typeIndex, String iconName) {
        return getDamageFor(typeIndex, iconName) - typeIndex;
    }
    
    /// Returns the damage index for DCItem associated with the given string.
    public static boolean isFoodEnabled(String iconName) {
        int length = DCFoodNames.length;
        for (int i = 0; i < length; i++)
            if (DCFoodNames[i] != null && iconName.equalsIgnoreCase(DCFoodNames[i][1]))
                return true;
        return false;
    }
    public static boolean isFoodEnabled(int typeIndex, String iconName) {
        int end = typeIndex + 64;
        for (int i = typeIndex; i < end; i++)
            if (DCFoodNames[i] != null && iconName.equalsIgnoreCase(DCFoodNames[i][1]))
                return true;
        return false;
    }
    public static boolean isFoodEnabled(int damageIndex) {
        return DCFoodNames[damageIndex] != null;
    }
    
    /// Returns the damage index for DCItem associated with the given string.
    public static int getDamageForFood(String iconName) {
        int length = DCFoodNames.length;
        for (int i = 0; i < length; i++)
            if (DCFoodNames[i] != null && iconName.equalsIgnoreCase(DCFoodNames[i][1]))
                return i;
        return -1;
    }
    public static int getDamageForFood(int typeIndex, String iconName) {
        int end = typeIndex + 64;
        for (int i = typeIndex; i < end; i++)
            if (DCFoodNames[i] != null && iconName.equalsIgnoreCase(DCFoodNames[i][1]))
                return i;
        return -1;
    }
    public static int getTypeForFood(int typeIndex, String iconName) {
        return getDamageForFood(typeIndex, iconName) - typeIndex;
    }
    
    /// Returns the material index for the given material.
    public static int getMaterialIndex(EnumToolMaterial material) {
        if (material == EnumToolMaterial.GOLD)
            return 4;
        return (int)material.getDamageVsEntity();
    }
    public static int getMaterialIndex(String materialName) {
        String[] materialNames = { "WOODEN", "STONE", "IRON", "DIAMOND", "GOLDEN" };
        for (int i = 0; i < materialNames.length; i++)
            if (materialNames[i].equalsIgnoreCase(materialName))
                return i;
        _DungeonCrawler.debugException("Invalid material name (" + materialName + ")!");
        return 0;
    }
    
    /// Returns an armor slot index for the given string.
    public static int getArmorIndex(String armorName) {
        String[] armorNames = { "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS" };
        for (int i = 0; i < armorNames.length; i++)
            if (armorNames[i].equalsIgnoreCase(armorName))
                return i;
        _DungeonCrawler.debugException("Invalid armor name (" + armorName + ")!");
        return 0;
    }
    
    /// Returns the string armor slot name for the given armor slot index.
    public static String getArmorName(int id) {
        switch (id) {
            case 0:
                return "Helmet";
            case 1:
                return "Chestplate";
            case 2:
                return "Leggings";
            case 3:
                return "Boots";
        }
        _DungeonCrawler.debugException("Invalid armor type (" + id + ") has requested a name!");
        return "Toast";
    }
    
    /// Returns the string material name for the given material.
    public static String getMaterialName(EnumToolMaterial material) {
        switch ((int)material.getDamageVsEntity()) {
            case 0:
                if (material == EnumToolMaterial.GOLD)
                    return "Golden";
                return "Wooden";
            case 1:
                return "Stone";
            case 2:
                return "Iron";
            case 3:
                return "Diamond";
        }
        _DungeonCrawler.debugException("Invalid tool material (material=" + material.toString() + ", damage=" + Float.toString(material.getDamageVsEntity()) + ") has requested a name!");
        return "Toast";
    }
    public static String getMaterialName(int materialIndex) {
        switch (materialIndex) {
            case 0:
                return "Wooden";
            case 1:
                return "Stone";
            case 2:
                return "Iron";
            case 3:
                return "Diamond";
            case 4:
                return "Golden";
        }
        _DungeonCrawler.debugException("Invalid tool material (index=" + materialIndex + ") has requested a name!");
        return "Toast";
    }
}