#!/usr/bin/env python
# -*- coding: utf-8 -*-
#

"""
1. 邏輯：

 1.1 每日凌晨 02:00，把 ES 裡前一日的索引複製（合併）至當月的索引，並把前一日的索引刪除。
     例如：2016.11.21 凌晨 02:00 時，把 ES 裡的 aplog_aes3g-2016.11.20 複製到 aplog_aes3g-2016.11

 1.2 每日凌晨 02:00，把 ES 裡當月索引快照至 HDFS 之中。
     例如：2016.11.21 凌晨 02:00 時，把 ES 裡的 aplog_aes3g-2016.11 快照至 HDFS 的 snapshot-ap-aes3g-2016.11.20

 1.3 每月一日凌晨 02:00，完成上述兩項動作之後，把 ES 裡上個月的索引刪除。
     例如：2016.12.01 凌晨 02:00 時，把 ES 裡的 aplog_aes3g-2016.11 刪除。

2. References:

 2.1 https://elasticsearch-py.readthedocs.io/en/master/api.html
 2.2 https://tryolabs.com/blog/2015/02/17/python-elasticsearch-first-steps/
 2.3 https://docs.python.org/2/howto/logging-cookbook.html

3. Some commands:

 3.1 List all snapshots:
     curl 'hdpr01wn01:9200/_snapshot/backup/*?pretty'

4. TODO:

 4.1 E-mail
 4.2 Delete old indices
"""

import logging

from elasticsearch import Elasticsearch
from datetime import date, timedelta

class ESIndexBackup:

    def __init__(self, es, logger):
        self.es = es
        self.logger = logger
        self.snapshot_prefix = 'snapshot-'

    def backupIndex(self, index, srcIndex, destIndex):
        """
        備份索引（使用 reindex API 的 copy 功能）
        :param srcIndex: 來源索引名稱
        :param destIndex: 目標索引名稱
        :return: 成功回傳 True; 失敗回傳 False
        """
        if self.copyIndex(srcIndex, destIndex):
            self.logger.info('copyIndex() OK: %s', srcIndex)
            if self.snapshot(destIndex, srcIndex):
                self.logger.info('snapshot() OK: %s', srcIndex)
                return True
            else:
                self.logger.error('snapshot() FAILED: %s', srcIndex)
                return False
        else:
            self.logger.error('copyIndex() FAILED: %s', srcIndex)
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
            self.es.reindex(body = {"source": { "index": srcIndex },
                         "dest": { "index": destIndex } })
            self.logger.info("index '%s' has been copied into '%s' successfully", srcIndex, destIndex)
        except Exception as e:
            self.logger.error(e)
            # print e
            return False

        return True

    # 要設定足夠長的 master_timeout
    # 由於 snapshot 是 incremental 的，所以 snapshot 應該備份「每月索引」
    def snapshot(self, indices, snapshot):
        """
        對索引進行快照
        :param indices: 要進行快照的索引 (string)
        :param snapshot: 快照的名稱
        :return: 成功回傳 True; 失敗回傳 False
        """
        try:
            self.es.snapshot.create('backup', self.snapshot_prefix + snapshot,
                               body = {"indices": indices, "ignore_unavailable": True, "include_global_state": False},
                               master_timeout = '10s',
                               wait_for_completion = True)
            self.logger.info("index '%s' has been snapshotted to '%s%s' successfully", indices, self.snapshot_prefix, snapshot)
        except Exception as e:
            self.logger.error(e)
            return False

        return True

if __name__ == '__main__':

    # Global options: ignore, request_timeout, response filtering(filter_path)
    es = Elasticsearch(
        ['hdpr01wn01', 'hdpr01wn02', 'hdpr01wn03', 'hdpr01wn04', 'hdpr01wn05'],
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

    yesterdayDate = '2016.11.22'
    # yesterday = date.today() - timedelta(1)
    #yesterdayDate = yesterday.strftime('%Y.%m.%d')
    month = yesterdayDate[:-3]

    indices = ['aes3g', 'pos', 'wds', 'upcc']
    index_prefix = 'aplog_'

    for index in indices:
        srcIndex = index_prefix + index + '-' + yesterdayDate
        destIndex = index_prefix + index + '-' + month
        eib.backupIndex(index, srcIndex, destIndex)
