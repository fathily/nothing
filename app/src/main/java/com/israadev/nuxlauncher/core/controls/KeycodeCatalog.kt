package com.israadev.nuxlauncher.core.controls

import com.movtery.inputmap.keycodes.LwjglGlfwKeycode

data class KeyOption(
    val displayName: String,
    val keyCode: Int,
    val isMouseButton: Boolean = false,
    val mouseButton: Int = 0,
    val isScroll: Boolean = false,
    val category: String = "Umum"
)

object KeycodeCatalog {
    val ALL_KEYS: List<KeyOption> = listOf(
        KeyOption("ESC", LwjglGlfwKeycode.GLFW_KEY_ESCAPE, category = "Keyboard"),
        KeyOption("TAB", LwjglGlfwKeycode.GLFW_KEY_TAB, category = "Keyboard"),
        KeyOption("CAPS LOCK", LwjglGlfwKeycode.GLFW_KEY_CAPS_LOCK, category = "Keyboard"),
        KeyOption("SHIFT", LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT, category = "Keyboard"),
        KeyOption("CTRL", LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL, category = "Keyboard"),
        KeyOption("ALT", LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT, category = "Keyboard"),
        KeyOption("BACKSPACE", LwjglGlfwKeycode.GLFW_KEY_BACKSPACE, category = "Keyboard"),
        KeyOption("ENTER", LwjglGlfwKeycode.GLFW_KEY_ENTER, category = "Keyboard"),
        KeyOption("SPACE", LwjglGlfwKeycode.GLFW_KEY_SPACE, category = "Keyboard"),
        KeyOption("INSERT", LwjglGlfwKeycode.GLFW_KEY_INSERT, category = "Keyboard"),
        KeyOption("DELETE", LwjglGlfwKeycode.GLFW_KEY_DELETE, category = "Keyboard"),
        KeyOption("HOME", LwjglGlfwKeycode.GLFW_KEY_HOME, category = "Keyboard"),
        KeyOption("END", LwjglGlfwKeycode.GLFW_KEY_END, category = "Keyboard"),
        KeyOption("PAGE UP", LwjglGlfwKeycode.GLFW_KEY_PAGE_UP, category = "Keyboard"),
        KeyOption("PAGE DOWN", LwjglGlfwKeycode.GLFW_KEY_PAGE_DOWN, category = "Keyboard"),
        KeyOption("ARROW UP", LwjglGlfwKeycode.GLFW_KEY_UP, category = "Keyboard"),
        KeyOption("ARROW DOWN", LwjglGlfwKeycode.GLFW_KEY_DOWN, category = "Keyboard"),
        KeyOption("ARROW LEFT", LwjglGlfwKeycode.GLFW_KEY_LEFT, category = "Keyboard"),
        KeyOption("ARROW RIGHT", LwjglGlfwKeycode.GLFW_KEY_RIGHT, category = "Keyboard"),

        KeyOption("F1", LwjglGlfwKeycode.GLFW_KEY_F1, category = "Function"),
        KeyOption("F2", LwjglGlfwKeycode.GLFW_KEY_F2, category = "Function"),
        KeyOption("F3", LwjglGlfwKeycode.GLFW_KEY_F3, category = "Function"),
        KeyOption("F4", LwjglGlfwKeycode.GLFW_KEY_F4, category = "Function"),
        KeyOption("F5", LwjglGlfwKeycode.GLFW_KEY_F5, category = "Function"),
        KeyOption("F6", LwjglGlfwKeycode.GLFW_KEY_F6, category = "Function"),
        KeyOption("F7", LwjglGlfwKeycode.GLFW_KEY_F7, category = "Function"),
        KeyOption("F8", LwjglGlfwKeycode.GLFW_KEY_F8, category = "Function"),
        KeyOption("F9", LwjglGlfwKeycode.GLFW_KEY_F9, category = "Function"),
        KeyOption("F10", LwjglGlfwKeycode.GLFW_KEY_F10, category = "Function"),
        KeyOption("F11", LwjglGlfwKeycode.GLFW_KEY_F11, category = "Function"),
        KeyOption("F12", LwjglGlfwKeycode.GLFW_KEY_F12, category = "Function"),

        // Alphabet keys used by the visual keyboard
        KeyOption("Q", LwjglGlfwKeycode.GLFW_KEY_Q, category = "Keyboard"),
        KeyOption("W", LwjglGlfwKeycode.GLFW_KEY_W, category = "Keyboard"),
        KeyOption("E", LwjglGlfwKeycode.GLFW_KEY_E, category = "Keyboard"),
        KeyOption("R", LwjglGlfwKeycode.GLFW_KEY_R, category = "Keyboard"),
        KeyOption("T", LwjglGlfwKeycode.GLFW_KEY_T, category = "Keyboard"),
        KeyOption("Y", LwjglGlfwKeycode.GLFW_KEY_Y, category = "Keyboard"),
        KeyOption("U", LwjglGlfwKeycode.GLFW_KEY_U, category = "Keyboard"),
        KeyOption("I", LwjglGlfwKeycode.GLFW_KEY_I, category = "Keyboard"),
        KeyOption("O", LwjglGlfwKeycode.GLFW_KEY_O, category = "Keyboard"),
        KeyOption("P", LwjglGlfwKeycode.GLFW_KEY_P, category = "Keyboard"),
        KeyOption("A", LwjglGlfwKeycode.GLFW_KEY_A, category = "Keyboard"),
        KeyOption("S", LwjglGlfwKeycode.GLFW_KEY_S, category = "Keyboard"),
        KeyOption("D", LwjglGlfwKeycode.GLFW_KEY_D, category = "Keyboard"),
        KeyOption("F", LwjglGlfwKeycode.GLFW_KEY_F, category = "Keyboard"),
        KeyOption("G", LwjglGlfwKeycode.GLFW_KEY_G, category = "Keyboard"),
        KeyOption("H", LwjglGlfwKeycode.GLFW_KEY_H, category = "Keyboard"),
        KeyOption("J", LwjglGlfwKeycode.GLFW_KEY_J, category = "Keyboard"),
        KeyOption("K", LwjglGlfwKeycode.GLFW_KEY_K, category = "Keyboard"),
        KeyOption("L", LwjglGlfwKeycode.GLFW_KEY_L, category = "Keyboard"),
        KeyOption("Z", LwjglGlfwKeycode.GLFW_KEY_Z, category = "Keyboard"),
        KeyOption("X", LwjglGlfwKeycode.GLFW_KEY_X, category = "Keyboard"),
        KeyOption("C", LwjglGlfwKeycode.GLFW_KEY_C, category = "Keyboard"),
        KeyOption("V", LwjglGlfwKeycode.GLFW_KEY_V, category = "Keyboard"),
        KeyOption("B", LwjglGlfwKeycode.GLFW_KEY_B, category = "Keyboard"),
        KeyOption("N", LwjglGlfwKeycode.GLFW_KEY_N, category = "Keyboard"),
        KeyOption("M", LwjglGlfwKeycode.GLFW_KEY_M, category = "Keyboard"),
        KeyOption("0", LwjglGlfwKeycode.GLFW_KEY_0, category = "Keyboard"),

        // Mouse Controls
        KeyOption("Mouse Kiri (Attack/Break)", 0, isMouseButton = true, mouseButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT, category = "Mouse"),
        KeyOption("Mouse Kanan (Use/Place)", 0, isMouseButton = true, mouseButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT, category = "Mouse"),
        KeyOption("Mouse Tengah (Pick Block)", 0, isMouseButton = true, mouseButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_MIDDLE, category = "Mouse"),
        KeyOption("Scroll Wheel (Slide Naik/Turun)", 0, isScroll = true, category = "Mouse"),

        // Gerakan (Movement)
        KeyOption("W (Maju)", LwjglGlfwKeycode.GLFW_KEY_W, category = "Gerakan"),
        KeyOption("A (Kiri)", LwjglGlfwKeycode.GLFW_KEY_A, category = "Gerakan"),
        KeyOption("S (Mundur)", LwjglGlfwKeycode.GLFW_KEY_S, category = "Gerakan"),
        KeyOption("D (Kanan)", LwjglGlfwKeycode.GLFW_KEY_D, category = "Gerakan"),
        KeyOption("SPACE (Lompat)", LwjglGlfwKeycode.GLFW_KEY_SPACE, category = "Gerakan"),
        KeyOption("LEFT SHIFT (Sneak/Jongkok)", LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT, category = "Gerakan"),
        KeyOption("LEFT CTRL (Sprint/Lari)", LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL, category = "Gerakan"),

        // Interaksi Game
        KeyOption("E (Inventory)", LwjglGlfwKeycode.GLFW_KEY_E, category = "Aksi"),
        KeyOption("Q (Drop Item)", LwjglGlfwKeycode.GLFW_KEY_Q, category = "Aksi"),
        KeyOption("F (Swap Hand)", LwjglGlfwKeycode.GLFW_KEY_F, category = "Aksi"),
        KeyOption("T (Buka Chat)", LwjglGlfwKeycode.GLFW_KEY_T, category = "Aksi"),
        KeyOption("/ (Buka Command)", LwjglGlfwKeycode.GLFW_KEY_SLASH, category = "Aksi"),
        KeyOption("ENTER (Kirim/Chat)", LwjglGlfwKeycode.GLFW_KEY_ENTER, category = "Aksi"),
        KeyOption("ESCAPE (Pause/Menu)", LwjglGlfwKeycode.GLFW_KEY_ESCAPE, category = "Aksi"),
        KeyOption("TAB (Daftar Player)", LwjglGlfwKeycode.GLFW_KEY_TAB, category = "Aksi"),

        // Fungsi Debug & Tampilan
        KeyOption("F1 (Sembunyikan HUD MC)", LwjglGlfwKeycode.GLFW_KEY_F1, category = "Fungsi"),
        KeyOption("F2 (Screenshot)", LwjglGlfwKeycode.GLFW_KEY_F2, category = "Fungsi"),
        KeyOption("F3 (Debug Layar)", LwjglGlfwKeycode.GLFW_KEY_F3, category = "Fungsi"),
        KeyOption("F5 (Ubah Perspektif)", LwjglGlfwKeycode.GLFW_KEY_F5, category = "Fungsi"),
        KeyOption("F11 (Fullscreen)", LwjglGlfwKeycode.GLFW_KEY_F11, category = "Fungsi"),

        // Hotbar Angka (1-9)
        KeyOption("Slot 1", LwjglGlfwKeycode.GLFW_KEY_1, category = "Hotbar"),
        KeyOption("Slot 2", LwjglGlfwKeycode.GLFW_KEY_2, category = "Hotbar"),
        KeyOption("Slot 3", LwjglGlfwKeycode.GLFW_KEY_3, category = "Hotbar"),
        KeyOption("Slot 4", LwjglGlfwKeycode.GLFW_KEY_4, category = "Hotbar"),
        KeyOption("Slot 5", LwjglGlfwKeycode.GLFW_KEY_5, category = "Hotbar"),
        KeyOption("Slot 6", LwjglGlfwKeycode.GLFW_KEY_6, category = "Hotbar"),
        KeyOption("Slot 7", LwjglGlfwKeycode.GLFW_KEY_7, category = "Hotbar"),
        KeyOption("Slot 8", LwjglGlfwKeycode.GLFW_KEY_8, category = "Hotbar"),
        KeyOption("Slot 9", LwjglGlfwKeycode.GLFW_KEY_9, category = "Hotbar"),

        // Huruf Tambahan
        KeyOption("B", LwjglGlfwKeycode.GLFW_KEY_B, category = "Lainnya"),
        KeyOption("C (Zoom)", LwjglGlfwKeycode.GLFW_KEY_C, category = "Lainnya"),
        KeyOption("G", LwjglGlfwKeycode.GLFW_KEY_G, category = "Lainnya"),
        KeyOption("H", LwjglGlfwKeycode.GLFW_KEY_H, category = "Lainnya"),
        KeyOption("M", LwjglGlfwKeycode.GLFW_KEY_M, category = "Lainnya"),
        KeyOption("R", LwjglGlfwKeycode.GLFW_KEY_R, category = "Lainnya"),
        KeyOption("V", LwjglGlfwKeycode.GLFW_KEY_V, category = "Lainnya"),
        KeyOption("X", LwjglGlfwKeycode.GLFW_KEY_X, category = "Lainnya"),
        KeyOption("Z", LwjglGlfwKeycode.GLFW_KEY_Z, category = "Lainnya")
    )
}
