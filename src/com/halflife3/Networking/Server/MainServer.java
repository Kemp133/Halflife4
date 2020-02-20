package com.halflife3.Networking.Server;
import com.halflife3.Controller.Input;
import com.halflife3.Controller.KeyHandle;
import com.halflife3.Controller.MouseInput;
import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.*;
import com.halflife3.Model.Interfaces.IRenderable;
import com.halflife3.Model.Interfaces.IUpdateable;
import com.halflife3.View.MapRender;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.FileInputStream;
import static javafx.scene.input.KeyCode.*;


public class MainServer {

    public static void main(String[] args) {

        Server server = new Server();
        server.start();
    }

}






