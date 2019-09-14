# @Author: newyinbao
# @Date: 2019-09-14 17:31:17
# @Function: 测试模型效果
# @TODO: 
# @Last Modified by:   newyinbao
# @Last Modified time: 2019-09-14 17:31:17


import os
from model import conv_1d_time_stacked_model
from nybrecord import paly_pcm, read_data_from_file
import tensorflow as tf
import numpy as np
if __name__ == "__main__":
    
    classes = {"一号":1, "二号":2, "三号":3, "四号":4, "全体":5,
    "前进":6, "后退":7, "上升":8, "下降":9, "向左":10, "向右":11, "左转":12, "右转":13, "停止":14,
    "搜索":15, "跟踪":16, "治疗":17, "拍照":18, "识别":19, "其他":0}
    
    model = conv_1d_time_stacked_model()
    model.load_weights( 'trained_89_1530.h5')

    filepath = 'test/'
    for filename in os.listdir(filepath):
        print('now play: '+filename[:2])
        paly_pcm(filepath + filename )
        data = read_data_from_file(filepath + filename, 16000)
        data = data/32767
        data = tf.expand_dims(data,0)
        y = np.argmax( model.predict(data) )
        print('predict result: ' + list(classes.keys())[list(classes.values()).index(y)])
        print('******************************************')
