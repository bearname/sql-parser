package com.sqlparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                throw new Exception("Usage: java -jar *jar <sql file path>");
            }
            final String filePath = args[1];

            final String sqlQuery = readFile(filePath);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static String readFile(String filePath) {
        StringBuilder builder = new StringBuilder();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNext()) {

                final String line = scanner.nextLine();
                builder.append(line);
                System.out.println(line);
            }
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }

        final String sqlQuery = builder.toString();
        return sqlQuery;
    }
}
