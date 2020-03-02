package parserLogs;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.nio.file.Paths;
import java.util.Date;

import static parserLogs.Event.LOGIN;
import static parserLogs.Status.OK;


// Лог файл имеет следующий формат:
// ip username date event status
//
// Где:
// ip - ip адрес с которого пользователь произвел событие.
// user - имя пользователя (одно или несколько слов разделенные пробелами).
// date - дата события в формате day.month.year hour:minute:second.
// event - одно из событий:
// LOGIN - пользователь залогинился,
// DOWNLOAD_PLUGIN - пользователь скачал плагин,
// WRITE_MESSAGE - пользователь отправил сообщение,
// SOLVE_TASK - пользователь попытался решить задачу,
// DONE_TASK - пользователь решил задачу.
//      Для событий SOLVE_TASK и DONE_TASK существует дополнительный параметр,
//      который указывается через пробел, это номер задачи.
//      status - статус:
//      OK - событие выполнилось успешно,
// FAILED - событие не выполнилось,
// ERROR - произошла ошибка.
//
//  Пример строки из лог файла:
// "146.34.15.5 Eduard Petrovich Morozko 05.01.2021 20:22:55 DONE_TASK 48 FAILED".

public class Solution {
    public static void main(String[] args){
        LogParser logParser = new LogParser(Paths.get("/Users/samuilolegovich/Documents/" +
                "JAVA Project/JavaRushTasks/JavaRushTasks/4.JavaCollections/src/com/javarush/" +
                "task/task39/task3913/logs"));

        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern("dd.MM.yyyy HH:mm:ss");

        Date dateParserLogInfo = null;
        Date dateParserLogInfo2 = null;

        try {
            dateParserLogInfo = format.parse("12.12.2013 21:56:30");
            dateParserLogInfo2 = format.parse("03.01.2014 21:56:31");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println();
        System.out.println();
        System.out.println("Количество уникальных IP ---- \n"
                + logParser.getNumberOfUniqueIPs(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Уникальные IP ---- \n"
                + logParser.getUniqueIPs(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Уникальныe IP с заданным статусом ---- \n"
                + logParser.getIPsForStatus(OK,null, null));
        System.out.println();
        System.out.println();

        System.out.println("Уникальныe IP с заданным событием ---- \n"
                + logParser.getIPsForEvent(LOGIN,null, null));
        System.out.println();
        System.out.println();

        System.out.println("Уникальныe IP с заданным пользователем ---- \n"
                + logParser.getIPsForUser("Eduard Petrovich Morozko",null, null));
        System.out.println();
        System.out.println();

        System.out.println("Все пользователи ---- \n"
                + logParser.getAllUsers());
        System.out.println();
        System.out.println();

        System.out.println("Все пользователи с даддного IP за определенный период ---- \n"
                + logParser.getUsersForIP("127.0.0.1", null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает пользователей которые скачали плагин ---- \n"
                + logParser.getDownloadedPluginUsers(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Воозвращает пользователей которые отправили сообщение ---- \n"
                + logParser.getWroteMessageUsers(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает множество пользователей которые решили любую задачу ---- \n"
                + logParser.getDoneTaskUsers(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает пользователей которые были залогинены за выбранный период ---- \n"
                + logParser.getLoggedUsers(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает пользователей которые решали любую задачу ---- \n"
                + logParser.getSolvedTaskUsers(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает пользователей которые решали задачу с номером ---- \n"
                + logParser.getSolvedTaskUsers(null, null, 18));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает пользователей которые решили задачу с номером ---- \n"
                + logParser.getDoneTaskUsers(null, null, 15));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает количество событий от определенного пользователя ---- \n"
                + logParser.getNumberOfUserEvents("Amigo",null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает даты когда любое событие закончилось ошибкой (статус ERROR) ---- \n"
                + logParser.getDatesWhenErrorHappened(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает даты когда любое событие не выполнилось (статус FAILED) ---- \n"
                + logParser.getDatesWhenSomethingFailed(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает дату когда пользователь залогинился впервые за указанный период\n"
                + "Если такой даты в логах нет - null ---- \n"
                + logParser.getDateWhenUserLoggedFirstTime("Amigg",null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает дату когда пользователь впервые решил определенную задачу.\n"
                + "Если такой даты в логах нет - null ---- \n"
                + logParser.getDateWhenUserDoneTask("Vasya Pupkin", 15, null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает дату когда пользователь впервые попытался решить определенную задачу.\n"
                + "Если такой даты в логах нет - null ---- \n"
                + logParser.getDateWhenUserSolvedTask("Amigo", 18, null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает даты когда пользователь скачал плагин ---- \n"
                + logParser.getDatesWhenUserDownloadedPlugin("Amigo",  null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает даты когда определенный пользователь произвел определенное событие ---- \n"
                + logParser.getDatesForUserAndEvent("Amigo", LOGIN,null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает даты когда пользователь написал сообщение ---- \n"
                + logParser.getDatesWhenUserWroteMessage("Eduard Petrovich Morozko", null, null));
        System.out.println();
        System.out.println();


        System.out.println("Возвращает количество событий за указанный период ---- \n"
                + logParser.getNumberOfAllEvents(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает все события за указанный период ---- \n"
                + logParser.getAllEvents(null, null).toString());
        System.out.println();
        System.out.println();

        System.out.println("Возвращает события, которые происходили с указанного IP  ---- \n"
                + logParser.getEventsForIP("146.34.15.5",null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает события, которые инициировал определенный пользователь ---- \n"
                + logParser.getEventsForUser("Amigo", null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает события, которые не выполнились ---- \n"
                + logParser.getFailedEvents(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает события, которые завершились ошибкой ---- \n"
                + logParser.getErrorEvents(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает количество попыток решить определенную задачу ---- \n"
                + logParser.getNumberOfAttemptToSolveTask(15, null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает количество успешных решений определенной задачи ---- \n"
                + logParser.getNumberOfSuccessfulAttemptToSolveTask( 15, null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает мапу (номер_задачи : количество_попыток_решить_ее) ---- \n"
                + logParser.getAllSolvedTasksAndTheirNumber(null, null));
        System.out.println();
        System.out.println();

        System.out.println("Возвращает мапу (номер_задачи : сколько_раз_ее_решили) ---- \n"
                + logParser.getAllDoneTasksAndTheirNumber(null, null));
        System.out.println();
        System.out.println();

        System.out.println(logParser.execute("get ip"));
        System.out.println();
        System.out.println();

        System.out.println(logParser.execute("get user"));
        System.out.println();
        System.out.println();

        System.out.println(logParser.execute("get date"));
        System.out.println();
        System.out.println();

        System.out.println(logParser.execute("get event"));
        System.out.println();
        System.out.println();

        System.out.println(logParser.execute("get status"));
        System.out.println();
        System.out.println();

        System.out.println(logParser.execute("get date for event = \"LOGIN\" and date between \"09.03.2047 05:04:07\" and \"29.2.31020 5:4:7\""));
        System.out.println();
        System.out.println();
    }
}