package com.pic.ala.spout;

import java.util.Map;
import java.util.Random;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class RandomLogSpout extends BaseRichSpout {

	private static final long serialVersionUID = 1L;
	SpoutOutputCollector _collector;
	Random _rand;

	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		_collector = collector;
		_rand = new Random();
	}

	public void nextTuple() {
		Utils.sleep(100);
		String[] logs = new String[] {
				"AESSNT1 $$ AES $$  2015-01-05 10:39:25.317 $$ 0.019    $$  JOB_START      $$ AbstractJobSender.service()             $$ 主程序       $$  程序開始   $$             $$ Job: MAT202 開始執行",
				"AESRCT1 $$ AES $$  2015-01-05 10:40:27.346 $$ 0.014    $$  FUNC_START     $$ MaImportServiceImpl.chkReceiveState()   $$ 子程序       $$ 程序開始    $$             $$ 系統確認檔案收檔狀態 開始",
				"AESRCT1 $$ AES $$  2015-01-05 10:40:28.346 $$ 0.014    $$  INFO           $$ MaImportServiceImpl.chkRuningFlag()     $$ 收送檔檢查   $$             $$             $$ 確認MA/MAT201/MA_STORE_SPEC_T收檔狀態",
				"AESRCT1 $$ AES $$  2015-01-05 10:40:28.346 $$ 0.014    $$  INFO           $$ MaImportServiceImpl.chkRuningFlag()     $$ 收送檔檢查   $$             $$             $$ 檢查 MA_STORE_SPEC_T 300秒x24次",
				"AESRCT1 $$ AES $$  2015-01-05 10:40:28.346 $$ 0.014    $$  INFO           $$ MaImportServiceImpl.chkRuningFlag()     $$ 收送檔檢查   $$             $$             $$ 檢查 MA_STORE_SPEC_T 收檔狀態仍未成功.WAITING... 重試第1次",
				"AESRCT1 $$ AES $$  2015-01-05 10:45:29.346 $$ 0.014    $$  INFO           $$ MaImportServiceImpl.chkRuningFlag()     $$ 收送檔檢查   $$             $$             $$ 檢查 MA_STORE_SPEC_T 收檔狀態仍未成功.WAITING... 重試第2次",
				"AESRCT1 $$ AES $$  2015-01-05 10:50:31.346 $$ 0.029    $$  ERROR          $$ MaImportServiceImpl.chkRuningFlag()     $$ 收送檔檢查   $$ 收送檔失敗  $$             $$ 收檔成功，檔名：MA/MAT201/MA_STORE_SPEC_T ",
				"AESRCT1 $$ AES $$  2015-01-05 10:50:53.346 $$ 0.014    $$  FUNC_END       $$ MaImportServiceImpl.chkReceiveState()   $$ 子程序       $$ 程序結束    $$             $$ 系統確認檔案收檔狀態 結束",
				"AESSNT1 $$ AES $$  2015-01-05 10:47:28.518 $$ 0.021    $$  JOB_END        $$ AbstractJobSender.service()             $$ 主程序       $$  程序結束   $$             $$ Job: MAT202 執行結束",
		};
		String log = logs[_rand.nextInt(logs.length)];
		_collector.emit(new Values(log));
	}

	@Override
	public void ack(Object id) {
	}

	@Override
	public void fail(Object id) {
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("log"));
	}

}
