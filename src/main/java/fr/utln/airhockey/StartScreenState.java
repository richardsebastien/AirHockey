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
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

public class StartScreenState extends BaseAppState implements ScreenController {
    @Setter
    private Nifty nifty;
    @Setter
    private Main app;


    @Override
    protected void initialize(Application application) {

    }

    @Override
    protected void cleanup(Application application) {

    }

    @Override
    protected void onEnable() {
        NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
                app.getAssetManager(),
                app.getInputManager(),
                app.getAudioRenderer(),
                app.getGuiViewPort());

        this.nifty = niftyDisplay.getNifty();
        this.app.setNifty(this.nifty); // Assuming you have a setter for Nifty in your Main class

        // Load your XML and go to your screen
        nifty.fromXml("Interface/Screens.xml", "start", this);
        nifty.gotoScreen("start");

        app.getGuiViewPort().addProcessor(niftyDisplay);
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
        System.out.println("Quitting game");
        if (app != null) {
            app.stop();
        } else {
            System.out.println("Application is null, cannot stop the game.");
        }
    }

    public void startGame1vsIA() {
        app.setIsStarted(true);
        app.setIsPaused(false);
        nifty.gotoScreen("emptyScreen");

    }

    public void startGame1vs1() {
        // Add code to start game
    }

    public void startGameNetwork() {
        // Add code to start game
    }

    public void showSettings() {
        System.out.println("Showing settings");
        nifty.gotoScreen("settings");
    }

    public void showPause() {
        System.out.println("Showing pause");
        nifty.gotoScreen("pause");
    }
}