#!/usr/bin/env python
# -*- coding: utf-8 -*-
#

"""
1. 邏輯：

 1.1 每日凌晨 03:30，把 ES 裡前一日的索引複製（合併）至當月的索引，並把前一日的索引刪除。
     例如：2016.11.21 凌晨 03:30 時，把 ES 裡的 aplog_aes3g-2016.11.20 複製到 aplog_aes3g-2016.11

 1.2 每日凌晨 03:30，把 ES 裡當月索引快照至 HDFS 之中。
     例如：2016.11.21 凌晨 03:30 時，把 ES 裡的 aplog_aes3g-2016.11 快照至 HDFS 的 snapshot-ap-aes3g-2016.11.20

 1.3 每月一日凌晨 03:30，完成上述兩項動作之後，把 ES 裡上上個月的索引和 HDFS 上上個月的快照刪除。
     例如：2016.12.01 凌晨 03:30 時，把 ES 裡的 aplog_aes3g-2016.10 刪除。

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
"""

import logging
import calendar

from elasticsearch import Elasticsearch
from datetime import datetime, date, timedelta
from dateutil.relativedelta import *

class ESIndexBackup:

    def __init__(self, es, logger):
        self.es = es
        self.logger = logger
        self.repository = 'backup' # Snapshot repository on HDFS
        self.snapshot_prefix = 'snapshot-'

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
        刪除 ES 昨天的索引與 HDFS 前天的 snapshot
        :param index: 昨天的索引名稱
        :param snapshot: 前天的snapshot名稱
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
            self.logger.info("index '%s' has been copied into '%s' successfully", srcIndex, destIndex)
        except Exception as e:
            self.logger.error(e)
            # print e
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
            self.logger.info("index '%s' has been snapshotted to '%s%s' successfully", indices, self.snapshot_prefix, snapshot)
        except Exception as e:
            self.logger.error(e)
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
            self.logger.info("index '%s' has been deleted successfully", snapshot)
        except Exception as e:
            self.logger.error(e)
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
            self.logger.info("index '%s' has been deleted successfully", index)
        except Exception as e:
            self.logger.error(e)
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

    # http://stackoverflow.com/questions/6290739/python-logging-use-milliseconds-in-time-format/7517430#7517430
    logger = logging.getLogger('ESIndexBackup')
    logger.setLevel(logging.INFO)

    # File Handler
    fh = logging.FileHandler('./ESIndexBackup.log')
    fh.setLevel(logging.INFO)
    fh_formatter = logging.Formatter('%(asctime)s.%(msecs)03d %(filename)s[line:%(lineno)d] %(levelname)s %(message)s', "%Y-%m-%d %H:%M:%S")
    fh.setFormatter(fh_formatter)

    # Console Handler
    ch = logging.StreamHandler()
    ch.setLevel(logging.ERROR)
    ch_formatter = logging.Formatter('%(filename)s[line:%(lineno)d] %(levelname)s %(message)s')
    ch.setFormatter(ch_formatter)

    # add the handlers to the logger
    logger.addHandler(fh)
    logger.addHandler(ch)

    eib = ESIndexBackup(es, logger)

    # today = datetime.strptime('2016 11 28', '%Y %m %d')
    today = date.today()
    todayDayOfMonth = today.strftime('%d');

    yesterday = today - timedelta(1)
    yesterdayDate = yesterday.strftime('%Y.%m.%d')
    yesterdayMonth = yesterdayDate[:-3]

    # 前天
    theDayBeforeYesterday = today - timedelta(2)
    theDayBeforeYesterdayDate = theDayBeforeYesterday.strftime('%Y.%m.%d')

    # 上上個月的最後一天（例如今天是 2016.11.28，則 上上個月的最後一天是 2016.09.30）
    # http://stackoverflow.com/questions/42950/get-last-day-of-the-month-in-python
    twoMonthsAgo = today - relativedelta(months=2)
    twoMonthsAgoYear = int(twoMonthsAgo.strftime("%Y"))
    twoMonthsAgoMonth = int(twoMonthsAgo.strftime("%-m"))
    twoMonthsAgoDay = int(calendar.monthrange(twoMonthsAgoYear, twoMonthsAgoMonth)[1])
    lastDayOfTwoMonthsAgo = "%d.%02d.%02d" % (twoMonthsAgoYear, twoMonthsAgoMonth, twoMonthsAgoDay)
    lastDayOfTwoMonthsAgoMonth = "%d.%02d" % (twoMonthsAgoYear, twoMonthsAgoMonth)

    # systems = ['aes3g', 'pos', 'wds', 'upcc']
    systems = ['pos']
    indexPrefix = 'aplog_'

    for system in systems:

        indexYesterday = indexPrefix + system + '-' + yesterdayDate
        indexTheDayBeforeYesterday = indexPrefix + system + '-' + theDayBeforeYesterdayDate
        indexYesterdayMonth = indexPrefix + system + '-' + yesterdayMonth
        indexTwoMonthsAgo = indexPrefix + system + '-' + lastDayOfTwoMonthsAgoMonth
        snapshotTwoMonthsAgo = indexPrefix + system + '-' + lastDayOfTwoMonthsAgo

        # 將昨天的 index 從 ES snapshot 至 HDFS
        if eib.backupIndex(indexYesterday, indexYesterdayMonth):
            logger.info("backupIndex() from %s to %s OK", indexYesterday, indexYesterdayMonth)

            # 將昨天的 index 從 ES 裡刪除
            if eib.deleteIndex(indexYesterday):
                logger.info("deleteIndex() %s OK", indexYesterday)
            else:
                logger.error("deleteIndex() %s FAILED", indexYesterday)

            # 將前天的 snapshot 從 HDFS 裡刪除，
            # 但是如果今天是 2 號，就刪除兩個月前的 index 與 snapshot。
            # http://stackoverflow.com/questions/394809/does-python-have-a-ternary-conditional-operator
            # snapToDelete = snapshotTwoMonthsAgo if (todayDayOfMonth == '2') else indexTheDayBeforeYesterday

            if (todayDayOfMonth == '2'):
                logger.info("Start deleting index '%s' and snapshot '%s'", indexTwoMonthsAgo, snapshotTwoMonthsAgo)
                eib.deleteIndex(indexTwoMonthsAgo)
                eib.deleteSnapshot(snapshotTwoMonthsAgo)
            else:
                logger.info("Start deleting index '%s'", indexTheDayBeforeYesterday)
                eib.deleteSnapshot(indexTheDayBeforeYesterday)

        else:
            logger.error("backupIndex() from %s to %s FAILED", indexYesterday, indexYesterdayMonth)


