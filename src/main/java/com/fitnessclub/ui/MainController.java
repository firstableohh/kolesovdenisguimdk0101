package com.fitnessclub.ui;

import com.fitnessclub.dao.*;
import com.fitnessclub.model.*;
import com.fitnessclub.service.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class MainController {

    @FXML private TabPane tabPane;
    @FXML private Label toolbarTitle;
    @FXML private Label userInfoLabel;
    @FXML private Button logoutButton;

    private final ClientDao clientDao = new ClientDao();
    private final UserDao userDao = new UserDao();
    private final ScheduleDao scheduleDao = new ScheduleDao();
    private final PlanDao planDao = new PlanDao();
    private final ExerciseDao exerciseDao = new ExerciseDao();
    private final ProgressDao progressDao = new ProgressDao();
    private final AttendanceDao attendanceDao = new AttendanceDao();
    private final ReportDao reportDao = new ReportDao();

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        User u = Session.getUser();
        if (u == null) return;

        userInfoLabel.setText(u.getFullName() + "  ·  " + roleRu(u.getRole()));
        tabPane.sceneProperty().addListener((obs, oldSc, newSc) -> {
            if (newSc != null) {
                var url = getClass().getResource("/styles/main.css");
                if (url != null) {
                    String ext = url.toExternalForm();
                    if (!newSc.getStylesheets().contains(ext)) {
                        newSc.getStylesheets().add(ext);
                    }
                }
            }
        });

        switch (u.getRole()) {
            case ADMIN -> {
                tabPane.getTabs().add(buildClientsTab());
                tabPane.getTabs().add(buildScheduleTab());
                tabPane.getTabs().add(buildPlansTab());
                tabPane.getTabs().add(buildExercisesTab());
                tabPane.getTabs().add(buildReportsTab());
                tabPane.getTabs().add(buildUsersTab());
            }
            case TRAINER -> {
                tabPane.getTabs().add(buildScheduleTab());
                tabPane.getTabs().add(buildPlansTab());
                tabPane.getTabs().add(buildExercisesTabReadOnly());
                tabPane.getTabs().add(buildReportsTab());
            }
            case CLIENT -> {
                tabPane.getTabs().add(buildScheduleTab());
                tabPane.getTabs().add(buildPlansTab());
            }
        }
    }

    @FXML
    private void onLogout() {
        if (!confirm("Выйти из аккаунта?")) {
            return;
        }
        Session.clear();
        try {
            Stage stage = (Stage) tabPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setCharset(StandardCharsets.UTF_8);
            Parent root = loader.load();
            Scene scene = new Scene(root, 440, 340);
            var loginCss = getClass().getResource("/styles/login.css");
            if (loginCss != null) {
                scene.getStylesheets().add(loginCss.toExternalForm());
            }
            stage.setScene(scene);
            stage.setTitle("Фитнес-клуб — вход");
            stage.setMinWidth(380);
            stage.setMinHeight(280);
        } catch (Exception e) {
            alertError(e);
        }
    }

    private static String roleRu(Role r) {
        return switch (r) {
            case ADMIN -> "Администратор";
            case TRAINER -> "Тренер";
            case CLIENT -> "Клиент";
        };
    }

    private static void styleDataTable(TableView<?> table) {
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private static void decorateCrud(Button add, Button upd, Button del) {
        add.getStyleClass().add("btn-action");
        upd.getStyleClass().add("btn-secondary");
        del.getStyleClass().add("btn-danger");
    }

    private static Label formLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-label");
        return l;
    }

    private static Label sectionLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-label");
        return l;
    }

    private Tab buildClientsTab() {
        Tab tab = new Tab("Клиенты");
        TableView<Client> table = new TableView<>();
        ObservableList<Client> data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Client, String> c1 = new TableColumn<>("Имя");
        c1.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Client, Integer> c2 = new TableColumn<>("Возраст");
        c2.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAge()).asObject());
        TableColumn<Client, String> c3 = new TableColumn<>("Пол");
        c3.setCellValueFactory(new PropertyValueFactory<>("gender"));
        TableColumn<Client, String> c4 = new TableColumn<>("Телефон");
        c4.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<Client, String> c5 = new TableColumn<>("Email");
        c5.setCellValueFactory(new PropertyValueFactory<>("email"));
        table.getColumns().addAll(c1, c2, c3, c4, c5);
        styleDataTable(table);

        TextField nameF = new TextField();
        nameF.setPromptText("Имя");
        Spinner<Integer> ageS = new Spinner<>(1, 120, 25);
        ComboBox<String> genderC = new ComboBox<>(FXCollections.observableArrayList("М", "Ж", "Другое"));
        genderC.getSelectionModel().selectFirst();
        TextField phoneF = new TextField();
        phoneF.setPromptText("Телефон");
        TextField emailF = new TextField();
        emailF.setPromptText("Email");

        Runnable refresh = () -> {
            try {
                data.setAll(clientDao.findAll());
            } catch (Exception e) {
                alertError(e);
            }
        };
        refresh.run();

        Button add = new Button("Добавить");
        add.setOnAction(e -> {
            try {
                clientDao.insert(nameF.getText().trim(), ageS.getValue(), genderC.getValue(),
                        phoneF.getText().trim(), emailF.getText().trim(), null);
                refresh.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });
        Button upd = new Button("Изменить");
        upd.setOnAction(e -> {
            Client sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            try {
                clientDao.update(sel.getId(), nameF.getText().trim(), ageS.getValue(), genderC.getValue(),
                        phoneF.getText().trim(), emailF.getText().trim(), sel.getUserId());
                refresh.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });
        Button del = new Button("Удалить");
        decorateCrud(add, upd, del);
        del.setOnAction(e -> {
            Client sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            if (!confirm("Удалить клиента?")) return;
            try {
                clientDao.delete(sel.getId());
                refresh.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });
        table.getSelectionModel().selectedItemProperty().addListener((o, a, sel) -> {
            if (sel != null) {
                nameF.setText(sel.getName());
                ageS.getValueFactory().setValue(sel.getAge());
                genderC.setValue(sel.getGender());
                phoneF.setText(sel.getPhone());
                emailF.setText(sel.getEmail());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(8));
        int r = 0;
        form.addRow(r++, formLabel("Имя"), nameF);
        form.addRow(r++, formLabel("Возраст"), ageS);
        form.addRow(r++, formLabel("Пол"), genderC);
        form.addRow(r++, formLabel("Телефон"), phoneF);
        form.addRow(r++, formLabel("Email"), emailF);
        HBox btns = new HBox(8, add, upd, del);
        form.addRow(r, btns);

        SplitPane sp = new SplitPane();
        sp.getStyleClass().add("main-split");
        sp.setDividerPositions(0.55);
        VBox left = new VBox(8, sectionLabel("Список клиентов"), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        sp.getItems().addAll(left, new ScrollPane(form));
        tab.setContent(sp);
        return tab;
    }

    private Tab buildScheduleTab() {
        Tab tab = new Tab("Расписание");
        User u = Session.getUser();
        TableView<TrainingSchedule> table = new TableView<>();
        ObservableList<TrainingSchedule> data = FXCollections.observableArrayList();

        TableColumn<TrainingSchedule, String> t1 = new TableColumn<>("Тренер");
        t1.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
        TableColumn<TrainingSchedule, String> t2 = new TableColumn<>("Клиент");
        t2.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        TableColumn<TrainingSchedule, String> t3 = new TableColumn<>("Начало");
        t3.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStartTime().format(DT)));
        TableColumn<TrainingSchedule, String> t4 = new TableColumn<>("Конец");
        t4.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEndTime().format(DT)));
        TableColumn<TrainingSchedule, String> t5 = new TableColumn<>("Заметки");
        t5.setCellValueFactory(new PropertyValueFactory<>("notes"));
        table.getColumns().addAll(t1, t2, t3, t4, t5);
        table.setItems(data);
        styleDataTable(table);

        ComboBox<User> trainerCombo = new ComboBox<>();
        ComboBox<Client> clientCombo = new ComboBox<>();
        TextField startF = new TextField();
        startF.setPromptText("yyyy-MM-dd HH:mm");
        TextField endF = new TextField();
        endF.setPromptText("yyyy-MM-dd HH:mm");
        TextField notesF = new TextField();
        notesF.setPromptText("Заметки");
        CheckBox attendedCb = new CheckBox("Клиент присутствовал (отметка посещения)");

        Runnable loadCombos = () -> {
            try {
                if (u.getRole() == Role.ADMIN) {
                    trainerCombo.setItems(FXCollections.observableArrayList(userDao.findTrainers()));
                } else {
                    trainerCombo.setItems(FXCollections.observableArrayList());
                    trainerCombo.getItems().add(u);
                    trainerCombo.getSelectionModel().selectFirst();
                    trainerCombo.setDisable(true);
                }
                clientCombo.setItems(FXCollections.observableArrayList(clientDao.findAll()));
            } catch (Exception e) {
                alertError(e);
            }
        };
        loadCombos.run();

        Runnable refresh = () -> {
            try {
                if (u.getRole() == Role.ADMIN) {
                    data.setAll(scheduleDao.findAll());
                } else if (u.getRole() == Role.TRAINER) {
                    data.setAll(scheduleDao.findByTrainer(u.getId()));
                } else {
                    Integer cid = u.getClientId();
                    if (cid != null) {
                        data.setAll(scheduleDao.findByClient(cid));
                    } else {
                        data.clear();
                    }
                }
            } catch (Exception e) {
                alertError(e);
            }
        };
        refresh.run();

        boolean canEdit = u.getRole() == Role.ADMIN || u.getRole() == Role.TRAINER;
        Button add = new Button("Добавить");
        Button upd = new Button("Изменить");
        Button del = new Button("Удалить");
        Button markAtt = new Button("Сохранить посещаемость");
        add.setDisable(!canEdit);
        upd.setDisable(!canEdit);
        del.setDisable(!canEdit);
        markAtt.setDisable(u.getRole() == Role.CLIENT);
        decorateCrud(add, upd, del);
        markAtt.getStyleClass().add("btn-action");

        add.setOnAction(e -> {
            try {
                User tr = u.getRole() == Role.ADMIN ? trainerCombo.getSelectionModel().getSelectedItem() : u;
                Client cl = clientCombo.getSelectionModel().getSelectedItem();
                if (tr == null || cl == null) {
                    alert("Выберите тренера и клиента.");
                    return;
                }
                LocalDateTime st = parseDt(startF.getText());
                LocalDateTime en = parseDt(endF.getText());
                scheduleDao.insert(tr.getId(), cl.getId(), st, en, notesF.getText());
                refresh.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });
        upd.setOnAction(e -> {
            TrainingSchedule sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            try {
                User tr = u.getRole() == Role.ADMIN ? trainerCombo.getSelectionModel().getSelectedItem() : u;
                Client cl = clientCombo.getSelectionModel().getSelectedItem();
                if (tr == null || cl == null) {
                    alert("Выберите тренера и клиента.");
                    return;
                }
                scheduleDao.update(sel.getId(), tr.getId(), cl.getId(), parseDt(startF.getText()), parseDt(endF.getText()), notesF.getText());
                refresh.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });
        del.setOnAction(e -> {
            TrainingSchedule sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            if (!confirm("Удалить запись расписания?")) return;
            try {
                scheduleDao.delete(sel.getId());
                refresh.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });
        markAtt.setOnAction(e -> {
            TrainingSchedule sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                alert("Выберите занятие в таблице.");
                return;
            }
            try {
                attendanceDao.replaceForScheduleClient(sel.getId(), sel.getClientId(), attendedCb.isSelected());
                alert("Посещаемость сохранена.");
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((o, a, sel) -> {
            if (sel != null) {
                startF.setText(sel.getStartTime().format(DT));
                endF.setText(sel.getEndTime().format(DT));
                notesF.setText(sel.getNotes() == null ? "" : sel.getNotes());
                for (User tr : trainerCombo.getItems()) {
                    if (tr.getId() == sel.getTrainerUserId()) {
                        trainerCombo.getSelectionModel().select(tr);
                        break;
                    }
                }
                for (Client cl : clientCombo.getItems()) {
                    if (cl.getId() == sel.getClientId()) {
                        clientCombo.getSelectionModel().select(cl);
                        break;
                    }
                }
            }
        });

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(8));
        int r = 0;
        if (u.getRole() == Role.ADMIN) {
            form.addRow(r++, formLabel("Тренер"), trainerCombo);
        }
        form.addRow(r++, formLabel("Клиент"), clientCombo);
        form.addRow(r++, formLabel("Начало"), startF);
        form.addRow(r++, formLabel("Конец"), endF);
        form.addRow(r++, formLabel("Заметки"), notesF);
        form.addRow(r++, attendedCb);
        form.addRow(r, new HBox(8, add, upd, del, markAtt));

        SplitPane sp = new SplitPane();
        sp.getStyleClass().add("main-split");
        sp.setDividerPositions(0.6);
        VBox left = new VBox(8, sectionLabel("Индивидуальные тренировки"), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        sp.getItems().addAll(left, new ScrollPane(form));
        tab.setContent(sp);
        return tab;
    }

    private Tab buildPlansTab() {
        Tab tab = new Tab("Планы и прогресс");
        User u = Session.getUser();

        TableView<TrainingPlan> planTable = new TableView<>();
        ObservableList<TrainingPlan> plans = FXCollections.observableArrayList();
        TableColumn<TrainingPlan, String> p1 = new TableColumn<>("План");
        p1.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<TrainingPlan, String> p2 = new TableColumn<>("Клиент");
        p2.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        TableColumn<TrainingPlan, String> p3 = new TableColumn<>("Тренер");
        p3.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
        planTable.getColumns().addAll(p1, p2, p3);
        planTable.setItems(plans);

        TableView<PlanExercise> exTable = new TableView<>();
        ObservableList<PlanExercise> exRows = FXCollections.observableArrayList();
        TableColumn<PlanExercise, String> e1 = new TableColumn<>("Упражнение");
        e1.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        TableColumn<PlanExercise, Integer> e2 = new TableColumn<>("Подходы");
        e2.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSets()).asObject());
        TableColumn<PlanExercise, Integer> e3 = new TableColumn<>("Повторы");
        e3.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getReps()).asObject());
        exTable.getColumns().addAll(e1, e2, e3);
        exTable.setItems(exRows);

        TableView<ProgressEntry> progTable = new TableView<>();
        ObservableList<ProgressEntry> progRows = FXCollections.observableArrayList();
        TableColumn<ProgressEntry, String> g1 = new TableColumn<>("Дата");
        g1.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLogDate().toString()));
        TableColumn<ProgressEntry, String> g2 = new TableColumn<>("Упр.");
        g2.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        TableColumn<ProgressEntry, Integer> g3 = new TableColumn<>("Подх.");
        g3.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCompletedSets()).asObject());
        TableColumn<ProgressEntry, Integer> g4 = new TableColumn<>("Повт.");
        g4.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCompletedReps()).asObject());
        TableColumn<ProgressEntry, String> g5 = new TableColumn<>("Заметки");
        g5.setCellValueFactory(new PropertyValueFactory<>("notes"));
        progTable.getColumns().addAll(g1, g2, g3, g4, g5);
        progTable.setItems(progRows);
        styleDataTable(planTable);
        styleDataTable(exTable);
        styleDataTable(progTable);

        TextField planNameF = new TextField();
        planNameF.setPromptText("Название плана");
        ComboBox<Client> clientCombo = new ComboBox<>();
        ComboBox<User> trainerCombo = new ComboBox<>();
        ComboBox<Exercise> exerciseCombo = new ComboBox<>();
        Spinner<Integer> setsS = new Spinner<>(1, 50, 3);
        Spinner<Integer> repsS = new Spinner<>(1, 100, 10);
        DatePicker progDate = new DatePicker(LocalDate.now());
        Spinner<Integer> progSets = new Spinner<>(0, 50, 3);
        Spinner<Integer> progReps = new Spinner<>(0, 100, 10);
        TextField progNotes = new TextField();

        boolean adminOrTrainer = u.getRole() == Role.ADMIN || u.getRole() == Role.TRAINER;

        Runnable loadPlanStuff = () -> {
            try {
                clientCombo.setItems(FXCollections.observableArrayList(clientDao.findAll()));
                trainerCombo.setItems(FXCollections.observableArrayList(userDao.findTrainers()));
                exerciseCombo.setItems(FXCollections.observableArrayList(exerciseDao.findAll()));
                if (u.getRole() == Role.TRAINER) {
                    trainerCombo.getSelectionModel().select(
                            trainerCombo.getItems().stream().filter(x -> x.getId() == u.getId()).findFirst().orElse(null));
                    trainerCombo.setDisable(true);
                }
            } catch (Exception e) {
                alertError(e);
            }
        };
        loadPlanStuff.run();

        Runnable refreshPlans = () -> {
            try {
                if (u.getRole() == Role.ADMIN) {
                    plans.setAll(planDao.findAll());
                } else if (u.getRole() == Role.TRAINER) {
                    plans.setAll(planDao.findByTrainer(u.getId()));
                } else {
                    Integer cid = u.getClientId();
                    if (cid != null) {
                        plans.setAll(planDao.findByClient(cid));
                    } else {
                        plans.clear();
                    }
                }
            } catch (Exception e) {
                alertError(e);
            }
        };
        refreshPlans.run();

        Runnable refreshExAndProg = () -> {
            TrainingPlan p = planTable.getSelectionModel().getSelectedItem();
            exRows.clear();
            progRows.clear();
            if (p == null) return;
            try {
                exRows.setAll(planDao.findExercisesForPlan(p.getId()));
                progRows.setAll(progressDao.findByPlan(p.getId()));
            } catch (Exception e) {
                alertError(e);
            }
        };

        planTable.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> {
            if (p != null) {
                planNameF.setText(p.getName());
            }
            refreshExAndProg.run();
        });

        Button createPlan = new Button("Создать план");
        createPlan.setDisable(!adminOrTrainer);
        createPlan.setOnAction(e -> {
            try {
                Client cl = clientCombo.getSelectionModel().getSelectedItem();
                User tr = u.getRole() == Role.ADMIN ? trainerCombo.getSelectionModel().getSelectedItem() : u;
                String nm = planNameF.getText().trim();
                if (cl == null || tr == null || nm.isEmpty()) {
                    alert("Клиент, тренер и название обязательны.");
                    return;
                }
                planDao.insert(cl.getId(), tr.getId(), nm);
                refreshPlans.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        Button renamePlan = new Button("Переименовать план");
        renamePlan.setDisable(!adminOrTrainer);
        renamePlan.setOnAction(e -> {
            TrainingPlan p = planTable.getSelectionModel().getSelectedItem();
            if (p == null) return;
            try {
                planDao.updateName(p.getId(), planNameF.getText().trim());
                refreshPlans.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        Button deletePlan = new Button("Удалить план");
        deletePlan.setDisable(!adminOrTrainer);
        deletePlan.setOnAction(e -> {
            TrainingPlan p = planTable.getSelectionModel().getSelectedItem();
            if (p == null) return;
            if (!confirm("Удалить план и все упражнения/прогресс?")) return;
            try {
                planDao.delete(p.getId());
                refreshPlans.run();
                exRows.clear();
                progRows.clear();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        Button addEx = new Button("Добавить упражнение в план");
        addEx.setDisable(!adminOrTrainer);
        addEx.setOnAction(e -> {
            TrainingPlan p = planTable.getSelectionModel().getSelectedItem();
            Exercise ex = exerciseCombo.getSelectionModel().getSelectedItem();
            if (p == null || ex == null) {
                alert("Выберите план и упражнение.");
                return;
            }
            try {
                int order = exRows.size();
                planDao.addExerciseToPlan(p.getId(), ex.getId(), setsS.getValue(), repsS.getValue(), order);
                refreshExAndProg.run();
            } catch (Exception ex2) {
                alertError(ex2);
            }
        });

        Button updEx = new Button("Изм. подходы/повторы");
        updEx.setDisable(!adminOrTrainer);
        updEx.setOnAction(e -> {
            PlanExercise pe = exTable.getSelectionModel().getSelectedItem();
            if (pe == null) return;
            try {
                planDao.updatePlanExercise(pe.getId(), setsS.getValue(), repsS.getValue());
                refreshExAndProg.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        Button delEx = new Button("Убрать из плана");
        delEx.setDisable(!adminOrTrainer);
        delEx.setOnAction(e -> {
            PlanExercise pe = exTable.getSelectionModel().getSelectedItem();
            if (pe == null) return;
            try {
                planDao.deletePlanExercise(pe.getId());
                refreshExAndProg.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        Button addProg = new Button("Записать прогресс");
        addProg.setDisable(!adminOrTrainer && u.getRole() != Role.CLIENT);
        addProg.setOnAction(e -> {
            PlanExercise pe = exTable.getSelectionModel().getSelectedItem();
            if (pe == null) {
                alert("Выберите строку упражнения в плане.");
                return;
            }
            if (u.getRole() == Role.CLIENT) {
                TrainingPlan p = planTable.getSelectionModel().getSelectedItem();
                if (p == null || u.getClientId() == null || p.getClientId() != u.getClientId()) {
                    alert("Можно вносить прогресс только по своему плану.");
                    return;
                }
            }
            try {
                LocalDate d = progDate.getValue() == null ? LocalDate.now() : progDate.getValue();
                progressDao.insert(pe.getId(), d, progSets.getValue(), progReps.getValue(), progNotes.getText());
                refreshExAndProg.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        Button updProg = new Button("Изменить запись прогресса");
        updProg.setDisable(!adminOrTrainer);
        updProg.setOnAction(e -> {
            ProgressEntry pr = progTable.getSelectionModel().getSelectedItem();
            if (pr == null) return;
            try {
                LocalDate d = progDate.getValue() == null ? LocalDate.now() : progDate.getValue();
                progressDao.update(pr.getId(), d, progSets.getValue(), progReps.getValue(), progNotes.getText());
                refreshExAndProg.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        Button delProg = new Button("Удалить запись");
        delProg.setDisable(!adminOrTrainer);
        delProg.setOnAction(e -> {
            ProgressEntry pr = progTable.getSelectionModel().getSelectedItem();
            if (pr == null) return;
            try {
                progressDao.delete(pr.getId());
                refreshExAndProg.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        exTable.getSelectionModel().selectedItemProperty().addListener((o, a, pe) -> {
            if (pe != null) {
                setsS.getValueFactory().setValue(pe.getSets());
                repsS.getValueFactory().setValue(pe.getReps());
            }
        });
        progTable.getSelectionModel().selectedItemProperty().addListener((o, a, pr) -> {
            if (pr != null) {
                progDate.setValue(pr.getLogDate());
                progSets.getValueFactory().setValue(pr.getCompletedSets());
                progReps.getValueFactory().setValue(pr.getCompletedReps());
                progNotes.setText(pr.getNotes() == null ? "" : pr.getNotes());
            }
        });

        createPlan.getStyleClass().add("btn-action");
        renamePlan.getStyleClass().add("btn-secondary");
        deletePlan.getStyleClass().add("btn-danger");
        addEx.getStyleClass().add("btn-action");
        updEx.getStyleClass().add("btn-secondary");
        delEx.getStyleClass().add("btn-danger");
        addProg.getStyleClass().add("btn-action");
        updProg.getStyleClass().add("btn-secondary");
        delProg.getStyleClass().add("btn-danger");

        GridPane planForm = new GridPane();
        planForm.setHgap(8);
        planForm.setVgap(8);
        planForm.setPadding(new Insets(8));
        int r = 0;
        planForm.addRow(r++, formLabel("Название"), planNameF);
        if (adminOrTrainer) {
            planForm.addRow(r++, formLabel("Клиент"), clientCombo);
            if (u.getRole() == Role.ADMIN) {
                planForm.addRow(r++, formLabel("Тренер"), trainerCombo);
            }
        }
        planForm.addRow(r++, new HBox(8, createPlan, renamePlan, deletePlan));

        GridPane exForm = new GridPane();
        exForm.setHgap(8);
        exForm.setVgap(8);
        exForm.addRow(0, formLabel("Упражнение"), exerciseCombo);
        exForm.addRow(1, formLabel("Подходы"), setsS);
        exForm.addRow(2, formLabel("Повторы"), repsS);
        exForm.addRow(3, new HBox(8, addEx, updEx, delEx));

        GridPane progForm = new GridPane();
        progForm.setHgap(8);
        progForm.setVgap(8);
        progForm.addRow(0, formLabel("Дата"), progDate);
        progForm.addRow(1, formLabel("Сделано подх."), progSets);
        progForm.addRow(2, formLabel("Сделано повт."), progReps);
        progForm.addRow(3, formLabel("Заметки"), progNotes);
        progForm.addRow(4, new HBox(8, addProg, updProg, delProg));

        TitledPane tp1 = new TitledPane("План", planForm);
        TitledPane tp2 = new TitledPane("Упражнения в плане", exForm);
        TitledPane tp3 = new TitledPane("Прогресс", progForm);
        tp1.setExpanded(true);
        tp2.setExpanded(true);
        tp3.setExpanded(true);
        Accordion acc = new Accordion(tp1, tp2, tp3);

        SplitPane mid = new SplitPane();
        mid.getStyleClass().add("main-split");
        mid.setDividerPositions(0.5);
        VBox exBox = new VBox(8, sectionLabel("Состав плана"), exTable);
        VBox.setVgrow(exTable, Priority.ALWAYS);
        VBox progBox = new VBox(8, sectionLabel("Журнал прогресса"), progTable);
        VBox.setVgrow(progTable, Priority.ALWAYS);
        mid.getItems().addAll(exBox, progBox);

        SplitPane main = new SplitPane();
        main.getStyleClass().add("main-split");
        main.setDividerPositions(0.28, 0.72);
        VBox left = new VBox(8, sectionLabel("Планы тренировок"), planTable);
        VBox.setVgrow(planTable, Priority.ALWAYS);
        VBox centerCol = new VBox(mid);
        VBox.setVgrow(mid, Priority.ALWAYS);
        main.getItems().addAll(left, centerCol, new ScrollPane(acc));

        tab.setContent(main);
        return tab;
    }

    private Tab buildExercisesTab() {
        Tab tab = new Tab("Справочник упражнений");
        TableView<Exercise> table = new TableView<>();
        ObservableList<Exercise> data = FXCollections.observableArrayList();
        TableColumn<Exercise, String> c1 = new TableColumn<>("Название");
        c1.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Exercise, String> c2 = new TableColumn<>("Описание");
        c2.setCellValueFactory(new PropertyValueFactory<>("description"));
        table.getColumns().addAll(c1, c2);
        table.setItems(data);
        styleDataTable(table);
        TextField nameF = new TextField();
        TextField descF = new TextField();
        Runnable ref = () -> {
            try {
                data.setAll(exerciseDao.findAll());
            } catch (Exception e) {
                alertError(e);
            }
        };
        ref.run();
        Button add = new Button("Добавить");
        add.getStyleClass().add("btn-action");
        add.setOnAction(e -> {
            try {
                exerciseDao.insert(nameF.getText().trim(), descF.getText().trim());
                ref.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });
        VBox form = new VBox(8, sectionLabel("Новое упражнение"), nameF, descF, add);
        form.setPadding(new Insets(8));
        SplitPane sp = new SplitPane(table, form);
        sp.getStyleClass().add("main-split");
        sp.setDividerPositions(0.65);
        tab.setContent(sp);
        return tab;
    }

    private Tab buildExercisesTabReadOnly() {
        Tab tab = new Tab("Справочник упражнений");
        TableView<Exercise> table = new TableView<>();
        ObservableList<Exercise> data = FXCollections.observableArrayList();
        TableColumn<Exercise, String> c1 = new TableColumn<>("Название");
        c1.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Exercise, String> c2 = new TableColumn<>("Описание");
        c2.setCellValueFactory(new PropertyValueFactory<>("description"));
        table.getColumns().addAll(c1, c2);
        table.setItems(data);
        styleDataTable(table);
        try {
            data.setAll(exerciseDao.findAll());
        } catch (Exception e) {
            alertError(e);
        }
        VBox wrap = new VBox(8, sectionLabel("Каталог упражнений для планов"), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        wrap.setPadding(new Insets(8));
        tab.setContent(wrap);
        return tab;
    }

    private Tab buildReportsTab() {
        Tab tab = new Tab("Отчёты");
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.getStyleClass().add("report-area");
        Button refresh = new Button("Обновить отчёт посещаемости");
        refresh.getStyleClass().add("btn-action");
        refresh.setOnAction(e -> {
            try {
                area.setText(reportDao.buildAttendanceReportText());
            } catch (Exception ex) {
                alertError(ex);
            }
        });
        refresh.fire();
        VBox vb = new VBox(8, sectionLabel("Посещаемость по клиентам"), refresh, area);
        VBox.setVgrow(area, Priority.ALWAYS);
        vb.setPadding(new Insets(8));
        tab.setContent(vb);
        return tab;
    }

    private Tab buildUsersTab() {
        Tab tab = new Tab("Пользователи");
        TableView<User> table = new TableView<>();
        ObservableList<User> data = FXCollections.observableArrayList();
        TableColumn<User, String> u1 = new TableColumn<>("Логин");
        u1.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<User, String> u2 = new TableColumn<>("ФИО");
        u2.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        TableColumn<User, String> u3 = new TableColumn<>("Роль");
        u3.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(roleRu(c.getValue().getRole())));
        table.getColumns().addAll(u1, u2, u3);
        table.setItems(data);
        styleDataTable(table);

        TextField loginF = new TextField();
        PasswordField passF = new PasswordField();
        TextField fullF = new TextField();
        ComboBox<Role> roleC = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        roleC.getSelectionModel().select(Role.CLIENT);
        ComboBox<Client> clientLink = new ComboBox<>();
        Label clientLbl = formLabel("Привязка клиента (роль «Клиент»)");

        Runnable refClients = () -> {
            try {
                clientLink.setItems(FXCollections.observableArrayList(clientDao.findAll()));
            } catch (Exception e) {
                alertError(e);
            }
        };
        refClients.run();

        Runnable ref = () -> {
            try {
                data.setAll(userDao.findAll());
                refClients.run();
            } catch (Exception e) {
                alertError(e);
            }
        };
        ref.run();

        Button add = new Button("Добавить");
        add.setOnAction(e -> {
            try {
                Role r = roleC.getValue();
                Integer cid = null;
                if (r == Role.CLIENT) {
                    Client cl = clientLink.getSelectionModel().getSelectedItem();
                    if (cl == null) {
                        alert("Для клиента выберите карточку клиента.");
                        return;
                    }
                    cid = cl.getId();
                }
                userDao.insert(loginF.getText().trim(), passF.getText(), fullF.getText().trim(), r, cid);
                if (r == Role.CLIENT && cid != null) {
                    Optional<User> nu = userDao.findByUsername(loginF.getText().trim());
                    if (nu.isPresent()) {
                        clientDao.setUserId(cid, nu.get().getId());
                    }
                }
                ref.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        Button upd = new Button("Изменить");
        upd.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            try {
                Role r = roleC.getValue();
                Integer cid = sel.getClientId();
                if (r == Role.CLIENT) {
                    Client cl = clientLink.getSelectionModel().getSelectedItem();
                    cid = cl != null ? cl.getId() : cid;
                } else {
                    cid = null;
                }
                for (Client c : clientDao.findAll()) {
                    if (c.getUserId() != null && c.getUserId() == sel.getId()) {
                        clientDao.setUserId(c.getId(), null);
                    }
                }
                userDao.update(sel.getId(), loginF.getText().trim(), passF.getText(), fullF.getText().trim(), r, cid);
                if (r == Role.CLIENT && cid != null) {
                    clientDao.setUserId(cid, sel.getId());
                }
                ref.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        Button del = new Button("Удалить");
        decorateCrud(add, upd, del);
        del.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            if (sel.getId() == Session.getUser().getId()) {
                alert("Нельзя удалить себя.");
                return;
            }
            if (!confirm("Удалить пользователя?")) return;
            try {
                clientDao.clearUserIdForUser(sel.getId());
                userDao.delete(sel.getId());
                ref.run();
            } catch (Exception ex) {
                alertError(ex);
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((o, a, sel) -> {
            if (sel != null) {
                loginF.setText(sel.getUsername());
                passF.setText(sel.getPassword());
                fullF.setText(sel.getFullName());
                roleC.setValue(sel.getRole());
                if (sel.getClientId() != null) {
                    for (Client c : clientLink.getItems()) {
                        if (c.getId() == sel.getClientId()) {
                            clientLink.getSelectionModel().select(c);
                            break;
                        }
                    }
                }
            }
        });

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(8));
        int r = 0;
        form.addRow(r++, formLabel("Логин"), loginF);
        form.addRow(r++, formLabel("Пароль"), passF);
        form.addRow(r++, formLabel("ФИО"), fullF);
        form.addRow(r++, formLabel("Роль"), roleC);
        form.addRow(r++, clientLbl, clientLink);
        form.addRow(r, new HBox(8, add, upd, del));

        SplitPane sp = new SplitPane();
        sp.getStyleClass().add("main-split");
        sp.setDividerPositions(0.55);
        VBox leftUsers = new VBox(8, sectionLabel("Учётные записи"), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        sp.getItems().addAll(leftUsers, new ScrollPane(form));
        tab.setContent(sp);
        return tab;
    }

    private static LocalDateTime parseDt(String s) {
        try {
            return LocalDateTime.parse(s.trim(), DT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Дата/время: формат yyyy-MM-dd HH:mm", e);
        }
    }

    private static void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private static void alertError(Throwable e) {
        Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage() == null ? e.toString() : e.getMessage());
        a.setHeaderText("Ошибка");
        a.showAndWait();
        e.printStackTrace();
    }

    private static boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
