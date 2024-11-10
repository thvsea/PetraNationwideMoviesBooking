package com.example.petranationwidemovies;

import com.example.petranationwidemovies.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // open login window
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("LoginView.fxml"));
        stage.setResizable(false);

        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        Image image = new Image("/logo.png");
        stage.getIcons().add(image);

        stage.setTitle("Petra Nationwide Movies");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
//         DatabaseConnection.migrate();
        launch();
    }
}

// REPORT
// SELECT SUM(total_price) AS "TOTAL PENJUALAN" FROM bookings; (total penjualan)

// ANALISIS
// SELECT e.name AS "MOVIE TERBANYAK DIBELI" FROM movies e JOIN bookings j ON j.movie_id = e.id HAVING MAX(booked_seat); (MOVIE TERBANYAK DIBELI)
// SELECT e.name
//FROM payment_methods e
//JOIN bookings j ON j.payment_method_id = e.id
//HAVING MAX(COUNT(j.payment_method_id))