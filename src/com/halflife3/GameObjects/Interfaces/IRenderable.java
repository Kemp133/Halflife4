package com.halflife3.GameObjects.Interfaces;

import javafx.scene.canvas.GraphicsContext;

@FunctionalInterface
public interface IRenderable {
    void render(GraphicsContext gc);
}
