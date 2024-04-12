package fr.utln.airhockey;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

import javax.annotation.Nonnull;

public class StartScreenState extends BaseAppState implements ScreenController {
    private Nifty nifty;
    private Application app;

    @Override
    protected void initialize(Application app) {
        System.out.println("Initialize calles. App is: " + app);
        this.app = app;
        NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
                app.getAssetManager(),
                app.getInputManager(),
                app.getAudioRenderer(),
                app.getGuiViewPort());

        nifty = niftyDisplay.getNifty();
        app.getInputManager().setCursorVisible(true);

        nifty.fromXml("Interface/Screens.xml", "start", this);



        nifty.gotoScreen("start");

        app.getGuiViewPort().addProcessor(niftyDisplay);
    }

    @Override
    protected void cleanup(Application application) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void bind(@Nonnull Nifty nifty, @Nonnull Screen screen) {
        this.nifty = nifty;
    }

    @Override
    public void onStartScreen() {
        // Called when the screen gets shown


    }

    @Override
    public void onEndScreen() {
        // Called when the screen gets hidden
    }




    public void quitGame() {
        if (app != null) {
            app.stop();
        } else {
            System.out.println("Application is null, cannot stop the game.");
        }
    }

    public void startGame1vsIA() {
        nifty.gotoScreen("emptyScreen");

        AppSettings actualSettings = getApplication().getContext().getSettings();
        int width = actualSettings.getWidth();
        int height = actualSettings.getHeight();


        AppSettings settings = new AppSettings(true);
        settings.setUseJoysticks(true);
        settings.setFullscreen(true);
        settings.setResolution(width, height);
        Main appjeu = new Main();
        appjeu.setSettings(settings);
        appjeu.setShowSettings(false);
        appjeu.start();


    }

    public void startGame1vs1() {
        // Add code to start game
    }

    public void startGameNetwork() {
        // Add code to start game
    }
}