package toast.dungeonCrawler;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.util.EnumHelper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * This is the mod class. Everything the mod does is initialized by this class.
 */
@Mod(modid = DungeonCrawlerMod.MODID, name = "Dungeon Crawler", version = DungeonCrawlerMod.VERSION)
public class DungeonCrawlerMod {

    /* INFO *\
    >> Blocks
        > blocks added: 0
        > IDs used: 0
    >> Items
        > items added: 57
        > IDs used: 14
    \* **** */
    /* TO DO *\
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
    \* ** ** */

    /** This mod's id. */
    public static final String MODID = "DungeonCrawler";
    /** This mod's version. */
    public static final String VERSION = "0.2.5";

    /** If true, this mod starts up in debug mode. */
    public static boolean debug = false;
    /** This mod's sided proxy. */
    @SidedProxy(clientSide = "toast.dungeonCrawler.client.ClientProxy", serverSide = "toast.dungeonCrawler.CommonProxy")
    public static CommonProxy proxy;

    /** The random number generator for this mod. */
    public static final Random random = new Random();
    /** The network channel for this mod. */
    public static SimpleNetworkWrapper CHANNEL;

    /** The path to the textures folder. */
    public static final String TEXTURE_PATH = DungeonCrawlerMod.MODID + ":textures/";

    // Item variables.
    public static Item[] knife = new Item[5];
    public static Item sharpStick;
    public static Item boneClub;
    public static Item rockshot;
    public static Item[] boneArmor = new Item[4];
    public static Item DCItem;
    public static Item DCFood;
    /** Trap data map. */
    public static HashMap<EntityPlayer, int[]> trapData = new HashMap<EntityPlayer, int[]>();
    // Material variables.
    /** The armor material used by {@link #boneArmor}. */
    public static final ItemArmor.ArmorMaterial boneArmorMaterial = EnumHelper.addArmorMaterial("BONE", 6, new int[] {1, 2, 1, 1}, 0);

    /** Called before initialization. Loads the properties/configurations. */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Properties.init(new File(event.getModConfigurationDirectory(), DungeonCrawlerMod.MODID));
        DungeonCrawlerMod.debug = Properties.getBoolean(Properties.GENERAL, "debug");
        DungeonCrawlerMod.logDebug("Loading in debug mode!");

        DungeonCrawlerMod.CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("DungeonCrawler|Info");
        int id = 0;
        //DungeonCrawlerMod.CHANNEL.registerMessage(MessageWorldDifficulty.Handler.class, MessageWorldDifficulty.class, id++, Side.CLIENT);

        this.registerItems();
    }

    /** Called during initialization. Registers entities, mob spawns, and renderers. */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        this.registerEntities();
        DungeonCrawlerMod.proxy.registerRenderers();
    }

    /** Registers the entities in this mod. */
	private void registerEntities() {
        int id = 0;
        //EntityRegistry.registerModEntity(EntityKnife.class, "Knife", id++, this, 64, 10, false);
        EntityRegistry.registerModEntity(EntityRock.class, "Rock", id++, this, 64, 10, true);
        EntityRegistry.registerModEntity(EntityPebble.class, "Pebble", id++, this, 64, 10, true);

        EntityRegistry.registerModEntity(EntityBomb.class, "Bomb", id++, this, 80, 5, false);
        EntityRegistry.registerModEntity(EntityGrenade.class, "Grenade", id++, this, 64, 10, true);
        EntityRegistry.registerModEntity(EntityMine.class, "Mine", id++, this, 80, 5, true);
        EntityRegistry.registerModEntity(EntityGasCloud.class, "GasCloud", id++, this, 0, Integer.MAX_VALUE, false);
        EntityRegistry.registerModEntity(EntityTrap.class, "Trap", id++, this, 80, Integer.MAX_VALUE, false);
	}

    /** Registers the items in this mod. */
	private void registerItems() {
        int renderIndex;
        String texture = DungeonCrawlerMod.MODID + ":";

        //DungeonCrawlerMod.bucketHelm = (ItemBucketHelm) new ItemBucketHelm().setUnlocalizedName("bucketHelm").setCreativeTab(CreativeTabs.tabCombat).setTextureName(texture + "bucket_helm");
        //GameRegistry.registerItem(DungeonCrawlerMod.bucketHelm, DungeonCrawlerMod.bucketHelm.getUnlocalizedName().substring(5));

        renderIndex = DungeonCrawlerMod.proxy.getRenderIndex("bone", 5);
        for (int i = 0; i < 4; i++) {
            if (this.isEnabled("armor_bone_" + ItemHelper.getArmorName(i).toLowerCase())) {
				DungeonCrawlerMod.boneArmor[i] = new ItemBoneArmor(renderIndex, i).setTextureName(texture + "bone_" + i);
			}
        }

        if (this.isEnabled("bone_club")) {
			DungeonCrawlerMod.boneClub = new ItemBoneClub().setTextureName(texture + "bone_club");
		}
        if (this.isEnabled("dc_food")) {
			DungeonCrawlerMod.DCFood = new ItemDCFood().setTextureName(texture + "zombie_meat");
		}
        if (this.isEnabled("dc_item")) {
			DungeonCrawlerMod.DCItem = new ItemDCItem().setTextureName(texture + "pebble");
		}

        for (int i = 0; i < ItemHelper.materials.length; i++) {
        	if (this.isEnabled("knife_" + ItemHelper.getMaterialName(i).toLowerCase())) {
				DungeonCrawlerMod.knife[i] = new ItemKnife(ItemHelper.materials[i]).setTextureName(texture + "knife_" + i);
			}
        }

        if (this.isEnabled("rockshot")) {
			DungeonCrawlerMod.rockshot = new ItemRockshot().setTextureName(texture + "rockshot");
		}
        if (this.isEnabled("sharp_stick")) {
			DungeonCrawlerMod.sharpStick = new ItemSharpStick().setTextureName(texture + "sharp_stick");
		}
	}

    // Gets the appropriate ID for the given item name, based on the properties.
    private boolean isEnabled(String item) {
        return Properties.getBoolean(Properties.ITEMS, item);
    }

    // Registers the crafting and smelting recipes in this mod.
    private void addRecipes() {
        boolean boneSticks = Properties.getBoolean(Properties.GENERAL, "bone_stick_recipes");
        Block[] pressurePlates = { Blocks.stone_pressure_plate, Blocks.wooden_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate };
        Block[] buttons = { Blocks.stone_button, Blocks.wooden_button };
        Item[][] tools = {
            { Items.wooden_sword, Items.stone_sword, Items.iron_sword, Items.diamond_sword, Items.golden_sword },
            { Items.wooden_shovel, Items.stone_shovel, Items.iron_shovel, Items.diamond_shovel, Items.golden_shovel },
            { Items.wooden_pickaxe, Items.stone_pickaxe, Items.iron_pickaxe, Items.diamond_pickaxe, Items.golden_pickaxe },
            { Items.wooden_axe, Items.stone_axe, Items.iron_axe, Items.diamond_axe, Items.golden_axe },
            { Items.wooden_hoe, Items.stone_hoe, Items.iron_hoe, Items.diamond_hoe, Items.golden_hoe }
        };
        Item[] chainmail = { Items.chainmail_helmet, Items.chainmail_chestplate, Items.chainmail_leggings, Items.chainmail_boots };
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
        if (DungeonCrawlerMod.debug) {
            if (DungeonCrawlerMod.knife[0] != null) {
				GameRegistry.addRecipe(ItemHelper.knife("WOODEN"), new Object[] {
                    "#",
                    "#", '#', Blocks.dirt
                });
			}
            if (DungeonCrawlerMod.DCItem != null) {
                addRecipe(new ItemStack(Items.mushroom_stew, 64, 0), Blocks.dirt);
                addRecipe(ItemHelper.foodCraft("spider_bucket", 64), Blocks.dirt, Blocks.dirt);
            }
            for (int i = 0; i < ItemHelper.materials.length; i++) if (DungeonCrawlerMod.knife[i] != null) {
				addRecipe(ItemHelper.knife(i + 1 == DungeonCrawlerMod.knife.length ? 0 : i + 1), ItemHelper.knife(i, true));
			}
            if (DungeonCrawlerMod.rockshot != null) {
                GameRegistry.addRecipe(new ItemStack(DungeonCrawlerMod.rockshot, 1, 0), new Object[] {
                    "###",
                    "#  ", '#', Blocks.dirt
                });
                addRecipe(ItemRockshot.setAmmo(new ItemStack(DungeonCrawlerMod.rockshot, 1, 0), new ItemStack(Blocks.cobblestone)), new ItemStack(DungeonCrawlerMod.rockshot, 1, 32767));
            }
        }

        /// Knife crafting
        recipe = Properties.getBoolean(Properties.ALT_RECIPES, "knife") ? new String[] { " #", "| " } : new String[] { "#", "|" };
        for (int i = 0; i < ItemHelper.materials.length; i++) {
            if (DungeonCrawlerMod.knife[i] == null) {
				continue;
			}
            ItemStack craftKnife = ItemHelper.knife(i, true);
            GameRegistry.addRecipe(ItemHelper.knife(i), new Object[] {
                recipe, '#', new ItemStack(ItemHelper.materialIDs[i], 1, 32767), '|', Items.stick
            });
            if (boneSticks) {
                GameRegistry.addRecipe(ItemHelper.knife(i), new Object[] {
                    recipe, '#', new ItemStack(ItemHelper.materialIDs[i], 1, 32767), '|', Items.bone
                });
                addRecipe(new ItemStack(Items.arrow, 4, 0), craftKnife, Items.flint, Items.bone, Items.feather);
                addRecipe(new ItemStack(Items.arrow, 4, 0), craftKnife, Items.quartz, Items.bone, Items.feather);
            }

            /// Main recipes
            if (DungeonCrawlerMod.sharpStick != null) {
				addRecipe(ItemHelper.sharpStick(), craftKnife, Items.stick);
			}
            if (DungeonCrawlerMod.boneClub != null) {
				addRecipe(ItemHelper.boneClub(), craftKnife, Items.bone);
			}
            if (DungeonCrawlerMod.DCItem != null) {
                if (ItemHelper.isEnabled(ItemHelper.BOMB + 1)) {
					addRecipe(ItemHelper.impBomb(1), craftKnife, Items.string, Items.gunpowder, Items.gunpowder);
				}
                if (ItemHelper.isEnabled(ItemHelper.BOMB + 4)) {
					addRecipe(ItemHelper.impBomb(4), craftKnife, Items.string, Items.gunpowder, Items.blaze_powder);
				}
                if (ItemHelper.isEnabled(ItemHelper.BOMB + 7)) {
					addRecipe(ItemHelper.impBomb(7), craftKnife, Items.string, Items.gunpowder, Items.ender_pearl);
				}
                if (ItemHelper.isEnabled(ItemHelper.BOMB + 2)) {
					addRecipe(ItemHelper.impBomb(2), craftKnife, Blocks.tnt);
				}
                if (ItemHelper.isEnabled(ItemHelper.BASIC, "pebble")) {
					addRecipe(ItemHelper.pebble(6), craftKnife, Blocks.gravel);
				}
                if (DungeonCrawlerMod.sharpStick != null && ItemHelper.isEnabled(ItemHelper.TRAP, "trap_spike")) {
					addRecipe(ItemHelper.trap("trap_spike"), craftKnife, ItemHelper.sharpStick(), ItemHelper.sharpStick(), ItemHelper.sharpStick());
				}
            }

            /// Survival recipes
            addRecipe(new ItemStack(Items.stick, 2, 0), craftKnife, Blocks.planks);
            addRecipe(new ItemStack(Items.arrow, 4, 0), craftKnife, Items.flint, Items.stick, Items.feather);
            addRecipe(new ItemStack(Items.arrow, 4, 0), craftKnife, Items.quartz, Items.stick, Items.feather);
            for (int j = 0; j < 2; j++) {
                addRecipe(new ItemStack(tools[0][j], 1, 0), craftKnife, new ItemStack(ItemHelper.materialIDs[j], 1, 32767), new ItemStack(ItemHelper.materialIDs[j], 1, 32767), Items.stick);
                if (boneSticks) {
					addRecipe(new ItemStack(tools[0][j], 1, 0), craftKnife, new ItemStack(ItemHelper.materialIDs[j], 1, 32767), new ItemStack(ItemHelper.materialIDs[j], 1, 32767), Items.bone);
				}
            }
            addRecipe(new ItemStack(Items.potato, 1, 0), craftKnife, Items.poisonous_potato);
            if (DungeonCrawlerMod.DCFood != null) {
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_SMELT, "zombie_meat")) {
					addRecipe(ItemHelper.foodSmelt("zombie_meat"), craftKnife, Items.rotten_flesh);
				}
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_meat")) {
                    addRecipe(ItemHelper.foodCraft("spider_meat"), craftKnife, Items.spider_eye);
                    if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_soup")) {
						addRecipe(ItemHelper.foodCraft("spider_soup"), craftKnife, ItemHelper.foodCraft("spider_meat"), ItemHelper.foodCraft("spider_meat"), Items.bowl);
					}
                    if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_bucket")) {
						addRecipe(ItemHelper.foodCraft("spider_soup"), craftKnife, ItemHelper.foodCraft("spider_meat"), ItemHelper.foodCraft("spider_meat"), Items.bucket);
					}
                }
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "mushroom_bucket")) {
					addRecipe(ItemHelper.foodCraft("mushroom_bucket"), craftKnife, Blocks.brown_mushroom, Blocks.red_mushroom, Items.bucket);
				}
            }
        }

        /// Main recipes
        if (DungeonCrawlerMod.boneClub != null) {
            recipe = Properties.getBoolean(Properties.ALT_RECIPES, "bone_club") ? new String[] { " #", "# " } : new String[] { "#", "#" };
            GameRegistry.addRecipe(ItemHelper.boneClub(), new Object[] {
                recipe, '#', Items.bone
            });
        }
        if (DungeonCrawlerMod.rockshot != null) {
            GameRegistry.addRecipe(new ItemStack(DungeonCrawlerMod.rockshot, 1, 0), new Object[] {
                "&##",
                "@##",
                "|  ", '&', Items.flint_and_steel, '|', Items.stick, '@', Blocks.planks, '#', Blocks.cobblestone
            });
            GameRegistry.addRecipe(new RecipeRockshot());
        }
        for (int i = 0; i < armorRecipes.length; i++) {
            if (DungeonCrawlerMod.boneArmor[i] != null) {
				GameRegistry.addRecipe(ItemHelper.boneArmor(i), new Object[] {
                    armorRecipes[i], '#', Items.bone
                });
			}
            GameRegistry.addRecipe(new ItemStack(chainmail[i], 1, 0), new Object[] {
                armorRecipes[i], '#', ItemHelper.chainLinks()
            });
        }
        if (DungeonCrawlerMod.DCItem != null) {
            if (ItemHelper.isEnabled(ItemHelper.BASIC, "pebble")) {
				addRecipe(ItemHelper.pebble(3), Blocks.gravel);
			}
            if (ItemHelper.isEnabled(ItemHelper.BASIC, "chain"))
                if (Properties.getBoolean(Properties.ALT_RECIPES, "chain")) {
					addRecipe(ItemHelper.chainLinks(6), Items.iron_ingot, Items.iron_ingot);
				} else {
					addRecipe(ItemHelper.chainLinks(3), Items.iron_ingot);
				}

            /// Improvised bombs
            recipe = Properties.getBoolean(Properties.ALT_RECIPES, "bomb") ? new String[] { "  @", " # ", "%  " } : new String[] { "@", "#", "%" };
            if (ItemHelper.isEnabled(ItemHelper.BOMB + 1)) {
				GameRegistry.addRecipe(ItemHelper.impBomb(1), new Object[] {
                    recipe, '@', Items.string, '#', Items.gunpowder, '%', Items.gunpowder
                });
			}
            if (ItemHelper.isEnabled(ItemHelper.BOMB + 4)) {
				GameRegistry.addRecipe(ItemHelper.impBomb(4), new Object[] {
                    recipe, '@', Items.string, '#', Items.gunpowder, '%', Items.blaze_powder
                });
			}
            if (ItemHelper.isEnabled(ItemHelper.BOMB + 7)) {
				GameRegistry.addRecipe(ItemHelper.impBomb(7), new Object[] {
                    recipe, '@', Items.string, '#', Items.gunpowder, '%', Items.ender_pearl
                });
			}
            for (int i = 0; i < 64; i++) {
                if (!ItemHelper.isEnabled(ItemHelper.BOMB + i)) {
					continue;
				}
                if (ItemHelper.isEnabled(ItemHelper.MINE + i)) {
					for (int j = 0; j < pressurePlates.length; j++) {
						GameRegistry.addRecipe(ItemHelper.mine(i), new Object[] {
					        "-",
					        "#", '#', ItemHelper.impBomb(i), '-', pressurePlates[j]
					    });
					}
				}
                if (ItemHelper.isEnabled(ItemHelper.GRENADE + i)) {
					for (int j = 0; j < buttons.length; j++){
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
				}
                if (i % 3 == 2 || !ItemHelper.isEnabled(ItemHelper.BOMB + i + 1)) {
					continue;
				}
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
            if (ItemHelper.isEnabled(ItemHelper.MINE, "mine_gas")) {
				for (int i = 0; i < pressurePlates.length; i++) {
					GameRegistry.addRecipe(ItemHelper.gasMine(), new Object[] {
				        " - ",
				        "@#@",
				        "&&&", '-', pressurePlates[i], '@', Items.dye, '#', Items.gunpowder, '&', new ItemStack(Items.coal, 1, 32767)
				    });
				}
			}

            /// Traps
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_spike")) {
                if (DungeonCrawlerMod.sharpStick != null) {
					GameRegistry.addRecipe(ItemHelper.trap("trap_spike"), new Object[] {
                        "|||", '|', ItemHelper.sharpStick()
                    });
				}
                if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_spike_poison")) {
					GameRegistry.addRecipe(ItemHelper.trap("trap_spike_poison"), new Object[] {
                        "@",
                        "#", '@', Items.spider_eye, '#', ItemHelper.trap("trap_spike")
                    });
				}
            }
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_fire")) {
				for (int i = 0; i < pressurePlates.length; i++) {
					GameRegistry.addRecipe(ItemHelper.trap("trap_fire"), new Object[] {
				        " - ",
				        "#&#", '-', pressurePlates[i], '#', Blocks.planks, '&', new ItemStack(Items.coal, 1, 32767)
				    });
				}
			}
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_end")) {
				for (int i = 0; i < pressurePlates.length; i++) {
					GameRegistry.addRecipe(ItemHelper.trap("trap_end"), new Object[] {
				        " - ",
				        "#@#", '-', pressurePlates[i], '#', Blocks.stone, '@', Items.ender_pearl
				    });
				}
			}
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_screamer")) {
				for (int i = 0; i < pressurePlates.length; i++) {
					GameRegistry.addRecipe(ItemHelper.trap("trap_screamer"), new Object[] {
				        " - ",
				        "*#*", '-', pressurePlates[i], '*', Items.ghast_tear, '#', Items.reeds
				    });
				}
			}
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_launcher")) {
				GameRegistry.addRecipe(ItemHelper.trap("trap_launcher"), new Object[] {
                    " - ",
                    "#=#", '-', Blocks.stone_pressure_plate, '#', Blocks.stone, '=', Blocks.piston
                });
			}
            if (ItemHelper.isEnabled(ItemHelper.TRAP, "trap_crippling")) {
				GameRegistry.addRecipe(ItemHelper.trap("trap_crippling"), new Object[] {
                    "# #",
                    "#-#", '-', Blocks.stone_pressure_plate, '#', Items.iron_ingot
                });
			}
        }

        /// Survival recipes
        GameRegistry.addRecipe(new ItemStack(Items.flint_and_steel, 1, 0), new Object[] {
            "# ",
            " &", '#', Items.iron_ingot, '&', Items.quartz
        });
        GameRegistry.addRecipe(new ItemStack(Items.arrow, 4, 0), new Object[] {
            "@",
            "|",
            "#", '@', Items.quartz, '|', Items.stick, '#', Items.feather
        });
        addRecipe(new ItemStack(Items.flint, 1, 0), Blocks.gravel, Blocks.gravel);
        GameRegistry.addSmelting(Blocks.gravel, new ItemStack(Items.flint), 0.1F);
        if (DungeonCrawlerMod.DCFood != null) {
            if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_BASIC, "cookie_golden")) {
				GameRegistry.addRecipe(ItemHelper.food("cookie_golden"), new Object[] {
                    "***",
                    "*O*",
                    "***", 'O', Items.cookie, '*', Items.gold_nugget
                });
			}
            if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_SMELT, "zombie_meat") && ItemHelper.isFoodEnabled(ItemHelper.FOOD_SMELT, "zombie_meat_cooked")) {
				FurnaceRecipes.smelting().addSmelting(DungeonCrawlerMod.DCFood, ItemHelper.getDamageForFood(ItemHelper.FOOD_SMELT, "zombie_meat"), ItemHelper.foodSmelt("zombie_meat_cooked"), 0.1F);
			}
            if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_meat")) {
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_soup")) {
					GameRegistry.addRecipe(ItemHelper.foodCraft("spider_soup"), new Object[] {
                        "@",
                        "@",
                        "U", '@', ItemHelper.foodCraft("spider_meat"), 'U', Items.bowl
                    });
				}
                if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "spider_bucket")) {
					GameRegistry.addRecipe(ItemHelper.foodCraft("spider_bucket"), new Object[] {
                        "@",
                        "@",
                        "U", '@', ItemHelper.foodCraft("spider_meat"), 'U', Items.bucket
                    });
				}
            }
            if (ItemHelper.isFoodEnabled(ItemHelper.FOOD_CRAFT, "mushroom_bucket")) {
                GameRegistry.addRecipe(ItemHelper.foodCraft("mushroom_bucket"), new Object[] {
                    "b",
                    "r",
                    "U", 'b', Blocks.brown_mushroom, 'r', Blocks.red_mushroom, 'U', Items.bucket
                });
                GameRegistry.addRecipe(ItemHelper.foodCraft("mushroom_bucket"), new Object[] {
                    "r",
                    "b",
                    "U", 'b', Blocks.brown_mushroom, 'r', Blocks.red_mushroom, 'U', Items.bucket
                });
            }
        }

        /// Bone-stick recipes
        if (boneSticks) {
            for (int i = 0; i < tools.length; i++) {
                for (int j = 0; j < tools[i].length; j++) {
                    GameRegistry.addRecipe(new ItemStack(tools[i][j], 1, 0), new Object[] {
                        toolRecipes[i], '#', new ItemStack(ItemHelper.materialIDs[j], 1, 32767), '|', Items.bone
                    });
                }
            }
            if (DungeonCrawlerMod.rockshot != null) {
				GameRegistry.addRecipe(new ItemStack(DungeonCrawlerMod.rockshot, 1, 0), new Object[] {
                    "&##",
                    "@##",
                    "|  ", '&', Items.flint_and_steel, '|', Items.bone, '@', Blocks.planks, '#', Blocks.cobblestone
                });
			}
            GameRegistry.addRecipe(new ItemStack(Items.fishing_rod, 1, 0), new Object[] {
                "  |",
                " |@",
                "| @", '@', Items.string, '|', Items.bone
            });
            GameRegistry.addRecipe(new ItemStack(Items.bow, 1, 0), new Object[] {
                "@| ",
                "@ |",
                "@| ", '@', Items.string, '|', Items.bone
            });
            GameRegistry.addRecipe(new ItemStack(Items.arrow, 4, 0), new Object[] {
                "@",
                "|",
                "#", '@', Items.flint, '|', Items.bone, '#', Items.feather
            });
            GameRegistry.addRecipe(new ItemStack(Items.arrow, 4, 0), new Object[] {
                "@",
                "|",
                "#", '@', Items.quartz, '|', Items.bone, '#', Items.feather
            });
            GameRegistry.addRecipe(new ItemStack(Blocks.torch, 4, 0), new Object[] {
                "@",
                "|", '@', new ItemStack(Items.coal, 1, 32767), '|', Items.bone
            });
            GameRegistry.addRecipe(new ItemStack(Blocks.redstone_torch, 1, 0), new Object[] {
                "@",
                "|", '@', Items.redstone, '|', Items.bone
            });
        }
        if (Properties.getBoolean(Properties.GENERAL, "salvage_recipes")) {
			//addSalvageRecipes();
		}
    }

    /** Prints the message to the console with this mod's name tag. */
    public static void log(String message) {
        System.out.println("[" + DungeonCrawlerMod.MODID + "] " + message);
    }
    /** Prints the message to the console with this mod's name tag if debugging is enabled. */
    public static void logDebug(String message) {
        if (DungeonCrawlerMod.debug) {
            System.out.println("[" + DungeonCrawlerMod.MODID + "] [debug] " + message);
        }
    }
    /** Prints the message to the console with this mod's name tag and a warning tag. */
    public static void logWarning(String message) {
        System.out.println("[" + DungeonCrawlerMod.MODID + "] [WARNING] " + message);
    }
    /** Prints the message to the console with this mod's name tag and an error tag.<br>
     * Throws a runtime exception with a message and this mod's name tag if debugging is enabled. */
    public static void logError(String message) {
        if (DungeonCrawlerMod.debug)
            throw new RuntimeException("[" + DungeonCrawlerMod.MODID + "] " + message);
        DungeonCrawlerMod.log("[ERROR] " + message);
    }
    /** Throws a runtime exception with a message and this mod's name tag. */
    public static void exception(String message) {
        throw new RuntimeException("[" + DungeonCrawlerMod.MODID + "] " + message);
    }
}
