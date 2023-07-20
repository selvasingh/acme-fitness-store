package com.microsoft.azure.acme.askforhelp.webapi;

import com.microsoft.azure.acme.askforhelp.webapi.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@ComponentScan
public class BuildVectorStoreApplication implements ApplicationRunner {

    private final VectorStoreService vectorStoreService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(BuildVectorStoreApplication.class)
                .web(WebApplicationType.NONE) // .REACTIVE, .SERVLET
                .run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
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
        var jsonFiles = List.of(from.get(0).split(","));
        vectorStoreService.buildFromJson(jsonFiles, to.get(0));
    }
}
