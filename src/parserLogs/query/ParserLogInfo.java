package parserLogs.query;


public class ParserLogInfo {
    private String nameUser;
    private String status;
    private String event;
    private String date;
    private String ip;

    public ParserLogInfo(String ip, String nameUser, String date, String event, String status) {
        this.nameUser = nameUser;
        this.status = status;
        this.event = event;
        this.date = date;
        this.ip = ip;
    }

    public String getNameUser() {
        return nameUser;
    }
    public String getStatus() {
        return status;
    }
    public String getEvent() {
        return event;
    }
    public String getDate() {
        return date;
    }
    public String getIp() {
        return ip;
    }

}
