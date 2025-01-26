package Util;

/*
Pedro Fellipe Cruz Antunes
Code for adjusting of color values of image by using a cubic interpolated spline.

The output will be n PNG files.

User inputs:
    Control RGBA values through spline;
    Drop files;
    Save png file to the same folder as the original file;
*/

import Windows.DropDownWindow;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DropDownWindow dropDownWindow = new DropDownWindow();
        });
    }
}