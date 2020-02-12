package com.halflife3.Controller;

import com.halflife3.Model.GameObject;

import java.util.HashSet;

public class ObjectManager {
    private HashSet<GameObject> gameObjects = new HashSet<>();

    public ObjectManager() { }

    public void addObject(GameObject toAdd) {
        gameObjects.add(toAdd);
    }

    public void removeObject(GameObject toRemove) {
        gameObjects.remove(toRemove);
    }

    public HashSet<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void setGameObjects(HashSet<GameObject> toSet) {
        this.gameObjects = toSet;
    }
}
