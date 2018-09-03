/*
 * Copyright (c) by Malte Dostal
 * Germany, 8.2018
 * All rights reserved
 */

package de.edgelord.saltyengine.scene;

import de.edgelord.saltyengine.gameobject.DrawingRoutine;
import de.edgelord.saltyengine.gameobject.FixedTask;
import de.edgelord.saltyengine.gameobject.GameObject;
import de.edgelord.saltyengine.gameobject.GameObjectComponent;
import de.edgelord.saltyengine.graphics.SaltyGraphics;
import de.edgelord.saltyengine.ui.UIElement;
import de.edgelord.saltyengine.ui.UISystem;
import de.edgelord.saltyengine.utils.Directions;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Scene {

    /*
    private List<GameObject> gameObjects = Collections.synchronizedList(new LinkedList<>());
    private List<FixedTask> fixedTasks = Collections.synchronizedList(new LinkedList<>());
    */
    private CopyOnWriteArrayList<GameObject> gameObjects = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<FixedTask> fixedTasks = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<DrawingRoutine> drawingRoutines = new CopyOnWriteArrayList<>();
    private UISystem ui = null;
    private float xDelta, yDelta;
    private boolean initialized = false;

    public Scene() {

    }

    public void addFixedTask(FixedTask fixedTask) {

        fixedTasks.add(fixedTask);
    }

    public void doFixedTasks() {

        synchronized (getFixedTasks()) {
            for (FixedTask fixedTask : fixedTasks) {

                fixedTask.onFixedTick();
            }
        }
    }

    public void doCollisionDetection() {

        List<GameObjectComponent> collisionComponents = new ArrayList<>();

        synchronized (getGameObjects()) {

            for (GameObject gameObject : gameObjects) {

                gameObject.doCollisionDetection(getGameObjects(), collisionComponents);
            }
        }
    }

    public void resetPosition() {

        synchronized (getGameObjects()) {
            for (GameObject gameObject : getGameObjects()) {

                gameObject.setX(gameObject.getX() + xDelta);
                gameObject.setY(gameObject.getY() + yDelta);
            }
        }
    }

    public void addGameObject(GameObject gameObject) {

        gameObjects.add(gameObject);
    }

    public void draw(SaltyGraphics saltyGraphics) {

        synchronized (getGameObjects()) {

            for (DrawingRoutine drawingRoutine : drawingRoutines) {
                if (drawingRoutine.getDrawingPosition() == DrawingRoutine.DrawingPosition.BEFORE_GAMEOBJECTS) {
                    drawingRoutine.draw(saltyGraphics);
                }
            }

            for (GameObject gameObject : gameObjects) {
                gameObject.draw(saltyGraphics);
                gameObject.doComponentDrawing(saltyGraphics);
            }

            if (ui != null) {
                ui.drawUI(saltyGraphics);
            }

            for (DrawingRoutine drawingRoutine : drawingRoutines) {
                if (drawingRoutine.getDrawingPosition() == DrawingRoutine.DrawingPosition.AFTER_GAMEOBJECTS) {
                    drawingRoutine.draw(saltyGraphics);
                }
            }
        }
    }

    public void setUI(UISystem uiSystem) {
        this.ui = uiSystem;
    }

    public UISystem getUI() {
        return ui;
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void onFixedTick() {

        doFixedTasks();
        doCollisionDetection();

        synchronized (getGameObjects()) {
            for (GameObject gameObject : getGameObjects()) {
                gameObject.doComponentOnFixedTick();
                gameObject.onFixedTick();
            }
        }

        if (ui != null) {

            ui.onFixedTick();
        }
    }

    public void onTick() {

        synchronized (getGameObjects()) {
            for (GameObject gameObject : getGameObjects()) {
                gameObject.onTick();
            }
        }
    }

    public void initGameObjects() {

        if (initialized) {

            return;
        } else {

            synchronized (getGameObjects()) {
                for (GameObject gameObject : getGameObjects()) {
                    gameObject.initialize();
                }
            }

            initialized = true;
        }
    }

    public void moveCamera(Directions.BasicDirection direction, float delta) {

        synchronized (getGameObjects()) {
            if (direction == Directions.BasicDirection.x) {

                xDelta += delta;

                for (GameObject gameObject : getGameObjects()) {

                    gameObject.setX(gameObject.getX() + delta);
                }
            } else {

                yDelta += delta;

                for (GameObject gameObject : getGameObjects()) {

                    gameObject.setY(gameObject.getY() + delta);
                }
            }
        }
    }

    public void setGameObjects(CopyOnWriteArrayList<GameObject> gameObjects) {
        this.gameObjects = gameObjects;
    }

    public List<FixedTask> getFixedTasks() {
        return fixedTasks;
    }

    public void setFixedTasks(CopyOnWriteArrayList<FixedTask> fixedTasks) {
        this.fixedTasks = fixedTasks;
    }

    public void addDrawingRoutin(DrawingRoutine drawingRoutine) {
        getDrawingRoutines().add(drawingRoutine);
    }

    public CopyOnWriteArrayList<DrawingRoutine> getDrawingRoutines() {
        return drawingRoutines;
    }

    public void setDrawingRoutines(CopyOnWriteArrayList<DrawingRoutine> drawingRoutines) {
        this.drawingRoutines = drawingRoutines;
    }
}