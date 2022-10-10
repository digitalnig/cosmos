package cope.cosmos.client.features.modules.player;

import cope.cosmos.client.events.entity.hitbox.EntityHitboxSizeEvent;
import cope.cosmos.client.events.entity.player.interact.ReachEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author linustouchtips
 * @since 12/31/2021
 */
public class ReachModule extends Module {
    public static ReachModule INSTANCE;

    public ReachModule() {
        super("Reach", Category.PLAYER, "Extends your reach", () -> String.valueOf(reach.getValue().floatValue()));
        INSTANCE = this;
    }

    // **************************** general ****************************

    public static Setting<Double> reach = new Setting<>("Reach", 0.0, 0.0, 3.0, 2)
            .setDescription("Player reach extension");

    // **************************** hitbox ****************************

    public static Setting<Boolean> noHitbox = new Setting<>("NoHitbox", true)
            .setDescription("Ignores entity hitboxes");

    public static Setting<Double> hitBoxExtend = new Setting<>("HitboxExtend", 0.0, 0.0, 2.0, 2)
            .setDescription("Entity hitbox extension")
            .setVisible(() -> !noHitbox.getValue());

    public static Setting<Boolean> hitBoxPlayers = new Setting<>("PlayersOnly", false)
            .setDescription("Only ignores player hitboxes")
            .setVisible(() -> noHitbox.getValue());

    @Override
    public void onUpdate() {

        // ignore entity hitboxes
        if (noHitbox.getValue()) {

            // mining at an entity hitbox
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {

                // check we are mining at a player hitbox
                if (hitBoxPlayers.getValue() && !(mc.objectMouseOver.entityHit instanceof EntityPlayer)) {
                    return;
                }

                // raytrace to player look at
                RayTraceResult mineResult = mc.player.rayTrace(mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks());

                // check if it's valid mine
                if (mineResult != null && mineResult.typeOfHit.equals(Type.BLOCK)) {

                    // position of the mine
                    BlockPos minePos = mineResult.getBlockPos();

                    // start mining
                    if (mc.gameSettings.keyBindAttack.isKeyDown()) {

                        // break block
                        mc.playerController.onPlayerDamageBlock(minePos, EnumFacing.UP);

                        // swing player arm
                        mc.player.swingArm(SwingModule.INSTANCE.isEnabled() ? SwingModule.INSTANCE.getHand() : EnumHand.MAIN_HAND);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onHitboxSize(EntityHitboxSizeEvent event) {

        // extend hitboxes
        if (!noHitbox.getValue()) {

            // set hitbox size if we allow hitboxes
            event.setHitboxSize(hitBoxExtend.getValue().floatValue());
        }
    }

    @SubscribeEvent
    public void onReach(ReachEvent event) {

        // add reach on top of vanilla reach
        event.setCanceled(true);
        event.setReach(getReachDistance() + reach.getValue().floatValue());
    }

    /**
     * Gets the real player reach distance
     * @return The real player reach distance
     */
    public float getReachDistance() {
        return mc.playerController.isInCreativeMode() ? 5 : 4.5F;
    }
}
