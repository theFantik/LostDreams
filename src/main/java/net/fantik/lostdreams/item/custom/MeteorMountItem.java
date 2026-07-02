package net.fantik.lostdreams.item.custom;

import net.fantik.lostdreams.entity.MeteorMountEntity;
import net.fantik.lostdreams.entity.ModEntities; // ЗАМЕНИ НА СВОЙ ИМПОРТ
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class MeteorMountItem extends Item {

    public MeteorMountItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Ищем блок или жидкость, куда кликнул игрок
        HitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

        if (hitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        }

        if (hitresult.getType() == HitResult.Type.BLOCK) {
            if (!level.isClientSide) {
                // Создаем и настраиваем метеор
                MeteorMountEntity meteor = ModEntities.METEOR_MOUNT.get().create(level);
                if (meteor != null) {
                    meteor.setPos(hitresult.getLocation().x, hitresult.getLocation().y, hitresult.getLocation().z);
                    meteor.setYRot(player.getYRot()); // Поворачиваем туда же, куда смотрит игрок
                    level.addFreshEntity(meteor);
                }
            }

            // Тратим предмет, если игрок не в креативе
            if (!player.isCreative()) {
                itemstack.shrink(1);
            }

            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }

        return InteractionResultHolder.pass(itemstack);
    }
}