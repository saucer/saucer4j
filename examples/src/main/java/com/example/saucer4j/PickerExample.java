package com.example.saucer4j;

import java.io.File;

import app.saucer.SaucerApp;
import app.saucer.SaucerFilePicker;

public class PickerExample {

    public static void main(String[] args) {
        SaucerApp.initialize("com.example.saucer4j", true);

        // Picking a save destination
//        File file = SaucerFilePicker.create()
//            .filter("*.png", "*.jpg", "*.jpeg") // Only allow image files
//            .save();
//        System.out.println("Save destination picked: " + file);

        // Picking a single file
        File file = SaucerFilePicker.create()
            .filter("*.png", "*.jpg", "*.jpeg") // Only allow image files
            .pickFile();
        System.out.println("Single file picked: " + file);

        // Picking a folder
//        File folder = SaucerFilePicker.create()
//            .pickFolder();
//        System.out.println("Folder picked: " + folder);

        // Picking a multiple files
//        List<File> file = SaucerFilePicker.create()
//            .filter("*.png", "*.jpg", "*.jpeg") // Only allow image files
//            .pickFiles();
//        System.out.println("Multiple files picked: " + file);

    }

}
