# @Author: newyinbao
# @Date: 2019-09-14 16:20:52
# @Function: 定义模型
# @TODO:
# @Last Modified by:   newyinbao
# @Last Modified time: 2019-09-14 16:20:52


import tensorflow.keras as keras
import tensorflow as tf
from tensorflow.keras.layers import Input, Reshape, Conv1D, Conv2D, BatchNormalization, MaxPool1D, Activation
# import tensorflow.keras.regularizers.l2 as l2
from tensorflow.keras import Model
from tensorflow.keras.layers import GlobalAveragePooling1D
from tensorflow.python.keras.layers.advanced_activations import Softmax


def relu6(x):
    return tf.nn.relu6(x)


def _reduce_conv(x, num_filters, k, strides=2, padding='same'):
    '''_reduce_conv 

    Args:
        x : [input]
        num_filters : [filters number]
        k : [kenal size]
        strides : [strides]. Defaults to 2.
        padding (str, optional): [padding type]. Defaults to 'same'.
    '''
    x = Conv1D(
        num_filters,
        k,
        padding=padding,
        use_bias=False,
        kernel_regularizer=tf.keras.regularizers.l2(0.00001))(x)
    x = BatchNormalization()(x)
    x = Activation(relu6)(x)
    x = MaxPool1D(pool_size=k, strides=strides, padding=padding)(x)
    return x


def _context_conv(x, num_filters, k, dilation_rate=1, padding='same'):
    
    x = Conv1D(
        num_filters,
        k,
        padding=padding,
        dilation_rate=dilation_rate,
        kernel_regularizer=tf.keras.regularizers.l2(0.00001),
        use_bias=False)(x)
    x = BatchNormalization()(x)
    x = Activation(relu6)(x)
    return x


def conv_1d_time_stacked_model(input_size=16000, num_classes=20):
    
    """ Creates a 1D model for temporal data.

    Note: Use only
    with compute_mfcc = False (e.g. raw waveform data).
    Args:
      input_size: How big the input vector is.
      num_classes: How many classes are to be recognized.

    Returns:
      Compiled keras model
    """
    input_layer = Input(shape=(input_size))
    x = input_layer
    x = Reshape([800, 20])(x)

    x = _context_conv(x, 32, 5)
    x = _context_conv(x, 64, 5)
    x = _reduce_conv(x, 64, 7, strides=2)

    x = _context_conv(x, 128, 7)
    x = _reduce_conv(x, 128, 7, strides=2)

    x = _context_conv(x, 128, 9)
    x = _reduce_conv(x, 256, 9, strides=2)

    x = _context_conv(x, 128, 7)
    x = _reduce_conv(x, 256, 7, strides=2)

    x = _context_conv(x, 128, 5)
    x = _reduce_conv(x, 128, 3, strides=2)

    x = _context_conv(x, 64, 3)
    x = _reduce_conv(x, 64, 3, strides=1)

    x = _context_conv(x, num_classes, 3)
    x = GlobalAveragePooling1D()(x)

    x = Softmax()(x)

    model = Model(input_layer, x, name='conv_1d_time_stacked')
    model.compile(
        optimizer=keras.optimizers.Adam(lr=1e-3),
        loss=keras.losses.categorical_crossentropy,
        metrics=[keras.metrics.categorical_accuracy])
    return model
