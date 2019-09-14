import matplotlib.pyplot as plt
from model import conv_1d_time_stacked_model
from tensorflow import keras
import os
import numpy as np
from nybrecord import data_generator

if __name__ == "__main__":
    
    classes = {"一号":1, "二号":2, "三号":3, "四号":4, "全体":5,
    "前进":6, "后退":7, "上升":8, "下降":9, "向左":10, "向右":11, "左转":12, "右转":13, "停止":14,
    "搜索":15, "跟踪":16, "治疗":17, "拍照":18, "识别":19, "其他":0}

    model = conv_1d_time_stacked_model()
    model.summary()
    model.load_weights('trained_89_1530.h5')

    train_files = os.listdir('bindatanew/')
    train_files = ['bindatanew/'+train_file for train_file in train_files]

    val_split = 0.1
    test_split = 0.1
    np.random.shuffle(train_files)
    num_val = int(len(train_files)*val_split)
    num_test = int(len(train_files)*test_split)
    num_train = len(train_files) - num_val - num_test

    test_files = train_files[:num_test]
    train_files = train_files[num_test:]
    print('train on {0} datas, Verify on {1} datas, test on {2} datas'.format(num_train, num_val, num_test))
    batch_size = 32

    reduce_lr = keras.callbacks.ReduceLROnPlateau(monitor='val_loss', factor=0.2,patience=5, min_lr=0.00001)
    early_stop=keras.callbacks.EarlyStopping(monitor='val_loss', patience=10, verbose=0, mode='auto')

    train_history = model.fit_generator(generator=data_generator(train_files[:num_train], classes, batchsize=batch_size), 
                                        steps_per_epoch=max(1, num_train//batch_size), 
                                        epochs=100, verbose=1, callbacks=[reduce_lr, early_stop], 
                                        validation_data=data_generator(train_files[num_train:], classes, batchsize=batch_size),
                                        validation_steps=max(1, num_val//batch_size))


    model.save('trained_89_1530.h5')


    scores = model.evaluate_generator(generator=data_generator(test_files, classes, batchsize=num_test),
                                      steps=1)
    
    print("\t[Info] Accuracy of testing data = {:2.1f}%".format(scores[1]*100.0))

    plt.figure()
    plt.plot(train_history.history['loss'], linestyle='-', label = 'Train Loss')
    plt.plot(train_history.history['val_loss'], linestyle='--', label = 'Val Loss')
    plt.xlabel('Trained Times')
    plt.ylabel('Loss')
    plt.title('Loss Function')
    plt.legend()

    plt.figure()
    plt.plot(train_history.history['categorical_accuracy'], linestyle='-', label = 'Train accuracy')
    plt.plot(train_history.history['val_categorical_accuracy'], linestyle='--', label = 'Val accuracy')
    plt.xlabel('Trained Times')
    plt.ylabel('accuracy')
    plt.title('accuracy')
    plt.legend()

    plt.show()
