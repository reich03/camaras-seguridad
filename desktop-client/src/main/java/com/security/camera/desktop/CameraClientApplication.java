package com.security.camera.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Aplicación de escritorio JavaFX mejorada para cliente de cámara
 * Soporta captura desde webcam Y selección de archivos
 */
public class CameraClientApplication extends Application {

    private final ApiClient apiClient = new ApiClient("http://localhost:8082");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final WebcamCaptureService webcamService = new WebcamCaptureService();
    
    // Usuario logueado
    private ApiClient.UserDTO loggedUser;
    
    // Controles comunes
    private Label userInfoLabel;
    private ComboBox<CameraItem> cameraComboBox;
    private TextField cameraNameField;
    private TextField ipAddressField;
    private Label statusLabel;
    private Label connectionLabel;
    private TextArea logArea;
    
    // Controles webcam
    private ImageView webcamPreview;
    private Button startWebcamButton;
    private Button stopWebcamButton;
    private Label recordingStatusLabel;
    private ProgressBar recordingProgressBar;
    private Label statusBadge;
    
    // Controles archivo
    private Button selectFileButton;
    private Button startAutoSendButton;
    private Button stopAutoSendButton;
    private Label selectedFileLabel;
    
    private Long currentConnectionId;
    private boolean isAutoRecording = false;
    private boolean isAutoSending = false;
    private File selectedVideoFile;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Mostrar ventana de login primero
        if (!showLoginDialog()) {
            Platform.exit();
            return;
        }

        primaryStage.setTitle("🎥 Security Camera Client - " + loggedUser.getUsername());

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #c3cfe2);");
        
        // Panel superior - Configuración
        VBox topPanel = createConfigPanel();
        root.setTop(topPanel);

        // Panel central - Tabs con las dos opciones
        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);

        // Panel inferior - Log y Status
        VBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 950, 800);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(650);
        primaryStage.setOnCloseRequest(e -> shutdown());
        primaryStage.show();

        loadCamerasForUser();
    }

    private boolean showLoginDialog() {
        Stage loginStage = new Stage();
        loginStage.setTitle("🔐 Security Camera - Login");
        loginStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        loginStage.setResizable(false);

        VBox loginBox = new VBox(20);
        loginBox.setPadding(new Insets(30));
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);");

        // Logo/Título
        Label titleLabel = new Label("🎥 Security Camera System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Desktop Client");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e0e0e0;");

        // Panel blanco para el formulario
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        formBox.setMaxWidth(350);

        Label loginLabel = new Label("Iniciar Sesión");
        loginLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Usuario
        Label userLabel = new Label("👤 Usuario:");
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Ingresa tu usuario");
        usernameField.setStyle("-fx-pref-height: 35px; -fx-font-size: 13px;");

        // Contraseña
        Label passLabel = new Label("🔒 Contraseña:");
        passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Ingresa tu contraseña");
        passwordField.setStyle("-fx-pref-height: 35px; -fx-font-size: 13px;");

        // Mensaje de error
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        errorLabel.setVisible(false);

        // Botones
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginButton = new Button("🔓 Iniciar Sesión");
        loginButton.setPrefWidth(150);
        loginButton.setStyle(
            "-fx-background-color: #27ae60; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20;"
        );

        Button cancelButton = new Button("✖ Cancelar");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle(
            "-fx-background-color: #95a5a6; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20;"
        );

        final boolean[] loginSuccess = {false};

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("❌ Por favor completa todos los campos");
                errorLabel.setVisible(true);
                return;
            }

            try {
                ApiClient.UserDTO user = apiClient.login(username, password);
                if (user != null) {
                    loggedUser = user;
                    loginSuccess[0] = true;
                    loginStage.close();
                } else {
                    errorLabel.setText("❌ Usuario o contraseña incorrectos");
                    errorLabel.setVisible(true);
                    passwordField.clear();
                }
            } catch (Exception ex) {
                errorLabel.setText("❌ Error conectando al servidor");
                errorLabel.setVisible(true);
                ex.printStackTrace();
            }
        });

        cancelButton.setOnAction(e -> loginStage.close());

        // Enter para login
        passwordField.setOnAction(e -> loginButton.fire());

        buttonBox.getChildren().addAll(loginButton, cancelButton);
        formBox.getChildren().addAll(loginLabel, userLabel, usernameField, passLabel, passwordField, errorLabel, buttonBox);
        loginBox.getChildren().addAll(titleLabel, subtitleLabel, formBox);

        Scene loginScene = new Scene(loginBox, 450, 500);
        loginStage.setScene(loginScene);
        loginStage.showAndWait();

        return loginSuccess[0];
    }

    private VBox createConfigPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label titleLabel = new Label("📹 Camera Configuration");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Info del usuario logueado
        HBox userInfoBox = new HBox(15);
        userInfoBox.setPadding(new Insets(10));
        userInfoBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");
        userInfoBox.setAlignment(Pos.CENTER_LEFT);
        
        Label userIconLabel = new Label("👤");
        userIconLabel.setStyle("-fx-font-size: 24px;");
        
        VBox userDetailsBox = new VBox(3);
        Label userNameLabel = new Label("Usuario: " + loggedUser.getUsername());
        userNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        Label userEmailLabel = new Label("Email: " + loggedUser.getEmail());
        userEmailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        userDetailsBox.getChildren().addAll(userNameLabel, userEmailLabel);
        
        Button logoutButton = new Button("🚪 Cerrar Sesión");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");
        logoutButton.setOnAction(e -> logout());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        userInfoBox.getChildren().addAll(userIconLabel, userDetailsBox, spacer, logoutButton);

        // Selección de cámara
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        
        Label cameraLabel = new Label("📷 Camera:");
        cameraLabel.setStyle("-fx-font-weight: bold;");
        cameraComboBox = new ComboBox<>();
        cameraComboBox.setPrefWidth(300);
        cameraComboBox.setPromptText("Select camera...");
        
        Button refreshCamerasBtn = new Button("🔄 Refresh");
        refreshCamerasBtn.setOnAction(e -> loadCamerasForUser());
        
        gridPane.add(cameraLabel, 0, 0);
        gridPane.add(cameraComboBox, 1, 0);
        gridPane.add(refreshCamerasBtn, 2, 0);

        // Registrar nueva cámara
        TitledPane registerPane = new TitledPane();
        registerPane.setText("➕ Register New Camera");
        registerPane.setExpanded(false);
        
        GridPane registerGrid = new GridPane();
        registerGrid.setHgap(10);
        registerGrid.setVgap(10);
        registerGrid.setPadding(new Insets(10));
        
        Label nameLabel = new Label("Camera Name:");
        cameraNameField = new TextField();
        cameraNameField.setPromptText("e.g., Front Door Camera");
        
        Label ipLabel = new Label("IP Address:");
        ipAddressField = new TextField();
        ipAddressField.setPromptText("e.g., 192.168.1.100");
        
        Button registerButton = new Button("✅ Register Camera");
        registerButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        registerButton.setOnAction(e -> registerCamera());
        
        registerGrid.add(nameLabel, 0, 0);
        registerGrid.add(cameraNameField, 1, 0);
        registerGrid.add(ipLabel, 0, 1);
        registerGrid.add(ipAddressField, 1, 1);
        registerGrid.add(registerButton, 1, 2);
        
        registerPane.setContent(registerGrid);

        panel.getChildren().addAll(titleLabel, new Separator(), gridPane, registerPane);
        return panel;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Webcam Capture - CON SCROLL
        Tab webcamTab = new Tab("📹 Webcam Capture");
        ScrollPane webcamScrollPane = new ScrollPane(createWebcamPanel());
        webcamScrollPane.setFitToWidth(true);
        webcamScrollPane.setStyle("-fx-background-color: white;");
        webcamTab.setContent(webcamScrollPane);

        // Tab 2: File Upload - CON SCROLL
        Tab fileTab = new Tab("📁 File Upload");
        ScrollPane fileScrollPane = new ScrollPane(createFilePanel());
        fileScrollPane.setFitToWidth(true);
        fileScrollPane.setStyle("-fx-background-color: white;");
        fileTab.setContent(fileScrollPane);

        tabPane.getTabs().addAll(webcamTab, fileTab);
        return tabPane;
    }

    private VBox createWebcamPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("🎥 CÁMARA EN VIVO");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Preview compacto
        webcamPreview = new ImageView();
        webcamPreview.setFitWidth(400);
        webcamPreview.setFitHeight(300);
        webcamPreview.setPreserveRatio(true);
        
        StackPane previewContainer = new StackPane(webcamPreview);
        previewContainer.setPrefSize(420, 320);
        previewContainer.setMaxSize(420, 320);
        previewContainer.setStyle(
            "-fx-background-color: #1a1a1a; " +
            "-fx-border-color: #34495e; " +
            "-fx-border-width: 4; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        
        Label placeholderLabel = new Label("📷 Tu cámara aparecerá aquí");
        placeholderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        previewContainer.getChildren().add(placeholderLabel);
        
        // Badge de estado
        statusBadge = new Label("● DESCONECTADA");
        statusBadge.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // BOTÓN PRINCIPAL GRANDE - Inicia cámara Y auto-grabación juntos
        HBox webcamControls = new HBox(10);
        webcamControls.setAlignment(Pos.CENTER);
        webcamControls.setPadding(new Insets(10));
        
        startWebcamButton = new Button("▶ INICIAR VIGILANCIA\n(Cámara + Auto-Grabación)");
        startWebcamButton.setPrefSize(300, 60);
        startWebcamButton.setStyle(
            "-fx-background-color: #27ae60; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 8;"
        );
        startWebcamButton.setOnAction(e -> startFullSurveillance());
        
        stopWebcamButton = new Button("⏹ DETENER TODO");
        stopWebcamButton.setPrefSize(200, 60);
        stopWebcamButton.setStyle(
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 8;"
        );
        stopWebcamButton.setOnAction(e -> stopFullSurveillance());
        stopWebcamButton.setDisable(true);
        
        webcamControls.getChildren().addAll(startWebcamButton, stopWebcamButton);

        // Separador
        Separator sep1 = new Separator();
        sep1.setPrefWidth(400);
        
        // ESTADO DE GRABACIÓN - Solo informativo
        Label autoLabel = new Label("📹 ESTADO DE VIGILANCIA");
        autoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        recordingStatusLabel = new Label("⏸ Sistema detenido - Presiona INICIAR VIGILANCIA");
        recordingStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 5;");
        
        recordingProgressBar = new ProgressBar(0);
        recordingProgressBar.setPrefWidth(400);
        recordingProgressBar.setVisible(false);
        recordingProgressBar.setStyle("-fx-accent: #27ae60;");

        panel.getChildren().addAll(
            titleLabel, 
            statusBadge,
            previewContainer, 
            webcamControls, 
            sep1,
            autoLabel,
            recordingStatusLabel,
            recordingProgressBar
        );
        return panel;
    }

    private VBox createFilePanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("📁 Upload Video Files");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        // Área de selección de archivo
        VBox fileSelectionBox = new VBox(10);
        fileSelectionBox.setAlignment(Pos.CENTER);
        fileSelectionBox.setPadding(new Insets(30));
        fileSelectionBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 5; -fx-background-color: #ecf0f1; -fx-background-radius: 5;");
        
        Label dropLabel = new Label("📂 Select a video file to upload");
        dropLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        
        selectFileButton = new Button("📂 Browse Files");
        selectFileButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-font-size: 14px;");
        selectFileButton.setOnAction(e -> selectVideoFile());
        
        selectedFileLabel = new Label("No file selected");
        selectedFileLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
        
        fileSelectionBox.getChildren().addAll(dropLabel, selectFileButton, selectedFileLabel);

        // Botón de envío único
        Button sendOnceButton = new Button("📤 Upload Selected File Once");
        sendOnceButton.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-font-size: 14px;");
        sendOnceButton.setOnAction(e -> sendSelectedFile());

        // Separador
        Separator separator = new Separator();

        // Envío automático
        Label autoSendLabel = new Label("⏰ Automatic Upload (every 60 seconds)");
        autoSendLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label autoSendInfo = new Label("Upload the selected file automatically every 60 seconds");
        autoSendInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        HBox autoSendControls = new HBox(10);
        autoSendControls.setAlignment(Pos.CENTER);
        
        startAutoSendButton = new Button("🚀 Start Auto-Upload");
        startAutoSendButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-font-size: 14px;");
        startAutoSendButton.setOnAction(e -> startAutoSending());
        
        stopAutoSendButton = new Button("⏹ Stop Auto-Upload");
        stopAutoSendButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-font-size: 14px;");
        stopAutoSendButton.setOnAction(e -> stopAutoSending());
        stopAutoSendButton.setDisable(true);
        
        autoSendControls.getChildren().addAll(startAutoSendButton, stopAutoSendButton);

        panel.getChildren().addAll(titleLabel, fileSelectionBox, sendOnceButton, 
                                   separator, autoSendLabel, autoSendInfo, autoSendControls);
        return panel;
    }

    private VBox createBottomPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10, 0, 0, 0));

        // Status bar
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(10));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        statusLabel = new Label("🟢 Status: Ready");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        connectionLabel = new Label("📡 Connection: Not connected");
        connectionLabel.setStyle("-fx-font-size: 13px;");
        
        statusBar.getChildren().addAll(statusLabel, new Separator(), connectionLabel);

        // Log area
        Label logLabel = new Label("📋 Activity Log");
        logLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 11px; -fx-control-inner-background: #2c3e50; -fx-text-fill: #ecf0f1;");

        panel.getChildren().addAll(statusBar, logLabel, logArea);
        return panel;
    }

    // ==================== WEBCAM METHODS ====================

    /**
     * Inicia todo el sistema de vigilancia: cámara + auto-grabación
     */
    private void startFullSurveillance() {
        // Verificar selección de cámara y usuario logueado
        CameraItem selectedCamera = cameraComboBox.getValue();
        
        if (loggedUser == null || selectedCamera == null) {
            showError("Configuración Requerida", "Por favor selecciona una cámara antes de iniciar");
            return;
        }

        try {
            // 1. Iniciar cámara
            webcamService.startCapture(this::updateWebcamPreview);
            
            // 2. Conectar al servidor
            String ipAddress = ipAddressField.getText().trim();
            if (ipAddress.isEmpty()) ipAddress = "127.0.0.1";
            
            var connection = apiClient.connect(loggedUser.getId(), ipAddress);
            currentConnectionId = connection.getId();
            
            // 3. Activar auto-grabación
            isAutoRecording = true;
            
            // 4. Actualizar UI
            startWebcamButton.setDisable(true);
            stopWebcamButton.setDisable(false);
            statusBadge.setText("● VIGILANCIA ACTIVA");
            statusBadge.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            recordingStatusLabel.setText("🔴 GRABANDO - Videos cada 60 segundos (Conexión ID: " + currentConnectionId + ")");
            recordingStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            recordingProgressBar.setVisible(true);
            connectionLabel.setText("📡 Connection: Connected (ID: " + currentConnectionId + ")");
            
            log("✓ Sistema de vigilancia iniciado correctamente");
            log("✓ Cámara: " + selectedCamera.getName());
            log("✓ Usuario: " + loggedUser.getUsername());
            
            // 5. Grabar primer video inmediatamente
            recordAndUploadVideo(selectedCamera.getId());
            
            // 6. Programar grabaciones cada 60 segundos
            scheduler.scheduleAtFixedRate(() -> {
                if (isAutoRecording) {
                    recordAndUploadVideo(selectedCamera.getId());
                }
            }, 60, 60, java.util.concurrent.TimeUnit.SECONDS);
            
        } catch (Exception e) {
            log("✗ Error iniciando vigilancia: " + e.getMessage());
            showError("Error de Vigilancia", "No se pudo iniciar el sistema: " + e.getMessage());
            stopFullSurveillance(); // Limpieza en caso de error
        }
    }

    /**
     * Detiene todo el sistema de vigilancia
     */
    private void stopFullSurveillance() {
        // Detener auto-grabación
        isAutoRecording = false;
        
        // Desconectar del servidor
        if (currentConnectionId != null) {
            try {
                apiClient.disconnect(currentConnectionId);
                log("✓ Desconectado del servidor");
            } catch (Exception e) {
                log("⚠ Error al desconectar: " + e.getMessage());
            }
            currentConnectionId = null;
        }
        
        // Detener cámara
        webcamService.stopCapture();
        
        // Actualizar UI
        startWebcamButton.setDisable(false);
        stopWebcamButton.setDisable(true);
        statusBadge.setText("● DESCONECTADA");
        statusBadge.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        recordingStatusLabel.setText("⏸ Sistema detenido - Presiona INICIAR VIGILANCIA");
        recordingStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        recordingProgressBar.setVisible(false);
        webcamPreview.setImage(null);
        connectionLabel.setText("📡 Connection: Not connected");
        
        log("⏹ Sistema de vigilancia detenido");
    }

    // Métodos antiguos - Ya no se usan directamente (solo para compatibilidad)
    private void startWebcam() {
        // Este método ya no se usa - usar startFullSurveillance() en su lugar
        log("⚠ Usar INICIAR VIGILANCIA en lugar de este botón");
    }

    private void stopWebcam() {
        // Este método ya no se usa - usar stopFullSurveillance() en su lugar
        stopFullSurveillance();
    }

    private void updateWebcamPreview(BufferedImage bufferedImage) {
        if (bufferedImage != null) {
            Platform.runLater(() -> {
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                webcamPreview.setImage(image);
            });
        }
    }

    // Método antiguo - Ya no se usa
    private void startAutoRecording() {
        log("⚠ Usar INICIAR VIGILANCIA para activar auto-grabación");
    }

    private void recordAndUploadVideo(Long cameraId) {
        Platform.runLater(() -> {
            recordingStatusLabel.setText("🔴 GRABANDO VIDEO (60 segundos)...");
            recordingProgressBar.setVisible(true);
            recordingProgressBar.setProgress(-1); // Indeterminate
            statusLabel.setText("🔴 Status: Recording...");
        });

        try {
            // Grabar video de 60 segundos
            File videoFile = webcamService.recordVideo(60);
            
            Platform.runLater(() -> {
                recordingStatusLabel.setText("📤 Uploading video...");
                statusLabel.setText("📤 Status: Uploading...");
            });
            
            // Subir video
            var video = apiClient.uploadVideo(cameraId, videoFile);
            
            if (currentConnectionId != null) {
                apiClient.incrementFilesSent(currentConnectionId);
            }
            
            Platform.runLater(() -> {
                recordingStatusLabel.setText("✅ Video uploaded successfully! Waiting 60s...");
                recordingProgressBar.setProgress(1.0);
                statusLabel.setText("🟢 Status: Ready");
            });
            
            log("✓ Video recorded and uploaded! ID: " + video.getId() + " Size: " + (videoFile.length() / 1024) + " KB");
            
            // Limpiar archivo temporal
            videoFile.delete();
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                recordingStatusLabel.setText("❌ Error: " + e.getMessage());
                recordingProgressBar.setVisible(false);
                statusLabel.setText("🟢 Status: Ready");
            });
            log("✗ Error recording/uploading video: " + e.getMessage());
        }
    }

    // Método antiguo - Ya no se usa
    private void stopAutoRecording() {
        stopFullSurveillance();
    }

    // ==================== FILE METHODS ====================

    private void selectVideoFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Video File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov", "*.mkv")
        );
        
        // Establecer directorio inicial en Descargas
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            fileChooser.setInitialDirectory(downloadsDir);
        }
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedVideoFile = file;
            selectedFileLabel.setText("✓ " + file.getName() + " (" + (file.length() / 1024 / 1024) + " MB)");
            selectedFileLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            log("📁 File selected: " + file.getName());
        }
    }

    private void sendSelectedFile() {
        if (selectedVideoFile == null) {
            showError("No File Selected", "Please select a video file first");
            return;
        }

        CameraItem selectedCamera = cameraComboBox.getValue();
        if (selectedCamera == null) {
            showError("Selection Required", "Please select a camera");
            return;
        }

        uploadVideoFile(selectedCamera.getId(), selectedVideoFile);
    }

    private void startAutoSending() {
        if (selectedVideoFile == null) {
            showError("No File Selected", "Please select a video file first");
            return;
        }

        CameraItem selectedCamera = cameraComboBox.getValue();
        
        if (loggedUser == null || selectedCamera == null) {
            showError("Selection Required", "Please select a camera");
            return;
        }

        try {
            // Conectar
            String ipAddress = ipAddressField.getText().trim();
            if (ipAddress.isEmpty()) ipAddress = "127.0.0.1";
            
            var connection = apiClient.connect(loggedUser.getId(), ipAddress);
            currentConnectionId = connection.getId();
            
            isAutoSending = true;
            startAutoSendButton.setDisable(true);
            stopAutoSendButton.setDisable(false);
            selectFileButton.setDisable(true);
            
            connectionLabel.setText("📡 Connection: Connected (ID: " + currentConnectionId + ")");
            log("✓ Connected to server - Starting auto-send mode");
            
            // Enviar primer archivo inmediatamente
            uploadVideoFile(selectedCamera.getId(), selectedVideoFile);
            
            // Programar envío cada 60 segundos
            scheduler.scheduleAtFixedRate(() -> {
                if (isAutoSending && selectedVideoFile != null) {
                    uploadVideoFile(selectedCamera.getId(), selectedVideoFile);
                }
            }, 60, 60, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            log("✗ Error starting auto-send: " + e.getMessage());
            showError("Connection Failed", e.getMessage());
        }
    }

    private void stopAutoSending() {
        if (currentConnectionId != null) {
            try {
                apiClient.disconnect(currentConnectionId);
                log("✓ Disconnected from server");
            } catch (Exception e) {
                log("✗ Error disconnecting: " + e.getMessage());
            }
        }
        
        isAutoSending = false;
        startAutoSendButton.setDisable(false);
        stopAutoSendButton.setDisable(true);
        selectFileButton.setDisable(false);
        connectionLabel.setText("📡 Connection: Not connected");
        log("⏹ Stopped auto-send mode");
    }

    private void uploadVideoFile(Long cameraId, File videoFile) {
        try {
            statusLabel.setText("📤 Status: Uploading...");
            log("📤 Uploading " + videoFile.getName() + " (" + (videoFile.length() / 1024 / 1024) + " MB)");
            
            var video = apiClient.uploadVideo(cameraId, videoFile);
            
            if (currentConnectionId != null) {
                apiClient.incrementFilesSent(currentConnectionId);
            }
            
            statusLabel.setText("🟢 Status: Ready");
            log("✓ Video uploaded successfully! ID: " + video.getId());
            
        } catch (Exception e) {
            statusLabel.setText("❌ Status: Error");
            log("✗ Error uploading video: " + e.getMessage());
            showError("Upload Failed", e.getMessage());
        }
    }

    // ==================== COMMON METHODS ====================

    private void loadCamerasForUser() {
        if (loggedUser == null) return;

        try {
            var cameras = apiClient.getCamerasByUserId(loggedUser.getId());
            cameraComboBox.getItems().clear();
            for (var camera : cameras) {
                cameraComboBox.getItems().add(new CameraItem(camera.getId(), camera.getCameraName()));
            }
            log("✓ Loaded " + cameras.size() + " cameras for " + loggedUser.getUsername());
        } catch (Exception e) {
            log("✗ Error loading cameras: " + e.getMessage());
        }
    }

    private void registerCamera() {
        if (loggedUser == null) return;
        
        String cameraName = cameraNameField.getText().trim();
        String ipAddress = ipAddressField.getText().trim();

        if (cameraName.isEmpty()) {
            showError("Validation Error", "Please enter camera name");
            return;
        }

        try {
            var camera = apiClient.registerCamera(cameraName, loggedUser.getId(), ipAddress);
            log("✓ Camera registered: " + camera.getCameraName());
            cameraNameField.clear();
            ipAddressField.clear();
            loadCamerasForUser();
            showInfo("Success", "Camera registered successfully!");
        } catch (Exception e) {
            log("✗ Error registering camera: " + e.getMessage());
            showError("Registration Failed", e.getMessage());
        }
    }
    
    private void logout() {
        Platform.runLater(() -> {
            if (isAutoRecording) {
                stopFullSurveillance();
            }
            if (isAutoSending) {
                stopAutoSending();
            }
            loggedUser = null;
            Stage stage = (Stage) cameraComboBox.getScene().getWindow();
            stage.close();
            Platform.exit();
        });
    }

    private void log(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void shutdown() {
        if (isAutoRecording) stopAutoRecording();
        if (isAutoSending) stopAutoSending();
        if (webcamService.isCapturing()) webcamService.stopCapture();
        scheduler.shutdownNow();
        Platform.exit();
    }

    // ==================== INNER CLASSES ====================

    static class CameraItem {
        private final Long id;
        private final String name;

        public CameraItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
    }
}
