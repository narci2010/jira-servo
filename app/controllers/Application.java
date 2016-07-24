package controllers;

import models.JiraRestClient;
import models.ProcessingResult;
import play.mvc.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Application extends Controller {

    public static void index() {
        render("index.html");
    }

    public static void createIssues(String username, String password, File inputFile) throws IOException {
        System.out.println(username + " " + password + " " + inputFile.getAbsolutePath());

        Path workingDir = Files.createTempDirectory("jira-servo");

        Path dest = new File( workingDir + File.separator + inputFile.getName()).toPath();
        Path workingFile = Files.copy(inputFile.toPath(), dest);
        final File file = workingFile.toFile();

        System.out.println("Copied > " + file.getAbsolutePath());

        ProcessingResult result = processFile(username, password, file);
        /*
        result.createdIssueCount = 100;
        result.fileName = inputFile.getName();
        result.filePath = "c:/path";
        result.totalIssueCount = 11;
        */
       // ProcessingResult result = ProcessingResult.failed("Failed process", 100, 11);


        flash.put("lastModifiedFile", file.getAbsoluteFile());

        String fileName = file.getName();
        render("result.html", result, fileName);
    }

    private static ProcessingResult processFile(String username, String password, File inputFile) throws IOException {

        System.out.println("Proessing " + inputFile.getAbsolutePath());

        String fileName = inputFile.getName();
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls") || fileName.endsWith(".xlsm")) {
            JiraRestClient restClient = new JiraRestClient();
            return restClient.createIssue(username, password, inputFile);
        }
        flash.put("lastModifiedFile", inputFile.getAbsoluteFile());
        return ProcessingResult.failed(inputFile.getName() +" is not an excel file.", 0, 0);
    }


    public static void downloadTemplate() {
        String filename = "template.xlsx";

        response.contentType = "application/x-download";
        response.setHeader("Content-disposition", "attachment; filename=" + filename);
        renderBinary(new File("public/data/templates/template.xlsx"));
    }

    public static void downloadResultFile() {
        String filePath = flash.get("lastModifiedFile");
        if (filePath == null) {
            render("nofile.html");
        } else{
            File resulFile = new File(filePath);
            response.contentType = "application/x-download";
            response.setHeader("Content-disposition", "attachment; filename=" + resulFile.getName());
            renderBinary(resulFile);
        }

    }

}