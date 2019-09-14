# @Author: newyinbao
# @Date: 2019-09-14 16:21:31
# @Function: 检查数据是否正确
# @TODO: 
# @Last Modified by:   newyinbao
# @Last Modified time: 2019-09-14 16:21:31


import os
from nybrecord import get_one_s, get_train_data, get_rand_data, paly_pcm, read_data_from_file
from nybrecord import write_short_to_file
import matplotlib.pyplot as plt
if __name__ == "__main__":
    #%% 裁剪数据
    # opath = 'bindata/'
    # npath = 'bindatanew/'
    # for filename in os.listdir(opath):
    #     data = get_one_s(opath+filename)
    #     write_short_to_file(npath+filename, data)
        
    classes = {"一号":1, "二号":2, "三号":3, "四号":4, "全体":5,
    "前进":6, "后退":7, "上升":8, "下降":9, "向左":10, "向右":11, "左转":12, "右转":13, "停止":14,
    "搜索":15, "跟踪":16, "治疗":17, "拍照":18, "识别":19, "其他":0}

    #%% 测试数据增强用
    for filename in os.listdir('bindata/'):
        ofile = 'bindata/'+filename
        x, y = get_rand_data(ofile, classes,noise_type='optional_noize',noise_maxvalue=1,noise_path='test/')
        write_short_to_file('test.pcm', x)
        paly_pcm('test.pcm')
        data1 = read_data_from_file('test.pcm', 16000)
        data = read_data_from_file(ofile, 16000)
        fig1 = plt.figure()
        fig11 = fig1.add_subplot(211)
        fig11.plot(data)
        fig12 = fig1.add_subplot(212)
        fig12.plot(data1)
        plt.show()