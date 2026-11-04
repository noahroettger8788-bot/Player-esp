package com.example.playeresp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class PlayerEspClient implements ClientModInitializer {
    private static boolean enabled = true;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.playeresp.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H,
                "category.playeresp"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) enabled = !enabled;
        });
        WorldRenderEvents.LAST.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!enabled || client.world == null || client.player == null) return;

            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();
            matrices.push();
            matrices.translate(-camera.x, -camera.y, -camera.z);

            VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
            client.world.getPlayers().forEach(player -> {
                if (player == client.player || !player.isAlive()) return;
                Box box = player.getBoundingBox().expand(0.035);
                WorldRenderer.drawBox(matrices, lines, box, 0.2f, 0.9f, 1.0f, 1.0f);
            });
            matrices.pop();
        });
    }
}
