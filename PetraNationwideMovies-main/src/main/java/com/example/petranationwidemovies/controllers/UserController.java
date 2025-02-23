package com.example.petranationwidemovies.controllers;

import com.example.petranationwidemovies.Main;
import com.example.petranationwidemovies.model.*;
import com.example.petranationwidemovies.repositories.BookingRepository;
import com.example.petranationwidemovies.repositories.MovieRepository;
import com.example.petranationwidemovies.repositories.PaymentMethodRepository;
import com.example.petranationwidemovies.repositories.UserRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.function.UnaryOperator;

public class UserController implements Initializable {
    // Variable navigasi
    @FXML
    private Button homeButton;
    @FXML
    private Button transactionButton;
    @FXML
    private Button profileButton;
    @FXML
    private Button logoutButton;

    // Variable untuk home
    @FXML
    TableView movieTableView = new TableView();
    ObservableList<Map<String, Object>> movieItems = FXCollections.<Map<String, Object>>observableArrayList();

    // Variable untuk transaction
    @FXML
    TableView transactionTableView = new TableView();
    ObservableList<Map<String, Object>> transactionItems = FXCollections.<Map<String, Object>>observableArrayList();

    // Variable movie popup
    @FXML
    public ImageView bookingMovieImage = new ImageView();
    @FXML
    public Label bookingMovieName = new Label();
    @FXML
    public Label bookingMoviePrice = new Label();
    @FXML
    public TextField totalTicketField;
    @FXML
    public ChoiceBox<String> paymentMethodField = new ChoiceBox<>();;
    ObservableList<String> paymentItems = FXCollections.observableArrayList();
    @FXML
    public Label errorMessageLabel;
    @FXML
    public Button bookBtn;

    // Variable profile page
    @FXML
    public TextField usernameField = new TextField();
    @FXML
    public TextField nrpField = new TextField();
    @FXML
    public TextField phoneField;
    @FXML
    public PasswordField passwordField = new PasswordField();
    @FXML
    public PasswordField newpasswordField = new PasswordField();

    // Inisialisasi data page" user untuk setiap url
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // ngeset data yg ada di homeview (tabel yg isinya movie)
        if(url.toString().contains("HomeView")){
            setMovieTableView();
        }
        else if(url.toString().contains("TransactionView")){
            setTransactionTableView();
        }
        else if(url.toString().contains("TransactionModal")){
            setBookingPopup();
        }
        else if(url.toString().contains("ProfileView")){
            setProfileData();
        }
    }

    // Function untuk memanggil data dan menempelkan kedalam elemen pada view
    public void setMovieTableView(){
        Platform.runLater(() -> {
            TableColumn<Map, String> idColumn = new TableColumn<>("id");
            idColumn.setCellValueFactory(new MapValueFactory<>("id"));
            TableColumn<Map, String> nameColumn = new TableColumn<>("name");
            nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
            TableColumn<Map, String> startColumn = new TableColumn<>("start_date");
            startColumn.setCellValueFactory(new MapValueFactory<>("start_date"));
            TableColumn<Map, String> endColumn = new TableColumn<>("end_date");
            endColumn.setCellValueFactory(new MapValueFactory<>("end_date"));
            TableColumn<Map, String> timeColumn = new TableColumn<>("playing_time");
            timeColumn.setCellValueFactory(new MapValueFactory<>("playing_time"));
            TableColumn<Map, String> priceColumn = new TableColumn<>("price (Rp)");
            priceColumn.setCellValueFactory(new MapValueFactory<>("price"));
            TableColumn<Map, String> locationColumn = new TableColumn<>("location");
            locationColumn.setCellValueFactory(new MapValueFactory<>("location"));
            TableColumn<Map, String> seatColumn = new TableColumn<>("available_seat");
            seatColumn.setCellValueFactory(new MapValueFactory<>("available_seat"));

            movieTableView.getColumns().add(idColumn);
            movieTableView.getColumns().add(nameColumn);
            movieTableView.getColumns().add(startColumn);
            movieTableView.getColumns().add(endColumn);
            movieTableView.getColumns().add(timeColumn);
            movieTableView.getColumns().add(priceColumn);
            movieTableView.getColumns().add(locationColumn);
            movieTableView.getColumns().add(seatColumn);

            MovieRepository movieRepository = new MovieRepository();
            BookingRepository bookingRepository = new BookingRepository();
            try {
                List<Movie> movies = (List<Movie>) movieRepository.get();
                for (Movie movie: movies) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", movie.getId());
                    item.put("name", movie.getName());
                    item.put("start_date" , movie.getStart_date().toString());
                    item.put("end_date", movie.getEnd_date().toString());
                    item.put("playing_time" , movie.getPlaying_time().toString());
                    item.put("price", movie.getPrice());
                    item.put("location" ,  movie.getLocation().getBuilding()+"-"+ movie.getLocation().getRoom());

                    Integer booked_seat = (Integer) bookingRepository.getBookedSeatForMovieId(movie.getId());
                    item.put("available_seat" ,  (movie.getLocation().getTotal_seat() - booked_seat));
                    movieItems.add(item);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            movieTableView.getItems().addAll(movieItems);
        });

        movieTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2){
                    Map<String, Object> movie = (Map<String, Object>) movieTableView.getSelectionModel().getSelectedItem();
                    MovieRepository movieRepository = new MovieRepository();
                    int movieId = (int) movie.get("id");
                    int availableSeat = (int) movie.get("available_seat");

                    if(availableSeat == 0){
                        errorMessageLabel.setText("* Kursi untuk film ini sudah tidak tersedia!");
                        return ;
                    }

                    try {
                        SelectedMovie.setMovie((Movie) movieRepository.get(movieId));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    // OPEN TRANSACTION PAGE
                    Stage stage = new Stage();
                    Parent root = null;
                    try {
                        root = FXMLLoader.load(Main.class.getResource("TransactionModal.fxml"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    Scene scene = new Scene(root, 640, 480);
                    stage.setResizable(false);
                    stage.setScene(scene);
                    stage.setTitle("Booking Ticket");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.showAndWait();

                    // update movie table
                    updateMovieTableView();
                    errorMessageLabel.setText("* Tiket untuk movie "+movie.get("name") + " berhasil dibeli!");
                    errorMessageLabel.setTextFill(Color.GREEN);
                }
            }
        });
    }
    public void updateMovieTableView(){
        // Update tabel
        MovieRepository movieRepository = new MovieRepository();
        BookingRepository bookingRepository = new BookingRepository();
        try {
            List<Movie> movies = (List<Movie>) movieRepository.get();
            for (int i = 0; i < movies.size(); i++) {
                Movie movie = movies.get(i);
                movieItems.get(i).replace("id", movie.getId());
                movieItems.get(i).replace("name", movie.getName());
                movieItems.get(i).replace("start_date" , movie.getStart_date().toString());
                movieItems.get(i).replace("end_date", movie.getEnd_date().toString());
                movieItems.get(i).replace("playing_time" , movie.getPlaying_time().toString());
                movieItems.get(i).replace("price", movie.getPrice());
                movieItems.get(i).replace("location" ,  movie.getLocation().getBuilding()+"-"+ movie.getLocation().getRoom());

                Integer booked_seat = (Integer) bookingRepository.getBookedSeatForMovieId(movie.getId());
                movieItems.get(i).replace("available_seat" ,  (movie.getLocation().getTotal_seat() - booked_seat));
            }
            movieTableView.refresh();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void setTransactionTableView() {
        Platform.runLater(() -> {
            TableColumn<Map, String> idColumn = new TableColumn<>("id");
            idColumn.setCellValueFactory(new MapValueFactory<>("id"));
            TableColumn<Map, String> movie = new TableColumn<>("movie");
            movie.setCellValueFactory(new MapValueFactory<>("movie"));
            TableColumn<Map, String> booked_seat = new TableColumn<>("booked_seat");
            booked_seat.setCellValueFactory(new MapValueFactory<>("booked_seat"));
            TableColumn<Map, String> total_price = new TableColumn<>("total_price (Rp)");
            total_price.setCellValueFactory(new MapValueFactory<>("total_price"));
            TableColumn<Map, String> payment_method = new TableColumn<>("payment_method");
            payment_method.setCellValueFactory(new MapValueFactory<>("payment_method"));

            transactionTableView.getColumns().add(idColumn);
            transactionTableView.getColumns().add(movie);
            transactionTableView.getColumns().add(booked_seat);
            transactionTableView.getColumns().add(total_price);
            transactionTableView.getColumns().add(payment_method);

            BookingRepository bookingRepository = new BookingRepository();
            try {
                List<Booking> bookings = (List<Booking>) bookingRepository.getByUserId(UserLogin.getUser().getId());
                for (Booking booking: bookings) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", booking.getId());
                    item.put("movie", booking.getMovie().getName());
                    item.put("booked_seat" , booking.getBooked_seat());
                    item.put("total_price", booking.getTotal_price());
                    item.put("payment_method" , booking.getPaymentMethod().getName());

                    transactionItems.add(item);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            transactionTableView.getItems().addAll(transactionItems);
        });
    }
    public void setBookingPopup(){
        Platform.runLater(() -> {
            if(SelectedMovie.getMovie() != null){
                Image image = new Image(SelectedMovie.getMovie().getImage());
                bookingMovieImage.setImage(image);
                bookingMovieName.setText(SelectedMovie.getMovie().getName());
                bookingMoviePrice.setText("Price : "+SelectedMovie.getMovie().getPrice());

                PaymentMethodRepository paymentMethodRepository = new PaymentMethodRepository();
                try {
                    List<PaymentMethod> paymentMethods = (List<PaymentMethod>) paymentMethodRepository.get();
                    for (PaymentMethod paymentMethod: paymentMethods) {
                        paymentItems.add(paymentMethod.getName());
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                paymentMethodField.getItems().addAll(paymentItems);
                paymentMethodField.getSelectionModel().selectFirst();
            }
        });
    }
    public void setProfileData(){
        Platform.runLater(() -> {
            usernameField.setText(UserLogin.getUser().getName());
            nrpField.setText(UserLogin.getUser().getNrp());
            phoneField.setText(UserLogin.getUser().getPhone());
        });
    }


    // Function Navigasi antar page
    @FXML
    private void homeButtonClicked() throws IOException {
        Stage stageTheLabelBelongs = (Stage) homeButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("HomeView.fxml"));
        stageTheLabelBelongs.setResizable(false);
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stageTheLabelBelongs.setScene(scene);
    }
    @FXML
    private void transactionButtonClicked() throws IOException {
        Stage stageTheLabelBelongs = (Stage) transactionButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TransactionView.fxml"));
        stageTheLabelBelongs.setResizable(false);
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stageTheLabelBelongs.setScene(scene);
    }
    @FXML
    private void profileButtonClicked() throws IOException {
        Stage stageTheLabelBelongs = (Stage) profileButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ProfileView.fxml"));
        stageTheLabelBelongs.setResizable(false);
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stageTheLabelBelongs.setScene(scene);
    }
    @FXML
    private void logoutButtonClicked() throws IOException {
        Stage stageTheLabelBelongs = (Stage) logoutButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("LoginView.fxml"));
        stageTheLabelBelongs.setResizable(false);
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stageTheLabelBelongs.setScene(scene);
    }

    // Function Page Transaksi
    @FXML
    private void bookButtonClicked() throws IOException, SQLException {
        BookingRepository bookingRepository = new BookingRepository();
        PaymentMethodRepository paymentMethodRepository = new PaymentMethodRepository();


        int totalTicket = Integer.parseInt(totalTicketField.getText());
        int method = paymentMethodField.getSelectionModel().getSelectedIndex()+1;

        Integer booked_seat = (Integer) bookingRepository.getBookedSeatForMovieId(SelectedMovie.getMovie().getId());
        int available_seat = SelectedMovie.getMovie().getLocation().getTotal_seat() - booked_seat;
        if(available_seat - totalTicket < 0){
            errorMessageLabel.setText("Jumlah tiket melebihi kursi yang tersedia!");
            return;
        }

        PaymentMethod paymentMethod = (PaymentMethod) paymentMethodRepository.get(method);

        Booking newBooking = new Booking();
        newBooking.setTotal_price(SelectedMovie.getMovie().getPrice()*totalTicket);
        newBooking.setBooked_seat(totalTicket);
        newBooking.setUser(UserLogin.getUser());
        newBooking.setMovie(SelectedMovie.getMovie());
        newBooking.setPaymentMethod(paymentMethod);

        System.out.println(newBooking);
        bookingRepository.add(newBooking);

        Stage stageTheLabelBelongs = (Stage) bookBtn.getScene().getWindow();
        stageTheLabelBelongs.close();
    }

    // Function Page Profile
    @FXML
    private void updateButtonClicked() {
        UserRepository userRepository = new UserRepository();

        User newUser = new User();
        newUser.setName(usernameField.getText());
        newUser.setNrp(nrpField.getText());
        newUser.setNrp(phoneField.getText());
        newUser.setPassword(newpasswordField.getText());
        newUser.setId(UserLogin.getUser().getId());

        if(!passwordField.getText().equals(UserLogin.getUser().getPassword())){
            errorMessageLabel.setText("Password invalid");
            return;
        }
        try {
            userRepository.update(newUser);
            errorMessageLabel.setTextFill(Color.GREEN);
            errorMessageLabel.setText("Update berhasil!");
            passwordField.setText("");
            newpasswordField.setText("");
            UserLogin.setUser(newUser);
        }
        catch (SQLException e){
            errorMessageLabel.setText("Gagal update");
        }

    }

}
