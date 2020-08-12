package org.cyclops.integratedterminalscompat.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridClear;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import java.util.List;
import java.util.Map;

/**
 * Packet for setting the crafting grid recipe and filling it with items.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridSetRecipe extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private int channel;
    @CodecField
    private boolean maxTransfer;
    @CodecField
    private Map<Integer, List<ItemStack>> slottedIngredients;

    public TerminalStorageIngredientItemStackCraftingGridSetRecipe() {

    }

    public TerminalStorageIngredientItemStackCraftingGridSetRecipe(String tabId, int channel, boolean maxTransfer,
                                                                   Map<Integer, List<ItemStack>> slottedIngredients) {
        this.tabId = tabId;
        this.channel = channel;
        this.maxTransfer = maxTransfer;
        this.slottedIngredients = slottedIngredients;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(World world, PlayerEntity player) {

    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {
        if(player.openContainer instanceof ContainerTerminalStorage) {
            ContainerTerminalStorage container = ((ContainerTerminalStorage) player.openContainer);
            ITerminalStorageTabCommon tabCommon = container.getTabCommon(tabId);
            if (tabCommon instanceof TerminalStorageTabIngredientComponentItemStackCraftingCommon) {
                TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServerCrafting =
                        (TerminalStorageTabIngredientComponentServer<ItemStack, Integer>) container.getTabServer(tabId);
                TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommonCrafting =
                        (TerminalStorageTabIngredientComponentItemStackCraftingCommon) tabCommon;
                PartTypeTerminalStorage.State partState = container.getPartState().get();
                int slotOffset = tabCommonCrafting.getSlotCrafting().slotNumber;

                // Clear current grid into storage
                TerminalStorageIngredientItemStackCraftingGridClear.clearGrid(tabCommonCrafting, tabServerCrafting,
                        channel, false, player);

                // Try filling the grid with the given recipe
                IIngredientComponentStorage<ItemStack, Integer> storage = tabServerCrafting.getIngredientNetwork()
                        .getChannel(channel);
                IIngredientMatcher<ItemStack, Integer> matcher = IngredientComponent.ITEMSTACK.getMatcher();
                for (Map.Entry<Integer, List<ItemStack>> entry : this.slottedIngredients.entrySet()) {
                    int slotId = entry.getKey() + slotOffset;
                    Slot slot = container.getSlot(slotId);

                    if (!slot.getHasStack()) {
                        ItemStack extracted = ItemStack.EMPTY;
                        for (ItemStack itemStack : entry.getValue()) {
                            extracted = storage.extract(itemStack, ItemMatch.EXACT, false);
                            if (!extracted.isEmpty()) {
                                break;
                            }
                        }
                        if (!extracted.isEmpty()) {
                            slot.putStack(extracted);
                        }
                    }
                }

                // Notify the client
                // TODO?
            }
        }
    }

}