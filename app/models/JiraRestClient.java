package models;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.naming.AuthenticationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Iterator;

public class JiraRestClient {

    private final static String BASE_URL = "https://itrack.innova.com.tr";

    public ProcessingResult createIssue(String username, String password, File inputFile) {

        String auth = new String(Base64.encode(username + ":" + password));

        int totalIssueCount = 0;
        int createdIssueCount = 0;

        try {
            //Get Projects
            String projects = get(auth, BASE_URL+"/rest/api/2/project");
            System.out.println(projects);
            JSONArray projectArray = new JSONArray(projects);
            for (int i = 0; i < projectArray.length(); i++) {
                JSONObject proj = projectArray.getJSONObject(i);
                System.out.println("Key:"+proj.getString("key")+", Name:"+proj.getString("name"));
            }

            //Excel things
            FileInputStream file = new FileInputStream(inputFile);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> lineIterator = sheet.iterator();

            totalIssueCount = sheet.getLastRowNum();

            while (lineIterator.hasNext()) {
                Row line = lineIterator.next();

                if(line.getCell(13) != null){ //// SUB-TASK STARTS
                    if (line.getCell(15) == null || line.getCell(15).getStringCellValue().equals("")) {

                        String project_key = "";
                        String issue_type = "";
                        String summary = "";
                        String versions = "";
                        String description = "";
                        String fix_version = "";
                        String assignee = "";
                        String components = "qqq";
                        String due_date = "";
                        //String securityLevel = "";
                        int securityLevel = 0;
                        String priority = "";
                        String labels = "";
                        String issue_key = "";
                        String createIssueData = "";

                        if(line.getCell(0) != null) project_key = line.getCell(0).getStringCellValue();
                        if(line.getCell(1) != null) issue_type = line.getCell(1).getStringCellValue();
                        if(line.getCell(2) != null) versions = line.getCell(2).getStringCellValue();
                        if(line.getCell(3) != null) fix_version = line.getCell(3).getStringCellValue();
                        if(line.getCell(4) != null) summary = line.getCell(4).getStringCellValue();
                        if(line.getCell(5) != null) description = line.getCell(5).getStringCellValue();
                        if(line.getCell(6) != null) components = line.getCell(6).getStringCellValue();
                        if(line.getCell(7) != null) due_date = line.getCell(7).getStringCellValue();
                        if(line.getCell(8) != null) assignee = line.getCell(8).getStringCellValue();


                        if(line.getCell(9) != null) {
                            if (line.getCell(9).getStringCellValue()=="Administrators")securityLevel = 10205;
                            else if (line.getCell(9).getStringCellValue()=="Developers")securityLevel = 10206;
                            else if (line.getCell(9).getStringCellValue()=="Reporter")securityLevel = 11100;
                            else if (line.getCell(9).getStringCellValue()=="Test Users")securityLevel = 10207;
                            else securityLevel = 10208;
                        }

                        if(line.getCell(10) != null) priority = line.getCell(10).getStringCellValue();
                        if(line.getCell(11) != null) labels = line.getCell(11).getStringCellValue();
                        if(line.getCell(14) != null) issue_key = line.getCell(14).getStringCellValue();

                        ////COMPONENT IS NULL LABEL IS NOT
                        if(line.getCell(6) == null && line.getCell(11) != null) {
                            createIssueData = "{\"fields\":{\"parent\":{\"key\":\"" + issue_key + "\"},\"project\":{\"key\":\"" + project_key + "\"},\"priority\":{\"name\":\"" + priority + "\"},\"security\":{\"id\":\"" + securityLevel + "\"},\"summary\":\"" + summary + "\",\"issuetype\":{\"name\":\"" + issue_type + "\"},\"versions\":[{\"name\":\"" + versions + "\"}],\"labels\":[\"" + labels + "\"],\"description\":\"" + description + "\",\"fixVersions\":[{\"name\":\"" + fix_version + "\"}],\"duedate\":\"" + due_date + "\",\"assignee\":{\"name\":\"" + assignee + "\"}}}";
                        }
                        //////////////////////////////////

                        ////LABEL IS NULL COMPONENT IS NOT
                        if(line.getCell(6) != null && line.getCell(11) == null) {
                            createIssueData = "{\"fields\":{\"parent\":{\"key\":\"" + issue_key + "\"},\"project\":{\"key\":\"" + project_key + "\"},\"priority\":{\"name\":\"" + priority + "\"},\"security\":{\"id\":\"" + securityLevel + "\"},\"summary\":\"" + summary + "\",\"issuetype\":{\"name\":\"" + issue_type + "\"},\"versions\":[{\"name\":\"" + versions + "\"}],\"description\":\"" + description + "\",\"fixVersions\":[{\"name\":\"" + fix_version + "\"}],\"components\":[{\"name\":\"" + components + "\"}],\"duedate\":\"" + due_date + "\",\"assignee\":{\"name\":\"" + assignee + "\"}}}";
                        }
                        //////////////////////////////////

                        ////COMPONENT AND LABEL ARE NULL
                        if(line.getCell(6) == null && line.getCell(11) == null) {
                            createIssueData = "{\"fields\":{\"parent\":{\"key\":\"" + issue_key + "\"},\"project\":{\"key\":\"" + project_key + "\"},\"priority\":{\"name\":\"" + priority + "\"},\"security\":{\"id\":\"" + securityLevel + "\"},\"summary\":\"" + summary + "\",\"issuetype\":{\"name\":\"" + issue_type + "\"},\"versions\":[{\"name\":\"" + versions + "\"}],\"description\":\"" + description + "\",\"fixVersions\":[{\"name\":\"" + fix_version + "\"}],\"duedate\":\"" + due_date + "\",\"assignee\":{\"name\":\"" + assignee + "\"}}}";
                        }
                        /////////////////////////////////

                        ////COMPONENT AND LABEL ARE NOT NULL(NORMAL)

                        if(line.getCell(6) != null && line.getCell(11) != null) {
                            createIssueData = "{\"fields\":{\"parent\":{\"key\":\"" + issue_key + "\"},\"project\":{\"key\":\"" + project_key + "\"},\"priority\":{\"name\":\"" + priority + "\"},\"security\":{\"id\":\"" + securityLevel + "\"},\"summary\":\"" + summary + "\",\"issuetype\":{\"name\":\"" + issue_type + "\"},\"versions\":[{\"name\":\"" + versions + "\"}],\"labels\":[\"" + labels + "\"],\"description\":\"" + description + "\",\"fixVersions\":[{\"name\":\"" + fix_version + "\"}],\"components\":[{\"name\":\"" + components + "\"}],\"duedate\":\"" + due_date + "\",\"assignee\":{\"name\":\"" + assignee + "\"}}}";
                        }
                        ///////////////////////////////////




                        String issue = post(auth, BASE_URL + "/rest/api/2/issue", createIssueData);

                        System.out.println("sdasdasdasd                 :" + issue);
                        JSONObject issueObj = new JSONObject(issue);
                        if (issueObj.has("errors")) {
                            throw new Exception("Errors from Jira:" + issueObj.get("errors"));
                        }
                        String jiraId = issueObj.getString("key");

                        Cell cell = null;
                        Date tarih = new Date();
                        line.createCell(15);
                        cell = line.getCell(15);
                        cell.setCellValue(tarih.toString());

                        Cell cell2 = null;
                        line.createCell(12);
                        cell2 = line.getCell(12);
                        cell2.setCellValue(jiraId);

                        createdIssueCount++;
                    }

                }//////SUB-TASK ENDS

                else{/// NORMAL ISSUE STARTS
                if (line.getCell(15) == null || line.getCell(15).getStringCellValue().equals("")) {

                    String project_key = "";
                    String issue_type = "";
                    String summary = "";
                    String versions = "";
                    String description = "";
                    String fix_version = "";
                    String assignee = "";
                    String components = "";
                    String due_date = "";
                    int securityLevel = 0;
                    String priority = "";
                    String labels = "";
                    String createIssueData = "";

                    if(line.getCell(0) != null) project_key = line.getCell(0).getStringCellValue();
                    if(line.getCell(1) != null) issue_type = line.getCell(1).getStringCellValue();
                    if(line.getCell(2) != null) versions = line.getCell(2).getStringCellValue();
                    if(line.getCell(3) != null) fix_version = line.getCell(3).getStringCellValue();
                    if(line.getCell(4) != null) summary = line.getCell(4).getStringCellValue();
                    if(line.getCell(5) != null) description = line.getCell(5).getStringCellValue();
                    if(line.getCell(6) != null) components = line.getCell(6).getStringCellValue();
                    if(line.getCell(7) != null) due_date = line.getCell(7).getStringCellValue();
                    if(line.getCell(8) != null) assignee = line.getCell(8).getStringCellValue();


                    if(line.getCell(9) != null) {
                        if (line.getCell(9).getStringCellValue().equals("Administrators"))securityLevel = 10205; //Administrators
                        else if (line.getCell(9).getStringCellValue().equals("Developers"))securityLevel = 10206; //Developers
                        else if (line.getCell(9).getStringCellValue().equals("Reporter"))securityLevel = 11100; //Reporter
                        else if (line.getCell(9).getStringCellValue().equals("Test Users"))securityLevel = 10207; //Test Users
                        else securityLevel = 10208;
                    }

                    if(line.getCell(10) != null) priority = line.getCell(10).getStringCellValue();
                    if(line.getCell(11) != null) labels = line.getCell(11).getStringCellValue();


                    ////COMPONENT IS NULL LABEL IS NOT
                    if(line.getCell(6) == null && line.getCell(11) != null) {
                        createIssueData = "{\"fields\":{\"project\":{\"key\":\"" + project_key + "\"},\"priority\":{\"name\":\"" + priority + "\"},\"security\":{\"id\":\"" + securityLevel + "\"},\"summary\":\"" + summary + "\",\"issuetype\":{\"name\":\"" + issue_type + "\"},\"versions\":[{\"name\":\"" + versions + "\"}],\"labels\":[\"" + labels + "\"],\"description\":\"" + description + "\",\"fixVersions\":[{\"name\":\"" + fix_version + "\"}],\"duedate\":\"" + due_date + "\",\"assignee\":{\"name\":\"" + assignee + "\"}}}";
                    }
                    //////////////////////////////////

                    ////LABEL IS NULL COMPONENT IS NOT
                    if(line.getCell(6) != null && line.getCell(11) == null) {
                        createIssueData = "{\"fields\":{\"project\":{\"key\":\"" + project_key + "\"},\"priority\":{\"name\":\"" + priority + "\"},\"security\":{\"id\":\"" + securityLevel + "\"},\"summary\":\"" + summary + "\",\"issuetype\":{\"name\":\"" + issue_type + "\"},\"versions\":[{\"name\":\"" + versions + "\"}],\"description\":\"" + description + "\",\"fixVersions\":[{\"name\":\"" + fix_version + "\"}],\"components\":[{\"name\":\"" + components + "\"}],\"duedate\":\"" + due_date + "\",\"assignee\":{\"name\":\"" + assignee + "\"}}}";
                    }
                    //////////////////////////////////

                    ////COMPONENT AND LABEL ARE NULL
                    if(line.getCell(6) == null && line.getCell(11) == null) {
                        createIssueData = "{\"fields\":{\"project\":{\"key\":\"" + project_key + "\"},\"priority\":{\"name\":\"" + priority + "\"},\"security\":{\"id\":\"" + securityLevel + "\"},\"summary\":\"" + summary + "\",\"issuetype\":{\"name\":\"" + issue_type + "\"},\"versions\":[{\"name\":\"" + versions + "\"}],\"description\":\"" + description + "\",\"fixVersions\":[{\"name\":\"" + fix_version + "\"}],\"duedate\":\"" + due_date + "\",\"assignee\":{\"name\":\"" + assignee + "\"}}}";
                    }
                    /////////////////////////////////

                    ////COMPONENT AND LABEL ARE NOT NULL(NORMAL)

                    if(line.getCell(6) != null && line.getCell(11) != null) {
                        createIssueData = "{\"fields\":{\"project\":{\"key\":\"" + project_key + "\"},\"priority\":{\"name\":\"" + priority + "\"},\"security\":{\"id\":\"" + securityLevel + "\"},\"summary\":\"" + summary + "\",\"issuetype\":{\"name\":\"" + issue_type + "\"},\"versions\":[{\"name\":\"" + versions + "\"}],\"labels\":[\"" + labels + "\"],\"description\":\"" + description + "\",\"fixVersions\":[{\"name\":\"" + fix_version + "\"}],\"components\":[{\"name\":\"" + components + "\"}],\"duedate\":\"" + due_date + "\",\"assignee\":{\"name\":\"" + assignee + "\"}}}";
                    }
                    ///////////////////////////////////



                    String issue = post(auth, BASE_URL + "/rest/api/2/issue", createIssueData);

                    JSONObject issueObj = new JSONObject(issue);
                    if (issueObj.has("errors")) {
                        throw new Exception("Errors from Jira:" + issueObj.get("errors"));
                    }
                    String jiraId = issueObj.getString("key");

                    Cell cell = null;
                    Date tarih = new Date();
                    line.createCell(15);
                    cell = line.getCell(15);
                    cell.setCellValue(tarih.toString());

                    Cell cell2 = null;
                    line.createCell(12);
                    cell2 = line.getCell(12);
                    cell2.setCellValue(jiraId);

                    Cell cell3 = null;
                    line.createCell(14);
                    cell3 = line.getCell(14);
                    cell3.setCellValue(jiraId);

                    createdIssueCount++;

                }

             } // NORMAL ISSUE ENDS

            }
            FileOutputStream fos = new FileOutputStream(inputFile);
            workbook.write(fos);
            fos.close();
            return ProcessingResult.success(createdIssueCount, totalIssueCount);

        } catch (AuthenticationException e) {
            e.printStackTrace();
            return ProcessingResult.failed(e.getMessage(), createdIssueCount, totalIssueCount);
        } catch (ClientHandlerException e) {
            e.printStackTrace();
            return ProcessingResult.failed("Invoking the REST Failed", createdIssueCount, totalIssueCount);
        } catch (JSONException e) {
            e.printStackTrace();
            return ProcessingResult.failed("Wrong JSON Output", createdIssueCount, totalIssueCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ProcessingResult.failed("Unexpected error: " + e.getMessage(), createdIssueCount, totalIssueCount);
        }

    }

    private String getValueNullable(Row line, int cellIndex) throws Exception {
        Cell cell = line.getCell(cellIndex);
        if (cell == null){
           return null;
        }
        return cell.getStringCellValue();
    }

    private String getValueNullSafe(Row line, int cellIndex) throws Exception {
        Cell cell = line.getCell(cellIndex);
        if (cell == null){
            throw new Exception("Error processing file: Cell is empty at row " + line.getRowNum() +" cell " +cellIndex);
        }
        return cell.getStringCellValue();
    }


    //HTTP GET
    private static String get(String auth, String url) throws AuthenticationException, ClientHandlerException {
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                                             .accept("application/json").get(ClientResponse.class);
        int statusCode = response.getStatus();
        if (statusCode == 401) {
            throw new AuthenticationException("Invalid Username or Password");
        }
        return response.getEntity(String.class);
    }

    // HTTP POST for create
    private static String post(String auth, String url, String data) throws AuthenticationException, ClientHandlerException {
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").post(ClientResponse.class, data);
        int statusCode = response.getStatus();
        if (statusCode == 401) {
            throw new AuthenticationException("Invalid Username or Password");
        }

        return response.getEntity(String.class);
    }

    // HTTP PUT for update
    private static String put(String auth, String url, String data) throws AuthenticationException, ClientHandlerException {

        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").put(ClientResponse.class, data);

        int statusCode = response.getStatus();
        if(statusCode == 401) {
            throw new AuthenticationException("Invalid Username or Password");
        }

        return response.getEntity(String.class);
    }
}
