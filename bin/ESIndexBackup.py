#!/usr/bin/env python
# -*- coding: utf-8 -*-
#

"""
1. 邏輯：

    最多只保留最近兩個月內之索引（本月與上個月）

    1.1 每日凌晨，備份索引至 HDFS：
        1.1.1 將昨日索引與月索引合併 (使用 reindex API 的 copy 功能)
        1.1.2 將月索引快照至 HDFS 之中
        1.1.3 例如：2016.11.21 凌晨 03:30 時
            1.1.3.1 把昨日索引「aplog_aes3g-2016.11.20」合併到月索引「aplog_aes3g-2016.11」（此索引內會有 2016.11.01～2016.11.20 的索引）
            1.1.3.2 把月索引「aplog_aes3g-2016.11」快照至 HDFS 的「snapshot-ap-aes3g-2016.11.20」（此快照內最後會有 2016.11.01～2016.11.20 的索引）

    1.2 確認上述動作都執行成功後，才進行索引與快照的 HouseKeeping：
        1.2.1 刪除昨日索引
        1.2.2 如果今天是 2 號，就刪除兩個月前的索引與快照
        1.2.3 如果今天不是 2 號，就刪除前天快照
        1.2.4 例如：2016.12.02 凌晨
            1.2.4.1 刪除昨日索引「 aplog_aes3g-2016.12.01」、兩個月前索引「aplog_aes3g-2016.10」與兩個月前快照「snapshot-aplog_aes3g-2016.10.31」
        1.2.5 例如：2016.12.01 凌晨
            1.2.5.1 刪除昨日索引「 aplog_aes3g-2016.11.31」、前天快照「snapshot-aplog_aes3g-2016.11.30」

2. References:

 2.1 https://elasticsearch-py.readthedocs.io/en/master/api.html
 2.2 https://tryolabs.com/blog/2015/02/17/python-elasticsearch-first-steps/
 2.3 https://docs.python.org/2/howto/logging-cookbook.html

3. Some commands:

 3.1 List all snapshots:

     curl 'hdpr01wn01:9200/_snapshot/backup/*?pretty' or
     curl 'hdpr01wn01:9200/_cat/snapshots/backup?pretty'

 3.2 Delete snapshots;

     curl -XDELETE 'hdpr01wn01:9200/_snapshot/backup/snapshot-aplog_pos-2016.09.23?pretty'

4. TODO:

 4.1 E-mail
 4.2 HDFS

"""

import logging
import calendar

from elasticsearch import Elasticsearch
from datetime import datetime, date, timedelta
from dateutil.relativedelta import *

class ESIndexBackup:

    def __init__(self, es):
        self.es = es
        self.repository = 'backup' # Snapshot repository on HDFS
        self.snapshot_prefix = 'snapshot-'

        # http://stackoverflow.com/questions/6290739/python-logging-use-milliseconds-in-time-format/7517430#7517430
        self.logger = logging.getLogger('ESIndexBackup')
        self.logger.setLevel(logging.INFO)

        # File Handler
        fh = self.logging.FileHandler('./ESIndexBackup.log')
        fh.setLevel(logging.INFO)
        fh_formatter = logging.Formatter(
            '%(asctime)s.%(msecs)03d %(filename)s[line:%(lineno)d] %(levelname)s %(message)s', "%Y-%m-%d %H:%M:%S")
        fh.setFormatter(fh_formatter)

        # Console Handler
        ch = logging.StreamHandler()
        ch.setLevel(logging.ERROR)
        ch_formatter = logging.Formatter('%(filename)s[line:%(lineno)d] %(levelname)s %(message)s')
        ch.setFormatter(ch_formatter)

        # add the handlers to the logger
        self.logger.addHandler(fh)
        self.logger.addHandler(ch)

    def backupIndex(self, srcIndex, destIndex):
        """
        備份索引（使用 reindex API 的 copy 功能）
        :param srcIndex: 來源索引名稱
        :param destIndex: 目標索引名稱
        :return: 成功回傳 True; 失敗回傳 False
        """
        if self.copyIndex(srcIndex, destIndex):
            if self.createSnapshot(destIndex, srcIndex):
                return True
        return False

    def housekeepIndexAndSnapshot(self, index, snapshot):
        """
        刪除索引與 snapshot
        :param index: 索引名稱
        :param snapshot: snapshot名稱
        :return: 成功回傳 True; 失敗回傳 False
        """
        if self.deleteIndex(index):
            if self.deleteSnapshot(snapshot):
                return True
        return False

    # ignore 400 cause by IndexAlreadyExistsException when creating an index
    #es.indices.create(index='test-index', ignore=400)
    def copyIndex(self, srcIndex, destIndex):
        """
        將 srcIndex 複製（合併）到 destIndex
        :param srcIndex: 來源索引名稱
        :param destIndex: 目標索引名稱
        :return: 成功回傳 True; 失敗回傳 False
        """
        try:
            self.es.reindex(body = {"source": { "index": srcIndex },"dest": { "index": destIndex }},
                            refresh = False,
                            timeout = '3m',
                            wait_for_completion=True )
            self.logger.info("copyIndex() OK. '%s' --> '%s'", srcIndex, destIndex)
        except Exception as e:
            self.logger.error("copyIndex() FAILED. '%s' --> '%s': %s", srcIndex, destIndex, e)
            return False

        return True

    # 要設定足夠長的 master_timeout
    # 由於 snapshot 是 incremental 的，所以 snapshot 應該備份「每月索引」
    def createSnapshot(self, indices, snapshot):
        """
        對索引進行快照
        :param indices: 要建立快照的索引 (string)
        :param snapshot: 要建立的快照名稱
        :return: 成功回傳 True; 失敗回傳 False
        """
        try:
            self.es.snapshot.create(self.repository, self.snapshot_prefix + snapshot,
                               body = {"indices": indices, "ignore_unavailable": True, "include_global_state": False},
                               master_timeout = '120s',
                               wait_for_completion = True)
            self.logger.info("createSnapshot() OK. '%s' --> '%s%s'", indices, self.snapshot_prefix, snapshot)
        except Exception as e:
            self.logger.error("createSnapshot() FAILED. '%s' --> '%s%s': %s" , indices, self.snapshot_prefix, snapshot, e)
            return False

        return True


    def deleteSnapshot(self, snapshot):
        """
        刪除快照
        :param snapshot: 要刪除的快照名稱
        :return: 成功回傳 True; 失敗回傳 False
        """
        try:
            self.es.snapshot.delete(self.repository, self.snapshot_prefix + snapshot,
                               master_timeout = '120s')
            self.logger.info("deleteSnapshot() OK. '%s%s'", self.snapshot_prefix, snapshot)
        except Exception as e:
            self.logger.error("deleteSnapshot() FAILED. '%s%s': %s", self.snapshot_prefix, snapshot, e)
            return False

        return True

    def deleteIndex(self, index):
        """
        刪除索引
        :param index: 要刪除的索引
        :return: 成功回傳 True; 失敗回傳 False
        """
        try:
            self.es.indices.delete(index)
            self.logger.info("deleteIndex() OK. '%s'", index)
        except Exception as e:
            self.logger.error("deleteIndex() FAILED. '%s'", index)
            return False

        return True


if __name__ == '__main__':

    # Global options: ignore, request_timeout, response filtering(filter_path)
    es = Elasticsearch(
        ['hdpr01wn01', 'hdpr01wn02', 'hdpr01wn03', 'hdpr01wn04', 'hdpr01wn05'],
        # http://stackoverflow.com/questions/25908484/how-to-fix-read-timed-out-in-elasticsearch
        # 預設 10 分鐘!
        timeout=600,
        # sniff before doing anything
        sniff_on_start=True,
        # refresh nodes after a node fails to respond
        sniff_on_connection_fail=True,
        # and also every 60 senconds
        sniff_timeout=60
    )

    eib = ESIndexBackup(es)

    # today = datetime.strptime('2016 11 2', '%Y %m %d')
    today = date.today()
    todayDayOfMonth = today.strftime('%d');

    yesterday = today - timedelta(1)
    yesterdayDate = yesterday.strftime('%Y.%m.%d')
    yesterdayMonth = yesterdayDate[:-3]

    # 前天
    theDayBeforeYesterday = today - timedelta(2)
    theDayBeforeYesterdayDate = theDayBeforeYesterday.strftime('%Y.%m.%d')

    # 上上個月的最後一天，例如今天是 2016.11.28，則 上上個月的最後一天是 2016.09.30
    # http://stackoverflow.com/questions/42950/get-last-day-of-the-month-in-python
    twoMonthsAgo = today - relativedelta(months=2)
    twoMonthsAgoYear = int(twoMonthsAgo.strftime("%Y"))
    twoMonthsAgoMonth = int(twoMonthsAgo.strftime("%-m"))
    twoMonthsAgoDay = int(calendar.monthrange(twoMonthsAgoYear, twoMonthsAgoMonth)[1])
    lastDayOfTwoMonthsAgo = "%d.%02d.%02d" % (twoMonthsAgoYear, twoMonthsAgoMonth, twoMonthsAgoDay)
    lastDayOfTwoMonthsAgoMonth = "%d.%02d" % (twoMonthsAgoYear, twoMonthsAgoMonth)

    #systems = ['pos']
    systems = ['aes3g', 'pos', 'wds', 'upcc']
    indexPrefix = 'aplog_'

    for system in systems:

        indexYesterday = snapshotYesterday = indexPrefix + system + '-' + yesterdayDate
        indexTheDayBeforeYesterday = snapshotTheDayBeforeYesterday = indexPrefix + system + '-' + theDayBeforeYesterdayDate
        indexYesterdayMonth = indexPrefix + system + '-' + yesterdayMonth
        indexTwoMonthsAgo = indexPrefix + system + '-' + lastDayOfTwoMonthsAgoMonth
        snapshotTwoMonthsAgo = indexPrefix + system + '-' + lastDayOfTwoMonthsAgo

        # 將昨天的 index 從 ES snapshot 至 HDFS
        # (先將昨日的 index 與昨日的全月 index 合併，然後再對全月 index 做 snapshot)
        if eib.copyIndex(indexYesterday, indexYesterdayMonth):
            if eib.createSnapshot(indexYesterdayMonth, snapshotYesterday):
                # 將昨天的 index 從 ES 裡刪除
                eib.deleteIndex(indexYesterday)
                # 將前天的 snapshot 從 HDFS 裡刪除，
                # 但是如果今天是 2 號，就刪除兩個月前的 index 與 snapshot。
                if todayDayOfMonth == '2':
                    eib.deleteIndex(indexTwoMonthsAgo)
                    eib.deleteSnapshot(snapshotTwoMonthsAgo)
                else:
                    eib.deleteSnapshot(indexTheDayBeforeYesterday)

