
namespace java dev.vality.damsel.fraudbusters_notificator
namespace erlang fraudbusters_notificator

const i32 VALIDATION_ERROR = 1

typedef i64 ID
typedef ID NotificationID
typedef ID NotificationTemplateID
typedef string ChannelID
/**
 * Отметка во времени согласно RFC 3339.
 *
 * Строка должна содержать дату и время в UTC в следующем формате:
 * `2016-03-22T06:12:27Z`.
 */
typedef string Timestamp

enum NotificationStatus {
    CREATED
    ACTIVE
    ARCHIVE
}

struct Notification {
    1: optional NotificationID id;
    2: required string name;
    3: required string subject;
    4: optional Timestamp created_at;
    5: optional Timestamp updated_at;
    6: required string period;
    7: required string frequency;
    8: required string channel;
    9: required NotificationStatus status;
    10: required NotificationTemplateID template_id;
}

enum ChannelType {
    mail
    telegram
}

struct Channel {
    1: required ChannelID name;
    2: optional Timestamp created_at;
    3: required ChannelType type;
    4: required string destination;
}

union ValidationResponse {
    1: list<string> errors;
    2: string result;
}

struct NotificationListResponse {
    1: required list<Notification> notifications;
    2: optional NotificationID continuationId;
}

struct ChannelListResponse {
    1: required list<Channel> channels;
    2: optional ChannelID continuationId;
}

struct ChannelTypeListResponse {
    1: required list<string> channel_types;
}

struct NotificationTemplate {
    1: optional NotificationTemplateID id;
    2: required string name;
    3: required string type;
    4: required string skeleton;
    5: required string query_text;
    6: required string basic_params;
    7: optional Timestamp created_at;
    8: optional Timestamp updated_at;
}

struct NotificationTemplateListResponse {
    1: list<NotificationTemplate> notification_templates;
}

struct Filter {
    1: optional string search_field;
}

struct Page {
    1: required i64 size
    2: optional string continuation_id
}

/**
* Общее исключение сервиса работы с нотификациями
**/
exception NotificationServiceException {
    1: required i32 code
    2: optional string reason
}

/**
* Интерфейс для работы с нотификациями
*/
service NotificationService {

    Notification create(1: Notification notification) throws (1: NotificationServiceException ex);

    void remove(1: NotificationID id);

    void updateStatus(1: NotificationID id, 2: NotificationStatus notification_status);

    ValidationResponse validate(1: Notification notification);

    NotificationListResponse getAll(1: Page page, 2: Filter filter);

    Notification getById(1: NotificationID id);
}


/**
* Интерфейс для работы с каналами нотфиикаций
*/
service ChannelService {

    Channel create(1: Channel channel);

    void remove(1: ChannelID id);

    ChannelListResponse getAll(1: Page page, 2: Filter filter);

    ChannelTypeListResponse getAllTypes();

    Channel getById(1: ChannelID id);

}

/**
* Интерфейс для работы с шаблонами нотификаций
*/
service NotificationTemplateService {

    NotificationTemplateListResponse getAll();

}

