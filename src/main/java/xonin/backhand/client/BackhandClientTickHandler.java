package xonin.backhand.client;

import xonin.backhand.packet.OffhandAttackPacket;
import xonin.backhand.packet.OffhandToServerPacket;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.ResourceLocation;
import xonin.backhand.Backhand;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xonin.backhand.HookContainerClass;
import xonin.backhand.api.PlayerEventChild;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.api.core.InventoryPlayerBackhand;
import xonin.backhand.packet.OffhandPlaceBlockPacket;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO;
import xonin.backhand.CommonProxy;

public final class BackhandClientTickHandler {
    public final Minecraft mc = Minecraft.getMinecraft();
    public static float ticksBeforeUse = 0;
    public static boolean prevRightClickHeld = false;
    public static int attackDelay = 0;

    public BackhandClientTickHandler() {
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(event.player == mc.thePlayer) {
            if (event.phase == TickEvent.Phase.START) {
                if (ticksBeforeUse > 0)
                    ticksBeforeUse--;
                tickStart(mc.thePlayer);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void tickStart(EntityPlayer player) {
        ItemStack mainhand = player.getCurrentEquippedItem();
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        boolean mainhandUse = BackhandUtils.checkForRightClickFunction(mainhand);
        boolean offhandUse = BackhandUtils.checkForRightClickFunction(offhand);
        if (attackDelay > 0) {
            attackDelay--;
        }

        boolean usedItem = false;
        if (offhand != null) {
            if (mc.gameSettings.keyBindUseItem.getIsKeyPressed()) {
                if (ticksBeforeUse == 0) {
                    usedItem = tryCheckUseItem(offhand, player);
                }
            } else {
                ticksBeforeUse = 0;
            }
        }
        if (mc.gameSettings.keyBindUseItem.getIsKeyPressed() && attackDelay == 0 && !usedItem) {
            if (!prevRightClickHeld && player.getItemInUse() == null && !mainhandUse && !offhandUse) {
                tryAttackEntity(player);
            }
            prevRightClickHeld = true;
        } else {
            prevRightClickHeld = false;
        }
        if (player.getItemInUse() == null) {
            CommonProxy.offhandItemUsed = null;
        }
    }

    public void tryAttackEntity(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            Entity target = mc.objectMouseOver.entityHit;
            ((EntityClientPlayerMP) player).sendQueue.addToSendQueue(
                    new OffhandAttackPacket(player,target).generatePacket()
            );
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean tryCheckUseItem(ItemStack offhandItem, EntityPlayer player){
        MovingObjectPosition mouseOver = mc.objectMouseOver;

        if (offhandItem.getItem() instanceof ItemBow && !Backhand.UseOffhandBow) {
            return false;
        }

        ItemStack mainHandItem = player.getCurrentEquippedItem();
        if (mainHandItem != null && (BackhandUtils.checkForRightClickFunction(mainHandItem)
                    || HookContainerClass.isItemBlock(mainHandItem.getItem()) || player.getItemInUse() == mainHandItem)) {
            ticksBeforeUse = 10;
            return false;
        }

        if (mouseOver != null && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            if (BackhandUtils.blockHasUse(player.worldObj.getBlock(mouseOver.blockX, mouseOver.blockY,mouseOver.blockZ))
                && !BackhandUtils.getOffhandItem(player).getItem().doesSneakBypassUse(player.worldObj, mouseOver.blockX, mouseOver.blockY,mouseOver.blockZ, player)
                && !(offhandItem.getItem() instanceof ItemBlock)) {
                ticksBeforeUse = 4;
                return false;
            }
        }

        boolean interacted = false;
        if (BackhandUtils.usagePriorAttack(offhandItem)) {
            boolean flag = true;
            if (mouseOver != null)
            {
                if (mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
                {
                    if(mc.playerController.interactWithEntitySendPacket(player, mouseOver.entityHit)) {
                        flag = false;
                        interacted = true;
                    }
                }

                if (flag)
                {
                    offhandItem = BackhandUtils.getOffhandItem(player);
                    PlayerEventChild.UseOffhandItemEvent useItemEvent = new PlayerEventChild.UseOffhandItemEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, player.worldObj), offhandItem);
                    if (offhandItem != null && !MinecraftForge.EVENT_BUS.post(useItemEvent)) {
                        interacted = HookContainerClass.tryUseItem(player, offhandItem, Side.CLIENT);
                    }
                }

                offhandItem = BackhandUtils.getOffhandItem(player);
                if (offhandItem != null && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                {
                    int j = mouseOver.blockX;
                    int k = mouseOver.blockY;
                    int l = mouseOver.blockZ;
                    if (!player.worldObj.getBlock(j, k, l).isAir(player.worldObj, j, k, l)) {
                        final int size = offhandItem.stackSize;
                        int i1 = mouseOver.sideHit;
                        PlayerEventChild.UseOffhandItemEvent useItemEvent = new PlayerEventChild.UseOffhandItemEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, j, k, l, i1, player.worldObj), offhandItem);
                        if (player.capabilities.allowEdit || !HookContainerClass.isItemBlock(offhandItem.getItem())) {
                            if (!MinecraftForge.EVENT_BUS.post(useItemEvent) && onPlayerPlaceBlock(mc.playerController, player, offhandItem, j, k, l, i1, mouseOver.hitVec)) {
                                ((IBackhandPlayer) player).swingOffItem();
                                interacted = true;
                            }
                        }
                        if (offhandItem.stackSize == 0)
                        {
                            BackhandUtils.setPlayerOffhandItem(player, null);
                        }
                    }
                }
            }
            ticksBeforeUse = 4;
        }

        return interacted;
    }

    private boolean onPlayerPlaceBlock(PlayerControllerMP controller, EntityPlayer player, ItemStack offhand, int i, int j, int k, int l, Vec3 hitVec) {
        float f = (float)hitVec.xCoord - i;
        float f1 = (float)hitVec.yCoord - j;
        float f2 = (float)hitVec.zCoord - k;
        boolean flag = false;
        int i1;
        final World worldObj = player.worldObj;

        Minecraft mc = Minecraft.getMinecraft();
        MovingObjectPosition objectMouseOver = mc.objectMouseOver;
        Block block = mc.theWorld.getBlock(objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ);

        Block b = worldObj.getBlock(i, j, k);
        boolean blockActivated = b.onBlockActivated(worldObj, i, j, k, player, l, f, f1, f2);
        boolean prevSneaking = player.isSneaking();
        player.setSneaking(true);
        boolean blockSneakActivated = b.onBlockActivated(worldObj, i, j, k, player, l, f, f1, f2);
        player.setSneaking(prevSneaking);

        if (blockActivated && !(player.isSneaking() && blockSneakActivated)) {
            return false;
        }

        if (player.getCurrentEquippedItem() != null) {
            if ((block instanceof BlockLog && player.getCurrentEquippedItem().getItem() instanceof ItemAxe)
                    || (block instanceof BlockGrass && player.getCurrentEquippedItem().getItem() instanceof ItemSpade)) {
                return false;
            }
        }

        if (offhand.getItem().onItemUseFirst(offhand, player, worldObj, i, j, k, l, f, f1, f2)){
            return true;
        }
        /*if (!player.isSneaking() || BattlegearUtils.getOffhandItem(player) == null || BattlegearUtils.getOffhandItem(player).getItem().doesSneakBypassUse(worldObj, i, j, k, player)){
            if (!b.isAir(worldObj, i, j, k)){
                flag = true;
            }
        }*/
        if (!flag && offhand.getItem() instanceof ItemBlock){
            ItemBlock itemblock = (ItemBlock)offhand.getItem();
            if (!itemblock.func_150936_a(worldObj, i, j, k, l, player, offhand)){
                return false;
            }
        }
        Backhand.packetHandler.sendPacketToServer(new OffhandPlaceBlockPacket(i, j, k, l, offhand, f, f1, f2).generatePacket());
        if (flag) {
            return true;
        } else {
            if (controller.isInCreativeMode()){
                i1 = offhand.getItemDamage();
                int j1 = offhand.stackSize;
                boolean flag1 = offhand.tryPlaceItemIntoWorld(player, worldObj, i, j, k, l, f, f1, f2);
                offhand.setItemDamage(i1);
                offhand.stackSize = j1;
                if (flag1) {
                    HookContainerClass.sendOffSwingEventNoCheck(player, offhand, player.getCurrentEquippedItem());
                }
                return flag1;
            } else {
                if (!offhand.tryPlaceItemIntoWorld(player, worldObj, i, j, k, l, f, f1, f2)){
                    return false;
                }
                if (offhand.stackSize <= 0){
                    ForgeEventFactory.onPlayerDestroyItem(player, offhand);
                }
                HookContainerClass.sendOffSwingEventNoCheck(player,offhand,player.getCurrentEquippedItem());
                return true;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void tryBreakBlockOffhand(MovingObjectPosition objectMouseOver, ItemStack offhandItem, ItemStack mainHandItem, TickEvent.PlayerTickEvent event) {
        Minecraft mcInstance = Minecraft.getMinecraft();
        int i = objectMouseOver.blockX;
        int j = objectMouseOver.blockY;
        int k = objectMouseOver.blockZ;
        int prevHeldItem = event.player.inventory.currentItem;
        boolean broken = false;

        if (mcInstance.thePlayer.capabilities.isCreativeMode)
        {
            if (ClientTickHandler.delay <= 0) {
                mcInstance.effectRenderer.addBlockHitEffects(i, j, k, objectMouseOver);
                mcInstance.effectRenderer.addBlockHitEffects(i, j, k, objectMouseOver);
                if (!(BackhandUtils.usagePriorAttack(offhandItem)) && (offhandItem == null || !(offhandItem.getItem() instanceof ItemSword))) {
                    PlayerControllerMP.clickBlockCreative(mcInstance, mcInstance.playerController, i, j, k, objectMouseOver.sideHit);
                    HookContainerClass.sendOffSwingEventNoCheck(event.player, mainHandItem, offhandItem); // force offhand swing anyway because we broke a block
                    mcInstance.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(2, i, j, k, objectMouseOver.sideHit));
                }
                ClientTickHandler.delay = 20;
            }
            return;
        }
        if (mcInstance.theWorld.getBlock(i, j, k).getMaterial() != Material.air)
        {
            if (mcInstance.playerController.blockHitDelay > 0)
            {
                --mcInstance.playerController.blockHitDelay;
            }
            else
            {
                mcInstance.playerController.isHittingBlock = true;

                if (mcInstance.playerController.currentBlockX != i || mcInstance.playerController.currentBlockY != j || mcInstance.playerController.currentblockZ != k) {
                    mcInstance.playerController.curBlockDamageMP = 0f;
                }

                mcInstance.playerController.currentBlockX = i;
                mcInstance.playerController.currentBlockY = j;
                mcInstance.playerController.currentblockZ = k;

                if (offhandItem != null)
                {
                    if (mcInstance.gameSettings.heldItemTooltips) {
                        mcInstance.gameSettings.heldItemTooltips = false;
                        HookContainerClass.changedHeldItemTooltips = true;
                    }

                    mcInstance.thePlayer.inventory.currentItem = InventoryPlayerBackhand.OFFHAND_HOTBAR_SLOT;
                    mcInstance.playerController.currentItemHittingBlock = BackhandUtils.getOffhandItem(mcInstance.thePlayer);
                    mcInstance.playerController.syncCurrentPlayItem();
                }


                Block block = mcInstance.theWorld.getBlock(i, j, k);
                if (block.getMaterial() == Material.air)
                {
                    mcInstance.playerController.isHittingBlock = false;
                    return;
                }
                MysteriumPatchesFixesO.countToCancel = 5;
                mcInstance.playerController.curBlockDamageMP += block.getPlayerRelativeBlockHardness(mcInstance.thePlayer, mcInstance.thePlayer.worldObj, i, j, k);

                if (mcInstance.playerController.stepSoundTickCounter % 4.0F == 0.0F)
                {
                    mcInstance.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getStepResourcePath()), (block.stepSound.getVolume() + 1.0F) / 8.0F, block.stepSound.getPitch() * 0.5F, (float)i + 0.5F, (float)j + 0.5F, (float)k + 0.5F));
                }

                ++mcInstance.playerController.stepSoundTickCounter;

                if (mcInstance.playerController.curBlockDamageMP >= 1.0F)
                {

                    ItemStack itemstack = mcInstance.thePlayer.getCurrentEquippedItem();

                    if (itemstack != null)
                    {
                        int prevDamage = itemstack.getItemDamage();
                        int damage = itemstack.getMaxDamage() - itemstack.getItemDamage();
                        itemstack.func_150999_a(mcInstance.theWorld, block, i, j, k, mcInstance.thePlayer);
                        if (itemstack.stackSize == 0 || (damage <= 0 && prevDamage < itemstack.getItemDamage()))
                        {
                            broken = true;
                            mcInstance.thePlayer.destroyCurrentEquippedItem();
                        }
                    }
                    mcInstance.playerController.isHittingBlock = false;
                    mcInstance.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(2, i, j, k, objectMouseOver.sideHit));
                    mcInstance.playerController.onPlayerDestroyBlock(i, j, k, objectMouseOver.sideHit);
                    mcInstance.playerController.curBlockDamageMP = 0.0F;
                    mcInstance.playerController.stepSoundTickCounter = 0.0F;
                    mcInstance.playerController.blockHitDelay = 5;
                }
                mcInstance.theWorld.destroyBlockInWorldPartially(mcInstance.thePlayer.getEntityId(), mcInstance.playerController.currentBlockX, mcInstance.playerController.currentBlockY, mcInstance.playerController.currentblockZ, (int)(mcInstance.playerController.curBlockDamageMP * 10.0F) - 1);
            }

            if (mcInstance.thePlayer.isCurrentToolAdventureModeExempt(i, j, k))
            {
                mcInstance.effectRenderer.addBlockHitEffects(i, j, k, objectMouseOver);
            }
            HookContainerClass.sendOffSwingEventNoCheck(event.player, mainHandItem, offhandItem); // force offhand swing anyway because we broke a block
        }
        event.player.inventory.currentItem = prevHeldItem;
        mcInstance.playerController.syncCurrentPlayItem();

        if (broken) {
            BackhandUtils.setPlayerOffhandItem(event.player,null);
            ((EntityClientPlayerMP)event.player).sendQueue.addToSendQueue(
                    new OffhandToServerPacket(null, event.player).generatePacket()
            );
        }
    }
}
