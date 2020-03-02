package parserLogs;

import parserLogs.query.*;


import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.util.*;
import java.io.*;

import static parserLogs.Status.*;
import static parserLogs.Event.*;


public class LogParser implements IPQuery, UserQuery, DateQuery, EventQuery, QLQuery {
    private final String dataFormat = "dd.MM.yyyy HH:mm:ss";
    private Path logDir;


    public LogParser(Path logDir) {
        this.logDir = logDir;
    }

    
    private ArrayList<String> getAllLogStrings() {
        File[] files = new File(String.valueOf(logDir)).listFiles();
        ArrayList<String> list = new ArrayList<>();

        for (File file : files) {
            if (file.getName().endsWith(".log")) {

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                    while (reader.ready()) {
                        list.add(reader.readLine());
                    }
                } catch (Exception e) {
                    System.out.println("ошибка 1");
                }
            }
        }
        return list;
    }

    private String[] parseString(String string) {
        String[] resultString = new String[5];

        Pattern patternIp = Pattern.compile("^\\d+[\".\"]\\d+[\".\"]\\d+[\".\"]\\d+");
        Matcher matcherIp = patternIp.matcher(string);

//        Pattern patternName = Pattern.compile("[\"A-z\"][a-z]+[\" \"]?([\"A-z\"][a-z]+[\" \"]?)*");
        Pattern patternName = Pattern.compile("[A-z]+[\" \"]?([A-z]+[\" \"]?)*");
        Matcher matcherName = patternName.matcher(string);

        Pattern patternDate = Pattern.compile("\\d+[\".\"]\\d+[\".\"]\\d+[\" \"]\\d+[\":\"]\\d+[\":\"]\\d+");
        Matcher matcherDate = patternDate.matcher(string);

        Pattern patternEvent = Pattern.compile("LOGIN|DOWNLOAD_PLUGIN|WRITE_MESSAGE|"
                + "SOLVE_TASK[\" \"]\\d+|DONE_TASK[\" \"]\\d+");
        Matcher matcherEvent = patternEvent.matcher(string);

        Pattern patternStatus = Pattern.compile("OK|FAILED|ERROR");
        Matcher matcherStatus = patternStatus.matcher(string);

        if (matcherIp.find() && matcherName.find() && matcherDate.find()
                && matcherEvent.find() && matcherStatus.find()) {
            resultString[0] = matcherIp.group();
            resultString[1] = matcherName.group();
            resultString[2] = matcherDate.group();
            resultString[3] = matcherEvent.group();
            resultString[4] = matcherStatus.group();
        } else {
            System.out.println("неправильный формат строки");
        }
        return resultString;
    }

    private boolean testDate(String stringDate, Date after, Date before) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date dateFromString = null;
        try {
            dateFromString = simpleDateFormat.parse(stringDate);
        } catch (Exception e) {
            System.out.println("неверный формат даты");
        }

        if (before == null && after == null) {
            return true;
        } else if (before == null) {
            return dateFromString.getTime() >= after.getTime();
        } else if (after == null) {
            return dateFromString.getTime() <= before.getTime();
        } else {
            return dateFromString.getTime() >= after.getTime() && dateFromString.getTime() <= before.getTime();
        }
    }

    private boolean testDateBetween(String stringDate, Date after, Date before) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date dateFromString = null;
        try {
            dateFromString = simpleDateFormat.parse(stringDate);
        } catch (Exception e) {
            System.out.println("неверный формат даты");
        }

        if (before == null && after == null) {
            return true;
        } else if (before == null) {
            return dateFromString.getTime() > after.getTime();
        } else if (after == null) {
            return dateFromString.getTime() < before.getTime();
        } else {
            return dateFromString.getTime() > after.getTime() && dateFromString.getTime() < before.getTime();
        }
    }

    private String returnTaskInfo(String event) {
        String[] task = event.split(" ");
        return task[0];
    }

    private boolean isTaskNumberTrueFalse(String event, int i) {
        String[] task = event.split(" ");
        int number = -1;
        if (task.length > 1) {
             number = Integer.parseInt(task[1].trim());
        } else {
            return false;
        }
        return number == i;
    }

    private int returnTaskNumber(String event) {
        String[] task = event.split(" ");
        int number = -1;

        if (task.length > 1) {
            number = Integer.parseInt(task[1].trim());
        }
        return number;
    }

    private Date getDate(String date) {
        if (date.equals("null")){
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern(dataFormat);
        Date dateParser;
        try {
            dateParser = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return dateParser;
    }

    private Date getTheFirstDateInHashSet(Set<Date> set) {
        if (set.size() > 0) {
            System.out.println(set.size());
            Date dateMin = null;
            Date dateMax = null;
            Iterator<Date> it = set.iterator();

            while (it.hasNext()){
                dateMax = it.next();
                if (dateMax != null && dateMin == null) {
                    dateMin = dateMax;
                } else if (dateMin.after(dateMax)) {
                    dateMin = dateMax;
                }
            }
            return dateMin;
        } else {
            return null;
        }
    }

    private Event returnEvent(String event) {
        String stringEvent = event.replaceAll("\\d", "").trim();

        switch (stringEvent) {
            case "DOWNLOAD_PLUGIN" :
                return DOWNLOAD_PLUGIN;
            case "WRITE_MESSAGE" :
                return WRITE_MESSAGE;
            case "SOLVE_TASK" :
                return SOLVE_TASK;
            case "DONE_TASK" :
                return DONE_TASK;
            case "LOGIN" :
                return LOGIN;
            default:
                return null;
        }
    }

    private Status returnStatus(String status) {
        switch (status) {
            case "FAILED" :
                return FAILED;
            case "ERROR" :
                return ERROR;
            case "OK" :
                return OK;
            default:
                return null;
        }
    }

    private Set<Status> getStatus() {
        Set<Status> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);
            switch (stringArray[4]) {
                case "OK" :
                    set.add(OK);
                    break;
                case  "FAILED" :
                    set.add(FAILED);
                    break;
                case "ERROR" :
                    set.add(ERROR);
                    break;
            }
        }
        return set;
    }

    private Set<Date> getAllDate() {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);
            set.add(getDate(stringArray[2]));
        }
        return set;
    }

    private Set<String> getUsersEvents(String event) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (returnTaskInfo(stringArray[3]).equals(event)) {
                set.add(stringArray[1]);
            }
        }
        return set;
    }

    private Set<String> getUsersEvents(String event, Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                if (returnTaskInfo(stringArray[3]).equals(event)) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

    private Set<String> getUsersStatus(String status, Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[4].equals(status)) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

    private Set<Status> getStatusForUser(String user, Date after, Date before) {
        Set<Status> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[1].equals(user)) {
                    set.add(returnStatus(stringArray[4]));
                }
            }
        }
        return set;
    }

    private Set<Status> getStatusForEvent(String event, Date after, Date before) {
        Set<Status> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (returnTaskInfo(stringArray[3]).equals(event)) {
                    set.add(returnStatus(stringArray[4]));
                }
            }
        }
        return set;
    }

    private Set<Date> getDateToIP(String ip, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[0].equals(ip)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return set;
    }

    private Set<Date> getDatesForUser(String user, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[1].equals(user)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return set;
    }

    private Set<Date> getDatesForEvent(String event, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDateBetween(stringArray[2], after, before)) {

                if (returnTaskInfo(stringArray[3]).equals(event)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return set;
    }

    private Set<Event> getAllEvents(Date date, Date after, Date before) {
        Set<Event> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (date == null) {
                    set.add(returnEvent(stringArray[3]));
                } else if (testDate(stringArray[2], date, date)) {
                    set.add(returnEvent(stringArray[3]));
                }
            }
        }
        return set;
    }

    private Set<Date> getDatesForStatus(String status, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[4].equals(status)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return set;
    }

    private Set<Event> getEventsForStatus(String status, Date after, Date before) {
        Set<Event> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[4].equals(status)) {
                    set.add(returnEvent(stringArray[3]));
                }
            }
        }
        return set;
    }

    private Set<Status> getStatusForIPs(String ip, Date after, Date before) {
        Set<Status> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[0].equals(ip)) {
                    set.add(returnStatus(stringArray[4]));
                }
            }
        }
        return set;
    }

    private Set<Status> getStatusForDate(Date date, Date after, Date before) {
        Set<Status> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (date == null) {
                    set.add(returnStatus(stringArray[4]));
                } else if (testDate(stringArray[2], date, date)) {
                    set.add(returnStatus(stringArray[4]));
                }
            }
        }
        return set;
    }

    private Set<String> getUsersForDate(Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                set.add(stringArray[1]);
            }
        }
        return set;
    }

    private Set<String> getUsersForDate(Date date, Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
            if (testDate(stringArray[2], date, date)) {
                set.add(stringArray[1]);
            }
            }
        }
        return set;
    }

    public Set<String> getUniqueIPsForDate(Date unic, Date after, Date before) {
        Set<String> uniqueIpSet = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                if (testDate(stringArray[2], unic, unic)) {
                    uniqueIpSet.add(stringArray[0]);
                }
            }
        }
        return uniqueIpSet;
    }



    @Override
    public int getNumberOfUniqueIPs(Date after, Date before) {
        return getUniqueIPs(after, before).size();
    }

    @Override
    public Set<String> getUniqueIPs(Date after, Date before) {
        Set<String> uniqueIpSet = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                uniqueIpSet.add(stringArray[0]);
            }
        }
        return uniqueIpSet;
    }

    @Override
    public Set<String> getIPsForUser(String user, Date after, Date before) {
        Set<String> uniqueIpSetForUser = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before) && stringArray[1].equals(user)) {
                uniqueIpSetForUser.add(stringArray[0]);
            }
        }
        return uniqueIpSetForUser;
    }

    @Override
    public Set<String> getIPsForEvent(Event event, Date after, Date before) {
        Set<String> uniqueIpSetForUser = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);
            String[] eventStr = stringArray[3].split(" ");

            try {
                Event eventFromString = Event.valueOf(eventStr[0]);

                if (testDateBetween(stringArray[2], after, before) && eventFromString.equals(event)) {
                    uniqueIpSetForUser.add(stringArray[0]);
                }
            } catch (Exception e) {
                System.out.println("нет EventEnum : " + eventStr[0]);
            }
        }
        return uniqueIpSetForUser;
    }

    @Override
    public Set<String> getIPsForStatus(Status status, Date after, Date before) {
        Set<String> uniqueIpSetForUser = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);
            try {
                Status eventFromString = Status.valueOf(stringArray[4]);

                if (testDateBetween(stringArray[2], after, before) && eventFromString == status) {
                    uniqueIpSetForUser.add(stringArray[0]);
                }
            } catch (Exception e) {
                System.out.println("нет StatusEnum : " + stringArray[4]);
            }
        }
        return uniqueIpSetForUser;
    }

    @Override
        // должен возвращать всех пользователей
    public Set<String> getAllUsers() {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);
            set.add(stringArray[1]);
        }
        return set;
    }

        // должен возвращать количество уникальных пользователей
    @Override
    public int getNumberOfUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                set.add(stringArray[1]);
            }
        }
        return set.size();
    }

        //  должен возвращать пользователей, которые делали логин
    @Override
    public Set<String> getLoggedUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[3].equals(LOGIN.toString())) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

        // должен возвращать множество содержащее пользователей,
        // которые решили любую задачу за выбранный период
    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (returnTaskInfo(stringArray[3]).equals(DONE_TASK.toString())) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

        // должен возвращать пользователей, которые решали любую задачу
    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (returnTaskInfo(stringArray[3]).equals(SOLVE_TASK.toString())) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

        // должен возвращать пользователей, которые отправили сообщение
    @Override
    public Set<String> getWroteMessageUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[3].equals(WRITE_MESSAGE.toString())) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

        // должен возвращать пользователей, которые скачали плагин
    @Override
    public Set<String> getDownloadedPluginUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[3].equals(DOWNLOAD_PLUGIN.toString())) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

        // должен возвращать пользователей с определенным IP
        // Несколько пользователей могут использовать один и тот же IP
    @Override
    public Set<String> getUsersForIP(String ip, Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (stringArray[0].equals(ip)) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

        // должен возвращать множество содержащее пользователей,
        // которые решили задачу с номером task за выбранный период
    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before, int task) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);
            if (testDate(stringArray[2], after, before)) {

                if (returnTaskInfo(stringArray[3]).equals(DONE_TASK.toString())
                        && isTaskNumberTrueFalse(stringArray[3], task)) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

        // должен возвращать количество событий от определенного пользователя
    @Override
    public int getNumberOfUserEvents(String user, Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                if (stringArray[1].equals(user)) {
                    set.add(stringArray[3]);
                }
            }
        }
        return set.size();
    }

        //  должен возвращать пользователей, которые решали задачу с номером task
    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before, int task) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (returnTaskInfo(stringArray[3]).equals(SOLVE_TASK.toString())
                        && isTaskNumberTrueFalse(stringArray[3], task)) {
                    set.add(stringArray[1]);
                }
            }
        }
        return set;
    }

        // Метод должен возвращать даты, когда определенный пользователь произвел определенное событие.
    @Override
    public Set<Date> getDatesForUserAndEvent(String user, Event event, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                if (returnTaskInfo(stringArray[3]).equals(event.toString())
                        && stringArray[1].equals(user)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return set;
    }

        // Метод должен возвращать даты, когда пользователь скачал плагин.
    @Override
    public Set<Date> getDatesWhenUserDownloadedPlugin(String user, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (returnTaskInfo(stringArray[3]).equals(DOWNLOAD_PLUGIN.toString())
                        && stringArray[1].equals(user)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return set;
    }

        // Метод должен возвращать дату, когда пользователь впервые попытался решить определенную задачу.
        // Если такой даты в логах нет - null.
    @Override
    public Date getDateWhenUserSolvedTask(String user, int task, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                if (returnTaskInfo(stringArray[3]).equals(SOLVE_TASK.toString())
                        && stringArray[1].equals(user)
                        && isTaskNumberTrueFalse(stringArray[3], task)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return getTheFirstDateInHashSet(set);
    }

        // Метод должен возвращать дату, когда пользователь впервые решил определенную задачу.
        // Если такой даты в логах нет - null.
    @Override
    public Date getDateWhenUserDoneTask(String user, int task, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                if (returnTaskInfo(stringArray[3]).equals(DONE_TASK.toString())
                        && stringArray[1].equals(user)
                        && isTaskNumberTrueFalse(stringArray[3], task)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return getTheFirstDateInHashSet(set);
    }

        // Метод должен возвращать даты, когда пользователь написал сообщение.
    @Override
    public Set<Date> getDatesWhenUserWroteMessage(String user, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                if (returnTaskInfo(stringArray[3]).equals(WRITE_MESSAGE.toString())
                        && stringArray[1].equals(user)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return set;
    }

        // Метод должен возвращать дату, когда пользователь залогинился впервые за указанный период.
        // Если такой даты в логах нет - null.
    @Override
    public Date getDateWhenUserLoggedFirstTime(String user, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                if (returnTaskInfo(stringArray[3]).equals(LOGIN.toString())
                        && stringArray[1].equals(user)) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return getTheFirstDateInHashSet(set);
    }

        // Метод должен возвращать даты, когда любое событие не выполнилось (статус FAILED).
    @Override
    public Set<Date> getDatesWhenSomethingFailed(Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (returnTaskInfo(stringArray[4]).equals(FAILED.toString())) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return set;
    }

        // Метод должен возвращать даты, когда любое событие закончилось ошибкой (статус ERROR).
    @Override
    public Set<Date> getDatesWhenErrorHappened(Date after, Date before) {
        Set<Date> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {

                if (returnTaskInfo(stringArray[4]).equals(ERROR.toString())) {
                    set.add(getDate(stringArray[2]));
                }
            }
        }
        return set;
    }

        //  Метод должен возвращать количество успешных решений определенной задачи.
    @Override
    public int getNumberOfSuccessfulAttemptToSolveTask(int task, Date after, Date before) {
        ArrayList<String> list = getAllLogStrings();
        int count = 0;

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before) && isTaskNumberTrueFalse(stringArray[3], task)
                    && returnTaskInfo(stringArray[3]).equals(DONE_TASK.toString())) {
                count++;
            }
        }
        return count;
    }

        // Метод должен возвращать мапу (номер_задачи : количество_попыток_решить_ее).
    @Override
    public Map<Integer, Integer> getAllSolvedTasksAndTheirNumber(Date after, Date before) {
        ArrayList<String> list = getAllLogStrings();
        Map<Integer, Integer> map = new HashMap<>();

        for (String string : list) {
            String[] stringArray = parseString(string);
            int task = returnTaskNumber(stringArray[3]);

            if (task != -1) {

                if (testDate(stringArray[2], after, before) && isTaskNumberTrueFalse(stringArray[3], task)
                        && returnTaskInfo(stringArray[3]).equals(SOLVE_TASK.toString())) {
                    if (map.containsKey(task)) {
                        int value = map.get(task) + 1;
                        map.put(task, value);
                    } else {
                        map.put(task, 1);
                    }
                }
            }
        }
        return map;
    }

        // Метод должен возвращать мапу (номер_задачи : сколько_раз_ее_решили).
    @Override
    public Map<Integer, Integer> getAllDoneTasksAndTheirNumber(Date after, Date before) {
        ArrayList<String> list = getAllLogStrings();
        Map<Integer, Integer> map = new HashMap<>();

        for (String string : list) {
            String[] stringArray = parseString(string);
            int task = returnTaskNumber(stringArray[3]);

            if (task != -1) {

                if (testDate(stringArray[2], after, before) && isTaskNumberTrueFalse(stringArray[3], task)
                        && returnTaskInfo(stringArray[3]).equals(DONE_TASK.toString())) {
                    if (map.containsKey(task)) {
                        int value = map.get(task) + 1;
                        map.put(task, value);
                    } else {
                        map.put(task, 1);
                    }
                }
            }
        }
        return map;
    }

        //  Метод должен возвращать количество попыток решить определенную задачу.
    @Override
    public int getNumberOfAttemptToSolveTask(int task, Date after, Date before) {
        ArrayList<String> list = getAllLogStrings();
        int count = 0;

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before) && isTaskNumberTrueFalse(stringArray[3], task)
                    && returnTaskInfo(stringArray[3]).equals(SOLVE_TASK.toString())) {
                count++;
            }
        }
        return count;
    }

        //  Метод должен возвращать события, которые инициировал определенный пользователь.
    @Override
    public Set<Event> getEventsForUser(String user, Date after, Date before) {
        Set<Event> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before) && stringArray[1].equals(user)) {
                set.add(returnEvent(stringArray[3]));
            }
        }
        return set;
    }

        // Метод должен возвращать события, которые происходили с указанного IP.
    @Override
    public Set<Event> getEventsForIP(String ip, Date after, Date before) {
        Set<Event> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before) && stringArray[0].equals(ip)) {
                set.add(returnEvent(stringArray[3]));
            }
        }
        return set;
    }

        // Метод должен возвращать события, которые не выполнились.
    @Override
    public Set<Event> getFailedEvents(Date after, Date before) {
        Set<Event> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before) && stringArray[4].equals(FAILED.toString())) {
                set.add(returnEvent(stringArray[3]));
            }
        }
        return set;
    }

        // Метод должен возвращать события, которые завершились ошибкой.
    @Override
    public Set<Event> getErrorEvents(Date after, Date before) {
        Set<Event> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before) && stringArray[4].equals(ERROR.toString())) {
                set.add(returnEvent(stringArray[3]));
            }
        }
        return set;
    }

        // Метод должен возвращать количество событий за указанный период.
    @Override
    public int getNumberOfAllEvents(Date after, Date before) {
        Set<String> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                set.add(stringArray[3].replaceAll("\\d", "").trim());
            }
        }
        return set.size();
    }

        // Метод должен возвращать все события за указанный период.
    @Override
    public Set<Event> getAllEvents(Date after, Date before) {
        Set<Event> set = new HashSet<>();
        ArrayList<String> list = getAllLogStrings();

        for (String string : list) {
            String[] stringArray = parseString(string);

            if (testDate(stringArray[2], after, before)) {
                set.add(returnEvent(stringArray[3]));
            }
        }
        return set;
    }

    @Override
    public Set<Object> execute(String query) {
        if (query == null) return null;

        switch (query) {
            case "get status" :
                return new HashSet<>(getStatus());
            case "get event" :
                return new HashSet<>(getAllEvents(null, null));
            case "get user" :
                return new HashSet<>(getAllUsers());
            case "get date" :
                return new HashSet<>(getAllDate());
            case "get ip" :
                return new HashSet<>(getUniqueIPs(null, null));
            default:
                return new HashSet<>(getExecuteNext(query));
        }
    }

    private Set<Object> getExecuteNext(String string) {
        String[] strings = string.split(" = ");
//        System.out.println(strings[0] + " ===== " + strings[1].replace("\"", ""));
        String result = strings[1].replace("\"", "").trim();
        String[] stringsTwo = result.split(" and date between ");


        if (stringsTwo.length > 1) {
            String whoOrWhat = stringsTwo[0].replace("\"", "");
            String parameters = stringsTwo[1].replace("\"", "");
            return new HashSet<>(getExecuteNext2(strings[0], whoOrWhat, parameters));
        } else if (stringsTwo.length == 1) {

            switch (strings[0]) {
                // Вызов метода execute с параметром "get ip for user = "[any_user]"" должен
                // возвращать множество уникальных IP адресов, с которых работал пользователь с именем [any_user].
                case "get ip for user":
                    return new HashSet<>(getIPsForUser(result, null, null));
                // Вызов метода execute с параметром "get ip for date = "[any_date]"" должен
                // возвращать множество уникальных IP адресов, события с которых произведены
                // в указанное время [any_date].
                case "get ip for date":
                    return new HashSet<>(getUniqueIPs(getDate(result), getDate(result)));
                // Вызов метода execute с параметром "get ip for event = "[any_event]"" должен
                // возвращать множество уникальных IP адресов, у которых событие равно [any_event].
                case "get ip for event":
                    return new HashSet<>(getIPsForEvent(returnEvent(result), null, null));
                // Вызов метода execute с параметром "get ip for status = "[any_status]"" должен
                // возвращать множество уникальных IP адресов, события с которых закончились со статусом [any_status].
                case "get ip for status":
                    return new HashSet<>(getIPsForStatus(returnStatus(result), null, null));
                // Вызов метода execute с параметром "get user for ip = "[any_ip]"" должен
                // возвращать множество уникальных пользователей, которые работали с IP адреса [any_ip].
                case "get user for ip":
                    return new HashSet<>(getUsersForIP(result, null, null));
                // Вызов метода execute с параметром "get user for date = "[any_date]"" должен
                // возвращать множество уникальных пользователей, которые произвели
                // любое действие в указанное время [any_date].
                case "get user for date":
                    return new HashSet<>(getUsersForDate(getDate(result), getDate(result)));
                // Вызов метода execute с параметром "get user for event = "[any_event]"" должен
                // возвращать множество уникальных пользователей, у которых событие равно [any_event].
                case "get user for event":
                    return new HashSet<>(getUsersEvents(result));
                // Вызов метода execute с параметром "get user for status = "[any_status]"" должен
                // возвращать множество уникальных пользователей, у которых статус равен [any_status].
                case "get user for status":
                    return new HashSet<>(getUsersStatus(result, null, null));
                // Вызов метода execute с параметром "get date for ip = "[any_ip]"" должен
                // возвращать множество уникальных дат, за которые с IP адреса [any_ip] произведено любое действие.
                case "get date for ip":
                    return new HashSet<>(getDateToIP(result, null, null));
                // Вызов метода execute с параметром "get date for user = "[any_user]"" должен
                // возвращать множество уникальных дат, за которые пользователь [any_user] произвел любое действие.
                case "get date for user":
                    return new HashSet<>(getDatesForUser(result, null, null));
                // Вызов метода execute с параметром "get date for event = "[any_event]"" должен
                // возвращать множество уникальных дат, за которые произошло событие равно [any_event].
                case "get date for event":
                    return new HashSet<>(getDatesForEvent(result, null, null));
                // Вызов метода execute с параметром "get date for status = "[any_status]"" должен
                // возвращать множество уникальных дат, за которые произошло любое событие со статусом [any_status].
                case "get date for status":
                    return new HashSet<>(getDatesForStatus(result, null, null));
                // Вызов метода execute с параметром "get event for ip = "[any_ip]"" должен возвращать
                // множество уникальных событий, которые произошли с IP адреса [any_ip].
                case "get event for ip":
                    return new HashSet<>(getEventsForIP(result, null, null));
                // Вызов метода execute с параметром "get event for user = "[any_user]"" должен
                // возвращать множество уникальных событий, которые произвел пользователь [any_user].
                case "get event for user":
                    return new HashSet<>(getEventsForUser(result, null, null));
                // Вызов метода execute с параметром "get event for date = "[any_date]"" должен
                // возвращать множество уникальных событий, которые произошли во время [any_date].
                case "get event for date":
                    return new HashSet<>(getAllEvents(getDate(result), getDate(result)));
                // Вызов метода execute с параметром "get event for status = "[any_status]"" должен
                // возвращать множество уникальных событий, которые завершены со статусом [any_status].
                case "get event for status":
                    return new HashSet<>(getEventsForStatus(result, null, null));
                // Вызов метода execute с параметром "get status for ip = "[any_ip]"" должен
                // возвращать множество уникальных статусов, которые произошли с IP адреса [any_ip].
                case "get status for ip":
                    return new HashSet<>(getStatusForIPs(result, null, null));
                // Вызов метода execute с параметром "get status for user = "[any_user]"" должен
                // возвращать множество уникальных статусов, которые произвел пользователь [any_user].
                case "get status for user":
                    return new HashSet<>(getStatusForUser(result, null, null));
                // Вызов метода execute с параметром "get status for date = "[any_date]"" должен
                // возвращать множество уникальных статусов, которые произошли во время [any_date].
                case "get status for date":
                    return new HashSet<>(getStatusForDate(null, getDate(result), getDate(result)));
                // Вызов метода execute с параметром "get status for event = "[any_event]"" должен
                // возвращать множество уникальных статусов, у которых событие равно [any_event].
                case "get status for event":
                    return new HashSet<>(getStatusForEvent(result, null, null));
            }
        }
        return null;
    }

    private Set<Object> getExecuteNext2(String command, String whoOrWhat, String pr) {
        String[] parameters = pr.split(" and ");

        switch (command) {
            case "get ip for user":
                return new HashSet<>(getIPsForUser(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get ip for date":
                return new HashSet<>(getUniqueIPsForDate(getDate(whoOrWhat), getDate(parameters[0]), getDate(parameters[1])));
            case "get ip for event":
                return new HashSet<>(getIPsForEvent(returnEvent(whoOrWhat), getDate(parameters[0]), getDate(parameters[1])));
            case "get ip for status":
                return new HashSet<>(getIPsForStatus(returnStatus(whoOrWhat), getDate(parameters[0]), getDate(parameters[1])));
            case "get user for ip":
                return new HashSet<>(getUsersForIP(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get user for date":
                return new HashSet<>(getUsersForDate(getDate(whoOrWhat), getDate(parameters[0]), getDate(parameters[1])));
            case "get user for event":
                return new HashSet<>(getUsersEvents(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get user for status":
                return new HashSet<>(getUsersStatus(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get date for ip":
                return new HashSet<>(getDateToIP(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get date for user":
                return new HashSet<>(getDatesForUser(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get date for event":
                return new HashSet<>(getDatesForEvent(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get date for status":
                return new HashSet<>(getDatesForStatus(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get event for ip":
                return new HashSet<>(getEventsForIP(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get event for user":
                return new HashSet<>(getEventsForUser(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get event for status":
                return new HashSet<>(getEventsForStatus(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get event for date":
                return new HashSet<>(getAllEvents(getDate(whoOrWhat), getDate(parameters[0]), getDate(parameters[1])));
            case "get status for ip":
                return new HashSet<>(getStatusForIPs(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get status for user":
                return new HashSet<>(getStatusForUser(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
            case "get status for date":
                return new HashSet<>(getStatusForDate(getDate(whoOrWhat), getDate(parameters[0]), getDate(parameters[1])));
            case "get status for event":
                return new HashSet<>(getStatusForEvent(whoOrWhat, getDate(parameters[0]), getDate(parameters[1])));
        }
        return null;
    }
}
