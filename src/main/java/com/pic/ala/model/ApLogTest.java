/**
 * AP Log 產生器（於 local 端執行，使用 log4j 的「Kafka appender」寫入 Kafka ）
 * 可以用這隻程式來做 performance benchmark
 *
 * enableSleep = false : running threads at full speed
 * enableSleep = true : suspend threads occasionally
 */
/*
  1. 先建立 indexes：

     curl -XPUT 'localhost:9200/aplog_aes3g-2016.05.25/?pretty'
     curl -XPUT 'localhost:9200/aplog_wds-2016.05.25/?pretty'
     curl -XPUT 'localhost:9200/aplog_upcc-2016.05.25/?pretty'
     curl -XPUT 'localhost:9200/aplog_pos-2016.05.25/?pretty'

  2. 關閉 refresh：

    curl -XPUT 'localhost:9200/aplog_*-2016.05.25/_settings?pretty' -d '{
     "index" : {
         "refresh_interval" : "-1"
    }}'

  3. 看一下目前 documents 總數，應該是 0 才對：

     curl -XGET 'localhost:9200/aplog_*-2016.05.25/_count?q=*&pretty'

  4. 開始執行這隻程式！建議至少跑 1 分鐘。

  5. 執行完畢後，再看一下 document 總數：

     curl -XGET 'localhost:9200/aplog_*-2016.05.25/_count?q=*&pretty'

  6. 執行段合併

     curl -XPUT 'localhost:9200/aplog_*-2016.05.25/_forcemerge?max_num_segments=5&pretty'

  7. 恢復 refresh_interval 的預設值：

     curl -XPUT 'localhost:9200/aplog_*-2016.05.25/_settings?pretty' -d '{
      "index" : {
          "refresh_interval" : "1s"
     }}'

  8. 或是刪除 indexes：

     curl -XDELETE 'localhost:9200/aplog_*-2016.05.25/?pretty'
 */

package com.pic.ala.model;

public class ApLogTest {

	public static void main(String[] args) {
		startAllThreads();
	}

	public static void startAllThreads() {
		boolean enableSleep = true;
		for (String sysID : ApLog.SYSTEMS) {
			(new BatchJobThread(new BatchJob(sysID, enableSleep))).start();
			(new UIActionThread(new UIAction(sysID, enableSleep))).start();
			(new TPIPASEventThread(new TPIPASEvent(sysID, enableSleep))).start();
		}
	}

}
