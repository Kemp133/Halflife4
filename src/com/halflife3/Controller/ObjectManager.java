package com.halflife3.Controller;

import com.halflife3.Model.GameObject;

import java.util.HashSet;

public class ObjectManager  {

    private static HashSet<GameObject> gameObjects = new HashSet<>();

    public ObjectManager() { }

    public static void addObject(GameObject toAdd) {
        gameObjects.add(toAdd);
    }

    public static void removeObject(GameObject toRemove) {
        gameObjects.remove(toRemove);
    }

    public static HashSet<GameObject> getGameObjects() {
        return gameObjects;
    }

    public static void setGameObjects(HashSet<GameObject> toSet) {
        gameObjects = toSet;
    }
}
