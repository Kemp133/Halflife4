package com.halflife3.Model;

import javafx.scene.canvas.GraphicsContext;

@FunctionalInterface
public interface IRenderable {
    void render(GraphicsContext gc);
}
