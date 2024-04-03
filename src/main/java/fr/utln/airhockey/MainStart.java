package fr.utln.airhockey;

import com.jme3.app.SimpleApplication;

public class MainStart extends SimpleApplication {
    public static void main(String[] args) {
        MainStart app = new MainStart();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        StartScreenState startScreenState = new StartScreenState();
        stateManager.attach(startScreenState);
        startScreenState.setEnabled(true);
        inputManager.setCursorVisible(true);


    }

}
