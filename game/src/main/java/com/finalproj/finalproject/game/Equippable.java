package com.finalproj.finalproject.game;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * The Equippable interface which all tools implement
 * @authors Shreyan Ganguly, Aarush Jain
 */
public interface Equippable
{
    public void shoot(Vector3f angle, Node player);
    public void equip();
}
