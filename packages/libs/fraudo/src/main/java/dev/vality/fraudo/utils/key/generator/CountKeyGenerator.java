package dev.vality.fraudo.utils.key.generator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Function;

import static dev.vality.fraudo.FraudoPaymentParser.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CountKeyGenerator {

    public static <T> String generate(CountContext ctx, Function<String, T> resolve) {
        return CommonKeyGenerator.generateKeyGroupedFunction(ctx.STRING(),
                ctx.children.get(0),
                ctx.time_window(),
                ctx.group_by(),
                resolve);
    }

    public static <T> String generateSuccessKey(Count_successContext ctx, Function<String, T> resolve) {
        return CommonKeyGenerator.generateKeyGroupedFunction(ctx.STRING(),
                ctx.children.get(0),
                ctx.time_window(),
                ctx.group_by(),
                resolve);
    }

    public static <T> String generatePendingKey(Count_pendingContext ctx, Function<String, T> resolve) {
        return CommonKeyGenerator.generateKeyGroupedFunction(ctx.STRING(),
                ctx.children.get(0),
                ctx.time_window(),
                ctx.group_by(),
                resolve);
    }

    public static <T> String generateErrorKey(Count_errorContext ctx, Function<String, T> resolve) {
        if (ctx.STRING().size() == 2) {
            return CommonKeyGenerator.generateKeyGroupedTwoFieldFunction(ctx.STRING(0),
                    ctx.STRING(1),
                    ctx.children.get(0),
                    ctx.time_window(),
                    ctx.group_by(),
                    resolve);
        }
        return CommonKeyGenerator.generateKeyGroupedFunction(ctx.STRING(0),
                ctx.children.get(0),
                ctx.time_window(),
                ctx.group_by(),
                resolve);
    }

    public static <T> String generateChargebackKey(Count_chargebackContext ctx, Function<String, T> resolve) {
        return CommonKeyGenerator.generateKeyGroupedFunction(ctx.STRING(),
                ctx.children.get(0),
                ctx.time_window(),
                ctx.group_by(),
                resolve);
    }

    public static <T> String generateRefundKey(Count_refundContext ctx, Function<String, T> resolve) {
        return CommonKeyGenerator.generateKeyGroupedFunction(ctx.STRING(),
                ctx.children.get(0),
                ctx.time_window(),
                ctx.group_by(),
                resolve);
    }

}
