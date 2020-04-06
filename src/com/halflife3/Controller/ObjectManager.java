package com.halflife3.Controller;

import com.halflife3.GameObjects.GameObject;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

public class ObjectManager {
    private static AtomicReference<HashSet<GameObject>> atomicObjects = new AtomicReference<>();
    private static HashSet<GameObject> gameObjects = new HashSet<>();

    public static void addObject(GameObject toAdd) {
        while (true) {
            HashSet<GameObject> existingSet = getGameObjects();
            gameObjects.add(toAdd);
            if (atomicObjects.compareAndSet(existingSet, gameObjects))
                return;
        }
    }

    public static void removeObject(GameObject toRemove) {
        while (true) {
            HashSet<GameObject> existingSet = getGameObjects();
            gameObjects.remove(toRemove);
            if (atomicObjects.compareAndSet(existingSet, gameObjects))
                return;
        }
    }

    public static HashSet<GameObject> getGameObjects() {
        return atomicObjects.get();
    }
}
