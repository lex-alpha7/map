package ru.akhitev.rp.map.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javax.inject.Inject;

import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import ru.akhitev.rp.map.drawer.DrawingProperties;
import ru.akhitev.rp.map.drawer.GridOfCoordinatesDrawer;
import ru.akhitev.rp.map.drawer.StarSystemDrawer;
import ru.akhitev.rp.map.entity.StarSystem;
import ru.akhitev.rp.map.hyperspace.EmpireLightSpeedCalculator;
import ru.akhitev.rp.map.hyperspace.VortexSpeedCalculator;
import ru.akhitev.rp.map.repository.StarSystemRepository;
import ru.akhitev.rp.map.router.Router;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@FXMLController
public class MainController implements Initializable {
    private static final Integer MAP_WIDTH = 1000;
    private static final Integer MAP_HEIGHT = 1000;

    @FXML
    private Canvas map;

    @FXML
    private Canvas emblems;

    @FXML
    private TextArea objectInfo;

    @FXML
    private ToggleButton infoButton;

    @FXML
    private ToggleButton routeButton;

    @Inject
    private StarSystemRepository starSystemRepository;

    @Inject
    private GridOfCoordinatesDrawer gridOfCoordinatesDrawer;

    @Inject
    private StarSystemDrawer starSystemDrawer;

    @Inject
    private DrawingProperties drawingProperties;

    @Inject
    private Router router;

    @Inject
    private VortexSpeedCalculator vortexSpeedCalculator;

    @Inject
    private EmpireLightSpeedCalculator empireLightSpeedCalculator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawingProperties.setScale(1);
        initialize();
    }

    @FXML
    public void setScaleX1() {
        drawingProperties.setScale(1);
        initialize();
    }

    @FXML
    public void setScaleX2() {
        drawingProperties.setScale(2);
        initialize();
    }

    @FXML
    public void setScaleX3() {
        drawingProperties.setScale(3);
        initialize();
    }

    private void initialize() {
        clearMap();
        map.setWidth(MAP_WIDTH * drawingProperties.getScale());
        map.setHeight(MAP_HEIGHT * drawingProperties.getScale());
        GraphicsContext gc = map.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, map.getWidth(), map.getHeight());
        gridOfCoordinatesDrawer.draw(map);
        starSystemDrawer.draw(map);
        addOnMousePressedEvent();
    }

    private void clearMap() {
        GraphicsContext gc = map.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, map.getWidth(), map.getHeight());
        gc.clearRect(0, 0, map.getWidth(), map.getHeight());
    }

    private void addOnMousePressedEvent() {
        map.setOnMousePressed((event) -> {
            if (infoButton.isSelected()) {
                processInfoQuering(event);
            }
            if (routeButton.isSelected()) {
                processRouteCalculation(event);
            }
        });
    }

    private void processInfoQuering(MouseEvent event) {
        objectInfo.clear();
        GraphicsContext gc = emblems.getGraphicsContext2D();
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, emblems.getWidth(), emblems.getHeight());
        gc.clearRect(0, 0, emblems.getWidth(), emblems.getHeight());
        List<StarSystem> starSystems = starSystemRepository.findNearCoordinates(drawingProperties.reScaleCoordinate(event.getX()), drawingProperties.reScaleCoordinate(event.getY()));
        if (starSystems != null && starSystems.size() > 0) {
            StarSystem starSystem = starSystems.get(0);
            objectInfo.appendText(starSystem.toString());
            starSystemDrawer.drawEmblems(emblems, starSystem);
        } else {
            objectInfo.appendText("В выбранном участке +-15 единиц системы не найдены.");
        }
        infoButton.setSelected(false);
    }

    private void processRouteCalculation(MouseEvent event) {
        if (router.routeDidNotStarted()) {
            objectInfo.clear();
            router.startRoute(event.getX(), event.getY());
        } else {
            final String format = "Машрут:\n------\nКоординаты точек маршрута:\n%s\nДистанция: %.2f св.лет\nВремя на полет:\nВ вортекс пространстве: %.2fч.,\nВ имперском гипперпространстве: %.2fч.";
            double distance = router.finishRouteAndCalculate(event.getX(), event.getY());
            double timeInVortex = vortexSpeedCalculator.calculate(distance);
            double timeInEmpireLightSpeed = empireLightSpeedCalculator.calculate(distance);
            objectInfo.appendText(String.format(format, router.getPointsInfo(), distance, timeInVortex, timeInEmpireLightSpeed));
            router.reset();
            routeButton.setSelected(false);
        }
    }
}