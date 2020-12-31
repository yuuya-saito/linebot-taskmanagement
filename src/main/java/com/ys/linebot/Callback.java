package com.ys.linebot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.ys.linebot.replier.DatetimeSupplier;
import com.ys.linebot.replier.Follow;
import com.ys.linebot.replier.Omikuji;
import com.ys.linebot.replier.TaskDelSupplier;
import com.ys.linebot.replier.TaskRegConfirmSupplier;
import com.ys.linebot.replier.TaskRegSupplier;
import com.ys.userstatus.PseudoSession;
import com.ys.userstatus.Status;

@LineMessageHandler
@Service
public class Callback {

	@Autowired
	private TaskRepositiry taskRepositiry;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private final LineMessagingClient client;

	@Autowired
	public Callback(LineMessagingClient lineMessagingClient) {
		this.client = lineMessagingClient;
	}

	private static final Logger log = LoggerFactory.getLogger(Callback.class);

	// フォローイベントに対応する
	@EventMapping
	public Message handleFollow(FollowEvent event) {
		Follow follow = new Follow(event);
		return follow.get();
	}

	Task t = new Task();

	// 文章で話しかけられたとき（テキストメッセージのイベント）に対応する
	@EventMapping
	public List<Message> handleMessage(MessageEvent<TextMessageContent> event) {

		String id = UUID.randomUUID().toString();// ３６字の疑似乱数を生成する
		TextMessageContent tmc = event.getMessage();
		String text = tmc.getText();

		final String userId = event.getSource().getUserId();

		try {

			// 初めてアクセスするユーザーの場合、mapに値を入れる
			if (Objects.isNull(PseudoSession.getStatus(userId))) {
				PseudoSession.putStatus(userId, new Status());
			}

			if (PseudoSession.readContext(userId).equals("0")) {

				if (text.equals("登録")) {
					PseudoSession.updateContext(userId, "1");
					return Arrays.asList(new TaskRegSupplier().get());
				}

				else if (text.equals("おみくじ")) {
					PseudoSession.updatePlace(userId, "");
					PseudoSession.updateContext(userId, "0");
					return Arrays.asList(new Omikuji().get());

				} else if (text.equals("一覧")) {
					PseudoSession.updatePlace(userId, "");
					PseudoSession.updateContext(userId, "0");
					List<Task> resultset = taskRepositiry.findByUserId(userId);
					if (resultset.size() == 0) {
						return Arrays.asList(new TextMessage("タスクはありません"));
					}

					for (int i = 0; i < resultset.size(); i++) {
						ZoneId zone = ZoneId.systemDefault();
						ZonedDateTime zonedDateTime = ZonedDateTime.of(resultset.get(i).getSpecify_time(), zone);
						ZonedDateTime localDateTime = ZonedDateTime.of(LocalDateTime.now(), zone);
						Date spt = Date.from(zonedDateTime.toInstant());
						Date lct = Date.from(localDateTime.toInstant());

						if (spt.after(lct)) {
							DateTimeFormatter formmater = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)H時m分",
									Locale.JAPANESE);
							String sldt = resultset.get(i).getSpecify_time().format(formmater);
							try {
								PushMessage pMsg = new PushMessage(resultset.get(i).getUserId(),
										new TextMessage("[日時]" + System.getProperty("line.separator") + sldt
												+ System.getProperty("line.separator") + "[タスク名]"
												+ System.getProperty("line.separator") + resultset.get(i).getTitle()));
								BotApiResponse resp = client.pushMessage(pMsg).get();
								log.info("Sent messages: {}", resp);
							} catch (InterruptedException | ExecutionException e) {
								throw new RuntimeException(e);
							}
						}
					}
					return Arrays.asList(new TextMessage("タスクは以上です"));

				} else if (text.equals("削除")) {
					PseudoSession.updatePlace(userId, "");
					PseudoSession.updateContext(userId, "4");
					List<Task> resultset = taskRepositiry.findAll();
					if (resultset.size() == 0) {
						return Arrays.asList(new TextMessage("タスクはありません"));
					}

					for (int i = 0; i < resultset.size(); i++) {
						ZoneId zone = ZoneId.systemDefault();
						ZonedDateTime zonedDateTime = ZonedDateTime.of(resultset.get(i).getSpecify_time(), zone);
						ZonedDateTime localDateTime = ZonedDateTime.of(LocalDateTime.now(), zone);
						Date spt = Date.from(zonedDateTime.toInstant());
						Date lct = Date.from(localDateTime.toInstant());

						if (spt.after(lct)) {
							DateTimeFormatter formmater = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)H時m分",
									Locale.JAPANESE);
							String sldt = resultset.get(i).getSpecify_time().format(formmater);
							try {
								PushMessage pMsg = new PushMessage(resultset.get(i).getUserId(),
										new TextMessage("[日時]" + System.getProperty("line.separator") + sldt
												+ System.getProperty("line.separator") + "[タスク名]"
												+ System.getProperty("line.separator") + resultset.get(i).getTitle()));
								BotApiResponse resp = client.pushMessage(pMsg).get();
								log.info("Sent messages: {}", resp);
							} catch (InterruptedException | ExecutionException e) {
								throw new RuntimeException(e);
							}
						}
					}
					return Arrays.asList(new TextMessage("タスク一覧です"), new TaskDelSupplier().get());

				} else {
					PseudoSession.updatePlace(userId, "");
					PseudoSession.updateContext(userId, "0");
					return Arrays.asList(new TextMessage("下のメニューから選択してね！"));
				}

			} else if (PseudoSession.readContext(userId).equals("1")) {

				if (text.equals("はい")) {
					t.setId(id);
					t.setUserId(event.getSource().getUserId());
					PseudoSession.updatePlace(userId, text);
					PseudoSession.updateContext(userId, "2");
					return Arrays.asList(new DatetimeSupplier().get());
				} else {
					PseudoSession.updatePlace(userId, "");
					PseudoSession.updateContext(userId, "0");
					return Arrays.asList(new TextMessage("またお呼びください"));
				}

			} else if (PseudoSession.readContext(userId).equals("2")) {

				if (!("やめる".equals(text)) && !("いいえ".equals(text))) {
					t.setTitle(text);
					PseudoSession.updatePlace(userId, "");
					PseudoSession.updateContext(userId, "3");
					DateTimeFormatter formmater = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)H時m分", Locale.JAPANESE);
					String sldt = t.getSpecify_time().format(formmater);

					return Arrays.asList(new TextMessage("以下の内容で登録します" + System.getProperty("line.separator") + sldt
							+ "に　'" + t.getTitle() + "'　のタスク"), new TaskRegConfirmSupplier().get());

				} else {
					PseudoSession.updatePlace(userId, "");
					PseudoSession.updateContext(userId, "0");
					return Arrays.asList(new TextMessage("登録をキャンセルしました"));
				}

			} else if (PseudoSession.readContext(userId).equals("3")) {

				if (text.equals("はい")) {
					ZoneId zone = ZoneId.systemDefault();
					ZonedDateTime zonedDateTime = ZonedDateTime.of(t.getSpecify_time(), zone);
					ZonedDateTime localDateTime = ZonedDateTime.of(LocalDateTime.now(), zone);
					Date spt = Date.from(zonedDateTime.toInstant());
					Date lct = Date.from(localDateTime.toInstant());

					if (spt.after(lct)) {
						PseudoSession.updatePlace(userId, text);
						PseudoSession.updateContext(userId, "0");

						taskRepositiry.save(t);
						return Arrays.asList(new TextMessage("登録しました"));
					} else {
						PseudoSession.updatePlace(userId, "");
						PseudoSession.updateContext(userId, "0");
						return Arrays.asList(new TextMessage("現在の日時より前を選択することはできません"));
					}
				} else {
					PseudoSession.updatePlace(userId, "");
					PseudoSession.updateContext(userId, "0");
					return Arrays.asList(new TextMessage("登録をキャンセルしました"));
				}
			} else if (PseudoSession.readContext(userId).equals("4")) {

				if (text.equals("はい")) {
					PseudoSession.updatePlace(userId, text);
					PseudoSession.updateContext(userId, "5");
					return Arrays.asList(new TextMessage("削除するタスクのタイトルを入力して下さい"));
				} else {
					PseudoSession.updatePlace(userId, "");
					PseudoSession.updateContext(userId, "0");
					return Arrays.asList(new TextMessage("またお呼びください"));
				}

			} else if (PseudoSession.readContext(userId).equals("5")) {
				PseudoSession.updatePlace(userId, "");
				PseudoSession.updateContext(userId, "0");
				DefaultTransactionDefinition def = new DefaultTransactionDefinition();
				TransactionStatus status = transactionManager.getTransaction(def);
				List<Task> resultset = taskRepositiry.deleteByTitle(text);
				if (resultset.size() == 0) {
					return Arrays.asList(new TextMessage("該当するタスクはありません"));
				} else {
					transactionManager.commit(status);
				}
				return Arrays.asList(new TextMessage("削除しました"));
			}

		} catch (Exception e) {
			log.error("rollbacked", e);
		}
		return null;
	}

	@EventMapping
	public List<Message> handlePostBack(PostbackEvent event) {

		LocalDateTime ldt = LocalDateTime.parse(event.getPostbackContent().getParams().toString(), DateTimeFormatter
				.ofPattern("['{']['d']['a']['t']['e']['t']['i']['m']['e']['=']yyyy-MM-dd['T']HH:mm['}']"));

		DateTimeFormatter formmater = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)H時m分", Locale.JAPANESE);
		String sldt = ldt.format(formmater);

		t.setSpecify_time(ldt);
		t.setCreated_at(LocalDateTime.now());
		final String text = event.getPostbackContent().getData();
		switch (text) {
		case "DT":
			return Arrays.asList(new TextMessage(sldt));// 日時確認用
		}
		return Arrays.asList(new TextMessage("?"));
	}

}