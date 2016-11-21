#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# 1. 邏輯：
#  1.1 每日凌晨 02:00，把 ES 裡前一日的索引複製（合併）至當月的索引，並把前一日的索引刪除。
#      例如：2016.11.21 凌晨 02:00 時，把 ES 裡的 aplog_aes3g-2016.11.20 複製到 aplog_aes3g-2016.11
#
#  1.2 每日凌晨 02:00，把 ES 裡當月索引快照至 HDFS 之中。
#      例如：2016.11.21 凌晨 02:00 時，把 ES 裡的 aplog_aes3g-2016.11 快照至 HDFS 的 snapshot-ap-aes3g-2016.11.20
#
#  1.3 每月一日凌晨 02:00，完成上述兩項動作之後，把 ES 裡上個月的索引刪除。
#      例如：2016.12.01 凌晨 02:00 時，把 ES 裡的 aplog_aes3g-2016.11 刪除。
#
# 2. References:
#  2.1 https://elasticsearch-py.readthedocs.io/en/master/api.html
#  2.2 https://tryolabs.com/blog/2015/02/17/python-elasticsearch-first-steps/
#  2.3 https://docs.python.org/2/howto/logging-cookbook.html
#
# 3. Some commands:
# 
#  3.1 List all snapshots:
#      curl 'hdpr01wn01:9200/_snapshot/backup/*?pretty'
#
# TODO:
#
#    1. E-mail
#    2. Delete old indices
#

import logging

from elasticsearch import Elasticsearch
from datetime import date, timedelta

indices = ['aes3g', 'pos', 'wds', 'upcc']

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

# logging.basicConfig(filename='./ESIndexBackup.log',
#                     level = logging.INFO,
#                     format='%(asctime)s.%(msecs)03d %(filename)s[line:%(lineno)d] %(levelname)s %(message)s',
#                     datefmt="%Y-%m-%d %H:%M:%S")


# yesterdayDate = '2016.11.15'
yesterday = date.today() - timedelta(1)
yesterdayDate = yesterday.strftime('%Y.%m.%d')
month = yesterdayDate[:-3]

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

def BackupIndices(indices):
    for index in indices:
        srcIndex = 'aplog_' + index + '-' + yesterdayDate
        destIndex = 'aplog_' + index + '-' + month
        if copyIndex(srcIndex, destIndex):
            logger.info('copyIndex() OK: %s', srcIndex)
            if snapshot(destIndex, srcIndex):
                logger.info('snapshot() OK: %s', srcIndex)
            else:
                logger.error('snapshot() FAILED: %s', srcIndex)
        else:
            logger.error('copyIndex() FAILED: %s', srcIndex)

# ignore 400 cause by IndexAlreadyExistsException when creating an index
#es.indices.create(index='test-index', ignore=400)

def copyIndex(srcIndex, destIndex):
    try:
        es.reindex(body = {"source": { "index": srcIndex },
                     "dest": { "index": destIndex } })
        logger.info("index '%s' has been copied into '%s' successfully", srcIndex, destIndex)
    except Exception as e:
        logger.error(e)
        # print e
        return False

    return True

# 要設定足夠長的 master_timeout
# 由於 snapshot 是 incremental 的，所以 snapshot 應該備份「每月索引」
def snapshot(index, snapshot):
    try:
        es.snapshot.create('backup', 'snapshot-' + snapshot,
                           body = {"indices": index, "ignore_unavailable": True, "include_global_state": False},
                           master_timeout = '10s',
                           wait_for_completion = True)
        logger.info("index '%s' has been snapshotted to 'snapshot-%s' successfully", index, snapshot)
    except Exception as e:
        logger.error(e)
        return False

    return True


BackupIndices(indices)
