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
    private Node rootNode;


    public Grapple(AssetManager a, BulletAppState b, RigidBodyControl r, Node rn) {
        assetManager = a;
        bulletAppState = b;
        playerPhys = r;
        rootNode = rn;
    }


    public void shoot(Vector3f direction, Node player) {
        CollisionResults results = new CollisionResults();

        Ray ray = new Ray(player.getWorldTranslation(), direction);
        rootNode.collideWith(ray, results);

        
        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            Vector3f hookPoint = closest.getContactPoint();

            anchorNode = new Node("AnchorNode");
            rootNode.attachChild(anchorNode);
            anchorNode.setLocalTranslation(hookPoint);
            
            RigidBodyControl anchorPhys = new RigidBodyControl(0.0f);
            anchorNode.addControl(anchorPhys);
            
            float ropeLength = player.getWorldTranslation().distance(hookPoint);
        
            Line line = new Line(player.getWorldTranslation(), hookPoint);
            Geometry ropeGeom = new Geometry("Rope", line);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.White);
            ropeGeom.setMaterial(mat);
            
            rootNode.attachChild(ropeGeom); 
        

        
            staticHolder = new RigidBodyControl(0.0f); 
            anchorNode.addControl(staticHolder);
            bulletAppState.getPhysicsSpace().add(staticHolder);
        


            Vector3f pivotA = Vector3f.ZERO;
            Vector3f pivotB = Vector3f.ZERO;
        
            SixDofJoint ropeJoint = new SixDofJoint(playerPhys, staticHolder, pivotA, pivotB, false);
        
            ropeJoint.setLinearLowerLimit(new Vector3f(0, 0, 0));
            ropeJoint.setLinearUpperLimit(new Vector3f(ropeLength, ropeLength, ropeLength));
            
            bulletAppState.getPhysicsSpace().add(anchorPhys);

            RangeOfMotion.setupJoint(anchorPhys, player.getControl(RigidBodyControl.class));
        
            System.out.println("Hooked at dist: " + ropeLength);
        }



    }

    public void equip() {

    }
}
