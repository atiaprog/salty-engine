/*
 * Copyright 2018 Malte Dostal
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.edgelord.saltyengine.example.serialization;

import de.edgelord.saltyengine.core.Game;
import de.edgelord.saltyengine.io.serialization.Serializable;
import de.edgelord.saltyengine.io.serialization.Species;

public class AdvancedSaver implements Serializable {

    private final String CAMERA_POSITION_TAG = "camPos";
    private final String COUNTER_TAG = "counter";
    private final String REDUNDANT_MESSAGE_TAG = "message";
    private int counter = 0;

    @Override
    public void serialize(final Species species) {
        species.addTag(COUNTER_TAG, ++counter);
        species.addTag(REDUNDANT_MESSAGE_TAG, "This is a redundant message to show that you don't have to encode spaces with these weird combinations of stars and underscores any more!");
        species.addTag(CAMERA_POSITION_TAG, Game.getCamera().getX() + "," + Game.getCamera().getY());
    }

    @Override
    public void deserialize(final Species species) {
        counter = Integer.parseInt(species.getTagValue(COUNTER_TAG));

        System.out.println("This example started " + counter + " times before on this computer!");
        System.out.println(species.getTagValue(REDUNDANT_MESSAGE_TAG));
    }

    @Override
    public String getDataSetName() {
        return "advancedSaver";
    }
}
