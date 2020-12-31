package com.ys.linebot.replier;

import java.util.function.Supplier;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.quickreply.QuickReply;
import com.linecorp.bot.model.message.quickreply.QuickReplyItem;

public class DatetimeSupplier implements Supplier<Message> {
    @Override
    public Message get() {
        final List<QuickReplyItem> items = Arrays.<QuickReplyItem>asList(
                QuickReplyItem.builder()
                              .action(DatetimePickerAction.OfLocalDatetime.builder()
                                      .label("日時選択ボタン")
                                      .data("DT")
                                      .initial(LocalDateTime.now())
                                      .min(LocalDateTime.now().minusYears(10l))
                                      .max(LocalDateTime.now().plusYears(10l))
                                      .build())
                              .build()
        );

        final QuickReply quickReply = QuickReply.items(items);

        return TextMessage
                .builder()
                .text("下のボタンから日時を選択した後" + System.getProperty("line.separator") + "タイトルを入力してください" + System.getProperty("line.separator") + "'やめる''いいえ'で登録を中止します")
                .quickReply(quickReply)
                .build();
    }
}