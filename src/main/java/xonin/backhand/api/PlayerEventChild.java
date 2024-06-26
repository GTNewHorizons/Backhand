package xonin.backhand.api;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public abstract class PlayerEventChild extends PlayerEvent{

    /**
     * The event that this event is a child of
     */
	public final PlayerEvent parent;

	public PlayerEventChild(PlayerEvent parent) {
		super(parent.entityPlayer);
		this.parent = parent;
	}

    public void setCancelParentEvent(boolean cancel) {
        parent.setCanceled(cancel);
    }

    @Override
    public void setCanceled(boolean cancel) {
        super.setCanceled(cancel);
        parent.setCanceled(cancel);
    }

    @Override
    public void setResult(Result value) {
        super.setResult(value);
        parent.setResult(value);
    }

    public EntityPlayer getPlayer(){
        return parent.entityPlayer;
    }

    /**
     * Called when a player right clicks in battlemode
     * The parent event can be either {@link PlayerInteractEvent} or {@link EntityInteractEvent} if the OffhandAttackEvent allowed swinging
     * Both {@link ItemStack} can be null
     * If cancelled, no offhand swinging will be performed
     */
    @Cancelable
    public static class OffhandSwingEvent extends PlayerEventChild {
        public final ItemStack mainHand;
        public final ItemStack offHand;
        @Deprecated
        public OffhandSwingEvent(PlayerEvent parent, ItemStack mainHand, ItemStack offHand){
            this(parent, offHand);
        }

        public OffhandSwingEvent(PlayerEvent parent, ItemStack offHand){
            super(parent);
            this.mainHand = parent.entityPlayer.getCurrentEquippedItem();
            this.offHand = offHand;
        }

        public boolean onEntity(){
            return parent instanceof EntityInteractEvent;
        }

        public boolean onBlock(){
            return parent instanceof PlayerInteractEvent;
        }
    }

    /**
     * Called when a player right clicks an entity in battlemode
     * Both {@link ItemStack} can be null
     * Cancelling will prevent any further processing and prevails over the boolean fields
     */
    @Cancelable
    public static class OffhandAttackEvent extends PlayerEventChild {

        /**
         * If we should call the OffhandSwingEvent and perform swinging animation
         */
        public boolean swingOffhand = true;
        /**
         * If we should perform an attack on the entity with the offhand item
         * Note: Will post AttackEntityEvent and Item#onLeftClickEntity(ItemStack, EntityPlayer, Entity)
         * with InventoryPlayer#currentItem offset to the offhand.
         */
        public boolean shouldAttack = true;
        /**
         * If we should Prevent PlayerInteractEvent.Action.RIGHT_CLICK_AIR and
         * ItemStack#useItemRightClick(World, EntityPlayer)
         * from being called for the item in main hand.
         */
        public boolean cancelParent = true;
        /**
         * The base entity interaction event
         * This event has already been posted in EventBus, handled by all potential listeners and was not cancelled.
         * Changing its state will have no effect.
         */
        public final EntityInteractEvent event;
        /**
         * Content of the main hand slot
         */
        public final ItemStack mainHand;
        /**
         * Content of the off hand slot
         */
        public final ItemStack offHand;

        @Deprecated
        public OffhandAttackEvent(EntityInteractEvent parent, ItemStack mainHand, ItemStack offHand) {
            this(parent, offHand);
        }

        public OffhandAttackEvent(EntityInteractEvent parent, ItemStack offHand) {
            super(parent);
            this.event = parent;
            this.mainHand = parent.entityPlayer.getCurrentEquippedItem();
            this.offHand = offHand;
        }

        public Entity getTarget() {
            return ((EntityInteractEvent)parent).target;
        }
    }

    /**
     * This event replicates the event usage of {@link PlayerInteractEvent} for the item in left hand on right click,
     * allowing support for other mods that use such event to customize item usage
     * Item#onItemUseFirst, Item#onItemRightClick and Item#onItemUse will then get called the same way as with the item in the player right hand for PlayerInteractEvent
     */
    @Cancelable
    public static class UseOffhandItemEvent extends PlayerEventChild{
        /**
         * If we should call the OffhandSwingEvent and perform swinging animation
         */
        public boolean swingOffhand;
        /**
         * The {@link ItemStack} held in left hand
         */
        public final ItemStack offhand;
        /**
         * The equivalent {@link PlayerInteractEvent} that would have been triggered if the offhand item was held in right hand and right click was pressed
         */
        public final PlayerInteractEvent event;
        public UseOffhandItemEvent(PlayerInteractEvent event, ItemStack offhand){
            super(event);
            this.event = event;
            this.offhand = offhand;
            this.swingOffhand = onBlock();
        }

        public boolean onBlock(){
            return event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
        }
    }
}
