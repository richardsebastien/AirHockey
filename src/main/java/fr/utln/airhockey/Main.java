package fr.utln.airhockey;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;


public class Main extends SimpleApplication implements ActionListener {
public static void main(String[] args) {
        Main app = new Main();
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
    private Material cage_mat;
    private Material floor_mat;

    /** Prepare geometries for bricks and cannonballs. */
    private static final Box floor;
    private Boolean isRunning = true;
    private final Vector2f lastCursorPosition = new Vector2f();

    static{
        floor = new Box(30f, 0.1f, 15f);
    }

    @Override
    public void simpleInitApp() {
        /* Set up Physics Game */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        flyCam.setEnabled(false);

        initMaterials();
        initWalls();
        initFloor();
        initPalet();
        player = initRaquette();
        setUpKeys();

        /* Configure cam to look at scene */
        cam.setLocation(new Vector3f(0, 75f, 0f));
        cam.lookAt(new Vector3f(-1, 0, 0), Vector3f.UNIT_Y);
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
    }

    public void initMaterials() {
        wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/wall.png");
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        wall_mat.setTexture("ColorMap", tex);

        cage_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/cage.png");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        cage_mat.setTexture("ColorMap", tex2);

        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key3 = new TextureKey("Textures/Terrain/table_base.png");
        key3.setGenerateMips(true);
        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(Texture.WrapMode.EdgeClamp);
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
        /* Add physical brick to physics space. */
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


        RigidBodyControl object_phy = new RigidBodyControl(0.0f);


        Node compositeNode = new Node("CompositeObject");
        compositeNode.attachChild(cylinderGeom);
        compositeNode.attachChild(sphereGeom);
        compositeNode.move(4f, 4f, 4f);

        RigidBodyControl box_phy = new RigidBodyControl(1f);
        /* Add physical brick to physics space. */
        compositeNode.addControl(box_phy);
        bulletAppState.getPhysicsSpace().add(box_phy);
        box_phy.setAngularFactor(0f);
        box_phy.setRestitution(1.0f);
        box_phy.setFriction(1f);

        // Ajouter le nœud composite à la scène
        rootNode.attachChild(compositeNode);
        return compositeNode;

    }
/*
    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("MouseMovement")) {
            float speed = 0.1f;
            compositeNode.move(value * speed, 0, 0); // Modifie la position de la raquette selon le mouvement horizontal de la souris
            compositeNode.move(0, -value * speed, 0); // Modifie la position de la raquette selon le mouvement vertical de la souris
        }
    }
*/

    public void initWalls(){
        Box wall = new Box(31f, 1.5f, 1f);
        Box wall2 = new Box(1f, 1.5f, 5f);
        Box wall3 = new Box(1f, 1.5f, 5f);
        Box wall4 = new Box(31f, 1.5f, 1f);
        Box wall5 = new Box(1f, 1.5f, 5f);
        Box wall6 = new Box(1f, 1.5f, 5f);
        Box wall7 = new Box(1f, 1.5f, 5f);
        Box wall8 = new Box(1f, 1.5f, 5f);
        Geometry wall_geo = new Geometry("Wall", wall);
        Geometry wall_geo2 = new Geometry("Wall2", wall2);
        Geometry wall_geo3 = new Geometry("Wall3", wall3);
        Geometry wall_geo4 = new Geometry("Wall4", wall4);
        Geometry wall_geo5 = new Geometry("Wall5", wall5);
        Geometry wall_geo6 = new Geometry("Wall6", wall6);
        Geometry wall_geo7 = new Geometry("Wall7", wall7);
        Geometry wall_geo8 = new Geometry("Wall8", wall8);
        wall_geo.move(0, 0, -15);
        wall_geo2.move(-30, 0, 0);
        wall_geo3.move(30, 0, 0);
        wall_geo4.move(0, 0, 15);
        wall_geo5.move(-30, 0, -9);
        wall_geo6.move(30, 0, 9);
        wall_geo7.move(-30, 0, 9);
        wall_geo8.move(30, 0, -9);
        wall_geo.setMaterial(wall_mat);
        wall_geo2.setMaterial(cage_mat);
        wall_geo3.setMaterial(cage_mat);
        wall_geo4.setMaterial(wall_mat);
        wall_geo5.setMaterial(wall_mat);
        wall_geo6.setMaterial(wall_mat);
        wall_geo7.setMaterial(wall_mat);
        wall_geo8.setMaterial(wall_mat);

        rootNode.attachChild(wall_geo);
        rootNode.attachChild(wall_geo2);
        rootNode.attachChild(wall_geo3);
        rootNode.attachChild(wall_geo4);
        rootNode.attachChild(wall_geo5);
        rootNode.attachChild(wall_geo6);
        rootNode.attachChild(wall_geo7);
        rootNode.attachChild(wall_geo8);


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

        RigidBodyControl wall_phy5 = new RigidBodyControl(0.0f);
        wall_geo5.addControl(wall_phy5);
        bulletAppState.getPhysicsSpace().add(wall_phy5);

        RigidBodyControl wall_phy6 = new RigidBodyControl(0.0f);
        wall_geo6.addControl(wall_phy6);
        bulletAppState.getPhysicsSpace().add(wall_phy6);

        RigidBodyControl wall_phy7 = new RigidBodyControl(0.0f);
        wall_geo7.addControl(wall_phy7);
        bulletAppState.getPhysicsSpace().add(wall_phy7);

        RigidBodyControl wall_phy8 = new RigidBodyControl(0.0f);
        wall_geo8.addControl(wall_phy8);
        bulletAppState.getPhysicsSpace().add(wall_phy8);

        wall_phy.setRestitution(1.0f);
        wall_phy2.setRestitution(1.0f);
        wall_phy3.setRestitution(1.0f);
        wall_phy4.setRestitution(1.0f);
        wall_phy5.setRestitution(1.0f);
        wall_phy6.setRestitution(1.0f);
        wall_phy7.setRestitution(1.0f);
        wall_phy8.setRestitution(1.0f);
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
        inputManager.addMapping("Left", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("Click", new MouseAxisTrigger(MouseInput.BUTTON_LEFT, true));
        inputManager.addMapping("Right", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("Up", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("Down", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        //inputManager.addListener(analogListener, "Left", "Right", "Up","Down");
        lastCursorPosition.set(inputManager.getCursorPosition());

    }
/*
    private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float keyPressed, float tpf) {
            if (true) {

            }
        }
    };
*/


    public void simpleUpdate(float tpf) {
    // Calculer le déplacement de la souris depuis la dernière frame
        if (click) {
            Vector2f currentCursorPosition = inputManager.getCursorPosition();
            System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHH");
            player.move(currentCursorPosition.getX(), currentCursorPosition.getY(), 0);
            //player.
        }
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        switch (binding) {
            case "Left":
                left = value;
                break;
            case "Right":
                right = value;
                break;
            case "Click":
                click = value;
                break;
            case "Up":
                up = value;
                break;
            case "Down":
                down = value;
                break;
        }
    }
}
