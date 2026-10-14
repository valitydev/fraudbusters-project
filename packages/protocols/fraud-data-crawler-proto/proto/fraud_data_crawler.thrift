namespace java dev.vality.fraud_data_crawler
namespace erlang fraud_data_crawler

enum ListType {
    black
    white
    grey
    naming
}

/**
*  Кандидат на добавление в ЧБ списки
**/
struct FraudDataCandidate {
    1: required string type
    2: required string value
    3: required ListType listType
    4: required string source
    5: optional string merchantId
    6: optional string shopId
    7: required string batchId
}
