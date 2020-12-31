package com.ys.linebot;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.ys.linebot.replier.TaskDelSupplier;
import com.ys.userstatus.PseudoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

@RestController
public class Push {

	@Autowired
	private TaskRepositiry taskRepositiry;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private static final Logger log = LoggerFactory.getLogger(Push.class);

	private final LineMessagingClient client;

	@Autowired
	public Push(LineMessagingClient lineMessagingClient) {
		this.client = lineMessagingClient;
	}

	@GetMapping("test") // 生存確認用
	public String hello(HttpServletRequest request) {
		return "Get from " + request.getRequestURL();
	}

	@GetMapping("del") // 時間が過ぎたタスク削除用
	public String del(HttpServletRequest request) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		List<Task> resultset = taskRepositiry.findAll();
		for (int i = 0; i < resultset.size(); i++) {
			if (resultset.size() == 0) {
				// 何もしない
			}

			TransactionStatus status = transactionManager.getTransaction(def);
			ZoneId zone = ZoneId.systemDefault();
			ZonedDateTime zonedDateTime = ZonedDateTime.of(resultset.get(i).getSpecify_time(), zone);
			ZonedDateTime localDateTime = ZonedDateTime.of(LocalDateTime.now(), zone);
			Date spt = Date.from(zonedDateTime.toInstant());
			Date lct = Date.from(localDateTime.toInstant());

			if (spt.before(lct)) {
				taskRepositiry.deleteByTitle(resultset.get(i).getTitle());
				transactionManager.commit(status);

			}
		}

		return null;
	}

	// タスクをpush
	//etMapping("timetone")
	@Scheduled(cron = "0 */1 * * * *", zone = "Asia/Tokyo")
	public LocalDateTime pushTask() {
		List<Task> resultset = taskRepositiry.findAll();
		if (resultset.size() == 0) {
			return null;
		}
		for (int i = 0; i < resultset.size(); i++) {
			// LocalDateTimeからDateに変換する
			ZoneId zone = ZoneId.systemDefault();
			ZonedDateTime zonedDateTime = ZonedDateTime.of(resultset.get(i).getSpecify_time(), zone);
			ZonedDateTime localDateTime = ZonedDateTime.of(LocalDateTime.now(), zone);
			Instant spt = zonedDateTime.toInstant();
			Instant lct = localDateTime.toInstant();
			Date spt5 = Date.from(spt);
			// DateからCalenderに変換する
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(spt5);
			calendar.add(Calendar.MINUTE, -1);// 1分引く ここで何分前にPushするか決める DBからもってこれそう

			// Dateから書式を変更する（秒の切り捨て）
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");

			if (sdf.format(calendar.getTime()).equals(sdf.format(Date.from(lct)))) {
				String userid = resultset.get(i).getUserId();
				String text = resultset.get(i).getTitle();

				try {
					PushMessage pMsg = new PushMessage(userid, new TextMessage(text + "のタスクまであと１分です！"));
					BotApiResponse resp = client.pushMessage(pMsg).get();
					log.info("Sent messages: {}", resp);
					return resultset.get(i).getSpecify_time();
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}

			}
		}
		return null;

	}
}
