package mods.battlegear2.client;

import mods.battlegear2.packet.OffhandAttackPacket;
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
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.packet.OffhandPlaceBlockPacket;
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
import xonin.backhand.client.ClientTickHandler;

public final class BattlegearClientTickHandler {
    public final Minecraft mc = Minecraft.getMinecraft();

    public float partialTick;
    public int previousBattlemode = 0;
    public static float ticksBeforeUse = 0;
    public static final BattlegearClientTickHandler INSTANCE = new BattlegearClientTickHandler();

    public BattlegearClientTickHandler() {
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
        ItemStack offhand = ((InventoryPlayerBattle) player.inventory).getOffhandItem();
        if (offhand != null) {
            if (mc.gameSettings.keyBindUseItem.getIsKeyPressed()) {
                if (ticksBeforeUse == 0) {
                    tryCheckUseItem(offhand, player);
                }
            } else {
                ticksBeforeUse = 0;
            }
        }
        if (mc.gameSettings.keyBindUseItem.getIsKeyPressed()) {
            tryAttackEntity(player);
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
    public void tryCheckUseItem(ItemStack offhandItem, EntityPlayer player){
        if (offhandItem.getItem() instanceof ItemBow && !Backhand.UseOffhandBow) {
            return;
        }

        ItemStack mainHandItem = player.getCurrentEquippedItem();
        if (mainHandItem != null && (BattlegearUtils.checkForRightClickFunction(mainHandItem) || offhandItem == null)) {
            return;
        }

    	MovingObjectPosition mop = BattlemodeHookContainerClass.getRaytraceBlock(player);
		if (mop != null && BattlemodeHookContainerClass.canBlockBeInteractedWith(player.worldObj, mop.blockX, mop.blockY, mop.blockZ)) {
			return;
		}
        if (BattlegearUtils.usagePriorAttack(offhandItem)) {
            MovingObjectPosition mouseOver = mc.objectMouseOver;
            boolean flag = true;
            if (mouseOver != null)
            {
                if (mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
                {
                    if(mc.playerController.interactWithEntitySendPacket(player, mouseOver.entityHit))
                        flag = false;
                }

                if (flag)
                {
                    offhandItem = ((InventoryPlayerBattle) player.inventory).getOffhandItem();
                    PlayerEventChild.UseOffhandItemEvent useItemEvent = new PlayerEventChild.UseOffhandItemEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, player.worldObj), offhandItem);
                    if (offhandItem != null && !MinecraftForge.EVENT_BUS.post(useItemEvent)) {
                        BattlemodeHookContainerClass.tryUseItem(player, offhandItem, Side.CLIENT);
                    }
                }

                offhandItem = BattlegearUtils.getOffhandItem(player);
                if (offhandItem != null && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                {
                    int j = mouseOver.blockX;
                    int k = mouseOver.blockY;
                    int l = mouseOver.blockZ;
                    if (!player.worldObj.getBlock(j, k, l).isAir(player.worldObj, j, k, l)) {
                        final int size = offhandItem.stackSize;
                        int i1 = mouseOver.sideHit;
                        PlayerEventChild.UseOffhandItemEvent useItemEvent = new PlayerEventChild.UseOffhandItemEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, j, k, l, i1, player.worldObj), offhandItem);
                        if (player.capabilities.allowEdit || !BattlemodeHookContainerClass.isItemBlock(offhandItem.getItem())) {
                            if (!MinecraftForge.EVENT_BUS.post(useItemEvent) && onPlayerPlaceBlock(mc.playerController, player, offhandItem, j, k, l, i1, mouseOver.hitVec)) {
                                ((IBattlePlayer) player).swingOffItem();
                            }
                        }
                        if (offhandItem.stackSize == 0)
                        {
                            BattlegearUtils.setPlayerOffhandItem(player, null);
                        }
                    }
                }
            }
            ticksBeforeUse = 4;
        }
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
        if (player.getCurrentEquippedItem() != null) {
            if ((block instanceof BlockLog && player.getCurrentEquippedItem().getItem() instanceof ItemAxe)
                    || (block instanceof BlockGrass && player.getCurrentEquippedItem().getItem() instanceof ItemSpade)) {
                return false;
            }
        }

        if (offhand.getItem().onItemUseFirst(offhand, player, worldObj, i, j, k, l, f, f1, f2)){
            return true;
        }
        if (!player.isSneaking() || ((InventoryPlayerBattle) player.inventory).getOffhandItem() == null || ((InventoryPlayerBattle) player.inventory).getOffhandItem().getItem().doesSneakBypassUse(worldObj, i, j, k, player)){
            Block b = worldObj.getBlock(i, j, k);
            if (!b.isAir(worldObj, i, j, k) && b.onBlockActivated(worldObj, i, j, k, player, l, f, f1, f2)){
                flag = true;
            }
        }
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
                    BattlemodeHookContainerClass.sendOffSwingEventNoCheck(player, offhand, player.getCurrentEquippedItem());
                }
                return flag1;
            } else {
                if (!offhand.tryPlaceItemIntoWorld(player, worldObj, i, j, k, l, f, f1, f2)){
                    return false;
                }
                if (offhand.stackSize <= 0){
                    ForgeEventFactory.onPlayerDestroyItem(player, offhand);
                }
                BattlemodeHookContainerClass.sendOffSwingEventNoCheck(player,offhand,player.getCurrentEquippedItem());
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

        if (mcInstance.thePlayer.capabilities.isCreativeMode)
        {
            if (ClientTickHandler.delay <= 0) {
                mcInstance.effectRenderer.addBlockHitEffects(i, j, k, objectMouseOver);
                mcInstance.effectRenderer.addBlockHitEffects(i, j, k, objectMouseOver);
                if (!(BattlegearUtils.usagePriorAttack(offhandItem)) && (offhandItem == null || !(offhandItem.getItem() instanceof ItemSword))) {
                    PlayerControllerMP.clickBlockCreative(mcInstance, mcInstance.playerController, i, j, k, objectMouseOver.sideHit);
                    BattlemodeHookContainerClass.sendOffSwingEventNoCheck(event.player, mainHandItem, offhandItem); // force offhand swing anyway because we broke a block
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
                mcInstance.playerController.currentBlockX = i;
                mcInstance.playerController.currentBlockY = j;
                mcInstance.playerController.currentblockZ = k;

                if (offhandItem != null)
                {
                    if (mcInstance.gameSettings.heldItemTooltips) {
                        mcInstance.gameSettings.heldItemTooltips = false;
                        BattlemodeHookContainerClass.changedHeldItemTooltips = true;
                    }

                    mcInstance.thePlayer.inventory.currentItem = InventoryPlayerBattle.OFFHAND_HOTBAR_SLOT;
                    mcInstance.playerController.currentItemHittingBlock = ((InventoryPlayerBattle)mcInstance.thePlayer.inventory).getOffhandItem();
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
                        itemstack.func_150999_a(mcInstance.theWorld, block, i, j, k, mcInstance.thePlayer);

                        if (itemstack.stackSize == 0)
                        {
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
            BattlemodeHookContainerClass.sendOffSwingEventNoCheck(event.player, mainHandItem, offhandItem); // force offhand swing anyway because we broke a block
        }
        event.player.inventory.currentItem = prevHeldItem;
        mcInstance.playerController.syncCurrentPlayItem();
    }

    public static float getPartialTick(){
        return INSTANCE.partialTick;
    }

    public static ItemStack getPreviousMainhand(EntityPlayer player){
        return player.inventory.getStackInSlot(INSTANCE.previousBattlemode);
    }

    /*public static ItemStack getPreviousOffhand(EntityPlayer player){
        return player.inventory.getStackInSlot(INSTANCE.previousBattlemode+((InventoryPlayerBattle)player.inventory).getOffsetToInactiveHand());
    }*/
}
