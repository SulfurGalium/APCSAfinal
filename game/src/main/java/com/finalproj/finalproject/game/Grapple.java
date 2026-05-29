package com.finalproj.finalproject.game;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;

/**
 * The grapple hook that reels the player in
 * @authors Shreyan Ganguly, Aarush Jain
 */
public class Grapple implements Equippable
{
    private AssetManager assetManager;
    private BulletAppState bulletAppState;
    private RigidBodyControl staticHolder;
    private Node anchorNode;
    private RigidBodyControl playerPhys;
    private Node rootNode;
    private SixDofJoint ropeJoint;
    private Line ropeLine;
    private Geometry ropeLineGeometry;
    private Vector3f hookPoint;
    private boolean isGrappling = false;
    
    
    /**
     * Creates the tool and allows it to interact with the physics space
     * @param a
     * @param b
     * @param r
     * @param rn 
     */
    public Grapple(AssetManager a, BulletAppState b, RigidBodyControl r, Node rn) {
        assetManager = a;
        bulletAppState = b;
        playerPhys = r;
        rootNode = rn;
    }

    /**
     * Shoots the grapple at set distance then reels the player in at that angle
     * @param direction
     * @param player 
     */
    public void shoot(Vector3f direction, Node player) {
        if (isGrappling) {
            updateRopeLine(player);
            return;
        }

        CollisionResults results = new CollisionResults();

        Vector3f normDirection = direction.normalize();

        Ray ray = new Ray(player.getWorldTranslation(), normDirection);
        ray.setLimit(65f);
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

            hookPoint = hit.getContactPoint();


            anchorNode = new Node("AnchorNode");
            rootNode.attachChild(anchorNode);
            anchorNode.setLocalTranslation(hookPoint);
            
            RigidBodyControl anchorPhys = new RigidBodyControl(0.0f);
            anchorNode.addControl(anchorPhys);
            bulletAppState.getPhysicsSpace().add(anchorPhys);
            
            BetterCharacterControl playerControl = player.getControl(BetterCharacterControl.class);
            if (playerControl == null) {
                System.err.println("is player init?");
                return;
            }
            PhysicsRigidBody playerBody = playerControl.getRigidBody();

            ropeJoint = new SixDofJoint(anchorPhys, playerBody, Vector3f.ZERO, new Vector3f(0, 1f, 0), true);

            ropeJoint.getTranslationalLimitMotor().setLowerLimit(Vector3f.ZERO);
            ropeJoint.getTranslationalLimitMotor().setUpperLimit(Vector3f.ZERO);

            ropeJoint.getRotationalLimitMotor(0).setLowerLimit(-FastMath.PI);
            ropeJoint.getRotationalLimitMotor(0).setUpperLimit(FastMath.PI);

            ropeJoint.getRotationalLimitMotor(1).setLowerLimit(-FastMath.PI);
            ropeJoint.getRotationalLimitMotor(1).setUpperLimit(FastMath.PI);

            ropeJoint.getRotationalLimitMotor(2).setLowerLimit(-FastMath.PI);
            ropeJoint.getRotationalLimitMotor(2).setUpperLimit(FastMath.PI);

            float distance = player.getWorldTranslation().distance(hookPoint);
            //ropeJoint.setLinearUpperLimit(new Vector3f(distance, distance, distance));
            //ropeJoint.setLinearLowerLimit(new Vector3f(-distance, -distance, -distance));

            //ropeJoint.setAngularUpperLimit(new Vector3f(FastMath.HALF_PI, FastMath.HALF_PI, FastMath.HALF_PI));
            //ropeJoint.setAngularLowerLimit(new Vector3f(-FastMath.HALF_PI, -FastMath.HALF_PI, -FastMath.HALF_PI));

            bulletAppState.getPhysicsSpace().add(ropeJoint);
            createRopeLine(player);

            System.out.println("Hooked at: " + distance);

            isGrappling = true;
        }
    }

    private void createRopeLine(Node player) {
        ropeLine = new Line(player.getWorldTranslation(), hookPoint);
        ropeLine.setLineWidth(3f);

        ropeLineGeometry = new Geometry("GrappleLine", ropeLine);
        Material ropeMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        ropeMat.setColor("Color", ColorRGBA.Yellow);
        ropeLineGeometry.setMaterial(ropeMat);
        rootNode.attachChild(ropeLineGeometry);
    }

    private void updateRopeLine(Node player) {
        if (ropeLine != null && hookPoint != null) {
            ropeLine.updatePoints(player.getWorldTranslation(), hookPoint);
        }
    }

    /**
     * The method that happens when the tool is equipped
     */
    public void equip() {}
    
    
    /**
     * The method to delete and reset the grapple after releasing it
     */
    public void release() {
        if (!isGrappling) {
            return;
        }

        if (ropeJoint != null) {
            bulletAppState.getPhysicsSpace().remove(ropeJoint);
        }

        if (anchorNode != null) {
            RigidBodyControl anchorPhys = anchorNode.getControl(RigidBodyControl.class);
            if (anchorPhys != null) {
                bulletAppState.getPhysicsSpace().remove(anchorPhys);
            }
            anchorNode.removeFromParent();
        }

        if (ropeLineGeometry != null) {
            ropeLineGeometry.removeFromParent();
            ropeLineGeometry = null;
            ropeLine = null;
        }

        hookPoint = null;
        isGrappling = false;
    }
}
