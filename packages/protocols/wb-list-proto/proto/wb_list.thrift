/**
 * Сервис управление списками.
 */

namespace java dev.vality.damsel.wb_list
namespace erlang wb_list

typedef string ID
typedef string Value
typedef i64 Count

/**
 * Отметка во времени согласно RFC 3339.
 *
 * Строка должна содержать дату и время в UTC в следующем формате:
 * `2016-03-22T06:12:27Z`.
 */
typedef string Timestamp

exception ListNotFound {}

enum Command {
    CREATE
    DELETE
}

enum ListType {
    black
    white
    grey
    naming
}

struct Row {

    // ID party
    1: optional ID party_id
    // ID  магазина
    2: optional ID shop_id

    // Тип списка
    3: required ListType list_type
    // Идентификатор списка
    4: required ID list_name
    // Значение в списке
    5: required Value value
    // Дополнительная информация
    6: optional RowInfo row_info

    // ID
    7: optional IdInfo id

}

struct Result {
    1: optional RowInfo row_info
}

union RowInfo {
    1: CountInfo count_info
}

struct CountInfo {
    // Количество вызовов между start_count_time и time_to_live
    1: required Count count
    // Время жизни в списке
    2: required Timestamp time_to_live
    // Время начала подсчета
    3: optional Timestamp start_count_time
}

// Данная структура используется на уровне сервиса для обновления списков через KAFKA
struct ChangeCommand {
    // Запись в списке
    1: required Row row
    // Команда на изменение
    2: required Command command
    3: optional Timestamp command_time
    4: optional UserInfo user_info
}

// Модель пользователя
struct UserInfo {
    1: required ID user_id
}

enum EventType {
    CREATED
    DELETED
}

union IdInfo {
    1: PaymentId payment_id

    // DEPRECATED and may not be supported
    2: P2pId p2p_id
}

struct PaymentId {
    // ID party
    1: optional ID party_id
    // ID  магазина
    2: optional ID shop_id
}

// DEPRECATED and may not be supported
struct P2pId {
    // ID party
    1: optional ID identity_id
}

// Данная структура используется для EventSink для обновления оффлайн части сервиса списков через KAFKA
struct Event {
    // Запись в списке
    1: required Row row
    // Тип события
    2: required EventType event_type

    3: optional Timestamp command_time
    4: optional UserInfo user_info
}

/**
* Интерфейс для проверки по черным и белым спискам
*/
service WbListService {

    /**
    * Проверяет существование в списке
    * если списка не существует то выбрасывается ListNotFound
    **/
    bool isExist(1: Row row)
        throws (1: ListNotFound ex1)

    /**
    * Проверяет существование в списке  всех записей
    * если списка не существует то выбрасывается ListNotFound
    **/
    bool isAllExist(1: list<Row> row)
        throws (1: ListNotFound ex1)

    /**
    * Проверяет существование хотя бы одной записи
    * если списка не существует то выбрасывается ListNotFound
    **/
    bool isAnyExist(1: list<Row> row)
        throws (1: ListNotFound ex1)

    /**
    * Проверяет, что нет ни одной записи в списках
    * если списка не существует то выбрасывается ListNotFound
    **/
    bool isNotOneExist(1: list<Row> row)
        throws (1: ListNotFound ex1)

    /**
    * Возвращает информацию по записи в списке
    * если списка не существует то выбрасывается ListNotFound
    **/
    Result getRowInfo(1: Row row)
        throws (1: ListNotFound ex1)

}
