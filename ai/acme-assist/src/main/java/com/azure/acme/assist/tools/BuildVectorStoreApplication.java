package com.azure.acme.assist.tools;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

/**
 * A CLI application for building and persisting a vector store from files.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.azure.acme.assist.tools", "com.azure.acme.assist.openai"})
public class BuildVectorStoreApplication implements CommandLineRunner {

    @Autowired
    private VectorStoreService vectorStoreService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(BuildVectorStoreApplication.class)
                .web(WebApplicationType.NONE) // .REACTIVE, .SERVLET
                .run(args);
    }

    @Override
    public void run(String... originalArgs) throws Exception {
        DefaultApplicationArguments args = new DefaultApplicationArguments(originalArgs);
        var from = args.getOptionValues("from");
        if (from == null || from.size() != 1) {
            System.err.println("argument --from is required.");
            System.exit(-1);
        }
        var to = args.getOptionValues("to");
        if (to == null || to.size() != 1) {
            System.err.println("argument --to is required.");
            System.exit(-1);
        }
        var pages = args.getOptionValues("pages");

        var formatArgs = args.getOptionValues("format");
        String format = "json";
        if (formatArgs != null && formatArgs.size() == 1) {
            format = formatArgs.get(0);
        }
        var jsonFiles = List.of(from.get(0).split(","));
        vectorStoreService.buildFromJson(jsonFiles, to.get(0), pages, format);
    }
}
