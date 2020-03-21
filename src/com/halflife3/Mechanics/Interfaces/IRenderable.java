package com.halflife3.Mechanics.Interfaces;

import javafx.scene.canvas.GraphicsContext;

@FunctionalInterface
public interface IRenderable {
    void render(GraphicsContext gc);
}
