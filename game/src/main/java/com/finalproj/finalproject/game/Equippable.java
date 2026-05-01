package com.finalproj.finalproject.game;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public interface Equippable
{
    public void shoot(Vector3f angle, Node player);
    public void equip();
}
