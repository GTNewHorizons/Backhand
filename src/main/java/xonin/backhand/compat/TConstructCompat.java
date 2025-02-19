package xonin.backhand.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.tools.HarvestTool;
import tconstruct.library.weaponry.IWindup;
import tconstruct.tools.TinkerToolEvents;
import tconstruct.tools.TinkerTools;
import tconstruct.weaponry.client.CrosshairHandler;
import tconstruct.world.TinkerWorldEvents;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.utils.Mods;

@SuppressWarnings("unused")
@EventBusSubscriber
public class TConstructCompat {

    private static CrosshairHandler crosshairHandler;
    private static MethodHandle onHurt, onAttack;

    @EventBusSubscriber.Condition
    public static boolean register() {
        if (Mods.TINKERS_CONSTRUCT.isLoaded()) {
            try {
                // Gotta hide those NEW instructions from the JVM
                if (FMLCommonHandler.instance()
                    .getSide() == Side.CLIENT) {
                    crosshairHandler = CrosshairHandler.class.getConstructor()
                        .newInstance();
                }
                // These need to be MethodHandles since the compiler complains about Mobs-Info not being present
                // otherwise
                onHurt = MethodHandles.publicLookup()
                    .findVirtual(
                        TinkerWorldEvents.class,
                        "onHurt",
                        MethodType.methodType(void.class, LivingHurtEvent.class))
                    .bindTo(
                        TinkerWorldEvents.class.getConstructor()
                            .newInstance());
                onAttack = MethodHandles.publicLookup()
                    .findVirtual(
                        TinkerToolEvents.class,
                        "onAttack",
                        MethodType.methodType(void.class, LivingAttackEvent.class))
                    .bindTo(
                        TinkerToolEvents.class.getConstructor()
                            .newInstance());
            } catch (Exception ignored) {}
            BackhandUtils.addDeprioritizedMainhandItem(HarvestTool.class);
            return true;
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer player) || event.ammount == 0) return;
        ItemStack offhandItem = BackhandUtils.getOffhandItem(player);
        if (offhandItem == null) return;
        if (offhandItem.getItem() == TinkerTools.cutlass || offhandItem.getItem() == TinkerTools.battlesign) {
            BackhandUtils.useOffhandItem(player, () -> {
                try {
                    onHurt.invokeWithArguments(event);
                } catch (Throwable ignored) {}
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer player)) return;
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (offhand == null) return;
        if (offhand.getItem() == TinkerTools.battlesign) {
            BackhandUtils.useOffhandItem(player, () -> {
                try {
                    onAttack.invokeWithArguments(event);
                } catch (Throwable ignored) {}
            });
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return;
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (offhand != null && offhand.getItem() instanceof IWindup) {
            BackhandUtils.useOffhandItem(player, () -> crosshairHandler.onRenderOverlay(event));
        }
    }
}
