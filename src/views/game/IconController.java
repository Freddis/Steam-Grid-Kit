package views.game;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import kit.Config;
import kit.griddb.Game;
import kit.griddb.GridImage;
import kit.griddb.SteamGridDbClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class IconController {

    public Tab tab;
    public CheckBox checkBoxUseSteamId;
    public ImageView imageViewCurrentImage;
    public ImageView imageViewPreviewImage;
    public ListView<Game> listViewGames;
    public ListView<GridImage> listViewImages;
    public Button buttonFindGames;
    public AnchorPane nodePreviewImageParent;
    public AnchorPane nodeCurrentImageParent;
    public Label labelNoApiKey;
    private ObservableList<Game> listViewGameItems;
    private ObservableList<GridImage> listViewImageItems = FXCollections.observableArrayList();
    private int type;
    private ObservableList<Game> games;
    private Consumer<Runnable> finGamesCallback;
    private kit.models.Game game;
    private SteamGridDbClient client;
    private HashMap<String, Image> cache = new HashMap<>();

    public void findGames(MouseEvent mouseEvent) {
        buttonFindGames.setDisable(true);
        listViewGames.setDisable(true);
        listViewImages.setDisable(true);
        finGamesCallback.accept(() -> {
            buttonFindGames.setDisable(false);
            listViewGames.setDisable(false);
            listViewImages.setDisable(false);
        });
    }

    public void initialize(kit.models.Game game, SteamGridDbClient client, Tab tab, String imageName, int type, BooleanProperty useSteamId, ObservableList<Game> games, Consumer<Runnable> findGamesCallback) {
        this.type = type;
        this.tab = tab;
        this.game = game;


        if (client == null) {
            labelNoApiKey.setVisible(true);
            checkBoxUseSteamId.setVisible(false);
            buttonFindGames.setDisable(true);
        }
        this.client = client;
        this.listViewGameItems = games;
        this.finGamesCallback = findGamesCallback;
        tab.setText(imageName);

        imageViewPreviewImage.fitWidthProperty().setValue(nodePreviewImageParent.getMaxWidth());
        imageViewPreviewImage.fitHeightProperty().setValue(nodePreviewImageParent.getMaxHeight());

        imageViewCurrentImage.fitWidthProperty().setValue(nodeCurrentImageParent.getMaxWidth());
        imageViewCurrentImage.fitHeightProperty().setValue(nodeCurrentImageParent.getMaxHeight());

        checkBoxUseSteamId.selectedProperty().bindBidirectional(useSteamId);

        listViewGames.setItems(listViewGameItems);
        listViewGames.setCellFactory(listView -> new ListCell<Game>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    String value = item.getName();
                    if (item.getDate() != null) {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy");
                        String date = format.format(item.getDate());
                        value = value + " (" + date + ")";
                    }
                    String finalValue = value;
                    Platform.runLater(() -> {
                        setText(finalValue);
                    });
                }
            }
        });

        listViewImages.setItems(listViewImageItems);
        listViewImages.setCellFactory(listView -> new ListCell<GridImage>() {
            @Override
            protected void updateItem(GridImage item, boolean empty) {
                super.updateItem(item, empty);
                String value = !empty ? item.getId() + ": " + item.getAuthor() + " (" + item.getStyle() + ")" : null;
                Platform.runLater(() -> {
                    setText(value);
                });
            }
        });

        listViewImages.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayPreview(newValue);
            }
        });

        this.showCurrentImage();

        listViewGameItems.addListener((ListChangeListener<Game>) c -> {
            listViewImageItems.clear();
            if (games.size() > 0) {
                listViewGames.setOpacity(1);
                listViewGames.getSelectionModel().select(0);
            } else {
                listViewGames.setOpacity(0.5);
            }
        });

        listViewGames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!tab.isSelected() || newValue == null) {
                return;
            }
            searchImages(newValue);
        });

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected() && listViewGameItems.size() > 0 && listViewImageItems.size() == 0) {
                searchImages(listViewGames.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void showCurrentImage() {
        File file = this.getGameImageFile(game, type);
        if (file != null) {
            Image image = null;
            try {
                image = new Image(file.toURI().toURL().toString());
                imageViewCurrentImage.setImage(image);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }


    private void displayPreview(GridImage newValue) {
        String url = newValue.getUrl();
        Thread thread = new Thread(() -> {
            cache.computeIfAbsent(url, s -> {
                try {
                    URLConnection conn = new URL(url).openConnection();
                    conn.setRequestProperty("User-Agent", Config.getUserAgent());
                    InputStream stream = conn.getInputStream();
                    Image newImg = new Image(stream);
                    return newImg;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            });
            Image image = cache.get(url);
            imageViewPreviewImage.setImage(image);
        });
        thread.start();

    }

    private File getGameImageFile(kit.models.Game game, int type) {

        File result = null;
        switch (type) {
            case 0:
                result = game.getHeaderImageFile();
                break;
            case 1:
                result = game.getCoverImageFile();
                break;
            case 2:
                result = game.getBackgroundImageFile();
                break;
            case 3:
                result = game.getLogoImageFile();
                break;
        }
        return result;
    }

    void searchImages(Game game) {
        Consumer<List<GridImage>> callback = (list) -> {
            listViewImageItems.clear();
            listViewImageItems.setAll(list);
            if (list.size() > 0) {
                listViewImages.setOpacity(1);
                listViewImages.getSelectionModel().select(0);
            } else {
                listViewImages.setOpacity(0.5);
            }
        };
        switch (type) {
            case 0:
                client.findGridImages(game.getId(), SteamGridDbClient.Dimentions.d460x215, callback);
                break;
            case 1:
                client.findGridImages(game.getId(), SteamGridDbClient.Dimentions.d600x900, callback);
                break;
            case 2:
                client.findHeroImages(game.getId(), SteamGridDbClient.Dimentions.d1920x620, callback);
                break;
            case 3:
                client.findLogoImages(game.getId(), callback);
                break;
        }

    }

    public void uploadFromDrive(MouseEvent mouseEvent) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image");
        File file = fileChooser.showOpenDialog(buttonFindGames.getScene().getWindow());
        if (file == null) {
            return;
        }
        URL url = file.toURI().toURL();
        Image image = new Image(url.toString());
        imageViewPreviewImage.setImage(image);
    }

    public void setIcon(MouseEvent mouseEvent) {
        Image image = imageViewPreviewImage.getImage();
        String path = this.getGameImagePath(game, this.type);
        File outFile = new File(path);

        File file = new File(Config.getSetImagesDirectory());
        if(!file.exists() && !file.mkdir())
        {
            this.showError("Cannot create image dir");
            return;
        }

        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        String format = outFile.getAbsolutePath().toLowerCase().contains(".png") ? "png" : "jpg";
        try {
            ImageIO.write(bImage, format, outFile);
            this.showCurrentImage();
        } catch (IOException e) {
            this.showError("Something went wrong");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("");
        alert.setContentText(message);
        alert.show();
    }

    private String getGameImagePath(kit.models.Game game, int type) {
        String path;
        switch (type) {
            case 1:
                path = game.getCustomCoverImagePath();
                break;
            case 2:
                path = game.getCustomBackgroundImagePath();
                break;
            case 3:
                path = game.getCustomLogoImagePath();
                break;
            default:
                path = game.getCustomHeaderImagePath();
        }
        return path;
    }
}
