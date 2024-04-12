package fr.utln.airhockey;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
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
    private RigidBodyControl player;
    private RigidBodyControl palet;

    private boolean click = false;

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
        palet = initPalet();
        player = initRaquette();

        //RigidBodyControl[] pomme;
        //pomme = initWalls();
        //MyCollisionListener collisionListener = new MyCollisionListener(player,pomme[0],pomme[1],pomme[2],pomme[3]);
        //bulletAppState.getPhysicsSpace().addCollisionListener(collisionListener);

        setUpKeys();
        inputManager.addMapping("LeftClick", new MouseButtonTrigger(0));
        // Définir l'écouteur d'action pour le clic gauche
        inputManager.addListener(actionListener, "LeftClick");

        /** Configure cam to look at scene */
        cam.setLocation(new Vector3f(0, 75f, 0f));
        cam.lookAt(new Vector3f(-1, 0, 0), Vector3f.UNIT_Y);
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);

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

    public RigidBodyControl initPalet(){
        float radius = 1.0f; // Rayon du cylindre
        float height = 1f; // Hauteur du cylindre
        int radialSamples = 32; // Nombre d'échantillons radiaux pour le cylindre (doit être >= 3)
        int axialSamples = 2; // Nombre d'échantillons axiaux pour le cylindre (doit être >= 2)
        boolean closed = true; // Le cylindre est fermé à une extrémité

        Geometry geom = new Geometry("Cylinder", new Cylinder( axialSamples, radialSamples, radius, height, closed));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        geom.rotate(FastMath.HALF_PI, 0, 0);
        mat.setColor("Color", ColorRGBA.Red); // Couleur du matériau
        geom.setMaterial(mat);                   // set the cube's material





        RigidBodyControl box_phy = new RigidBodyControl(1f);
        /** Add physical brick to physics space. */
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

        // je crée le cylindre
        Cylinder cylinderMesh = new Cylinder(32, 32, 2f, cylinderHeight, true);
        Geometry cylinderGeom = new Geometry("Cylinder", cylinderMesh);
        Material cylinderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        cylinderMat.setColor("Color", ColorRGBA.Blue);
        cylinderGeom.setMaterial(cylinderMat);


        // je crée la sphère
        Sphere sphereMesh = new Sphere(32, 32, sphereRadius);
        Geometry sphereGeom = new Geometry("Sphere", sphereMesh);
        Material sphereMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sphereMat.setColor("Color", ColorRGBA.Red);
        sphereGeom.setMaterial(sphereMat);


        // je positionne la sphère au sommet du cylindre
        sphereGeom.setLocalTranslation(new Vector3f(0, cylinderHeight + sphereRadius, 0));

        cylinderGeom.setLocalTranslation(0, 0f, 0);
        sphereGeom.setLocalTranslation(0, 1.2f, 0f);
        cylinderGeom.rotate(FastMath.HALF_PI, 0, 0);

        //tu crées tes 2 objets à lier ...... dans mon cas cylinderGeom et sphereGeom
        Node compositeNode = new Node("CompositeObject");
        compositeNode.attachChild(cylinderGeom);
        compositeNode.attachChild(sphereGeom);

        //je bouge les 2 objets liées en même temps en utilisante le composite node
        compositeNode.move(4f, 2f, 4f);

        RigidBodyControl box_phy = new RigidBodyControl(1f);


        compositeNode.addControl(box_phy);
        bulletAppState.getPhysicsSpace().add(box_phy);
        box_phy.setRestitution(0.0f);
        box_phy.setFriction(0f);

        rootNode.attachChild(compositeNode);
        return box_phy;

    }


    public RigidBodyControl[] initWalls(){
        RigidBodyControl[] ans = new RigidBodyControl[4];
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
        ans[0] = wall_phy;
        ans[1] = wall_phy2;
        ans[2] = wall_phy3;
        ans[3] = wall_phy4;
        return ans;
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
        inputManager.addMapping("Click", new MouseAxisTrigger(MouseInput.BUTTON_LEFT, true));
    }




    public void simpleUpdate(float tpf) {

        Vector3f bloqueRaquette;
        Vector3f bloquePalet;

        Vector3f rotatePalet;

        player.setAngularVelocity(new Vector3f(0f, 0f, 0f));
        bloqueRaquette = player.getLinearVelocity();
        player.setLinearVelocity(new Vector3f(bloqueRaquette.x,0f,bloqueRaquette.z));

        rotatePalet = palet.getAngularVelocity();
        palet.setAngularVelocity(new Vector3f(rotatePalet.x, 0f, 0f));
        bloquePalet = palet.getLinearVelocity();
        palet.setLinearVelocity(new Vector3f(bloquePalet.x,0f,bloquePalet.z));


        if (click == true) {

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
                if (results.getCollision(i).getGeometry().getName() == "Floor") {


                    Vector3f posSouris = results.getCollision(i).getContactPoint();
                    Vector3f posPalet = player.getPhysicsLocation();

                    // Pour eviter que la raquette est Parkinson
                    if ((Math.abs(posSouris.x - posPalet.x) > 0.1) || (Math.abs(posSouris.z - posPalet.z) > 0.1)) {
                        Vector3f direction = results.getCollision(i).getContactPoint().subtract(player.getPhysicsLocation());
                        float distance = direction.length();
                        // Normaliser le vecteur de déplacement pour avoir une direction unitaire
                        direction = direction.normalize();
                        float speedMultiplier = Math.min(distance / 6, 1.0f);
                        // Appliquer le déplacement au joueur
                        player.setLinearVelocity(direction.mult(speedMultiplier * 75)); // Multiplier par une vitesse de déplacement
                    }
                    else {
                        // Arreter le déplacement du joueur
                        player.setLinearVelocity(new Vector3f(0f,0f,0f));
                    }
                }
            }
        }
    }

    public void onAction(String a, boolean b, float tpf) {

    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("LeftClick") && isPressed) {
                click = true;
            }
            //Fin du clic gauche
            else {
                click = false;
            }
        }
    };

}
