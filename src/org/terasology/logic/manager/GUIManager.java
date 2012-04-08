package org.terasology.logic.manager;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.components.UIMessageBox;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

import javax.vecmath.Vector2f;
import java.util.HashMap;

public class GUIManager {
    private static GUIManager _instance;
    private UIDisplayRenderer _renderer;
    private UIDisplayWindow   _focusedWindow;
    private UIDisplayWindow   _lastFocused;
    private HashMap<String, UIDisplayWindow> _windowsById = new HashMap<String, UIDisplayWindow>();

    public GUIManager(){
        _renderer = new UIDisplayRenderer();
        _renderer.setVisible(true);
    }

    public static GUIManager getInstance(){
        if(_instance == null){
            _instance = new GUIManager();
        }
        return _instance;
    }

    public void render(){
        _renderer.render();
    }

    public void update(){
        if(_focusedWindow==null){
            int size = _renderer.getDisplayElements().size();
            if(size>0){
                _focusedWindow = (UIDisplayWindow)_renderer.getDisplayElements().get(size-1);
            }
        }
        _renderer.update();
    }

    public void addWindow(UIDisplayWindow window, String windowId){
        if(window.isMaximized()){
            _renderer.addtDisplayElementToPosition(0,window);
        }else{
            _renderer.addDisplayElement(window);
        }

        _windowsById.put(windowId, window);
    }

    public void removeWindow(UIDisplayWindow window){
        _renderer.removeDisplayElement(window);

        if(_windowsById.containsValue(window)){
            for(String key : _windowsById.keySet()){
                if( _windowsById.get(key).equals(window) ){
                    _windowsById.remove(key);
                    break;
                }
            }
        }
    }

    public void removeWindow(String windowId){
        UIDisplayWindow window = getWindowById(windowId);

        _renderer.removeDisplayElement(window);

        if(_windowsById.containsValue(window)){
            for(String key : _windowsById.keySet()){
                if( _windowsById.get(key).equals(window) ){
                    _windowsById.remove(key);
                    break;
                }
            }
        }
    }

    public UIDisplayWindow getWindowById(String windowId){
        if( _windowsById.containsKey(windowId) ){
            return _windowsById.get(windowId);
        }else{
            return null;
        }
    }

    /**
     * Process keyboard input - first look for "system" like events, then otherwise pass to the Player object
     */
    public void processKeyboardInput(int key) {
        for (UIDisplayElement screen : _renderer.getDisplayElements()) {
            if (screen.isVisible() && !screen.isOverlay()) {
                screen.processKeyboardInput(key);
            }
        }
    }


    public void processMouseInput(int button, boolean state, int wheelMoved) {

        if(button==0 && state){
            checkTopWindow();
        }

        if(_focusedWindow != null){
            _focusedWindow.processMouseInput(button, state, wheelMoved);
        }
    }

    public void setFocusedWindow(UIDisplayWindow window){
        int size = _renderer.getDisplayElements().size();

        for(int i = 0; i < size; i++){
            if( window.equals( _renderer.getDisplayElements().get(i) ) ){
                setTopWindow(i);
                break;
            }
        }
    }

    public void setFocusedWindow(String windowId){
        if(_windowsById == null || _windowsById.size()<1 || !_windowsById.containsKey(windowId)){
            return;
        }
        setFocusedWindow(_windowsById.get(windowId));
    }

    private void setTopWindow(int windowPosition){
        _lastFocused = _focusedWindow;
        _focusedWindow = (UIDisplayWindow)_renderer.getDisplayElements().get(windowPosition);
        if( !_focusedWindow.isMaximized() ){
            _renderer.changeElementDepth(windowPosition, _renderer.getDisplayElements().size()-1);
        }
    }

    private void checkTopWindow(){

        if(_focusedWindow.isModal()&&_focusedWindow.isVisible()){
            return;
        }

        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        int size = _renderer.getDisplayElements().size();

        for(int i = size - 1; i>=0; i--){
            UIDisplayWindow window = (UIDisplayWindow)_renderer.getDisplayElements().get(i);
            if(window.isVisible() && window.intersects(mousePos)){
                setTopWindow(i);
                break;
            };
        }
    }

    public void showMessage(String title, String text){
        UIDisplayWindow messageWindow = new UIMessageBox(title, text);
        messageWindow.setVisible(true);
        messageWindow.center();
        addWindow(messageWindow, "messageBox");
        setFocusedWindow(messageWindow);
    }

    public void setLasFocused(){
        _focusedWindow = _lastFocused;
    }

    /*private boolean screenCanFocus(UIDisplayElement s) {
        boolean result = true;

        for (UIDisplayElement screen : _renderer.getDisplayElements()) {
            if (screen.isVisible() && !screen.isOverlay() && screen != s)
                result = false;
        }
        return result;
    } */
}
