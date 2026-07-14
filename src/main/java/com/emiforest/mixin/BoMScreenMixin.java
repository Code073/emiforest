package com.emiforest.mixin;

import com.emiforest.forest.ForestManager;
import dev.emi.emi.screen.BoMScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
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

    @Unique
    private static final int PANEL_X = 10;
    @Unique
    private static final int PANEL_Y = 10;
    @Unique
    private static final int PANEL_WIDTH = 130;
    @Unique
    private static final int ROW_HEIGHT = 13;
    @Unique
    private static final int MAX_VISIBLE_ROWS = 8;
    @Unique
    private static final int HEADER_HEIGHT = 16;
    @Unique
    private static final int COLLAPSED_HEIGHT = HEADER_HEIGHT;
    @Unique
    private static final int BUTTON_HEIGHT = 15;
    @Unique
    private static final int SCROLLBAR_W = 3;
    @Unique
    private static final int MAX_NAME_LENGTH =19;

    @Unique
    private static final int C_EDIT_SELECTION = new Color(90, 150, 230, 130).getRGB();
    @Unique
    private static final int C_BG_TOP        = new Color(16, 22, 18, 235).getRGB();
    @Unique
    private static final int C_BG_BOTTOM     = new Color(12, 16, 14, 235).getRGB();
    @Unique
    private static final int C_BORDER        = new Color(58, 92, 62, 255).getRGB();
    @Unique
    private static final int C_BORDER_SOFT   = new Color(40, 60, 42, 200).getRGB();
    @Unique
    private static final int C_HEADER_BG     = new Color(24, 34, 26, 255).getRGB();
    @Unique
    private static final int C_HEADER_BG_HOVER = new Color(30, 42, 32, 255).getRGB();
    @Unique
    private static final int C_HEADER_ACCENT = new Color(97, 191, 105, 255).getRGB();
    @Unique
    private static final int C_HEADER_TEXT   = new Color(210, 230, 210, 255).getRGB();
    @Unique
    private static final int C_ROW_HOVER     = new Color(255, 255, 255, 18).getRGB();
    @Unique
    private static final int C_ROW_SELECTED_BG = new Color(48, 94, 56, 160).getRGB();
    @Unique
    private static final int C_TEXT_NORMAL   = new Color(196, 206, 198, 255).getRGB();
    @Unique
    private static final int C_TEXT_SELECTED = new Color(148, 224, 150, 255).getRGB();
    @Unique
    private static final int C_TEXT_MUTED    = new Color(130, 140, 132, 255).getRGB();
    @Unique
    private static final int C_EDIT_BG       = new Color(70, 66, 30, 210).getRGB();
    @Unique
    private static final int C_EDIT_BORDER   = new Color(214, 189, 60, 255).getRGB();
    @Unique
    private static final int C_EDIT_TEXT     = new Color(255, 224, 120, 255).getRGB();
    @Unique
    private static final int C_SCROLL_TRACK  = new Color(255, 255, 255, 14).getRGB();
    @Unique
    private static final int C_SCROLL_THUMB  = new Color(120, 180, 124, 220).getRGB();
    @Unique
    private static final int C_DELETE_BG     = new Color(120, 34, 34, 210).getRGB();
    @Unique
    private static final int C_DELETE_BG_HOVER = new Color(158, 44, 44, 230).getRGB();
    @Unique
    private static final int C_DELETE_BORDER = new Color(200, 90, 90, 255).getRGB();
    @Unique
    private static final int C_TEXT_ON_DANGER = new Color(255, 235, 235, 255).getRGB();
    @Unique
    private static final int C_CAPS_INDICATOR = new Color(255, 190, 90, 255).getRGB();


    @Unique
    private static boolean emiforest$isCollapsed = false;
    @Unique
    private int emiforest$scrollOffset = 0;
    @Unique
    private boolean emiforest$isEditing = false;
    @Unique
    private int emiforest$editingTreeIndex = -1;
    @Unique
    private String emiforest$editingText = "";
    @Unique
    private boolean emiforest$capsLockOn = false;
    @Unique
    private boolean emiforest$isDraggingScrollbar = false;
    @Unique
    private int emiforest$cursorPos = 0;
    @Unique
    private int emiforest$selectionAnchor = -1; // -1 significa que no hay selección activa


    @Inject(method = "render", at = @At("TAIL"))
    private void renderForestOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        List<dev.emi.emi.bom.MaterialTree> trees = ForestManager.getTrees();
        if (trees.isEmpty()) {
            emiforest$scrollOffset = 0;
            return;
        }

        Font font = ((BoMScreen) (Object) this).getMinecraft().font;

        if (emiforest$isCollapsed) {
            drawPanelBackground(graphics, PANEL_X, PANEL_Y, PANEL_WIDTH, COLLAPSED_HEIGHT);
            drawHeader(graphics, font, mouseX, mouseY, PANEL_X, PANEL_Y, PANEL_WIDTH,
                    Component.translatable("emi_forest.gui.panel_title"),
                    "\u25B8", trees.size());
            return;
        }


        int totalRows = trees.size();
        int maxOffset = Math.max(0, totalRows - MAX_VISIBLE_ROWS);
        if (emiforest$scrollOffset > maxOffset) emiforest$scrollOffset = maxOffset;
        if (emiforest$scrollOffset < 0) emiforest$scrollOffset = 0;

        int visibleRows = Math.min(MAX_VISIBLE_ROWS, totalRows - emiforest$scrollOffset);
        int panelHeight = HEADER_HEIGHT + visibleRows * ROW_HEIGHT + BUTTON_HEIGHT + 6;

        drawPanelBackground(graphics, PANEL_X, PANEL_Y, PANEL_WIDTH, panelHeight);
        drawHeader(graphics, font, mouseX, mouseY, PANEL_X, PANEL_Y, PANEL_WIDTH,
                Component.translatable("emi_forest.gui.panel_title"),
                "\u25BE", trees.size());

        int current = ForestManager.getCurrentIndex();
        int rowsTop = PANEL_Y + HEADER_HEIGHT;

        for (int i = 0; i < visibleRows; i++) {
            int treeIndex = emiforest$scrollOffset + i;
            int y = rowsTop + i * ROW_HEIGHT;
            boolean hovered = mouseX >= PANEL_X + 2 && mouseX <= PANEL_X + PANEL_WIDTH - 2
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;

            if (emiforest$isEditing && treeIndex == emiforest$editingTreeIndex) {
                graphics.fill(PANEL_X + 3, y + 1, PANEL_X + PANEL_WIDTH - 3, y + ROW_HEIGHT - 1, C_EDIT_BG);
                drawBorder(graphics, PANEL_X + 3, y + 1, PANEL_WIDTH - 6, ROW_HEIGHT - 2, C_EDIT_BORDER);

                int textX = PANEL_X + 8;
                int textY = y + 3;

                // Resaltado de selección
                if (emiforest$selectionAnchor != -1 && emiforest$selectionAnchor != emiforest$cursorPos) {
                    int selStart = Math.min(emiforest$selectionAnchor, emiforest$cursorPos);
                    int selEnd = Math.max(emiforest$selectionAnchor, emiforest$cursorPos);
                    int selStartX = textX + font.width(emiforest$editingText.substring(0, selStart));
                    int selEndX = textX + font.width(emiforest$editingText.substring(0, selEnd));
                    graphics.fill(selStartX, y + 2, selEndX, y + ROW_HEIGHT - 2, C_EDIT_SELECTION);
                }

                graphics.drawString(font, Component.literal(emiforest$editingText), textX, textY, C_EDIT_TEXT);

                // Cursor parpadeante
                if (System.currentTimeMillis() % 1000 > 500) {
                    int safeCursor = Math.max(0, Math.min(emiforest$cursorPos, emiforest$editingText.length()));
                    int cursorX = textX + font.width(emiforest$editingText.substring(0, safeCursor));
                    graphics.fill(cursorX, y + 2, cursorX + 1, y + ROW_HEIGHT - 2, C_EDIT_TEXT);
                }

                // Indicador de Bloq Mayús
                if (emiforest$capsLockOn) {
                    String caps = "CAPS";
                    int capsWidth = font.width(caps);
                    graphics.drawString(font, Component.literal(caps),
                            PANEL_X + PANEL_WIDTH - capsWidth - 6, y + 3, C_CAPS_INDICATOR);
                }
                continue;
            }
            boolean selected = treeIndex == current;
            if (selected) {
                graphics.fill(PANEL_X + 3, y + 1, PANEL_X + PANEL_WIDTH - 3, y + ROW_HEIGHT - 1, C_ROW_SELECTED_BG);
            } else if (hovered) {
                graphics.fill(PANEL_X + 3, y + 1, PANEL_X + PANEL_WIDTH - 3, y + ROW_HEIGHT - 1, C_ROW_HOVER);
            }

            String name = ForestManager.getDisplayName(treeIndex);
            String marker = selected ? "\u25CF" : " ";
            Component line = Component.literal(marker + " " + name);
            int color = selected ? C_TEXT_SELECTED : C_TEXT_NORMAL;
            graphics.drawString(font, line, PANEL_X + 8, y + 3, color);
        }

        // Separador entre filas y botón
        graphics.fill(PANEL_X + 3, rowsTop + visibleRows * ROW_HEIGHT, PANEL_X + PANEL_WIDTH - 3,
                rowsTop + visibleRows * ROW_HEIGHT + 1, C_BORDER_SOFT);

        // Scrollbar
        if (totalRows > MAX_VISIBLE_ROWS) {
            int scrollbarX = PANEL_X + PANEL_WIDTH - SCROLLBAR_W - 3;
            int scrollbarY = rowsTop;
            int scrollbarH = visibleRows * ROW_HEIGHT;
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_W, scrollbarY + scrollbarH, C_SCROLL_TRACK);
            float ratio = maxOffset == 0 ? 0 : (float) emiforest$scrollOffset / maxOffset;
            int thumbH = Math.max(8, scrollbarH * MAX_VISIBLE_ROWS / totalRows);
            int thumbY = scrollbarY + (int) ((scrollbarH - thumbH) * ratio);
            graphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_W, thumbY + thumbH, C_SCROLL_THUMB);
        }


        int buttonX = PANEL_X + 5;
        int buttonY = rowsTop + visibleRows * ROW_HEIGHT + 4;
        int buttonWidth = PANEL_WIDTH - 10;
        boolean buttonHovered = mouseX >= buttonX && mouseX <= buttonX + buttonWidth
                && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;

        graphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + BUTTON_HEIGHT,
                buttonHovered ? C_DELETE_BG_HOVER : C_DELETE_BG);
        drawBorder(graphics, buttonX, buttonY, buttonWidth, BUTTON_HEIGHT, C_DELETE_BORDER);

        String buttonText = Component.translatable("emi_forest.gui.delete_all").getString();
        int textWidth = font.width(buttonText);
        graphics.drawString(font, Component.literal(buttonText),
                buttonX + (buttonWidth - textWidth) / 2, buttonY + (BUTTON_HEIGHT - 8) / 2, C_TEXT_ON_DANGER);

        if (emiforest$isDraggingScrollbar) {
            long window = Minecraft.getInstance().getWindow().getWindow();
            if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_RELEASE) {
                emiforest$isDraggingScrollbar = false;
            }
        }
    }


    @Unique
    private void drawPanelBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        int mid = y + h / 2;
        graphics.fill(x, y, x + w, mid, C_BG_TOP);
        graphics.fill(x, mid, x + w, y + h, C_BG_BOTTOM);
        drawBorder(graphics, x, y, w, h, C_BORDER);
    }

    @Unique
    private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Unique
    private void drawHeader(GuiGraphics graphics, Font font, int mouseX, int mouseY,
                            int x, int y, int w, Component title, String arrow, int count) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
        graphics.fill(x, y, x + w, y + HEADER_HEIGHT, hovered ? C_HEADER_BG_HOVER : C_HEADER_BG);
        graphics.fill(x, y + HEADER_HEIGHT - 1, x + w, y + HEADER_HEIGHT, C_HEADER_ACCENT);

        Component text = title.copy()
                .append(Component.literal("  (" + count + ")").withStyle(s -> s.withColor(0x8FA890)));
        graphics.drawString(font, text, x + 6, y + 4, C_HEADER_TEXT);

        int arrowWidth = font.width(arrow);
        graphics.drawString(font, Component.literal(arrow), x + w - arrowWidth - 6, y + 4, C_HEADER_ACCENT);
    }


    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        List<dev.emi.emi.bom.MaterialTree> trees = ForestManager.getTrees();
        if (trees.isEmpty()) {
            if (emiforest$isEditing) cancelEditing();
            return;
        }

        int totalRows = trees.size();
        int visibleRows;
        int panelHeight;
        if (emiforest$isCollapsed) {
            panelHeight = COLLAPSED_HEIGHT;
            visibleRows = 0;
        } else {
            visibleRows = Math.min(MAX_VISIBLE_ROWS, totalRows - emiforest$scrollOffset);
            panelHeight = HEADER_HEIGHT + visibleRows * ROW_HEIGHT + BUTTON_HEIGHT + 6;
        }

        // Click fuera del panel: guardar edición
        if (mouseX < PANEL_X || mouseX > PANEL_X + PANEL_WIDTH ||
                mouseY < PANEL_Y || mouseY > PANEL_Y + panelHeight) {
            if (emiforest$isEditing) {
                applyEditing();
                cir.setReturnValue(true);
            }
            return;
        }

        // Clic en el encabezado: colapsar/expandir
        if (mouseY >= PANEL_Y && mouseY <= PANEL_Y + HEADER_HEIGHT) {
            emiforest$isCollapsed = !emiforest$isCollapsed;
            if (emiforest$isEditing) cancelEditing();
            cir.setReturnValue(true);
            return;
        }

        if (emiforest$isCollapsed) {
            cir.setReturnValue(true);
            return;
        }

        int rowsTop = PANEL_Y + HEADER_HEIGHT;

        // Zona de la scrollbar: iniciar arrastre / salto directo
        if (totalRows > MAX_VISIBLE_ROWS) {
            int scrollbarX = PANEL_X + PANEL_WIDTH - SCROLLBAR_W - 3;
            int scrollbarH = visibleRows * ROW_HEIGHT;
            if (mouseX >= scrollbarX - 2 && mouseX <= scrollbarX + SCROLLBAR_W + 2
                    && mouseY >= rowsTop && mouseY <= rowsTop + scrollbarH) {
                if (emiforest$isEditing) applyEditing();

                int maxOffset = Math.max(0, totalRows - MAX_VISIBLE_ROWS);
                int thumbH = Math.max(8, scrollbarH * MAX_VISIBLE_ROWS / totalRows);
                int trackRange = scrollbarH - thumbH;
                if (trackRange > 0) {
                    float ratio = (float) (mouseY - rowsTop - thumbH / 2.0) / trackRange;
                    ratio = Math.max(0f, Math.min(1f, ratio));
                    emiforest$scrollOffset = Math.round(ratio * maxOffset);
                }
                emiforest$isDraggingScrollbar = true;
                cir.setReturnValue(true);
                return;
            }
        }

        // Clic en filas
        if (mouseY >= rowsTop && mouseY <= rowsTop + visibleRows * ROW_HEIGHT) {
            int relY = (int) mouseY - rowsTop;
            int row = relY / ROW_HEIGHT;
            if (row >= 0 && row < visibleRows) {
                int treeIndex = emiforest$scrollOffset + row;

                if (button == 1) {
                    if (emiforest$isEditing && emiforest$editingTreeIndex != treeIndex) {
                        applyEditing();
                    }
                    startEditing(treeIndex);
                    cir.setReturnValue(true);
                    return;
                }

                if (button == 0) {
                    if (emiforest$isEditing) {
                        applyEditing();
                    }
                    ForestManager.select(treeIndex);
                    ForestManager.refreshBoM();
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        // Botón eliminar
        int buttonY = rowsTop + visibleRows * ROW_HEIGHT + 4;
        if (mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT &&
                mouseX >= PANEL_X + 5 && mouseX <= PANEL_X + PANEL_WIDTH - 5) {
            ForestManager.deleteAll();
            if (emiforest$isEditing) cancelEditing();
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        List<dev.emi.emi.bom.MaterialTree> trees = ForestManager.getTrees();
        if (trees.isEmpty() || emiforest$isCollapsed) return;

        int totalRows = trees.size();
        int maxOffset = Math.max(0, totalRows - MAX_VISIBLE_ROWS);
        if (maxOffset == 0) return;

        int visibleRows = Math.min(MAX_VISIBLE_ROWS, totalRows - emiforest$scrollOffset);
        int panelHeight = HEADER_HEIGHT + visibleRows * ROW_HEIGHT + BUTTON_HEIGHT + 6;

        if (mouseX >= PANEL_X && mouseX <= PANEL_X + PANEL_WIDTH &&
                mouseY >= PANEL_Y && mouseY <= PANEL_Y + panelHeight) {
            emiforest$scrollOffset -= (int) delta;
            emiforest$scrollOffset = Math.max(0, Math.min(emiforest$scrollOffset, maxOffset));
            cir.setReturnValue(true);
        }
    }


    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!emiforest$isEditing) return;

        if (keyCode == GLFW.GLFW_KEY_CAPS_LOCK) {
            emiforest$capsLockOn = !emiforest$capsLockOn;
            cir.setReturnValue(true);
            return;
        }

        boolean ctrl = Screen.hasControlDown();
        boolean shift = Screen.hasShiftDown();

        // Atajos de teclado: Ctrl+A, Ctrl+C, Ctrl+X, Ctrl+V
        if (ctrl && keyCode == GLFW.GLFW_KEY_A) {
            emiforest$selectionAnchor = 0;
            emiforest$cursorPos = emiforest$editingText.length();
            cir.setReturnValue(true);
            return;
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_C) {
            emiforest$copySelectionToClipboard();
            cir.setReturnValue(true);
            return;
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_X) {
            emiforest$copySelectionToClipboard();
            emiforest$deleteSelection();
            cir.setReturnValue(true);
            return;
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
            emiforest$pasteFromClipboard();
            cir.setReturnValue(true);
            return;
        }

        // Navegación con teclas de flecha
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (shift) {
                if (emiforest$selectionAnchor == -1) emiforest$selectionAnchor = emiforest$cursorPos;
                emiforest$cursorPos = Math.max(0, emiforest$cursorPos - 1);
            } else {
                if (emiforest$selectionAnchor != -1) {
                    emiforest$cursorPos = Math.min(emiforest$selectionAnchor, emiforest$cursorPos);
                    emiforest$selectionAnchor = -1;
                } else {
                    emiforest$cursorPos = Math.max(0, emiforest$cursorPos - 1);
                }
            }
            cir.setReturnValue(true);
            return;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (shift) {
                if (emiforest$selectionAnchor == -1) emiforest$selectionAnchor = emiforest$cursorPos;
                emiforest$cursorPos = Math.min(emiforest$editingText.length(), emiforest$cursorPos + 1);
            } else {
                if (emiforest$selectionAnchor != -1) {
                    emiforest$cursorPos = Math.max(emiforest$selectionAnchor, emiforest$cursorPos);
                    emiforest$selectionAnchor = -1;
                } else {
                    emiforest$cursorPos = Math.min(emiforest$editingText.length(), emiforest$cursorPos + 1);
                }
            }
            cir.setReturnValue(true);
            return;
        }
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            if (shift) {
                if (emiforest$selectionAnchor == -1) emiforest$selectionAnchor = emiforest$cursorPos;
            } else {
                emiforest$selectionAnchor = -1;
            }
            emiforest$cursorPos = 0;
            cir.setReturnValue(true);
            return;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            if (shift) {
                if (emiforest$selectionAnchor == -1) emiforest$selectionAnchor = emiforest$cursorPos;
            } else {
                emiforest$selectionAnchor = -1;
            }
            emiforest$cursorPos = emiforest$editingText.length();
            cir.setReturnValue(true);
            return;
        }

        // Enter: guardar
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            applyEditing();
            cir.setReturnValue(true);
            return;
        }
        // Escape: cancelar
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            cancelEditing();
            cir.setReturnValue(true);
            return;
        }

        // Backspace y Delete
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (emiforest$selectionAnchor != -1) {
                emiforest$deleteSelection();
            } else if (emiforest$cursorPos > 0) {
                emiforest$editingText = emiforest$editingText.substring(0, emiforest$cursorPos - 1)
                        + emiforest$editingText.substring(emiforest$cursorPos);
                emiforest$cursorPos--;
            }
            cir.setReturnValue(true);
            return;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (emiforest$selectionAnchor != -1) {
                emiforest$deleteSelection();
            } else if (emiforest$cursorPos < emiforest$editingText.length()) {
                emiforest$editingText = emiforest$editingText.substring(0, emiforest$cursorPos)
                        + emiforest$editingText.substring(emiforest$cursorPos + 1);
            }
            cir.setReturnValue(true);
            return;
        }

        // Caracteres normales (letras, números, símbolos)
        char typedChar = keyCodeToChar(keyCode, shift, emiforest$capsLockOn);
        if (typedChar != 0) {
            if (emiforest$selectionAnchor != -1) {
                emiforest$deleteSelection();
            }
            if (emiforest$editingText.length() < MAX_NAME_LENGTH) {
                emiforest$editingText = emiforest$editingText.substring(0, emiforest$cursorPos)
                        + typedChar + emiforest$editingText.substring(emiforest$cursorPos);
                emiforest$cursorPos++;
            }
            cir.setReturnValue(true);
        }
    }
    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void onMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if (!emiforest$isDraggingScrollbar) return;

        List<dev.emi.emi.bom.MaterialTree> trees = ForestManager.getTrees();
        int totalRows = trees.size();
        if (totalRows <= MAX_VISIBLE_ROWS || emiforest$isCollapsed) {
            emiforest$isDraggingScrollbar = false;
            return;
        }

        int maxOffset = Math.max(0, totalRows - MAX_VISIBLE_ROWS);
        int rowsTop = PANEL_Y + HEADER_HEIGHT;
        int scrollbarH = MAX_VISIBLE_ROWS * ROW_HEIGHT;
        int thumbH = Math.max(8, scrollbarH * MAX_VISIBLE_ROWS / totalRows);
        int trackRange = scrollbarH - thumbH;
        if (trackRange <= 0) {
            emiforest$isDraggingScrollbar = false;
            return;
        }

        float ratio = (float) (mouseY - rowsTop - thumbH / 2.0) / trackRange;
        ratio = Math.max(0f, Math.min(1f, ratio));
        emiforest$scrollOffset = Math.round(ratio * maxOffset);
        cir.setReturnValue(true);
    }
    @Unique
    private static char keyCodeToChar(int keyCode, boolean shift, boolean capsLock) {

        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            char base = (char) ('A' + (keyCode - GLFW.GLFW_KEY_A));
            boolean upper = shift ^ capsLock;  // XOR: si uno es true y el otro no, mayúscula
            return upper ? base : Character.toLowerCase(base);
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

        // Otros caracteres
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


    @Unique
    private void startEditing(int treeIndex) {
        emiforest$isEditing = true;
        emiforest$editingTreeIndex = treeIndex;
        emiforest$editingText = ForestManager.getDisplayName(treeIndex);
        emiforest$cursorPos = emiforest$editingText.length();
        emiforest$selectionAnchor = -1;
    }

    @Unique
    private void applyEditing() {
        if (!emiforest$isEditing) return;
        String trimmed = emiforest$editingText.trim();
        if (!trimmed.isEmpty()) {
            ForestManager.setCustomName(emiforest$editingTreeIndex, trimmed);
        } else {
            ForestManager.setCustomName(emiforest$editingTreeIndex, null);
        }
        emiforest$isEditing = false;
        emiforest$editingTreeIndex = -1;
        emiforest$editingText = "";
        emiforest$cursorPos = 0;
        emiforest$selectionAnchor = -1;
    }

    @Unique
    private void cancelEditing() {
        emiforest$isEditing = false;
        emiforest$editingTreeIndex = -1;
        emiforest$editingText = "";
        emiforest$cursorPos = 0;
        emiforest$selectionAnchor = -1;
    }

    @Unique
    private void emiforest$deleteSelection() {
        if (emiforest$selectionAnchor == -1) return;
        int start = Math.min(emiforest$selectionAnchor, emiforest$cursorPos);
        int end = Math.max(emiforest$selectionAnchor, emiforest$cursorPos);
        emiforest$editingText = emiforest$editingText.substring(0, start) + emiforest$editingText.substring(end);
        emiforest$cursorPos = start;
        emiforest$selectionAnchor = -1;
    }

    @Unique
    private void emiforest$copySelectionToClipboard() {
        String toCopy;
        if (emiforest$selectionAnchor != -1 && emiforest$selectionAnchor != emiforest$cursorPos) {
            int start = Math.min(emiforest$selectionAnchor, emiforest$cursorPos);
            int end = Math.max(emiforest$selectionAnchor, emiforest$cursorPos);
            toCopy = emiforest$editingText.substring(start, end);
        } else {
            toCopy = emiforest$editingText;
        }
        long window = Minecraft.getInstance().getWindow().getWindow();
        GLFW.glfwSetClipboardString(window, toCopy);
    }

    @Unique
    private void emiforest$pasteFromClipboard() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        String clip = GLFW.glfwGetClipboardString(window);
        if (clip == null || clip.isEmpty()) return;
        clip = clip.replace("\n", " ").replace("\r", "");

        if (emiforest$selectionAnchor != -1) {
            emiforest$deleteSelection();
        }

        int availableSpace = MAX_NAME_LENGTH - emiforest$editingText.length();
        if (availableSpace <= 0) return;
        if (clip.length() > availableSpace) {
            clip = clip.substring(0, availableSpace);
        }

        emiforest$editingText = emiforest$editingText.substring(0, emiforest$cursorPos)
                + clip + emiforest$editingText.substring(emiforest$cursorPos);
        emiforest$cursorPos += clip.length();
    }
}