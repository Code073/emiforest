package com.emiforest.mixin;

import com.emiforest.forest.ForestManager;
import dev.emi.emi.screen.BoMScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.List;

@Mixin(BoMScreen.class)
public abstract class BoMScreenMixin {

    // ---------- Configuración del panel ----------
    private static final int PANEL_X = 10;
    private static final int PANEL_Y = 10;
    private static final int PANEL_WIDTH = 160;
    private static final int ROW_HEIGHT = 12;
    private static final int MAX_VISIBLE_ROWS = 8;
    private static final int HEADER_HEIGHT = 14;
    private static final int COLLAPSED_HEIGHT = HEADER_HEIGHT + 4;
    private static final int BUTTON_HEIGHT = 14;  // altura del botón "Eliminar todos"

    // ---------- Estado interno ----------
    @Unique
    private boolean isCollapsed = false;
    @Unique
    private int scrollOffset = 0;
    @Unique
    private boolean isEditing = false;
    @Unique
    private int editingTreeIndex = -1;
    @Unique
    private String editingText = "";

    // ==================== RENDERIZADO ====================
    @Inject(method = "render", at = @At("TAIL"))
    private void renderForestOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        List<dev.emi.emi.bom.MaterialTree> trees = ForestManager.getTrees();
        if (trees.isEmpty()) {
            scrollOffset = 0;
            return;
        }

        Font font = ((BoMScreen) (Object) this).getMinecraft().font;

        if (isCollapsed) {
            // Panel colapsado
            graphics.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_WIDTH, PANEL_Y + COLLAPSED_HEIGHT,
                    new Color(30, 30, 30, 200).getRGB());
            Component title = Component.translatable("emi_forest.gui.panel_title");
            Component expand = Component.translatable("emi_forest.gui.expand");
            Component text = title.copy().append(" ").append(expand);
            graphics.drawString(font, text, PANEL_X + 5, PANEL_Y + 3, Color.WHITE.getRGB());
            return;
        }

        // --- Panel expandido ---
        int totalRows = trees.size();
        int maxOffset = Math.max(0, totalRows - MAX_VISIBLE_ROWS);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;
        if (scrollOffset < 0) scrollOffset = 0;

        int visibleRows = Math.min(MAX_VISIBLE_ROWS, totalRows - scrollOffset);
        // Altura total = título + filas visibles + botón (siempre) + un poco de margen
        int panelHeight = HEADER_HEIGHT + visibleRows * ROW_HEIGHT + BUTTON_HEIGHT + 4;

        // Fondo
        graphics.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_WIDTH, PANEL_Y + panelHeight,
                new Color(20, 20, 20, 210).getRGB());

        // Título con [ - ]
        Component title = Component.translatable("emi_forest.gui.panel_title");
        Component collapse = Component.translatable("emi_forest.gui.collapse");
        Component headerText = title.copy().append(" ").append(collapse);
        graphics.drawString(font, headerText, PANEL_X + 5, PANEL_Y + 2, Color.LIGHT_GRAY.getRGB());

        int current = ForestManager.getCurrentIndex();
        for (int i = 0; i < visibleRows; i++) {
            int treeIndex = scrollOffset + i;

            if (isEditing && treeIndex == editingTreeIndex) {
                // Modo edición
                String display = editingText + (System.currentTimeMillis() % 1000 > 500 ? "|" : "");
                int color = Color.YELLOW.getRGB();
                int y = PANEL_Y + HEADER_HEIGHT + i * ROW_HEIGHT;
                graphics.fill(PANEL_X + 4, y, PANEL_X + PANEL_WIDTH - 4, y + ROW_HEIGHT,
                        new Color(50, 50, 50, 200).getRGB());
                graphics.drawString(font, Component.literal(display), PANEL_X + 6, y + 2, color);
                continue;
            }

            // Nombre normal
            String name = ForestManager.getDisplayName(treeIndex);
            String prefix = (treeIndex == current) ? "> " : "  ";
            Component line = Component.literal(prefix + name);
            int color = (treeIndex == current) ? Color.GREEN.getRGB() : Color.LIGHT_GRAY.getRGB();
            int y = PANEL_Y + HEADER_HEIGHT + i * ROW_HEIGHT;
            graphics.drawString(font, line, PANEL_X + 5, y + 2, color);
        }

        // Scrollbar
        if (totalRows > MAX_VISIBLE_ROWS) {
            int scrollbarX = PANEL_X + PANEL_WIDTH - 5;
            int scrollbarY = PANEL_Y + HEADER_HEIGHT;
            int scrollbarH = visibleRows * ROW_HEIGHT;
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 3, scrollbarY + scrollbarH,
                    new Color(100, 100, 100, 200).getRGB());
            float ratio = (float) scrollOffset / maxOffset;
            int thumbH = Math.max(6, scrollbarH * MAX_VISIBLE_ROWS / totalRows);
            int thumbY = scrollbarY + (int) ((scrollbarH - thumbH) * ratio);
            graphics.fill(scrollbarX, thumbY, scrollbarX + 3, thumbY + thumbH, Color.WHITE.getRGB());
        }

        // --- Botón "Eliminar todos" ---
        int buttonX = PANEL_X + 5;
        int buttonY = PANEL_Y + HEADER_HEIGHT + visibleRows * ROW_HEIGHT + 2;
        int buttonWidth = PANEL_WIDTH - 10;
        // Fondo rojo oscuro
        graphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + BUTTON_HEIGHT,
                new Color(180, 40, 40, 200).getRGB());

        String buttonText = Component.translatable("emi_forest.gui.delete_all").getString();
        int textWidth = font.width(buttonText);
        graphics.drawString(font, Component.literal(buttonText),
                buttonX + (buttonWidth - textWidth) / 2, buttonY + 3, Color.WHITE.getRGB());
    }

    // ==================== EVENTOS DE RATÓN ====================
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        List<dev.emi.emi.bom.MaterialTree> trees = ForestManager.getTrees();
        if (trees.isEmpty()) {
            // Si estábamos editando, cancelar
            if (isEditing) cancelEditing();
            return;
        }

        int panelHeight;
        if (isCollapsed) {
            panelHeight = COLLAPSED_HEIGHT;
        } else {
            int visibleRows = Math.min(MAX_VISIBLE_ROWS, trees.size() - scrollOffset);
            panelHeight = HEADER_HEIGHT + visibleRows * ROW_HEIGHT + BUTTON_HEIGHT + 4;
        }

        // Si clic fuera del panel, cancelamos edición si la hay
        if (mouseX < PANEL_X || mouseX > PANEL_X + PANEL_WIDTH ||
                mouseY < PANEL_Y || mouseY > PANEL_Y + panelHeight) {
            if (isEditing) {
                cancelEditing();
                cir.setReturnValue(true);
            }
            return;
        }

        // Clic en la barra de título -> colapsar/expandir
        if (mouseY >= PANEL_Y && mouseY <= PANEL_Y + HEADER_HEIGHT) {
            isCollapsed = !isCollapsed;
            if (isEditing) cancelEditing();
            cir.setReturnValue(true);
            return;
        }

        // Si está colapsado, no hay más zonas activas
        if (isCollapsed) {
            cir.setReturnValue(true);
            return;
        }

        // Zona de filas (debajo del título)
        int visibleRows = Math.min(MAX_VISIBLE_ROWS, trees.size() - scrollOffset);
        if (mouseY >= PANEL_Y + HEADER_HEIGHT && mouseY <= PANEL_Y + HEADER_HEIGHT + visibleRows * ROW_HEIGHT) {
            int relY = (int) mouseY - (PANEL_Y + HEADER_HEIGHT);
            int row = relY / ROW_HEIGHT;
            if (row >= 0 && row < visibleRows) {
                int treeIndex = scrollOffset + row;

                if (button == 1) { // botón derecho: editar
                    if (isEditing && editingTreeIndex != treeIndex) {
                        applyEditing();
                    }
                    startEditing(treeIndex);
                    cir.setReturnValue(true);
                    return;
                }

                if (button == 0) { // botón izquierdo: seleccionar
                    if (isEditing) {
                        applyEditing();
                    }
                    ForestManager.select(treeIndex);
                    ForestManager.refreshBoM();
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        // Botón "Eliminar todos"
        int buttonY = PANEL_Y + HEADER_HEIGHT + visibleRows * ROW_HEIGHT + 2;
        if (mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT &&
                mouseX >= PANEL_X + 5 && mouseX <= PANEL_X + PANEL_WIDTH - 5) {
            ForestManager.deleteAll();
            if (isEditing) cancelEditing();
            cir.setReturnValue(true);
            return;
        }

        // Cualquier otro clic en el panel lo consumimos
        cir.setReturnValue(true);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        List<dev.emi.emi.bom.MaterialTree> trees = ForestManager.getTrees();
        if (trees.isEmpty() || isCollapsed) return;

        int totalRows = trees.size();
        int maxOffset = Math.max(0, totalRows - MAX_VISIBLE_ROWS);
        if (maxOffset == 0) return;

        int visibleRows = Math.min(MAX_VISIBLE_ROWS, totalRows - scrollOffset);
        int panelHeight = HEADER_HEIGHT + visibleRows * ROW_HEIGHT + BUTTON_HEIGHT + 4;

        if (mouseX >= PANEL_X && mouseX <= PANEL_X + PANEL_WIDTH &&
                mouseY >= PANEL_Y && mouseY <= PANEL_Y + panelHeight) {
            scrollOffset -= (int) delta;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
            cir.setReturnValue(true);
        }
    }

    // ==================== TECLADO (para edición) ====================
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!isEditing) return;

        // Teclas de control
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            applyEditing();
            cir.setReturnValue(true);
            return;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            cancelEditing();
            cir.setReturnValue(true);
            return;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!editingText.isEmpty()) {
                editingText = editingText.substring(0, editingText.length() - 1);
            }
            cir.setReturnValue(true);
            return;
        }

        // Mapeo de teclas a caracteres
        char typedChar = keyCodeToChar(keyCode, modifiers);
        if (typedChar != 0) {
            if (editingText.length() < 30) {
                editingText += typedChar;
            }
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static char keyCodeToChar(int keyCode, int modifiers) {
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        // Letras
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            char base = (char) ('A' + (keyCode - GLFW.GLFW_KEY_A));
            return shift ? base : Character.toLowerCase(base);
        }

        // Números
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            if (shift) {
                return switch (keyCode) {
                    case GLFW.GLFW_KEY_1 -> '!';
                    case GLFW.GLFW_KEY_2 -> '"';
                    case GLFW.GLFW_KEY_3 -> '\u00A7';
                    case GLFW.GLFW_KEY_4 -> '$';
                    case GLFW.GLFW_KEY_5 -> '%';
                    case GLFW.GLFW_KEY_6 -> '&';
                    case GLFW.GLFW_KEY_7 -> '/';
                    case GLFW.GLFW_KEY_8 -> '(';
                    case GLFW.GLFW_KEY_9 -> ')';
                    case GLFW.GLFW_KEY_0 -> '=';
                    default -> (char) 0;
                };
            } else {
                return (char) ('0' + (keyCode - GLFW.GLFW_KEY_0));
            }
        }

        // Teclas especiales
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> ' ';
            case GLFW.GLFW_KEY_MINUS -> shift ? '_' : '-';
            case GLFW.GLFW_KEY_PERIOD -> shift ? ':' : '.';
            case GLFW.GLFW_KEY_COMMA -> shift ? ';' : ',';
            case GLFW.GLFW_KEY_SLASH -> shift ? '?' : '/';
            case GLFW.GLFW_KEY_BACKSLASH -> shift ? '|' : '\\';
            default -> 0;
        };
    }

    // ==================== Métodos auxiliares de edición ====================
    @Unique
    private void startEditing(int treeIndex) {
        isEditing = true;
        editingTreeIndex = treeIndex;
        editingText = ForestManager.getDisplayName(treeIndex);
    }

    @Unique
    private void applyEditing() {
        if (!isEditing) return;
        String trimmed = editingText.trim();
        if (!trimmed.isEmpty()) {
            ForestManager.setCustomName(editingTreeIndex, trimmed);
        } else {
            ForestManager.setCustomName(editingTreeIndex, null);
        }
        isEditing = false;
        editingTreeIndex = -1;
        editingText = "";
    }

    @Unique
    private void cancelEditing() {
        isEditing = false;
        editingTreeIndex = -1;
        editingText = "";
    }
}