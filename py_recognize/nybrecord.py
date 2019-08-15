import struct, os
import soundfile as sf 
import sounddevice as sd 
import time
from queue import Queue
from sklearn import preprocessing
import numpy as np
import tensorflow as tf
def read_data_from_file(path, data_length):
    ''' 从二进制文件读取short数组
        输入: 二进制文件路径, 读取short数组长度
        输出:长度为data_length的short数组
    '''
    if not os.path.exists(path):
        print('error: files not found!!!')
        print('please check path:', path)
        return None
    with open(path,'rb') as binfile:
        context = binfile.read(data_length*2)
        if data_length*2 > len(context):
            print('warning: data_length is bigger than binfile data!!!')
            print('    return data with binfile data_length' + str(len(context)//2))
            data_length = len(context)//2
        data = struct.unpack('>%dh' % (data_length),  context)
    data = np.array(data,dtype=np.short)
    return data

def read_raw_data_from_file(path, data_length):
    ''' 从二进制文件读取short数组
        输入: 二进制文件路径, 读取short数组长度
        输出:长度为data_length * 2 的byte数组
    '''
    if not os.path.exists(path):
        print('error: files not found!!!')
        print('please check path:', path)
        return None
    with open(path,'rb') as binfile:
        context = binfile.read(data_length*2)
    return context

def write_short_to_file(path, short_datas):
    ''' short数组写入文件
        输入: 二进制文件路径, short数组
        输出: data_length
    '''
    # if not os.path.exists(path):
    #     print('error: files not found!!!')
    #     print('please check path:', path)
    #     return None
    short_datas = np.array(short_datas, dtype=np.short)
    with open(path,'wb') as binfile:
        data = struct.pack('>%dh' % (len(short_datas)), *short_datas)
        binfile.write(data)
        print('success to write to ' + path)
    return len(data)

def paly_pcm(file_path, channels=1, samplerate=16000):
    ''' 播放pcm语音'''
    sig, fs = sf.read(file_path , channels=channels, samplerate=samplerate, 
            format='RAW', subtype='PCM_16', endian='BIG') 
    sd.play(sig, fs) 
    time.sleep(fs/samplerate+1)

##############################################################
# 训练数据处理
##############################################################
def get_train_data(filepath, words):
    ''' 从文件得到训练数据 x, y'''
    data_x = read_data_from_file(filepath, 16000)
    filepath = filepath.split("/")[-1]
    data_y = words.get(filepath[:2], 0)
    data_y = tf.keras.utils.to_categorical(data_y, num_classes=len(words))
    return np.array(data_x), np.array(data_y)

# 已resize到16000长度
def get_one_s(file_name):
    ''' 将数据裁到1s, 前面没声音的去掉, 后面的补零'''
    chunk = 1600
    threshold = 0.5
    chunks_before = 2
    num = read_data_from_file(file_name, 32000)
    num = np.array(num)
    num_temp = preprocessing.scale(num)
    num_temp = np.array(num_temp)
    
    q = Queue()

    x = np.array([])
    #y = np.array(chunk*np.array(range(20)))
    for i in range(int(32000/chunk)):
        frame_test = num_temp[chunk*i:chunk*(i+1)]
        frame = num[chunk*i:chunk*(i+1)]
        q.put(frame)
        # frame = abs(frame)
        
        while(q.qsize() > chunks_before):
            q.get()

        temp = (frame_test.var())
        print(chunk*i,temp)
        if temp > threshold:
            # print(chunk*i)
            while(q.qsize() > 0):
                x = np.append(x,np.array(q.get()))
            
            for j in range(i+1,i+9):
                start_position = chunk*j
                end_position = min(chunk*(j+1),len(num))
                to_be_append = (num[start_position:end_position])
                x = np.append(x,to_be_append)
            break
    zeros = np.zeros(16000)
    zeros[:x.shape[0]] = x
    x = np.array(zeros, dtype=np.short)
    x = x.reshape(-1)
    # x = x / 32767
    return x

def data_generator(filenames, words, batchsize = 32):
    ''' 数据生成器, 训练用'''
    n = len(filenames)
    i = 0
    while True:
        x = []
        y = []
        for b in range(batchsize):
            if i == 0:
                np.random.shuffle(filenames)
            tx, ty = get_rand_data(filenames[i], words)
            x.append(tx)
            y.append(ty)
            i = (i+1) % n
        x = np.array(x)
        x = x/32767
        y = np.array(y)
        yield x, y
   
def get_rand_data(filepath, words, add_noise=True, change_volume=True):
    ''' 数据增强
        已有: 添加白噪声,改变音量
        TODO: 特定噪声叠加, 时移
    '''
    x, y = get_train_data(filepath, words)

    # 添加白噪声
    if add_noise:
        maxvalue = x.max()
        scale = rand(0, 0.1)
        x = x + scale * maxvalue * np.random.randint(-1, 1,size=x.shape)
    # 改变音量
    if change_volume:
        scale = rand(.7, 1.5)
        x = x * scale
        x[x>32767] = 32767
    return x, y

def rand(a=0, b=1):
    ''' 生成(a,b)区间的随机数'''
    return np.random.rand()*(b-a) + a

if __name__ == "__main__":
    # num = read_data_from_file('Shortdata2019-07-28_11-52-52-453.data', 32000)
    # print(num[0:3])
    # print(num[-3:])
    pass

