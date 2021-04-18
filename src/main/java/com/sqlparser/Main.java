package com.sqlparser;

import com.sqlparser.service.SqlAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                throw new Exception("Usage: java -jar *jar <sql file path>");
            }
            final String filePath = args[0];

            final List<String> sqlQueries = readFile(filePath);
            final int[] line = {0};
            sqlQueries.forEach(sqlQuery -> {
                try {
                    line[0]++;
                    new SqlAnalyzer(sqlQuery).analyze();
                } catch (Exception exception) {
                    System.out.println(ANSI_CYAN + "Invalid query at line " + line[0] + ".g " + exception.getMessage() + ANSI_RESET) ;
                }
            });

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static List<String> readFile(String filePath) {
        List<String> queries = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNext()) {

                final String line = scanner.nextLine();
                queries.add(line);
                System.out.println(line);
            }
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }

        return queries;
    }
}
