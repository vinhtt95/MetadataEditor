package com.vinhtt.metadataeditor;

import atlantafx.base.theme.Dracula;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vinhtt.metadataeditor.config.AppModule;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private final Injector injector = Guice.createInjector(new AppModule());

    @Override
    public void start(Stage stage) throws IOException {
        // Set Theme Dark Mode
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        // Load FXML và sử dụng Guice để tạo Controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setControllerFactory(injector::getInstance);

        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 700);

        stage.setTitle("Video Metadata Cleaner");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}