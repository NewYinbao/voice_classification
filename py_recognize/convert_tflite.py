# @Author: newyinbao
# @Date: 2019-09-14 16:21:59
# @Function: 将 .h5 转化成 .tflite
# @TODO: 
# @Last Modified by:   newyinbao
# @Last Modified time: 2019-09-14 16:21:59


import tensorflow as tf
from tensorflow.python.keras.models import load_model
from tensorflow.python.keras.utils import CustomObjectScope


def Hswish(x):
    return x * tf.nn.relu6(x + 3) / 6


def relu6(x):
    return tf.nn.relu6(x)


if __name__ == "__main__":

    with CustomObjectScope({'Hswish': Hswish, 'relu6': relu6}):

        # 需要转换的h5模型
        h5file = 'trained_89_1530.h5'

        kerasmodel = load_model(h5file)
        tflite_model = tf.lite.TFLiteConverter.from_keras_model(kerasmodel)

        # 参数量化
        # tflite_model.optimizations = [tf.lite.Optimize.OPTIMIZE_FOR_SIZE]

        # 整数量化
        # tflite_model.optimizations = [tf.lite.Optimize.DEFAULT]
        # tflite_model.representative_dataset = representative_data_gen

        model = tflite_model.convert()

        # 存储模型
        with open('tflite/record_89_1500.tflite', 'wb') as f:
            f.write(model)

    print('successiful')
