package com.finalproj.finalproject.game;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.shape.Line;
import com.jme3.math.Ray;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;


public class Grapple implements Equippable
{
    private AssetManager assetManager;
    private BulletAppState bulletAppState;
    private RigidBodyControl staticHolder;
    private Node anchorNode;
    private RigidBodyControl playerPhys;


    public Grapple(AssetManager a, BulletAppState b, RigidBodyControl r) {
        assetManager = a;
        bulletAppState = b;
        playerPhys = r;
    }


    public void shoot(Vector3f angle, Node player) {

        CollisionResults results = new CollisionResults();

        Ray ray = new Ray(player.getLocalTranslation(), angle);

        player.collideWith(ray, results);

        System.out.println("shot...");

        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            Vector3f hookPoint = closest.getContactPoint();

            Line line = new Line(player.getWorldTranslation(), hookPoint);
            Geometry ropeGeom = new Geometry("Rope", line);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.White);
            ropeGeom.setMaterial(mat);
            player.attachChild(ropeGeom);

            staticHolder = new RigidBodyControl(0.0f); 
            anchorNode = new Node("AnchorNode");

            anchorNode.addControl(staticHolder);
            player.attachChild(anchorNode);
            bulletAppState.getPhysicsSpace().add(staticHolder);

            SixDofJoint ropeJoint = new SixDofJoint(playerPhys, staticHolder, 
                        Vector3f.ZERO, Vector3f.ZERO, true);

            Vector3f lowerLimit = new Vector3f(0, 0, 0);
            Vector3f upperLimit = new Vector3f(10, 10, 10); //make it not hardcoded
            
            ropeJoint.setLinearLowerLimit(lowerLimit);
            ropeJoint.setLinearUpperLimit(upperLimit);
            
            bulletAppState.getPhysicsSpace().add(ropeJoint);


            System.out.println("hit!");
        }



    }

    public void equip() {

    }
}
