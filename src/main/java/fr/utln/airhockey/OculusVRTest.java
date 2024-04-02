package fr.utln.airhockey;

import com.jme3.app.*;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.vr.VRInputAPI;
import com.jme3.input.vr.VRInputType;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OculusVRTest extends SimpleApplication {
    private BulletAppState bulletAppState;
    private int score = 0;
    private Timer timer;
    private BitmapText scoreText;
    private BitmapText timeText;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.put(VRConstants.SETTING_VRAPI, VRConstants.SETTING_VRAPI_OPENVR_LWJGL_VALUE);
        settings.put(VRConstants.SETTING_ENABLE_MIRROR_WINDOW, true);

        VREnvironment env = new VREnvironment(settings);
        env.initialize();

        // Checking if the VR environment is well initialized
        // (access to the underlying VR system is effective, VR devices are detected).
        if (env.isInitialized()){
            VRAppState vrAppState = new VRAppState(settings, env);
            vrAppState.setMirrorWindowSize(1024, 800);
            OculusVRTest app = new OculusVRTest(vrAppState);
            app.setLostFocusBehavior(LostFocusBehavior.Disabled);
            app.setSettings(settings);
            app.setShowSettings(false);
            app.start();
        }
    }

    public OculusVRTest(VRAppState appStates) {
        super(appStates);
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        getStateManager().attach(bulletAppState);

        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        // Create a control for the box with a mass of 1.0f
        RigidBodyControl boxControl = new RigidBodyControl(1.0f);

        // Add the control to the box
        geom.addControl(boxControl);

        // Add the box to the physics space
        bulletAppState.getPhysicsSpace().add(boxControl);

        // Now that the control is added to the box and the physics space, you can set its gravity
        boxControl.setGravity(Vector3f.ZERO);
        boxControl.setAngularFactor(0.0f);
        boxControl.setLinearVelocity(Vector3f.ZERO);

        //Affichage du score
        scoreText = new BitmapText(guiFont, false);
        scoreText.setName("scoreText");
        scoreText.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText.setColor(ColorRGBA.White);
        scoreText.setText("Score: " + score);
        scoreText.setLocalTranslation(-50, 100, -200);
        rootNode.attachChild(scoreText);

        // Créer une nouvelle instance de Timer
        timer = new Timer();

        // Créer un BitmapText pour le temps restant
        timeText = new BitmapText(guiFont, false);
        timeText.setName("timeText");
        timeText.setSize(guiFont.getCharSet().getRenderedSize());
        timeText.setColor(ColorRGBA.White);
        timeText.setText("Temps restant : 30");
        timeText.setLocalTranslation(-50, 150, -200); // Ajustez la position comme nécessaire
        rootNode.attachChild(timeText);

        // Créer une nouvelle instance de TimerTask
        TimerTask countdownTask = new TimerTask() {
            int remainingSeconds = 30;

            @Override
            public void run() {
                // Ce code sera exécuté chaque seconde
                remainingSeconds--;
                timeText.setText("Temps restant : " + remainingSeconds);

                // Si le temps est écoulé, afficher le score final
                if (remainingSeconds <= 0) {
                    timeText.setText("Temps écoulé ! Score final : " + score+" \n Appuyez sur 'Grip' pour recommencer une partie");
                    score=0;
                    scoreText.setText("");
                    timer.cancel(); // Arrêter le timer
                }
            }
        };

        // Démarrer le timer pour qu'il se termine après 30 secondes (30000 millisecondes)
        // et exécute countdownTask chaque seconde (1000 millisecondes)
        timer.scheduleAtFixedRate(countdownTask, 0, 1000);


        // Add a collision listener to the bulletAppState
        bulletAppState.getPhysicsSpace().addCollisionListener(event -> {
            if (("Box".equals(event.getNodeA().getName()) && "Bullet".equals(event.getNodeB().getName()))
                    || ("Bullet".equals(event.getNodeA().getName()) && "Box".equals(event.getNodeB().getName()))) {
                score++;
                scoreText.setText("Score: " + score);
                // The box was hit by a bullet, move it to a new random location
                Geometry box;
                RigidBodyControl hitBoxControl;
                if ("Box".equals(event.getNodeA().getName())) {
                    box = (Geometry) event.getNodeA();
                    hitBoxControl = box.getControl(RigidBodyControl.class);


                } else {
                    box = (Geometry) event.getNodeB();
                    hitBoxControl = box.getControl(RigidBodyControl.class);

                }
                // Set the box to kinematic
                hitBoxControl.setKinematic(true);

                Vector3f newPosition = new Vector3f((float) Math.random() * 10 - 5, (float) Math.random() * 10 - 5, (float) Math.random() * 10 - 2);
                box.setLocalTranslation(newPosition);
                hitBoxControl.setPhysicsLocation(newPosition);
                hitBoxControl.setGravity(Vector3f.ZERO);
                hitBoxControl.setLinearVelocity(Vector3f.ZERO);

                // Enqueue a runnable to be run in the next update loop after a delay
                this.enqueue(() -> {
                    // This will be run in the next update loop after the delay
                    // Reset the box to dynamic
                    hitBoxControl.setKinematic(false);
                    hitBoxControl.setLinearVelocity(Vector3f.ZERO);
                    return null;
                });

            }else if (("resetBox".equals(event.getNodeA().getName()) && "Bullet".equals(event.getNodeB().getName()))
                    || ("Bullet".equals(event.getNodeA().getName()) && "resetBox".equals(event.getNodeB().getName()))) {
                score = 0;
                scoreText.setText("Score: " + score);
            }
        });

        // Ajouter une source de lumière directionnelle
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0,0,-1).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        rootNode.attachChild(geom);
        viewPort.setClearColor(true);
        viewPort.setBackgroundColor(ColorRGBA.Black);
    }

    private Geometry createLine(Vector3f start, Vector3f end) {
        Line line = new Line(start, end);
        Geometry geometry = new Geometry("ControllerLine", line);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        geometry.setMaterial(mat);
        return geometry;
    }

    List<Geometry> handGeometries = new ArrayList<>();

    @Override
    public void simpleUpdate(float tpf) {
        VRAppState vrAppState = getStateManager().getState(VRAppState.class);
        int numberOfControllers = vrAppState.getVRinput().getTrackedControllerCount(); //almost certainly 2, one for each hand

        //build as many geometries as hands, as markers for the demo (Will only tigger on first loop or if number of controllers changes)
        while(handGeometries.size()<numberOfControllers){
            Box b = new Box(0.1f, 0.1f, 0.1f);
            Geometry handMarker = new Geometry("hand", b);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Red);
            handMarker.setMaterial(mat);
            rootNode.attachChild(handMarker);
            handGeometries.add(handMarker);
        }

        VRInputAPI vrInput = vrAppState.getVRinput();
        for(int i=0;i<numberOfControllers;i++){
            if (vrInput.isInputDeviceTracking(i)){ //might not be active currently, avoid NPE if that's the case
                Vector3f position = vrInput.getFinalObserverPosition(i);
                Quaternion rotation = vrInput.getFinalObserverRotation(i);

                Geometry geometry = handGeometries.get(i);
                geometry.setLocalTranslation(position);
                geometry.setLocalRotation(rotation);
                boolean grip = vrInput.isButtonDown(i, VRInputType.ViveGripButton); //<--Don't worry about the way it says "Vive", anything that supports SteamVR/OpenVR will work with this
                boolean trigger = vrInput.wasButtonPressedSinceLastCall(i, VRInputType.ViveTriggerAxis);
                boolean abutton = vrInput.isButtonDown(i, VRInputType.OculusBottomButton); //A button on Oculus Touch controllers (Xbox A button on Vive controllers
                if (grip){
                    // Réinitialiser le score
                    score = 0;
                    scoreText.setText("Score: " + score);

                    // Annuler le timer actuel
                    timer.cancel();

                    // Créer une nouvelle instance de Timer
                    timer = new Timer();

                    // Créer une nouvelle instance de TimerTask
                    TimerTask countdownTask = new TimerTask() {
                        int remainingSeconds = 30;

                        @Override
                        public void run() {
                            // Ce code sera exécuté chaque seconde
                            remainingSeconds--;
                            timeText.setText("Temps restant : " + remainingSeconds);

                            // Si le temps est écoulé, afficher le score final
                            if (remainingSeconds <= 0) {
                                timeText.setText("Score final : " + score);
                                timer.cancel(); // Arrêter le timer
                            }
                        }
                    };

                    // Démarrer le timer pour qu'il se termine après 30 secondes (30000 millisecondes)
                    // et exécute countdownTask chaque seconde (1000 millisecondes)
                    timer.scheduleAtFixedRate(countdownTask, 0, 1000);

                }else if (trigger){
                    vrInput.triggerHapticPulse(i, 0.8f);
                    geometry.getMaterial().setColor("Color", ColorRGBA.Yellow);
                    // Créer une nouvelle sphère (la balle)
                    Sphere sphere = new Sphere(32, 32, 0.1f);
                    Geometry bullet = new Geometry("Bullet", sphere);

                    // Créer un nouveau matériau éclairé
                    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

                    // Définir la couleur diffuse (la couleur principale de la balle)
                    mat.setColor("Diffuse", ColorRGBA.Cyan);

                    // Définir la couleur spéculaire (la couleur de la brillance) et la brillance
                    mat.setColor("Specular", ColorRGBA.White);
                    mat.setFloat("Shininess", 128f);  // [1,128] plus c'est élevé, plus c'est brillant

                    // Activer l'utilisation de la couleur diffuse dans l'éclairage
                    mat.setBoolean("UseMaterialColors", true);

                    // Définir le matériau de la balle
                    bullet.setMaterial(mat);

                    // Positionner la balle à la position du contrôleur
                    bullet.setLocalTranslation(position);

                    // Ajouter la balle à la scène
                    rootNode.attachChild(bullet);

                    // Create a control for the bullet with a mass of 1.0f
                    RigidBodyControl bulletControl = new RigidBodyControl(10.0f);

                    // Add the control to the bullet
                    bullet.addControl(bulletControl);

                    // Add the bullet to the physics space
                    bulletAppState.getPhysicsSpace().add(bulletControl);

                    // Set the bullet's velocity in the direction the controller is pointing
                    Vector3f direction = rotation.mult(Vector3f.UNIT_Z);
                    bulletControl.setLinearVelocity(direction.mult(75f));

                }else if(abutton){
                    System.out.println("A button pressed");
                    // Réinitialiser le score
                    score = 0;
                    scoreText.setText("Score: " + score);

                    // Annuler le timer actuel
                    timer.cancel();

                    // Créer une nouvelle instance de Timer
                    timer = new Timer();

                    // Créer une nouvelle instance de TimerTask
                    TimerTask countdownTask = new TimerTask() {
                        int remainingSeconds = 30;

                        @Override
                        public void run() {
                            // Ce code sera exécuté chaque seconde
                            remainingSeconds--;
                            timeText.setText("Temps restant : " + remainingSeconds);

                            // Si le temps est écoulé, afficher le score final
                            if (remainingSeconds <= 0) {
                                timeText.setText("Score final : " + score);
                                timer.cancel(); // Arrêter le timer
                            }
                        }
                    };

                    // Démarrer le timer pour qu'il se termine après 30 secondes (30000 millisecondes)
                    // et exécute countdownTask chaque seconde (1000 millisecondes)
                    timer.scheduleAtFixedRate(countdownTask, 0, 1000);
                }
                else{
                    geometry.getMaterial().setColor("Color", ColorRGBA.Red);
                }

                // Remove the old line from the scene
                if (rootNode.getChild("ControllerLine") != null) {
                    rootNode.detachChildNamed("ControllerLine");
                }

                // Create a new line from the controller's position in the direction the controller is pointing
                Vector3f direction = rotation.mult(Vector3f.UNIT_Z);
                Vector3f end = position.add(direction.mult(10f)); // The line will be 10 units long
                Geometry line = createLine(position, end);

                // Add the line to the scene
                rootNode.attachChild(line);





            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}