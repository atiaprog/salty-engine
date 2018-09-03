/*
 * Copyright (c) by Malte Dostal
 * Germany, 8.2018
 * All rights reserved
 */

package de.edgelord.saltyengine.gameobject;

import de.edgelord.saltyengine.core.event.CollisionEvent;
import de.edgelord.saltyengine.gameobject.components.*;
import de.edgelord.saltyengine.graphics.SaltyGraphics;
import de.edgelord.saltyengine.hitbox.SimpleHitbox;
import de.edgelord.saltyengine.transform.Coordinates;
import de.edgelord.saltyengine.transform.Dimensions;
import de.edgelord.saltyengine.transform.Transform;
import de.edgelord.saltyengine.transform.Vector2f;
import de.edgelord.saltyengine.utils.Directions;
import de.edgelord.stdf.Species;
import de.edgelord.stdf.reading.DataReader;
import de.edgelord.stdf.reading.ValueToListConverter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class GameObject {

    public static final String DEFAULT_PHYSICS_NAME = "de.edgelord.saltyengine.coreComponents.physics";
    public static final String DEFAULT_RECALCULATE_HITBOX_NAME = "de.edgelord.saltyengine.coreComponents.recalculateHitbox";
    public static final String DEFAULT_RECALCULATE_MIDDLE_NAME = "de.edgelord.saltyengine.coreComponents.recalculateMiddle";
    public static final String DEFAULT_ACCELERATOR_NAME = "de.edgelord.saltyengine.coreComponents.accelerator";

    private final List<GameObjectComponent> components = new LinkedList<>();

    private final SimplePhysicsComponent physicsComponent;
    private final RecalculateHitboxComponent recalculateHitboxComponent;
    private final RecalculateMiddleComponent recalculateMiddleComponent;
    private final Accelerator defaultAccelerator;

    private Directions.Direction lastDirection = null;

    private Transform transform;
    private Vector2f middle;
    private String tag;
    private HashMap<String, String> properties = new HashMap<>();
    private File propertiesFile;
    private SimpleHitbox hitbox;
    private float mass = 1f;

    public GameObject(final float xPos, final float yPos, final float width, final float height, final String tag) {

        transform = new Transform(new Vector2f(xPos, yPos), new Dimensions(width, height));
        hitbox = new SimpleHitbox(this, getWidth(), getHeight(), 0, 0);
        this.tag = tag;

        middle = new Vector2f(getCoordinates().getX() + getWidth() / 2, getCoordinates().getY() + getHeight() / 2);

        physicsComponent = new SimplePhysicsComponent(this, GameObject.DEFAULT_PHYSICS_NAME);
        recalculateHitboxComponent = new RecalculateHitboxComponent(this, GameObject.DEFAULT_RECALCULATE_HITBOX_NAME);
        recalculateMiddleComponent = new RecalculateMiddleComponent(this, GameObject.DEFAULT_RECALCULATE_MIDDLE_NAME);
        defaultAccelerator = new Accelerator(this, GameObject.DEFAULT_ACCELERATOR_NAME);

        components.add(physicsComponent);
        components.add(recalculateHitboxComponent);
        components.add(recalculateMiddleComponent);
        components.add(defaultAccelerator);
    }

    public GameObject(Transform transform, String tag) {
        this(transform.getX(), transform.getY(), transform.getWidth(), transform.getHeight(), tag);
    }

    public GameObject(Coordinates coordinates, Dimensions dimensions, String tag) {
        this(coordinates.getX(), coordinates.getY(), dimensions.getWidth(), dimensions.getHeight(), tag);
    }

    public GameObject(Vector2f position, Dimensions dimensions, String tag) {
        this(position.getX(), position.getY(), dimensions.getWidth(), dimensions.getHeight(), tag);
    }

    public abstract void initialize();

    public abstract void onCollision(CollisionEvent event);

    public abstract void onFixedTick();

    public abstract void onTick();

    public abstract void draw(SaltyGraphics saltyGraphics);

    /**
     * This method can be overridden but It's not necessary and you won't need this nearly always, so it's not abstract
     * @param collisions the detected collisions of this run
     */
    public void onCollisionDetectionFinish(List<CollisionEvent> collisions) {

    }

    public void addComponent(final GameObjectComponent gameObjectComponent) {

        components.add(gameObjectComponent);
    }

    public void doComponentOnFixedTick() {

        for (final GameObjectComponent gameObjectComponent : components) {

            if (gameObjectComponent.isEnabled()) {
                gameObjectComponent.onFixedTick();
            }
        }
    }

    public void doComponentDrawing(final SaltyGraphics saltyGraphics) {

        for (final GameObjectComponent gameObjectComponent : components) {

            if (gameObjectComponent.isEnabled()) {
                gameObjectComponent.draw(saltyGraphics);
            }
        }
    }

    public void doCollisionDetection(final List<GameObject> gameObjects, final List<GameObjectComponent> collisionComponenets) {

        Directions collisionDirections = new Directions();
        List<CollisionEvent> collisions = new ArrayList<>();

        for (final GameObject other : gameObjects) {

            if (other == this) {
                continue;
            }

            if (getHitbox().collides(other)) {

                Directions.appendGameObjectRelation(this, other, collisionDirections);

                // final CollisionEvent e = new CollisionEvent(other, collisionDirections);
                final CollisionEvent eSelf = new CollisionEvent(other, collisionDirections);


                collisions.add(eSelf);
                // other.onCollision(e);
                onCollision(eSelf);

                for (final GameObjectComponent component : getComponents()) {
                    component.onCollision(eSelf);
                    collisionComponenets.add(component);
                }

                /*
                for (final GameObjectComponent component : other.getComponents()) {
                    if (!component.getTag().equals(GameObjectComponent.PUSH_OUT_ON_COLLISION)) {
                        component.onCollision(e);
                    }
                }
                */
            } else {
                final CollisionEvent eSelf = new CollisionEvent(other, new Directions());
                getPhysics().onCollision(eSelf);
            }
        }

        onCollisionDetectionFinish(collisions);
    }

    public void removeComponent(final String name) {
        components.removeIf(gameObjectComponent -> gameObjectComponent.getName().equals(name));
    }

    public void initPropertiesFile(final File file) {

        propertiesFile = file;
    }

    public void addProperty(final String key, final String value) {

        getProperties().put(key, value);
    }

    public void changeProperty(final String key, final String newValue) {

        getProperties().replace(key, getProperties().get(key), newValue);
    }

    public String getLocalProperty(final String key) {

        return getProperties().get(key);
    }

    public int getPropertyAsInteger(final String key) {

        return Integer.valueOf(getLocalProperty(key));
    }

    public String readProperty(final String property) throws IOException {

        final DataReader propertiesReader = new DataReader(propertiesFile);

        return propertiesReader.getTagValue(property);
    }

    public void syncPropertiesToFile() {

    }

    public void readKeyProperties() throws IOException {

        final DataReader propertiesReader = new DataReader(propertiesFile);
        final Species keyProperties = propertiesReader.getSpecies("keyProperties");

        if (keyProperties.getContent().contains("location")) {

            final List<Integer> readenCoordinates = ValueToListConverter.convertToIntegerList(keyProperties, "location", ",");
            setPosition(new Vector2f(readenCoordinates.get(0), readenCoordinates.get(1)));
        }
    }

    public void basicMove(final float delta, final Directions.BasicDirection direction) {

        if (direction == Directions.BasicDirection.x) {
            setX(getX() + delta);
        } else {
            setY(getY() + delta);
        }
    }

    public void move(float delta, final Directions.Direction direction) {

        if (delta != 0) {
            lastDirection = direction;
        }

        // Check if delta is negative and if so, mirror its value
        if (delta < 0f) {
            delta = delta * (-1);
        }

        switch (direction) {

            case RIGHT:
                basicMove(delta, Directions.BasicDirection.x);
                break;
            case LEFT:
                basicMove(-delta, Directions.BasicDirection.x);
                break;
            case UP:
                basicMove(-delta, Directions.BasicDirection.y);
                break;
            case DOWN:
                basicMove(delta, Directions.BasicDirection.y);
                break;
        }
    }

    public void moveY(final float delta) {
        transform.setY(getY() + delta);
    }

    public void moveX(final float delta) {
        transform.setX(getX() + delta);
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(final HashMap<String, String> properties) {
        this.properties = properties;
    }

    public Coordinates getCoordinates() {
        return transform.getCoordinates();
    }

    public float getWidth() {
        return transform.getWidth();
    }

    public int getWidthAsInt() {
        return transform.getWidthAsInt();
    }

    public void setWidth(final float width) {
        transform.setWidth(width);
    }

    public float getHeight() {
        return transform.getHeight();
    }

    public int getHeightAsInt() {
        return transform.getHeightAsInt();
    }

    public void setHeight(final float height) {
        transform.setHeight(height);
    }

    public SimpleHitbox getHitbox() {
        return hitbox;
    }

    public void setHitbox(final SimpleHitbox hitbox) {
        this.hitbox = hitbox;
    }

    public Vector2f getPosition() {
        return transform.getPosition();
    }

    public void setPosition(final Vector2f position) {
        transform.setPosition(position);
    }

    public float getX() {
        return getPosition().getX();
    }

    public void setX(final float x) {

        getPosition().setX(x);
    }

    public float getY() {

        return getPosition().getY();
    }

    public void setY(final float y) {

        getPosition().setY(y);
    }

    public List<GameObjectComponent> getComponents() {
        return components;
    }

    public SimplePhysicsComponent getPhysics() {
        return physicsComponent;
    }

    public RecalculateHitboxComponent getRecalculateHitboxComponent() {
        return recalculateHitboxComponent;
    }

    public Accelerator getDefaultAccelerator() {
        return defaultAccelerator;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public Vector2f getMiddle() {
        return middle;
    }

    /**
     * WARNING!! THIS METHOD WON'T PLACE THE GAMEOBJECT BY ITS MIDDLE BUT SET THE MIDDLE WITHOUT CHANGING THE POSITION!
     * Usually, you should not call this method manually!
     * @param middle the new middle
     */
    public void setMiddle(final Vector2f middle) {
        this.middle = middle;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(final float mass) {
        this.mass = mass;
    }

    public Directions.Direction getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(final Directions.Direction lastDirection) {
        this.lastDirection = lastDirection;
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }
}