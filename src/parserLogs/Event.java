package parserLogs;


public enum Event {
    LOGIN,              // пользователь залогинился
    DOWNLOAD_PLUGIN,    // пользователь скачал плагин
    WRITE_MESSAGE,      // пользователь отправил сообщение
    SOLVE_TASK,         // пользователь попытался решить задачу
    DONE_TASK           // пользователь решил задачу
}