package dev.vality.fraudo;

import dev.vality.fraudo.FraudoPaymentParser.ParseContext;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.utils.ResultUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ListTest extends AbstractPaymentTest {

    @Test
    void whiteBlackListTest() throws Exception {
        InputStream resourceAsStream = ListTest.class.getResourceAsStream("/rules/whitelist.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        Mockito.when(inListFinder.findInWhiteList(anyList(), any())).thenReturn(true);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());

        resourceAsStream = ListTest.class.getResourceAsStream("/rules/blacklist.frd");
        parseContext = getParseContext(resourceAsStream);
        Mockito.when(inListFinder.findInBlackList(anyList(), any())).thenReturn(true);
        result = invokeParse(parseContext);

        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
        assertEquals(1, ResultUtils.getNotifications(result).size());
    }

    @Test
    void greyListTest() throws Exception {
        InputStream resourceAsStream = ListTest.class.getResourceAsStream("/rules/greyList.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        Mockito.when(inListFinder.findInGreyList(anyList(), any())).thenReturn(true);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void namingListTest() throws Exception {
        InputStream resourceAsStream = ListTest.class.getResourceAsStream("/rules/namingList.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        Mockito.when(inListFinder.findInList(anyString(), anyList(), any())).thenReturn(true);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }
}
