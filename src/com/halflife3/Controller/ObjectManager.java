package com.halflife3.Controller;

import com.halflife3.GameObjects.GameObject;

import java.util.HashSet;

public class ObjectManager {
    private static HashSet<GameObject> gameObjects = new HashSet<>();

    public synchronized static void addObject(GameObject toAdd) {
        gameObjects.add(toAdd);
    }

    public synchronized static void removeObject(GameObject toRemove) {
        gameObjects.remove(toRemove);
    }

    public synchronized static HashSet<GameObject> getGameObjects() {
        return new HashSet<>(gameObjects);
    }

    public synchronized static void resetObjects() {
        gameObjects = new HashSet<>();
    }
}
