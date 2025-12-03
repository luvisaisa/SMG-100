package com.completionist.ui;

import java.util.ArrayList;
import java.util.List;

// buffers screen output to display next to ascii art
public class ScreenBuffer {
    private final List<String> lines = new ArrayList<>();
    private static final int CONTENT_WIDTH = 55;
    private static final int TOP_PADDING = 6; // stars above content

    // add a line
    public void addLine(String line) {
        lines.add(line);
    }

    // add empty line
    public void addEmptyLine() {
        lines.add("");
    }

    // clear buffer
    public void clear() {
        lines.clear();
    }

    // print everything with ascii art on both sides
    // uses buffered output for less flickering
    public void printWithArt() {
        int artLineCount = ConsoleUtils.getAsciiArtLineCount();
        int contentLines = lines.size();
        
        // match ascii art height exactly
        int totalLines = artLineCount;

        // double buffer - build everything first
        StringBuilder output = new StringBuilder();
        
        // move cursor home (less flicker than clearing)
        output.append("\033[H");
        
        // build all lines
        for (int i = 0; i < totalLines; i++) {
            String contentLine;
            
            if (i < TOP_PADDING) {
                // top padding with stars
                contentLine = ConsoleUtils.getStarFillLine(i);
            } else if (i < TOP_PADDING + contentLines) {
                // actual content wrapped with stars
                contentLine = ConsoleUtils.wrapContentWithStars(lines.get(i - TOP_PADDING), CONTENT_WIDTH, i);
            } else {
                // bottom padding with stars
                contentLine = ConsoleUtils.getStarFillLine(i);
            }

            String leftArt = ConsoleUtils.getAsciiArtLeft(i);
            String rightArt = ConsoleUtils.getAsciiArtRight(i);
            
            // clear rest of line to handle variable lengths
            output.append(leftArt).append(contentLine).append(rightArt).append("\033[K\n");
        }
        
        // clear anything below
        output.append("\033[J");
        
        // print all at once
        System.out.print(output);
        System.out.flush();
    }

    // print without ascii art (for special screens like 100% completion)
    // still has the starfield background look
    public void printWithoutArt() {
        int artLineCount = ConsoleUtils.getAsciiArtLineCount();
        int contentLines = lines.size();
        
        int totalLines = artLineCount;

        StringBuilder output = new StringBuilder();
        output.append("\033[H");
        
        for (int i = 0; i < totalLines; i++) {
            String contentLine;
            
            if (i < TOP_PADDING) {
                contentLine = ConsoleUtils.getStarFillLine(i);
            } else if (i < TOP_PADDING + contentLines) {
                contentLine = ConsoleUtils.wrapContentWithStars(lines.get(i - TOP_PADDING), CONTENT_WIDTH, i);
            } else {
                contentLine = ConsoleUtils.getStarFillLine(i);
            }

            // no ascii art - just stars and content
            String leftStars = ConsoleUtils.getStarPatternLeft(i);
            String rightStars = ConsoleUtils.getStarPatternRight(i);
            
            output.append(leftStars).append(contentLine).append(rightStars).append("\033[K\n");
        }
        
        output.append("\033[J");
        System.out.print(output);
        System.out.flush();
    }

    // line count
    public int size() {
        return lines.size();
    }
}
