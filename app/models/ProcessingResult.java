package models;

/**
 * Created by bakar on 09.03.2016.
 */
public class ProcessingResult {
    private int createdIssueCount = 0;
    private int totalIssueCount = 0;
    private int status = 0;

    public String msg;

    public static ProcessingResult failed(String errorMsg, int createdIssueCount, int totalIssueCount) {
        ProcessingResult pr = new ProcessingResult();
        pr.createdIssueCount = createdIssueCount;
        pr.totalIssueCount = totalIssueCount;

        pr.status = -1;
        pr.msg = errorMsg;
        return pr;
    }

    public boolean hasError(){
        return status < 0;
    }

    public static ProcessingResult success(int createdIssueCount, int totalIssueCount) {
        ProcessingResult pr = new ProcessingResult();
        pr.createdIssueCount = createdIssueCount;
        pr.totalIssueCount = totalIssueCount;

        return pr;
    }

    public int getCreatedIssueCount() {
        return createdIssueCount;
    }

    public int getTotalIssueCount() {
        return totalIssueCount;
    }

    public String getMsg() {
        return msg;
    }
}
