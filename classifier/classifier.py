#!/usr/bin/python
# -*- encoding: utf-8 -*-
import sys
reload(sys)
sys.setdefaultencoding("utf-8")
import pandas as pd
import jieba
import re
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import Pipeline
from sklearn import metrics
import numpy as np
import pickle
# Requirements:
# change label name to 'class'
# pip install xlrd
# pip install jieba
# pip install flask


def seg_sent(question_raw):
    seg_list = jieba.cut(question_raw)
    question = " ".join(seg_list)
    return question

# def init():
#     excel_path = 'class.xlsx'
#     data = pd.ExcelFile(excel_path, encoding='utf-8')
#     df = data.parse("Sheet1")
#     question_raw = df['question'].tolist()
#     label = df['class'].tolist()
#     text_clf = Pipeline([('vect', CountVectorizer(decode_error='ignore')),
#                          ('tfidf', TfidfTransformer()),
#                          ('clf', MultinomialNB()),
#                          ])
#     label_names = set(label)
#     question = []
#     for ques in question_raw:
#         seg_list = seg_sent(ques)
#         question.append(seg_list)
#
#     train_X, test_X, train_y, test_y = train_test_split(question,
#                                                         label,
#                                                         test_size=0.1,
#                                                         random_state=0)
#
#     print len(train_X), len(train_y), len(test_X), len(test_y)
#     text_clf.fit(train_X, train_y)
#
#
#     predicted = text_clf.predict(train_X)
#     print(metrics.classification_report(train_y, predicted, target_names=label_names))
#     with open('classifier.pickle', 'wb') as fw:
#         pickle.dump(text_clf, fw)

def add_dic_value(dic,key,value):
    value=value.strip()
    if value is not "":
        if dic.has_key(key):
            dic[key].append(value)
        else:
            dic[key]=[value]

#后续改成通用的
def get_cls_data_dic():
    excel_path = './data/训练数据.xlsx'
    data = pd.ExcelFile(excel_path)
    df = data.parse("Sheet1",)
    cls = df['分类'].tolist()
    data = df['客户回答语料'].tolist()
    dic={}
    for i in range(0,len(cls)):
        if re.match(".*e\d+",cls[i]):
            cls_num=re.findall(".*e(\d+)",cls[i])
            if len(cls_num)>0:
                cls_num=cls_num[0]
            for line in data[i].split("\n"):
                add_dic_value(dic,int(cls_num),seg_sent(line))
    return dic


def train_save_model(classifier, train_x, train_y):
    text_clf = Pipeline([('vect', CountVectorizer(decode_error='ignore')),
                         ('tfidf', TfidfTransformer()),
                         ('clf', MultinomialNB()),
                         ])
    text_clf.fit(train_x, train_y)
    with open('model/model_classifier_'+classifier+'.pickle', 'wb') as fw:
        pickle.dump(text_clf, fw)


class Classifier:
    def __init__(self,classifier2class=None):
        self.text_clf = None
        if classifier2class is not None:
            cls_data_dic=get_cls_data_dic()
            for classifier,cls_list in classifier2class.items():
                train_x=[]
                train_y=[]
                cls_max_num=0
                for cls in cls_list:
                    data=cls_data_dic[cls]
                    cls_max_num=max(cls_max_num,len(data))
                for cls in cls_list:
                    num=0
                    data=cls_data_dic[cls]
                    for item in data:
                        train_x.append(item)
                        train_y.append(cls)
                        num+=1

                    # augment and balance data
                    if num<cls_max_num:
                        for k1 in range(0,len(data)):
                            if num<cls_max_num:
                                for k2 in range(k1+1,len(data)):
                                    train_x.append(data[k1]+" "+data[k2])
                                    train_y.append(cls)
                                    num+=1
                                    if num>=cls_max_num:
                                        break
                    if num<cls_max_num:
                        k=2
                        while num<cls_max_num:
                            for t in range(0,len(data)):
                                tmp=""
                                for m in range(0,k):
                                    tmp+=data[t]+" "
                                train_x.append(tmp.strip())
                                train_y.append(cls)
                                num+=1
                                if num>=cls_max_num:
                                    break
                            k+=1
                train_save_model(classifier,train_x,train_y)

    def test(self, classifier, sent):
        sent=sent.strip()
        with open('model/model_classifier_'+classifier+'.pickle', 'rb') as fr:
            self.text_clf = pickle.load(fr)
        docs_test = seg_sent(sent)
        predicted = self.text_clf.predict([docs_test]).tolist()
        return predicted[0]
