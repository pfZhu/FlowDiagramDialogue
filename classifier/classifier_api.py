#!/usr/bin/python
# -*- coding: utf-8 -*-
import json
from flask import Flask
from flask import request
from flask import redirect
from flask import jsonify
from classifier import Classifier
import traceback
app = Flask(__name__)



@app.route('/init' , methods=['POST'])
def init():
    classifier2class = request.values.get("classifier2class")
    output={}
    try:
        if classifier2class is not None:
            classifier2class=json.loads(classifier2class)
            Classifier(classifier2class)
            output["code"]=200
        else:
            output["code"] = 500
    except Exception, e:
        print traceback.format_exc()
        output["status"]=500
    return json.dumps(output)


@app.route('/predict' , methods=['POST'])
def predict():
    output = {}
    try:
        labeler = Classifier()
        print request.values

        classifier_num=request.values.get("classifier_num")
        message = request.values.get("message")
        message=message.strip()
        label=labeler.test(classifier_num,message)
        output["message"] = message
        output["label"]= label
        output["code"] = 200
    except Exception, e:
        print traceback.format_exc()
        output["code"] = 500
    return json.dumps(output)

@app.route('/user/<name>')
def user(name):
    return'<h1>hello, %s</h1>' % name

if __name__ =='__main__':
    app.run(host='0.0.0.0',debug=True, port=5000,threaded=True)