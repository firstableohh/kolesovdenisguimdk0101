package com.fitnessclub.ui;

import com.fitnessclub.dao.UserDao;
import com.fitnessclub.model.User;
import com.fitnessclub.service.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private javafx.scene.control.Button loginButton;

    private final UserDao userDao = new UserDao();

    @FXML
    private void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        String u = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String p = passwordField.getText() == null ? "" : passwordField.getText();
        if (u.isEmpty() || p.isEmpty()) {
            errorLabel.setText("Введите логин и пароль.");
            return;
        }
        try {
            Optional<User> opt = userDao.findByUsernameAndPassword(u, p);
            if (opt.isEmpty()) {
                errorLabel.setText("Неверный логин или пароль.");
                return;
            }
            Session.setUser(opt.get());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setCharset(StandardCharsets.UTF_8);
            Parent root = loader.load();
            stage.setTitle("Фитнес-клуб — " + opt.get().getFullName() + " (" + opt.get().getRole() + ")");
            Scene scene = new Scene(root, 1100, 720);
            var mainCss = getClass().getResource("/styles/main.css");
            if (mainCss != null) {
                scene.getStylesheets().add(mainCss.toExternalForm());
            }
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
        } catch (Exception e) {
            errorLabel.setText("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegister() {
        errorLabel.setText("");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            loader.setCharset(StandardCharsets.UTF_8);
            Parent root = loader.load();
            RegisterController reg = loader.getController();
            reg.setLoginUsernameField(usernameField);

            Stage owner = (Stage) loginButton.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Регистрация клиента");
            Scene sc = new Scene(root, 420, 560);
            var css = getClass().getResource("/styles/login.css");
            if (css != null) {
                sc.getStylesheets().add(css.toExternalForm());
            }
            dialog.setScene(sc);
            dialog.setMinWidth(380);
            dialog.setMinHeight(400);
            dialog.show();
        } catch (Exception e) {
            errorLabel.setText("Не удалось открыть регистрацию: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
