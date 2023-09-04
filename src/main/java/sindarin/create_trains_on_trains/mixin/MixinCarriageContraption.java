package sindarin.create_trains_on_trains.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CarriageContraption.class, remap = false)
public abstract class MixinCarriageContraption extends Contraption {
    @Shadow private Direction assemblyDirection;
    
    @Inject(method = "capture", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/simibubi/create/content/trains/bogey/AbstractBogeyBlock;captureBlockEntityForTrain()Z"), cancellable = true)
    private void addNewBogey(Level world, BlockPos pos, CallbackInfoReturnable<Pair<StructureTemplate.StructureBlockInfo, BlockEntity>> cir){
        BlockState blockState = world.getBlockState(pos);
        if (pos == this.anchor) return; // This is the anchor; no intervention needed. Return from the mixin and let the original code do its job.
        
        // Check if the bogey is in the same direction as we expect true carriage bogeys to be
        Vec3i bogeyDir = this.anchor.subtract(pos);
        boolean isInLine = true;
        
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            boolean isAssemblyAxis = axis == assemblyDirection.getAxis();
            boolean bogeyDirOnThisAxis = bogeyDir.get(axis) != 0;
            if (bogeyDirOnThisAxis != isAssemblyAxis) {
                isInLine = false;
                break;
            }
        }
        
        // If the bogey is not in line, treat it like a normal structure block and return the original method
        if (!isInLine) {
            cir.setReturnValue(Pair.of(new StructureTemplate.StructureBlockInfo(pos, blockState, getBlockEntityNBT(world, pos)),
                    world.getBlockEntity(pos)));
        }
    }
}
