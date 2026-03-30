package com.fitnessclub.ui;

import com.fitnessclub.dao.ClientDao;
import com.fitnessclub.dao.UserDao;
import com.fitnessclub.model.Role;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regPasswordConfirmField;
    @FXML private Label registerErrorLabel;

    private final ClientDao clientDao = new ClientDao();
    private final UserDao userDao = new UserDao();

    private TextField loginScreenUsernameField;

    public void setLoginUsernameField(TextField loginScreenUsernameField) {
        this.loginScreenUsernameField = loginScreenUsernameField;
    }

    @FXML
    private void initialize() {
        registerErrorLabel.setText("");
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 25));
        genderCombo.setItems(FXCollections.observableArrayList("М", "Ж", "Другое"));
        genderCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onCancel() {
        close();
    }

    @FXML
    private void onSubmit() {
        registerErrorLabel.setText("");
        String fullName = trim(fullNameField.getText());
        String phone = trim(phoneField.getText());
        String email = trim(emailField.getText());
        String username = trim(regUsernameField.getText());
        String pass = regPasswordField.getText() == null ? "" : regPasswordField.getText();
        String pass2 = regPasswordConfirmField.getText() == null ? "" : regPasswordConfirmField.getText();

        if (fullName.isEmpty()) {
            registerErrorLabel.setText("Укажите ФИО.");
            return;
        }
        if (phone.isEmpty() || email.isEmpty()) {
            registerErrorLabel.setText("Укажите телефон и email.");
            return;
        }
        if (!email.contains("@")) {
            registerErrorLabel.setText("Проверьте формат email.");
            return;
        }
        if (username.isEmpty()) {
            registerErrorLabel.setText("Придумайте логин.");
            return;
        }
        if (pass.length() < 4) {
            registerErrorLabel.setText("Пароль не короче 4 символов.");
            return;
        }
        if (!pass.equals(pass2)) {
            registerErrorLabel.setText("Пароли не совпадают.");
            return;
        }
        Integer age = ageSpinner.getValue();
        if (age == null || age < 1) {
            registerErrorLabel.setText("Укажите возраст.");
            return;
        }
        String gender = genderCombo.getValue();
        if (gender == null || gender.isEmpty()) {
            registerErrorLabel.setText("Выберите пол.");
            return;
        }

        try {
            if (userDao.findByUsername(username).isPresent()) {
                registerErrorLabel.setText("Такой логин уже занят.");
                return;
            }
            int clientId = clientDao.insert(fullName, age, gender, phone, email, null);
            try {
                userDao.insert(username, pass, fullName, Role.CLIENT, clientId);
            } catch (Exception ex) {
                try {
                    clientDao.delete(clientId);
                } catch (Exception ignored) {
                    // best-effort rollback
                }
                throw ex;
            }
            Optional<com.fitnessclub.model.User> nu = userDao.findByUsername(username);
            if (nu.isPresent()) {
                clientDao.setUserId(clientId, nu.get().getId());
            }
            if (loginScreenUsernameField != null) {
                loginScreenUsernameField.setText(username);
            }
            Alert ok = new Alert(Alert.AlertType.INFORMATION, "Теперь войдите, используя логин и пароль.");
            ok.setHeaderText("Регистрация выполнена");
            ok.showAndWait();
            close();
        } catch (Exception e) {
            registerErrorLabel.setText(e.getMessage() != null ? e.getMessage() : e.toString());
            e.printStackTrace();
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private void close() {
        Stage st = (Stage) registerErrorLabel.getScene().getWindow();
        if (st != null) {
            st.close();
        }
    }
}
