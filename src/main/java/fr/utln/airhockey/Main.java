package fr.utln.airhockey;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class Main extends SimpleApplication implements ActionListener {
public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setUseJoysticks(true);
        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }
    @Setter
    private Nifty nifty;
    @Getter
    @Setter
    private Boolean isPaused = true;
    @Getter
    @Setter
    private Boolean isStarted = false;

    /** Prepare the Physics Application State (jBullet) */
    private BulletAppState bulletAppState;
    private RigidBodyControl player;
    private RigidBodyControl palet;

    private boolean click = false;
    private final Vector2f lastCursorPosition = new Vector2f();

    /** Prepare the cages */
    private GhostControl red_cage;
    private GhostControl blue_cage;

    /** Prepare Materials */
    private Material wall_mat;
    private Material red_cage_mat;
    private Material blue_cage_mat;
    private Material floor_mat;
    /** Prepare geometries for bricks and cannonballs. */
    private static final Box floor;

    /** Prepare all variables for scoring and timer */
    private int redScore= 0;
    private int blueScore = 0;
    private int time = 0;
    private float timer = 0.0f;
    private float tpfTime = 0.0f;
    private boolean isGoal = true;

    static{
        floor = new Box(30f, 0.1f, 15f);
    }

    @Override
    public void simpleInitApp() {

        StartScreenState startScreenState = new StartScreenState();
        startScreenState.setApp(this);
        stateManager.attach(startScreenState);

        /* Set up Physics Game */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        flyCam.setEnabled(false);
        flyCam.setMoveSpeed(45);

        bulletAppState.setDebugEnabled(true); // For prod only
        /*

        // Create two cam to render the scene
        Camera cam1 = new Camera(settings.getWidth(), settings.getHeight());
        Camera cam2 = new Camera(settings.getWidth(), settings.getHeight());

        // Create two viewports
        ViewPort viewPort1 = renderManager.createMainView("Left View", cam1);
        viewPort1.setClearFlags(true, true, true);
        viewPort1.attachScene(rootNode);

        ViewPort viewPort2 = renderManager.createMainView("Right View", cam2);
        viewPort2.setClearFlags(true, true, true);
        viewPort2.attachScene(rootNode);

        cam1.setViewPort( 0.0f , 0.5f   ,   0.0f , 1.0f );
        cam2.setViewPort( 0.5f , 1.0f   ,   0.0f , 1.0f );
        cam1.setLocation(new Vector3f(0, 75f, 0f));
        cam1.lookAt(new Vector3f(-1, 0, 0), Vector3f.UNIT_Y);
        cam2.setLocation(new Vector3f(0, 75f, 0f));
        cam2.lookAt(new Vector3f(1, 0, 0), Vector3f.UNIT_Y);
        cam1.setFrustumPerspective(45f, (float) (cam1.getWidth()/2) / cam1.getHeight(), 0.01f, 1000f);
        cam2.setFrustumPerspective(45f, (float) (cam2.getWidth()/2) / cam2.getHeight(), 0.01f, 1000f);
        cam1.update();
        cam2.update();*/
        /*
        // Create a new font
        assetManager.registerLoader(TrueTypeLoader.class, "ttf");
        TrueTypeKeyBMP ttk = new TrueTypeKeyBMP("Policies/LasEnter.ttf",
                Style.Plain, 72);
        TrueTypeFont ttf = (TrueTypeBMP)assetManager.loadAsset(ttk);
        ttf.setScale(16/21f);

        // Create a new text geometry
        TrueTypeNode trueNode = ttf.getText("Hello World", 0, ColorRGBA.White);
        trueNode.setLocalTranslation((settings.getWidth()/2)-150, settings.getHeight()-50, 0);
        guiNode.attachChild(trueNode);
        */

        // Init all the game elements
        initMaterials();
        initWalls();
        initCages();
        initFloor();
        palet = initPalet();
        player = initRaquette();
        // Init the inputs
        listerManettes();
        setUpKeys();

        /* Configure cam to look at scene (no flying cam) */
        cam.setLocation(new Vector3f(0, 75f, 0f));
        cam.lookAt(new Vector3f(-1, 0, 0), Vector3f.UNIT_Y);
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);

        lastCursorPosition.set(inputManager.getCursorPosition());

    }

    public void initMaterials() {
        wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/wall.png");
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        wall_mat.setTexture("ColorMap", tex);

        red_cage_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/cage_rouge.png");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        red_cage_mat.setTexture("ColorMap", tex2);

        blue_cage_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key4 = new TextureKey("Textures/Terrain/cage_bleue.png");
        key4.setGenerateMips(true);
        Texture tex4 = assetManager.loadTexture(key4);
        blue_cage_mat.setTexture("ColorMap", tex4);

        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key3 = new TextureKey("Textures/Terrain/table_base.png");
        key3.setGenerateMips(true);
        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(Texture.WrapMode.EdgeClamp);
        floor_mat.setTexture("ColorMap", tex3);
    }

    public RigidBodyControl initPalet(){
        float radius = 1.0f; // Rayon du cylindre
        float height = 1f; // Hauteur du cylindre
        int radialSamples = 32; // Nombre d'échantillons radiaux pour le cylindre (doit être >= 3)
        int axialSamples = 2; // Nombre d'échantillons axiaux pour le cylindre (doit être >= 2)
        boolean closed = true; // Le cylindre est fermé à une extrémité

        Geometry geom = new Geometry("Cylinder", new Cylinder( axialSamples, radialSamples, radius, height, closed));
        // Créer un matériau pour le cylindre (par exemple, rouge)
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        geom.rotate(FastMath.HALF_PI, 0, 0);
        mat.setColor("Color", ColorRGBA.Red); // Couleur du matériau
        geom.setMaterial(mat);                   // set the cube's material

        RigidBodyControl box_phy = new RigidBodyControl(1f);
        /* Add physical brick to physics space. */
        geom.addControl(box_phy);
        bulletAppState.getPhysicsSpace().add(box_phy);
        box_phy.setAngularFactor(0f);
        box_phy.setRestitution(1.0f);
        box_phy.setFriction(0f);

        rootNode.attachChild(geom);

        return box_phy;
    }

    public RigidBodyControl initRaquette(){

        float cylinderHeight = 1.5f;
        float sphereRadius = 0.5f;

        // Créer le cylindre
        Cylinder cylinderMesh = new Cylinder(32, 32, 2f, cylinderHeight, true);
        Geometry cylinderGeom = new Geometry("Cylinder", cylinderMesh);
        Material cylinderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        cylinderMat.setColor("Color", ColorRGBA.Blue);
        cylinderGeom.setMaterial(cylinderMat);


        // Créer la sphère
        Sphere sphereMesh = new Sphere(32, 32, sphereRadius);
        Geometry sphereGeom = new Geometry("Sphere", sphereMesh);
        Material sphereMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sphereMat.setColor("Color", ColorRGBA.Red);
        sphereGeom.setMaterial(sphereMat);


        // Positionner la sphère au sommet du cylindre

        sphereGeom.setLocalTranslation(new Vector3f(0, cylinderHeight + sphereRadius, 0));

        cylinderGeom.setLocalTranslation(0, 0f, 0);
        sphereGeom.setLocalTranslation(0, 1.2f, 0f);
        cylinderGeom.rotate(FastMath.HALF_PI, 0, 0);



        Node compositeNode = new Node("CompositeObject");
        compositeNode.attachChild(cylinderGeom);
        compositeNode.attachChild(sphereGeom);
        compositeNode.move(4f, 2f, 4f);

        RigidBodyControl box_phy = new RigidBodyControl(1f);
        /* Add physical brick to physics space. */
        compositeNode.addControl(box_phy);
        bulletAppState.getPhysicsSpace().add(box_phy);
        box_phy.setRestitution(0.0f);
        box_phy.setFriction(0f);

        // Ajouter le nœud composite à la scène
        rootNode.attachChild(compositeNode);
        return box_phy;

    }

    public RigidBodyControl[] initWalls(){
        RigidBodyControl[] ans = new RigidBodyControl[6];
        /* Initialization of walls */
        Box wall1 = new Box(31f, 1.5f, 1f);
        Box wall2 = new Box(31f, 1.5f, 1f);
        Box wall3 = new Box(2f, 1.2f, 4.5f);
        Box wall4 = new Box(2f, 1.2f, 4.5f);
        Box wall5 = new Box(2f, 1.2f, 4.5f);
        Box wall6 = new Box(2f, 1.2f, 4.5f);
        Geometry wall_geo1 = new Geometry("Wall1", wall1);
        Geometry wall_geo2 = new Geometry("Wall2", wall2);
        Geometry wall_geo3 = new Geometry("Wall3", wall3);
        Geometry wall_geo4 = new Geometry("Wall4", wall4);
        Geometry wall_geo5 = new Geometry("Wall5", wall5);
        Geometry wall_geo6 = new Geometry("Wall6", wall6);
        /* Move the walls to the correct position */
        wall_geo1.move(0, 0, -15);
        wall_geo2.move(0, 0, 15);
        wall_geo3.move(-29, 0, -9.5f);
        wall_geo4.move(29, 0, 9.5f);
        wall_geo5.move(-29, 0, 9.5f);
        wall_geo6.move(29, 0, -9.5f);

        wall_geo1.setMaterial(wall_mat);
        wall_geo2.setMaterial(wall_mat);
        wall_geo3.setMaterial(wall_mat);
        wall_geo4.setMaterial(wall_mat);
        wall_geo5.setMaterial(wall_mat);
        wall_geo6.setMaterial(wall_mat);

        rootNode.attachChild(wall_geo1);
        rootNode.attachChild(wall_geo2);
        rootNode.attachChild(wall_geo3);
        rootNode.attachChild(wall_geo4);
        rootNode.attachChild(wall_geo5);
        rootNode.attachChild(wall_geo6);

        /* Make the walls physical with mass 0.0f */
        RigidBodyControl wall_phy1 = new RigidBodyControl(0.0f);
        wall_geo1.addControl(wall_phy1);
        bulletAppState.getPhysicsSpace().add(wall_phy1);

        RigidBodyControl wall_phy2 = new RigidBodyControl(0.0f);
        wall_geo2.addControl(wall_phy2);
        bulletAppState.getPhysicsSpace().add(wall_phy2);

        RigidBodyControl wall_phy3 = new RigidBodyControl(0.0f);
        wall_geo3.addControl(wall_phy3);
        bulletAppState.getPhysicsSpace().add(wall_phy3);

        RigidBodyControl wall_phy4 = new RigidBodyControl(0.0f);
        wall_geo4.addControl(wall_phy4);
        bulletAppState.getPhysicsSpace().add(wall_phy4);

        RigidBodyControl wall_phy5 = new RigidBodyControl(0.0f);
        wall_geo5.addControl(wall_phy5);
        bulletAppState.getPhysicsSpace().add(wall_phy5);

        RigidBodyControl wall_phy6 = new RigidBodyControl(0.0f);
        wall_geo6.addControl(wall_phy6);
        bulletAppState.getPhysicsSpace().add(wall_phy6);

        wall_phy1.setRestitution(1.0f);
        wall_phy2.setRestitution(1.0f);
        wall_phy3.setRestitution(1.0f);
        wall_phy4.setRestitution(1.0f);
        wall_phy5.setRestitution(1.0f);
        wall_phy6.setRestitution(1.0f);

        ans[0] = wall_phy1;
        ans[1] = wall_phy2;
        ans[2] = wall_phy3;
        ans[3] = wall_phy4;
        ans[4] = wall_phy5;
        ans[5] = wall_phy6;

        return ans;
    }

    public void initCages(){

        /* Red cage */
        red_cage = new GhostControl(new BoxCollisionShape(new Vector3f(2f, 1.2f, 4.1f)));
        // Create a node for the red cage to manipulate it easily
        Node red_cage_node = new Node("Red Cage");
        red_cage_node.addControl(red_cage);
        // Place it correctly
        red_cage_node.setLocalTranslation(-29, 0, 0);
        rootNode.attachChild(red_cage_node);
        // Add the cage to the physics space
        bulletAppState.getPhysicsSpace().add(red_cage);

        /* Blue cage */
        blue_cage = new GhostControl(new BoxCollisionShape(new Vector3f(2f, 1.2f, 4.1f)));
        // Create a node for the blue cage to manipulate it easily
        Node blue_cage_node = new Node("Blue Cage");
        blue_cage_node.addControl(blue_cage);
        // Place it correctly
        blue_cage_node.setLocalTranslation(29, 0, 0);
        rootNode.attachChild(blue_cage_node);
        // Add the cage to the physics space
        bulletAppState.getPhysicsSpace().add(blue_cage);

        /* Initialization of the red cage walls */
        Box red_cage_back = new Box(0.1f, 1.2f, 5f);
        Box red_cage_top = new Box(2f, 0.1f, 5f);

        /* Initialization of the blue cage walls */
        Box blue_cage_back = new Box(0.1f, 1.2f, 5f);
        Box blue_cage_top = new Box(2f, 0.1f, 5f);

        /* Geometries (red then blue) */
        Geometry red_cage_back_geo = new Geometry("Red Cage Back", red_cage_back);
        Geometry red_cage_top_geo = new Geometry("Red Cage Top", red_cage_top);

        Geometry blue_cage_back_geo = new Geometry("Blue Cage Back", blue_cage_back);
        Geometry blue_cage_top_geo = new Geometry("Blue Cage Top", blue_cage_top);

        /* Move the geometries to the correct position */
        red_cage_back_geo.move(-30.9f, 0, 0);
        red_cage_top_geo.move(-29, 1.1f, 0);

        blue_cage_back_geo.move(30.9f, 0, 0);
        blue_cage_top_geo.move(29, 1.1f, 0);

        /* Set the materials */
        red_cage_back_geo.setMaterial(wall_mat);
        red_cage_top_geo.setMaterial(red_cage_mat);

        blue_cage_back_geo.setMaterial(wall_mat);
        blue_cage_top_geo.setMaterial(blue_cage_mat);

        /* Attach to root */
        rootNode.attachChild(red_cage_back_geo);
        rootNode.attachChild(red_cage_top_geo);

        rootNode.attachChild(blue_cage_back_geo);
        rootNode.attachChild(blue_cage_top_geo);

        /* Make the cages physical with mass 0.0f */
        RigidBodyControl red_cage_top_phy = new RigidBodyControl(0.0f);
        red_cage_top_geo.addControl(red_cage_top_phy);
        bulletAppState.getPhysicsSpace().add(red_cage_top_phy);

        RigidBodyControl red_cage_back_phy = new RigidBodyControl(0.0f);
        red_cage_back_geo.addControl(red_cage_back_phy);
        bulletAppState.getPhysicsSpace().add(red_cage_back_phy);

        RigidBodyControl blue_cage_top_phy = new RigidBodyControl(0.0f);
        blue_cage_top_geo.addControl(blue_cage_top_phy);
        bulletAppState.getPhysicsSpace().add(blue_cage_top_phy);

        RigidBodyControl blue_cage_back_phy = new RigidBodyControl(0.0f);
        blue_cage_back_geo.addControl(blue_cage_back_phy);
        bulletAppState.getPhysicsSpace().add(blue_cage_back_phy);

        red_cage_top_phy.setRestitution(1.0f);
        red_cage_back_phy.setRestitution(1.0f);
        blue_cage_back_phy.setRestitution(1.0f);
        blue_cage_top_phy.setRestitution(1.0f);
    }

    public void initFloor() {
        Geometry floor_geo = new Geometry("Floor", floor);
        floor_geo.setMaterial(floor_mat);
        floor_geo.setLocalTranslation(0, -0.1f, 0);
        this.rootNode.attachChild(floor_geo);
        /* Make the floor physical with mass 0.0f! */
        RigidBodyControl floor_phy = new RigidBodyControl(0.0f);
        floor_geo.addControl(floor_phy);
        bulletAppState.getPhysicsSpace().add(floor_phy);
    }


    private void setUpKeys() {
        int joyCarre = 0;
        inputManager.addMapping("Button_Carré", new JoyButtonTrigger(0, joyCarre));
        int joyTriangle = 3;
        inputManager.addMapping("Button_Triangle", new JoyButtonTrigger(0, joyTriangle));
        int joyCercle = 2;
        inputManager.addMapping("Button_Cercle", new JoyButtonTrigger(0, joyCercle));
        int joyCroix = 1;
        inputManager.addMapping("Button_Croix", new JoyButtonTrigger(0, joyCroix));
        int joyLeftStickX = 0;
        inputManager.addMapping("PS5LeftJoystickLeftBottom", new JoyAxisTrigger(0, joyLeftStickX, true));
        int joyLeftStickY = 1;
        inputManager.addMapping("PS5LeftJoystickRight", new JoyAxisTrigger(0, joyLeftStickY, false));
        int joyRightStickX = 2;
        inputManager.addMapping("PS5RightJoystickRightBottom", new JoyAxisTrigger(0, joyRightStickX, false));
        int joyButtonL2 = 3;
        inputManager.addMapping("PS5ButtonL2", new JoyAxisTrigger(0, joyButtonL2, false));
        int joyButtonR2 = 4;
        inputManager.addMapping("PS5ButtonR2", new JoyAxisTrigger(0, joyButtonR2, false));
        int joyRightStickY = 5;
        inputManager.addMapping("PS5RightJoystickLeft", new JoyAxisTrigger(0, joyRightStickY, false));
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        inputManager.addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addListener(analogListener, "PS5LeftJoystickLeftBottom", "PS5LeftJoystickRight", "PS5RightJoystickRightBottom", "PS5ButtonL2", "PS5ButtonR2", "PS5RightJoystickLeft");
        inputManager.addListener(actionListener, "Button_Carré", "Button_Triangle", "Button_Cercle", "Button_Croix");
        inputManager.addListener(actionListener, "Escape");

        inputManager.addMapping("LeftClick", new MouseButtonTrigger(0));
        // Définir l'écouteur d'action pour le clic gauche
        inputManager.addListener(actionListener, "LeftClick");

        inputManager.addJoystickConnectionListener(new JoystickConnectionListener() {
            @Override
            public void onConnected(Joystick joystick) {
                System.out.println("Joystick connected: " + joystick.getName());
            }

            @Override
            public void onDisconnected(Joystick joystick) {
                System.out.println("Joystick Disconnected: " + joystick.getName());
            }
        });
        inputManager.getJoysticks();
    }

    private final ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Button_Croix") && isPressed) {
                System.out.println("Button Croix pressed");
            }
            if (name.equals("Button_Triangle") && isPressed) {
                System.out.println("Button Triangle pressed");
            }
            if (name.equals("Button_Carré") && isPressed) {
                System.out.println("Button Carré pressed");
            }
            if (name.equals("Button_Cercle") && isPressed) {
                System.out.println("Button Cercle pressed");
            }
            if(name.equals("Escape") && isPressed){
                if(isStarted){
                    if(!isPaused){
                        isPaused = true;
                        nifty.gotoScreen("pause");
                    }else{
                        nifty.gotoScreen("hud");
                        isPaused = false;
                    }
                }else{
                    stop();
                }
            }

            //Fin du clic gauche
            click = name.equals("LeftClick") && isPressed;
        }
    };

    private final AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("PS5LeftJoystickLeftBottom")) {
                System.out.printf("Joystick value: %f\n", value);
            }
            if(name.equals("PS5LeftJoystickRight")){
                System.out.printf("Joystick value: %f\n", value);
            }
            if(name.equals("PS5RightJoystickRightBottom")){
                System.out.printf("Joystick value: %f\n", value);
            }
            if(name.equals("PS5ButtonL2")){
                System.out.printf("Joystick value: %f\n", value);
            }
            if(name.equals("PS5ButtonR2")){
                System.out.printf("Joystick value: %f\n", value);
            }
            if(name.equals("PS5RightJoystickLeft")){
                System.out.printf("Joystick value: %f\n", value);
            }
        }
    };

    public void listerManettes() {
        Joystick[] joysticks = inputManager.getJoysticks();

        if (joysticks != null && joysticks.length > 0) {
            System.out.println("Manettes détectées :");

            for (int i = 0; i < joysticks.length; i++) {
                Joystick joystick = joysticks[i];
                System.out.println("Manette " + (i + 1) + ": " + joystick.getName());

                // Liste des axes de la manette
                for (JoystickAxis axis : joystick.getAxes()) {
                    System.out.println("   Axe " + axis.getAxisId() + ": " + axis.getName());
                }

                // Liste des boutons de la manette
                for (JoystickButton button : joystick.getButtons()) {
                    System.out.println("   Bouton " + button.getButtonId() + ": " + button.getName());
                }
            }
        } else {
            System.out.println("Aucune manette détectée.");
        }
    }

    @Override
    public void onAction(String s, boolean b, float v) {
    }

    public void simpleUpdate(float tpf) {
        if(!isPaused) {
            /*
            tpfTime += tpf;
            if (tpfTime >= 1.0f) {
                // Une seconde s'est écoulée, incrémenter le compteur
                time++;
                tpfTime = 0.0f;
            }
            */

            if(blueScore == 5 || redScore == 5){
                isPaused = true;
                isStarted = false;
                nifty.gotoScreen("end");
            }
            // If the palet is in the red cage
            if (red_cage.getOverlappingObjects().contains(palet)) {
                if(isGoal){
                    // Reset the palet and the player
                    palet.setLinearVelocity(new Vector3f(0f, 0f, 0f));
                    palet.setPhysicsLocation(new Vector3f(0, 2, 0));
                    player.setPhysicsLocation(new Vector3f(15, 5, 0));
                    player.setLinearVelocity(new Vector3f(0f, 0f, 0f));
                    // Increment the blue score
                    blueScore++;
                    // Set the goal to false to avoid incrementing the score twice
                    isGoal = false;
                }


            }
            // If the palet is in the blue cage
            if (blue_cage.getOverlappingObjects().contains(palet)) {
                if(isGoal){
                    // Reset the palet and the player
                    palet.setLinearVelocity(new Vector3f(0f, 0f, 0f));
                    palet.setPhysicsLocation(new Vector3f(0, 2, 0));
                    player.setPhysicsLocation(new Vector3f(15, 5, 0));
                    player.setLinearVelocity(new Vector3f(0f, 0f, 0f));
                    // Increment the red score
                    redScore++;
                    // Set the goal to false to avoid incrementing the score twice
                    isGoal = false;
                }
            }


            Vector3f bloqueRaquette;
            Vector3f bloquePalet;

            Vector3f rotatePalet;

            player.setAngularVelocity(new Vector3f(0f, 0f, 0f));
            bloqueRaquette = player.getLinearVelocity();
            player.setLinearVelocity(new Vector3f(bloqueRaquette.x, 0f, bloqueRaquette.z));

            rotatePalet = palet.getAngularVelocity();
            palet.setAngularVelocity(new Vector3f(rotatePalet.x, 0f, 0f));
            bloquePalet = palet.getLinearVelocity();
            palet.setLinearVelocity(new Vector3f(bloquePalet.x, 0f, bloquePalet.z));

            // Mettre à jour le score et le temps
            Element blueScoreText = Objects.requireNonNull(nifty.getScreen("hud")).findElementById("blueScoreText");
            Element redScoreText = Objects.requireNonNull(nifty.getScreen("hud")).findElementById("redScoreText");
            //Element timeText = Objects.requireNonNull(nifty.getScreen("hud")).findElementById("timeText");

            Objects.requireNonNull(Objects.requireNonNull(blueScoreText).getRenderer(TextRenderer.class)).setText("Blue score: " + blueScore);
            Objects.requireNonNull(Objects.requireNonNull(redScoreText).getRenderer(TextRenderer.class)).setText("Red score: " + redScore);
            //Objects.requireNonNull(Objects.requireNonNull(timeText).getRenderer(TextRenderer.class)).setText("Time: " + time);


            if (click) {

                // Reset results list.
                CollisionResults results = new CollisionResults();
                // Convert screen click to 3d position
                Vector2f click2d = inputManager.getCursorPosition();
                Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
                // Aim the ray from the clicked spot forwards.
                Ray ray = new Ray(click3d, dir);
                // Collect intersections between ray and all nodes in results list.
                rootNode.collideWith(ray, results);
                for (int i = 0; i < results.size(); i++) {
                    //On ne garde que la collision avec le sol
                    if (Objects.equals(results.getCollision(i).getGeometry().getName(), "Floor")) {

                        Vector3f posSouris = results.getCollision(i).getContactPoint();
                        Vector3f posPalet = player.getPhysicsLocation();

                        // Pour eviter que la raquette ai Parkinson
                        if ((Math.abs(posSouris.x - posPalet.x) > 0.1) || (Math.abs(posSouris.z - posPalet.z) > 0.1)) {
                            Vector3f direction = results.getCollision(i).getContactPoint().subtract(player.getPhysicsLocation());
                            float distance = direction.length();
                            // Normaliser le vecteur de déplacement pour avoir une direction unitaire
                            direction = direction.normalize();
                            float speedMultiplier = Math.min(distance / 6, 1.0f);
                            // Appliquer le déplacement au joueur
                            player.setLinearVelocity(direction.mult(speedMultiplier * 50)); // Multiplier par une vitesse de déplacement
                            // Avoid the goals to be scored twice for 1 goal
                            if (timer > 1){
                                timer = 0;
                                isGoal = true;
                            }
                            else {
                                timer += tpf;
                            }
                        } else {
                            // Arreter le déplacement du joueur
                            player.setLinearVelocity(new Vector3f(0f, 0f, 0f));
                        }
                    }
                }
            }
        }else{
            player.setLinearVelocity(new Vector3f(0f, 0f, 0f));
            palet.setLinearVelocity(new Vector3f(0f, 0f, 0f));
        }
    }

    @SuppressWarnings("unused")
    public void pauseGame() {
        isPaused = true;
    }

    @SuppressWarnings("unused")
    public void resumeGame() {
        isPaused = false;
    }

}
