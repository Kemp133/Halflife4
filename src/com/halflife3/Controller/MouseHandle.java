//package com.halflife3.Controller;
//
//import javafx.event.EventHandler;
//import javafx.scene.input.MouseEvent;
//
//public class MouseHandle implements EventHandler<MouseEvent> {
//    Input input;
//
//    @Override
//    public void handle(MouseEvent mouseEvent) {
//        if(mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
//            input.mouseButtonPressed.replace(mouseEvent.getButton(), true);
//        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
//            input.mouseButtonClicked.replace(mouseEvent.getButton(), true);
//        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
//            input.mouseButtonReleased.replace(mouseEvent.getButton(), true);
//        }
//        //TODO: There are so many different kinds of mouse event that happen, didn't have time to figure the rest out
//    }
//
//    {
//        //TODO: Initialise input value with a global shared Input reference in the main class
//    }
//}
