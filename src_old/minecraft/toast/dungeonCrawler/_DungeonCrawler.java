package toast.dungeonCrawler;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.EnumHelper;

@Mod(modid = "DungeonCrawler", name = "Dungeon Crawler", version = "0.2.5")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { "DC|TPEntity" }, packetHandler = PacketHandler.class)
public class _DungeonCrawler
{
    /** INFO **\
    >> Blocks
        > blocks added: 0
        > IDs used: 0
    >> Items
        > items added: 57
        > IDs used: 14
    \** **** **/
    /** TO DO **\
    >> currentTasks
        * Items
            / improv. weapons, explosives, tools, armor
                / string+gunpowder bomb
                x sharp stick
                x rockshot
                    > shoot wither skulls
                x bone armor/club
                x throwable gravel
                / traps
                ? dart
            / knives
                x make item
                x helps craft without workbench
                / throwable
            / scavenged food
                x way to improve rotten flesh
                / way to improve poisonous potato - weaponize?
                x way to improve spider eye
            > spawner breaker
        * Crafting
            / knife crafting - needs a solid recipe instead of self-container
            x salvaging
        * Mobility
            > enchanting
            x crafting
            > smelting
    >> tasks
        * Dungeon survival
            x food
            / weapons
            > spawner disabling
            / mobility
    >> goals
        * Compatibility
            ? ModLoader
    \** ** ** **/
    /// If true, this mod starts up in debug mode.
    public static final boolean debug = false;
    @SidedProxy(clientSide = "toast.dungeonCrawler.client.ClientProxy", serverSide = "toast.dungeonCrawler.CommonProxy")
    public static CommonProxy proxy;
    /// The mod's random number generator.
    public static final Random random = new Random();
    /// Property; whether salvage recipes are enabled.
    private static boolean salvageRecipes = true;
    /// Item variables.
    public static Item[] knife = new Item[5];
    public static Item sharpStick;
    public static Item boneClub;
    public static Item rockshot;
    public static Item[] boneArmor = new Item[4];
    public static Item DCItem;
    public static Item DCFood;
    /// Trap data map.
    public static Map<EntityPlayer, int[]> trapData = new HashMap<EntityPlayer, int[]>();
    /// Material variables.
    public static final EnumArmorMaterial boneArmorMaterial = EnumHelper.addArmorMaterial("BONE", 6, new int[] {1, 2, 1, 1}, 0);
    
    /// Registers the entities and renderers in this mod.
	private void registerEntities() {
        int id = 0;
        String path = "entity.DungeonCrawler.";
        ///EntityRegistry.registerModEntity(EntityKnife.class, "Knife", id++, this, 64, 10, false);
        ///LanguageRegistry.instance().addStringLocalization(path + "Knife.name", "Knife");
        EntityRegistry.registerModEntity(EntityRock.class, "Rock", id++, this, 64, 10, true);
        LanguageRegistry.instance().addStringLocalization(path + "Rock.name", "Rock");
        EntityRegistry.registerModEntity(EntityPebble.class, "Pebble", id++, this, 64, 10, true);
        LanguageRegistry.instance().addStringLocalization(path + "Pebble.name", "Pebble");
        
        EntityRegistry.registerModEntity(EntityBomb.class, "Bomb", id++, this, 80, 5, false);
        LanguageRegistry.instance().addStringLocalization(path + "Bomb.name", "Improvised Bomb");
        EntityRegistry.registerModEntity(EntityGrenade.class, "Grenade", id++, this, 64, 10, true);
        LanguageRegistry.instance().addStringLocalization(path + "Grenade.name", "Grenade");
        EntityRegistry.registerModEntity(EntityMine.class, "Mine", id++, this, 80, 5, true);
        LanguageRegistry.instance().addStringLocalization(path + "Mine.name", "Mine");
        EntityRegistry.registerModEntity(EntityGasCloud.class, "GasCloud", id++, this, 0, Integer.MAX_VALUE, false);
        LanguageRegistry.instance().addStringLocalization(path + "GasCloud.name", "Gas Cloud");
        EntityRegistry.registerModEntity(EntityTrap.class, "Trap", id++, this, 80, Integer.MAX_VALUE, false);
        LanguageRegistry.instance().addStringLocalization(path + "Trap.name", "Trap");
	}
    
    /// Registers the items in this mod.
	private void registerItems() {
        boolean increment = Properties.getBoolean(Properties.ITEMS, "_easy_ids");
        int id = Properties.getInt(Properties.ITEMS, "_starting_id") - 257;
        int tempId;
        int renderIndex;
        String tex = ""; /// Until forge mod separating works.
        
        renderIndex = proxy.newArmor("bone");
        for (int i = 0; i < 4; i++) {
            tempId = getIdFor(id, increment, "armor_bone_" + ItemHelper.getArmorName(i).toLowerCase());
            if (tempId > 0)
                boneArmor[i] = (new ItemBoneArmor(id = tempId, renderIndex, i)).setTextureName(tex + "bone_" + i);
        }
        
        tempId = getIdFor(id, increment, "bone_club");
        if (tempId > 0)
            boneClub = (new ItemBoneClub(id = tempId)).setTextureName(tex + "bone_club");
        tempId = getIdFor(id, increment, "dc_food");
        if (tempId > 0)
            DCFood = (new ItemDCFood(id = tempId)).setTextureName(tex + "zombie_meat");
        tempId = getIdFor(id, increment, "dc_item");
        if (tempId > 0)
            DCItem = (new ItemDCItem(id = tempId)).setTextureName(tex + "pebble");
        
        for (int i = 0; i < ItemHelper.materials.length; i++) {
            tempId = getIdFor(id, increment, "knife_" + ItemHelper.getMaterialName(i).toLowerCase());
            if (tempId > 0)
                knife[i] = (new ItemKnife(id = tempId, ItemHelper.materials[i])).setTextureName(tex + "knife_" + i);
        }
        
        tempId = getIdFor(id, increment, "rockshot");
        if (tempId > 0)
            rockshot = (new ItemRockshot(id = tempId)).setTextureName(tex + "rockshot");
        tempId = getIdFor(id, increment, "sharp_stick");
        if (tempId > 0)
            sharpStick = (new ItemSharpStick(id = tempId)).setTextureName(tex + "sharp_stick");
	}
    
    /// Gets the appropriate ID for the given item name, based on the properties.
    private int getIdFor(int prevId, boolean increment, String item) {
        int id = Properties.getInt(Properties.ITEMS, item) - 256;
        if (increment && id > 0)
            return prevId + 1;
        return id;
    }
    
    /// Registers the crafting and smelting recipes in this mod.
    private void addRecipes() {
        boolean boneSticks = Properties.getBoolean(Properties.GENERAL, "bone_stick_recipes");
        Block[] pressurePlates = { Block.pressurePlateStone, Block.pressurePlatePlanks, Block.pressurePlateGold, Block.pressurePlateIron };
        Block[] buttons = { Block.stoneButton, Block.woodenButton };
        Item[][] tools = {
            { Item.swordWood, Item.swordStone, Item.swordIron, Item.swordDiamond, Item.swordGold },
            { Item.shovelWood, Item.shovelStone, Item.shovelIron, Item.shovelDiamond, Item.shovelGold },
            { Item.pickaxeWood, Item.pickaxeStone, Item.pickaxeIron, Item.pickaxeDiamond, Item.pickaxeGold },
            { Item.axeWood, Item.axeStone, Item.axeIron, Item.axeDiamond, Item.axeGold },
            { Item.hoeWood, Item.hoeStone, Item.hoeIron, Item.hoeDiamond, Item.hoeGold }
        };
        Item[] chainmail = { Item.helmetChain, Item.plateChain, Item.legsChain, Item.bootsChain };
        String[][] toolRecipes = {
            { "#", "#", "|" },
            { "#", "|", "|" },
            { "###", " | ", " | " },
            { "##", "|#", "| " },
            { "##", "| ", "| " }
        };
        String[][] armorRecipes = {
            { "###", "# #" },
            { "# #", "###", "###" },
            { "###", "# #", "# #" },
            { "# #", "# #" }
        };
        String[] recipe;
        if (debug) {
            if (knife[0] != null)
                GameRegistry.addRecipe(ItemHelper.knife("WOODEN"), new Object[] {
                    "#",
                    "#", '#', Block.dirt
                });
            if (DCItem != null) {
                addRecipe(new ItemStack(Item.bowlSoup, 64, 0), Block.dirt);
                addRecipe(ItemHelper.foodCraft("spider_bucket", 64), Block.dirt, Block.dirt);
            }
            for (int i = 0; i < ItemHelper.materials.length; i++) if (knife[i] != null)
                addRecipe(ItemHelper.knife(i + 1 == knife.length ? 0 : i + 1), ItemHelper.knife(i, true));
            if (rockshot != null) {
                GameRegistry.addRecipe(new ItemStack(rockshot, 1, 0), new Object[] {
                    "###",
                    "#  ", '#', Block.dirt
                });
                addRecipe(ItemRockshot.setAmmo(new ItemStack(rockshot, 1, 0), new ItemStack(Block.cobblestone)), new ItemStack(rockshot, 1, 32767));
            }
        }
        
        /// Knife crafting
        recipe = Properties.getBoolean(Properties.ALT_RECIPES, "knife") ? new String[] { " #", "| " } : new String[] { "#", "|" };
        for (int i = 0; i < ItemHelper.materials.length; i++) {
            if (knife[i] == null)
                continue;
            ItemStack craftKnife = ItemHelper.knife(i, true);
            GameRegistry.addRecipe(ItemHelper.knife(i), new Object[] {
                recipe, '#', new ItemStack(ItemHelper.materialIDs[i], 1, 32767), '|', Item.stick
            });
            if (boneSticks) {
                GameRegistry.addRecipe(ItemHelper.knife(i), new Object[] {
                    recipe, '#', new ItemStack(ItemHelper.materialIDs[i], 1, 32767), '|', Item.bone
                });
                addRecipe(new ItemStack(Item.arrow, 4, 0), craftKnife, Item.flint, Item.bone, Item.feather);
                addRecipe(new ItemStack(Item.arrow, 4, 0), craftKnife, Item.netherQuartz, Item.bone, Item.feather);
            }
            
            /// Main recipes
            if (sharpStick != null)
                addRecipe(ItemHelper.sharpStick(), craftKnife, Item.stick);
            if (boneClub != null)
                addRecipe(ItemHelper.boneClub(), craftKnife, Item.bone);
            if (DCItem != null) {
                if (ItemHelper.isEnabled(ItemHelper.BOMB + 1))
                    addRecipe(ItemHelper.impBomb(1), craftKnife, Item.silk, Item.gunpowder, Item.gunpowder);
                if (ItemHelper.isEnabled(ItemHelper.BOMB + 4))
                    addRecipe(ItemHelper.impBomb(4), craftKnife, Item.silk, Item.gunpowder, Item.blazePowder);
                if (ItemHelper.isEnabled(ItemHelper.BOMB + 7))
                    addRecipe(ItemHelper.impBomb(7), craftKnife, Item.silk, Item.gunpowder, Item.enderPearl);
                if (ItemHelper.isEnabled(ItemHelper.BOMB + 2))
                    addRecipe(ItemHelper.impBomb(2), craftKnife, Block.tnt);
                if (ItemHelper.isEnabled(ItemHelper.BASIC, "pebble"))
                    addRecipe(ItemHelper.pebble(6), craftKnife, Block.gravel);
                if (sharpStick != null && ItemHelper.isEnabled(ItemHelper.TRAP, "trap_spike"))
                    addRecipe(ItemHelper.trap("trap_spike"), craftKnife, ItemHelper.sharpStick(), ItemHelper.sharpStick(), ItemHelper.sharpStick());
            }
            
            /// Survival recipes
            addRecipe(new ItemStack(Item.stick, 2, 0), craftKnife, Block.planks);
            addRecipe(new ItemStack(Item.arrow, 4, 0), craftKnife, Item.flint, Item.stick, Item.feather);
            addRecipe(new ItemStack(Item.arrow, 4, 0), craftKnife, Item.netherQuartz, Item.stick, Item.feather);
            for (int j = 0; j < 2; j++) {
                addRecipe(new ItemStack(tools[0][j], 1, 0), craftKnife, new ItemStack(ItemHelper.materialIDs[j], 1, 32767), new ItemStack(ItemHelper.materialIDs[j], 1, 32767), Item.stick);
                if (boneSticks)
                    addRecipe(new ItemStack(tools[0][j], 1, 0), craftKnife, new ItemStack(ItemHelper.materialIDs[j], 1, 32767), new ItemStack(ItemHelper.materialIDs[j], 1, 32767), Item.bone);
            }
            addRecipe(new ItemStack(Item.potato, 1, 0), craftKnife, Item.poisonousPotato);
            if (DCFood != null) {
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_SMELT, "zombie_meat"))
                addRecipe(ItemHelper.foodSmelt("zombie_meat"), craftKnife, Item.rottenFlesh);
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_meat")) {
                    addRecipe(ItemHelper.foodCraft("spider_meat"), craftKnife, Item.spiderEye);
                    if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_soup"))
                        addRecipe(ItemHelper.foodCraft("spider_soup"), craftKnife, ItemHelper.foodCraft("spider_meat"), ItemHelper.foodCraft("spider_meat"), Item.bowlEmpty);
                    if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_bucket"))
                        addRecipe(ItemHelper.foodCraft("spider_soup"), craftKnife, ItemHelper.foodCraft("spider_meat"), ItemHelper.foodCraft("spider_meat"), Item.bucketEmpty);
                }
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "mushroom_bucket"))
                    addRecipe(ItemHelper.foodCraft("mushroom_bucket"), craftKnife, Block.mushroomBrown, Block.mushroomRed, Item.bucketEmpty);
            }
        }
        
        /// Main recipes
        if (boneClub != null) {
            recipe = Properties.getBoolean(Properties.ALT_RECIPES, "bone_club") ? new String[] { " #", "# " } : new String[] { "#", "#" };
            GameRegistry.addRecipe(ItemHelper.boneClub(), new Object[] {
                recipe, '#', Item.bone
            });
        }
        if (rockshot != null) {
            GameRegistry.addRecipe(new ItemStack(rockshot, 1, 0), new Object[] {
                "&##",
                "@##",
                "|  ", '&', Item.flintAndSteel, '|', Item.stick, '@', Block.planks, '#', Block.cobblestone
            });
            GameRegistry.addRecipe(new RecipeRockshot());
        }
        for (int i = 0; i < armorRecipes.length; i++) {
            if (boneArmor[i] != null)
                GameRegistry.addRecipe(ItemHelper.boneArmor(i), new Object[] {
                    armorRecipes[i], '#', Item.bone
                });
            GameRegistry.addRecipe(new ItemStack(chainmail[i], 1, 0), new Object[] {
                armorRecipes[i], '#', ItemHelper.chainLinks()
            });
        }
        if (DCItem != null) {
            if (ItemHelper.isEnabled(ItemHelper.BASIC, "pebble"))
                addRecipe(ItemHelper.pebble(3), Block.gravel);
            if (ItemHelper.isEnabled(ItemHelper.BASIC, "chain"))
                if (Properties.getBoolean(Properties.ALT_RECIPES, "chain"))
                    addRecipe(ItemHelper.chainLinks(6), Item.ingotIron, Item.ingotIron);
                else
                    addRecipe(ItemHelper.chainLinks(3), Item.ingotIron);
            
            /// Improvised bombs
            recipe = Properties.getBoolean(Properties.ALT_RECIPES, "bomb") ? new String[] { "  @", " # ", "%  " } : new String[] { "@", "#", "%" };
            if (ItemHelper.isEnabled(ItemHelper.BOMB + 1))
                GameRegistry.addRecipe(ItemHelper.impBomb(1), new Object[] {
                    recipe, '@', Item.silk, '#', Item.gunpowder, '%', Item.gunpowder
                });
            if (ItemHelper.isEnabled(ItemHelper.BOMB + 4))
                GameRegistry.addRecipe(ItemHelper.impBomb(4), new Object[] {
                    recipe, '@', Item.silk, '#', Item.gunpowder, '%', Item.blazePowder
                });
            if (ItemHelper.isEnabled(ItemHelper.BOMB + 7))
                GameRegistry.addRecipe(ItemHelper.impBomb(7), new Object[] {
                    recipe, '@', Item.silk, '#', Item.gunpowder, '%', Item.enderPearl
                });
            for (int i = 0; i < 64; i++) {
                if (!ItemHelper.isEnabled(ItemHelper.BOMB + i))
                    continue;
                if (ItemHelper.isEnabled(ItemHelper.MINE + i)) for (int j = 0; j < pressurePlates.length; j++)
                    GameRegistry.addRecipe(ItemHelper.mine(i), new Object[] {
                        "-",
                        "#", '#', ItemHelper.impBomb(i), '-', pressurePlates[j]
                    });
                if (ItemHelper.isEnabled(ItemHelper.GRENADE + i)) for (int j = 0; j < buttons.length; j++){
                    GameRegistry.addRecipe(ItemHelper.grenade(i), new Object[] {
                        "#*", '#', ItemHelper.impBomb(i), '*', buttons[j]
                    });
                    GameRegistry.addRecipe(ItemHelper.grenade(i), new Object[] {
                        "*",
                        "#", '#', ItemHelper.impBomb(i), '*', buttons[j]
                    });
                    GameRegistry.addRecipe(ItemHelper.grenade(i), new Object[] {
                        "#",
                        "*", '#', ItemHelper.impBomb(i), '*', buttons[j]
                    });
                }
                if (i % 3 == 2 || !ItemHelper.isEnabled(ItemHelper.BOMB + i + 1))
                    continue;
                GameRegistry.addRecipe(ItemHelper.impBomb(i + 1), new Object[] {
                    "##", '#', ItemHelper.impBomb(i)
                });
                GameRegistry.addRecipe(ItemHelper.impBomb(i + 1), new Object[] {
                    "#",
                    "#", '#', ItemHelper.impBomb(i)
                });
                addRecipe(ItemHelper.impBomb(i, 2), ItemHelper.impBomb(i + 1));
            }
            
            /// Mines
            if (ItemHelper.isEnabled(ItemHelper.MINE, "mine_gas")) for (int i = 0; i < pressurePlates.length; i++)
                GameRegistry.addRecipe(ItemHelper.gasMine(), new Object[] {
                    " - ",
                    "@#@",
                    "&&&", '-', pressurePlates[i], '@', Item.dyePowder, '#', Item.gunpowder, '&', new ItemStack(Item.coal, 1, 32767)
                });
            
            /// Traps
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_spike")) {
                if (sharpStick != null)
                    GameRegistry.addRecipe(ItemHelper.trap("trap_spike"), new Object[] {
                        "|||", '|', ItemHelper.sharpStick()
                    });
                if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_spike_poison"))
                    GameRegistry.addRecipe(ItemHelper.trap("trap_spike_poison"), new Object[] {
                        "@",
                        "#", '@', Item.spiderEye, '#', ItemHelper.trap("trap_spike")
                    });
            }
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_fire")) for (int i = 0; i < pressurePlates.length; i++)
                GameRegistry.addRecipe(ItemHelper.trap("trap_fire"), new Object[] {
                    " - ",
                    "#&#", '-', pressurePlates[i], '#', Block.planks, '&', new ItemStack(Item.coal, 1, 32767)
                });
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_end")) for (int i = 0; i < pressurePlates.length; i++)
                GameRegistry.addRecipe(ItemHelper.trap("trap_end"), new Object[] {
                    " - ",
                    "#@#", '-', pressurePlates[i], '#', Block.stone, '@', Item.enderPearl
                });
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_screamer")) for (int i = 0; i < pressurePlates.length; i++)
                GameRegistry.addRecipe(ItemHelper.trap("trap_screamer"), new Object[] {
                    " - ",
                    "*#*", '-', pressurePlates[i], '*', Item.ghastTear, '#', Item.reed
                });
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_launcher"))
                GameRegistry.addRecipe(ItemHelper.trap("trap_launcher"), new Object[] {
                    " - ",
                    "#=#", '-', Block.pressurePlateStone, '#', Block.stone, '=', Block.pistonBase
                });
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_crippling"))
                GameRegistry.addRecipe(ItemHelper.trap("trap_crippling"), new Object[] {
                    "# #",
                    "#-#", '-', Block.pressurePlateStone, '#', Item.ingotIron
                });
        }
        
        /// Survival recipes
        GameRegistry.addRecipe(new ItemStack(Item.flintAndSteel, 1, 0), new Object[] {
            "# ",
            " &", '#', Item.ingotIron, '&', Item.netherQuartz
        });
        GameRegistry.addRecipe(new ItemStack(Item.arrow, 4, 0), new Object[] {
            "@",
            "|",
            "#", '@', Item.netherQuartz, '|', Item.stick, '#', Item.feather
        });
        addRecipe(new ItemStack(Item.flint, 1, 0), Block.gravel, Block.gravel);
        GameRegistry.addSmelting(Block.gravel.blockID, new ItemStack(Item.flint), 0.1F);
        if (DCFood != null) {
            if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_BASIC, "cookie_golden"))
                GameRegistry.addRecipe(ItemHelper.food("cookie_golden"), new Object[] {
                    "***",
                    "*O*",
                    "***", 'O', Item.cookie, '*', Item.goldNugget
                });
            if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_SMELT, "zombie_meat") && ItemHelper.isFoodEnabled(ItemHelper.FOOD_SMELT, "zombie_meat_cooked"))
                FurnaceRecipes.smelting().addSmelting(DCFood.itemID, ItemHelper.getDamageForFood(ItemHelper.FOOD_SMELT, "zombie_meat"), ItemHelper.foodSmelt("zombie_meat_cooked"), 0.1F);
            if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_meat")) {
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_soup"))
                    GameRegistry.addRecipe(ItemHelper.foodCraft("spider_soup"), new Object[] {
                        "@",
                        "@",
                        "U", '@', ItemHelper.foodCraft("spider_meat"), 'U', Item.bowlEmpty
                    });
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_bucket"))
                    GameRegistry.addRecipe(ItemHelper.foodCraft("spider_bucket"), new Object[] {
                        "@",
                        "@",
                        "U", '@', ItemHelper.foodCraft("spider_meat"), 'U', Item.bucketEmpty
                    });
            }
            if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "mushroom_bucket")) {
                GameRegistry.addRecipe(ItemHelper.foodCraft("mushroom_bucket"), new Object[] {
                    "b",
                    "r",
                    "U", 'b', Block.mushroomBrown, 'r', Block.mushroomRed, 'U', Item.bucketEmpty
                });
                GameRegistry.addRecipe(ItemHelper.foodCraft("mushroom_bucket"), new Object[] {
                    "r",
                    "b",
                    "U", 'b', Block.mushroomBrown, 'r', Block.mushroomRed, 'U', Item.bucketEmpty
                });
            }
        }
        
        /// Bone-stick recipes
        if (boneSticks) {
            for (int i = 0; i < tools.length; i++) {
                for (int j = 0; j < tools[i].length; j++) {
                    GameRegistry.addRecipe(new ItemStack(tools[i][j], 1, 0), new Object[] {
                        toolRecipes[i], '#', new ItemStack(ItemHelper.materialIDs[j], 1, 32767), '|', Item.bone
                    });
                }
            }
            if (rockshot != null)
                GameRegistry.addRecipe(new ItemStack(rockshot, 1, 0), new Object[] {
                    "&##",
                    "@##",
                    "|  ", '&', Item.flintAndSteel, '|', Item.bone, '@', Block.planks, '#', Block.cobblestone
                });
            GameRegistry.addRecipe(new ItemStack(Item.fishingRod, 1, 0), new Object[] {
                "  |",
                " |@",
                "| @", '@', Item.silk, '|', Item.bone
            });
            GameRegistry.addRecipe(new ItemStack(Item.bow, 1, 0), new Object[] {
                "@| ",
                "@ |",
                "@| ", '@', Item.silk, '|', Item.bone
            });
            GameRegistry.addRecipe(new ItemStack(Item.arrow, 4, 0), new Object[] {
                "@",
                "|",
                "#", '@', Item.flint, '|', Item.bone, '#', Item.feather
            });
            GameRegistry.addRecipe(new ItemStack(Item.arrow, 4, 0), new Object[] {
                "@",
                "|",
                "#", '@', Item.netherQuartz, '|', Item.bone, '#', Item.feather
            });
            GameRegistry.addRecipe(new ItemStack(Block.torchWood, 4, 0), new Object[] {
                "@",
                "|", '@', new ItemStack(Item.coal, 1, 32767), '|', Item.bone
            });
            GameRegistry.addRecipe(new ItemStack(Block.torchRedstoneActive, 1, 0), new Object[] {
                "@",
                "|", '@', Item.redstone, '|', Item.bone
            });
        }
        if (Properties.getBoolean(Properties.GENERAL, "salvage_recipes"))
            addSalvageRecipes();
    }
    private void addSalvageRecipes() {
        String[] stickRecipe = { "#", "#" };
        /// Knife crafting
        for (int i = 0; i < ItemHelper.materials.length; i++) {
            if (knife[i] == null)
                continue;
            ItemStack craftKnife = ItemHelper.knife(i, true);
            /// Stick
            addRecipe(new ItemStack(Item.stick, 2, 0), craftKnife, Block.sapling, Block.sapling);
            addRecipe(new ItemStack(Item.stick, 2, 0), craftKnife, Block.ladder);
            addRecipe(new ItemStack(Item.stick, 3, 0), craftKnife, Block.fence);
            addRecipe(new ItemStack(Item.stick, 4, 0), craftKnife, Block.cactus, Block.cactus);
            addRecipe(new ItemStack(Item.stick, 8, 0), craftKnife, Item.painting);
            
            /// Wooden planks
            addRecipe(new ItemStack(Block.planks, 2, 0), craftKnife, Block.pressurePlatePlanks);
            addRecipe(new ItemStack(Block.planks, 2, 0), craftKnife, Block.fenceGate);
            addRecipe(new ItemStack(Block.planks, 2, 0), craftKnife, Item.sign);
            addRecipe(new ItemStack(Block.planks, 3, 0), craftKnife, Item.bed);
            addRecipe(new ItemStack(Block.planks, 3, 0), craftKnife, Block.trapdoor);
            addRecipe(new ItemStack(Block.planks, 4, 0), craftKnife, Block.workbench);
            addRecipe(new ItemStack(Block.planks, 5, 0), craftKnife, Item.boat);
            addRecipe(new ItemStack(Block.planks, 6, 0), craftKnife, Item.doorWood);
            addRecipe(new ItemStack(Block.planks, 8, 0), craftKnife, Block.chest);
            addRecipe(new ItemStack(Block.planks, 8, 0), craftKnife, Block.music);
            
            /// Miscellaneous
            addRecipe(new ItemStack(Item.bow, 1, 0), craftKnife, Block.dispenser);
            addRecipe(new ItemStack(Block.chest, 1, 0), craftKnife, Block.hopperBlock);
            addRecipe(new ItemStack(Block.tripWire, 1, 0), craftKnife, Block.chestTrapped);
            addRecipe(new ItemStack(Item.leather, 1, 0), craftKnife, Item.book);
            addRecipe(new ItemStack(Item.leather, 1, 0), craftKnife, Item.itemFrame);
            addRecipe(new ItemStack(Item.ingotIron, 1, 0), craftKnife, Block.pistonBase);
            addRecipe(new ItemStack(Item.diamond, 1, 0), craftKnife, Block.jukebox);
            addRecipe(new ItemStack(Item.melon, 9, 0), craftKnife, Block.melon);
            addRecipe(new ItemStack(Item.netherQuartz, 1, 0), craftKnife, Item.comparator);
            addRecipe(new ItemStack(Item.netherQuartz, 2, 0), craftKnife, Block.daylightSensor);
        }
        
        /// Cobblestone
        addRecipe(new ItemStack(Block.cobblestone, 1, 0), Block.pressurePlateStone);
        addRecipe(new ItemStack(Block.cobblestone, 1, 0), Block.cobblestoneMossy);
        addRecipe(new ItemStack(Block.cobblestone, 1, 0), new ItemStack(Block.cobblestoneWall, 1, 0));
        addRecipe(new ItemStack(Block.cobblestone, 3, 0), Block.dropper);
        addRecipe(new ItemStack(Block.cobblestone, 4, 0), Block.furnaceIdle);
        
        /// Stone
        addRecipe(new ItemStack(Block.stone, 1, 0), new ItemStack(Block.stoneBrick, 1, 0));
        
        /// Stone bricks
        addRecipe(new ItemStack(Block.stoneBrick, 1, 0), new ItemStack(Block.stoneBrick, 1, 1));
        addRecipe(new ItemStack(Block.stoneBrick, 1, 0), new ItemStack(Block.stoneBrick, 1, 2));
        addRecipe(new ItemStack(Block.stoneBrick, 1, 0), new ItemStack(Block.stoneBrick, 1, 3));
        
        /// Stick
        GameRegistry.addRecipe(new ItemStack(Item.stick, 1, 0), new Object[] {
            stickRecipe, '#', Block.sapling
        });
        addRecipe(new ItemStack(Item.stick, 1, 0), Block.ladder);
        GameRegistry.addRecipe(new ItemStack(Item.stick, 2, 0), new Object[] {
            stickRecipe, '#', Block.cactus
        });
        addRecipe(new ItemStack(Item.stick, 2, 0), Block.fence);
        addRecipe(new ItemStack(Item.stick, 4, 0), Item.painting);
        addRecipe(new ItemStack(Item.stick, 4, 0), Item.itemFrame);
        
        /// Wooden planks
        addRecipe(new ItemStack(Block.planks, 1, 0), Block.pressurePlatePlanks);
        addRecipe(new ItemStack(Block.planks, 1, 0), Block.fenceGate);
        addRecipe(new ItemStack(Block.planks, 2, 0), Block.workbench);
        addRecipe(new ItemStack(Block.planks, 2, 0), Item.bed);
        addRecipe(new ItemStack(Block.planks, 2, 0), Block.pistonBase);
        addRecipe(new ItemStack(Block.planks, 2, 0), Block.trapdoor);
        addRecipe(new ItemStack(Block.planks, 3, 0), Item.doorWood);
        addRecipe(new ItemStack(Block.planks, 3, 0), Item.boat);
        addRecipe(new ItemStack(Block.planks, 4, 0), Block.chest);
        addRecipe(new ItemStack(Block.planks, 4, 0), Block.music);
        addRecipe(new ItemStack(Block.planks, 4, 0), Block.jukebox);
        
        /// Leather
        addRecipe(new ItemStack(Item.leather, 2, 0), Item.saddle);
        GameRegistry.addSmelting(Item.porkCooked.itemID, new ItemStack(Item.leather), 0.1F);
        GameRegistry.addSmelting(Item.fishCooked.itemID, new ItemStack(Item.leather), 0.1F);
        GameRegistry.addSmelting(Item.porkCooked.itemID, new ItemStack(Item.leather), 0.1F);
        GameRegistry.addSmelting(Item.beefCooked.itemID, new ItemStack(Item.leather), 0.1F);
        GameRegistry.addSmelting(Item.chickenCooked.itemID, new ItemStack(Item.leather), 0.1F);
        
        /// Iron ingots
        addRecipe(new ItemStack(Item.ingotIron, 1, 0), Block.pressurePlateIron);
        addRecipe(new ItemStack(Item.ingotIron, 2, 0), Item.compass);
        addRecipe(new ItemStack(Item.ingotIron, 3, 0), Item.doorIron);
        addRecipe(new ItemStack(Item.ingotIron, 3, 0), Block.hopperBlock);
        addRecipe(new ItemStack(Item.ingotIron, 4, 0), Block.cauldron);
        addRecipe(new ItemStack(Item.ingotIron, 16, 0), Block.anvil);
        
        /// Gold ingots
        addRecipe(new ItemStack(Item.ingotGold, 1, 0), Block.pressurePlateGold);
        addRecipe(new ItemStack(Item.ingotGold, 2, 0), Item.pocketSundial);
        
        /// Redstone
        addRecipe(new ItemStack(Item.redstone, 1, 0), Block.torchRedstoneActive);
        addRecipe(new ItemStack(Item.redstone, 3, 0), Item.redstoneRepeater);
        addRecipe(new ItemStack(Item.redstone, 3, 0), Item.comparator);
        
        /// Miscellaneous
        GameRegistry.addRecipe(new ItemStack(Item.feather, 1, 0), new Object[] {
            stickRecipe, '#', Item.silk
        });
        addRecipe(new ItemStack(Item.bow, 1, Item.bow.getMaxDamage() - 63), Block.dispenser);
        addRecipe(new ItemStack(Item.silk, 1, 0), Block.cloth);
        addRecipe(new ItemStack(Item.blazeRod, 1, 0), Item.brewingStand);
        addRecipe(new ItemStack(Item.netherStar, 1, 0), Block.beacon);
        addRecipe(new ItemStack(Item.netherQuartz, 1, 0), Block.daylightSensor);
        addRecipe(new ItemStack(Item.paper, 3, 0), Item.book);
        addRecipe(new ItemStack(Block.cobblestoneMossy, 1, 0), new ItemStack(Block.cobblestoneWall, 1, 1));
        addRecipe(new ItemStack(Block.pistonBase, 1, 0), Block.pistonStickyBase);
        addRecipe(new ItemStack(Block.chest, 1, 0), Block.chestTrapped);
        addRecipe(new ItemStack(Block.netherBrick, 1, 0), Block.netherFence);
        
        /// Place/break
        addRecipe(new ItemStack(Item.melon, 5, 0), Block.melon);
        addRecipe(new ItemStack(Item.book, 3, 0), Block.bookShelf);
        addRecipe(new ItemStack(Item.snowball, 4, 0), Block.blockSnow);
        addRecipe(new ItemStack(Item.clay, 4, 0), Block.blockClay);
        
        /// Stairs
        addRecipe(new ItemStack(Block.cobblestone, 1, 0), Block.stairsCobblestone);
        addRecipe(new ItemStack(Block.stoneBrick, 1, 0), Block.stairsStoneBrick);
        addRecipe(new ItemStack(Block.planks, 1, 0), Block.stairsWoodOak);
        addRecipe(new ItemStack(Block.planks, 1, 1), Block.stairsWoodSpruce);
        addRecipe(new ItemStack(Block.planks, 1, 2), Block.stairsWoodBirch);
        addRecipe(new ItemStack(Block.planks, 1, 3), Block.stairsWoodJungle);
        addRecipe(new ItemStack(Block.brick, 1, 0), Block.stairsBrick);
        addRecipe(new ItemStack(Block.netherBrick, 1, 0), Block.stairsNetherBrick);
        addRecipe(new ItemStack(Block.sandStone, 1, 0), Block.stairsSandStone);
        addRecipe(new ItemStack(Block.blockNetherQuartz, 1, 0), Block.stairsNetherQuartz);
        
        /// Slabs
        GameRegistry.addRecipe(new ItemStack(Block.stone, 1, 0), new Object[] {
            stickRecipe, '#', new ItemStack(Block.stoneSingleSlab, 1, 0)
        });
        GameRegistry.addRecipe(new ItemStack(Block.planks, 1, 0), new Object[] {
            stickRecipe, '#', new ItemStack(Block.stoneSingleSlab, 1, 2)
        });
        GameRegistry.addRecipe(new ItemStack(Block.cobblestone, 1, 0), new Object[] {
            stickRecipe, '#', new ItemStack(Block.stoneSingleSlab, 1, 3)
        });
        GameRegistry.addRecipe(new ItemStack(Block.brick, 1, 0), new Object[] {
            stickRecipe, '#', new ItemStack(Block.stoneSingleSlab, 1, 4)
        });
        GameRegistry.addRecipe(new ItemStack(Block.stoneBrick, 1, 0), new Object[] {
            stickRecipe, '#', new ItemStack(Block.stoneSingleSlab, 1, 5)
        });
        GameRegistry.addRecipe(new ItemStack(Block.netherBrick, 1, 0), new Object[] {
            stickRecipe, '#', new ItemStack(Block.stoneSingleSlab, 1, 6)
        });
        GameRegistry.addRecipe(new ItemStack(Block.planks, 1, 0), new Object[] {
            stickRecipe, '#', new ItemStack(Block.woodSingleSlab, 1, 0)
        });
        GameRegistry.addRecipe(new ItemStack(Block.planks, 1, 1), new Object[] {
            stickRecipe, '#', new ItemStack(Block.woodSingleSlab, 1, 1)
        });
        GameRegistry.addRecipe(new ItemStack(Block.planks, 1, 2), new Object[] {
            stickRecipe, '#', new ItemStack(Block.woodSingleSlab, 1, 2)
        });
        GameRegistry.addRecipe(new ItemStack(Block.planks, 1, 3), new Object[] {
            stickRecipe, '#', new ItemStack(Block.woodSingleSlab, 1, 3)
        });
    }
    
    /// Adds a simple, shapeless recipe.
    private void addRecipe(ItemStack output, Object... input) {
        GameRegistry.addShapelessRecipe(output, input);
    }
    
    /// Called before initialization. Loads the properties/configurations.
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        debugConsole("Loading in debug mode!");
        Properties.init(new Configuration(event.getSuggestedConfigurationFile()));
        if (Properties.getBoolean(Properties.GENERAL, "override_mushroom_stew"))
            Item.bowlSoup = new ItemStackableSoup(26, 6).setUnlocalizedName("mushroomStew").setTextureName("mushroom_stew");
    }
    
    /// Called during initialization. Registers entities, mob spawns, and renderers.
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        registerItems();
        addRecipes();
        registerEntities();
        proxy.registerRenderers();
        new CraftingHandler(); // Band-aid for knife dupe
        new TickHandler();
    }
    
    /// Called after initialization. Used to check for dependencies.
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
    
    /// Makes the first letter upper case.
	public static String cap(String string) {
        char[] chars = string.toCharArray();
        if (chars.length <= 0)
            return "";
        chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}
    
    /// Makes the first letter lower case.
	public static String decap(String string) {
        char[] chars = string.toCharArray();
        if (chars.length <= 0)
            return "";
        chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}
    
    /// Applies the potion's effect on the entity. If the potion is already active, its duration is increased up to the given duration and its amplifier is increased by the given amplifier + 1.
    public static void stackEffect(EntityLivingBase entity, Potion potion, int duration, int amplifier) {
        if (entity.isPotionActive(potion)) {
            PotionEffect potionEffect = entity.getActivePotionEffect(potion);
            entity.addPotionEffect(new PotionEffect(potion.id, Math.max(duration, potionEffect.getDuration()), potionEffect.getAmplifier() + amplifier + 1));
        }
        else
            entity.addPotionEffect(new PotionEffect(potion.id, duration, amplifier));
    }
    
    /// Applies the potion's effect on the entity. If the potion is already active, its duration is increased up to the given duration and its amplifier is increased by the given amplifier + 1 up to the given amplifierMax.
    public static void stackEffect(EntityLivingBase entity, Potion potion, int duration, int amplifier, int amplifierMax) {
        if (amplifierMax < 0) {
            stackEffect(entity, potion, duration, amplifier);
            return;
        }
        if (entity.isPotionActive(potion)) {
            PotionEffect potionEffect = entity.getActivePotionEffect(potion);
            entity.addPotionEffect(new PotionEffect(potion.id, Math.max(duration, potionEffect.getDuration()), Math.min(amplifierMax, potionEffect.getAmplifier() + amplifier + 1)));
        }
        else if (amplifier >= 0)
            entity.addPotionEffect(new PotionEffect(potion.id, duration, Math.min(amplifier, amplifierMax)));
    }
    
    /// Applies the enchantment to the itemStack at the given level. Called by all other enchantItem methods to do the actual enchanting.
    public static ItemStack enchantItem(ItemStack itemStack, Enchantment enchantment, int level) {
        itemStack.addEnchantment(enchantment, level);
        return itemStack;
    }
    
    /// Applies the enchantment with the given enchantment id and level.
    public static ItemStack enchantItem(ItemStack itemStack, int enchantmentID, int level) {
        enchantItem(itemStack, Enchantment.enchantmentsList[enchantmentID], level);
        return itemStack;
    }
    
    /// Creates an instance of an explosion at the exploder with the given power.
    public static Explosion explosion(Entity exploder, float power) {
        Explosion explosion = new Explosion(exploder.worldObj, exploder, exploder.posX, exploder.posY, exploder.posZ, power);
        explosion.isSmoking = Properties.getBoolean(Properties.GENERAL, "block_damage");
        return explosion;
    }
    
    /// Causes a standard explosion at the exploder with the given power.
    public static Explosion explode(Entity exploder, float power) {
        return explode(explosion(exploder, power));
    }
    
    /// Triggers an explosion that damages entities and blocks.
    public static Explosion explode(Explosion explosion) {
        explosion.doExplosionA();
        explosion.doExplosionB(true);
        return explosion;
    }
    
    /// Causes a fiery explosion at the exploder with the given power.
    public static Explosion explodeFire(Entity exploder, float power) {
        return explodeFire(explosion(exploder, power));
    }
    
    /// Makes the explosion fiery before calling explode(explosion).
    public static Explosion explodeFire(Explosion explosion) {
        explosion.isFlaming = true;
        return explode(explosion);
    }
    
    /// Causes an explosion that does not destroy blocks at the exploder with the given power.
    public static Explosion explodeSafe(Entity exploder, float power) {
        return explodeSafe(explosion(exploder, power));
    }
    
    /// Triggers an explosion that damages entities without destroying blocks.
    public static Explosion explodeSafe(Explosion explosion) {
        explosion.isSmoking = false;
        return explode(explosion);
    }
    
    /// Causes a fiery explosion at the exploder with the given power.
    public static Explosion explodeFireSafe(Entity exploder, float power) {
        return explodeFireSafe(explosion(exploder, power));
    }
    
    /// Makes the explosion fiery before calling explode(explosion).
    public static Explosion explodeFireSafe(Explosion explosion) {
        explosion.isFlaming = true;
        return explodeSafe(explosion);
    }
    
    /// Causes an explosion that damages and teleports entities without destroying blocks.
	public static Explosion explodeWarp(Entity exploder, float power) {
        return explodeWarp(explosion(exploder, power));
	}
    
    /// Triggers the damage part of an explosion without destroying blocks, also randomly teleports them.
    public static Explosion explodeWarp(Explosion explosion) {
        if (explosion.exploder == null)
            return explosion;
        explosion.isSmoking = false;
        World world = explosion.exploder.worldObj;
        double explosionSize = (double)(explosion.explosionSize * 2F);
        int dX = MathHelper.floor_double(explosion.explosionX - explosionSize - 1D);
        int dX1 = MathHelper.floor_double(explosion.explosionX + explosionSize + 1D);
        int dY = MathHelper.floor_double(explosion.explosionY - explosionSize - 1D);
        int dY1 = MathHelper.floor_double(explosion.explosionY + explosionSize + 1D);
        int dZ = MathHelper.floor_double(explosion.explosionZ - explosionSize - 1D);
        int dZ1 = MathHelper.floor_double(explosion.explosionZ + explosionSize + 1D);
        List entitiesHit = world.getEntitiesWithinAABBExcludingEntity(explosion.exploder, AxisAlignedBB.getAABBPool().getAABB((double)dX, (double)dY, (double)dZ, (double)dX1, (double)dY1, (double)dZ1));
        Vec3 posVec = world.getWorldVec3Pool().getVecFromPool(explosion.explosionX, explosion.explosionY, explosion.explosionZ);
        for (int i = 0; i < entitiesHit.size(); i++) {
            Entity entityHit = (Entity)entitiesHit.get(i);
            double scale = entityHit.getDistance(explosion.explosionX, explosion.explosionY, explosion.explosionZ) / explosionSize;
            if (scale <= 1D) {
                if (entityHit instanceof EntityLivingBase)
                    for (int j = 60; j-- > 0;)
                        if (teleportRandomly(entityHit))
                            break;
                entityHit.fallDistance = 0F;
                entityHit.attackEntityFrom(DamageSource.fall, 5);
            }
        }
        explosion.doExplosionB(true);
        return explosion;
    }
    
    /// Teleport the entity to a random nearby position
    public static boolean teleportRandomly(Entity entity) {
        double x = entity.posX + (random.nextDouble() - 0.5D) * 64D;
        double y = entity.posY + (double)(random.nextInt(64) - 32);
        double z = entity.posZ + (random.nextDouble() - 0.5D) * 64D;
        return teleportEntityTo(entity, x, y, z);
    }
    
    /// Teleport the entity to the given co-ordinates.
    public static boolean teleportEntityTo(Entity entity, double x, double y, double z) {
        if (entity == null || entity.worldObj.isRemote)
            return true;
        World world = entity.worldObj;
        double xI = entity.posX;
        double yI = entity.posY;
        double zI = entity.posZ;
        boolean canTeleport = false;
        int blockX = MathHelper.floor_double(x);
        int blockY = MathHelper.floor_double(y);
        int blockZ = MathHelper.floor_double(z);
        int blockID;
        if (world.blockExists(blockX, blockY, blockZ)) {
            boolean canTeleportToBlock = false;
            while (!canTeleportToBlock && blockY > 0) {
                blockID = world.getBlockId(blockX, blockY - 1, blockZ);
                if (blockID != 0 && Block.blocksList[blockID].blockMaterial.blocksMovement())
                    canTeleportToBlock = true;
                else {
                    y--;
                    blockY--;
                }
            }
            if (canTeleportToBlock) {
                if (!(entity instanceof EntityLivingBase))
                    entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
                else if (!(entity instanceof EntityPlayerMP) || !((EntityPlayerMP)entity).playerNetServerHandler.connectionClosed)
                    ((EntityLivingBase)entity).setPositionAndUpdate(x, y, z);
                if (world.getCollidingBoundingBoxes(entity, entity.boundingBox).size() == 0 && !world.isAnyLiquid(entity.boundingBox))
                    canTeleport = true;
            }
        }
        if (!canTeleport) {
            if (!(entity instanceof EntityLivingBase))
                entity.setLocationAndAngles(xI, yI, zI, entity.rotationYaw, entity.rotationPitch);
            else if (!(entity instanceof EntityPlayerMP) || !((EntityPlayerMP)entity).playerNetServerHandler.connectionClosed)
                ((EntityLivingBase)entity).setPositionAndUpdate(xI, yI, zI);
            return false;
        }
        else {
            PacketHandler.sendTeleportPacket(entity, xI, yI, zI, x, y, z);
            return true;
        }
    }
    
    /// Prints the message to the console with this mod's name tag.
    public static void console(String message) {
        System.out.println("[DungeonCrawler] " + message);
    }
    
    /// Prints the message to the console with this mod's name tag if debugging is enabled.
    public static void debugConsole(String message) {
        if (debug)
            System.out.println("[DungeonCrawler] (debug) " + message);
    }
    
    /// Throws an exception with the message and this mod's name tag if debugging is enabled.
    public static void debugException(String message) {
        if (debug)
            throw new RuntimeException("[DungeonCrawler] (debug) " + message);
    }
}