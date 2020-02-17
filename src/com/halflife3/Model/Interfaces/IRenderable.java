package com.halflife3.Model.Interfaces;

import javafx.scene.canvas.GraphicsContext;

@FunctionalInterface
public interface IRenderable {
    void render(GraphicsContext gc);
}
