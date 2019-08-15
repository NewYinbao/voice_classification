import tensorflow as tf 
from tensorflow.python.keras.models import load_model
from tensorflow.python.keras.utils import CustomObjectScope

def Hswish(x):
    return x * tf.nn.relu6(x + 3) / 6
def relu6(x):
    return tf.nn.relu6(x)

if __name__ == "__main__":

    with CustomObjectScope({'Hswish': Hswish, 'relu6':relu6}):
        h5file = 'trained_89_1530.h5'
        kerasmodel = load_model(h5file)
        tflite_model = tf.lite.TFLiteConverter.from_keras_model(kerasmodel)
        # 参数量化
        # tflite_model.optimizations = [tf.lite.Optimize.OPTIMIZE_FOR_SIZE]

        # 整数量化
        # tflite_model.optimizations = [tf.lite.Optimize.DEFAULT]
        # tflite_model.representative_dataset = representative_data_gen

        model= tflite_model.convert()
        with open('tflite/record_89_1500.tflite','wb') as f:
            f.write(model)

    print('successiful')