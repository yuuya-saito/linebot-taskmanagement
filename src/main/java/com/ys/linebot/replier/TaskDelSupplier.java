package com.ys.linebot.replier;

import java.util.function.Supplier;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;

public class TaskDelSupplier implements Supplier<Message> {
	public Message get() {
		return new TemplateMessage("タスク削除",
						new ConfirmTemplate("タスクを削除しますか？", 
						new MessageAction("はい", "はい"), 
						new MessageAction("いいえ", "いいえ")));
	}

}
