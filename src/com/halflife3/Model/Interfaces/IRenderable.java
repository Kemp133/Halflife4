package com.halflife3.Model.Interfaces;

import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;

@FunctionalInterface
public interface IRenderable {
    void render(GraphicsContext gc, Vector2 offset);
}
