package org.cyclops.integratedterminalscompat.modcompat.jei.terminalstorage;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminalscompat.modcompat.jei.JEIIntegratedTerminalsConfig;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * This handler allows JEI to recognise the terminal storage slot contents.
 * @author rubensworks
 */
public class TerminalStorageGuiHandler implements IGuiContainerHandler<ContainerScreenTerminalStorage<?, ?>> {
    @Nullable
    @Override
    public Object getIngredientUnderMouse(ContainerScreenTerminalStorage<?, ?> guiContainer, double mouseX, double mouseY) {
        int slotIndex = guiContainer.getStorageSlotIndexAtPosition(mouseX, mouseY);
        if (slotIndex >= 0) {
            Optional<ITerminalStorageTabClient<?>> tabOptional = guiContainer.getSelectedClientTab();
            if (tabOptional.isPresent()) {
                ITerminalStorageTabClient<?> tab = tabOptional.get();
                if (tab instanceof TerminalStorageTabIngredientComponentClient) {
                    Object instance = ((TerminalStorageTabIngredientComponentClient) tab).getSlotInstance(
                            guiContainer.getMenu().getSelectedChannel(), slotIndex).orElse(null);
                    try {
                        JEIIntegratedTerminalsConfig.jeiRuntime.getIngredientManager().getIngredientType(instance);
                        return instance;
                    } catch (IllegalArgumentException | NullPointerException e) {
                        // Ignore ingredient types unsupported by JEI
                    }
                }
            }
        }
        return null;
    }
}
