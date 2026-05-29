package com.finalproj.finalproject.game;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;

/**
 * The JMonkeyEngine game entry, you should only do initializations for your game here, game logic is handled by
 * Custom states {@link com.jme3.app.state.BaseAppState}, Custom controls {@link com.jme3.scene.control.AbstractControl}
 * and your custom entities implementations of the previous.
 *
 * @author JMonkey, Shreyan Ganguly, Aarush Jain
 */
public class FinalProject extends SimpleApplication implements ActionListener {

    BulletAppState bulletAppState;
    BetterCharacterControl playerControl;
    Node playerNode;
    Equippable tool;
    
    private static final Vector3f START_LOCATION = new Vector3f(0, 5f, 0);
    private static final float DEATH_HEIGHT = -20f;
    private static final float TOP_PLATFORM_HEIGHT = 270f;
    private static final Vector3f PLATFORM_EXTENTS = new Vector3f(10f, 3f, 10f);

    private float gameTimer = 0.0f;
    private boolean timerRunning = true;
    private boolean finishedRun = false;
    private Scores scores = new Scores();
    
    // UI elements
    private BitmapText timerHudText;
    private BitmapText scoresHudText;
    
    /**
     * The base Constructor with no arguments
     */
    public FinalProject() {
    }
    
    /**
     * The base Constructor with arguments
     * @param initialStates 
     */
    public FinalProject(AppState... initialStates) {
        super(initialStates);
    }
    
    /**
     * Maps actions to keys and adds an eventlistener for those events
     */
    private void initKeys() {
        inputManager.addMapping("Jump",  new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Left",  new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up",    new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down",  new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Shift", new KeyTrigger(KeyInput.KEY_LSHIFT));
    
        inputManager.addListener(this, "Jump", "Left", "Right", "Up", "Down", "Click", "Shift");
    }

    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false, click = false, shift = false;
    private final float WALK_SPEED = 10f;
    private final float SPRINT_SPEED = 25f;
    
    /**
     * Determines what to do depending on the input
     */
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Jump") && isPressed) {
            playerControl.jump();
        }

        if (name.equals("Left"))  { left  = isPressed; }
        if (name.equals("Right")) { right = isPressed; }
        if (name.equals("Up"))    { up    = isPressed; }
        if (name.equals("Down"))  { down  = isPressed; }
        if (name.equals("Shift")) { shift = isPressed; }
        if (name.equals("Click")) {
            click = isPressed;
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
    
    /**
     * Handles most of the physics and allows the player to move
     */
    @Override
    public void simpleUpdate(float tpf) {
        if (timerRunning) {
            gameTimer += tpf;
        }
        
        updateHud();

        Vector3f camDir = cam.getDirection().clone().setY(0).normalizeLocal();
        Vector3f camLeft = cam.getLeft().clone().setY(0).normalizeLocal();
        walkDirection.set(0, 0, 0);
        float speed = WALK_SPEED;
    
        if (left)  { walkDirection.addLocal(camLeft); }
        if (right) { walkDirection.addLocal(camLeft.mult(-1f)); }
        if (down)  { walkDirection.addLocal(camDir.mult(-1f)); }
        if (up)    { walkDirection.addLocal(camDir); }
        if (click) { tool.shoot(camDir, playerNode); }
        if (shift) { speed = SPRINT_SPEED; }
    
        // moves the physical character
        playerControl.setWalkDirection(walkDirection.multLocal(speed)); 
        
        // Keep the camera locked to the player
        cam.setLocation(playerNode.getWorldTranslation().add(0, 4f, 0));
        
        float playerHeight = playerNode.getWorldTranslation().y;
        if (playerHeight < DEATH_HEIGHT) {
            resetRun();
        } else if (!finishedRun && playerHeight >= TOP_PLATFORM_HEIGHT) {
            finishRun();
        }
    }

    private void resetRun() {
        playerControl.warp(START_LOCATION);
        gameTimer = 0.0f;
        timerRunning = true;
        finishedRun = false;
        System.out.println("Player fell! Position and timer reset.");
    }

    private void finishRun() {
        timerRunning = false;
        finishedRun = true;
        scores.add(gameTimer);
        updateHud();
        System.out.println(String.format("Finished run in %.2fs", gameTimer));
    }

    private void updateHud() {
        if (timerHudText != null) {
            String timerState = finishedRun ? "Finished" : "Time";
            timerHudText.setText(String.format("%s: %.2fs", timerState, gameTimer));
        }

        if (scoresHudText != null) {
            StringBuilder scoreText = new StringBuilder("Top 10 Times\n");
            java.util.List<Float> topTimes = scores.getTopTenTimes();
            if (topTimes.isEmpty()) {
                scoreText.append("No finishes yet");
            } else {
                for (int i = 0; i < topTimes.size(); i++) {
                    scoreText.append(String.format("%d. %.2fs", i + 1, topTimes.get(i)));
                    if (i < topTimes.size() - 1) {
                        scoreText.append("\n");
                    }
                }
            }
            scoresHudText.setText(scoreText.toString());
        }
    }
    
    /**
     * Initializes the 2D UI overlay for the timer
     */
    private void initTimerHud() {
        // Load default engine font
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        timerHudText = new BitmapText(guiFont);
        scoresHudText = new BitmapText(guiFont);
        
        // Configuration settings
        timerHudText.setSize(guiFont.getCharSet().getRenderedSize() * 1.5f); // Scale font size up slightly
        timerHudText.setColor(ColorRGBA.White);
        scoresHudText.setSize(guiFont.getCharSet().getRenderedSize() * 1.1f);
        scoresHudText.setColor(ColorRGBA.White);
        
        // Position it at the top left corner of the window
        float padding = 20f;
        timerHudText.setLocalTranslation(padding, settings.getHeight() - padding, 0);
        scoresHudText.setLocalTranslation(padding, settings.getHeight() - padding - 40f, 0);
        updateHud();
        
        // Attach elements to the 2D layout space
        guiNode.attachChild(timerHudText);
        guiNode.attachChild(scoresHudText);
    }

    private Geometry createBox(String name, Vector3f extents, Vector3f location, Material material) {
        Geometry geometry = new Geometry(name, new Box(extents.x, extents.y, extents.z));
        geometry.setMaterial(material);
        geometry.setLocalTranslation(location);
        return geometry;
    }

    private RigidBodyControl addStaticPlatform(String name, Vector3f location, Material material) {
        Geometry platform = createBox(name, PLATFORM_EXTENTS, location, material);
        rootNode.attachChild(platform);

        RigidBodyControl platformPhy = new RigidBodyControl(0.0f);
        platform.addControl(platformPhy);
        bulletAppState.getPhysicsSpace().add(platformPhy);
        return platformPhy;
    }

    private RigidBodyControl initPlatforms() {
        Material platformMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        platformMat.setTexture("ColorMap", assetManager.loadTexture("Textures/texture.png"));

        RigidBodyControl centralPlatform = addStaticPlatform("CentralPlatform", new Vector3f(0, -1f, 0), platformMat);
        addStaticPlatform("Platform01", new Vector3f(10f, 10f, 20f), platformMat);
        addStaticPlatform("Platform02", new Vector3f(20f, 70f, 30f), platformMat);
        addStaticPlatform("Platform03", new Vector3f(40f, 43f, 80f), platformMat);
        addStaticPlatform("Platform04", new Vector3f(30f, 53f, 70f), platformMat);
        addStaticPlatform("Platform05", new Vector3f(80f, 33f, 40f), platformMat);
        addStaticPlatform("Platform06", new Vector3f(90f, 17f, 58f), platformMat);
        addStaticPlatform("Platform07", new Vector3f(60f, 60f, 78f), platformMat);
        addStaticPlatform("Platform08", new Vector3f(50f, 50f, 28f), platformMat);
        addStaticPlatform("Platform09", new Vector3f(70f, 26f, 10f), platformMat);
        addStaticPlatform("Platform10", new Vector3f(30f, 29f, 50f), platformMat);
        addStaticPlatform("Platform11", new Vector3f(60f, 40f, 60f), platformMat);
        addStaticPlatform("Platform12", new Vector3f(50f, 80f, 78f), platformMat);
        addStaticPlatform("TopPlatform", new Vector3f(20f, 90f, 30f), platformMat);
        addStaticPlatform("Stack2CentralPlatform", new Vector3f(0, 89f, 0), platformMat);
        addStaticPlatform("Stack2Platform01", new Vector3f(10f, 100f, 20f), platformMat);
        addStaticPlatform("Stack2Platform02", new Vector3f(20f, 160f, 30f), platformMat);
        addStaticPlatform("Stack2Platform03", new Vector3f(40f, 133f, 80f), platformMat);
        addStaticPlatform("Stack2Platform04", new Vector3f(30f, 143f, 70f), platformMat);
        addStaticPlatform("Stack2Platform05", new Vector3f(80f, 123f, 40f), platformMat);
        addStaticPlatform("Stack2Platform06", new Vector3f(90f, 107f, 58f), platformMat);
        addStaticPlatform("Stack2Platform07", new Vector3f(60f, 150f, 78f), platformMat);
        addStaticPlatform("Stack2Platform08", new Vector3f(50f, 140f, 28f), platformMat);
        addStaticPlatform("Stack2Platform09", new Vector3f(70f, 116f, 10f), platformMat);
        addStaticPlatform("Stack2Platform10", new Vector3f(30f, 119f, 50f), platformMat);
        addStaticPlatform("Stack2Platform11", new Vector3f(60f, 130f, 60f), platformMat);
        addStaticPlatform("Stack2Platform12", new Vector3f(50f, 170f, 78f), platformMat);
        addStaticPlatform("Stack2TopPlatform", new Vector3f(20f, 180f, 30f), platformMat);
        addStaticPlatform("Stack3CentralPlatform", new Vector3f(0, 179f, 0), platformMat);
        addStaticPlatform("Stack3Platform01", new Vector3f(10f, 190f, 20f), platformMat);
        addStaticPlatform("Stack3Platform02", new Vector3f(20f, 250f, 30f), platformMat);
        addStaticPlatform("Stack3Platform03", new Vector3f(40f, 223f, 80f), platformMat);
        addStaticPlatform("Stack3Platform04", new Vector3f(30f, 233f, 70f), platformMat);
        addStaticPlatform("Stack3Platform05", new Vector3f(80f, 213f, 40f), platformMat);
        addStaticPlatform("Stack3Platform06", new Vector3f(90f, 197f, 58f), platformMat);
        addStaticPlatform("Stack3Platform07", new Vector3f(60f, 240f, 78f), platformMat);
        addStaticPlatform("Stack3Platform08", new Vector3f(50f, 230f, 28f), platformMat);
        addStaticPlatform("Stack3Platform09", new Vector3f(70f, 206f, 10f), platformMat);
        addStaticPlatform("Stack3Platform10", new Vector3f(30f, 209f, 50f), platformMat);
        addStaticPlatform("Stack3Platform11", new Vector3f(60f, 220f, 60f), platformMat);
        addStaticPlatform("Stack3Platform12", new Vector3f(50f, 260f, 78f), platformMat);
        addStaticPlatform("Stack3TopPlatform", new Vector3f(20f, 270f, 30f), platformMat);

        return centralPlatform;
    }
    
    /**
     * Initialises the Crosshairs
     */
    private void initCrosshairs() {
        setDisplayStatView(false);
        setDisplayFps(false);

        float centerX = settings.getWidth() / 2f;
        float centerY = settings.getHeight() / 2f;
        float radius = 10f;

        Line horizontalMesh = new Line(new Vector3f(centerX - radius, centerY, 0), new Vector3f(centerX + radius, centerY, 0));
        Line verticalMesh = new Line(new Vector3f(centerX, centerY - radius, 0), new Vector3f(centerX, centerY + radius, 0));

        Geometry horizontalGeom = new Geometry("HorizontalCrosshair", horizontalMesh);
        Geometry verticalGeom = new Geometry("VerticalCrosshair", verticalMesh);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);

        horizontalGeom.setMaterial(mat);
        verticalGeom.setMaterial(mat);

        guiNode.attachChild(horizontalGeom);
        guiNode.attachChild(verticalGeom);
    }

    /**
     * Initializes the app as a whole, creates the level and places the character in it
     */
    @Override
    public void simpleInitApp() {
        initKeys(); // Ensure keys are initialized
        initTimerHud(); // Setup the timer UI text block
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        RigidBodyControl floorPhy = initPlatforms();

        playerControl = new BetterCharacterControl(1.5f, 6f, 1f);

        playerNode = new Node("PlayerNode");
        playerNode.addControl(playerControl);

        Box playerMesh = new Box(0.5f, 2.5f, 0.5f);
        Geometry playerGeo = new Geometry("PlayerVisual", playerMesh);
        Material playerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        playerMat.setColor("Color", ColorRGBA.Blue);
        playerGeo.setMaterial(playerMat);

        rootNode.attachChild(playerNode);
        bulletAppState.getPhysicsSpace().add(playerControl);

        playerControl.setJumpForce(new Vector3f(0, 20f, 0));
        playerControl.setGravity(new Vector3f(0,-27f,0));
        playerControl.warp(new Vector3f(0,5,0));

        flyCam.setMoveSpeed(0);

        tool = new Grapple(assetManager, bulletAppState, floorPhy, rootNode);
        
        rootNode.attachChild(SkyFactory.createSky(
            assetManager, 
            "Textures/Sky/Bright/BrightSky.dds", 
            EnvMapType.CubeMap
        ));
        
        initCrosshairs();
    }
}
