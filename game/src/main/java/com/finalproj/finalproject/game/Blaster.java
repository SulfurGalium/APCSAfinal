package com.finalproj.finalproject.game;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Blaster implements Equippable
{
    private AssetManager assetManager;
    private BulletAppState bulletAppState;
    private RigidBodyControl staticHolder;
    private Node anchorNode;
    private RigidBodyControl playerPhys;
    private Node rootNode;
    private SixDofJoint ropeJoint;

    public Blaster(AssetManager a, BulletAppState b, RigidBodyControl r, Node rn) {
        assetManager = a;
        bulletAppState = b;
        playerPhys = r;
        rootNode = rn;
    }


    public void shoot(Vector3f direction, Node player) {

        CollisionResults results = new CollisionResults();

        Ray ray = new Ray(player.getWorldTranslation(), direction);
        rootNode.collideWith(ray, results);

        CollisionResult hit = null;

        for (CollisionResult r : results) {
            if (!r.getGeometry().hasAncestor(player)) {
                hit = r;
                break;
            }
        }

        if (hit == null) {
            return;
        }
  
        if (results.size() > 0) {
            //CollisionResult closest = results.getClosestCollision();
            //Vector3f hookPoint = closest.getContactPoint();

            Vector3f pushoff = hit.getContactPoint();

            float radius = pushoff.distance(player.getLocalTranslation());
            
            RigidBodyControl anchorPhys = new RigidBodyControl(0.0f);
            anchorNode.addControl(anchorPhys);
            bulletAppState.getPhysicsSpace().add(anchorPhys);
            
            BetterCharacterControl playerControl = player.getControl(BetterCharacterControl.class);
            if (playerControl == null) {
                System.err.println("is player init?");
                return;
            }
            PhysicsRigidBody playerBody = playerControl.getRigidBody();

            Node physicsNode = new Node("Physics Node");

            RigidBodyControl rigidBody = new RigidBodyControl(1.0f);
            PhysicsNode.addControl(rigidBody);

            bulletAppState.getPhysicsSpace().add(rigidBody);
            anchorNode = new Node("AnchorNode");
            rootNode.attachChild(anchorNode);
            anchorNode.setLocalTranslation(push);
            
            RigidBodyControl anchorPhys = new RigidBodyControl(0.0f);
            anchorNode.addControl(anchorPhys);
            bulletAppState.getPhysicsSpace().add(anchorPhys);
            
            BetterCharacterControl playerControl = player.getControl(BetterCharacterControl.class);
            if (playerControl == null) {
                System.err.println("is player init?");
                return;
            }
            PhysicsRigidBody playerBody = playerControl.getRigidBody();

        }



    }

    public void equip() {

    }
}
