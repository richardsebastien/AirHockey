package fr.utln.airhockey;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

public class main extends SimpleApplication implements ActionListener {
public static void main(String[] args) {
        main app = new main();
        app.start();
    }

    /** Prepare the Physics Application State (jBullet) */
    private BulletAppState bulletAppState;
    private CharacterControl player;

    final private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    final private Vector3f camDir = new Vector3f();
    final private Vector3f camLeft = new Vector3f();
    /** Prepare Materials */
    private Material wall_mat;
    private Material stone_mat;
    private Material floor_mat;

    /** Prepare geometries for bricks and cannonballs. */
    private static final Box floor;

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



        initMaterials();
        initWalls();
        initFloor();
        initPalet();
        initRaquette();
        setUpKeys();

        /** Configure cam to look at scene */
        cam.setLocation(new Vector3f(0, 75f, 0f));
        cam.lookAt(new Vector3f(-1, 0, 0), Vector3f.UNIT_Y);
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(0);
        player.setPhysicsLocation(new Vector3f(0, 75, 0));


        bulletAppState.getPhysicsSpace().add(player);




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
        geom.setLocalTranslation(0, 3, 5);
        geom.setMaterial(mat);                   // set the cube's material





        RigidBodyControl box_phy = new RigidBodyControl(1f);
        /** Add physical brick to physics space. */
        geom.addControl(box_phy);
        bulletAppState.getPhysicsSpace().add(box_phy);
        box_phy.setAngularFactor(0f);
        box_phy.setRestitution(1.0f);
        box_phy.setFriction(0f);

        bulletAppState.getPhysicsSpace().addCollisionListener(new PhysicsCollisionListener() {
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

        rootNode.attachChild(geom);
    }

    public void initRaquette(){

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
        //cylinderGeom.move(4f, 4f, 4f);
        //sphereGeom.move(4f, 4f, 4f);

        RigidBodyControl object_phy = new RigidBodyControl(0.0f);
        //compositeNode.addControl(object_phy);
        //bulletAppState.getPhysicsSpace().add(object_phy);

        Node compositeNode = new Node("CompositeObject");
        compositeNode.attachChild(cylinderGeom);
        compositeNode.attachChild(sphereGeom);
        compositeNode.move(4f, 4f, 4f);

        // Ajouter le nœud composite à la scène
        rootNode.attachChild(compositeNode);

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


    private void setUpKeys() {
        inputManager.addMapping("Left", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("Right", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("Up", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("Down", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
    }

    public void simpleUpdate(float tpf) {
        camDir.set(cam.getDirection()).multLocal(0.6f);
        camLeft.set(cam.getLeft()).multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            if (value) { left = true; } else { left = false; }
        } else if (binding.equals("Right")) {
            if (value) { right = true; } else { right = false; }
        } else if (binding.equals("Up")) {
            if (value) { up = true; } else { up = false; }
        } else if (binding.equals("Down")) {
            if (value) { down = true; } else { down = false; }
        } else if (binding.equals("Jump")) {
            player.jump();
        }
    }
}
