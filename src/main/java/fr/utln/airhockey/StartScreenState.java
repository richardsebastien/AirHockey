package fr.utln.airhockey;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class StartScreenState extends BaseAppState implements ScreenController {
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;

    @Override
    protected void initialize(Application app) {
        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
                app.getAssetManager(),
                app.getInputManager(),
                app.getAudioRenderer(),
                app.getGuiViewPort());

        nifty = niftyDisplay.getNifty();
        app.getInputManager().setCursorVisible(true);

        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");

        nifty.addScreen("start", new ScreenBuilder("start") {{
            controller(StartScreenState.this);
            layer(new LayerBuilder("background") {{
                backgroundColor("#808080");
                childLayoutCenter();
            }});
            layer(new LayerBuilder("foreground") {{
                backgroundColor("#0000");
                childLayoutVertical();
                panel(new PanelBuilder("panel_top") {{
                    height("25%");
                    width("75%");
                    childLayoutCenter();
                }});
                panel(new PanelBuilder("panel_mid") {{
                    height("5%");
                    width("100%");
                    childLayoutCenter();
                    control(new ButtonBuilder("StartButton", "Jouer en 1 vs IA") {{
                        alignCenter();
                        valignCenter();
                        visibleToMouse(true);
                        interactOnClick("startGame1vsIA()");
                    }});
                }});

                panel(new PanelBuilder("panel_mid2") {{
                    height("5%");
                    width("100%");
                    childLayoutCenter();
                    control(new ButtonBuilder("QuitButton", "Jouer en 1 vs 1") {{
                        alignCenter();
                        valignCenter();
                        visibleToMouse(true);
                        interactOnClick("startGame1vs1()");
                    }});
                }});
                panel(new PanelBuilder("panel_mid3") {{
                    height("5%");
                    width("100%");
                    childLayoutCenter();
                    control(new ButtonBuilder("QuitButton", "Jouer en réseau") {{
                        alignCenter();
                        valignCenter();
                        visibleToMouse(true);
                        interactOnClick("startGameNetwork()");
                    }});
                }});
                panel(new PanelBuilder("panel_mid4") {{
                    height("5%");
                    width("100%");
                    childLayoutCenter();
                    control(new ButtonBuilder("QuitButton", "Quitter") {{
                        alignCenter();
                        valignCenter();
                        visibleToMouse(true);
                        interactOnClick("quitGame()");
                    }});
                }});
                panel(new PanelBuilder("panel_bottom") {{
                    height("25%");
                    width("75%");
                    childLayoutCenter();

                }});
            }});
        }}.build(nifty));

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
    public void bind(Nifty nifty, Screen screen) {
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
        // Add code to quit game
        ((MainStart) getApplication()).stop();;
    }

    public void startGame1vsIA() {
        // Add code to start game
    }

    public void startGame1vs1() {
        // Add code to start game
    }

    public void startGameNetwork() {
        // Add code to start game
    }
}