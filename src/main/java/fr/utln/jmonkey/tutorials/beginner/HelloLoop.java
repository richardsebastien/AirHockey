package fr.utln.jmonkey.tutorials.beginner;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/** Sample 4 - how to trigger repeating actions from the Main event loop.
 * In this example, you use the loop to make the player character
 * rotate continuously. */
public class HelloLoop extends SimpleApplication {

    public static void main(String[] args){
        HelloLoop app = new HelloLoop();
        app.start();
    }

    private Geometry player;

    @Override
    public void simpleInitApp() {
        /** this blue box is our player character */
        Box b = new Box(1, 1, 1);
        player = new Geometry("blue cube", b);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        player.setMaterial(mat);
        rootNode.attachChild(player);
        initKeys();
    }



    private void initKeys(){
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_L));

        inputManager.addListener(analogListener, "Up", "Down");
    }

    private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String s, float value, float tpf) {
            if (s.equals("Up")) {
                player.move(new Vector3f(0, -value, 0));
            }
            if (s.equals("Down")) {
                player.move(new Vector3f(0, 0, value));
            }
        }

    };
}