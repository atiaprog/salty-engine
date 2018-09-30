/*
 * Copyright (c) by Malte Dostal
 * Germany, 8.2018
 * All rights reserved
 */

package de.edgelord.saltyengine.components;

import de.edgelord.saltyengine.core.Component;
import de.edgelord.saltyengine.core.event.CollisionEvent;
import de.edgelord.saltyengine.gameobject.Components;
import de.edgelord.saltyengine.gameobject.GameObject;
import de.edgelord.saltyengine.graphics.SaltyGraphics;

public class Accelerator extends Component<GameObject> {

    private long ticks;
    private long duration;
    private String forceName;
    private boolean accelerationFinished = true;

    public Accelerator(GameObject parent, String name) {
        super(parent, name, Components.ACCELERATOR_COMPONENT);
    }

    @Override
    public void onFixedTick() {

        if (!accelerationFinished) {
            ticks++;

            if (ticks >= duration) {

                getParent().getPhysics().getForce(forceName).setAcceleration(0f);
                ticks = 0;
                duration = 0;
                accelerationFinished = true;
            }
        }
    }

    @Override
    public void draw(SaltyGraphics saltyGraphics) {

    }

    @Override
    public void onCollision(CollisionEvent e) {

    }

    public void accelerate(String forceName, float acceleration, long duration) {
        getParent().getPhysics().getForce(forceName).setAcceleration(acceleration);

        this.accelerationFinished = false;
        this.forceName = forceName;
        this.duration = duration;
    }
}