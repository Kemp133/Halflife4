package GameUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/*get the outputStream from console and display them into a javafx windows
* This class is called after the server button is clicked*/
public class ServerDisplay extends Application {

    private static final double SCREEN_WIDTH = 800;
    private static final double SCREEN_HEIGHT = 600;

    private TextArea textArea = new TextArea();


    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Server display");
        System.setOut(new PrintStream(new OutDisplay(textArea)));
        stage.setScene(new Scene(textArea,SCREEN_WIDTH,SCREEN_HEIGHT, Color.WHITE));
        stage.show();
    }

}

class OutDisplay extends OutputStream {

    private final TextInputControl control;
    private final Charset charset;

    public OutDisplay(TextInputControl control) {
        this(control, Charset.defaultCharset());
    }

    public OutDisplay(TextInputControl control, Charset charset) {
        this.control = control;
        this.charset = charset;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b});
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /*Write the str using system.out into textArea*/
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        final String str = new String(b, off, len, this.charset);
        Platform.runLater(() -> this.control.appendText(str));
    }
}