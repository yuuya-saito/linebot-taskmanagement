package com.ys.linebot.replier;

import java.util.function.Supplier;

import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

// フォローされた時用の返信クラス
public class Follow implements Supplier<Message> {

  @SuppressWarnings("unused")
private FollowEvent event;

  public Follow(FollowEvent event) {
    this.event = event;
  }

  @Override
  public Message get() {
    String text = String.format("追加ありがとうございます" + System.getProperty("line.separator") + "下のメニューから選択できます");
    return new TextMessage(text);
  }

}