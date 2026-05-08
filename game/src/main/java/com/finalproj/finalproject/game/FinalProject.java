package com.finalproj.finalproject.game;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.scene.Node;



/**
 * The JMonkeyEngine game entry, you should only do initializations for your game here, game logic is handled by
 * Custom states {@link com.jme3.app.state.BaseAppState}, Custom controls {@link com.jme3.scene.control.AbstractControl}
 * and your custom entities implementations of the previous.
 *
 */
public class FinalProject extends SimpleApplication implements ActionListener {

    BulletAppState bulletAppState;
    BetterCharacterControl playerControl;
    Node playerNode;
    Equippable tool;

    public FinalProject() {
    }

    public FinalProject(AppState... initialStates) {
        super(initialStates);
    }

    private void initKeys() {
        inputManager.addMapping("Jump",  new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Left",  new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up",    new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down",  new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    

        inputManager.addListener(this, "Jump", "Left", "Right", "Up", "Down", "Click");
    }
    

    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false, click = false;

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Jump") && isPressed) {
            playerControl.jump();
        }

        if (name.equals("Left"))  { left  = isPressed; }
        if (name.equals("Right")) { right = isPressed; }
        if (name.equals("Up"))    { up    = isPressed; }
        if (name.equals("Down"))  { down  = isPressed; }
        if (name.equals("Click")) {
            if (tool instanceof Grapple) {
                Grapple grapple = (Grapple) tool;

                if (isPressed) {
                    grapple.shoot(cam.getDirection(), playerNode);
                } else {
                    grapple.release();
                }
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().setY(0).normalizeLocal();
        Vector3f camLeft = cam.getLeft().clone().setY(0).normalizeLocal();
        walkDirection.set(0, 0, 0);
    
        if (left)  { walkDirection.addLocal(camLeft); }
        if (right) { walkDirection.addLocal(camLeft.mult(-1f)); }
        if (down)  { walkDirection.addLocal(camDir.mult(-1f)); }
        if (up)    { walkDirection.addLocal(camDir); }
        if (click) { tool.shoot(camDir, playerNode);}
    
        // This actually moves the physical character
        playerControl.setWalkDirection(walkDirection.multLocal(15f)); 
        
        // Keep the camera locked to the player
        cam.setLocation(playerNode.getWorldTranslation().add(0, 4f, 0));
    }
    


    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        Box floorBox = new Box(10f, 0.1f, 10f);
        Geometry floorGeo = new Geometry("Floor", floorBox);
        Material floorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMat.setColor("Color", ColorRGBA.Blue);
        floorGeo.setMaterial(floorMat);
        floorGeo.setLocalTranslation(0, -0.1f, 0);
        rootNode.attachChild(floorGeo);

        RigidBodyControl floorPhy = new RigidBodyControl(0.0f);
        floorGeo.addControl(floorPhy);
        bulletAppState.getPhysicsSpace().add(floorPhy);

        playerControl = new BetterCharacterControl(1.5f, 6f, 1f);

        playerNode = new Node("PlayerNode");
        playerNode.addControl(playerControl);

        Box playerMesh = new Box(0.5f, 2.5f, 0.5f);
        Geometry playerGeo = new Geometry("PlayerVisual", playerMesh);
        Material playerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        playerMat.setColor("Color", ColorRGBA.Blue);
        playerGeo.setMaterial(playerMat);
        playerNode.attachChild(playerGeo);

        rootNode.attachChild(playerNode);
        bulletAppState.getPhysicsSpace().add(playerControl);

        playerControl.setJumpForce(new Vector3f(0, 5f, 0));
        playerControl.setGravity(new Vector3f(0,-9.8f,0));
        playerControl.warp(new Vector3f(0,5,0));


        flyCam.setMoveSpeed(0);


        tool = new Grapple(assetManager, bulletAppState, floorPhy, rootNode);

        initKeys();
    }


    public static void main(String[] args) {
        FinalProject app = new FinalProject();
        app.start();
    }
}
