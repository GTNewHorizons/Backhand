package xonin.backhand;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xonin.backhand.api.PlayerEventChild;
import xonin.backhand.api.core.BackhandTranslator;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.InventoryPlayerBackhand;
import xonin.backhand.api.core.OffhandExtendedProperty;
import xonin.backhand.packet.OffhandConfigSyncPacket;
import xonin.backhand.packet.OffhandPlaceBlockPacket;
import xonin.backhand.packet.OffhandSyncItemPacket;
import xonin.backhand.utils.EnumAnimations;

public final class HookContainerClass {

    public static final HookContainerClass INSTANCE = new HookContainerClass();

    private HookContainerClass() {}

    private boolean isFake(Entity entity) {
        return entity instanceof FakePlayer;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayer player && !(isFake(player))) {
            if (FMLCommonHandler.instance()
                .getEffectiveSide() == Side.SERVER) {
                if (!(player.inventory instanceof InventoryPlayerBackhand)) {
                    Backhand.LOGGER.info("Player inventory has been replaced with {}", player.inventory.getClass());
                }
                Backhand.packetHandler.sendPacketToPlayer(
                    new OffhandConfigSyncPacket(player).generatePacket(),
                    (EntityPlayerMP) event.entity);
                Backhand.packetHandler.sendPacketToPlayer(
                    new OffhandSyncItemPacket(player).generatePacket(),
                    (EntityPlayerMP) event.entity);
            }
            ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
            if (Backhand.isOffhandBlacklisted(offhandItem)) {
                BackhandUtils.setPlayerOffhandItem(player, null);
                if (!player.inventory.addItemStackToInventory(offhandItem)) {
                    event.entity.entityDropItem(offhandItem, 0);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityConstructing(EntityEvent.EntityConstructing event) {
        if (!(event.entity instanceof EntityPlayer player && !(isFake(event.entity)))) return;
        event.entity.registerExtendedProperties("OffhandStorage", new OffhandExtendedProperty(player));
    }

    public static MovingObjectPosition getRaytraceBlock(EntityPlayer p) {
        float scaleFactor = 1.0F;
        float rotPitch = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * scaleFactor;
        float rotYaw = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * scaleFactor;
        double testX = p.prevPosX + (p.posX - p.prevPosX) * scaleFactor;
        double testY = p.prevPosY + (p.posY - p.prevPosY) * scaleFactor + 1.62D - p.yOffset;// 1.62 is player eye height
        double testZ = p.prevPosZ + (p.posZ - p.prevPosZ) * scaleFactor;
        Vec3 testVector = Vec3.createVectorHelper(testX, testY, testZ);
        float var14 = MathHelper.cos(-rotYaw * 0.017453292F - (float) Math.PI);
        float var15 = MathHelper.sin(-rotYaw * 0.017453292F - (float) Math.PI);
        float var16 = -MathHelper.cos(-rotPitch * 0.017453292F);
        float vectorY = MathHelper.sin(-rotPitch * 0.017453292F);
        float vectorX = var15 * var16;
        float vectorZ = var14 * var16;
        double reachLength = 5.0D;
        Vec3 testVectorFar = testVector.addVector(vectorX * reachLength, vectorY * reachLength, vectorZ * reachLength);
        return p.worldObj.rayTraceBlocks(testVector, testVectorFar, false);
    }

    @SubscribeEvent
    public void playerInteract(PlayerInteractEvent event) {
        if (isFake(event.entityPlayer)) return;

        if (!Backhand.EmptyOffhand && BackhandUtils.getOffhandItem(event.entityPlayer) == null) {
            return;
        }

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR
            || event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {// Right click
            ItemStack mainHandItem = event.entityPlayer.getCurrentEquippedItem();
            ItemStack offhandItem = BackhandUtils.getOffhandItem(event.entityPlayer);

            if (mainHandItem != null
                && (BackhandUtils.checkForRightClickFunction(mainHandItem) || offhandItem == null)) {
                return;
            }

            if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && Backhand.proxy.isRightClickHeld()) {
                Backhand.proxy.setRightClickCounter(Backhand.proxy.getRightClickCounter() + 1);
                if (Backhand.proxy.getRightClickCounter() > 1) {
                    return;
                }
            }

            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && mainHandItem != null
                && mainHandItem.getItem() instanceof ItemMonsterPlacer) {
                if (event.world.isRemote && !event.entityPlayer.capabilities.isCreativeMode) {
                    mainHandItem.stackSize--;
                }
            }

            boolean swingHand = true;
            PlayerInteractEvent.Result blk = event.useBlock;
            PlayerInteractEvent.Result itm = event.useItem;
            event.useBlock = PlayerInteractEvent.Result.DENY;
            MovingObjectPosition mop = getRaytraceBlock(event.entityPlayer);
            if (mop != null) {
                event.setCanceled(true);
                int i = mop.blockX, j = mop.blockY, k = mop.blockZ;

                if (!event.entityPlayer.isSneaking()
                    && canBlockBeInteractedWith(event.entityPlayer.worldObj, i, j, k)) {
                    event.setCanceled(false);
                    event.useBlock = blk;
                    event.useItem = itm;
                    swingHand = false;
                }
            }
            if (event.entityPlayer.worldObj.isRemote && !BackhandUtils.usagePriorAttack(offhandItem)
                && Backhand.OffhandAttack
                && swingHand) {
                HookContainerClass.sendOffSwingEventNoCheck(event.entityPlayer, mainHandItem, offhandItem);
            }
        }
    }

    private static String[] activatedBlockMethodNames = {
        BackhandTranslator.getMapedMethodName("Block", "func_149727_a", "onBlockActivated"),
        BackhandTranslator.getMapedMethodName("Block", "func_149699_a", "onBlockClicked") };
    private static Class[][] activatedBlockMethodParams = {
        new Class[] { World.class, int.class, int.class, int.class, EntityPlayer.class, int.class, float.class,
            float.class, float.class },
        new Class[] { World.class, int.class, int.class, int.class, EntityPlayer.class } };

    @SuppressWarnings("unchecked")
    public static boolean canBlockBeInteractedWith(World worldObj, int x, int y, int z) {
        if (worldObj == null) return false;
        Block block = worldObj.getBlock(x, y, z);
        if (block == null) return false;
        if (block.getClass()
            .equals(Block.class)) return false;
        try {
            Class c = block.getClass();
            while (!(c.equals(Block.class))) {
                try {
                    try {
                        c.getDeclaredMethod(activatedBlockMethodNames[0], activatedBlockMethodParams[0]);
                        return true;
                    } catch (NoSuchMethodException ignored) {}

                    try {
                        c.getDeclaredMethod(activatedBlockMethodNames[1], activatedBlockMethodParams[1]);
                        return true;
                    } catch (NoSuchMethodException ignored) {}
                } catch (NoClassDefFoundError ignored) {

                }

                c = c.getSuperclass();
            }

            return false;
        } catch (NullPointerException e) {
            return true;
        }
    }

    public static boolean changedHeldItemTooltips = false;
    // used in hostwapping the item to dig with, to remember where to return the main slot to
    public static int prevOffhandOffset;

    public static boolean isItemBlock(Item item) {
        return item instanceof ItemBlock || item instanceof ItemDoor
            || item instanceof ItemSign
            || item instanceof ItemReed
            || item instanceof ItemSeedFood
            || item instanceof ItemRedstone
            || item instanceof ItemBucket
            || item instanceof ItemSkull;
    }

    /**
     * Attempts to right-click-use an item by the given EntityPlayer
     */
    public static boolean tryUseItem(EntityPlayer player, ItemStack itemStack, Side side) {
        if (side.isClient()) {
            Backhand.packetHandler.sendPacketToServer(
                new OffhandPlaceBlockPacket(-1, -1, -1, 255, itemStack, 0.0F, 0.0F, 0.0F).generatePacket());
        }
        final int i = itemStack.stackSize;
        final int j = itemStack.getItemDamage();
        ItemStack prevHeldItem = player.getCurrentEquippedItem();

        player.setCurrentItemOrArmor(0, itemStack);
        ItemStack itemUsed = player.getCurrentEquippedItem()
            .copy();
        ItemStack itemStackResult = itemStack.useItemRightClick(player.getEntityWorld(), player);
        if (!ItemStack.areItemStacksEqual(itemUsed, itemStackResult)) {
            BackhandUtils.setPlayerOffhandItem(player, itemStackResult);
            BackhandUtils.getOffhandEP(player).syncOffhand = true;
            if (player.getCurrentEquippedItem() == null || player.getCurrentEquippedItem().stackSize == 0) {
                ForgeEventFactory.onPlayerDestroyItem(player, player.getCurrentEquippedItem());
            }
        }

        player.setCurrentItemOrArmor(0, prevHeldItem);
        CommonProxy.offhandItemUsed = itemStackResult;

        if (itemStackResult == itemStack && itemStackResult.stackSize == i
            && (!side.isServer()
                || itemStackResult.getMaxItemUseDuration() <= 0 && itemStackResult.getItemDamage() == j)) {
            return false;
        } else {
            BackhandUtils.setPlayerOffhandItem(player, itemStackResult);
            if (side.isServer() && (player).capabilities.isCreativeMode) {
                itemStackResult.stackSize = i;
                if (itemStackResult.isItemStackDamageable()) {
                    itemStackResult.setItemDamage(j);
                }
            }
            if (itemStackResult.stackSize <= 0) {
                BackhandUtils.setPlayerOffhandItem(player, null);
                ForgeEventFactory.onPlayerDestroyItem(player, itemStackResult);
            }
            if (side.isServer() && !player.isUsingItem()) {
                ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            }
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void sendOffSwingEvent(PlayerEvent event, ItemStack mainHandItem, ItemStack offhandItem) {
        if (!MinecraftForge.EVENT_BUS.post(new PlayerEventChild.OffhandSwingEvent(event, mainHandItem, offhandItem))) {
            ((IBackhandPlayer) event.entityPlayer).swingOffItem();
            Backhand.proxy.sendAnimationPacket(EnumAnimations.OffHandSwing, event.entityPlayer);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void sendOffSwingEventNoCheck(EntityPlayer player, ItemStack mainHandItem, ItemStack offhandItem) {
        ((IBackhandPlayer) player).swingOffItem();
        Backhand.proxy.sendAnimationPacket(EnumAnimations.OffHandSwing, player);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onOffhandSwing(PlayerEventChild.OffhandSwingEvent event) {}

    public boolean interactWithNoEvent(EntityPlayer pl, Entity p_70998_1_) {
        ItemStack itemstack = pl.getCurrentEquippedItem();
        ItemStack itemstack1 = ItemStack.copyItemStack(itemstack);

        if (!p_70998_1_.interactFirst(pl)) {
            if (itemstack != null && p_70998_1_ instanceof EntityLivingBase) {
                if (pl.capabilities.isCreativeMode) {
                    itemstack = itemstack1;
                }

                if (itemstack.interactWithEntity(pl, (EntityLivingBase) p_70998_1_)) {
                    if (itemstack.stackSize <= 0 && !pl.capabilities.isCreativeMode) {
                        pl.destroyCurrentEquippedItem();
                    }

                    return true;
                }
            }

            return false;
        } else {
            if (itemstack != null && itemstack == pl.getCurrentEquippedItem()) {
                if (itemstack.stackSize <= 0 && !pl.capabilities.isCreativeMode) {
                    pl.destroyCurrentEquippedItem();
                } else if (itemstack.stackSize < itemstack1.stackSize && pl.capabilities.isCreativeMode) {
                    itemstack.stackSize = itemstack1.stackSize;
                }
            }

            return true;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onOffhandAttack(PlayerEventChild.OffhandAttackEvent event) {
        if (event.offHand != null) {
            if (hasEntityInteraction(
                event.getPlayer().capabilities.isCreativeMode ? event.offHand.copy() : event.offHand,
                event.getTarget(),
                event.getPlayer(),
                false)) {
                event.setCanceled(true);
                if (event.offHand.stackSize <= 0 && !event.getPlayer().capabilities.isCreativeMode) {
                    ItemStack orig = event.offHand;
                    BackhandUtils.setPlayerOffhandItem(event.getPlayer(), null);
                    MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(event.getPlayer(), orig));
                }
            }
        }
    }

    /**
     * Check if a stack has a specific interaction with an entity.
     * Use a call to {@link net.minecraft.item.ItemStack#interactWithEntity(EntityPlayer, EntityLivingBase)}
     *
     * @param itemStack    to interact last with
     * @param entity       to interact first with
     * @param entityPlayer holding the stack
     * @param asTest       if data should be cloned before testing
     * @return true if a specific interaction exist (and has been done if asTest is false)
     */
    private boolean hasEntityInteraction(ItemStack itemStack, Entity entity, EntityPlayer entityPlayer,
        boolean asTest) {
        if (asTest) {
            Entity clone = EntityList.createEntityByName(EntityList.getEntityString(entity), entity.worldObj);
            if (clone != null) {
                clone.copyDataFrom(entity, true);
                return !clone.interactFirst(entityPlayer) && clone instanceof EntityLivingBase
                    && itemStack.copy()
                        .interactWithEntity(entityPlayer, (EntityLivingBase) clone);
            }
        } else if (!entity.interactFirst(entityPlayer) && entity instanceof EntityLivingBase) {
            return itemStack.interactWithEntity(entityPlayer, (EntityLivingBase) entity);
        }
        return false;
    }

    @SubscribeEvent
    public void addTracking(PlayerEvent.StartTracking event) {
        if (event.target instanceof EntityPlayer && !isFake(event.target)) {
            ((EntityPlayerMP) event.entityPlayer).playerNetServerHandler
                .sendPacket(new OffhandSyncItemPacket((EntityPlayer) event.target).generatePacket());
        }
    }
}
