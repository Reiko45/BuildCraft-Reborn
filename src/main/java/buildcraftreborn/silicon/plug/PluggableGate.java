/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.LogicalSide;
import net.minecraft.core.HolderLookup;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import buildcraftreborn.api.core.render.EnumRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;


import buildcraftreborn.api.transport.IWireEmitter;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

import buildcraftreborn.lib.expression.DefaultContexts;
import buildcraftreborn.lib.expression.FunctionContext;
import buildcraftreborn.lib.expression.info.ContextInfo;
import buildcraftreborn.lib.expression.info.VariableInfo.CacheType;
import buildcraftreborn.lib.expression.info.VariableInfo.VariableInfoBoolean;
import buildcraftreborn.lib.expression.info.VariableInfo.VariableInfoBoolean.BooleanPossibilities;
import buildcraftreborn.lib.expression.info.VariableInfo.VariableInfoObject;
import buildcraftreborn.lib.expression.node.value.NodeVariableBoolean;
import buildcraftreborn.lib.expression.node.value.NodeVariableObject;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.data.ModelVariableData;
import buildcraftreborn.lib.net.IPayloadWriter;
import buildcraftreborn.lib.net.PacketBufferBC;

import buildcraftreborn.silicon.BCSiliconGuis;
import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.client.model.key.KeyPlugGate;
import buildcraftreborn.silicon.gate.EnumGateLogic;
import buildcraftreborn.silicon.gate.EnumGateMaterial;
import buildcraftreborn.silicon.gate.EnumGateModifier;
import buildcraftreborn.silicon.gate.GateLogic;
import buildcraftreborn.silicon.gate.GateVariant;
import buildcraftreborn.silicon.item.ItemGateCopier;
import buildcraftreborn.transport.pipe.PluggableHolder;

public class PluggableGate extends PipePluggable implements IWireEmitter {
    public static final FunctionContext MODEL_FUNC_CTX_STATIC, MODEL_FUNC_CTX_DYNAMIC;
    private static final NodeVariableObject<String> MODEL_MATERIAL;
    private static final NodeVariableObject<String> MODEL_MODIFIER;
    private static final NodeVariableObject<String> MODEL_LOGIC;
    private static final NodeVariableObject<Direction> MODEL_SIDE;
    private static final NodeVariableBoolean MODEL_IS_ON;
    public static final ContextInfo MODEL_VAR_INFO;

    private static final AABB[] BOXES = new AABB[6];

    private static final ResourceLocation ADVANCEMENT_PLACE_GATE = BuildCraftReborn.id("pipe_logic");

    private static final ResourceLocation ADVANCEMENT_PLACE_ADV_GATE = BuildCraftReborn.id("extended_logic");

    public final GateLogic logic;

    public final ModelVariableData clientModelData = new ModelVariableData();

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 5 / 16.0;
        double max = 11 / 16.0;

        BOXES[Direction.DOWN.get3DDataValue()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.get3DDataValue()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.get3DDataValue()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.get3DDataValue()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.get3DDataValue()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.get3DDataValue()] = new AABB(ul, min, min, uu, max, max);

        MODEL_FUNC_CTX_STATIC = DefaultContexts.createWithAll();
        MODEL_MATERIAL = MODEL_FUNC_CTX_STATIC.putVariableString("material");
        MODEL_MODIFIER = MODEL_FUNC_CTX_STATIC.putVariableString("modifier");
        MODEL_LOGIC = MODEL_FUNC_CTX_STATIC.putVariableString("logic");
        MODEL_SIDE = MODEL_FUNC_CTX_STATIC.putVariableObject("side", Direction.class);

        MODEL_FUNC_CTX_DYNAMIC = new FunctionContext(MODEL_FUNC_CTX_STATIC);
        MODEL_IS_ON = MODEL_FUNC_CTX_DYNAMIC.putVariableBoolean("on");

        MODEL_VAR_INFO = new ContextInfo(MODEL_FUNC_CTX_DYNAMIC);
        VariableInfoObject<String> infoMaterial = MODEL_VAR_INFO.createInfoObject(MODEL_MATERIAL);
        infoMaterial.cacheType = CacheType.ALWAYS;
        infoMaterial.setIsComplete = true;
        infoMaterial.possibleValues
            .addAll(Arrays.stream(EnumGateMaterial.VALUES).map(m -> m.tag).collect(Collectors.toList()));

        VariableInfoObject<String> infoModifier = MODEL_VAR_INFO.createInfoObject(MODEL_MODIFIER);
        infoModifier.cacheType = CacheType.ALWAYS;
        infoModifier.setIsComplete = true;
        infoModifier.possibleValues
            .addAll(Arrays.stream(EnumGateModifier.VALUES).map(m -> m.tag).collect(Collectors.toList()));

        VariableInfoObject<String> infoLogic = MODEL_VAR_INFO.createInfoObject(MODEL_LOGIC);
        infoLogic.cacheType = CacheType.ALWAYS;
        infoLogic.setIsComplete = true;
        infoLogic.possibleValues
            .addAll(Arrays.stream(EnumGateLogic.VALUES).map(m -> m.tag).collect(Collectors.toList()));

        VariableInfoObject<Direction> infoSide = MODEL_VAR_INFO.createInfoObject(MODEL_SIDE);
        infoSide.cacheType = CacheType.ALWAYS;
        infoSide.setIsComplete = true;
        Collections.addAll(infoSide.possibleValues, Direction.values());

        VariableInfoBoolean infoIsOn = MODEL_VAR_INFO.createInfoBoolean(MODEL_IS_ON);
        infoIsOn.cacheType = CacheType.ALWAYS;
        infoIsOn.setIsComplete = true;
        infoIsOn.possibleValues = BooleanPossibilities.FALSE_TRUE;
    }

    // Manual constructor (called by the specific item pluggable gate code)

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, GateVariant variant) {
        super(def, holder, side);
        logic = new GateLogic(this, variant);
    }

    // Saving + Loading

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt,
        HolderLookup.Provider registries) {
        super(def, holder, side);
        logic = new GateLogic(this, nbt.getCompound("data"), registries);
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        nbt.put("data", logic.writeToNbt(registries));
        return nbt;
    }

    // Networking

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, PacketBufferBC buffer) {
        super(def, holder, side);
        logic = new GateLogic(this, buffer);
    }

    @Override
    public void writeCreationPayload(PacketBufferBC buffer) {
        logic.writeCreationToBuf(buffer);
    }

    public void sendMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendMessage(to, (buffer) -> {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(PluggableHolder.ID_UPDATE_PLUG);
            writer.write(buffer);
        });
    }

    public void sendGuiMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendGuiMessage(to, (buffer) -> {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(PluggableHolder.ID_UPDATE_PLUG);
            writer.write(buffer);
        });
    }

    @Override
    public void writePayload(PacketBufferBC buffer, LogicalSide side) {
        throw new Error("All messages must have an ID, and we can't just write a payload directly!");
    }

    @Override
    public void readPayload(PacketBufferBC b, LogicalSide side, @Nullable IPayloadContext ctx) throws IOException {
        logic.readPayload(b, side, ctx);
    }

    // PipePluggable

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.get3DDataValue()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return BCSiliconItems.PLUG_GATE.get().getStack(logic.variant);
    }

    @Override
    public PluggableModelKey getModelRenderKey(EnumRenderLayer layer) {
        if (layer == EnumRenderLayer.CUTOUT) {
            return new KeyPlugGate(side, logic.variant);
        }
        return null;
    }

    @Override
    public void onPlacedBy(Player player) {
        super.onPlacedBy(player);
        if (!holder.getPipeWorld().isClientSide) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_GATE);
            if (logic.variant.numActionArgs >= 1) {
                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_ADV_GATE);
            }
        }
    }

    @Override
    public boolean onPluggableActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ) {
        if (!player.level().isClientSide) {
            if (interactWithCopier(player, player.getMainHandItem())) {
                return true;
            }
            if (interactWithCopier(player, player.getOffhandItem())) {
                return true;
            }

            BlockPos pos = holder.getPipePos();
            BCSiliconGuis.openGate(player, pos, side, this);
        }
        return true;
    }

    private boolean interactWithCopier(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemGateCopier)) {
            return false;
        }

        CompoundTag stored = ItemGateCopier.getCopiedGateData(stack);

        if (stored != null) {

            logic.readConfigData(stored, player.level().registryAccess());
            holder.scheduleNetworkUpdate(PipeMessageReceiver.PLUGGABLES[side.ordinal()]);

            player.displayClientMessage(Component.translatable("chat.gateCopier.gatePasted"), true);

        } else {
            stored = logic.writeToNbt(player.level().registryAccess());
            stored.remove("wireBroadcasts");

            // Just the variant and connections: nothing is set
            if (stored.size() <= 2 && stored.getShort("connections") == 0) {
                player.displayClientMessage(Component.translatable("chat.gateCopier.noInformation"), true);
                return false;
            }

            ItemGateCopier.setCopiedGateData(stack, stored);
            player.displayClientMessage(Component.translatable("chat.gateCopier.gateCopied"), true);
        }

        return true;
    }

    @Override
    public boolean isEmitting(DyeColor colour) {
        return logic.isEmitting(colour);
    }

    @Override
    public void emitWire(DyeColor colour) {
        logic.emitWire(colour);
    }

    // Gate methods

    @Override
    public void onTick() {
        logic.onTick();
        if (holder.getPipeWorld().isClientSide) {
            clientModelData.tick();
        }
    }

    @Override
    public boolean canConnectToRedstone(@Nullable Direction to) {
        return true;
    }

    // Model

    public static void setClientModelVariables(Direction side, GateVariant variant) {
        MODEL_SIDE.value = side;
        MODEL_MATERIAL.value = variant.material.tag;
        MODEL_MODIFIER.value = variant.modifier.tag;
        MODEL_LOGIC.value = variant.logic.tag;
        MODEL_IS_ON.value = false;// Used by the item
    }

    public void setClientModelVariables() {
        setClientModelVariables(side, logic.variant);
        MODEL_IS_ON.value = logic.isOn;
    }
}
