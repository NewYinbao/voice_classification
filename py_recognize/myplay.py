# @Author: newyinbao
# @Date: 2019-09-14 16:22:49
# @Function: 播放 .pcm 文件
# @TODO: 
# @Last Modified by:   newyinbao
# @Last Modified time: 2019-09-14 16:22:49


from nybrecord import paly_pcm, read_data_from_file
import os
import matplotlib.pyplot as plt


if __name__ == "__main__":
    for filename in os.listdir('test/'):
        paly_pcm('test/' + filename )
        print(filename[:2])
        data = read_data_from_file('test/' + filename, 32000)
        plt.plot(range(len(data)), data)
        plt.show()
