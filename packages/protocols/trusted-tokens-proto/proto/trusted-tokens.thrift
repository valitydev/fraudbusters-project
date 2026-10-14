namespace java dev.vality.trusted.tokens
namespace erlang trusted_tokens

typedef string CardToken
typedef string ConditionTemplateName
typedef string CurrencySymbolicCode
typedef i64 TransactionsSum
typedef i32 TransactionsCount

exception InvalidRequest {
    1: required list<string> errors
}

exception ConditionTemplateNotFound {
    1: optional string message
}

exception ConditionTemplateAlreadyExists {
    1: optional string message
}

enum YearsOffset {
    current_year = 0
    current_with_last_years = 1
    current_with_two_last_years = 2
}

struct ConditionTemplateRequest {
    1: required ConditionTemplateName name
    2: required ConditionTemplate template
}

struct ConditionTemplate {
    1: optional PaymentsConditions payments_conditions
    2: optional WithdrawalsConditions withdrawals_conditions
}

struct PaymentsConditions {
    1: required list<Condition> conditions
}

struct WithdrawalsConditions {
    1: required list<Condition> conditions
}

struct Condition {
    1: required CurrencySymbolicCode currency_symbolic_code
    2: required YearsOffset years_offset
    3: optional TransactionsSum sum
    4: required TransactionsCount count
}



service TrustedTokens {

        bool IsTokenTrusted (1: CardToken card_token, 2: ConditionTemplate condition_template) throws (
            1: InvalidRequest ex1)

        bool IsTokenTrustedByConditionTemplateName
            (1: CardToken card_token, 2: ConditionTemplateName condition_template_name) throws (
                1: InvalidRequest ex1
                2: ConditionTemplateNotFound ex2)

        void CreateNewConditionTemplate (1: ConditionTemplateRequest condition_template_request) throws (
            1: InvalidRequest ex1
            2: ConditionTemplateAlreadyExists ex2)

}
