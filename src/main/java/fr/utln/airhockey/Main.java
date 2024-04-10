package fr.utln.airhockey;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;


public class Main extends SimpleApplication implements ActionListener {
public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setUseJoysticks(true);
        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }

    /** Prepare the Physics Application State (jBullet) */
    private BulletAppState bulletAppState;
    private Node player;

    final private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false, click = false;

    final private Vector3f camDir = new Vector3f();
    final private Vector3f camLeft = new Vector3f();
    /** Prepare Materials */
    private Material wall_mat;
    private Material stone_mat;
    private Material floor_mat;

    /** Prepare geometries for bricks and cannonballs. */
    private static final Box floor;
    private Boolean isRunning = true;
    private Vector2f lastCursorPosition = new Vector2f();
    private float sensitivity = 0.5f; // Ajustez cette valeur selon vos besoins

    static{
        floor = new Box(30f, 0.1f, 15f);
        floor.scaleTextureCoordinates(new Vector2f(3, 6));
    }

    @Override
    public void simpleInitApp() {
        /** Set up Physics Game */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        flyCam.setEnabled(false);
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
        cam2.update();

         */


        listerManettes();

        initMaterials();
        initWalls();
        initFloor();
        initPalet();
        player = initRaquette();
        setUpKeys();

        /** Configure cam to look at scene */
        cam.setLocation(new Vector3f(0, 75f, 0f));
        cam.lookAt(new Vector3f(-1, 0, 0), Vector3f.UNIT_Y);
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        //player = new CharacterControl(capsuleShape, 0.05f);
        //player.setJumpSpeed(20);
        //player.setFallSpeed(30);
        //player.setGravity(0);
        //player.setPhysicsLocation(new Vector3f(0, 75, 0));


        //bulletAppState.getPhysicsSpace().add(player);

        lastCursorPosition.set(inputManager.getCursorPosition());



    }

    public void initMaterials() {
        wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        wall_mat.setTexture("ColorMap", tex);

        stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        stone_mat.setTexture("ColorMap", tex2);

        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key3 = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
        key3.setGenerateMips(true);
        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(Texture.WrapMode.Repeat);
        floor_mat.setTexture("ColorMap", tex3);
    }

    public void initPalet(){
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
        geom.setMaterial(mat);
        geom.setMaterial(mat);                   // set the cube's material





        RigidBodyControl box_phy = new RigidBodyControl(1f);
        /** Add physical brick to physics space. */
        geom.addControl(box_phy);
        bulletAppState.getPhysicsSpace().add(box_phy);
        box_phy.setAngularFactor(0f);
        box_phy.setRestitution(1.0f);
        box_phy.setFriction(0f);

        /*bulletAppState.getPhysicsSpace().addCollisionListener(new PhysicsCollisionListener() {
            @Override
            public void collision(PhysicsCollisionEvent event) {
                if (event.getObjectA() == player && event.getObjectB() == box_phy ||
                        event.getObjectA() == box_phy && event.getObjectB() == player) {
                    // Si la caméra entre en collision avec le cube
                    // Déplacez le cube avec la caméra en ajustant sa position
                    Vector3f camDirection = cam.getDirection().mult(25); // Vous pouvez ajuster le facteur multiplicatif selon vos besoins
                    box_phy.setLinearVelocity(camDirection);
                }
            }
        });
*/
        rootNode.attachChild(geom);
    }

    public Node initRaquette(){

        float cylinderHeight = 1.5f;
        float sphereRadius = 0.5f;

        // Créer le cylindre
        Cylinder cylinderMesh = new Cylinder(32, 32, 1.5f, cylinderHeight, true);
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
        compositeNode.move(4f, 4f, 4f);

        RigidBodyControl box_phy = new RigidBodyControl(0f);
        /** Add physical brick to physics space. */
        compositeNode.addControl(box_phy);
        bulletAppState.getPhysicsSpace().add(box_phy);
        box_phy.setAngularFactor(0f);
        box_phy.setRestitution(1.0f);
        box_phy.setFriction(1f);

        // Ajouter le nœud composite à la scène
        rootNode.attachChild(compositeNode);
        return compositeNode;

    }




    public void initWalls(){
        Box wall = new Box(30f, 1.5f, 5f);
        Box wall2 = new Box(5f, 1.5f, 15f);
        Box wall3 = new Box(5f, 1.5f, 15f);
        Box wall4 = new Box(30f, 1.5f, 5f);
        Geometry wall_geo = new Geometry("Wall", wall);
        Geometry wall_geo2 = new Geometry("Wall2", wall2);
        Geometry wall_geo3 = new Geometry("Wall3", wall3);
        Geometry wall_geo4 = new Geometry("Wall4", wall4);
        Material wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        wall_geo.move(0, 0, -15);
        wall_geo2.move(-30, 0, 0);
        wall_geo3.move(30, 0, 0);
        wall_geo4.move(0, 0, 15);
        wall_mat.setColor("Color", ColorRGBA.Blue);
        wall_geo.setMaterial(wall_mat);
        wall_geo2.setMaterial(wall_mat);
        wall_geo3.setMaterial(wall_mat);
        wall_geo4.setMaterial(wall_mat);

        rootNode.attachChild(wall_geo);
        rootNode.attachChild(wall_geo2);
        rootNode.attachChild(wall_geo3);
        rootNode.attachChild(wall_geo4);


        RigidBodyControl wall_phy = new RigidBodyControl(0.0f);
        wall_geo.addControl(wall_phy);
        bulletAppState.getPhysicsSpace().add(wall_phy);

        RigidBodyControl wall_phy2 = new RigidBodyControl(0.0f);
        wall_geo2.addControl(wall_phy2);
        bulletAppState.getPhysicsSpace().add(wall_phy2);

        RigidBodyControl wall_phy3 = new RigidBodyControl(0.0f);
        wall_geo3.addControl(wall_phy3);
        bulletAppState.getPhysicsSpace().add(wall_phy3);

        RigidBodyControl wall_phy4 = new RigidBodyControl(0.0f);
        wall_geo4.addControl(wall_phy4);
        bulletAppState.getPhysicsSpace().add(wall_phy4);

        wall_phy.setRestitution(1.0f);
        wall_phy2.setRestitution(1.0f);
        wall_phy3.setRestitution(1.0f);
        wall_phy4.setRestitution(1.0f);
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

    private boolean dragging = false;
    private Vector2f dragOffset = new Vector2f();

    private int joyCarré = 0;
    private int joyCroix = 1;
    private int joyCercle = 2;
    private int joyTriangle = 3;

    private int joyLeftStickX = 0;
    private int joyLeftStickY = 1;
    private int joyRightStickX = 2;
    private int joyButtonL2 = 3;
    private int joyButtonR2 = 4;
    private int joyRightStickY = 5;

    private void setUpKeys() {
        inputManager.addMapping("MouseMoved", new MouseAxisTrigger(MouseInput.AXIS_X, true), new MouseAxisTrigger(MouseInput.AXIS_X, false), new MouseAxisTrigger(MouseInput.AXIS_Y, true), new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("MousePressed", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("MouseReleased", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Button_Carré", new JoyButtonTrigger(0, joyCarré));
        inputManager.addMapping("Button_Triangle", new JoyButtonTrigger(0, joyTriangle));
        inputManager.addMapping("Button_Cercle", new JoyButtonTrigger(0, joyCercle));
        inputManager.addMapping("Button_Croix", new JoyButtonTrigger(0, joyCroix));
        inputManager.addMapping("PS5LeftJoystickLeftBottom", new JoyAxisTrigger(0, joyLeftStickX, true));
        inputManager.addMapping("PS5LeftJoystickRight", new JoyAxisTrigger(0, joyLeftStickY, false));
        inputManager.addMapping("PS5RightJoystickRightBottom", new JoyAxisTrigger(0, joyRightStickX, false));
        inputManager.addMapping("PS5ButtonL2", new JoyAxisTrigger(0, joyButtonL2, false));
        inputManager.addMapping("PS5ButtonR2", new JoyAxisTrigger(0, joyButtonR2, false));
        inputManager.addMapping("PS5RightJoystickLeft", new JoyAxisTrigger(0, joyRightStickY, false));

        inputManager.addListener(analogListener, "MouseMoved");
        inputManager.addListener(analogListener, "PS5LeftJoystickLeftBottom", "PS5LeftJoystickRight", "PS5RightJoystickRightBottom", "PS5ButtonL2", "PS5ButtonR2", "PS5RightJoystickLeft");
        inputManager.addListener(actionListener, "Button_Carré", "Button_Triangle", "Button_Cercle", "Button_Croix");
        inputManager.addListener(actionListener, "MousePressed", "MouseReleased");
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
            if (name.equals("MousePressed")) {
                Vector3f mouse3D = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0).clone();
                Vector3f dir = cam.getWorldCoordinates(inputManager.getCursorPosition(), 1).subtractLocal(mouse3D).normalizeLocal();
                Ray ray = new Ray(mouse3D, dir);
                CollisionResults results = new CollisionResults();
                player.collideWith(ray, results);
                if (results.size() > 0) {
                    dragging = true;
                    System.out.println("Raquette position après le clic : " + player.getWorldTranslation());
                    Vector3f collisionPoint = results.getClosestCollision().getContactPoint();
                    Vector3f player2D = cam.getScreenCoordinates(player.getWorldTranslation());
                    Vector2f mouse2D = inputManager.getCursorPosition();
                    dragOffset.set(player2D.x - mouse2D.x, player2D.y - mouse2D.y);
                }
            } else if (name.equals("MouseReleased")) {
                dragging = false;
            } else if (name.equals("Button_Croix") && isPressed) {
                System.out.println("Button Croix pressed");
            }  else if (name.equals("Button_Triangle") && isPressed) {
                System.out.println("Button Triangle pressed");
            } else if (name.equals("Button_Carré") && isPressed) {
                System.out.println("Button Carré pressed");
            }else if (name.equals("Button_Cercle") && isPressed) {
                System.out.println("Button Cercle pressed");
            }
        }
    };

    private final AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("MouseMoved") && dragging) {
                Vector2f mouse2D = inputManager.getCursorPosition();
                Vector2f mouseDelta = mouse2D.subtract(lastCursorPosition);
                lastCursorPosition.set(mouse2D);

                Vector3f pos = player.getWorldTranslation().clone();
                Vector3f targetPos = new Vector3f(pos.x - mouseDelta.y * sensitivity, pos.y, pos.z - mouseDelta.x * sensitivity);
                pos.interpolateLocal(targetPos, tpf); // Interpolate position for smoother movement
                player.getControl(RigidBodyControl.class).setPhysicsLocation(pos);
            }else if (name.equals("PS5LeftJoystickLeftBottom")) {
                System.out.printf("Joystick value: %f\n", value);
            }else if(name.equals("PS5LeftJoystickRight")){
                System.out.printf("Joystick value: %f\n", value);
            }else if(name.equals("PS5RightJoystickRightBottom")){
                System.out.printf("Joystick value: %f\n", value);
            }else if(name.equals("PS5ButtonL2")){
                System.out.printf("Joystick value: %f\n", value);
            }else if(name.equals("PS5ButtonR2")){
                System.out.printf("Joystick value: %f\n", value);
            }else if(name.equals("PS5RightJoystickLeft")){
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
}
