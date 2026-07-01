
**EMI Forest** is a client-side Minecraft mod that enhances [EMI](https://modrinth.com/mod/emi) (or [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) via EMI) by allowing you to **switch between, rename, and manage multiple "Bill of Materials" trees** in the BoM screen.

---

## ✨ Features

- **Save & Restore Trees**  
  Save any BoM tree while viewing it (default keys: `X`, `N`, `B`) and quickly switch between them.

- **Custom Names**  
  Right-click a saved tree in the in‑screen panel to rename it.

- **Tree Management Panel**  
  An overlay on the BoM screen shows all saved trees with a sleek forest‑inspired design, collapsible header, scroll support, and a "Delete All" button.

- **Fully Customisable Keybinds**  
  Next tree, previous tree, and delete tree actions are configurable in the normal Minecraft Controls menu (category "EMI Forest").

---

## 📦 Installation

1. Install **Minecraft Forge** for **1.20.1** (or the version you built for).
2. Install **EMI** (the mod this addon depends on).
3. Download the latest `.jar` from the [Releases](https://github.com/Code073/emiforest/releases) page (or build from source).
4. Place the `.jar` into your `mods` folder.
5. Launch the game.

---

## 🎮 How to Use

1. Open any recipe's BoM screen (click the "Tree" button in EMI).
2. Use the default keybinds to manage trees:  
   - **Next tree** (`N`) – cycle forward through saved trees  
   - **Previous tree** (`B`) – cycle backward  
   - **Delete current tree** (`X`) – remove the selected tree  
   _All keybinds only work when the BoM screen is open._
3. The overlay panel on the left lists all saved trees.  
   - **Click** a tree to switch to it.  
   - **Right‑click** a tree to rename it.  
   - **Click the header** to collapse/expand the panel.  
   - **Click "Delete All"** to clear every saved tree.

---

## ⚙️ Configuration

- **Keybinds**  
  Go to `Options → Controls → Key Binds` and scroll down to the **EMI Forest** category. There you can change the three actions to any keys you like.

---

## 🛠️ Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/Code073/emiforest.git

   ## 🧰 Dependencies

- **Minecraft Forge** 1.20.1 (47.x or compatible)
- **EMI** (version 1.1.22+ for 1.20.1)

---

## 📝 License

This project is licensed under the [MIT License](LICENSE). You are free to use, modify, and distribute it as you wish, including in modpacks.

---

## 💬 Credits & Acknowledgments

- **[EMI](https://modrinth.com/mod/emi)** by Emily – the amazing item & recipe viewer that makes EMI Forest possible.
- The **Minecraft Forge** team for the modding platform.
- All the open‑source libraries used behind the scenes (Gson, Mixin, etc.).
