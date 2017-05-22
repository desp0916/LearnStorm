package com.pic.ala.model;

/*

  https://www.elastic.co/guide/en/elasticsearch/reference/current/search-count.html
  https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-update-settings.html

  注意：將 refresh_interval 設為 -1（關閉 refresh）或調大一點，會提昇 ES 的效能。

  1. 先刪除所有 aplog indexes：

    curl -XDELETE 'hdpr01wn01:9200/aplog_*-2016.08.22/?pretty'

  2. 關閉 refresh：

    curl -XPUT 'hdpr01wn01:9200/aplog_*-2016.08.22/_settings?pretty' -d '{
     "index" : {
         "refresh_interval" : "-1"
    }}'

  3. 看一下目前 documents 總數，應該是 0 才對：

     curl -XGET 'hdpr01wn01:9200/aplog_*-2016.08.22/_count?q=*&pretty'

  4. 開始執行這隻程式！建議至少跑 1 分鐘。

  5. 執行完畢後，再看一下 document 總數：

     curl -XGET 'hdpr01wn01:9200/aplog_*-2016.08.22/_count?q=*&pretty'

  6. 執行段合併

     curl -XPUT 'hdpr01wn01:9200/aplog_*-2016.08.22/_forcemerge?max_num_segments=5&pretty'

  7. 恢復 refresh_interval 的預設值：

     curl -XPUT 'hdpr01wn01:9200/aplog_*-2016.08.22/_settings?pretty' -d '{
      "index" : {
          "refresh_interval" : "1s"
     }}'

  8. 或是刪除 indexes：

     curl -XDELETE 'hdpr01wn01:9200/aplog_*-2016.08.22/?pretty'
*/

/**
 * AP Log 產生器（於 local 端執行，使用 log4j 的「Kafka appender」寫入 Kafka ）
 * 可以用這隻程式來做 performance benchmark
 *
 * enableSleep = false : running threads at full speed
 * enableSleep = true : suspend threads occasionally
 */
public class ApLogTest {

	/**
	 * 第一個參數
	 *  all: 預設值，輸出全部欄位
	 *  partial: 僅輸出部分欄位：sysID, logType, apID, logTime, reqFrom,
	 *          reqAt, reqTo, who, reqAction, reqResult, msg, dataCnt
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		String outputFields = "all";
		if (args != null && args.length > 0) {
			outputFields = args[0].trim();
		}
		startAllThreads(outputFields);
	}

	public static void startAllThreads(final String outputFields) {
		Event.enableSleep = true;
		for (String sysID : ApLog.SYSTEMS) {
			new Command(new Event.BatchJob(sysID, outputFields)).start();
			new Command(new Event.UIAction(sysID, outputFields)).start();
			new Command(new Event.TPIPASEvent(sysID, outputFields)).start();
			new Command(new Event.APIEvent(sysID, outputFields)).start();
		}
	}

}