package com.halflife3.Controller;

import com.halflife3.GameObjects.GameObject;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

public class ObjectManager {
    private static AtomicReference<HashSet<GameObject>> atomicObjects = new AtomicReference<>();
    private static HashSet<GameObject> gameObjects = new HashSet<>();

    public static void addObject(GameObject toAdd) {
        gameObjects.add(toAdd);
        while (true) {
            HashSet<GameObject> existingSet = getGameObjects();
            if (atomicObjects.compareAndSet(existingSet, gameObjects))
                return;
        }
    }

    public static void removeObject(GameObject toRemove) {
        gameObjects.remove(toRemove);
        while (true) {
            HashSet<GameObject> existingSet = getGameObjects();
            if (atomicObjects.compareAndSet(existingSet, gameObjects))
                return;
        }
    }

    public static HashSet<GameObject> getGameObjects() {
        return atomicObjects.get();
    }

    public static void resetObjects() {
        gameObjects = new HashSet<>();
        while (true) {
            HashSet<GameObject> existingSet = getGameObjects();
            if (atomicObjects.compareAndSet(existingSet, gameObjects))
                return;
        }
    }
}
